package com.zbs.de.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.support.TransactionTemplate;

import com.zbs.de.model.EventMaster;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryEventMaster;
import com.zbs.de.util.UtilDateAndTime;

/**
 * What the calendar greys out is what a save would refuse.
 *
 * <h2>What this is for</h2>
 *
 * Two things answer "can this day be booked". {@code canBookEvent} decides, on
 * save. {@code getAlreadyBookedDates} draws, on the customer's calendar. They
 * were separate statements of the same rule and they disagreed, in the
 * direction that matters most: the calendar greyed out a day the save would
 * have accepted.
 *
 * <h2>The failure this is written from</h2>
 *
 * A customer reopening their own booking was shown their own date as taken.
 * Their event counted towards the capacity that closed the day, so on a
 * Saturday holding two — theirs and somebody else's — the day they had booked
 * and paid a deposit against came back greyed out at 55% opacity, reading as
 * "no longer available". It was not a control, so pressing it did nothing, and
 * nothing on the screen said why. {@code canBookEvent} would have accepted that
 * same date without complaint, because it has always excluded the event being
 * edited.
 *
 * <p>
 * {@link #whatIsGreyedOutIsWhatWouldBeRefused()} is the property rather than
 * the symptom: it walks every day in a stretch of calendar and asks both, so a
 * third copy of the rule cannot appear without this failing.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class BookedDatesMatchWhatCanBeBookedIT {

	private static final String MARKER = "IT-BOOKEDDATES";

	/**
	 * A Saturday far enough out that nothing else in the suite is using it.
	 *
	 * <p>
	 * Deliberately not a Sunday or a Monday: those two are coupled by the rule,
	 * and the point of these first tests is the ordinary case. The coupling is
	 * covered by {@link EventDayCapacityTest} and swept again by
	 * {@link #whatIsGreyedOutIsWhatWouldBeRefused()}, which crosses several.
	 */
	private static final LocalDate SATURDAY = LocalDate.now(ZoneId.systemDefault())
			.plusYears(4).with(TemporalAdjusters.next(DayOfWeek.SATURDAY));

	@Autowired
	private com.zbs.de.service.impl.ServiceEventMasterImpl serviceEventMaster;

	@Autowired
	private RepositoryEventMaster repositoryEventMaster;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@BeforeAll
	static void requireDatabase() {
		String url = System.getenv().getOrDefault("TEST_DB_URL",
				"jdbc:postgresql://localhost:5432/diamond_ev_test");
		String user = System.getenv().getOrDefault("TEST_DB_USERNAME", "postgres");
		String password = System.getenv().getOrDefault("TEST_DB_PASSWORD", "postgres");

		try (Connection ignored = DriverManager.getConnection(url, user, password)) {
			// reachable
		} catch (Exception e) {
			Assumptions.abort("No test database at " + url + " — skipping. (" + e.getMessage() + ")");
		}
	}

	@AfterEach
	void removeSeed() {
		repositoryEventMaster.findAll().stream()
				.filter(e -> e.getTxtEventMasterName() != null
						&& e.getTxtEventMasterName().startsWith(MARKER))
				.forEach(repositoryEventMaster::delete);
	}

	private EventMaster anEventOn(LocalDate day) {
		EventMaster event = new EventMaster();
		event.setTxtEventMasterName(MARKER + " " + System.nanoTime());
		event.setTxtEventMasterCode(MARKER + "-" + System.nanoTime());
		event.setDteEventDate(Date.from(day.atStartOfDay(ZoneId.systemDefault()).toInstant()));
		event.setBlnIsActive(true);
		event.setBlnIsDeleted(false);
		return repositoryEventMaster.saveAndFlush(event);
	}

	@SuppressWarnings("unchecked")
	private List<String> greyedOut(Integer excludeEventId) {
		DtoResult result = serviceEventMaster.getAlreadyBookedDates(excludeEventId);
		assertThat(result.getTxtMessage())
				.as("the call failed, so nothing below is testing the rule")
				.isEqualTo("Success");
		return (List<String>) result.getResult();
	}

	private static String asShown(LocalDate day) {
		return UtilDateAndTime.mmddyyyyDateToString(
				Date.from(day.atStartOfDay(ZoneId.systemDefault()).toInstant()));
	}

	@Test
	@DisplayName("a day holding all it can is greyed out")
	void aFullDayIsGreyedOut() {
		assertThat(greyedOut(null))
				.as("the day was already closed before this test put anything on it")
				.doesNotContain(asShown(SATURDAY));

		anEventOn(SATURDAY);
		assertThat(greyedOut(null))
				.as("one event closed an ordinary day, which holds two")
				.doesNotContain(asShown(SATURDAY));

		anEventOn(SATURDAY);
		assertThat(greyedOut(null))
				.as("a day holding two was still offered to a third customer")
				.contains(asShown(SATURDAY));
	}

	@Test
	@DisplayName("the event being edited does not close the day it is already on")
	void anEditedEventDoesNotBlockItself() {
		/*
		  The reported fault. Two events on the Saturday: somebody else's, and
		  the one our customer is reopening. Counted together the day is full,
		  and their own date comes back greyed out — while the save path would
		  have accepted it, because that one leaves the event being edited out
		  of the count.
		*/
		anEventOn(SATURDAY);
		EventMaster mine = anEventOn(SATURDAY);

		assertThat(greyedOut(null))
				.as("the fixture did not fill the day, so this proves nothing")
				.contains(asShown(SATURDAY));

		assertThat(greyedOut(mine.getSerEventMasterId()))
				.as("a customer reopening their booking was shown their own date as taken")
				.doesNotContain(asShown(SATURDAY));
	}

	@Test
	@DisplayName("today is never offered, whatever the calendar holds")
	void todayIsNeverOffered() {
		// canBookEvent refuses a same-day booking outright, before capacity is
		// considered. A calendar that offers today offers something that cannot
		// be taken.
		assertThat(greyedOut(null)).contains(asShown(LocalDate.now(ZoneId.systemDefault())));
	}

	@Test
	@DisplayName("what is greyed out is what a save would refuse")
	void whatIsGreyedOutIsWhatWouldBeRefused() {
		/*
		  The property, rather than a case of it.

		  A Sunday can take a third event only while the Monday after it is
		  clear, and that Monday is then closed entirely — so the interesting
		  days are the ones either side of a weekend, and a fixture that fills
		  one Saturday says nothing about them. This fills a run of days to the
		  edge of the rule and then asks both answers about every day in it.

		  A third hand-written copy of the capacity rule cannot be added without
		  this failing, which is the point: there were three, and the one on the
		  calendar had drifted.
		*/
		LocalDate sunday = SATURDAY.plusDays(1);

		anEventOn(SATURDAY);
		anEventOn(SATURDAY);
		anEventOn(sunday);
		anEventOn(sunday);
		anEventOn(sunday);

		List<String> shown = greyedOut(null);

		for (LocalDate day = SATURDAY.minusDays(2); !day.isAfter(SATURDAY.plusDays(4)); day = day.plusDays(1)) {
			boolean greyed = shown.contains(asShown(day));
			/*
			  In a transaction because canBookEvent takes the day's row lock,
			  and that repository method is propagation = MANDATORY: it refuses
			  to run outside one rather than quietly taking a lock that is
			  released at the end of the statement. Every real caller reaches it
			  from inside a save.
			*/
			final LocalDate asked = day;
			boolean refused = Boolean.FALSE.equals(transactionTemplate.execute(status -> serviceEventMaster
					.canBookEvent(Date.from(asked.atStartOfDay(ZoneId.systemDefault()).toInstant()), null, null)
					.isAllowed()));

			assertThat(greyed)
					.as("%s (%s): the calendar %s it, the save %s it",
							day, day.getDayOfWeek(),
							greyed ? "closes" : "offers",
							refused ? "refuses" : "accepts")
					.isEqualTo(refused);
		}
	}
}
