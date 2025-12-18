package com.zbs.de.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zbs.de.model.dto.DtoItineraryBulkAssignmentRequest;
import com.zbs.de.model.dto.DtoItineraryAssignment;
import com.zbs.de.model.dto.DtoItineraryAssignmentDetail;
import com.zbs.de.model.dto.DtoRemoveItineraryItemsRequest;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.mapper.MapperItineraryAssignment;
import com.zbs.de.model.ItineraryAssignment;
import com.zbs.de.model.ItineraryAssignmentDetail;
import com.zbs.de.model.ItineraryItem;
import com.zbs.de.model.MenuItem;
import com.zbs.de.repository.RepositoryItineraryAssignment;
import com.zbs.de.repository.RepositoryItineraryAssignmentDetail;
import com.zbs.de.repository.RepositoryItineraryItem;
import com.zbs.de.repository.RepositoryMenuItem;
import com.zbs.de.service.ServiceItineraryAssignment;
import com.zbs.de.util.UtilDateAndTime;
import com.zbs.de.util.enums.EnmItineraryUnitType;

@Service
@Transactional
public class ServiceItineraryAssignmentImpl implements ServiceItineraryAssignment {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceItineraryAssignmentImpl.class);

	@Autowired
	private RepositoryItineraryAssignment repository;

	@Autowired
	private RepositoryItineraryAssignmentDetail detailRepository;

	@Autowired
	private RepositoryMenuItem menuItemRepository;

	@Autowired
	private RepositoryItineraryItem itineraryItemRepository;

	@Override
	public DtoResult create(DtoItineraryAssignment dto) {
		try {
			// Validate menu item exists
			MenuItem menuItem = menuItemRepository.findById(dto.getSerMenuItemId())
					.orElseThrow(() -> new RuntimeException("Menu Item not found with ID: " + dto.getSerMenuItemId()));

			// Generate assignment code
			String assignmentCode = generateAssignmentCode();

			// Create assignment entity
			ItineraryAssignment entity = new ItineraryAssignment();
			entity.setMenuItem(menuItem);
			entity.setTxtAssignmentCode(assignmentCode);
			entity.setTxtAssignmentName(dto.getTxtAssignmentName());
			entity.setTxtDescription(dto.getTxtDescription());
			entity.setDteValidFrom(UtilDateAndTime.ddMMyyyyDashedStringToDate(dto.getDteValidFrom()));
			entity.setDteValidTo(UtilDateAndTime.ddMMyyyyDashedStringToDate(dto.getDteValidTo()));
			entity.setNumPriority(dto.getNumPriority() != null ? dto.getNumPriority() : 1);
			entity.setNumTotalItineraries(0);
			entity.setMetadata(dto.getMetadata());
			entity.setBlnIsActive(dto.getBlnIsActive() != null ? dto.getBlnIsActive() : true);
			entity.setBlnIsDeleted(false);
			entity.setBlnIsApproved(dto.getBlnIsApproved() != null ? dto.getBlnIsApproved() : false);

			// Set custom assignment code
			// Note: You'll need to add txtAssignmentCode field to ItineraryAssignment
			// entity
			// entity.setTxtAssignmentCode(assignmentCode);

			repository.save(entity);

			// Save details if provided
			if (dto.getDetails() != null && !dto.getDetails().isEmpty()) {
				saveAssignmentDetails(entity, dto.getDetails());
			}

			dto.setSerItineraryAssignmentId(entity.getSerItineraryAssignmentId());
			// dto.setTxtAssignmentCode(assignmentCode);

			// *** Fetching Result ****
			DtoItineraryAssignment dtoItineraryAssignment = MapperItineraryAssignment.convertToDto(entity);

			List<ItineraryAssignmentDetail> details = detailRepository
					.findByItineraryAssignment_SerItineraryAssignmentIdAndBlnIsDeletedFalseOrderByNumDisplayOrderAsc(
							entity.getSerItineraryAssignmentId());

			List<DtoItineraryAssignmentDetail> detailDtos = details.stream()
					.map(MapperItineraryAssignment::convertDetailToDto).collect(Collectors.toList());

			dtoItineraryAssignment.setDetails(detailDtos);

			return new DtoResult("Itinerary Assignment created successfully.", null, dtoItineraryAssignment, null);

		} catch (Exception e) {
			LOGGER.error("Error creating Itinerary Assignment", e);
			return new DtoResult("Failed to create Itinerary Assignment: " + e.getMessage(), null, null, null);
		}
	}

