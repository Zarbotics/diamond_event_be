package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.MenuFoodMaster;

@Repository("repositoryMenuFoodMaster")
public interface RepositoryMenuFoodMaster extends JpaRepository<MenuFoodMaster, Integer> {
	List<MenuFoodMaster> findByBlnIsDeleted(Boolean blnIsDeleted);
}