package com.zbs.de.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;

import com.zbs.de.model.Booking;
import com.zbs.de.model.CustomerMaster;
import com.zbs.de.model.EventMaster;
import com.zbs.de.model.EventType;
import com.zbs.de.model.UserMaster;
import com.zbs.de.model.dto.DtoEventMaster;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryBooking;
import com.zbs.de.repository.RepositoryCustomerMaster;
import com.zbs.de.repository.RepositoryEventMaster;
import com.zbs.de.repository.RepositoryEventType;

/**
 * Stage 2 of putting a booking above the event. See PLATFORM.md §15.
 *
 * <h2>What changed</h2>
 *
 * V11 gave every event that existed at the time a parent. Nothing kept that
 * true afterwards: an event created the day after the migration had none, and
 * the proportion with no parent would have grown with every sale until stage 3
 * arrived and had to invent parents for whatever had accumulated.
 *
 * <p>
 * So the create branches now make one. That is the whole of this stage — no
 * screen shows a booking, no endpoint returns one, and several events still
 * cannot share a parent. Grouping a wedding's three days under one booking is
 * stage 4, and it needs the journey to ask the question first.
 *
 * <h2>What is worth asserting</h2>
 *
 * That a created event has a parent, that the parent knows who the booking is
 * with, and — the one that would otherwise go unnoticed — that saving the event
 * again neither clears the parent nor makes a second one. Both of those are
 * silent failures: the first empties the column the migration filled, the
 * second leaves a trail of empty bookings that stage 3 would later try to move
 * money onto.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class BookingCreatedWithEventIT {

	private static final String MARKER = "IT-STAGE2";

	/** Whoever is taking the booking. The create paths read this. */
	private static final long SOMEBODY = 90_101L;

	/** How {@code MapperEventMaster.toEntity} parses a date off the wire. */
	private static final DateTimeFormatter WIRE = DateTimeFormatter.ofPattern("dd-MM-yyyy");

	/**
	 * Far enough out to be nobody else's, and a different day per test.
	 *
	 * <p>
	 * The create paths check capacity, so two tests booking the same day would
	 * eventually have the second refused for a reason that has nothing to do
	 * with bookings — and the failure would read as this change breaking.
	 */
	private static final LocalDate FIRST_FREE_DAY = LocalDate.now(ZoneOffset.UTC).plusYears(7);

	@Autowired
	private ServiceEventMaster serviceEventMaster;

	@Autowired
	private RepositoryEventMaster repositoryEventMaster;

	@Autowired
	private RepositoryBooking repositoryBooking;

	@Autowired
	private RepositoryCustomerMaster repositoryCustomerMaster;

	@Autowired
	private RepositoryEventType repositoryEventType;

	private CustomerMaster customer;
	private EventType eventType;

	/**
	 * What distinguishes this run's events from an earlier run's.
	 *
	 * <p>
	 * Events are found back by name, and a run that ended badly can leave rows
	 * behind. Without this the next run finds the old row first and asserts
	 * against it — which reads as the booking being attached to the wrong
	 * customer rather than as a dirty database.
	 */
	private String run;

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
	void seedTheParties() {
		/*
		 * Signed in, because the real thing always is. Two of the create paths
		 * notify the office when an event is registered, and that step reads the
		 * current user without checking — so a test that saved as nobody would
		 * fail inside a notification for reasons that have nothing to do with
		 * bookings, and would read as this change breaking.
		 */
		UserMaster user = new UserMaster();
		user.setSerUserId(SOMEBODY);
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(user, null, List.of()));

		run = String.valueOf(System.nanoTime());

		customer = new CustomerMaster();
		customer.setTxtCustCode(MARKER + "-" + run);
		customer.setTxtCustName(MARKER + " customer");
		customer = repositoryCustomerMaster.save(customer);

		eventType = repositoryEventType.findAll().stream()
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

	/**
	 * Clears up in SQL rather than through the repositories.
	 *
	 * <p>
	 * Not a shortcut. {@code repositoryEventMaster.delete(…)} on an event this
	 * suite created through the service merges a detached graph back into a new
	 * session first, and Hibernate refuses that with a
	 * {@code TransientObjectException} — which arrives as a failed test in
	 * whatever ran last, points at nothing, and leaves the rows behind so that
	 * every subsequent run inherits them.
	 *
	 * <p>
	 * The tables pointing at an event are read out of the catalogue rather than
	 * listed here. There are eleven of them today — a budget, a quote, decor,
	 * extras, four kinds of menu selection — and a hand-written list would be
	 * one short the first time somebody adds a twelfth, which shows up as this
	 * class failing for a reason that has nothing to do with it.
	 *
	 * <p>
	 * Then events, then bookings, then customers. The foreign keys point that
	 * way, and bookings are cleared by customer rather than by walking back from
	 * the events so that a booking whose event never got saved goes too.
	 */
	@AfterEach
	void removeSeed() throws Exception {
		SecurityContextHolder.clearContext();

		String myEvents = "SELECT ser_event_master_id FROM event_master WHERE txt_event_master_name LIKE '"
				+ MARKER + "%'";

		try (Connection connection = connect(); Statement remove = connection.createStatement()) {
			/*
			 * The column as well as the table. Nine of the eleven call it
			 * ser_event_master_id and two call it ser_event_id, so assuming the
			 * common name leaves two tables holding their events down — which
			 * presents as the event delete failing on a foreign key, naming a table
			 * the code above appears to have already cleared.
			 */
			List<String[]> children = new ArrayList<>();
			try (ResultSet found = remove.executeQuery("""
					SELECT child.relname, a.attname
					FROM pg_constraint c
					JOIN pg_class child ON child.oid = c.conrelid
					JOIN pg_class parent ON parent.oid = c.confrelid
					JOIN unnest(c.conkey) k(attnum) ON true
					JOIN pg_attribute a ON a.attrelid = child.oid AND a.attnum = k.attnum
					WHERE c.contype = 'f' AND parent.relname = 'event_master'
					""")) {
				while (found.next()) {
					children.add(new String[] { found.getString(1), found.getString(2) });
				}
			}

			for (String[] child : children) {
				remove.executeUpdate(
						"DELETE FROM " + child[0] + " WHERE " + child[1] + " IN (" + myEvents + ")");
			}

			remove.executeUpdate("DELETE FROM event_master WHERE txt_event_master_name LIKE '" + MARKER + "%'");
			remove.executeUpdate("""
					DELETE FROM booking WHERE ser_cust_id IN (
					    SELECT ser_cust_id FROM customer_master WHERE txt_cust_code LIKE '%s%%')
					""".formatted(MARKER));
			remove.executeUpdate("DELETE FROM customer_master WHERE txt_cust_code LIKE '" + MARKER + "%'");
		}
	}

	// -----------------------------------------------------------------

	/** This run's name for one of its events. */
	private String nameOf(String name) {
		return MARKER + "-" + run + " " + name;
	}

	/** A booking as a client sends one for an event that does not exist yet. */
	private DtoEventMaster newEventOn(LocalDate day, String name) {
		DtoEventMaster dto = new DtoEventMaster();
		dto.setSerCustId(customer.getSerCustId());
		dto.setSerEventTypeId(eventType.getSerEventTypeId());
		dto.setTxtEventMasterName(nameOf(name));
		dto.setDteEventDate(day.format(WIRE));
		dto.setNumNumberOfGuests(120);
		dto.setIsEditAllowed(true);
		return dto;
	}

	/** The event this save created, reloaded. */
	private EventMaster created(DtoResult result, String name) {
		assertThat(result.getTxtMessage())
				.as("the save was refused, so nothing here is being tested")
				.isEqualTo("Success");

		return repositoryEventMaster.findAll().stream()
				.filter(e -> nameOf(name).equals(e.getTxtEventMasterName()))
				.findFirst()
				.orElseThrow(() -> new AssertionError("the save reported Success but created no event"));
	}

	private Connection connect() throws Exception {
		return DriverManager.getConnection(
				System.getenv().getOrDefault("TEST_DB_URL", "jdbc:postgresql://localhost:5432/diamond_ev_test"),
				System.getenv().getOrDefault("TEST_DB_USERNAME", "postgres"),
				System.getenv().getOrDefault("TEST_DB_PASSWORD", "postgres"));
	}

	private long countWhere(String sql) throws Exception {
		try (Connection connection = connect();
				Statement query = connection.createStatement();
				ResultSet result = query.executeQuery(sql)) {
			result.next();
			return result.getLong(1);
		}
	}

	// -----------------------------------------------------------------

	@Test
	@DisplayName("an event created today gets a booking, as one created before the migration did")
	void aNewEventHasAParent() {
		EventMaster event = created(
				serviceEventMaster.saveAndUpdate(newEventOn(FIRST_FREE_DAY, "walima")), "walima");

		assertThat(event.getSerBookingId())
				.as("""
						the event was created with no booking above it, so the gap V11 closed \
						has started reopening — one row per sale""")
				.isNotNull();
	}

	@Test
	@DisplayName("the booking knows who it is with and what to quote on the telephone")
	void theBookingCarriesTheCustomerAndTheCode() {
		EventMaster event = created(
				serviceEventMaster.saveAndUpdate(newEventOn(FIRST_FREE_DAY.plusDays(1), "nikkah")), "nikkah");

		Booking booking = repositoryBooking.findById(event.getSerBookingId()).orElseThrow();

		assertThat(booking.getCustomerMaster())
				.as("a booking with no customer cannot be the thing that makes three events one wedding")
				.isNotNull();
		assertThat(booking.getCustomerMaster().getSerCustId()).isEqualTo(customer.getSerCustId());

		assertThat(booking.getTxtBookingCode())
				.as("""
						the booking does not carry the event's reference, so it cannot be found \
						the way a backfilled one can — the two kinds have drifted apart""")
				.isEqualTo(event.getTxtEventMasterCode());
	}

	@Test
	@DisplayName("saving the event again neither clears the booking nor makes a second one")
	void savingAgainChangesNothing() throws Exception {
		/*
		 * Both halves of this are silent when they go wrong.
		 *
		 * A cleared parent empties the column the migration filled, one event at a
		 * time, starting with the ones people touch most — which is why
		 * ser_booking_id is updatable = false.
		 *
		 * A second parent per save leaves a trail of empty bookings behind every
		 * edited event, and stage 3 would later try to move money onto them —
		 * which is why giveItABooking returns early when there already is one.
		 */
		EventMaster event = created(
				serviceEventMaster.saveAndUpdate(newEventOn(FIRST_FREE_DAY.plusDays(2), "mehndi")), "mehndi");

		Long originalBooking = event.getSerBookingId();
		long bookingsBefore = countWhere("SELECT COUNT(*) FROM booking");

		DtoEventMaster edit = new DtoEventMaster();
		edit.setSerEventMasterId(event.getSerEventMasterId());
		edit.setSerCustId(customer.getSerCustId());
		edit.setSerEventTypeId(eventType.getSerEventTypeId());
		edit.setTxtEventMasterName(event.getTxtEventMasterName());
		edit.setDteEventDate(FIRST_FREE_DAY.plusDays(2).format(WIRE));
		edit.setNumNumberOfGuests(240);
		edit.setIsEditAllowed(true);
		edit.setNumVersion(event.getNumVersion());

		assertThat(serviceEventMaster.saveAndUpdate(edit).getTxtMessage()).isEqualTo("Success");

		EventMaster afterEdit = repositoryEventMaster.findById(event.getSerEventMasterId()).orElseThrow();

		assertThat(afterEdit.getSerBookingId())
				.as("saving the event moved or cleared its booking")
				.isEqualTo(originalBooking);

		assertThat(countWhere("SELECT COUNT(*) FROM booking"))
				.as("an ordinary edit created a second booking, which nothing will ever point at")
				.isEqualTo(bookingsBefore);
	}

	@Test
	@DisplayName("the paths that insert the event first attach a booking too")
	void theOtherCreatePathsAlsoMakeOne() throws Exception {
		/*
		 * Not a formality. These two do not create an event the way saveAndUpdate
		 * does: they insert a bare row immediately after the capacity check, with
		 * its associations nulled, and fill it in afterwards. By the time there is
		 * a customer and a reference code to make a booking from, the insert that
		 * would have carried the booking id has already happened — which is why
		 * giveItABooking has a second way of attaching one, and why the first
		 * version of this change quietly did nothing at all down these two paths
		 * while passing every structural check.
		 *
		 * The admin portal's branch takes a different DTO and a great deal more
		 * setup; it is left to EveryNewEventGetsABookingTest deliberately. It
		 * shares this shape — the same early insert — so what it needs asserting
		 * is that the call is there, and that is exactly what the structural test
		 * asserts.
		 */
		DtoResult withDocs = serviceEventMaster.saveAndUpdateWithDocs(
				newEventOn(FIRST_FREE_DAY.plusDays(3), "reception"), List.of());

		assertThat(created(withDocs, "reception").getSerBookingId())
				.as("saveAndUpdateWithDocs creates events with no booking above them")
				.isNotNull();

		DtoResult customerEdit = serviceEventMaster.saveAndUpdateWithDocsCE(
				newEventOn(FIRST_FREE_DAY.plusDays(6), "sangeet"), List.of());

		assertThat(created(customerEdit, "sangeet").getSerBookingId())
				.as("saveAndUpdateWithDocsCE creates events with no booking above them")
				.isNotNull();
	}

	@Test
	@DisplayName("no two events were given the same booking")
	void eachEventStillGetsItsOwn() throws Exception {
		/*
		 * Where this is heading is several events under one booking — the mehndi,
		 * the nikkah and the walima of one wedding. It is not there yet, and it
		 * must not arrive by accident: a booking shared before anything asks the
		 * customer whether two events belong together would be two families'
		 * money on one row, with no way to tell afterwards.
		 */
		serviceEventMaster.saveAndUpdate(newEventOn(FIRST_FREE_DAY.plusDays(4), "first"));
		serviceEventMaster.saveAndUpdate(newEventOn(FIRST_FREE_DAY.plusDays(5), "second"));

		assertThat(countWhere("""
				SELECT COUNT(*) FROM (
				    SELECT ser_booking_id FROM event_master
				    WHERE ser_booking_id IS NOT NULL
				    GROUP BY ser_booking_id HAVING COUNT(*) > 1
				) duplicated
				"""))
				.as("two events ended up under one booking before anything asked whether they belong together")
				.isZero();
	}
}
