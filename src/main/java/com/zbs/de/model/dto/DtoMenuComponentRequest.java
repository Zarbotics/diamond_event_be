package com.zbs.de.model.dto;

import java.util.List;

public class DtoMenuComponentRequest {
	private Long parentMenuItemId;
	private List<DtoMenuComponent> components;

	// Getters and Setters
	public Long getParentMenuItemId() {
		return parentMenuItemId;
	}

	public void setParentMenuItemId(Long parentMenuItemId) {
		this.parentMenuItemId = parentMenuItemId;
	}

	public List<DtoMenuComponent> getComponents() {
		return components;
	}

	public void setComponents(List<DtoMenuComponent> components) {
		this.components = components;
	}
}