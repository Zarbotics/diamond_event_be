package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.Booking;

/**
 * Bookings.
 *
 * <p>
 * Stage 1 of §15.3, so this is thin on purpose: enough to assert that the
 * table, the backfill and the entity agree with one another, and nothing more.
 * The queries a booking will need — its events, its total, its payments — are
 * written in the stages that introduce them, against requirements rather than
 * in anticipation of them.
 */
@Repository("repositoryBooking")
public interface RepositoryBooking extends JpaRepository<Booking, Long> {

	List<Booking> findByTxtBookingCode(String txtBookingCode);

	/**
	 * Events that have no booking above them.
	 *
	 * <p>
	 * Expected to be empty for everything that existed when V11 ran, and
	 * expected <em>not</em> to stay empty afterwards: events created before
	 * stage 2 wires the save paths up will have none. Reported rather than
	 * enforced for exactly that reason.
	 */
	@Query("SELECT COUNT(e) FROM EventMaster e WHERE e.serBookingId IS NULL")
	long countEventsWithNoBooking();
}
