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

import com.zbs.de.model.CustomerMaster;
import com.zbs.de.model.EventMaster;
import com.zbs.de.model.EventType;
import com.zbs.de.model.dto.DtoEventMaster;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryCustomerMaster;
import com.zbs.de.repository.RepositoryEventMaster;
import com.zbs.de.repository.RepositoryEventType;
import com.zbs.de.util.UtilDateAndTime;

/**
 * A save that does not mention the date must not remove it.
 *
 * <h2>The failure</h2>
 *
 * Every save path wrote {@code parseDateFromClient(dto.getDteEventDate())}
 * straight onto the booking, and that parser answers null for a date that was
 * never sent just as it does for one it cannot read. So a request carrying
 * {@code {serEventMasterId, serCustId, serEventTypeId}} and nothing else
 * cleared the date of a booked wedding and answered 200.
 *
 * <p>
 * The damage is not only the missing date. The capacity check on the next save
 * reads {@code newDate != null && entity.getDteEventDate() != null}, so a
 * booking whose stored date has been wiped skips it — and can then be put on a
 * day that is already full. That is the same hazard
 * {@code refuseUnreadableDate} closes from the other end, reached by omission
 * instead of by a typo.
 *
 * <p>
 * Both frontends send the whole booking on every save, which is why nobody has
 * hit this. That is not a reason to leave it: it means the endpoint's behaviour
 * has never been exercised by anything except its two well-behaved callers.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class EventDateSurvivesAPartialSaveIT {

	private static final String MARKER = "IT-DATEKEEP";

	/** Far enough out that nothing else in the suite is using it. */
	private static final LocalDate DAY = LocalDate.now(ZoneOffset.UTC).plusYears(7);

	@Autowired
	private ServiceEventMaster serviceEventMaster;

	@Autowired
	private RepositoryEventMaster repositoryEventMaster;

	@Autowired
	private RepositoryCustomerMaster repositoryCustomerMaster;

	@Autowired
	private RepositoryEventType repositoryEventType;

	@Autowired
	private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

	private CustomerMaster customer;
	private EventType eventType;

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

	/*
	  Cleaned up by which customer owns the booking, not by the booking's name.

	  The first draft filtered on the name, as the neighbouring suites do, and
	  it left rows behind: the payload under test carries no name, and the save
	  — which replaces rather than patches — duly wrote null over it. So the
	  fixture could no longer recognise its own event, the delete of the
	  customer hit the foreign key, and every test in the class errored on
	  teardown.

	  That is this very bug, met by accident, on the field next to the one being
	  fixed. The fix deliberately covers the date alone (see setEventDate), so
	  the teardown has to hold a handle the save cannot erase.
	*/
	/*
	  In SQL, and in dependency order.

	  Through the repositories it cannot be done any more: saveAndUpdateWithDocs
	  attaches each child collection to the entity instance it was holding, so
	  by the time a test ends the persistence context contains rows pointing at
	  an EventMaster it has never seen. Hibernate auto-flushes before the first
	  query a teardown makes and throws TransientObjectException there — before
	  the teardown has deleted anything.

	  That is an artefact of clearing up after the service in the same context
	  the service just used, not something the application does.
	*/
	@AfterEach
	void removeSeed() throws Exception {
		for (String child : List.of("event_external_supplier", "event_decor_category_selection",
				"event_decor_extras_selection", "event_menu_food_selection",
				"event_menu_category_selection", "event_food_selection", "event_quote",
				"event_budget", "event_payment")) {
			String column = child.equals("event_payment") ? "ser_event_id" : "ser_event_master_id";
			jdbcTemplate.update("DELETE FROM " + child + " WHERE " + column + " IN ("
					+ "  SELECT e.ser_event_master_id FROM event_master e"
					+ "  JOIN customer_master c ON c.ser_cust_id = e.ser_cust_id"
					+ "  WHERE c.txt_cust_code LIKE ?)", MARKER + "%");
		}

		jdbcTemplate.update("DELETE FROM event_master WHERE ser_cust_id IN ("
				+ "  SELECT ser_cust_id FROM customer_master WHERE txt_cust_code LIKE ?)", MARKER + "%");
		jdbcTemplate.update("DELETE FROM customer_master WHERE txt_cust_code LIKE ?", MARKER + "%");
	}

	private EventType anEventType() {
		return repositoryEventType.findAll().stream()
				.filter(t -> t.getTxtEventTypeCode() != null && t.getTxtEventTypeCode().startsWith(MARKER))
				.findFirst()
				.orElseGet(() -> {
					EventType seeded = new EventType();
					seeded.setTxtEventTypeCode(MARKER + "-TYPE");
					seeded.setTxtEventTypeName(MARKER + " walima");
					seeded.setBlnIsMainEvent(true);
					return repositoryEventType.save(seeded);
				});
	}

	/** A booking that has a date, as one does the moment the customer picks one. */
	private EventMaster seedDatedEvent() {
		customer = new CustomerMaster();
		customer.setTxtCustCode(MARKER + "-" + System.nanoTime());
		customer.setTxtCustName(MARKER + " customer");
		customer = repositoryCustomerMaster.save(customer);

		eventType = anEventType();

		EventMaster event = new EventMaster();
		event.setTxtEventMasterName(MARKER + " walima");
		event.setTxtEventMasterCode(MARKER + "-" + System.nanoTime());
		event.setDteEventDate(Date.from(DAY.atStartOfDay(ZoneOffset.UTC).toInstant()));
		event.setCustomerMaster(customer);
		event.setEventType(eventType);
		event.setNumNumberOfGuests(100);
		event.setBlnIsActive(true);
		event.setBlnIsDeleted(false);
		event.setIsEditAllowed(true);
		return repositoryEventMaster.saveAndFlush(event);
	}

	/**
	 * The smallest payload the endpoint accepts: the three identifiers it
	 * validates, and nothing else. This is the shape that used to wipe the date.
	 */
	private DtoEventMaster barelyEnoughToSave(EventMaster event) {
		DtoEventMaster dto = new DtoEventMaster();
		dto.setSerEventMasterId(event.getSerEventMasterId());
		dto.setSerCustId(customer.getSerCustId());
		dto.setSerEventTypeId(eventType.getSerEventTypeId());
		dto.setIsEditAllowed(true);
		dto.setNumVersion(event.getNumVersion());
		return dto;
	}

	private Date storedDateOf(EventMaster event) {
		return repositoryEventMaster.findById(event.getSerEventMasterId()).orElseThrow().getDteEventDate();
	}

	@Test
	@DisplayName("a save that never mentions the date leaves it exactly as it was")
	void anOmittedDateIsKept() throws Exception {
		EventMaster event = seedDatedEvent();
		Date booked = storedDateOf(event);
		assertThat(booked).as("the fixture did not store a date, so this test proves nothing").isNotNull();

		serviceEventMaster.saveAndUpdateWithDocs(barelyEnoughToSave(event), null);

		assertThat(storedDateOf(event))
				.as("saving without a date cleared the date of a booked event")
				.hasSameTimeAs(booked);
	}

	@Test
	@DisplayName("a save that does carry a date still moves it")
	void aSuppliedDateStillWins() throws Exception {
		/*
			The other half of the guard, and the reason it is worth writing down.
			Keeping an absent date is only correct if a present one still lands —
			a rule that made the date immutable would pass the test above and be
			a far worse bug than the one it fixed.
		*/
		EventMaster event = seedDatedEvent();
		LocalDate moved = DAY.plusDays(11);

		DtoEventMaster dto = barelyEnoughToSave(event);
		dto.setDteEventDate(UtilDateAndTime.mmddyyyyDateToString(
				Date.from(moved.atStartOfDay(ZoneOffset.UTC).toInstant())));

		serviceEventMaster.saveAndUpdateWithDocs(dto, null);

		/*
			`hasSameTimeAs`, not `isEqualTo`. The driver returns a
			java.sql.Timestamp, which extends java.util.Date and is never equal
			to one — so isEqualTo failed on two values reading
			2033-09-28T00:00:00.000.
		*/
		assertThat(storedDateOf(event))
				.as("the event would not move to the date it was given")
				.hasSameTimeAs(Date.from(moved.atStartOfDay(ZoneOffset.UTC).toInstant()));
	}

	@Test
	@DisplayName("an event with no date yet is still allowed to have none")
	void anEventThatNeverHadADateStaysDateless() throws Exception {
		/*
			The journey creates the booking on the "which occasion" step, before
			the customer has reached the calendar, so a dateless event is a real
			and correct state. A guard that filled it in, or refused the save,
			would break the second screen of the journey.
		*/
		customer = new CustomerMaster();
		customer.setTxtCustCode(MARKER + "-" + System.nanoTime());
		customer.setTxtCustName(MARKER + " customer");
		customer = repositoryCustomerMaster.save(customer);
		eventType = anEventType();

		EventMaster dateless = new EventMaster();
		dateless.setTxtEventMasterName(MARKER + " undated");
		dateless.setTxtEventMasterCode(MARKER + "-" + System.nanoTime());
		dateless.setCustomerMaster(customer);
		dateless.setEventType(eventType);
		dateless.setBlnIsActive(true);
		dateless.setBlnIsDeleted(false);
		dateless.setIsEditAllowed(true);
		dateless = repositoryEventMaster.saveAndFlush(dateless);

		DtoResult result = serviceEventMaster.saveAndUpdateWithDocs(barelyEnoughToSave(dateless), null);

		assertThat(result.getTxtMessage())
				.as("a booking without a date yet was refused")
				.isNotEqualTo("bad_event_date");
		assertThat(storedDateOf(dateless))
				.as("a date appeared on a booking nobody had dated")
				.isNull();
	}

	@Test
	@DisplayName("a date that is not a date is still refused, not quietly kept")
	void anUnreadableDateIsStillARefusal() throws Exception {
		/*
			Keeping an absent date must not soften the existing refusal of a
			malformed one. "Absent" is a caller saying nothing; "32-13-2026" is a
			caller saying something wrong, and the two deserve different answers.
		*/
		EventMaster event = seedDatedEvent();
		Date booked = storedDateOf(event);

		DtoEventMaster dto = barelyEnoughToSave(event);
		dto.setDteEventDate("32-13-2026");

		DtoResult result = serviceEventMaster.saveAndUpdateWithDocs(dto, null);

		assertThat(result.getTxtMessage())
				.as("an unreadable date was accepted")
				.isEqualTo("bad_event_date");
		assertThat(storedDateOf(event))
				.as("the refused save changed the date anyway")
				.hasSameTimeAs(booked);
	}
}
