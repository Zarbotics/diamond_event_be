package com.zbs.de.service.impl;

import org.springframework.stereotype.Service;

import com.zbs.de.service.ServiceStateMaster;
import com.zbs.de.mapper.MapperStateMaster;
import com.zbs.de.model.CountryMaster;
import com.zbs.de.model.StateMaster;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoStateMaster;
import com.zbs.de.repository.RepositoryCountryMaster;
import com.zbs.de.repository.RepositoryStateMaster;
import com.zbs.de.util.ResponseMessage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Service("serviceStateMaster")
public class ServiceStateMasterImpl implements ServiceStateMaster {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceStateMasterImpl.class);

	@Autowired
	private RepositoryStateMaster repositoryState;

	@Autowired
	private RepositoryCountryMaster repositoryCountry;

	@Override
	public ResponseMessage getAllData() {

		ResponseMessage responseMessage = new ResponseMessage();
		try {
			List<StateMaster> list = repositoryState.findByBlnIsDeleted(false);
			List<DtoStateMaster> dtoStateMasters = list.stream().map(MapperStateMaster::toDto)
					.collect(Collectors.toList());
			responseMessage = new ResponseMessage();
			responseMessage.setResult(dtoStateMasters);
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			responseMessage.setMessage("Unable To Get Records From Database");

		}
		return responseMessage;
	}

	@Override
	public ResponseMessage saveAndUpdate(DtoStateMaster dto) {
		ResponseMessage res = new ResponseMessage();
		try {
			CountryMaster country = repositoryCountry.findById(dto.getSerCountryId()).orElse(null);
			if (country == null) {
				res.setMessage("Invalid country");
				return res;
			}
			StateMaster entity = MapperStateMaster.toEntity(dto);
			entity.setCountryMaster(country);
			entity.setBlnIsDeleted(false);
			entity.setBlnIsActive(true);
			entity.setBlnIsApproved(true);
			repositoryState.saveAndFlush(entity);
			res.setMessage("Saved Successfully");
			res.setResult(entity);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			res.setMessage("Error: " + e.getMessage());
		}
		return res;
	}

	@Override
	public ResponseMessage getById(Integer id) {
		ResponseMessage res = new ResponseMessage();
		try {
			Optional<StateMaster> entity = repositoryState.findById(id);
			entity.ifPresent(value -> {
				res.setResult(MapperStateMaster.toDto(value));
				res.setMessage("Record fetched successfully");
			});
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			res.setMessage("Error: " + e.getMessage());
		}
		return res;
	}
	
	@Override
	public DtoResult deleteById(Integer id) {
		DtoResult result = new DtoResult();
		Optional<StateMaster> optional = repositoryState.findById(id);
		if (optional.isPresent()) {
			StateMaster e = optional.get();
			e.setBlnIsDeleted(true);
			repositoryState.save(e);
			result.setTxtMessage("Deleted (soft) successfully");
		} else {
			result.setTxtMessage("No record found to delete");
		}
		return result;
	}
}
