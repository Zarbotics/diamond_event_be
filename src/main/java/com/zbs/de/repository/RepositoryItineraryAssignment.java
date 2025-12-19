package com.zbs.de.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.ItineraryAssignment;

@Repository
public interface RepositoryItineraryAssignment extends JpaRepository<ItineraryAssignment, Long> {
	
	// Find non-deleted assignments ordered by ID descending
	List<ItineraryAssignment> findByBlnIsDeletedFalseOrderBySerItineraryAssignmentIdDesc();

	// Find active and non-deleted assignments
	List<ItineraryAssignment> findByBlnIsActiveTrueAndBlnIsDeletedFalseOrderBySerItineraryAssignmentIdDesc();

	// Find by menu item ID
	List<ItineraryAssignment> findByMenuItem_SerMenuItemIdAndBlnIsDeletedFalseOrderBySerItineraryAssignmentIdDesc(
			Long menuItemId);

	// Find active assignments by menu item ID
	List<ItineraryAssignment> findByMenuItem_SerMenuItemIdAndBlnIsActiveTrueAndBlnIsDeletedFalseOrderBySerItineraryAssignmentIdDesc(
			Long menuItemId);

	// Find by multiple menu item IDs
	@Query("SELECT ia FROM ItineraryAssignment ia WHERE ia.menuItem.serMenuItemId IN :menuItemIds AND ia.blnIsDeleted = false ORDER BY ia.serItineraryAssignmentId DESC")
	List<ItineraryAssignment> findByMenuItemIdsAndBlnIsDeletedFalse(@Param("menuItemIds") List<Long> menuItemIds);

	// Find by assignment code
	Optional<ItineraryAssignment> findByTxtAssignmentCodeAndBlnIsDeletedFalse(String txtAssignmentCode);

	// Get max assignment code number
	@Query("SELECT MAX(CAST(SUBSTRING(ia.txtAssignmentCode, 5) AS long)) FROM ItineraryAssignment ia WHERE ia.txtAssignmentCode LIKE 'IIA-%'")
	Optional<Long> findMaxAssignmentCodeNumber();

	// Check if assignment exists for menu item
	@Query("SELECT COUNT(ia) > 0 FROM ItineraryAssignment ia WHERE ia.menuItem.serMenuItemId = :menuItemId AND ia.blnIsDeleted = false")
	boolean existsByMenuItemId(@Param("menuItemId") Long menuItemId);

	// Find with details eager loading
	@Query("SELECT ia FROM ItineraryAssignment ia LEFT JOIN FETCH ia.itineraryAssignmentDetails WHERE ia.serItineraryAssignmentId = :id AND ia.blnIsDeleted = false")
	Optional<ItineraryAssignment> findByIdWithDetails(@Param("id") Long id);
}