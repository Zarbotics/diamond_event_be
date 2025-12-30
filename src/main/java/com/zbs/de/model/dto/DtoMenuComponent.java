package com.zbs.de.model.dto;

import java.util.List;
import java.util.Map;

public class DtoMenuComponent {
	private Long serComponentId;
	private Long parentMenuItemId;
	private Long childMenuItemId;
	private Integer serComponenetKindRoleId; // Component kind role (INCLUDED|SELECTION|OPTIONAL|GROUP)
	private String txtComponenetKindRoleCode;
	private String txtComponenetKindRoleName;
	private String txtDisplayName;
	private Integer numSelectionMin;
	private Integer numSelectionMax;
	private Integer numSequenceOrder;
	private List<DtoMenuItem> componentItems;
	private Map<String, Object> metadata;

	// Getters and Setters
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

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	public List<DtoMenuItem> getComponentItems() {
		return componentItems;
	}

	public void setComponentItems(List<DtoMenuItem> componentItems) {
		this.componentItems = componentItems;
	}

	public Integer getSerComponenetKindRoleId() {
		return serComponenetKindRoleId;
	}

	public void setSerComponenetKindRoleId(Integer serComponenetKindRoleId) {
		this.serComponenetKindRoleId = serComponenetKindRoleId;
	}

	public String getTxtComponenetKindRoleCode() {
		return txtComponenetKindRoleCode;
	}

	public void setTxtComponenetKindRoleCode(String txtComponenetKindRoleCode) {
		this.txtComponenetKindRoleCode = txtComponenetKindRoleCode;
	}

	public String getTxtComponenetKindRoleName() {
		return txtComponenetKindRoleName;
	}

	public void setTxtComponenetKindRoleName(String txtComponenetKindRoleName) {
		this.txtComponenetKindRoleName = txtComponenetKindRoleName;
	}

}
