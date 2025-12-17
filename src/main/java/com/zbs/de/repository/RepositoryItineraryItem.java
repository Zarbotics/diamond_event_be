package com.zbs.de.repository;

import com.zbs.de.model.ItineraryItem;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RepositoryItineraryItem extends JpaRepository<ItineraryItem, Long> {
	@Query(value = "SELECT MAX(CAST(SUBSTRING(txt_code, 5) AS int)) " + "FROM itinerary_item "
			+ "WHERE txt_code LIKE 'ITI-%' AND bln_is_deleted = false", nativeQuery = true)
	Integer findMaxCodeNumber();

	List<ItineraryItem> findByBlnIsActiveTrueAndBlnIsDeletedFalseOrderBySerItineraryItemIdDesc();

	List<ItineraryItem> findByBlnIsDeletedFalseOrderBySerItineraryItemIdDesc();

	@Query("Select t from ItineraryItem t where t.serItineraryItemId = :id and t.blnIsDeleted = false")
	ItineraryItem getById(@Param("id") Long id);

	@Query("SELECT i FROM ItineraryItem i WHERE i.blnIsActive = true AND i.blnIsDeleted = false AND i.itineraryItemType.serItineraryItemTypeId = :typeId")
	List<ItineraryItem> findAllActiveByType(@Param("typeId") Integer typeId);

	@Query("SELECT i FROM ItineraryItem i WHERE i.blnIsDeleted = false AND i.itineraryItemType.serItineraryItemTypeId = :typeId")
	List<ItineraryItem> findAllByType(@Param("typeId") Integer typeId);
}
