package com.zbs.de.mapper;

import java.util.HashMap;

import com.zbs.de.model.MenuComponent;
import com.zbs.de.model.MenuItem;
import com.zbs.de.model.dto.DtoMenuComponent;

public class MapperMenuComponent {

	public static DtoMenuComponent toDto(MenuComponent entity) {
		if (entity == null)
			return null;

		DtoMenuComponent dto = new DtoMenuComponent();
		dto.setSerComponentId(entity.getSerComponentId());

		// ParentMenuItemId from entity's parentMenuItem reference, safely
		dto.setParentMenuItemId(
				entity.getParentMenuItem() != null ? entity.getParentMenuItem().getSerMenuItemId() : null);

		// ChildMenuItemId similarly, may be null
		dto.setChildMenuItemId(entity.getChildMenuItem() != null ? entity.getChildMenuItem().getSerMenuItemId() : null);

		dto.setTxtComponentKind(entity.getTxtComponentKind());
		dto.setNumSelectionMin(entity.getNumSelectionMin());
		dto.setNumSelectionMax(entity.getNumSelectionMax());
		dto.setNumSequenceOrder(entity.getNumSequenceOrder());

		// Defensive copy or direct assignment depending on your use case
		dto.setMetadata(entity.getMetadata() != null ? new HashMap<>(entity.getMetadata()) : null);

		return dto;
	}

	public static MenuComponent toEntity(DtoMenuComponent dto) {
		if (dto == null)
			return null;

		MenuComponent entity = new MenuComponent();

		entity.setSerComponentId(dto.getSerComponentId());

		// Only set parentMenuItem and childMenuItem references by IDs.
		// Assuming you have a method or service to fetch MenuItem by ID.
		// If not, you can create placeholder MenuItem with ID only to avoid unnecessary
		// DB calls.
		if (dto.getParentMenuItemId() != null) {
			MenuItem parent = new MenuItem();
			parent.setSerMenuItemId(dto.getParentMenuItemId());
			entity.setParentMenuItem(parent);
		}

		if (dto.getChildMenuItemId() != null) {
			MenuItem child = new MenuItem();
			child.setSerMenuItemId(dto.getChildMenuItemId());
			entity.setChildMenuItem(child);
		}

		entity.setTxtComponentKind(dto.getTxtComponentKind());
		entity.setNumSelectionMin(dto.getNumSelectionMin());
		entity.setNumSelectionMax(dto.getNumSelectionMax());
		entity.setNumSequenceOrder(dto.getNumSequenceOrder());

		// Defensive copy or direct assignment
		entity.setMetadata(dto.getMetadata() != null ? new HashMap<>(dto.getMetadata()) : null);

		return entity;
	}
}
