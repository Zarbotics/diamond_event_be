package com.zbs.de.service.impl;

import com.zbs.de.mapper.MapperIngredient;
import com.zbs.de.model.Ingredient;
import com.zbs.de.model.dto.DtoIngredient;
import com.zbs.de.repository.RepositoryIngredient;
import com.zbs.de.service.ServiceIngredient;
import com.zbs.de.util.exception.NotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service("serviceIngredientImpl")
public class ServiceIngredientImpl implements ServiceIngredient {

	@Autowired
	private RepositoryIngredient repo;

	@Override
	public DtoIngredient create(DtoIngredient dto) {
		Ingredient entity = MapperIngredient.toEntity(dto);
		repo.save(entity);
		return MapperIngredient.toDto(entity);
	}

	@Override
	public DtoIngredient update(Long id, DtoIngredient dto) {
		Ingredient exist = repo.findById(id).orElseThrow(() -> new NotFoundException("Ingredient not found"));
		exist.setTxtName(dto.getTxtName());
		exist.setMetadata(dto.getMetadata());
		repo.save(exist);
		return MapperIngredient.toDto(exist);
	}

	@Override
	public DtoIngredient getById(Long id) {
		return repo.findById(id).map(MapperIngredient::toDto).orElseThrow(() -> new NotFoundException("Ingredient not found"));
	}

	@Override
	public List<DtoIngredient> getAll() {
		return repo.findAll().stream().map(MapperIngredient::toDto).collect(Collectors.toList());
	}

	@Override
	public void delete(Long id) {
		Ingredient exist = repo.findById(id).orElseThrow(() -> new NotFoundException("Ingredient not found"));
		exist.setBlnIsDeleted(true);
		repo.save(exist);
	}
}
