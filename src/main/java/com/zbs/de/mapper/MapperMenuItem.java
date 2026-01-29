package com.zbs.de.mapper;

import com.zbs.de.model.MenuItem;
import com.zbs.de.model.dto.DtoMenuItem;
import com.zbs.de.util.enums.EnmPriceMultiplierType;

public class MapperMenuItem {

	// ----------------------------------------------------
	// ENTITY → DTO
	// ----------------------------------------------------
	public static DtoMenuItem toDto(MenuItem entity) {
		if (entity == null)
			return null;

		DtoMenuItem dto = new DtoMenuItem();

		dto.setSerMenuItemId(entity.getSerMenuItemId());
		dto.setTxtCode(entity.getTxtCode());
		dto.setTxtName(entity.getTxtName());
		dto.setTxtShortName(entity.getTxtShortName());
		dto.setTxtDescription(entity.getTxtDescription());
//		dto.setTxtRole(entity.getTxtRole());
		dto.setTxtType(entity.getTxtType());
		dto.setNumDisplayOrder(entity.getNumDisplayOrder());
		dto.setBlnIsSelectable(entity.getBlnIsSelectable());
		dto.setMetadata(entity.getMetadata());
		dto.setNumDefaultServingsPerGuest(entity.getNumDefaultServingsPerGuest());
		dto.setTxtPath(entity.getTxtPath());
		dto.setBlnIsActive(entity.getBlnIsActive());
		dto.setBlnIsCompostie(entity.getBlnIsComposite());
		dto.setBlnIsCateringItem(entity.getBlnIsCateringItem());
		dto.setNumPrice(entity.getNumPrice());

		// parentId (avoid triggering lazy load)
		dto.setParentId(entity.getParent() != null ? entity.getParent().getSerMenuItemId() : null);
		dto.setSerMenuItemRoleId(entity.getMenuItemRole() != null ? entity.getMenuItemRole().getSerMenuItemRoleId() : null);
		dto.setTxtRole(entity.getMenuItemRole() != null ? entity.getMenuItemRole().getTxtRoleName() : null);

		return dto;
	}

	// ----------------------------------------------------
	// DTO → ENTITY (used for creation)
	// ----------------------------------------------------
	public static MenuItem toEntity(DtoMenuItem dto) {
		if (dto == null)
			return null;

		MenuItem entity = new MenuItem();

		// ID is optional for create/update
		entity.setSerMenuItemId(dto.getSerMenuItemId());

		entity.setTxtCode(dto.getTxtCode());
		entity.setTxtName(dto.getTxtName());
		entity.setTxtShortName(dto.getTxtShortName());
		entity.setTxtDescription(dto.getTxtDescription());
		entity.setTxtRole(dto.getTxtRole());
		entity.setTxtType(dto.getTxtType());
		entity.setNumDisplayOrder(dto.getNumDisplayOrder());
		entity.setBlnIsSelectable(dto.getBlnIsSelectable());
		entity.setMetadata(dto.getMetadata());
		entity.setNumDefaultServingsPerGuest(dto.getNumDefaultServingsPerGuest());
		entity.setTxtPath(dto.getTxtPath());
		entity.setBlnIsComposite(dto.getBlnIsCompostie());
		entity.setBlnIsCateringItem(dto.getBlnIsCateringItem());
		entity.setNumPrice(dto.getNumPrice());
		entity.setEnmPriceMultiplierType(dto.getTxtPriceMultiplierType() != null ? EnmPriceMultiplierType.valueOf(dto.getTxtPriceMultiplierType()) : null);

		// Parent mapping (lazy reference — NO DB fetch)
		if (dto.getParentId() != null) {
			MenuItem parentRef = new MenuItem();
			parentRef.setSerMenuItemId(dto.getParentId());
			entity.setParent(parentRef);
		}

		return entity;
	}

	// ----------------------------------------------------
	// UPDATE EXISTING ENTITY (for PATCH/PUT operations)
	// ----------------------------------------------------
	public static void updateEntity(MenuItem entity, DtoMenuItem dto) {
		if (entity == null || dto == null)
			return;

		entity.setTxtCode(dto.getTxtCode());
		entity.setTxtName(dto.getTxtName());
		entity.setTxtShortName(dto.getTxtShortName());
		entity.setTxtDescription(dto.getTxtDescription());
		entity.setTxtRole(dto.getTxtRole());
		entity.setTxtType(dto.getTxtType());
		entity.setNumDisplayOrder(dto.getNumDisplayOrder());
		entity.setBlnIsSelectable(dto.getBlnIsSelectable());
		entity.setMetadata(dto.getMetadata());
		entity.setNumDefaultServingsPerGuest(dto.getNumDefaultServingsPerGuest());
		entity.setTxtPath(dto.getTxtPath());
		entity.setBlnIsComposite(dto.getBlnIsCompostie());
		entity.setBlnIsCateringItem(dto.getBlnIsCateringItem());
		entity.setNumPrice(dto.getNumPrice());
		entity.setEnmPriceMultiplierType(dto.getTxtPriceMultiplierType() != null ? EnmPriceMultiplierType.valueOf(dto.getTxtPriceMultiplierType()) : null);


		if (dto.getParentId() != null) {
			MenuItem parentRef = new MenuItem();
			parentRef.setSerMenuItemId(dto.getParentId());
			entity.setParent(parentRef);
		} else {
			entity.setParent(null);
		}
	}

}