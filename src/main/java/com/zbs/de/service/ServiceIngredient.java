package com.zbs.de.service;

import com.zbs.de.model.dto.DtoIngredient;

import java.util.List;

public interface ServiceIngredient {
	DtoIngredient create(DtoIngredient dto);

	DtoIngredient update(Long id, DtoIngredient dto);

	DtoIngredient getById(Long id);

	List<DtoIngredient> getAll();

	void delete(Long id);
}
