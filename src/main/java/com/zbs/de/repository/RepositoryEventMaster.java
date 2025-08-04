package com.zbs.de.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.EventMaster;
import com.zbs.de.model.dto.DtoEventMasterStats;

@Repository("repositoryEventMaster")
public interface RepositoryEventMaster extends JpaRepository<EventMaster, Integer> {

	@Query("SELECT MAX(e.txtEventMasterCode) FROM EventMaster e")
	String findMaxEventCode();

	@Query("SELECT e FROM EventMaster e WHERE e.customerMaster.serCustId = :custId AND e.eventType.serEventTypeId = :eventTypeId AND e.blnIsDeleted = false")
	Optional<EventMaster> findByCustomerAndEventType(@Param("custId") Integer custId,
			@Param("eventTypeId") Integer eventTypeId);

	@Query("SELECT e FROM EventMaster e WHERE e.customerMaster.serCustId = :custId AND e.blnIsDeleted = false")
	List<EventMaster> findByCustomerId(@Param("custId") Integer custId);

	List<EventMaster> findByBlnIsDeletedFalse();

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

}
