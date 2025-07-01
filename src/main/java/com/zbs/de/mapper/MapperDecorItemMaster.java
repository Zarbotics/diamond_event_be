package com.zbs.de.mapper;

import com.zbs.de.model.DecorCategoryMaster;
import com.zbs.de.model.DecorItemMaster;
import com.zbs.de.model.dto.DtoDecorItemMaster;

public class MapperDecorItemMaster {
	public static DecorItemMaster toEntity(DtoDecorItemMaster dto) {
		DecorItemMaster entity = new DecorItemMaster();
		entity.setSerDecorItemId(dto.getSerDecorItemId());
		entity.setTxtDecorCode(dto.getTxtDecorCode());
		entity.setTxtDecorName(dto.getTxtDecorName());
		entity.setBlnIsColorRequired(dto.getBlnIsColorRequired());
		entity.setBlnIsImageRequired(dto.getBlnIsImageRequired());
		entity.setBlnIsCountRequired(dto.getBlnIsCountRequired());
		entity.setBlnIsActive(dto.getBlnIsActive());

		if (dto.getSerDecorCategoryId() != null) {
			DecorCategoryMaster category = new DecorCategoryMaster();
			category.setSerDecorCategoryId(dto.getSerDecorCategoryId());
			entity.setDecorCategoryMaster(category);
		}
		return entity;
	}

	public static DtoDecorItemMaster toDto(DecorItemMaster entity) {
		DtoDecorItemMaster dto = new DtoDecorItemMaster();
		dto.setSerDecorItemId(entity.getSerDecorItemId());
		dto.setTxtDecorCode(entity.getTxtDecorCode());
		dto.setTxtDecorName(entity.getTxtDecorName());
		dto.setBlnIsColorRequired(entity.getBlnIsColorRequired());
		dto.setBlnIsImageRequired(entity.getBlnIsImageRequired());
		dto.setBlnIsCountRequired(entity.getBlnIsCountRequired());
		dto.setBlnIsActive(entity.getBlnIsActive());

		if (entity.getDecorCategoryMaster() != null) {
			dto.setSerDecorCategoryId(entity.getDecorCategoryMaster().getSerDecorCategoryId());
			dto.setTxtDecorCategoryCode(entity.getDecorCategoryMaster().getTxtDecorCategoryCode());
			dto.setTxtDecorCategoryName(entity.getDecorCategoryMaster().getTxtDecorCategoryName());
		}
		return dto;
	}
}