//	@Override
//	public DtoResult update(DtoItineraryAssignment dto) {
//		try {
//			ItineraryAssignment entity = repository.findById(dto.getSerItineraryAssignmentId())
//					.orElseThrow(() -> new RuntimeException(
//							"Itinerary Assignment not found with ID: " + dto.getSerItineraryAssignmentId()));
//
//			// Check if not deleted
//			if (Boolean.TRUE.equals(entity.getBlnIsDeleted())) {
//				return new DtoResult("Itinerary Assignment is deleted and cannot be updated.", null, null, null);
//			}
//
//			// Update basic fields
//			entity.setTxtAssignmentName(dto.getTxtAssignmentName());
//			entity.setTxtDescription(dto.getTxtDescription());
//			entity.setDteValidFrom(UtilDateAndTime.ddMMyyyyDashedStringToDate(dto.getDteValidFrom()));
//			entity.setDteValidTo(UtilDateAndTime.ddMMyyyyDashedStringToDate(dto.getDteValidTo()));
//			entity.setNumPriority(dto.getNumPriority());
//			entity.setMetadata(dto.getMetadata());
//
//			if (dto.getBlnIsActive() != null) {
//				entity.setBlnIsActive(dto.getBlnIsActive());
//			}
//
//			if (dto.getBlnIsApproved() != null) {
//				entity.setBlnIsApproved(dto.getBlnIsApproved());
//			}
//
//			repository.save(entity);
//
//			// *** Fetching Result ****
//			DtoItineraryAssignment dtoItineraryAssignment = MapperItineraryAssignment.convertToDto(entity);
//
//			List<ItineraryAssignmentDetail> details = detailRepository
//					.findByItineraryAssignment_SerItineraryAssignmentIdAndBlnIsDeletedFalseOrderByNumDisplayOrderAsc(
//							entity.getSerItineraryAssignmentId());
//
//			List<DtoItineraryAssignmentDetail> detailDtos = details.stream()
//					.map(MapperItineraryAssignment::convertDetailToDto).collect(Collectors.toList());
//
//			dtoItineraryAssignment.setDetails(detailDtos);
//
//			return new DtoResult("Itinerary Assignment updated successfully.", null, dtoItineraryAssignment, null);
//
//		} catch (Exception e) {
//			LOGGER.error("Error updating Itinerary Assignment", e);
//			return new DtoResult("Failed to update Itinerary Assignment: " + e.getMessage(), null, null, null);
//		}
//	}
	
	@Override
	@Transactional
	public DtoResult update(DtoItineraryAssignment dto) {
		try {
			ItineraryAssignment entity = repository.findById(dto.getSerItineraryAssignmentId())
					.orElseThrow(() -> new RuntimeException(
							"Itinerary Assignment not found with ID: " + dto.getSerItineraryAssignmentId()));

			// Check if not deleted
			if (Boolean.TRUE.equals(entity.getBlnIsDeleted())) {
				return new DtoResult("Itinerary Assignment is deleted and cannot be updated.", null, null, null);
			}

			// Update basic fields
			entity.setTxtAssignmentName(dto.getTxtAssignmentName());
			entity.setTxtDescription(dto.getTxtDescription());
			entity.setDteValidFrom(UtilDateAndTime.ddMMyyyyDashedStringToDate(dto.getDteValidFrom()));
			entity.setDteValidTo(UtilDateAndTime.ddMMyyyyDashedStringToDate(dto.getDteValidTo()));
			entity.setNumPriority(dto.getNumPriority());
			entity.setMetadata(dto.getMetadata());

			if (dto.getBlnIsActive() != null) {
				entity.setBlnIsActive(dto.getBlnIsActive());
			}

			if (dto.getBlnIsApproved() != null) {
				entity.setBlnIsApproved(dto.getBlnIsApproved());
			}

			// *** CRITICAL: Update details ***
			if (dto.getDetails() != null && !dto.getDetails().isEmpty()) {
				updateAssignmentDetails(entity, dto.getDetails());
			}

			repository.save(entity);

			// Fetch updated result with details
			DtoItineraryAssignment dtoItineraryAssignment = MapperItineraryAssignment.convertToDto(entity);

			List<ItineraryAssignmentDetail> details = detailRepository
					.findByItineraryAssignment_SerItineraryAssignmentIdAndBlnIsDeletedFalseOrderByNumDisplayOrderAsc(
							entity.getSerItineraryAssignmentId());

			List<DtoItineraryAssignmentDetail> detailDtos = details.stream()
					.map(MapperItineraryAssignment::convertDetailToDto).collect(Collectors.toList());

			dtoItineraryAssignment.setDetails(detailDtos);

			return new DtoResult("Itinerary Assignment updated successfully.", null, dtoItineraryAssignment, null);

		} catch (Exception e) {
			LOGGER.error("Error updating Itinerary Assignment", e);
			return new DtoResult("Failed to update Itinerary Assignment: " + e.getMessage(), null, null, null);
		}
	}

	// Add this helper method to handle detail updates
	private void updateAssignmentDetails(ItineraryAssignment assignment,
			List<DtoItineraryAssignmentDetail> detailDtos) {
		// Get existing details
		List<ItineraryAssignmentDetail> existingDetails = detailRepository
				.findByItineraryAssignment_SerItineraryAssignmentIdAndBlnIsDeletedFalseOrderByNumDisplayOrderAsc(
						assignment.getSerItineraryAssignmentId());

		// Create a map of existing details by itinerary item ID
		Map<Long, ItineraryAssignmentDetail> existingDetailMap = existingDetails.stream().collect(
				Collectors.toMap(detail -> detail.getItineraryItem().getSerItineraryItemId(), detail -> detail));

		// Track which items to keep
		Set<Long> incomingItineraryItemIds = new HashSet<>();

		// Process each incoming detail
		for (int i = 0; i < detailDtos.size(); i++) {
			DtoItineraryAssignmentDetail detailDto = detailDtos.get(i);
			Long itineraryItemId = detailDto.getSerItineraryItemId();

			// Validate itinerary item exists
			ItineraryItem itineraryItem = itineraryItemRepository.findById(itineraryItemId)
					.orElseThrow(() -> new RuntimeException("Itinerary Item not found with ID: " + itineraryItemId));

			incomingItineraryItemIds.add(itineraryItemId);

			if (existingDetailMap.containsKey(itineraryItemId)) {
				// Update existing detail
				ItineraryAssignmentDetail existingDetail = existingDetailMap.get(itineraryItemId);

				// Only update if the detail ID is not provided or matches
				if (detailDto.getSerItineraryAssignmentDetailId() == null
						|| detailDto.getSerItineraryAssignmentDetailId()
								.equals(existingDetail.getSerItineraryAssignmentDetailId())) {

					updateDetailFromDto(existingDetail, detailDto, itineraryItem, i + 1);
					detailRepository.save(existingDetail);
				}
			} else {
				// Create new detail
				ItineraryAssignmentDetail newDetail = new ItineraryAssignmentDetail();
				newDetail.setItineraryAssignment(assignment);
				newDetail.setItineraryItem(itineraryItem);
				newDetail.setNumDisplayOrder(i + 1);
				newDetail.setEnmMultiplierType(
						detailDto.getEnmMultiplierType() != null ? detailDto.getEnmMultiplierType()
								: EnmItineraryUnitType.PER_GUEST);
				newDetail.setNumMultiplierValue(
						detailDto.getNumMultiplierValue() != null ? detailDto.getNumMultiplierValue() : BigDecimal.ONE);
				newDetail.setBlnIsRequired(detailDto.getBlnIsRequired() != null ? detailDto.getBlnIsRequired() : false);
				newDetail.setTxtNotes(detailDto.getTxtNotes());
				newDetail.setDependencyExpression(detailDto.getDependencyExpression());
				newDetail.setBlnIsActive(true);
				newDetail.setBlnIsDeleted(false);

				detailRepository.save(newDetail);
			}
		}

		// Soft delete details that are not in the incoming list
		for (ItineraryAssignmentDetail existingDetail : existingDetails) {
			Long itineraryItemId = existingDetail.getItineraryItem().getSerItineraryItemId();
			if (!incomingItineraryItemIds.contains(itineraryItemId)) {
				existingDetail.setBlnIsDeleted(true);
				existingDetail.setBlnIsActive(false);
				detailRepository.save(existingDetail);
			}
		}

		// Update total count
		int totalCount = detailRepository.countByItineraryAssignment_SerItineraryAssignmentIdAndBlnIsDeletedFalse(
				assignment.getSerItineraryAssignmentId());
		assignment.setNumTotalItineraries(totalCount);
	}

	// Helper method to update detail from DTO
	private void updateDetailFromDto(ItineraryAssignmentDetail detail, DtoItineraryAssignmentDetail detailDto,
			ItineraryItem itineraryItem, Integer displayOrder) {
		detail.setItineraryItem(itineraryItem);
		detail.setNumDisplayOrder(displayOrder);

		if (detailDto.getEnmMultiplierType() != null) {
			detail.setEnmMultiplierType(detailDto.getEnmMultiplierType());
		}

		if (detailDto.getNumMultiplierValue() != null) {
			detail.setNumMultiplierValue(detailDto.getNumMultiplierValue());
		}

		if (detailDto.getBlnIsRequired() != null) {
			detail.setBlnIsRequired(detailDto.getBlnIsRequired());
		}

		if (detailDto.getTxtNotes() != null) {
			detail.setTxtNotes(detailDto.getTxtNotes());
		}

		if (detailDto.getDependencyExpression() != null) {
			detail.setDependencyExpression(detailDto.getDependencyExpression());
		}

		if (detailDto.getNumOverridePrice() != null) {
			detail.setNumOverridePrice(detailDto.getNumOverridePrice());
		}

		if (detailDto.getBlnIsActive() != null) {
			detail.setBlnIsActive(detailDto.getBlnIsActive());
		}
	}
	

	@Override
	public DtoResult getById(Long id) {
		try {
			ItineraryAssignment entity = repository.findById(id)
					.orElseThrow(() -> new RuntimeException("Itinerary Assignment not found with ID: " + id));

			if (Boolean.TRUE.equals(entity.getBlnIsDeleted())) {
				return new DtoResult("Itinerary Assignment is deleted.", null, null, null);
			}
			DtoItineraryAssignment dtoItineraryAssignment = MapperItineraryAssignment.convertToDto(entity);

			List<ItineraryAssignmentDetail> details = detailRepository
					.findByItineraryAssignment_SerItineraryAssignmentIdAndBlnIsDeletedFalseOrderByNumDisplayOrderAsc(
							entity.getSerItineraryAssignmentId());

			List<DtoItineraryAssignmentDetail> detailDtos = details.stream()
					.map(MapperItineraryAssignment::convertDetailToDto).collect(Collectors.toList());

			dtoItineraryAssignment.setDetails(detailDtos);

			return new DtoResult("Fetched successfully.", null, dtoItineraryAssignment, null);

		} catch (Exception e) {
			LOGGER.error("Error fetching Itinerary Assignment by ID", e);
			return new DtoResult("Failed to fetch Itinerary Assignment: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult getAll() {
		try {
			List<ItineraryAssignment> assignments = repository
					.findByBlnIsDeletedFalseOrderBySerItineraryAssignmentIdDesc();
			List<DtoItineraryAssignment> list = new ArrayList<>();
			for (ItineraryAssignment assignment : assignments) {
				DtoItineraryAssignment dtoItineraryAssignment = MapperItineraryAssignment.convertToDto(assignment);

				List<ItineraryAssignmentDetail> details = detailRepository
						.findByItineraryAssignment_SerItineraryAssignmentIdAndBlnIsDeletedFalseOrderByNumDisplayOrderAsc(
								assignment.getSerItineraryAssignmentId());

				List<DtoItineraryAssignmentDetail> detailDtos = details.stream()
						.map(MapperItineraryAssignment::convertDetailToDto).collect(Collectors.toList());

				dtoItineraryAssignment.setDetails(detailDtos);
				list.add(dtoItineraryAssignment);
			}
			return new DtoResult("Fetched successfully.", null, list, null);

		} catch (Exception e) {
			LOGGER.error("Error fetching all Itinerary Assignments", e);
			return new DtoResult("Failed to fetch Itinerary Assignments: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult getAllActive() {
		try {
			List<ItineraryAssignment> assignments = repository
					.findByBlnIsActiveTrueAndBlnIsDeletedFalseOrderBySerItineraryAssignmentIdDesc();
			List<DtoItineraryAssignment> list = new ArrayList<>();
			for (ItineraryAssignment assignment : assignments) {
				DtoItineraryAssignment dtoItineraryAssignment = MapperItineraryAssignment.convertToDto(assignment);

				List<ItineraryAssignmentDetail> details = detailRepository
						.findByItineraryAssignment_SerItineraryAssignmentIdAndBlnIsDeletedFalseOrderByNumDisplayOrderAsc(
								assignment.getSerItineraryAssignmentId());

				List<DtoItineraryAssignmentDetail> detailDtos = details.stream()
						.map(MapperItineraryAssignment::convertDetailToDto).collect(Collectors.toList());

				dtoItineraryAssignment.setDetails(detailDtos);
				list.add(dtoItineraryAssignment);
			}
			return new DtoResult("Fetched successfully.", null, list, null);

		} catch (Exception e) {
			LOGGER.error("Error fetching active Itinerary Assignments", e);
			return new DtoResult("Failed to fetch active Itinerary Assignments: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult getByMenuItemId(Long menuItemId) {
		try {
			List<ItineraryAssignment> assignments = repository
					.findByMenuItem_SerMenuItemIdAndBlnIsDeletedFalseOrderBySerItineraryAssignmentIdDesc(menuItemId);
			List<DtoItineraryAssignment> list = new ArrayList<>();
			for (ItineraryAssignment assignment : assignments) {
				DtoItineraryAssignment dtoItineraryAssignment = MapperItineraryAssignment.convertToDto(assignment);

				List<ItineraryAssignmentDetail> details = detailRepository
						.findByItineraryAssignment_SerItineraryAssignmentIdAndBlnIsDeletedFalseOrderByNumDisplayOrderAsc(
								assignment.getSerItineraryAssignmentId());

				List<DtoItineraryAssignmentDetail> detailDtos = details.stream()
						.map(MapperItineraryAssignment::convertDetailToDto).collect(Collectors.toList());

				dtoItineraryAssignment.setDetails(detailDtos);
				list.add(dtoItineraryAssignment);
			}
			return new DtoResult("Fetched successfully.", null, list, null);

		} catch (Exception e) {
			LOGGER.error("Error fetching Itinerary Assignments by Menu Item ID", e);
			return new DtoResult("Failed to fetch Itinerary Assignments: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult getByMenuItemIds(List<Long> menuItemIds) {
		try {
			if (menuItemIds == null || menuItemIds.isEmpty()) {
				return new DtoResult("Menu Item IDs list is empty.", null, new ArrayList<>(), null);
			}

			List<ItineraryAssignment> assignments = repository.findByMenuItemIdsAndBlnIsDeletedFalse(menuItemIds);
			List<DtoItineraryAssignment> list = new ArrayList<>();
			for (ItineraryAssignment assignment : assignments) {
				DtoItineraryAssignment dtoItineraryAssignment = MapperItineraryAssignment.convertToDto(assignment);

				List<ItineraryAssignmentDetail> details = detailRepository
						.findByItineraryAssignment_SerItineraryAssignmentIdAndBlnIsDeletedFalseOrderByNumDisplayOrderAsc(
								assignment.getSerItineraryAssignmentId());

				List<DtoItineraryAssignmentDetail> detailDtos = details.stream()
						.map(MapperItineraryAssignment::convertDetailToDto).collect(Collectors.toList());

				dtoItineraryAssignment.setDetails(detailDtos);
				list.add(dtoItineraryAssignment);
			}

			return new DtoResult("Fetched successfully.", null, list, null);

		} catch (Exception e) {
			LOGGER.error("Error fetching Itinerary Assignments by Menu Item IDs", e);
			return new DtoResult("Failed to fetch Itinerary Assignments: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult assignListOfItineraryItemsToMultipleItems(DtoItineraryBulkAssignmentRequest request) {
		try {
			if (request.getMenuItemIds() == null || request.getMenuItemIds().isEmpty()) {
				return new DtoResult("Menu Item IDs list is empty.", null, null, null);
			}

			if (request.getSerItineraryItemId() == null) {
				return new DtoResult("Itinerary Item ID is required.", null, null, null);
			}

			ItineraryItem itineraryItem = itineraryItemRepository.findById(request.getSerItineraryItemId()).orElseThrow(
					() -> new RuntimeException("Itinerary Item not found with ID: " + request.getSerItineraryItemId()));

			int successCount = 0;
			int skipCount = 0;
			int errorCount = 0;
			List<String> errors = new ArrayList<>();

			for (Long menuItemId : request.getMenuItemIds()) {
				try {
					// Get or create assignment for menu item
					List<ItineraryAssignment> assignments = repository
							.findByMenuItem_SerMenuItemIdAndBlnIsActiveTrueAndBlnIsDeletedFalseOrderBySerItineraryAssignmentIdDesc(
									menuItemId);

					ItineraryAssignment assignment;
					if (assignments.isEmpty()) {
						// Create new assignment
						MenuItem menuItem = menuItemRepository.findById(menuItemId)
								.orElseThrow(() -> new RuntimeException("Menu Item not found with ID: " + menuItemId));

						assignment = new ItineraryAssignment();
						assignment.setMenuItem(menuItem);
						assignment.setTxtAssignmentName("Auto-generated Assignment");
						assignment.setDteValidFrom(new Date());
						assignment.setBlnIsActive(true);
						assignment.setBlnIsDeleted(false);
						assignment.setBlnIsApproved(false);
						repository.save(assignment);
					} else {
						assignment = assignments.get(0); // Use first active assignment
					}

					// Check if itinerary item already assigned
					boolean exists = detailRepository.existsByAssignmentAndItineraryItem(
							assignment.getSerItineraryAssignmentId(), itineraryItem.getSerItineraryItemId());

					if (exists) {
						if ("SKIP".equals(request.getConflictResolution())) {
							skipCount++;
							continue;
						} else if ("OVERWRITE".equals(request.getConflictResolution())) {
							// Update existing detail
							Optional<ItineraryAssignmentDetail> existingDetail = detailRepository
									.findByAssignmentAndItineraryItem(assignment.getSerItineraryAssignmentId(),
											itineraryItem.getSerItineraryItemId());

							if (existingDetail.isPresent()) {
								ItineraryAssignmentDetail detail = existingDetail.get();
								detail.setEnmMultiplierType(request.getEnmMultiplierType());
								detail.setNumMultiplierValue(request.getNumMultiplierValue());
								detail.setBlnIsRequired(request.getBlnIsRequired());
								detail.setTxtNotes(request.getTxtNotes());
								detailRepository.save(detail);
								successCount++;
							}
							continue;
						}
						// For DUPLICATE, continue to create new
					}

					// Create new detail
					ItineraryAssignmentDetail detail = new ItineraryAssignmentDetail();
					detail.setItineraryAssignment(assignment);
					detail.setItineraryItem(itineraryItem);
					detail.setEnmMultiplierType(request.getEnmMultiplierType() != null ? request.getEnmMultiplierType()
							: com.zbs.de.util.enums.EnmItineraryUnitType.PER_GUEST);
					detail.setNumMultiplierValue(
							request.getNumMultiplierValue() != null ? request.getNumMultiplierValue() : BigDecimal.ONE);
					detail.setBlnIsRequired(request.getBlnIsRequired() != null ? request.getBlnIsRequired() : false);
					detail.setTxtNotes(request.getTxtNotes());
					detail.setBlnIsActive(true);
					detail.setBlnIsDeleted(false);

					// Set display order
					List<ItineraryAssignmentDetail> existingDetails = detailRepository
							.findByItineraryAssignment_SerItineraryAssignmentIdAndBlnIsDeletedFalseOrderByNumDisplayOrderAsc(
									assignment.getSerItineraryAssignmentId());
					detail.setNumDisplayOrder(existingDetails.size() + 1);

					detailRepository.save(detail);
					successCount++;

				} catch (Exception e) {
					errorCount++;
					errors.add("Menu Item ID " + menuItemId + ": " + e.getMessage());
				}
			}

			// Update total counts for affected assignments
			List<ItineraryAssignment> affectedAssignments = repository
					.findByMenuItemIdsAndBlnIsDeletedFalse(request.getMenuItemIds());

			for (ItineraryAssignment assignment : affectedAssignments) {
				detailRepository.updateTotalItinerariesCount(assignment.getSerItineraryAssignmentId());
			}

			String message = String.format("Bulk assignment completed. Success: %d, Skipped: %d, Failed: %d",
					successCount, skipCount, errorCount);

			return new DtoResult(message, errors, null, null);

		} catch (Exception e) {
			LOGGER.error("Error in bulk assignment", e);
			return new DtoResult("Failed to perform bulk assignment: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult removeItineraryItemFromAssignment(DtoRemoveItineraryItemsRequest request) {
		try {
			if (request.getSerItineraryAssignmentId() == null) {
				return new DtoResult("Assignment ID is required.", null, null, null);
			}

			ItineraryAssignment assignment = repository.findById(request.getSerItineraryAssignmentId())
					.orElseThrow(() -> new RuntimeException(
							"Assignment not found with ID: " + request.getSerItineraryAssignmentId()));

			if (Boolean.TRUE.equals(assignment.getBlnIsDeleted())) {
				return new DtoResult("Assignment is deleted.", null, null, null);
			}

			int removedCount = 0;

			if (Boolean.TRUE.equals(request.getRemoveAll())) {
				// Remove all items
				removedCount = detailRepository.softDeleteAllByAssignmentId(request.getSerItineraryAssignmentId());
			} else if (request.getItineraryItemIds() != null && !request.getItineraryItemIds().isEmpty()) {
				// Remove specific items
				removedCount = detailRepository.softDeleteByItineraryItemIds(request.getSerItineraryAssignmentId(),
						request.getItineraryItemIds());
			} else {
				return new DtoResult("No itinerary items specified for removal.", null, null, null);
			}

			// Update total count
			detailRepository.updateTotalItinerariesCount(request.getSerItineraryAssignmentId());

			return new DtoResult("Removed " + removedCount + " itinerary item(s) from assignment.", null, null, null);

		} catch (Exception e) {
			LOGGER.error("Error removing itinerary items from assignment", e);
			return new DtoResult("Failed to remove itinerary items: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult removeAllItineraryItemsFromAssignment(Long assignmentId) {
		try {
			DtoRemoveItineraryItemsRequest request = new DtoRemoveItineraryItemsRequest();
			request.setSerItineraryAssignmentId(assignmentId);
			request.setRemoveAll(true);
			return removeItineraryItemFromAssignment(request);

		} catch (Exception e) {
			LOGGER.error("Error removing all itinerary items from assignment", e);
			return new DtoResult("Failed to remove all itinerary items: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult delete(Long id) {
		try {
			ItineraryAssignment entity = repository.findById(id)
					.orElseThrow(() -> new RuntimeException("Itinerary Assignment not found with ID: " + id));

			if (Boolean.TRUE.equals(entity.getBlnIsDeleted())) {
				return new DtoResult("Itinerary Assignment is already deleted.", null, null, null);
			}

			entity.setBlnIsDeleted(true);
			entity.setBlnIsActive(false);
			repository.save(entity);

			// Also soft delete all details
			detailRepository.softDeleteAllByAssignmentId(id);

			return new DtoResult("Itinerary Assignment deleted successfully.", null, null, null);

		} catch (Exception e) {
			LOGGER.error("Error deleting Itinerary Assignment", e);
			return new DtoResult("Failed to delete Itinerary Assignment: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult deactivate(Long id) {
		try {
			ItineraryAssignment entity = repository.findById(id)
					.orElseThrow(() -> new RuntimeException("Itinerary Assignment not found with ID: " + id));

			if (Boolean.TRUE.equals(entity.getBlnIsDeleted())) {
				return new DtoResult("Itinerary Assignment is deleted.", null, null, null);
			}

			entity.setBlnIsActive(false);
			repository.save(entity);

			// *** Fetching Result ****
			DtoItineraryAssignment dtoItineraryAssignment = MapperItineraryAssignment.convertToDto(entity);

			List<ItineraryAssignmentDetail> details = detailRepository
					.findByItineraryAssignment_SerItineraryAssignmentIdAndBlnIsDeletedFalseOrderByNumDisplayOrderAsc(
							entity.getSerItineraryAssignmentId());

			List<DtoItineraryAssignmentDetail> detailDtos = details.stream()
					.map(MapperItineraryAssignment::convertDetailToDto).collect(Collectors.toList());

			dtoItineraryAssignment.setDetails(detailDtos);

			return new DtoResult("Itinerary Assignment deactivated successfully.", null, dtoItineraryAssignment, null);

		} catch (Exception e) {
			LOGGER.error("Error deactivating Itinerary Assignment", e);
			return new DtoResult("Failed to deactivate Itinerary Assignment: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult activate(Long id) {
		try {
			ItineraryAssignment entity = repository.findById(id)
					.orElseThrow(() -> new RuntimeException("Itinerary Assignment not found with ID: " + id));

			if (Boolean.TRUE.equals(entity.getBlnIsDeleted())) {
				return new DtoResult("Itinerary Assignment is deleted.", null, null, null);
			}

			entity.setBlnIsActive(true);
			repository.save(entity);

			// *** Fetching Result ****
			DtoItineraryAssignment dtoItineraryAssignment = MapperItineraryAssignment.convertToDto(entity);

			List<ItineraryAssignmentDetail> details = detailRepository
					.findByItineraryAssignment_SerItineraryAssignmentIdAndBlnIsDeletedFalseOrderByNumDisplayOrderAsc(
							entity.getSerItineraryAssignmentId());

			List<DtoItineraryAssignmentDetail> detailDtos = details.stream()
					.map(MapperItineraryAssignment::convertDetailToDto).collect(Collectors.toList());

			dtoItineraryAssignment.setDetails(detailDtos);

			return new DtoResult("Itinerary Assignment activated successfully.", null, dtoItineraryAssignment, null);

		} catch (Exception e) {
			LOGGER.error("Error activating Itinerary Assignment", e);
			return new DtoResult("Failed to activate Itinerary Assignment: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult approve(Long id) {
		try {
			ItineraryAssignment entity = repository.findById(id)
					.orElseThrow(() -> new RuntimeException("Itinerary Assignment not found with ID: " + id));

			if (Boolean.TRUE.equals(entity.getBlnIsDeleted())) {
				return new DtoResult("Itinerary Assignment is deleted.", null, null, null);
			}

			entity.setBlnIsApproved(true);
			repository.save(entity);

			// *** Fetching Result ****
			DtoItineraryAssignment dtoItineraryAssignment = MapperItineraryAssignment.convertToDto(entity);

			List<ItineraryAssignmentDetail> details = detailRepository
					.findByItineraryAssignment_SerItineraryAssignmentIdAndBlnIsDeletedFalseOrderByNumDisplayOrderAsc(
							entity.getSerItineraryAssignmentId());

			List<DtoItineraryAssignmentDetail> detailDtos = details.stream()
					.map(MapperItineraryAssignment::convertDetailToDto).collect(Collectors.toList());

			dtoItineraryAssignment.setDetails(detailDtos);

			return new DtoResult("Itinerary Assignment approved successfully.", null, dtoItineraryAssignment, null);

		} catch (Exception e) {
			LOGGER.error("Error approving Itinerary Assignment", e);
			return new DtoResult("Failed to approve Itinerary Assignment: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public ItineraryAssignment getAssignmentEntityById(Long id) {
		try {
			return repository.findByIdWithDetails(id)
					.orElseThrow(() -> new RuntimeException("Itinerary Assignment not found with ID: " + id));
		} catch (Exception e) {
			LOGGER.error("Error fetching assignment entity", e);
			throw new RuntimeException("Failed to fetch assignment entity: " + e.getMessage());
		}
	}

	@Override
	public String generateAssignmentCode() {
		try {
			Optional<Long> maxCodeNumber = repository.findMaxAssignmentCodeNumber();
			long nextNumber = maxCodeNumber.orElse(1000L) + 1;
			return String.format("IIA-%04d", nextNumber);
		} catch (Exception e) {
			LOGGER.error("Error generating assignment code", e);
			// Fallback: use timestamp
			return "IIA-" + System.currentTimeMillis() % 10000;
		}
	}

	// Private helper methods

	private void saveAssignmentDetails(ItineraryAssignment assignment, List<DtoItineraryAssignmentDetail> detailDtos) {
		int displayOrder = 1;

		for (DtoItineraryAssignmentDetail detailDto : detailDtos) {
			ItineraryItem itineraryItem = itineraryItemRepository.findById(detailDto.getSerItineraryItemId())
					.orElseThrow(() -> new RuntimeException(
							"Itinerary Item not found with ID: " + detailDto.getSerItineraryItemId()));

			ItineraryAssignmentDetail detail = new ItineraryAssignmentDetail();
			detail.setItineraryAssignment(assignment);
			detail.setItineraryItem(itineraryItem);
			detail.setNumDisplayOrder(displayOrder++);
			detail.setEnmMultiplierType(detailDto.getEnmMultiplierType());
			detail.setNumMultiplierValue(detailDto.getNumMultiplierValue());
			detail.setBlnIsRequired(detailDto.getBlnIsRequired());
			detail.setNumOverridePrice(detailDto.getNumOverridePrice());
			detail.setTxtNotes(detailDto.getTxtNotes());
			detail.setDependencyExpression(detailDto.getDependencyExpression());
			detail.setBlnIsActive(true);
			detail.setBlnIsDeleted(false);

			detailRepository.save(detail);
		}

		// Update total count
		assignment.setNumTotalItineraries(detailDtos.size());
		repository.save(assignment);
	}

}