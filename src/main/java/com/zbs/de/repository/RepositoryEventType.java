package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.EventType;

@Repository("repositoryEventType")
public interface RepositoryEventType extends JpaRepository<EventType, Integer> {
	List<EventType> findByBlnIsDeleted(Boolean blnIsDeleted);
}