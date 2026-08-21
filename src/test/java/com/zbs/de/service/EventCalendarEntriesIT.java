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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.zbs.de.model.EventMaster;
import com.zbs.de.model.dto.DtoEventCalendarEntry;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryEventMaster;

/**
 * What the admin calendar is sent.
 *
 * <p>
 * The calendar used to call {@code getAllDataAdminPortal}, which answers with
 * every event that has ever existed, in full — 624 KB across 296 events on a
 * development database, each carrying its food, decor, extras and running-order
 * collections. It drew boxes with a reference and a name on them.
 *
 * <p>
 * A calendar is the one list here that genuinely cannot be paginated: a month
 * view missing some of its events is worse than no month view at all. So the
 * saving has to come from the width of each row rather than the number of them,
 * and the assertion that matters is that the row stayed narrow.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class EventCalendarEntriesIT {

	private static final String MARKER = "IT-CALENDAR";

	/** Far enough out that nothing else in the suite is using these days. */
	private static final LocalDate LATER = LocalDate.now(ZoneOffset.UTC).plusYears(4);
	private static final LocalDate EARLIER = LATER.minusMonths(2);

	@Autowired
	private ServiceEventMaster serviceEventMaster;

	@Autowired
	private RepositoryEventMaster repositoryEventMaster;

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
				.filter(e -> e.getTxtEventMasterName() != null && e.getTxtEventMasterName().startsWith(MARKER))
				.forEach(repositoryEventMaster::delete);
	}

	private EventMaster seed(String name, LocalDate day, boolean deleted) {
		EventMaster event = new EventMaster();
		event.setTxtEventMasterName(MARKER + " " + name);
		event.setTxtEventMasterCode(MARKER + "-" + name);
		event.setDteEventDate(Date.from(day.atStartOfDay(ZoneOffset.UTC).toInstant()));
		event.setBlnIsDeleted(deleted);
		event.setBlnIsActive(true);
		return repositoryEventMaster.save(event);
	}

	@SuppressWarnings("unchecked")
	private List<DtoEventCalendarEntry> entries() {
		DtoResult result = serviceEventMaster.getCalendarEntries();
		assertThat(result.getTxtMessage()).isEqualTo("Success");
		return (List<DtoEventCalendarEntry>) (List<?>) result.getResulList();
	}

	private DtoEventCalendarEntry find(String name) {
		return entries().stream()
				.filter(e -> (MARKER + " " + name).equals(e.getTxtEventMasterName()))
				.findFirst()
				.orElse(null);
	}

	@Test
	@DisplayName("an event appears with a reference, a name and a date")
	void anEventAppears() {
		seed("walima", LATER, false);

		DtoEventCalendarEntry entry = find("walima");

		assertThat(entry).as("the seeded event is missing from the calendar").isNotNull();
		assertThat(entry.getTxtEventMasterCode()).isEqualTo(MARKER + "-walima");
		assertThat(entry.getSerEventMasterId()).isNotNull();
	}

	@Test
	@DisplayName("dates arrive in the format the calendar parses")
	void datesAreFormattedForTheClient() {
		/*
		 * The client does moment(dteEventDate, 'DD-MM-YYYY'). A date sent in any
		 * other shape does not throw — it produces an Invalid Date, and the event
		 * silently vanishes from the month rather than appearing on the wrong day.
		 * That is the failure worth pinning down, because nothing reports it.
		 */
		seed("nikkah", LATER, false);

		assertThat(find("nikkah").getDteEventDate())
				.matches("\\d{2}-\\d{2}-\\d{4}");
	}

	@Test
	@DisplayName("a deleted event is not on the calendar")
	void deletedEventsAreLeftOut() {
		seed("cancelled", LATER, true);

		assertThat(find("cancelled"))
				.as("a deleted event is being drawn on the calendar")
				.isNull();
	}

	@Test
	@DisplayName("entries come back oldest first, so the client does not have to sort")
	void entriesAreOrderedByDate() {
		seed("second", LATER, false);
		seed("first", EARLIER, false);

		List<String> names = entries().stream()
				.map(DtoEventCalendarEntry::getTxtEventMasterName)
				.filter(n -> n != null && n.startsWith(MARKER))
				.toList();

		assertThat(names).containsExactly(MARKER + " first", MARKER + " second");
	}

	@Test
	@DisplayName("the entry carries nothing beyond what a calendar draws")
	void theEntryStaysNarrow() {
		/*
		 * The point of the whole change, and the thing most likely to be undone
		 * by accident: somebody needs one more field on the calendar, adds the
		 * whole event DTO back, and the 624 KB returns. Asserting the shape means
		 * that is a deliberate decision with a failing test in front of it rather
		 * than a quiet regression.
		 */
		assertThat(DtoEventCalendarEntry.class.getDeclaredFields())
				.extracting(java.lang.reflect.Field::getName)
				.containsExactlyInAnyOrder("serEventMasterId", "txtEventMasterCode", "txtEventMasterName",
						"dteEventDate", "txtEventTypeName");
	}
}
