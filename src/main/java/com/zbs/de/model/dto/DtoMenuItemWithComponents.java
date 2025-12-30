package com.zbs.de.model.dto;

import java.util.List;

public class DtoMenuItemWithComponents {

	private DtoMenuItem menuItem;
	private List<DtoComponentGroup> componentGroups;

	// Getters and Setters
	public DtoMenuItem getMenuItem() {
		return menuItem;
	}

	public void setMenuItem(DtoMenuItem menuItem) {
		this.menuItem = menuItem;
	}

	public List<DtoComponentGroup> getComponentGroups() {
		return componentGroups;
	}

	public void setComponentGroups(List<DtoComponentGroup> componentGroups) {
		this.componentGroups = componentGroups;
	}
}
