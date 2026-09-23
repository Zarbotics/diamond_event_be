package com.zbs.de.repository;

import java.util.Date;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Serialises bookings that are competing for the same day.
 *
 * <h2>What this is for</h2>
 *
 * How many events may be held on a date is a <em>counting</em> rule — two on an
 * ordinary day, three on a Sunday unless the Monday after it is used. Counting
 * rules cannot be expressed as a database constraint the way uniqueness can:
 * there is no row to collide with, only a total to exceed.
 *
 * <p>
 * So the check is "count what is already there, then insert", and between those
 * two steps a second request can do exactly the same thing. Both count one, both
 * see room for another, both insert, and the day ends up with three events on
 * it. Neither customer did anything wrong and nothing in the log says what
 * happened — the team finds out when they try to staff it.
 *
 * <h2>Why an advisory lock rather than the alternatives</h2>
 *
 * <ul>
 * <li><strong>A unique index</strong> cannot count.</li>
 * <li><strong>SERIALIZABLE isolation</strong> would work, but it applies to the
 * whole transaction — and these transactions also write documents, menu
 * selections and running orders. Paying for serialisation across all of that, and
 * handling the retries it forces, to protect one count is a poor trade.</li>
 * <li><strong>A trigger</strong> would cover every write path including the
 * admin one, which is attractive, but it would also start refusing writes the
 * business currently makes deliberately. That needs an answer from them
 * first — see PLATFORM.md A3b.</li>
 * <li><strong>An advisory lock on the date</strong> costs one row-less lock,
 * held only to the end of the transaction, and only ever contends with another
 * booking for the same day. Two customers booking different dates never meet.</li>
 * </ul>
 *
 * <h2>What it does not fix</h2>
 *
 * This makes the check <em>reliable</em>; it does not make it <em>universal</em>.
 * A path that never calls the check is unaffected, and there is one — see A3b.
 */
@Repository
public class RepositoryEventDateLock {

	/**
	 * Distinguishes these locks from any other advisory lock in the application.
	 *
	 * <p>
	 * Advisory locks share one global namespace across the whole database, so
	 * two unrelated features picking the same number would block each other for
	 * no reason and be extremely hard to diagnose. The two-argument form keeps
	 * this feature's locks in their own space.
	 */
	private static final int EVENT_DATE_LOCK_NAMESPACE = 4_732_101;

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Waits until nobody else is booking this date, then returns.
	 *
	 * <p>
	 * The lock is released when the surrounding transaction ends, whether it
	 * commits or rolls back — there is no unlock to forget and no way for a
	 * failure to leave a date locked. {@code MANDATORY} because a lock taken
	 * outside a transaction would be released immediately and protect nothing,
	 * and silently protecting nothing is worse than failing loudly.
	 *
	 * @param eventDate the day being booked; only the day matters, so two
	 *                  requests for the same date at different times of day
	 *                  still contend
	 */
	@Transactional(propagation = Propagation.MANDATORY)
	public void lockForBooking(Date eventDate) {
		if (eventDate == null) {
			return;
		}

		/*
		 * The day number since the epoch, which is stable, fits comfortably in
		 * an int, and gives one lock per calendar day.
		 */
		int day = (int) (eventDate.toInstant().atZone(java.time.ZoneOffset.UTC)
				.toLocalDate().toEpochDay());

		entityManager
				.createNativeQuery("SELECT pg_advisory_xact_lock(:namespace, :day)")
				.setParameter("namespace", EVENT_DATE_LOCK_NAMESPACE)
				.setParameter("day", day)
				.getSingleResult();
	}
}
