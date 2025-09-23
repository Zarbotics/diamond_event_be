package com.zbs.de.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.DecorCategoryPropertyMaster;

@Repository("repositoryDecorCategoryPropertyMaster")
public interface RepositoryDecorCategoryPropertyMaster extends JpaRepository<DecorCategoryPropertyMaster, Integer> {
	List<DecorCategoryPropertyMaster> findAllByBlnIsDeletedFalse();

	Optional<DecorCategoryPropertyMaster> findBySerPropertyIdAndBlnIsDeletedFalse(Integer id);

	List<DecorCategoryPropertyMaster> findByDecorCategoryMaster_SerDecorCategoryIdAndBlnIsDeletedFalse(Integer categoryId);

	@Query("SELECT MAX(e.txtPropertyCode) FROM DecorCategoryPropertyMaster e")
	String findMaxDecorCategoryPropertyMasterCode();
}
