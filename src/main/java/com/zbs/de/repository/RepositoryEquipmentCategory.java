package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.EquipmentCategory;

@Repository("repositoryEquipmentCategory")
public interface RepositoryEquipmentCategory extends JpaRepository<EquipmentCategory, Integer> {

	@Query("SELECT c FROM EquipmentCategory c WHERE c.blnIsDeleted = false AND c.blnIsActive = true "
			+ "ORDER BY c.numDisplayOrder ASC NULLS LAST, c.txtName ASC")
	List<EquipmentCategory> findOffered();
}
