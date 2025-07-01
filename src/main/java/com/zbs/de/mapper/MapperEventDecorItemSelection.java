package com.zbs.de.mapper;

import com.zbs.de.model.DecorColorMaster;
import com.zbs.de.model.DecorItemMaster;
import com.zbs.de.model.EventDecorItemSelection;
import com.zbs.de.model.EventMaster;
import com.zbs.de.model.dto.DtoEventDecorItemSelection;

public class MapperEventDecorItemSelection {
	public static EventDecorItemSelection toEntity(DtoEventDecorItemSelection dto) {
		EventDecorItemSelection entity = new EventDecorItemSelection();
		entity.setSerEventDecorItemId(dto.getSerEventDecorItemId());
		entity.setNumCount(dto.getNumCount());
		entity.setBlnIsActive(dto.getBlnIsActive());

		if (dto.getSerDecorItemId() != null) {
			DecorItemMaster item = new DecorItemMaster();
			item.setSerDecorItemId(dto.getSerDecorItemId());
			entity.setDecorItemMaster(item);
		}

		if (dto.getSerEventMasterId() != null) {
			EventMaster event = new EventMaster();
			event.setSerEventMasterId(dto.getSerEventMasterId());
			entity.setEventMaster(event);
		}

		if (dto.getSelectedColorId() != null) {
			DecorColorMaster color = new DecorColorMaster();
			color.setSerDecorColorId(dto.getSelectedColorId());
			entity.setDecorColorMaster(color);
		}

		return entity;
	}

	public static DtoEventDecorItemSelection toDto(EventDecorItemSelection entity) {
		DtoEventDecorItemSelection dto = new DtoEventDecorItemSelection();
		dto.setSerEventDecorItemId(entity.getSerEventDecorItemId());
		dto.setNumCount(entity.getNumCount());
		dto.setBlnIsActive(entity.getBlnIsActive());

		if (entity.getEventMaster() != null) {
			dto.setSerEventMasterId(entity.getEventMaster().getSerEventMasterId());
		}
		if (entity.getDecorItemMaster() != null) {
			dto.setSerDecorItemId(entity.getDecorItemMaster().getSerDecorItemId());
			dto.setTxtDecorCode(entity.getDecorItemMaster().getTxtDecorCode());
		}
		if (entity.getDecorColorMaster() != null) {
			dto.setSelectedColorId(entity.getDecorColorMaster().getSerDecorColorId());
			dto.setSelectedColorName(entity.getDecorColorMaster().getTxtColorName());
		}
		return dto;
	}
}
