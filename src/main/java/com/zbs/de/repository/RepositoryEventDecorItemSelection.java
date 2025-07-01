package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.EventDecorItemSelection;

@Repository("repositoryEventDecorItemSelection")
public interface RepositoryEventDecorItemSelection extends JpaRepository<EventDecorItemSelection, Integer> {
	List<EventDecorItemSelection> findByEventMaster_SerEventMasterId(Integer serEventMasterId);
	
	@Query("SELECT e FROM EventDecorItemSelection e WHERE e.eventMaster.serEventMasterId = :eventId AND e.blnIsDeleted = false")
	List<EventDecorItemSelection> findByEventId(@Param("eventId") Integer eventId);


}
