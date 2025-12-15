package com.zbs.de.mapper;

import com.zbs.de.model.ItineraryItem;
import com.zbs.de.model.MenuItem;
import com.zbs.de.model.MenuItemItineraryMap;
import com.zbs.de.model.dto.DtoMenuItemItineraryMap;

public class MapperMenuItemItineraryMap {
	public static DtoMenuItemItineraryMap toDto(MenuItemItineraryMap entity) {
		if (entity == null) {
			return null;
		}

		DtoMenuItemItineraryMap dto = new DtoMenuItemItineraryMap();

		dto.setId(entity.getId());
		dto.setMenuItemId(entity.getMenuItem() != null ? entity.getMenuItem().getSerMenuItemId() : null);
		dto.setItineraryItemId(
				entity.getItineraryItem() != null ? entity.getItineraryItem().getSerItineraryItemId() : null);
		dto.setTxtMultiplierType(entity.getTxtMultiplierType());
		dto.setTxtMultiplierValue(entity.getTxtMultiplierValue());
		dto.setDependencyExpression(entity.getDependencyExpression());
		dto.setMetadata(entity.getMetadata());

		return dto;
	}

	public static MenuItemItineraryMap toEntity(DtoMenuItemItineraryMap dto) {
		if (dto == null) {
			return null;
		}

		MenuItemItineraryMap entity = new MenuItemItineraryMap();

		entity.setId(dto.getId());

		// Attach foreign key references without fetching full objects
		if (dto.getMenuItemId() != null) {
			MenuItem menuItem = new MenuItem();
			menuItem.setSerMenuItemId(dto.getMenuItemId()); // Reference only by ID
			entity.setMenuItem(menuItem);
		}

		if (dto.getItineraryItemId() != null) {
			ItineraryItem itineraryItem = new ItineraryItem();
			itineraryItem.setSerItineraryItemId(dto.getItineraryItemId());
			entity.setItineraryItem(itineraryItem);
		}

		entity.setTxtMultiplierType(dto.getTxtMultiplierType());
		entity.setTxtMultiplierValue(dto.getTxtMultiplierValue());
		entity.setDependencyExpression(dto.getDependencyExpression());
		entity.setMetadata(dto.getMetadata());

		return entity;
	}
}
