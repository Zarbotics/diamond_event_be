package com.zbs.de.model.dto;

import java.math.BigDecimal;
import java.util.List;

import com.zbs.de.util.enums.EnmItineraryUnitType;

public class DtoItineraryBulkAssignmentRequest {

	private List<Long> menuItemIds;
	private Long serItineraryItemId;
	private EnmItineraryUnitType enmMultiplierType;
	private BigDecimal numMultiplierValue;
	private Boolean blnIsRequired = false;
	private String txtNotes;
	private String conflictResolution = "SKIP"; // SKIP, OVERWRITE, DUPLICATE

	// Getters and Setters
	public List<Long> getMenuItemIds() {
		return menuItemIds;
	}

	public void setMenuItemIds(List<Long> menuItemIds) {
		this.menuItemIds = menuItemIds;
	}

	public Long getSerItineraryItemId() {
		return serItineraryItemId;
	}

	public void setSerItineraryItemId(Long serItineraryItemId) {
		this.serItineraryItemId = serItineraryItemId;
	}

	public EnmItineraryUnitType getEnmMultiplierType() {
		return enmMultiplierType;
	}

	public void setEnmMultiplierType(EnmItineraryUnitType enmMultiplierType) {
		this.enmMultiplierType = enmMultiplierType;
	}

	public BigDecimal getNumMultiplierValue() {
		return numMultiplierValue;
	}

	public void setNumMultiplierValue(BigDecimal numMultiplierValue) {
		this.numMultiplierValue = numMultiplierValue;
	}

	public Boolean getBlnIsRequired() {
		return blnIsRequired;
	}

	public void setBlnIsRequired(Boolean blnIsRequired) {
		this.blnIsRequired = blnIsRequired;
	}

	public String getTxtNotes() {
		return txtNotes;
	}

	public void setTxtNotes(String txtNotes) {
		this.txtNotes = txtNotes;
	}

	public String getConflictResolution() {
		return conflictResolution;
	}

	public void setConflictResolution(String conflictResolution) {
		this.conflictResolution = conflictResolution;
	}
}