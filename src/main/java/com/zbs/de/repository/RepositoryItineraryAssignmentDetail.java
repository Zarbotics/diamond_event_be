package com.zbs.de.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.ItineraryAssignmentDetail;

@Repository
public interface RepositoryItineraryAssignmentDetail extends JpaRepository<ItineraryAssignmentDetail, Long> {

	// Find non-deleted details ordered by ID descending
	List<ItineraryAssignmentDetail> findByBlnIsDeletedFalseOrderBySerItineraryAssignmentDetailIdDesc();

	// Find by assignment ID
	List<ItineraryAssignmentDetail> findByItineraryAssignment_SerItineraryAssignmentIdAndBlnIsDeletedFalseOrderByNumDisplayOrderAsc(
			Long assignmentId);

	// Find active details by assignment ID
	List<ItineraryAssignmentDetail> findByItineraryAssignment_SerItineraryAssignmentIdAndBlnIsActiveTrueAndBlnIsDeletedFalseOrderByNumDisplayOrderAsc(
			Long assignmentId);

	// Find by itinerary item ID
	List<ItineraryAssignmentDetail> findByItineraryItem_SerItineraryItemIdAndBlnIsDeletedFalse(Long itineraryItemId);

	// Check if detail exists for assignment and itinerary item
	@Query("SELECT COUNT(iad) > 0 FROM ItineraryAssignmentDetail iad WHERE iad.itineraryAssignment.serItineraryAssignmentId = :assignmentId AND iad.itineraryItem.serItineraryItemId = :itineraryItemId AND iad.blnIsDeleted = false")
	boolean existsByAssignmentAndItineraryItem(@Param("assignmentId") Long assignmentId,
			@Param("itineraryItemId") Long itineraryItemId);

	// Find specific detail
	@Query("SELECT iad FROM ItineraryAssignmentDetail iad WHERE iad.itineraryAssignment.serItineraryAssignmentId = :assignmentId AND iad.itineraryItem.serItineraryItemId = :itineraryItemId AND iad.blnIsDeleted = false")
	Optional<ItineraryAssignmentDetail> findByAssignmentAndItineraryItem(@Param("assignmentId") Long assignmentId,
			@Param("itineraryItemId") Long itineraryItemId);

	// Soft delete by itinerary item IDs
	@Modifying
	@Query("UPDATE ItineraryAssignmentDetail iad SET iad.blnIsDeleted = true, iad.updatedDate = CURRENT_TIMESTAMP WHERE iad.itineraryAssignment.serItineraryAssignmentId = :assignmentId AND iad.itineraryItem.serItineraryItemId IN :itineraryItemIds")
	int softDeleteByItineraryItemIds(@Param("assignmentId") Long assignmentId,
			@Param("itineraryItemIds") List<Long> itineraryItemIds);

	// Soft delete all details for an assignment
	@Modifying
	@Query("UPDATE ItineraryAssignmentDetail iad SET iad.blnIsDeleted = true, iad.updatedDate = CURRENT_TIMESTAMP WHERE iad.itineraryAssignment.serItineraryAssignmentId = :assignmentId")
	int softDeleteAllByAssignmentId(@Param("assignmentId") Long assignmentId);

	// Update total count in assignment
	@Modifying
	@Query("UPDATE ItineraryAssignment ia SET ia.numTotalItineraries = (SELECT COUNT(iad) FROM ItineraryAssignmentDetail iad WHERE iad.itineraryAssignment.serItineraryAssignmentId = :assignmentId AND iad.blnIsDeleted = false) WHERE ia.serItineraryAssignmentId = :assignmentId")
	void updateTotalItinerariesCount(@Param("assignmentId") Long assignmentId);

	@Query("SELECT COUNT(iad) FROM ItineraryAssignmentDetail iad WHERE iad.itineraryAssignment.serItineraryAssignmentId = :assignmentId AND iad.blnIsDeleted = false")
	int countByItineraryAssignment_SerItineraryAssignmentIdAndBlnIsDeletedFalse(
			@Param("assignmentId") Long assignmentId);
}