package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.EventPriceLine;

@Repository("repositoryEventPriceLine")
public interface RepositoryEventPriceLine extends JpaRepository<EventPriceLine, Long> {

	/** The engine's working for one booking, in the order it was written. */
	@Query("SELECT l FROM EventPriceLine l WHERE l.serEventMasterId = :eventId "
			+ "AND l.blnIsDeleted = false ORDER BY l.serEventPriceLineId")
	List<EventPriceLine> findForEvent(@Param("eventId") Integer eventId);

	/**
	 * Throws away the previous working for a booking.
	 *
	 * <h4>Why this deletes rather than marks deleted</h4>
	 *
	 * These rows are not a history. They are the current answer, rewritten from
	 * scratch every time the booking is saved, and a soft delete would leave the
	 * table growing by a full set of lines per save with nothing able to tell
	 * the sets apart. What a customer was quoted last month is recorded by the
	 * quote that was sent, which is a different thing and is kept elsewhere.
	 */
	@Modifying
	@Query("DELETE FROM EventPriceLine l WHERE l.serEventMasterId = :eventId")
	void deleteForEvent(@Param("eventId") Integer eventId);
}
