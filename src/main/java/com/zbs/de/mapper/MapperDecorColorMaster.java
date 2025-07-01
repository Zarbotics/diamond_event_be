package com.zbs.de.mapper;

import com.zbs.de.model.DecorColorMaster;
import com.zbs.de.model.dto.DtoDecorColorMaster;

public class MapperDecorColorMaster {
	public static DecorColorMaster toEntity(DtoDecorColorMaster dto) {
		DecorColorMaster entity = new DecorColorMaster();
		entity.setSerDecorColorId(dto.getSerDecorColorId());
		entity.setTxtColorCode(dto.getTxtColorCode());
		entity.setTxtColorName(dto.getTxtColorName());
		entity.setTxtColorHex(dto.getTxtColorHex());
		entity.setBlnIsActive(dto.getBlnIsActive());
		return entity;
	}

	public static DtoDecorColorMaster toDto(DecorColorMaster entity) {
		DtoDecorColorMaster dto = new DtoDecorColorMaster();
		dto.setSerDecorColorId(entity.getSerDecorColorId());
		dto.setTxtColorCode(entity.getTxtColorCode());
		dto.setTxtColorName(entity.getTxtColorName());
		dto.setTxtColorHex(entity.getTxtColorHex());
		dto.setBlnIsActive(entity.getBlnIsActive());
		return dto;
	}
}
