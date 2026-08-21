package com.zbs.de.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.zbs.de.model.CustomerMaster;
import com.zbs.de.model.EventMaster;
import com.zbs.de.model.dto.DtoEventSummary;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryCustomerMaster;
import com.zbs.de.repository.RepositoryEventMaster;

/**
 * What the "choose an event" step is sent.
 *
 * <p>
 * That screen is the second step of the customer journey and it used to be sent
 * every event the customer had ever had, in full — 1.1 MB across 278 events on a
 * development database, on a phone, on mobile data — to draw a list of names and
 * dates.
 *
 * <p>
 * The screen fix that came before this one bounded what was <em>drawn</em>. This
 * bounds what is <em>sent</em>, which is the half the customer's data allowance
 * pays for.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class EventSummariesByCustomerIT {

	private static final String MARKER = "IT-SUMMARY";

	/** Far enough out that nothing else in the suite is using these days. */
	private static final LocalDate DAY = LocalDate.now(ZoneOffset.UTC).plusYears(5);

	@Autowired
	private ServiceEventMaster serviceEventMaster;

	@Autowired
	private RepositoryEventMaster repositoryEventMaster;

	@Autowired
	private RepositoryCustomerMaster repositoryCustomerMaster;

	private CustomerMaster customer;
	private CustomerMaster otherCustomer;

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

	@BeforeEach
	void seedCustomers() {
		customer = newCustomer("ours");
		otherCustomer = newCustomer("theirs");
	}

	@AfterEach
	void removeSeed() {
		repositoryEventMaster.findAll().stream()
				.filter(e -> e.getTxtEventMasterName() != null && e.getTxtEventMasterName().startsWith(MARKER))
				.forEach(repositoryEventMaster::delete);

		repositoryCustomerMaster.findAll().stream()
				.filter(c -> c.getTxtCustCode() != null && c.getTxtCustCode().startsWith(MARKER))
				.forEach(repositoryCustomerMaster::delete);
	}

	private CustomerMaster newCustomer(String who) {
		CustomerMaster c = new CustomerMaster();
		c.setTxtCustCode(MARKER + "-" + who + "-" + System.nanoTime());
		c.setTxtCustName(MARKER + " " + who);
		return repositoryCustomerMaster.save(c);
	}

	private EventMaster seed(String name, CustomerMaster owner, boolean active, boolean deleted) {
		EventMaster event = new EventMaster();
		event.setTxtEventMasterName(MARKER + " " + name);
		event.setTxtEventMasterCode(MARKER + "-" + name);
		event.setDteEventDate(Date.from(DAY.atStartOfDay(ZoneOffset.UTC).toInstant()));
		event.setCustomerMaster(owner);
		event.setBlnIsActive(active);
		event.setBlnIsDeleted(deleted);
		event.setNumFormState(7);
		event.setIsEditAllowed(true);
		return repositoryEventMaster.save(event);
	}

	@SuppressWarnings("unchecked")
	private List<DtoEventSummary> summariesFor(CustomerMaster who) {
		DtoResult result = serviceEventMaster.getEventSummariesByCustomerId(who.getSerCustId());
		assertThat(result.getTxtMessage()).isEqualTo("Success");
		return (List<DtoEventSummary>) (List<?>) result.getResulList();
	}

	private List<String> namesFor(CustomerMaster who) {
		return summariesFor(who).stream().map(DtoEventSummary::getTxtEventMasterName).toList();
	}

	@Test
	@DisplayName("carries what the screen prints and what the choice needs")
	void carriesEverythingTheScreenUses() {
		seed("walima", customer, true, false);

		DtoEventSummary summary = summariesFor(customer).get(0);

		assertThat(summary.getTxtEventMasterName()).isEqualTo(MARKER + " walima");
		assertThat(summary.getDteEventDate()).matches("\\d{2}-\\d{2}-\\d{4}");
		assertThat(summary.getSerEventMasterId()).isNotNull();

		// The two that decide what happens when the card is pressed. Without
		// numFormState the journey cannot resume, and it would take a second
		// request to find out — which is the whole thing this is avoiding.
		assertThat(summary.getNumFormState()).isEqualTo(7);
		assertThat(summary.getIsEditAllowed()).isTrue();
	}

	@Test
	@DisplayName("shows only this customer's events")
	void oneCustomerCannotSeeAnother() {
		seed("ours", customer, true, false);
		seed("theirs", otherCustomer, true, false);

		assertThat(namesFor(customer)).containsExactly(MARKER + " ours");
	}

	@Test
	@DisplayName("leaves out deleted and inactive events, as the full list did")
	void matchesTheOldFilter() {
		/*
		 * The narrow query has to agree with findActiveEventMasterByCustomerId
		 * about which events exist. If it did not, a customer would see a
		 * different set of bookings from the one the rest of the journey works
		 * with — and the difference would be invisible until they opened one.
		 */
		seed("live", customer, true, false);
		seed("deleted", customer, true, true);
		seed("inactive", customer, false, false);

		assertThat(namesFor(customer)).containsExactly(MARKER + " live");
	}

	@Test
	@DisplayName("newest first, so the booking being worked on is at the top")
	void newestFirst() {
		// By id, not by event date. A booking started this week may be for next
		// summer, and ordering by date buries it behind everything already booked.
		seed("older", customer, true, false);
		seed("newer", customer, true, false);

		assertThat(namesFor(customer)).containsExactly(MARKER + " newer", MARKER + " older");
	}

	@Test
	@DisplayName("a customer with no events gets an empty list, not a failure")
	void noEventsIsNotAnError() {
		assertThat(summariesFor(customer)).isEmpty();
	}

	@Test
	@DisplayName("no customer id is refused rather than answered with everybody's events")
	void aMissingCustomerIsRefused() {
		DtoResult result = serviceEventMaster.getEventSummariesByCustomerId(null);

		assertThat(result.getTxtMessage()).isEqualTo("Customer ID Is Required");
		assertThat(result.getResulList()).isNull();
	}

	@Test
	@DisplayName("the summary carries nothing beyond what the screen uses")
	void theSummaryStaysNarrow() {
		/*
		 * The point of the whole change, and the way it regresses: somebody needs
		 * one more field on this screen and puts the full event DTO back, and the
		 * 1.1 MB returns. Asserting the shape makes that a decision with a failing
		 * test in front of it.
		 */
		assertThat(DtoEventSummary.class.getDeclaredFields())
				.extracting(java.lang.reflect.Field::getName)
				.containsExactlyInAnyOrder("serEventMasterId", "txtEventMasterCode", "txtEventMasterName",
						"dteEventDate", "txtEventTypeName", "txtNumberOfGuests", "numNumberOfGuests",
						"isEditAllowed", "numFormState");
	}
}
