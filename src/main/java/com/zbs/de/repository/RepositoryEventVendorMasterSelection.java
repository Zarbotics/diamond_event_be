package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.zbs.de.model.EventVendorMasterSelection;

public interface RepositoryEventVendorMasterSelection extends JpaRepository<EventVendorMasterSelection, Integer> {
	@Query("SELECT e FROM EventVendorMasterSelection e WHERE e.eventMaster.serEventMasterId = :eventId AND (e.blnIsDeleted = false OR e.blnIsDeleted is null)")
	List<EventVendorMasterSelection> findByEventId(@Param("eventId") Integer eventId);
}
