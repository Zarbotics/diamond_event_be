package com.zbs.de.service.impl;

import com.zbs.de.mapper.MapperCountryMaster;
import com.zbs.de.model.CountryMaster;
import com.zbs.de.model.dto.DtoCountryMaster;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryCountryMaster;
import com.zbs.de.service.ServiceCountryMaster;
import com.zbs.de.util.ResponseMessage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("serviceCountryMaster")
public class ServiceCountryMasterImpl implements ServiceCountryMaster {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCountryMasterImpl.class);

	@Autowired
	private RepositoryCountryMaster repository;

	@Override
	public ResponseMessage getAllData() {
		ResponseMessage responseMessage = new ResponseMessage();
		try {
			List<CountryMaster> list = repository.findByBlnIsDeleted(false);
			List<DtoCountryMaster> dtoCountryMasters = list.stream().map(MapperCountryMaster::toDto)
					.collect(Collectors.toList());
			responseMessage = new ResponseMessage();
			responseMessage.setResult(dtoCountryMasters);
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			responseMessage.setMessage("Unable To Get Records From Database");

		}
		return responseMessage;

	}

	@Override
	public ResponseMessage saveAndUpdate(DtoCountryMaster dto) {
		ResponseMessage res = new ResponseMessage();
		try {
			CountryMaster entity = MapperCountryMaster.toEntity(dto);
			entity.setBlnIsDeleted(false);
			entity.setBlnIsActive(true);
			entity.setBlnIsApproved(true);
			entity = repository.saveAndFlush(entity);
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
			Optional<CountryMaster> entity = repository.findById(id);
			entity.ifPresent(value -> {
				res.setResult(MapperCountryMaster.toDto(value));
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
		Optional<CountryMaster> optional = repository.findById(id);
		if (optional.isPresent()) {
			CountryMaster e = optional.get();
			e.setBlnIsDeleted(true);
			repository.save(e);
			result.setTxtMessage("Deleted (soft) successfully");
		} else {
			result.setTxtMessage("No record found to delete");
		}
		return result;
	}
}
