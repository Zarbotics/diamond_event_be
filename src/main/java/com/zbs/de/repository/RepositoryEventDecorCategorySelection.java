package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.EventDecorCategorySelection;

@Repository("repositoryEventDecorCategorySelection")
public interface RepositoryEventDecorCategorySelection extends JpaRepository<EventDecorCategorySelection, Integer> {
	void deleteByEventMaster_SerEventMasterId(Integer serEventMasterId);

	List<EventDecorCategorySelection> findByEventMaster_SerEventMasterId(Integer serEventMasterId);
}
