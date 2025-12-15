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

			if (dto.getSerMenuItemRoleId() != null) {
				Optional<MenuItemRole> optional = repository
						.findBySerMenuItemRoleIdAndBlnIsDeletedFalse(dto.getSerMenuItemRoleId());
				if (optional.isEmpty()) {
					throw new RuntimeException("Menu Item Role not found for update");
				}
				entity = optional.get();
				entity.setUpdatedDate(new Date());
			} else {
				entity = new MenuItemRole();
				entity.setBlnIsActive(true);
				entity.setBlnIsDeleted(false);
				entity.setBlnIsApproved(true);
			}

			entity.setTxtRoleCode(dto.getTxtRoleCode());
			entity.setTxtRoleName(dto.getTxtRoleName());
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
	public List<DtoMenuItemRole> getAllCompositionRoles() {
		LOGGER.info("Fetching all composition roles");
		try {
			return repository.findByBlnIsCompositionRoleTrueAndBlnIsDeletedFalse().stream().map(this::toDto)
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
			return repository.findByBlnIsCompositionRoleFalseAndBlnIsDeletedFalse().stream().map(this::toDto)
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
			return repository.findByBlnIsCompositionRoleTrueAndBlnIsActiveTrueAndBlnIsDeletedFalse().stream()
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
			return repository.findByBlnIsCompositionRoleFalseAndBlnIsActiveTrueAndBlnIsDeletedFalse().stream()
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
			return repository.findByBlnIsDeletedFalse().stream().map(this::toDto).collect(Collectors.toList());
		} catch (Exception e) {
			LOGGER.error("Error fetching MenuItemRoles", e);
			throw new RuntimeException("Failed to fetch Menu Item Roles");
		}
	}

	@Override
	public List<DtoMenuItemRole> getAllActiveRoles() {
		LOGGER.info("Fetching all active MenuItemRoles");
		try {
			return repository.findByBlnIsActiveTrueAndBlnIsDeletedFalse().stream().map(this::toDto)
					.collect(Collectors.toList());
		} catch (Exception e) {
			LOGGER.error("Error fetching active MenuItemRoles", e);
			throw new RuntimeException("Failed to fetch active Menu Item Roles");
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
		return dto;
	}
}
