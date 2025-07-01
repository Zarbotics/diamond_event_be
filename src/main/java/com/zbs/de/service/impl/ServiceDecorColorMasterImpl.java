package com.zbs.de.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zbs.de.mapper.MapperDecorColorMaster;
import com.zbs.de.model.DecorColorMaster;
import com.zbs.de.model.dto.DtoDecorColorMaster;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryDecorColorMaster;
import com.zbs.de.service.ServiceDecorColorMaster;

@Service("serviceDecorColorMasterImpl")
public class ServiceDecorColorMasterImpl implements ServiceDecorColorMaster {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCustomerMasterImpl.class);

	@Autowired
	private RepositoryDecorColorMaster repositoryDecorColorMaster;

	@Override
	public DtoResult saveOrUpdate(DtoDecorColorMaster dto) {
		DecorColorMaster entity = MapperDecorColorMaster.toEntity(dto);
		repositoryDecorColorMaster.save(entity);
		return new DtoResult("Saved Successfully", null, MapperDecorColorMaster.toDto(entity), null);
	}

	@Override
	public DtoResult getAll() {
		List<DtoDecorColorMaster> list = repositoryDecorColorMaster.findAll().stream()
				.map(MapperDecorColorMaster::toDto).collect(Collectors.toList());
		return new DtoResult("Fetched Successfully", null, null, new ArrayList<>(list));
	}

	@Override
	public DtoResult getById(Integer id) {
		Optional<DecorColorMaster> optional = repositoryDecorColorMaster.findById(id);
		if (optional.isPresent()) {
			return new DtoResult("Found", null, MapperDecorColorMaster.toDto(optional.get()), null);
		}
		return new DtoResult("Not Found", null, null, null);
	}

	@Override
	public DecorColorMaster getByPK(Integer id) {

		try {
			Optional<DecorColorMaster> optional = repositoryDecorColorMaster.findById(id);
			if (optional.isPresent()) {
				return optional.get();
			}
			return null;
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			return null;
		}

	}

	@Override
	public DtoResult deleteById(Integer id) {
		repositoryDecorColorMaster.deleteById(id);
		return new DtoResult("Deleted", null, null, null);
	}

}
