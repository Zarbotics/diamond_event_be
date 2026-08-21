package com.zbs.de.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.EventMaster;
import com.zbs.de.model.dto.DtoEventCalendarEntry;
import com.zbs.de.model.dto.DtoEventMasterStats;
import com.zbs.de.model.dto.DtoEventSummary;
import com.zbs.de.model.dto.DtoEventMasterTableView;

@Repository("repositoryEventMaster")
public interface RepositoryEventMaster
		extends JpaRepository<EventMaster, Integer>, JpaSpecificationExecutor<EventMaster> {

	@Query("SELECT MAX(e.txtEventMasterCode) FROM EventMaster e")
	String findMaxEventCode();

	@Query("SELECT e FROM EventMaster e WHERE e.customerMaster.serCustId = :custId AND e.eventType.serEventTypeId = :eventTypeId AND e.blnIsDeleted = false")
	Optional<EventMaster> findByCustomerAndEventType(@Param("custId") Integer custId,
			@Param("eventTypeId") Integer eventTypeId);

	@Query("SELECT e FROM EventMaster e WHERE e.customerMaster.serCustId = :custId AND e.blnIsDeleted = false")
	List<EventMaster> findByCustomerId(@Param("custId") Integer custId);

	@Query("SELECT e FROM EventMaster e WHERE e.customerMaster.serCustId = :custId AND e.blnIsDeleted = false and e.blnIsActive = true")
	List<EventMaster> findActiveEventMasterByCustomerId(@Param("custId") Integer custId);

	List<EventMaster> findByBlnIsDeletedFalse();

	@Query("SELECT e FROM EventMaster e WHERE e.blnIsDeleted = false order by e.serEventMasterId desc")
	List<EventMaster> getAllNotDeleted();

	@Query("SELECT new com.zbs.de.model.dto.DtoEventMasterStats(e.eventType.txtEventTypeName, COUNT(e)) "
			+ "FROM EventMaster e " + "WHERE e.blnIsDeleted = false " + "GROUP BY e.eventType.txtEventTypeName")
	List<DtoEventMasterStats> countEventsGroupedByEventType();

	@Query(value = """
			SELECT
			    EXTRACT(MONTH FROM created_date) AS month,
			    COUNT(*) AS event_count
			FROM event_master
			WHERE EXTRACT(YEAR FROM created_date) = :year
			GROUP BY EXTRACT(MONTH FROM created_date)
			ORDER BY month
			""", nativeQuery = true)
	List<Object[]> getMonthlyEventCounts(@Param("year") int year);

	@Query("""
			    SELECT em
			    FROM EventMaster em
			    WHERE em.serEventMasterId = :id
			      AND (em.blnIsDeleted = false OR em.blnIsDeleted IS NULL)
			""")
	Optional<EventMaster> findByIdAndBlnIsDeletedFalse(@Param("id") Integer id);

	@Query("Select new com.zbs.de.model.dto.DtoEventMasterTableView(e.serEventMasterId, e.txtEventMasterCode, e.txtEventMasterName, e.dteEventDate, "
			+ "c.serCustId, c.txtCustCode, c.txtCustName, t.serEventTypeId, t.txtEventTypeCode, t.txtEventTypeName, v.serVenueMasterId, "
			+ "v.txtVenueCode, v.txtVenueName) "
			+ "From EventMaster e left join e.customerMaster c left join e.eventType t left join e.venueMaster v Where e.blnIsDeleted=false order by e.serEventMasterId desc")
	List<DtoEventMasterTableView> getAllEventMastersTableView();

	// override findAll so Spring Data will apply entity-graph when using paging
	@Override
	@EntityGraph(attributePaths = { "customerMaster", "eventType", "venueMaster", "vendorMaster", "eventBudget" })
	Page<EventMaster> findAll(Specification<EventMaster> spec, Pageable pageable);

	@Query("SELECT MAX(e.txtEventMasterCode) FROM EventMaster e WHERE e.txtEventMasterCode LIKE CONCAT('DE-', :year, '-%')")
	String findMaxEventCodeForYear(@Param("year") int year);

	/**
	 * Whether a reference is already in use.
	 *
	 * <p>
	 * Used when allocating the next one, so a gap or a code claimed since the
	 * maximum was read is stepped over rather than handed out twice. See
	 * {@code ServiceEventMasterImpl.generateNextEventMasterCode}.
	 */
	boolean existsByTxtEventMasterCode(String txtEventMasterCode);

	boolean existsByDteEventDateAndBlnIsDeletedFalse(Date dteEventDate);

	@Query("select distinct(e.dteEventDate) from EventMaster e where e.blnIsDeleted = false order by e.dteEventDate asc")
	List<Date> getAlreadyBookedDates();

	@Query("""
			    SELECT COUNT(e) FROM EventMaster e
			    WHERE e.blnIsDeleted = false
			    AND e.dteEventDate BETWEEN :start AND :end
			    AND (:eventId IS NULL OR e.serEventMasterId <> :eventId)
			""")
	int countEventsOnDate(@Param("start") Date start, @Param("end") Date end, @Param("eventId") Integer eventId);

	/**
	 * One customer's events, as seven fields rather than sixty.
	 *
	 * <p>
	 * For the "choose an event" step of the journey. Same filter as
	 * {@link #findActiveEventMasterByCustomerId} — active, not deleted — so the
	 * two agree about which of a customer's events exist.
	 *
	 * <p>
	 * Newest first, by id rather than by event date: a booking being worked on
	 * this week may be for next summer, and ordering by date buries it behind
	 * everything already booked.
	 */
	@Query("SELECT new com.zbs.de.model.dto.DtoEventSummary(e.serEventMasterId, e.txtEventMasterCode, "
			+ "e.txtEventMasterName, e.dteEventDate, t.txtEventTypeName, e.txtNumberOfGuests, "
			+ "e.numNumberOfGuests, e.isEditAllowed, e.numFormState) "
			+ "FROM EventMaster e LEFT JOIN e.eventType t "
			+ "WHERE e.customerMaster.serCustId = :custId AND e.blnIsDeleted = false AND e.blnIsActive = true "
			+ "ORDER BY e.serEventMasterId DESC")
	List<DtoEventSummary> findEventSummariesByCustomerId(@Param("custId") Integer custId);

	/**
	 * Every event, as five fields rather than sixty.
	 *
	 * <p>
	 * For the admin calendar, which needs all of them — a month view missing some
	 * of its events is worse than no month view — but needs almost nothing about
	 * each one. A calendar cannot be paginated, so the saving has to come from the
	 * width of a row rather than the number of them.
	 *
	 * <p>
	 * Ordered by date, because that is the only order a calendar has any use for.
	 */
	@Query("SELECT new com.zbs.de.model.dto.DtoEventCalendarEntry(e.serEventMasterId, e.txtEventMasterCode, "
			+ "e.txtEventMasterName, e.dteEventDate, t.txtEventTypeName) "
			+ "FROM EventMaster e LEFT JOIN e.eventType t "
			+ "WHERE e.blnIsDeleted = false AND e.dteEventDate IS NOT NULL "
			+ "ORDER BY e.dteEventDate ASC")
	List<DtoEventCalendarEntry> getCalendarEntries();

	@Query("""
			    SELECT e.dteEventDate, COUNT(e)
			    FROM EventMaster e
			    WHERE e.blnIsDeleted = false
			    AND e.dteEventDate IS NOT NULL
			    GROUP BY e.dteEventDate
			""")
	List<Object[]> getEventDateCounts();
}
