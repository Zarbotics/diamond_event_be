package com.zbs.de.model.dto;

import java.util.List;

public class DtoSelectionGroupRequest {

	private Long parentMenuItemId;
	private String groupDisplayName; // e.g., "Selections Include"
	private String txtComponentKind = "SELECTION"; // SELECTION, INCLUDED, OPTIONAL
	private List<Long> menuItemIds;
	private Integer numSelectionMin = 1;
	private Integer numSelectionMax;
	private Boolean markAsComposite = true;
	private Integer startSequenceOrder = 1;

	// Getters and Setters
	public Long getParentMenuItemId() {
		return parentMenuItemId;
	}

	public void setParentMenuItemId(Long parentMenuItemId) {
		this.parentMenuItemId = parentMenuItemId;
	}

	public String getGroupDisplayName() {
		return groupDisplayName;
	}

	public void setGroupDisplayName(String groupDisplayName) {
		this.groupDisplayName = groupDisplayName;
	}

	public String getTxtComponentKind() {
		return txtComponentKind;
	}

	public void setTxtComponentKind(String txtComponentKind) {
		this.txtComponentKind = txtComponentKind;
	}

	public List<Long> getMenuItemIds() {
		return menuItemIds;
	}

	public void setMenuItemIds(List<Long> menuItemIds) {
		this.menuItemIds = menuItemIds;
	}

	public Integer getNumSelectionMin() {
		return numSelectionMin;
	}

	public void setNumSelectionMin(Integer numSelectionMin) {
		this.numSelectionMin = numSelectionMin;
	}

	public Integer getNumSelectionMax() {
		return numSelectionMax;
	}

	public void setNumSelectionMax(Integer numSelectionMax) {
		this.numSelectionMax = numSelectionMax;
	}

	public Boolean getMarkAsComposite() {
		return markAsComposite;
	}

	public void setMarkAsComposite(Boolean markAsComposite) {
		this.markAsComposite = markAsComposite;
	}

	public Integer getStartSequenceOrder() {
		return startSequenceOrder;
	}

	public void setStartSequenceOrder(Integer startSequenceOrder) {
		this.startSequenceOrder = startSequenceOrder;
	}
}