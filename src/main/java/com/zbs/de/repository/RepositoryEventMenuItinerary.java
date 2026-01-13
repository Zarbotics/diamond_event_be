package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.EventMenuItinerary;
import com.zbs.de.model.dto.DtoEventMenuItinerary;

import jakarta.transaction.Transactional;

@Repository("repositoryEventMenuItinerary")
public interface RepositoryEventMenuItinerary extends JpaRepository<EventMenuItinerary, Long> {

	List<EventMenuItinerary> findByEventMaster_SerEventMasterId(Integer eventId);

	@Query("""
			    SELECT e.itineraryItem.serItineraryItemId, SUM(e.numCalculatedQuantity)
			    FROM EventMenuItinerary e
			    WHERE e.eventMaster.serEventMasterId = :eventId
			    GROUP BY e.itineraryItem.serItineraryItemId
			""")
	List<Object[]> aggregateByItineraryItem(@Param("eventId") Integer eventId);

	void deleteByEventMaster_SerEventMasterId(Integer serEventMasterId);

	@Query("""
			    SELECT new com.zbs.de.model.dto.DtoEventMenuItinerary(
			        emi.serEventMenuItineraryId,

			        em.serEventMasterId,
			        em.txtEventMasterCode,
			        em.txtEventMasterName,

			        mi.serMenuItemId,
			        mi.txtCode,
			        mi.txtName,
			        mi.txtShortName,
			        mi.txtDescription,

			        ii.serItineraryItemId,
			        ii.txtCode,
			        ii.txtName,
			        iit.serItineraryItemTypeId,
			        iit.txtName,

			        emi.numCalculatedQuantity
			    )
			    FROM EventMenuItinerary emi
			    JOIN emi.eventMaster em
			    JOIN emi.menuItem mi
			    JOIN emi.itineraryItem ii
			    JOIN ii.itineraryItemType iit
			    WHERE em.serEventMasterId = :eventId
			      AND emi.blnIsDeleted = false
			""")
	List<DtoEventMenuItinerary> findMenuItineraryViewByEvent(@Param("eventId") Integer eventId);

	@Transactional
	@Modifying
	@Query("""
			    UPDATE EventMenuItinerary emi
			    SET emi.blnIsDeleted = true,
			        emi.blnIsActive = false
			    WHERE emi.eventMaster.serEventMasterId = :eventId
			      AND (emi.blnIsDeleted IS NULL OR emi.blnIsDeleted = false)
			""")
	int softDeleteByEventId(@Param("eventId") Integer eventId);
}
