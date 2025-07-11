package com.zbs.de.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zbs.de.mapper.MapperDecorCategoryPropertyValue;
import com.zbs.de.model.DecorCategoryPropertyValue;
import com.zbs.de.model.dto.DtoDecorCategoryPropertyValue;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryDecorCategoryPropertyValue;
import com.zbs.de.service.ServiceDecorCategoryPropertyValue;

@Service("serviceDecorCategoryPropertyValue")
public class ServiceDecorCategoryPropertyValueImpl implements ServiceDecorCategoryPropertyValue {

	@Autowired
	RepositoryDecorCategoryPropertyValue repositoryDecorCategoryPropertyValue;

	@Override
	public DtoResult saveOrUpdate(DtoDecorCategoryPropertyValue dto) {
		DecorCategoryPropertyValue entity = MapperDecorCategoryPropertyValue.toEntity(dto);
		repositoryDecorCategoryPropertyValue.save(entity);
		return new DtoResult("Saved Successfully", null, MapperDecorCategoryPropertyValue.toDto(entity), null);
	}

	@Override
	public DtoResult getAll() {
		List<DtoDecorCategoryPropertyValue> list = repositoryDecorCategoryPropertyValue.findAll().stream()
				.map(MapperDecorCategoryPropertyValue::toDto).collect(Collectors.toList());
		return new DtoResult("Fetched Successfully", null, null, new ArrayList<>(list));
	}

	@Override
	public DtoResult getByPropertyId(Integer propertyId) {
		List<DtoDecorCategoryPropertyValue> list = repositoryDecorCategoryPropertyValue
				.findByPropertyIdAndNotDeleted(propertyId).stream().map(MapperDecorCategoryPropertyValue::toDto)
				.collect(Collectors.toList());
		return new DtoResult("Fetched by PropertyId", null, null, new ArrayList<>(list));
	}

	@Override
	public DtoResult getById(Integer id) {
		Optional<DecorCategoryPropertyValue> optional = repositoryDecorCategoryPropertyValue.findById(id);
		return optional.map(value -> new DtoResult("Found", null, MapperDecorCategoryPropertyValue.toDto(value), null))
				.orElseGet(() -> new DtoResult("Not Found", null, null, null));
	}

	@Override
	public DtoResult deleteById(Integer id) {
		Optional<DecorCategoryPropertyValue> optional = repositoryDecorCategoryPropertyValue
				.findByIdAndBlnIsDeletedFalse(id);
		if (optional.isPresent()) {
			DecorCategoryPropertyValue entity = optional.get();
			entity.setBlnIsDeleted(true);
			repositoryDecorCategoryPropertyValue.save(entity);
			return new DtoResult("Deleted (soft) successfully", null, null, null);
		}
		return new DtoResult("No record found to delete", null, null, null);
	}
}
