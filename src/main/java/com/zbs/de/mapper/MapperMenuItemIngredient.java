package com.zbs.de.mapper;

import com.zbs.de.model.MenuItemIngredient;
import com.zbs.de.model.MenuItemIngredient.MenuItemIngredientKey;
import com.zbs.de.model.dto.DtoMenuItemIngredient;

public class MapperMenuItemIngredient {
	// -------------------------------------------------
	// ENTITY → DTO
	// -------------------------------------------------
	public DtoMenuItemIngredient toDto(MenuItemIngredient entity) {
		if (entity == null) {
			return null;
		}

		DtoMenuItemIngredient dto = new DtoMenuItemIngredient();

		dto.setIngredientId(entity.getId() != null ? entity.getId().getIngredientId() : null);
		dto.setNumQuantity(entity.getNumQuantity());
		dto.setTxtUOM(entity.getTxtUOM());

		return dto;
	}

	// -------------------------------------------------
	// DTO → ENTITY (requires the parent menuItemId)
	// -------------------------------------------------
	public MenuItemIngredient toEntity(DtoMenuItemIngredient dto, Long menuItemId) {
		if (dto == null) {
			return null;
		}

		MenuItemIngredient entity = new MenuItemIngredient();

		// ----- Composite Key -----
		MenuItemIngredientKey key = new MenuItemIngredientKey();
		key.setMenuItemId(menuItemId);
		key.setIngredientId(dto.getIngredientId());
		entity.setId(key);

		// ----- Other fields -----
		entity.setNumQuantity(dto.getNumQuantity());
		entity.setTxtUOM(dto.getTxtUOM());

		return entity;
	}

}
