package com.zbs.de.service.impl;

import com.zbs.de.mapper.MapperCityMaster;
import com.zbs.de.model.CityMaster;
import com.zbs.de.model.StateMaster;
import com.zbs.de.model.dto.DtoCityMaster;
import com.zbs.de.repository.RepositoryCityMaster;
import com.zbs.de.repository.RepositoryStateMaster;
import com.zbs.de.service.ServiceCityMaster;
import com.zbs.de.util.ResponseMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("serviceCityMaster")
public class ServiceCityMasterImpl implements ServiceCityMaster {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCityMasterImpl.class);

	@Autowired
	RepositoryCityMaster repositoryCityMaster;

	@Autowired
	RepositoryStateMaster repositoryStateMaster;

	@Override
	public ResponseMessage getAllData() {
		ResponseMessage responseMessage = new ResponseMessage();
		try {
			List<CityMaster> list = repositoryCityMaster.findByBlnIsDeleted(false);
			List<DtoCityMaster> dtoList = new ArrayList<>();
			for (CityMaster city : list) {
				dtoList.add(MapperCityMaster.toDto(city));
			}
			responseMessage.setResult(dtoList);
			responseMessage.setMessage("Fetched Records Successfully");
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			responseMessage.setMessage(e.getMessage());
		}

		return responseMessage;

	}

	@Override
	public ResponseMessage saveAndUpdate(DtoCityMaster dto) {
		ResponseMessage res = new ResponseMessage();
		try {
			StateMaster stateMaster = repositoryStateMaster.findById(dto.getSerStateId()).orElse(null);
			if (stateMaster == null) {
				res.setMessage("Invalid State ID");
				return res;
			}
			CityMaster entity = MapperCityMaster.toEntity(dto);
			entity.setStateMaster(stateMaster);
			entity.setBlnIsActive(true);
			entity.setBlnIsDeleted(false);
			entity.setBlnIsApproved(true);
			CityMaster saved = repositoryCityMaster.saveAndFlush(entity);
			res.setMessage("Saved Successfully");
			res.setResult(saved);
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			res.setMessage("Error: " + e.getMessage());
		}
		return res;
	}

	@Override
	public ResponseMessage getById(Integer serCityId) {
		ResponseMessage res = new ResponseMessage();
		try {
			Optional<CityMaster> entity = repositoryCityMaster.findById(serCityId);
			if (entity.isPresent()) {
				res.setMessage("Record Fetched Successfully");
				res.setResult(MapperCityMaster.toDto(entity.get()));
			} else {
				res.setMessage("Record Not Found");
			}
		} catch (Exception e) {
			LOGGER.error("Error fetching city by ID", e);
			res.setMessage("Error: " + e.getMessage());
		}
		return res;
	}
}
