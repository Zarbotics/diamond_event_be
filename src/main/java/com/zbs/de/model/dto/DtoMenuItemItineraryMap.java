package com.zbs.de.model.dto;

import java.util.Map;

public class DtoMenuItemItineraryMap {
	private Long id;
	private Long menuItemId;
	private Long itineraryItemId;
	private String txtMultiplierType; // PER_GUEST, PER_DISH, etc.
	private Double txtMultiplierValue;
	private Map<String, Object> dependencyExpression;
	private Map<String, Object> metadata;

	public DtoMenuItemItineraryMap() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DtoMenuItemItineraryMap(Long id, Long menuItemId, Long itineraryItemId, String txtMultiplierType,
			Double txtMultiplierValue, Map<String, Object> dependencyExpression, Map<String, Object> metadata) {
		super();
		this.id = id;
		this.menuItemId = menuItemId;
		this.itineraryItemId = itineraryItemId;
		this.txtMultiplierType = txtMultiplierType;
		this.txtMultiplierValue = txtMultiplierValue;
		this.dependencyExpression = dependencyExpression;
		this.metadata = metadata;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getMenuItemId() {
		return menuItemId;
	}

	public void setMenuItemId(Long menuItemId) {
		this.menuItemId = menuItemId;
	}

	public Long getItineraryItemId() {
		return itineraryItemId;
	}

	public void setItineraryItemId(Long itineraryItemId) {
		this.itineraryItemId = itineraryItemId;
	}

	public String getTxtMultiplierType() {
		return txtMultiplierType;
	}

	public void setTxtMultiplierType(String txtMultiplierType) {
		this.txtMultiplierType = txtMultiplierType;
	}

	public Double getTxtMultiplierValue() {
		return txtMultiplierValue;
	}

	public void setTxtMultiplierValue(Double txtMultiplierValue) {
		this.txtMultiplierValue = txtMultiplierValue;
	}

	public Map<String, Object> getDependencyExpression() {
		return dependencyExpression;
	}

	public void setDependencyExpression(Map<String, Object> dependencyExpression) {
		this.dependencyExpression = dependencyExpression;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

}
