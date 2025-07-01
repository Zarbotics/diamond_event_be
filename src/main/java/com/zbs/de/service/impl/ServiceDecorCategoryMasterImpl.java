package com.zbs.de.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.zbs.de.mapper.MapperDecorCategoryMaster;
import com.zbs.de.model.DecorCategoryMaster;
import com.zbs.de.model.dto.DtoDecorCategoryMaster;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryDecorCategoryMaster;
import com.zbs.de.service.ServiceDecorCategoryMaster;

public class ServiceDecorCategoryMasterImpl implements ServiceDecorCategoryMaster {

	@Autowired
	private RepositoryDecorCategoryMaster repositoryDecorCategoryMaster;

	@Override
	public DtoResult saveOrUpdate(DtoDecorCategoryMaster dto) {
		DecorCategoryMaster entity = MapperDecorCategoryMaster.toEntity(dto);
		repositoryDecorCategoryMaster.save(entity);
		return new DtoResult("Saved Successfully", null, MapperDecorCategoryMaster.toDto(entity), null);
	}

	@Override
	public DtoResult getAll() {
		List<DtoDecorCategoryMaster> list = repositoryDecorCategoryMaster.findAll().stream()
				.map(MapperDecorCategoryMaster::toDto).collect(Collectors.toList());
		return new DtoResult("Fetched Successfully", null, null, new ArrayList<>(list));
	}

	@Override
	public DtoResult getById(Integer id) {
		Optional<DecorCategoryMaster> optional = repositoryDecorCategoryMaster.findById(id);
		if (optional.isPresent()) {
			return new DtoResult("Found", null, MapperDecorCategoryMaster.toDto(optional.get()), null);
		}
		return new DtoResult("Not Found", null, null, null);
	}

	@Override
	public DtoResult deleteById(Integer id) {
		repositoryDecorCategoryMaster.deleteById(id);
		return new DtoResult("Deleted", null, null, null);
	}

}
