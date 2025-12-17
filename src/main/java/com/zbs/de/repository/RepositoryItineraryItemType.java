package com.zbs.de.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.ItineraryItemType;

import java.util.List;

@Repository
public interface RepositoryItineraryItemType extends JpaRepository<ItineraryItemType, Integer> {
	List<ItineraryItemType> findByBlnIsActiveTrueAndBlnIsDeletedFalseOrderBySerItineraryItemTypeIdDesc();
	
	List<ItineraryItemType> findByBlnIsDeletedFalseOrderBySerItineraryItemTypeIdDesc();

	@Query("Select t from ItineraryItemType t where t.serItineraryItemTypeId = :id and t.blnIsDeleted = false")
	ItineraryItemType getById(@Param("id") Integer id);
	
    // Returns the maximum number part of txtCode like 'IIT-###'
    @Query(value = "SELECT MAX(CAST(SUBSTRING(txt_code, 5) AS int)) FROM itinerary_item_type WHERE txt_code LIKE 'IIT-%'", nativeQuery = true)
    Integer findMaxCodeNumber();
}
