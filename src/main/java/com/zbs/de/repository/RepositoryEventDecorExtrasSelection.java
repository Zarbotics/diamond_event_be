package com.zbs.de.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.EventDecorExtrasSelection;

@Repository("repositoryEventDecorExtrasSelection")
public interface RepositoryEventDecorExtrasSelection extends JpaRepository<EventDecorExtrasSelection, Integer> {
	@Modifying
	@Query("DELETE FROM EventDecorExtrasSelection e WHERE e.eventMaster.serEventMasterId = :eventId")
	void deleteByEventMasterId(@Param("eventId") Integer eventId);
}
