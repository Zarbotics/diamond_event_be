package com.zbs.de.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.EventMaster;

@Repository("repositoryEventMaster")
public interface RepositoryEventMaster extends JpaRepository<EventMaster, Integer> {

	@Query("SELECT MAX(e.txtEventMasterCode) FROM EventMaster e")
	String findMaxEventCode();

	@Query("SELECT e FROM EventMaster e WHERE e.customerMaster.serCustId = :custId AND e.eventType.serEventTypeId = :eventTypeId AND e.blnIsDeleted = false")
	Optional<EventMaster> findByCustomerAndEventType(@Param("custId") Integer custId,
			@Param("eventTypeId") Integer eventTypeId);

	List<EventMaster> findByBlnIsDeletedFalse();

}
