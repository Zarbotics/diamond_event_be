package com.zbs.de.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.DecorCategoryPropertyValue;

@Repository("repositoryDecorCategoryPropertyValue")
public interface RepositoryDecorCategoryPropertyValue extends JpaRepository<DecorCategoryPropertyValue, Integer> {

	@Query("SELECT v FROM DecorCategoryPropertyValue v WHERE v.blnIsDeleted = false")
	List<DecorCategoryPropertyValue> findAll();

	@Query("SELECT v FROM DecorCategoryPropertyValue v WHERE v.decorCategoryProperty.serPropertyId = :propertyId AND v.blnIsDeleted = false")
	List<DecorCategoryPropertyValue> findByPropertyIdAndNotDeleted(@Param("propertyId") Integer propertyId);

	Optional<DecorCategoryPropertyValue> findByIdAndBlnIsDeletedFalse(Integer id);

}
