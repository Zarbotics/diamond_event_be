package com.zbs.de.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.ConsultationBooking;

@Repository
public interface RepositoryConsultationBooking extends JpaRepository<ConsultationBooking, Integer> {

	/**
	 * Live bookings overlapping a window.
	 *
	 * <p>
	 * Overlap, not containment: a meeting that began before the window and runs
	 * into it occupies just as much of it. Half-open, so a booking that ends
	 * exactly when the window opens does not count.
	 */
	@Query("""
			SELECT b FROM ConsultationBooking b
			WHERE b.serHostId = :hostId
			  AND b.txtStatus = 'BOOKED'
			  AND b.blnIsDeleted = false
			  AND b.dteStartsAt < :windowEnd
			  AND b.dteEndsAt   > :windowStart
			""")
	List<ConsultationBooking> liveBookingsOverlapping(@Param("hostId") Integer hostId,
			@Param("windowStart") Instant windowStart, @Param("windowEnd") Instant windowEnd);

	Optional<ConsultationBooking> findByTxtManagementToken(String token);

	/** A customer's live consultation for one event, so a second is not offered. */
	@Query("""
			SELECT b FROM ConsultationBooking b
			WHERE b.serEventMasterId = :eventId
			  AND b.txtStatus = 'BOOKED'
			  AND b.blnIsDeleted = false
			ORDER BY b.dteStartsAt ASC
			""")
	List<ConsultationBooking> liveBookingsForEvent(@Param("eventId") Integer eventId);

	@Query("""
			SELECT b FROM ConsultationBooking b
			WHERE b.blnIsDeleted = false
			  AND (:hostId IS NULL OR b.serHostId = :hostId)
			  AND (:status IS NULL OR b.txtStatus = :status)
			  AND (:from IS NULL OR b.dteStartsAt >= :from)
			  AND (:to   IS NULL OR b.dteStartsAt <  :to)
			ORDER BY b.dteStartsAt DESC
			""")
	Page<ConsultationBooking> search(@Param("hostId") Integer hostId, @Param("status") String status,
			@Param("from") Instant from, @Param("to") Instant to, Pageable pageable);
}
