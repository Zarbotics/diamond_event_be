package com.zbs.de.service.impl;

import com.zbs.de.mapper.MapperCustomerMaster;
import com.zbs.de.model.CustomerMaster;
import com.zbs.de.model.dto.DtoCustomerMaster;
import com.zbs.de.repository.RepositoryCustomerMaster;
import com.zbs.de.service.ServiceCustomerMaster;
import com.zbs.de.util.ResponseMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("serviceCustomerMaster")
public class ServiceCustomerMasterImpl implements ServiceCustomerMaster {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCustomerMasterImpl.class);
	@Autowired
	RepositoryCustomerMaster repositoryCustomerMaster;

	public List<DtoCustomerMaster> getAllData() {
		List<CustomerMaster> list = repositoryCustomerMaster.findByBlnIsDeleted(false);
		List<DtoCustomerMaster> dtolist = new ArrayList<>();
		if (list != null) {
			for (CustomerMaster customer : list) {
				DtoCustomerMaster dtoCustomerMaintenance = MapperCustomerMaster.toDto(customer);
				dtolist.add(dtoCustomerMaintenance);
			}
		}
		return dtolist;
	}

	@Override
	public ResponseMessage saveAndUpdate(DtoCustomerMaster dtoCustomerMaster) {
		ResponseMessage res = new ResponseMessage();
		try {
			LOGGER.info("Save or update CustomerMaster");

			if (dtoCustomerMaster.getSerCustId() != null) {

			}

			CustomerMaster customerMaster = MapperCustomerMaster.toEntity(dtoCustomerMaster);
			customerMaster.setTxtCustCode(this.generateCustomerCode());
			customerMaster.setBlnIsActive(Boolean.valueOf(true));
			customerMaster.setBlnIsDeleted(Boolean.valueOf(false));
			customerMaster.setBlnIsApproved(Boolean.valueOf(true));
			customerMaster = repositoryCustomerMaster.saveAndFlush(customerMaster);
			res.setMessage("Saved Successfully");
			res.setResult(customerMaster);
			return res;

		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			res.setMessage(e.getMessage());
		}

		return res;
	}

	@Override
	public ResponseMessage getById(Integer serCustId) {
		ResponseMessage res = new ResponseMessage();
		try {
			Optional<CustomerMaster> entity = repositoryCustomerMaster.findById(serCustId);

			if (entity.isPresent()) {
				res.setMessage("Record Fetched Successfully");
				res.setResult(entity.get());
			}
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			res.setMessage(e.getMessage());
		}

		return res;

	}
	
	public String generateCustomerCode() {
	    String maxCode = repositoryCustomerMaster.findMaxCustomerCode(); // e.g., "CUST-009"

	    int nextNumber = 1;

	    if (maxCode != null && maxCode.startsWith("CUST-")) {
	        try {
	            nextNumber = Integer.parseInt(maxCode.substring(5)) + 1;
	        } catch (NumberFormatException e) {
	            nextNumber = 1;
	        }
	    }

	    return String.format("CUST-%03d", nextNumber); // e.g., CUST-010
	}

}
