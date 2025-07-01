package com.zbs.de.service.impl;

import org.springframework.stereotype.Service;

import com.zbs.de.service.ServiceDecorItemMaster;
import com.zbs.de.mapper.MapperDecorItemMaster;
import com.zbs.de.model.DecorItemMaster;
import com.zbs.de.model.dto.DtoDecorItemMaster;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryDecorItemMaster;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Service("serviceDecorItemMaster")
public class ServiceDecorItemMasterImpl implements ServiceDecorItemMaster {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDecorItemMasterImpl.class);

	@Autowired
	RepositoryDecorItemMaster repositoryDecorItemMaster;

	@Override
	public DtoResult saveOrUpdate(DtoDecorItemMaster dto) {
		DecorItemMaster entity = MapperDecorItemMaster.toEntity(dto);
		repositoryDecorItemMaster.save(entity);
		return new DtoResult("Saved Successfully", null, MapperDecorItemMaster.toDto(entity), null);
	}

	@Override
	public DtoResult getAll() {
		List<DtoDecorItemMaster> list = repositoryDecorItemMaster.findAll().stream().map(MapperDecorItemMaster::toDto)
				.collect(Collectors.toList());
		return new DtoResult("Fetched Successfully", null, null, new ArrayList<>(list));
	}

	@Override
	public DtoResult getById(Integer id) {
		Optional<DecorItemMaster> optional = repositoryDecorItemMaster.findById(id);
		if (optional.isPresent()) {
			return new DtoResult("Found", null, MapperDecorItemMaster.toDto(optional.get()), null);
		}
		return new DtoResult("Not Found", null, null, null);
	}

	@Override
	public DtoResult deleteById(Integer id) {
		repositoryDecorItemMaster.deleteById(id);
		return new DtoResult("Deleted", null, null, null);
	}
}