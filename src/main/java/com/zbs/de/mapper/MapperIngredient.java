package com.zbs.de.mapper;

import java.util.HashMap;

import com.zbs.de.model.Ingredient;
import com.zbs.de.model.dto.DtoIngredient;

public class MapperIngredient {
	public static DtoIngredient toDto(Ingredient entity) {
		if (entity == null)
			return null;

		DtoIngredient dto = new DtoIngredient();
		dto.setSerIngredientId(entity.getSerIngredientId());
		dto.setTxtName(entity.getTxtName());
		dto.setMetadata(entity.getMetadata() != null ? new HashMap<>(entity.getMetadata()) : null);

		return dto;
	}

	public static Ingredient toEntity(DtoIngredient dto) {
		if (dto == null)
			return null;

		Ingredient entity = new Ingredient();
		entity.setSerIngredientId(dto.getSerIngredientId());
		entity.setTxtName(dto.getTxtName());
		entity.setMetadata(dto.getMetadata() != null ? new HashMap<>(dto.getMetadata()) : null);

		return entity;
	}
}
