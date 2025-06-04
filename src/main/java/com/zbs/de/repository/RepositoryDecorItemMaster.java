package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.DecorItemMaster;

@Repository("repositoryDecorItemMaster")
public interface RepositoryDecorItemMaster extends JpaRepository<DecorItemMaster, Integer> {
	List<DecorItemMaster> findByBlnIsDeleted(Boolean blnIsDeleted);
}