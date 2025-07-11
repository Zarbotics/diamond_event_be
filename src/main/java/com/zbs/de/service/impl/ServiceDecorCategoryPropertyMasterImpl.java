package com.zbs.de.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zbs.de.mapper.MapperDecorCategoryPropertyMaster;
import com.zbs.de.model.DecorCategoryPropertyMaster;
import com.zbs.de.model.dto.DtoDecorCategoryPropertyMaster;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryDecorCategoryPropertyMaster;
import com.zbs.de.service.ServiceDecorCategoryPropertyMaster;

@Service("serviceDecorCategoryPropertyMaster")
public class ServiceDecorCategoryPropertyMasterImpl implements ServiceDecorCategoryPropertyMaster {
	@Autowired
	RepositoryDecorCategoryPropertyMaster repositoryDecorCategoryPropertyMaster;

	@Override
	public DtoResult saveOrUpdate(DtoDecorCategoryPropertyMaster dto) {
		DecorCategoryPropertyMaster entity = MapperDecorCategoryPropertyMaster.toEntity(dto);
		repositoryDecorCategoryPropertyMaster.save(entity);
		return new DtoResult("Saved Successfully", null, MapperDecorCategoryPropertyMaster.toDto(entity), null);
	}

	@Override
	public DtoResult getAll() {
		List<DtoDecorCategoryPropertyMaster> list = repositoryDecorCategoryPropertyMaster.findAllByBlnIsDeletedFalse()
				.stream().map(MapperDecorCategoryPropertyMaster::toDto).collect(Collectors.toList());
		return new DtoResult("Fetched Successfully", null, null, new ArrayList<>(list));
	}

	@Override
	public DtoResult getById(Integer id) {
		Optional<DecorCategoryPropertyMaster> optional = repositoryDecorCategoryPropertyMaster
				.findBySerPropertyIdAndBlnIsDeletedFalse(id);
		return optional.map(value -> new DtoResult("Found", null, MapperDecorCategoryPropertyMaster.toDto(value), null))
				.orElseGet(() -> new DtoResult("Not Found", null, null, null));
	}

	@Override
	public DtoResult deleteById(Integer id) {
		Optional<DecorCategoryPropertyMaster> optional = repositoryDecorCategoryPropertyMaster
				.findBySerPropertyIdAndBlnIsDeletedFalse(id);
		if (optional.isPresent()) {
			DecorCategoryPropertyMaster entity = optional.get();
			entity.setBlnIsDeleted(true);
			repositoryDecorCategoryPropertyMaster.save(entity);
			return new DtoResult("Deleted (soft) successfully", null, null, null);
		}
		return new DtoResult("No record found to delete", null, null, null);
	}
}
