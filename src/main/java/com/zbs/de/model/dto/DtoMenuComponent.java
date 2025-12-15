package com.zbs.de.model.dto;

import java.util.Map;

public class DtoMenuComponent {
	private Long serComponentId;
	private Long parentMenuItemId;
	private Long childMenuItemId;
	private String txtComponentKind; // INCLUDED|SELECTION|OPTIONAL|GROUP
	private Integer numSelectionMin;
	private Integer numSelectionMax;
	private Integer numSequenceOrder;
	private Map<String, Object> metadata;

	public DtoMenuComponent() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DtoMenuComponent(Long serComponentId, Long parentMenuItemId, Long childMenuItemId, String txtComponentKind,
			Integer numSelectionMin, Integer numSelectionMax, Integer numSequenceOrder, Map<String, Object> metadata) {
		super();
		this.serComponentId = serComponentId;
		this.parentMenuItemId = parentMenuItemId;
		this.childMenuItemId = childMenuItemId;
		this.txtComponentKind = txtComponentKind;
		this.numSelectionMin = numSelectionMin;
		this.numSelectionMax = numSelectionMax;
		this.numSequenceOrder = numSequenceOrder;
		this.metadata = metadata;
	}

	public Long getSerComponentId() {
		return serComponentId;
	}

	public void setSerComponentId(Long serComponentId) {
		this.serComponentId = serComponentId;
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

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

}
