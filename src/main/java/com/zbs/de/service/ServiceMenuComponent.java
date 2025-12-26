package com.zbs.de.service;

import java.util.List;
import java.util.Map;

import com.zbs.de.model.dto.DtoMenuComponent;
import com.zbs.de.model.dto.DtoMenuComponentRequest;
import com.zbs.de.model.dto.DtoMenuItemRole;
import com.zbs.de.model.dto.DtoResult;

public interface ServiceMenuComponent {

	DtoResult saveOrUpdateComponents(DtoMenuComponentRequest request);

	List<DtoMenuComponent> getComponentsByParentId(Long parentMenuItemId);

	DtoResult deleteComponent(Long componentId);

	DtoResult deleteComponentsByGroup(Long parentMenuItemId, Integer roleId);

	List<DtoMenuItemRole> getAvailableComponentRoles();

	List<DtoMenuItemRole> getUsedComponentRoles(Long parentMenuItemId);

	Map<String, Object> validateComponentGroup(DtoMenuComponent group);
}