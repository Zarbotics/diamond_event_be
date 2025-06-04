package com.zbs.de.mapper;

import com.zbs.de.model.DecorItemMaster;
import com.zbs.de.model.dto.DtoDecorItemMaster;

public class MapperDecorItemMaster {
	public static DtoDecorItemMaster toDto(DecorItemMaster entity) {
		DtoDecorItemMaster dto = new DtoDecorItemMaster();
		dto.setSerDecorItemId(entity.getSerDecorItemId());
		dto.setTxtDecorCode(entity.getTxtDecorCode());
		dto.setTxtDecorName(entity.getTxtDecorName());
		dto.setTxtDescription(entity.getTxtDescription());
		dto.setNumPrice(entity.getNumPrice());
		return dto;
	}

	public static DecorItemMaster toEntity(DtoDecorItemMaster dto) {
		DecorItemMaster entity = new DecorItemMaster();
		entity.setSerDecorItemId(dto.getSerDecorItemId());
		entity.setTxtDecorCode(dto.getTxtDecorCode());
		entity.setTxtDecorName(dto.getTxtDecorName());
		entity.setTxtDescription(dto.getTxtDescription());
		entity.setNumPrice(dto.getNumPrice());
		return entity;
	}
}