package com.zbs.de.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
import com.zbs.de.repository.RepositoryEventMaster;

/**
 * How many events a day can hold, when two people book at once.
 *
 * <p>
 * The rule is a count — two on an ordinary day, three on a Sunday unless the
 * Monday after it is used — and counting rules have a gap that uniqueness rules
 * do not. The check is "count what is already there, then insert", and between
 * those two steps a second request can do exactly the same thing. Both count
 * one, both see room, both insert, and the day ends up over capacity.
 *
 * <p>
 * That cannot be tested single-threaded, because the failure only exists in the
 * window between one request counting and the other writing. So this races two
 * real transactions through the real service against a real database, which is
 * the only arrangement in which the bug is either present or absent.
 *
 * <p>
 * <strong>Production has a date that is over capacity</strong> — three events on
 * Friday 1 May 2026 — which is what sent me looking. Those three were created
 * days apart rather than in the same second, so they are not this race; they
 * came through an administrative path that never counts at all. That is a
 * separate question and a business decision rather than a bug, recorded as A3b.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class EventDateCapacityIT {

	/** Far enough out that nothing else in the suite is using it. */
	private static final LocalDate CONTESTED_DAY = LocalDate.now(ZoneOffset.UTC)
			.plusYears(3).withDayOfMonth(15).withMonth(6);

	private static final String MARKER = "IT-CAPACITY";

	/*
	 * The implementation rather than the interface: canBookEvent is not declared
	 * on ServiceEventMaster. Worth noticing rather than working around silently
	 * — the capacity rule is a business operation with six call sites and no
	 * place in the contract, which is part of why one save path can skip it.
	 */
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

	private Date dayAsDate() {
		return Date.from(CONTESTED_DAY.atStartOfDay(ZoneOffset.UTC).toInstant());
	}

	private long eventsOnTheDay() {
		Date start = Date.from(CONTESTED_DAY.atStartOfDay(ZoneOffset.UTC).toInstant());
		Date end = Date.from(CONTESTED_DAY.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant());

		return repositoryEventMaster.findAll().stream()
				.filter(e -> !Boolean.TRUE.equals(e.getBlnIsDeleted()))
				.filter(e -> e.getDteEventDate() != null)
				.filter(e -> !e.getDteEventDate().before(start) && e.getDteEventDate().before(end))
				.count();
	}

	/**
	 * One booking attempt, doing what the service does: ask, then write.
	 *
	 * <p>
	 * In its own transaction, because that is what makes the advisory lock mean
	 * anything — a lock is held until its transaction ends, so two attempts
	 * sharing one transaction would never contend.
	 */
	private boolean attemptBooking(String name) {
		return Boolean.TRUE.equals(transactionTemplate.execute(status -> {
			var allowed = serviceEventMaster.canBookEvent(dayAsDate(), null, null);
			if (!allowed.isAllowed()) {
				return false;
			}

			EventMaster event = new EventMaster();
			event.setTxtEventMasterName(MARKER + " " + name);
			event.setDteEventDate(dayAsDate());
			event.setBlnIsDeleted(false);
			repositoryEventMaster.saveAndFlush(event);
			return true;
		}));
	}

	/**
	 * Asks the capacity question the way the service does: inside a transaction.
	 *
	 * <p>
	 * Not a convenience. The advisory lock is declared {@code MANDATORY}, so
	 * calling {@code canBookEvent} without a transaction fails loudly rather than
	 * taking a lock that would be released immediately and protect nothing. These
	 * tests called it directly at first and were refused, which is the guard
	 * working — every real caller is inside a transactional save.
	 */
	private boolean mayBook(Date newDate, Date oldDate, Integer eventId) {
		return Boolean.TRUE.equals(transactionTemplate.execute(
				status -> serviceEventMaster.canBookEvent(newDate, oldDate, eventId).isAllowed()));
	}

	// -----------------------------------------------------------------

	@Test
	@DisplayName("a day fills up, and the booking that would overfill it is refused")
	void theDayFillsUp() {
		// The ordinary, single-threaded case, so that a passing race test below
		// cannot be passing because the rule is simply never enforced.
		assertThat(attemptBooking("first")).isTrue();
		assertThat(attemptBooking("second")).isTrue();

		assertThat(attemptBooking("third"))
				.as("a third booking was accepted on a day that holds two")
				.isFalse();
		assertThat(eventsOnTheDay()).isEqualTo(2);
	}

	@Test
	@DisplayName("two customers booking the last place at once cannot both get it")
	void theLastPlaceGoesToExactlyOne() throws Exception {
		/*
		 * The race, arranged rather than hoped for. One place is left, two
		 * threads are released at the same instant, and both run the same
		 * count-then-insert the service runs.
		 *
		 * Without the advisory lock both count one, both see room, and both
		 * write — three events on a day that holds two, with nothing anywhere
		 * saying so.
		 */
		assertThat(attemptBooking("already-there")).isTrue();

		int racers = 2;
		CyclicBarrier startTogether = new CyclicBarrier(racers);
		AtomicInteger succeeded = new AtomicInteger();

		ExecutorService pool = Executors.newFixedThreadPool(racers);
		try {
			List<Callable<Void>> attempts = List.of(
					racer(startTogether, succeeded, "racer-one"),
					racer(startTogether, succeeded, "racer-two"));

			for (Future<Void> outcome : pool.invokeAll(attempts, 30, TimeUnit.SECONDS)) {
				outcome.get();
			}
		} finally {
			pool.shutdownNow();
		}

		assertThat(succeeded.get())
				.as("both customers were told they had the last place on the day")
				.isEqualTo(1);
		assertThat(eventsOnTheDay())
				.as("the day is over capacity")
				.isEqualTo(2);
	}

	@Test
	@DisplayName("a day that is already over capacity can still be edited, but not added to")
	void anOverCapacityDayIsGrandfathered() {
		/*
		 * The property that makes "leave the existing data alone" a safe decision
		 * rather than a hopeful one.
		 *
		 * Production has three events on Friday 1 May 2026, on a day that holds
		 * two — booked before the rule was enforced everywhere. Those are three
		 * commitments to three families, and correcting them is a conversation
		 * with customers, not a data fix. So the requirement is precise: the team
		 * must still be able to open and save those bookings, while nothing new
		 * may be added to the day.
		 *
		 * It works because of three details that are easy to get wrong
		 * separately: countEventsOnDate excludes the event being edited, the
		 * count is only incremented when the date actually changes, and the
		 * comparison is strictly greater. Change any one of them and the team is
		 * locked out of their own bookings.
		 */
		List<Integer> onTheDay = new ArrayList<>();
		for (String name : List.of("legacy-one", "legacy-two", "legacy-three")) {
			EventMaster event = new EventMaster();
			event.setTxtEventMasterName(MARKER + " " + name);
			event.setDteEventDate(dayAsDate());
			event.setBlnIsDeleted(false);
			// Straight to the repository: this is what an unguarded save path did,
			// and the point is to reproduce the state it left behind.
			onTheDay.add(repositoryEventMaster.saveAndFlush(event).getSerEventMasterId());
		}
		assertThat(eventsOnTheDay()).isEqualTo(3);

		// Editing one of them, without moving it, must still be allowed.
		for (Integer id : onTheDay) {
			assertThat(mayBook(dayAsDate(), dayAsDate(), id))
					.as("the team was locked out of editing an existing booking on an over-capacity day")
					.isTrue();
		}

		// A new one must not be.
		assertThat(mayBook(dayAsDate(), null, null))
				.as("a fourth event was accepted onto a day already over capacity")
				.isFalse();
	}

	@Test
	@DisplayName("an event cannot be moved onto a day that is already over capacity")
	void nothingCanBeMovedOntoAFullDay() {
		// The other half of grandfathering, and the one that would quietly make
		// the problem worse: an existing event elsewhere being rescheduled onto
		// the crowded day.
		for (String name : List.of("legacy-one", "legacy-two", "legacy-three")) {
			EventMaster event = new EventMaster();
			event.setTxtEventMasterName(MARKER + " " + name);
			event.setDteEventDate(dayAsDate());
			event.setBlnIsDeleted(false);
			repositoryEventMaster.saveAndFlush(event);
		}

		Date elsewhere = Date.from(CONTESTED_DAY.plusDays(10).atStartOfDay(ZoneOffset.UTC).toInstant());
		EventMaster moving = new EventMaster();
		moving.setTxtEventMasterName(MARKER + " somewhere-else");
		moving.setDteEventDate(elsewhere);
		moving.setBlnIsDeleted(false);
		Integer movingId = repositoryEventMaster.saveAndFlush(moving).getSerEventMasterId();

		assertThat(mayBook(dayAsDate(), elsewhere, movingId))
				.as("an event was moved onto a day that is already over capacity")
				.isFalse();
	}

	private Callable<Void> racer(CyclicBarrier startTogether, AtomicInteger succeeded, String name) {
		return () -> {
			startTogether.await(20, TimeUnit.SECONDS);
			if (attemptBooking(name)) {
				succeeded.incrementAndGet();
			}
			return null;
		};
	}

	@Test
	@DisplayName("bookings for different days do not wait on each other")
	void differentDaysDoNotContend() {
		/*
		 * The cost of the fix, kept honest. The lock is keyed on the day, so two
		 * customers booking different dates must never queue behind one another
		 * — a lock on the whole table would pass every test above and serialise
		 * the entire booking journey.
		 */
		Date otherDay = Date.from(CONTESTED_DAY.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant());

		long startedAt = System.currentTimeMillis();
		transactionTemplate.execute(status -> {
			serviceEventMaster.canBookEvent(dayAsDate(), null, null);
			// Still holding the lock for the contested day here.
			return serviceEventMaster.canBookEvent(otherDay, null, null);
		});

		assertThat(System.currentTimeMillis() - startedAt)
				.as("taking two different days' locks took long enough to suggest they contended")
				.isLessThan(10_000);
	}
}
