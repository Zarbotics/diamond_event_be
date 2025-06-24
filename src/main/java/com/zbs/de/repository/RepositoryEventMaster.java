package com.zbs.de.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.EventMaster;

@Repository("repositoryEventMaster")
public interface RepositoryEventMaster extends JpaRepository<EventMaster, Integer> {
	
	@Query("SELECT MAX(e.txtEventMasterCode) FROM EventMaster e")
	String findMaxEventCode();

}
