package com.zbs.de.service;

import java.util.List;

import com.zbs.de.model.dto.DtoMenuItemRole;

public interface ServiceMenuItemRole {

	DtoMenuItemRole saveOrUpdate(DtoMenuItemRole dto);

	DtoMenuItemRole getById(Integer id);

	List<DtoMenuItemRole> getAllCompositionRoles();
	
	List<DtoMenuItemRole> getAllMenuItemRoles();
	
	List<DtoMenuItemRole> getAllActiveMenuItemRoles();
	
	List<DtoMenuItemRole> getAllActiveCompositionRoles();

	List<DtoMenuItemRole> getAllRoles();

	List<DtoMenuItemRole> getAllActiveRoles();
}
