package com.zbs.de.model.dto;

import java.util.List;

public class DtoComponentGroup {
	private String displayName;
	private String componentKind;
	private Integer minSelections;
	private Integer maxSelections;
	private List<DtoMenuItem> items;

	// Getters and Setters
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getComponentKind() {
		return componentKind;
	}

	public void setComponentKind(String componentKind) {
		this.componentKind = componentKind;
	}

	public Integer getMinSelections() {
		return minSelections;
	}

	public void setMinSelections(Integer minSelections) {
		this.minSelections = minSelections;
	}

	public Integer getMaxSelections() {
		return maxSelections;
	}

	public void setMaxSelections(Integer maxSelections) {
		this.maxSelections = maxSelections;
	}

	public List<DtoMenuItem> getItems() {
		return items;
	}

	public void setItems(List<DtoMenuItem> items) {
		this.items = items;
	}
}