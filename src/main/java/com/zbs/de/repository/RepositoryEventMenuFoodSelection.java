package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.EventMenuFoodSelection;

@Repository("repositoryEventMenuFoodSelection")
public interface RepositoryEventMenuFoodSelection extends JpaRepository<EventMenuFoodSelection, Integer> {
	@Query("SELECT e FROM EventMenuFoodSelection e WHERE e.eventMaster.serEventMasterId = :eventId AND e.blnIsDeleted = false")
	List<EventMenuFoodSelection> findByEventId(@Param("eventId") Integer eventId);

}
