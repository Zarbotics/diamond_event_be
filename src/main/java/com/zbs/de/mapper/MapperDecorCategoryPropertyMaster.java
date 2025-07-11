package com.zbs.de.mapper;

import com.zbs.de.model.DecorCategoryMaster;
import com.zbs.de.model.DecorCategoryPropertyMaster;
import com.zbs.de.model.dto.DtoDecorCategoryPropertyMaster;

public class MapperDecorCategoryPropertyMaster {
	public static DecorCategoryPropertyMaster toEntity(DtoDecorCategoryPropertyMaster dto) {
		DecorCategoryPropertyMaster entity = new DecorCategoryPropertyMaster();
		entity.setSerPropertyId(dto.getSerPropertyId());
		entity.setTxtPropertyName(dto.getTxtPropertyName());
		entity.setTxtInputType(dto.getTxtInputType());
		entity.setTxtRemarks(dto.getTxtRemarks());
		entity.setBlnIsRequired(dto.getBlnIsRequired());
		entity.setBlnIsActive(dto.getBlnIsActive());
		entity.setBlnIsDeleted(false);

		if (dto.getSerDecorCategoryId() != null) {
			DecorCategoryMaster category = new DecorCategoryMaster();
			category.setSerDecorCategoryId(dto.getSerDecorCategoryId());
			entity.setDecorCategory(category);
		}

		return entity;
	}

	public static DtoDecorCategoryPropertyMaster toDto(DecorCategoryPropertyMaster entity) {
		DtoDecorCategoryPropertyMaster dto = new DtoDecorCategoryPropertyMaster();
		dto.setSerPropertyId(entity.getSerPropertyId());
		dto.setTxtPropertyName(entity.getTxtPropertyName());
		dto.setTxtInputType(entity.getTxtInputType());
		dto.setTxtRemarks(entity.getTxtRemarks());
		dto.setBlnIsRequired(entity.getBlnIsRequired());
		dto.setBlnIsActive(entity.getBlnIsActive());

		if (entity.getDecorCategory() != null) {
			dto.setSerDecorCategoryId(entity.getDecorCategory().getSerDecorCategoryId());
			dto.setTxtDecorCategoryCode(entity.getDecorCategory().getTxtDecorCategoryCode());
			dto.setTxtDecorCategoryName(entity.getDecorCategory().getTxtDecorCategoryName());
		}

		return dto;
	}
}
