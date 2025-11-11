package com.zbs.de.service.impl;

import org.springframework.stereotype.Service;

import com.zbs.de.service.ServiceVendorMaster;
import com.zbs.de.mapper.MapperVendorMaster;
import com.zbs.de.model.VendorMaster;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoVendorMaster;
import com.zbs.de.repository.RepositoryVendorMaster;
import com.zbs.de.util.ResponseMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Service("serviceVendorMaster")
public class ServiceVendorMasterImpl implements ServiceVendorMaster {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceVendorMasterImpl.class);

	@Autowired
	RepositoryVendorMaster repositoryVendorMaster;

	@Override
	public List<DtoVendorMaster> getAllData() {
		List<VendorMaster> list = repositoryVendorMaster.findByBlnIsDeleted(false);
		List<DtoVendorMaster> dtos = new ArrayList<>();
		for (VendorMaster vendor : list) {
			dtos.add(MapperVendorMaster.toDto(vendor));
		}
		return dtos;
	}
	
	@Override
	public List<DtoVendorMaster> getAllActiveData() {
		List<VendorMaster> list = repositoryVendorMaster.findAllActive();
		List<DtoVendorMaster> dtos = new ArrayList<>();
		for (VendorMaster vendor : list) {
			dtos.add(MapperVendorMaster.toDto(vendor));
		}
		return dtos;
	}

	@Override
	public ResponseMessage saveAndUpdate(DtoVendorMaster dto) {
		ResponseMessage res = new ResponseMessage();
		try {
			VendorMaster entity = MapperVendorMaster.toEntity(dto);
			if(dto.getBlnIsActive() == null) {
				entity.setBlnIsActive(true);
			}
			entity.setBlnIsDeleted(false);
			entity.setBlnIsApproved(true);
			entity = repositoryVendorMaster.saveAndFlush(entity);
			res.setMessage("Saved successfully");
			res.setResult(entity);
		} catch (Exception e) {
			LOGGER.error("Error saving vendor", e);
			res.setMessage("Unexpected error occurred while saving vendor.");
		}
		return res;
	}

	@Override
	public ResponseMessage getById(Integer id) {
		ResponseMessage res = new ResponseMessage();
		try {
			Optional<VendorMaster> optional = repositoryVendorMaster.findById(id);
			if (optional.isPresent()) {
				res.setMessage("Record fetched successfully");
				res.setResult(optional.get());
			} else {
				res.setMessage("Vendor not found");
			}
		} catch (Exception e) {
			LOGGER.error("Error fetching vendor by id", e);
			res.setMessage("Unexpected error occurred while fetching vendor.");
		}
		return res;
	}

	@Override
	public VendorMaster getByPK(Integer id) {
		try {
			Optional<VendorMaster> optional = repositoryVendorMaster.findById(id);
			if (optional.isPresent()) {
				return optional.get();
			} else {
				return null;
			}
		} catch (Exception e) {
			LOGGER.error("Error fetching vendor by id", e);
			return null;
		}
	}

	@Override
	public DtoResult deleteById(Integer id) {
		DtoResult result = new DtoResult();
		Optional<VendorMaster> optional = repositoryVendorMaster.findById(id);
		if (optional.isPresent()) {
			VendorMaster e = optional.get();
			e.setBlnIsDeleted(true);
			repositoryVendorMaster.save(e);
			result.setTxtMessage("Deleted (soft) successfully");
		} else {
			result.setTxtMessage("No record found to delete");
		}
		return result;
	}
	
	@Override
	public String generateNextVendorMasterCode() {
		String maxCode = repositoryVendorMaster.findMaxVendorMasterCode();

		int nextNumber = 1;

		if (maxCode != null && maxCode.startsWith("VEN-")) {
			try {
				String numberPart = maxCode.substring(4);
				nextNumber = Integer.parseInt(numberPart) + 1;
			} catch (NumberFormatException e) {
				nextNumber = 1;
			}
		}

		return String.format("VEN-%03d", nextNumber);
	}
}
