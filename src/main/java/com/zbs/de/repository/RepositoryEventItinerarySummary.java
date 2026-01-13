package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.EventItinerarySummary;
import com.zbs.de.model.dto.DtoEventItinerarySummary;

import jakarta.transaction.Transactional;

@Repository("repositoryEventItinerarySummary")
public interface RepositoryEventItinerarySummary extends JpaRepository<EventItinerarySummary, Long> {

	List<EventItinerarySummary> findByEventMaster_SerEventMasterId(Integer eventId);

	void deleteByEventMaster_SerEventMasterId(Integer eventId);

	@Query("""
			    SELECT new com.zbs.de.model.dto.DtoEventItinerarySummary(
			        eis.serEventItinerarySummaryId,

			        em.serEventMasterId,
			        em.txtEventMasterCode,
			        em.txtEventMasterName,

			        ii.serItineraryItemId,
			        ii.txtCode,
			        ii.txtName,
			        iit.serItineraryItemTypeId,
			        iit.txtName,

			        eis.numTotalQuantity
			    )
			    FROM EventItinerarySummary eis
			    JOIN eis.eventMaster em
			    JOIN eis.itineraryItem ii
			    JOIN ii.itineraryItemType iit
			    WHERE em.serEventMasterId = :eventId
			      AND eis.blnIsDeleted = false
			""")
	List<DtoEventItinerarySummary> findSummaryViewByEvent(@Param("eventId") Integer eventId);

	@Transactional
	@Modifying
	@Query("""
			    UPDATE EventItinerarySummary eis
			    SET eis.blnIsDeleted = true,
			        eis.blnIsActive = false
			    WHERE eis.eventMaster.serEventMasterId = :eventId
			      AND (eis.blnIsDeleted IS NULL OR eis.blnIsDeleted = false)
			""")
	int softDeleteByEventId(@Param("eventId") Integer eventId);
}