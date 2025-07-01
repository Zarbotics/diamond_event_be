package com.zbs.de.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zbs.de.controller.ControllerEventType;
import com.zbs.de.mapper.MapperDecorCategoryMaster;
import com.zbs.de.model.DecorCategoryMaster;
import com.zbs.de.model.dto.DtoDecorCategoryMaster;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryDecorCategoryMaster;
import com.zbs.de.service.ServiceDecorCategoryMaster;

@Service("serviceDecorCategoryMasterImpl")
public class ServiceDecorCategoryMasterImpl implements ServiceDecorCategoryMaster {

	@Autowired
	private RepositoryDecorCategoryMaster repositoryDecorCategoryMaster;

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerEventType.class);

	
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
	
	@Override
	public DecorCategoryMaster getByPK(Integer id) {
		try {
			Optional<DecorCategoryMaster> optional = repositoryDecorCategoryMaster.findById(id);
			if (optional.isPresent()) {
				return optional.get();
			}else {
				return null;
			}
			
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(),e);
			return null;
		}
		

	}

}
