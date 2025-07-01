package com.zbs.de.mapper;

import com.zbs.de.model.DecorCategoryColorMapping;
import com.zbs.de.model.dto.DtoDecorCategoryColorMapping;
import com.zbs.de.util.UtilRandomKey;

public class MapperDecoreCategoryColorMapping {

	public static DtoDecorCategoryColorMapping toDto(DecorCategoryColorMapping entity) {
		if (UtilRandomKey.isNull(entity)) {
			return null;
		}

		DtoDecorCategoryColorMapping dto = new DtoDecorCategoryColorMapping();
		dto.setSerMappingId(entity.getSerMappingId());
		if (UtilRandomKey.isNotNull(entity.getDecorCategory())) {
			dto.setSerDecorCategoryId(entity.getDecorCategory().getSerDecorCategoryId());
			dto.setTxtDecorCategoryCode(entity.getDecorCategory().getTxtDecorCategoryCode());
			dto.setTxtDecorCategoryName(entity.getDecorCategory().getTxtDecorCategoryName());
		}

		if (UtilRandomKey.isNotNull(entity.getDecorColorMaster())) {
			dto.setSerDecorColorId(entity.getDecorColorMaster().getSerDecorColorId());
			dto.setTxtColorCode(entity.getDecorColorMaster().getTxtColorCode());
			dto.setTxtColorName(entity.getDecorColorMaster().getTxtColorName());
		}

		return dto;
	}

}
