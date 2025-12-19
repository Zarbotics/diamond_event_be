package com.zbs.de.service;

import java.util.List;

import com.zbs.de.model.dto.DtoItineraryBulkAssignmentRequest;
import com.zbs.de.model.dto.DtoItineraryAssignment;
import com.zbs.de.model.dto.DtoRemoveItineraryItemsRequest;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.ItineraryAssignment;

public interface ServiceItineraryAssignment {

	DtoResult create(DtoItineraryAssignment dto);

	DtoResult update(DtoItineraryAssignment dto);

	DtoResult getById(Long id);

	DtoResult getAll();

	DtoResult getAllActive();

	DtoResult getByMenuItemId(Long menuItemId);

	DtoResult getByMenuItemIds(List<Long> menuItemIds);

	DtoResult delete(Long id);

	DtoResult deactivate(Long id);

	DtoResult activate(Long id);

	DtoResult approve(Long id);

	// Bulk operations
	DtoResult assignListOfItineraryItemsToMultipleItems(DtoItineraryBulkAssignmentRequest request);

	DtoResult removeItineraryItemFromAssignment(DtoRemoveItineraryItemsRequest request);

	DtoResult removeAllItineraryItemsFromAssignment(Long assignmentId);

	// Helper methods
	ItineraryAssignment getAssignmentEntityById(Long id);

	String generateAssignmentCode();
	
	List<String> getAllItineraryUnits();
}