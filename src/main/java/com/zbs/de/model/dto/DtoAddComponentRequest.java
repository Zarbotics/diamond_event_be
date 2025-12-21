package com.zbs.de.model.dto;

public class DtoAddComponentRequest {

	private Long parentMenuItemId;
	private Long childMenuItemId; // Optional - for empty component groups
	private String txtComponentKind; // SELECTION, INCLUDED, OPTIONAL, GROUP
	private String txtDisplayName;
	private Integer numSelectionMin;
	private Integer numSelectionMax;
	private Integer numSequenceOrder;

	public DtoAddComponentRequest() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DtoAddComponentRequest(Long parentMenuItemId, Long childMenuItemId, String txtComponentKind,
			String txtDisplayName, Integer numSelectionMin, Integer numSelectionMax, Integer numSequenceOrder) {
		super();
		this.parentMenuItemId = parentMenuItemId;
		this.childMenuItemId = childMenuItemId;
		this.txtComponentKind = txtComponentKind;
		this.txtDisplayName = txtDisplayName;
		this.numSelectionMin = numSelectionMin;
		this.numSelectionMax = numSelectionMax;
		this.numSequenceOrder = numSequenceOrder;
	}

	public Long getParentMenuItemId() {
		return parentMenuItemId;
	}

	public void setParentMenuItemId(Long parentMenuItemId) {
		this.parentMenuItemId = parentMenuItemId;
	}

	public Long getChildMenuItemId() {
		return childMenuItemId;
	}

	public void setChildMenuItemId(Long childMenuItemId) {
		this.childMenuItemId = childMenuItemId;
	}

	public String getTxtComponentKind() {
		return txtComponentKind;
	}

	public void setTxtComponentKind(String txtComponentKind) {
		this.txtComponentKind = txtComponentKind;
	}

	public String getTxtDisplayName() {
		return txtDisplayName;
	}

	public void setTxtDisplayName(String txtDisplayName) {
		this.txtDisplayName = txtDisplayName;
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

	public Integer getNumSequenceOrder() {
		return numSequenceOrder;
	}

	public void setNumSequenceOrder(Integer numSequenceOrder) {
		this.numSequenceOrder = numSequenceOrder;
	}

}
