package com.zbs.de.service.impl;

import org.springframework.stereotype.Service;

import com.zbs.de.service.ServiceDecorItemMaster;
import com.zbs.de.mapper.MapperDecorItemMaster;
import com.zbs.de.model.DecorItemMaster;
import com.zbs.de.model.dto.DtoDecorItemMaster;
import com.zbs.de.repository.RepositoryDecorItemMaster;
import com.zbs.de.util.ResponseMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Service("serviceDecorItemMaster")
public class ServiceDecorItemMasterImpl implements ServiceDecorItemMaster {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDecorItemMasterImpl.class);

	@Autowired
	RepositoryDecorItemMaster repositoryDecorItemMaster;

	@Override
	public List<DtoDecorItemMaster> getAllData() {
		List<DecorItemMaster> list = repositoryDecorItemMaster.findByBlnIsDeleted(false);
		List<DtoDecorItemMaster> dtos = new ArrayList<>();
		for (DecorItemMaster item : list) {
			dtos.add(MapperDecorItemMaster.toDto(item));
		}
		return dtos;
	}

	@Override
	public ResponseMessage saveAndUpdate(DtoDecorItemMaster dto) {
		ResponseMessage res = new ResponseMessage();
		try {
			DecorItemMaster entity = MapperDecorItemMaster.toEntity(dto);
			entity.setBlnIsActive(true);
			entity.setBlnIsDeleted(false);
			entity.setBlnIsApproved(true);
			entity = repositoryDecorItemMaster.saveAndFlush(entity);
			res.setMessage("Saved successfully");
			res.setResult(entity);
		} catch (Exception e) {
			LOGGER.error("Error saving DecorItemMaster", e);
			res.setMessage("Unexpected error occurred while saving");
		}
		return res;
	}

	@Override
	public ResponseMessage getById(Integer id) {
		ResponseMessage res = new ResponseMessage();
		try {
			Optional<DecorItemMaster> optional = repositoryDecorItemMaster.findById(id);
			if (optional.isPresent()) {
				res.setMessage("Record fetched successfully");
				res.setResult(optional.get());
			} else {
				res.setMessage("DecorItemMaster not found");
			}
		} catch (Exception e) {
			LOGGER.error("Error fetching DecorItemMaster by ID", e);
			res.setMessage("Unexpected error occurred");
		}
		return res;
	}
}