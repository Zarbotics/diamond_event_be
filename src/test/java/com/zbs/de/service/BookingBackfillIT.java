package com.zbs.de.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.zbs.de.model.Booking;
import com.zbs.de.model.EventMaster;
import com.zbs.de.repository.RepositoryBooking;
import com.zbs.de.repository.RepositoryEventMaster;

/**
 * Stage 1 of putting a booking above the event. See PLATFORM.md §15.
 *
 * <h2>What this stage is</h2>
 *
 * A table, a nullable column and a backfill. No behaviour changes, nothing
 * reads the column, and the whole thing is undone by dropping it — which is the
 * point: it can go to production on its own while the stages that <em>do</em>
 * change behaviour are built and reviewed separately.
 *
 * <h2>What is worth asserting, then</h2>
 *
 * Two things, and they pull in opposite directions.
 *
 * <p>
 * That the backfill worked: every event that existed when V11 ran has a parent,
 * and each parent carries the customer and the reference from the event it came
 * from. A backfill nobody checks is a backfill that quietly missed the rows with
 * a NULL in them.
 *
 * <p>
 * And that an ordinary save does not undo it. The failure this stage could
 * actually cause is the column being made updatable by somebody who thinks
 * write-once looks unfinished — at which point Hibernate writes NULL over the
 * backfill on the first save of every event, silently, starting with the
 * busiest. Stage 2 made it insertable, which is what the create paths need; it
 * is still not updatable, and that half is what the last test here defends.
 *
 * <p>
 * What stage 2 does is asserted next door, in
 * {@code BookingCreatedWithEventIT}. This file is about the rows the migration
 * touched.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class BookingBackfillIT {

	private static final String MARKER = "IT-BOOKING";

	@Autowired
	private RepositoryBooking repositoryBooking;

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

		repositoryBooking.findAll().stream()
				.filter(b -> b.getTxtBookingCode() != null && b.getTxtBookingCode().startsWith(MARKER))
				.forEach(repositoryBooking::delete);
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

	@Test
	@DisplayName("the application starts, so the entity and the table agree")
	void theSchemaValidates() {
		/*
		 * ddl-auto is validate, so reaching this line means Hibernate compared
		 * Booking against the real table at startup and found every column it
		 * expects, with the types it expects.
		 */
		assertThat(repositoryBooking.count()).isNotNegative();
	}

	@Test
	@DisplayName("every event that existed when the migration ran has a booking above it")
	void theBackfillLeftNothingBehind() throws Exception {
		/*
		 * Deleted events included. The invariant worth having is "every event that
		 * existed when this ran has a parent", with no exceptions to remember —
		 * an exception is exactly what makes a later join quietly drop rows.
		 *
		 * Restricted to events older than the migration, because events created
		 * since then legitimately have none: the save paths are not wired up until
		 * stage 2.
		 */
		long parentless = countWhere("""
				SELECT COUNT(*) FROM event_master e
				WHERE e.ser_booking_id IS NULL
				  AND e.ser_event_master_id <= (
				      SELECT COALESCE(MAX(ser_event_master_id), 0) FROM event_master e2
				      WHERE e2.ser_booking_id IS NOT NULL)
				""");

		assertThat(parentless)
				.as("the backfill skipped events — most likely the ones with a NULL in the column it matched on")
				.isZero();
	}

	@Test
	@DisplayName("no two events were given the same booking")
	void eachEventGotItsOwn() throws Exception {
		// One booking per event at this stage. Several events sharing a booking is
		// where this is going, but not yet, and a duplicate now would be an error
		// in the backfill rather than a wedding.
		long shared = countWhere("""
				SELECT COUNT(*) FROM (
				    SELECT ser_booking_id FROM event_master
				    WHERE ser_booking_id IS NOT NULL
				    GROUP BY ser_booking_id HAVING COUNT(*) > 1
				) duplicated
				""");

		assertThat(shared).as("two events were attached to the same booking by the backfill").isZero();
	}

	@Test
	@DisplayName("a booking carries the customer and the reference from the event it came from")
	void theBackfillCopiedWhatMatters() throws Exception {
		/*
		 * The parent is only useful if it knows who the booking is with. A
		 * backfill that created rows but left them blank would satisfy every count
		 * above and be worth nothing.
		 */
		long mismatched = countWhere("""
				SELECT COUNT(*) FROM event_master e
				JOIN booking b ON b.ser_booking_id = e.ser_booking_id
				WHERE (b.txt_booking_code IS DISTINCT FROM e.txt_event_master_code
				       AND e.txt_event_master_code IS NOT NULL)
				   OR b.ser_cust_id IS DISTINCT FROM e.ser_cust_id
				""");

		assertThat(mismatched)
				.as("a backfilled booking does not match the event it was created from")
				.isZero();
	}

	@Test
	@DisplayName("saving an event built from a DTO does not wipe the booking it was attached to")
	void savingAnEventLeavesTheBackfillAlone() {
		/*
		 * The failure this stage could actually cause, and the reason the column
		 * is updatable = false.
		 *
		 * Reloading an event and saving it would not show this: the field
		 * round-trips, so Hibernate writes back what it read. The path that bites
		 * is the one this codebase actually uses in places — an entity built from
		 * a DTO, carrying the id but nothing the DTO does not know about, saved
		 * over the top. `MapperEventMaster.toEntity` does exactly that.
		 *
		 * With the column writable, Hibernate would write NULL into it and the
		 * migration would be undone one booking at a time, silently, starting
		 * with the events people touch most.
		 */
		Booking booking = new Booking();
		booking.setTxtBookingCode(MARKER + "-BOOKING-" + System.nanoTime());
		Long someBooking = repositoryBooking.saveAndFlush(booking).getSerBookingId();

		EventMaster event = new EventMaster();
		event.setTxtEventMasterName(MARKER + " walima");
		event.setTxtEventMasterCode(MARKER + "-" + System.nanoTime());
		event.setDteEventDate(Date.from(LocalDate.now(ZoneOffset.UTC).plusYears(2)
				.atStartOfDay(ZoneOffset.UTC).toInstant()));
		event.setBlnIsDeleted(false);
		Integer eventId = repositoryEventMaster.saveAndFlush(event).getSerEventMasterId();

		// Attached the way the backfill attached the rows that came before it.
		try (Connection connection = connect();
				Statement update = connection.createStatement()) {
			update.executeUpdate("UPDATE event_master SET ser_booking_id = " + someBooking
					+ " WHERE ser_event_master_id = " + eventId);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}

		/*
		 * A detached copy, as a mapper produces: the id, the fields being saved,
		 * and nothing else. Its serBookingId is null because a DTO has never
		 * carried one.
		 */
		EventMaster fromDto = new EventMaster();
		fromDto.setSerEventMasterId(eventId);
		fromDto.setTxtEventMasterName(MARKER + " walima, renamed");
		fromDto.setTxtEventMasterCode(event.getTxtEventMasterCode());
		fromDto.setDteEventDate(event.getDteEventDate());
		fromDto.setBlnIsDeleted(false);
		fromDto.setNumVersion(repositoryEventMaster.findById(eventId).orElseThrow().getNumVersion());

		repositoryEventMaster.saveAndFlush(fromDto);

		Long stillAttachedTo = repositoryEventMaster.findById(eventId).orElseThrow().getSerBookingId();

		assertThat(stillAttachedTo)
				.as("saving an event cleared its booking — the mapping has been made writable")
				.isEqualTo(someBooking);
	}

	@Test
	@DisplayName("how many events are still without a parent stays answerable")
	void theSizeOfTheGapIsKnown() {
		/*
		 * Stage 1 expected this number to grow: the migration gave a parent to
		 * everything that existed at the time, and nothing kept that true for
		 * events created afterwards. Stage 2 is what stopped it growing — see
		 * BookingCreatedWithEventIT, which asserts that an event created now gets
		 * one.
		 *
		 * It still cannot assert zero. A database that accumulated parentless
		 * events between the two stages deploying still has them, legitimately,
		 * and a test failing on those would fail everywhere except a database
		 * created after both. What matters is that the number remains cheap to
		 * ask for, because it is what tells stage 3 how much it must tidy before
		 * it can move the payments.
		 */
		assertThat(repositoryBooking.countEventsWithNoBooking())
				.as("this has to remain answerable; stage 3 sizes its backfill from it")
				.isNotNegative();
	}
}
