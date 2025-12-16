package com.zbs.de.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zbs.de.model.dto.DtoMenuItemRole;
import com.zbs.de.model.MenuItemRole;
import com.zbs.de.repository.RepositoryMenuItemRole;
import com.zbs.de.service.ServiceMenuItemRole;

@Service
public class ServiceMenuItemRoleImpl implements ServiceMenuItemRole {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceMenuItemRoleImpl.class);

	@Autowired
	private RepositoryMenuItemRole repository;

	@Override
	public DtoMenuItemRole saveOrUpdate(DtoMenuItemRole dto) {
		LOGGER.info("Saving MenuItemRole DTO: {}", dto);
		try {
			MenuItemRole entity;
			MenuItemRole parentRole = null;
			if(dto.getParentMenuRoleId() != null) {
				Optional<MenuItemRole> optionalParent = repository
						.findBySerMenuItemRoleIdAndBlnIsDeletedFalse(dto.getParentMenuRoleId());
				if(optionalParent != null && !optionalParent.isEmpty()) {
					parentRole = optionalParent.get();
				}else {
					throw new RuntimeException("Parent Item Role not found");
				}
			}
			

			if (dto.getSerMenuItemRoleId() != null) {
				Optional<MenuItemRole> optional = repository
						.findBySerMenuItemRoleIdAndBlnIsDeletedFalse(dto.getSerMenuItemRoleId());
				if (optional.isEmpty()) {
					throw new RuntimeException("Menu Item Role not found for update");
				}
				entity = optional.get();
				entity.setUpdatedDate(new Date());
				entity.setBlnIsActive(dto.getBlnIsActive());
			} else {
				entity = new MenuItemRole();
				entity.setBlnIsActive(true);
				entity.setBlnIsDeleted(false);
				entity.setBlnIsApproved(true);
			}

			entity.setTxtRoleCode(dto.getTxtRoleCode());
			entity.setTxtRoleName(dto.getTxtRoleName());
			validateParent(entity, parentRole);
			entity.setParentMenuItemRole(parentRole);
			entity.setBlnIsCompositionRole(dto.getBlnIsCompositionRole());

			MenuItemRole saved = repository.save(entity);
			return toDto(saved);

		} catch (Exception e) {
			LOGGER.error("Error saving MenuItemRole", e);
			throw new RuntimeException("Failed to save Menu Item Role: " + e.getMessage());
		}
	}

	@Override
	public DtoMenuItemRole getById(Integer id) {
		LOGGER.info("Fetching MenuItemRole by ID: {}", id);
		try {
			MenuItemRole entity = repository.findBySerMenuItemRoleIdAndBlnIsDeletedFalse(id)
					.orElseThrow(() -> new RuntimeException("Menu Item Role not found"));
			return toDto(entity);
		} catch (Exception e) {
			LOGGER.error("Error fetching MenuItemRole", e);
			throw new RuntimeException("Failed to fetch Menu Item Role: " + e.getMessage());
		}
	}

	
	@Override
	public MenuItemRole getMenuItemRoleById(Integer id) {
		LOGGER.info("Fetching MenuItemRole by ID: {}", id);
		try {
			MenuItemRole entity = repository.findBySerMenuItemRoleIdAndBlnIsDeletedFalse(id)
					.orElseThrow(() -> new RuntimeException("Menu Item Role not found"));
			return entity;
		} catch (Exception e) {
			LOGGER.error("Error fetching MenuItemRole", e);
			throw new RuntimeException("Failed to fetch Menu Item Role: " + e.getMessage());
		}
	}
	
	@Override
	public List<DtoMenuItemRole> getAllCompositionRoles() {
		LOGGER.info("Fetching all composition roles");
		try {
			return repository.findByBlnIsCompositionRoleTrueAndBlnIsDeletedFalseOrderBySerMenuItemRoleIdDesc().stream().map(this::toDto)
					.collect(Collectors.toList());
		} catch (Exception e) {
			LOGGER.error("Error fetching composition roles", e);
			throw new RuntimeException("Failed to fetch composition roles");
		}
	}

	@Override
	public List<DtoMenuItemRole> getAllMenuItemRoles() {
		LOGGER.info("Fetching all composition roles");
		try {
			return repository.findByBlnIsCompositionRoleFalseAndBlnIsDeletedFalseOrderBySerMenuItemRoleIdDesc().stream().map(this::toDto)
					.collect(Collectors.toList());
		} catch (Exception e) {
			LOGGER.error("Error fetching composition roles", e);
			throw new RuntimeException("Failed to fetch composition roles");
		}
	}

	@Override
	public List<DtoMenuItemRole> getAllActiveCompositionRoles() {
		LOGGER.info("Fetching all active composition roles");
		try {
			return repository.findByBlnIsCompositionRoleTrueAndBlnIsActiveTrueAndBlnIsDeletedFalseOrderBySerMenuItemRoleIdDesc().stream()
					.map(this::toDto).collect(Collectors.toList());
		} catch (Exception e) {
			LOGGER.error("Error fetching active composition roles", e);
			throw new RuntimeException("Failed to fetch active composition roles");
		}
	}

	@Override
	public List<DtoMenuItemRole> getAllActiveMenuItemRoles() {
		LOGGER.info("Fetching all active composition roles");
		try {
			return repository.findByBlnIsCompositionRoleFalseAndBlnIsActiveTrueAndBlnIsDeletedFalseOrderBySerMenuItemRoleIdDesc().stream()
					.map(this::toDto).collect(Collectors.toList());
		} catch (Exception e) {
			LOGGER.error("Error fetching active composition roles", e);
			throw new RuntimeException("Failed to fetch active composition roles");
		}
	}

	@Override
	public List<DtoMenuItemRole> getAllRoles() {
		LOGGER.info("Fetching all MenuItemRoles");
		try {
			return repository.findByBlnIsDeletedFalseOrderBySerMenuItemRoleIdDesc().stream().map(this::toDto).collect(Collectors.toList());
		} catch (Exception e) {
			LOGGER.error("Error fetching MenuItemRoles", e);
			throw new RuntimeException("Failed to fetch Menu Item Roles");
		}
	}

	@Override
	public List<DtoMenuItemRole> getAllActiveRoles() {
		LOGGER.info("Fetching all active MenuItemRoles");
		try {
			return repository.findByBlnIsActiveTrueAndBlnIsDeletedFalseOrderBySerMenuItemRoleIdDesc().stream().map(this::toDto)
					.collect(Collectors.toList());
		} catch (Exception e) {
			LOGGER.error("Error fetching active MenuItemRoles", e);
			throw new RuntimeException("Failed to fetch active Menu Item Roles");
		}
	}

	@Override
	public List<DtoMenuItemRole> getRolesByParentRoleId(Integer parentRoleId) {
		LOGGER.info("Fetching all composition roles");
		try {
			return repository.findByParentRoleId(parentRoleId).stream().map(this::toDto)
					.collect(Collectors.toList());
		} catch (Exception e) {
			LOGGER.error("Error fetching child roles", e);
			throw new RuntimeException("Failed to fetch composition roles");
		}
	}

	@Override
	public List<DtoMenuItemRole> getActiveRolesByParentRoleId(Integer parentRoleId) {
		LOGGER.info("Fetching all composition roles");
		try {
			return repository.findActiveByParentRoleId(parentRoleId).stream().map(this::toDto)
					.collect(Collectors.toList());
		} catch (Exception e) {
			LOGGER.error("Error fetching child roles", e);
			throw new RuntimeException("Failed to fetch composition roles");
		}
	}

	
	private void validateParent(MenuItemRole entity, MenuItemRole parent) {
	    if (parent == null) return;

	    // Self reference check
	    if (entity.getSerMenuItemRoleId() != null &&
	        entity.getSerMenuItemRoleId().equals(parent.getSerMenuItemRoleId())) {
	        throw new RuntimeException("A role cannot be parent of itself");
	    }

	    // Circular hierarchy check
	    MenuItemRole current = parent;
	    while (current != null) {
	        if (current.getSerMenuItemRoleId() != null &&
	            current.getSerMenuItemRoleId().equals(entity.getSerMenuItemRoleId())) {
	            throw new RuntimeException("Circular parent-child relationship detected");
	        }
	        current = current.getParentMenuItemRole();
	    }
	}

	
	/*
	 * ===================== Mapping =====================
	 */

	private DtoMenuItemRole toDto(MenuItemRole entity) {
		DtoMenuItemRole dto = new DtoMenuItemRole();
		dto.setSerMenuItemRoleId(entity.getSerMenuItemRoleId());
		dto.setTxtRoleCode(entity.getTxtRoleCode());
		dto.setTxtRoleName(entity.getTxtRoleName());
		dto.setBlnIsCompositionRole(entity.getBlnIsCompositionRole());
		dto.setBlnIsActive(entity.getBlnIsActive());
		if(entity.getParentMenuItemRole() != null) {
			dto.setParentMenuRoleId(entity.getParentMenuItemRole().getSerMenuItemRoleId());
		}
		return dto;
	}
}
