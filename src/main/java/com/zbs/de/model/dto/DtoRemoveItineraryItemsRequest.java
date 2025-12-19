package com.zbs.de.model.dto;

import java.util.List;

public class DtoRemoveItineraryItemsRequest {

	private Long serItineraryAssignmentId;
	private List<Long> itineraryItemIds; // Remove specific items
	private Boolean removeAll = false; // Remove all items

	// Getters and Setters
	public Long getSerItineraryAssignmentId() {
		return serItineraryAssignmentId;
	}

	public void setSerItineraryAssignmentId(Long serItineraryAssignmentId) {
		this.serItineraryAssignmentId = serItineraryAssignmentId;
	}

	public List<Long> getItineraryItemIds() {
		return itineraryItemIds;
	}

	public void setItineraryItemIds(List<Long> itineraryItemIds) {
		this.itineraryItemIds = itineraryItemIds;
	}

	public Boolean getRemoveAll() {
		return removeAll;
	}

	public void setRemoveAll(Boolean removeAll) {
		this.removeAll = removeAll;
	}
}