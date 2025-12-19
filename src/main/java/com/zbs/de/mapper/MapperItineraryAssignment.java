package com.zbs.de.mapper;


import com.zbs.de.model.ItineraryAssignment;
import com.zbs.de.model.ItineraryAssignmentDetail;
import com.zbs.de.model.dto.DtoItineraryAssignment;
import com.zbs.de.model.dto.DtoItineraryAssignmentDetail;
import com.zbs.de.util.UtilDateAndTime;

public class MapperItineraryAssignment {
	public static DtoItineraryAssignment convertToDto(ItineraryAssignment entity) {
		if (entity == null) {
			return null;
		}
		DtoItineraryAssignment dto = new DtoItineraryAssignment();
		dto.setSerItineraryAssignmentId(entity.getSerItineraryAssignmentId());
		dto.setSerMenuItemId(entity.getMenuItem().getSerMenuItemId());
		dto.setTxtAssignmentCode(entity.getTxtAssignmentCode());
		dto.setTxtAssignmentName(entity.getTxtAssignmentName());
		dto.setTxtDescription(entity.getTxtDescription());
		dto.setNumTotalItineraries(entity.getNumTotalItineraries());
		dto.setDteValidFrom(UtilDateAndTime.mmddyyyyDateToString(entity.getDteValidFrom()));
		dto.setDteValidTo(UtilDateAndTime.mmddyyyyDateToString(entity.getDteValidTo()));
		dto.setNumPriority(entity.getNumPriority());
		dto.setBlnIsActive(entity.getBlnIsActive());
		dto.setBlnIsApproved(entity.getBlnIsApproved());
		dto.setMetadata(entity.getMetadata());

		// Set menu item info
		dto.setMenuItemCode(entity.getMenuItem().getTxtCode());
		dto.setMenuItemName(entity.getMenuItem().getTxtName());

		if(entity.getMenuItem() != null && entity.getMenuItem().getMenuItemRole() != null) {
			dto.setSerMenuItemRoleId(entity.getMenuItem().getMenuItemRole().getSerMenuItemRoleId());
			dto.setTxtRoleCode(entity.getMenuItem().getMenuItemRole().getTxtRoleCode());
		}
//		// Fetch and convert details
//		List<ItineraryAssignmentDetail> details = detailRepository
//				.findByItineraryAssignment_SerItineraryAssignmentIdAndBlnIsDeletedFalseOrderByNumDisplayOrderAsc(
//						entity.getSerItineraryAssignmentId());
//
//		List<DtoItineraryAssignmentDetail> detailDtos = details.stream().map(this::convertDetailToDto)
//				.collect(Collectors.toList());
//
//		dto.setDetails(detailDtos);

		return dto;
	}

	public static DtoItineraryAssignmentDetail convertDetailToDto(ItineraryAssignmentDetail detail) {

		if (detail == null) {
			return null;
		}
		DtoItineraryAssignmentDetail dto = new DtoItineraryAssignmentDetail();
		dto.setSerItineraryAssignmentDetailId(detail.getSerItineraryAssignmentDetailId());
		dto.setSerItineraryAssignmentId(detail.getItineraryAssignment().getSerItineraryAssignmentId());
		dto.setSerItineraryItemId(detail.getItineraryItem().getSerItineraryItemId());
		dto.setNumDisplayOrder(detail.getNumDisplayOrder());
		dto.setEnmMultiplierType(detail.getEnmMultiplierType());
		dto.setNumMultiplierValue(detail.getNumMultiplierValue());
		dto.setBlnIsRequired(detail.getBlnIsRequired());
		dto.setNumOverridePrice(detail.getNumOverridePrice());
		dto.setTxtNotes(detail.getTxtNotes());
		dto.setDependencyExpression(detail.getDependencyExpression());
		dto.setBlnIsActive(detail.getBlnIsActive());

		// Set itinerary item info
		dto.setItineraryItemCode(detail.getItineraryItem().getTxtCode());
		dto.setItineraryItemName(detail.getItineraryItem().getTxtName());

		return dto;
	}
}
