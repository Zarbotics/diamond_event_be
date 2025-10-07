package com.zbs.de.service.impl;

import com.zbs.de.mapper.MapperCityMaster;
import com.zbs.de.model.CityMaster;
import com.zbs.de.model.dto.DtoCityMaster;
import com.zbs.de.model.dto.DtoResult;
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
	public DtoResult getAllActive() {
		try {
			List<CityMaster> list = repositoryCityMaster.findByBlnIsActiveTrueAndBlnIsDeletedFalse();
			if (list != null && !list.isEmpty()) {
				List<DtoCityMaster> dtoList = new ArrayList<>();
				for (CityMaster city : list) {
					dtoList.add(MapperCityMaster.toDto(city));
				}
				return new DtoResult("Fetched Successfully", null, dtoList, null);
			} else {
				return new DtoResult("No Record Found.", null, null, null);
			}

		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			return new DtoResult("Internal Server Error", null, null, null);
		}

	}

	
	@Override
	public ResponseMessage saveAndUpdate(DtoCityMaster dto) {
		ResponseMessage res = new ResponseMessage();
		try {
//			StateMaster stateMaster = repositoryStateMaster.findById(dto.getSerStateId()).orElse(null);
//			if (stateMaster == null) {
//				res.setMessage("Invalid State ID");
//				return res;
//			}
			CityMaster entity = MapperCityMaster.toEntity(dto);
//			entity.setStateMaster(stateMaster);
			entity.setBlnIsActive(dto.getBlnIsActive());
			entity.setBlnIsDeleted(false);
			entity.setBlnIsApproved(true);
			entity.setCreatedBy(ServiceCurrentUser.getCurrentUserId());
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

	@Override
	public CityMaster getByPK(Integer serCityId) {
		try {
			Optional<CityMaster> entity = repositoryCityMaster.findById(serCityId);
			if (entity.isPresent()) {
				return entity.get();
			} else {
				return null;
			}
		} catch (Exception e) {
			LOGGER.error("Error fetching city by ID", e);
			return null;
		}
	}

	@Override
	public DtoResult deleteById(Integer id) {
		DtoResult result = new DtoResult();
		Optional<CityMaster> optional = repositoryCityMaster.findById(id);
		if (optional.isPresent()) {
			CityMaster e = optional.get();
			e.setBlnIsDeleted(true);
			repositoryCityMaster.save(e);
			result.setTxtMessage("Success");
		} else {
			result.setTxtMessage("No record found to delete");
		}
		return result;
	}
}
