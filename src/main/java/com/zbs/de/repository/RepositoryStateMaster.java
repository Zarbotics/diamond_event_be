package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.StateMaster;

@Repository("repositoryStateMaster")
public interface RepositoryStateMaster extends JpaRepository<StateMaster, Integer> {
	List<StateMaster> findByBlnIsDeleted(Boolean blnIsDeleted);
}