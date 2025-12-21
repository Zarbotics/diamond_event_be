package com.zbs.de.service;

import java.util.List;

import com.zbs.de.model.dto.DtoMenuComponent;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSelectionGroupRequest;

public interface ServiceMenuComponent {

	DtoResult createComponent(DtoMenuComponent request);

	DtoResult updateComponent(DtoMenuComponent dto);

	DtoResult deleteComponent(Long componentId);

	DtoResult getComponentById(Long componentId);

	// Group operations
	DtoResult createSelectionGroup(DtoSelectionGroupRequest request);

	DtoResult deleteComponentsByParent(Long parentMenuItemId);

	DtoResult deleteComponentsByParentAndDisplayName(Long parentMenuItemId, String displayName);

	// Queries
	DtoResult getComponentsByMenuItem(Long menuItemId);

	DtoResult getActiveComponentsByMenuItem(Long menuItemId);

	DtoResult getComponentsGrouped(Long menuItemId);

	DtoResult getMenuItemWithComponents(Long menuItemId);

	// Bulk operations
	DtoResult updateComponentSequence(List<Long> componentIds); // Reorder

	DtoResult copyComponents(Long sourceMenuItemId, Long targetMenuItemId);
}
