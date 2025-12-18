package com.zbs.de.model.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DtoItineraryAssignment {

	private Long serItineraryAssignmentId;
	private Long serMenuItemId;
	private String txtAssignmentName;
	private String txtAssignmentCode; // Auto-generated: IIA-1001
	private String txtDescription;
	private Integer numTotalItineraries = 0;

	private String dteValidFrom;
	private String dteValidTo;

	private Integer numPriority = 1;
	private Boolean blnIsActive = true;
	private Boolean blnIsApproved = false;
	private Map<String, Object> metadata;

	private List<DtoItineraryAssignmentDetail> details;

	// Transient fields for UI
	private String menuItemCode;
	private String menuItemName;

	// Constructors
	public DtoItineraryAssignment() {
	}

	public DtoItineraryAssignment(Long serItineraryAssignmentId, String txtAssignmentCode, String txtAssignmentName,
			String dteValidFrom, String dteValidTo, Integer numPriority, Boolean blnIsActive) {
		this.serItineraryAssignmentId = serItineraryAssignmentId;
		this.txtAssignmentCode = txtAssignmentCode;
		this.txtAssignmentName = txtAssignmentName;
		this.dteValidFrom = dteValidFrom;
		this.dteValidTo = dteValidTo;
		this.numPriority = numPriority;
		this.blnIsActive = blnIsActive;
	}

	// Getters and Setters
	public Long getSerItineraryAssignmentId() {
		return serItineraryAssignmentId;
	}

	public void setSerItineraryAssignmentId(Long serItineraryAssignmentId) {
		this.serItineraryAssignmentId = serItineraryAssignmentId;
	}

	public Long getSerMenuItemId() {
		return serMenuItemId;
	}

	public void setSerMenuItemId(Long serMenuItemId) {
		this.serMenuItemId = serMenuItemId;
	}

	public String getTxtAssignmentName() {
		return txtAssignmentName;
	}

	public void setTxtAssignmentName(String txtAssignmentName) {
		this.txtAssignmentName = txtAssignmentName;
	}

	public String getTxtAssignmentCode() {
		return txtAssignmentCode;
	}

	public void setTxtAssignmentCode(String txtAssignmentCode) {
		this.txtAssignmentCode = txtAssignmentCode;
	}

	public String getTxtDescription() {
		return txtDescription;
	}

	public void setTxtDescription(String txtDescription) {
		this.txtDescription = txtDescription;
	}

	public Integer getNumTotalItineraries() {
		return numTotalItineraries;
	}

	public void setNumTotalItineraries(Integer numTotalItineraries) {
		this.numTotalItineraries = numTotalItineraries;
	}

	public String getDteValidFrom() {
		return dteValidFrom;
	}

	public void setDteValidFrom(String dteValidFrom) {
		this.dteValidFrom = dteValidFrom;
	}

	public String getDteValidTo() {
		return dteValidTo;
	}

	public void setDteValidTo(String dteValidTo) {
		this.dteValidTo = dteValidTo;
	}

	public Integer getNumPriority() {
		return numPriority;
	}

	public void setNumPriority(Integer numPriority) {
		this.numPriority = numPriority;
	}

	public Boolean getBlnIsActive() {
		return blnIsActive;
	}

	public void setBlnIsActive(Boolean blnIsActive) {
		this.blnIsActive = blnIsActive;
	}

	public Boolean getBlnIsApproved() {
		return blnIsApproved;
	}

	public void setBlnIsApproved(Boolean blnIsApproved) {
		this.blnIsApproved = blnIsApproved;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	public List<DtoItineraryAssignmentDetail> getDetails() {
		return details;
	}

	public void setDetails(List<DtoItineraryAssignmentDetail> details) {
		this.details = details;
	}

	public String getMenuItemCode() {
		return menuItemCode;
	}

	public void setMenuItemCode(String menuItemCode) {
		this.menuItemCode = menuItemCode;
	}

	public String getMenuItemName() {
		return menuItemName;
	}

	public void setMenuItemName(String menuItemName) {
		this.menuItemName = menuItemName;
	}
}