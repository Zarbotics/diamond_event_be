package com.zbs.de.model.dto;

import java.math.BigDecimal;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zbs.de.util.enums.EnmItineraryUnitType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DtoItineraryAssignmentDetail {

	private Long serItineraryAssignmentDetailId;
	private Long serItineraryAssignmentId;
	private Long serItineraryItemId;
	private Integer numDisplayOrder;
	private EnmItineraryUnitType enmMultiplierType = EnmItineraryUnitType.PER_GUEST;
	private BigDecimal numMultiplierValue = BigDecimal.ONE;
	private Boolean blnIsRequired = false;
	private BigDecimal numOverridePrice;
	private String txtNotes;
	private Map<String, Object> dependencyExpression;
	private Boolean blnIsActive = true;

	// Transient fields for UI
	private String itineraryItemCode;
	private String itineraryItemName;
	private String assignmentCode;

	// Constructors
	public DtoItineraryAssignmentDetail() {
	}

	public DtoItineraryAssignmentDetail(Long serItineraryAssignmentDetailId, Long serItineraryItemId,
			EnmItineraryUnitType enmMultiplierType, BigDecimal numMultiplierValue) {
		this.serItineraryAssignmentDetailId = serItineraryAssignmentDetailId;
		this.serItineraryItemId = serItineraryItemId;
		this.enmMultiplierType = enmMultiplierType;
		this.numMultiplierValue = numMultiplierValue;
	}

	// Getters and Setters
	public Long getSerItineraryAssignmentDetailId() {
		return serItineraryAssignmentDetailId;
	}

	public void setSerItineraryAssignmentDetailId(Long serItineraryAssignmentDetailId) {
		this.serItineraryAssignmentDetailId = serItineraryAssignmentDetailId;
	}

	public Long getSerItineraryAssignmentId() {
		return serItineraryAssignmentId;
	}

	public void setSerItineraryAssignmentId(Long serItineraryAssignmentId) {
		this.serItineraryAssignmentId = serItineraryAssignmentId;
	}

	public Long getSerItineraryItemId() {
		return serItineraryItemId;
	}

	public void setSerItineraryItemId(Long serItineraryItemId) {
		this.serItineraryItemId = serItineraryItemId;
	}

	public Integer getNumDisplayOrder() {
		return numDisplayOrder;
	}

	public void setNumDisplayOrder(Integer numDisplayOrder) {
		this.numDisplayOrder = numDisplayOrder;
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

	public BigDecimal getNumOverridePrice() {
		return numOverridePrice;
	}

	public void setNumOverridePrice(BigDecimal numOverridePrice) {
		this.numOverridePrice = numOverridePrice;
	}

	public String getTxtNotes() {
		return txtNotes;
	}

	public void setTxtNotes(String txtNotes) {
		this.txtNotes = txtNotes;
	}

	public Map<String, Object> getDependencyExpression() {
		return dependencyExpression;
	}

	public void setDependencyExpression(Map<String, Object> dependencyExpression) {
		this.dependencyExpression = dependencyExpression;
	}

	public Boolean getBlnIsActive() {
		return blnIsActive;
	}

	public void setBlnIsActive(Boolean blnIsActive) {
		this.blnIsActive = blnIsActive;
	}

	public String getItineraryItemCode() {
		return itineraryItemCode;
	}

	public void setItineraryItemCode(String itineraryItemCode) {
		this.itineraryItemCode = itineraryItemCode;
	}

	public String getItineraryItemName() {
		return itineraryItemName;
	}

	public void setItineraryItemName(String itineraryItemName) {
		this.itineraryItemName = itineraryItemName;
	}

	public String getAssignmentCode() {
		return assignmentCode;
	}

	public void setAssignmentCode(String assignmentCode) {
		this.assignmentCode = assignmentCode;
	}
}