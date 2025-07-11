package com.zbs.de.service.impl;

import com.zbs.de.mapper.MapperCustomerMaster;
import com.zbs.de.model.CustomerMaster;
import com.zbs.de.model.dto.DtoCustomerMaster;
import com.zbs.de.model.dto.DtoDashboardCustomer;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryCustomerMaster;
import com.zbs.de.service.ServiceCustomerMaster;
import com.zbs.de.util.ResponseMessage;
import com.zbs.de.util.UtilDateAndTime;

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

			String email = dtoCustomerMaster.getTxtEmail();
			if (email == null || email.trim().isEmpty()) {
				res.setMessage("Email is required.");
				return res;
			}

			// Trim and lowercase the email to ensure consistency
			email = email.trim().toLowerCase();

			// Check if customer exists (ignore case, not deleted)
			List<CustomerMaster> existingList = repositoryCustomerMaster
					.findByTxtEmailIgnoreCaseAndBlnIsDeletedFalse(email);

			CustomerMaster customerMaster;

			if (!existingList.isEmpty()) {
				// Update the first found customer
				customerMaster = existingList.get(0);
				customerMaster.setTxtAddress1(dtoCustomerMaster.getTxtAddress1());
				customerMaster.setTxtAddress2(dtoCustomerMaster.getTxtAddress2());
				customerMaster.setAddress3(dtoCustomerMaster.getAddress3());
				customerMaster.setTxtCustName(dtoCustomerMaster.getTxtCustName());
				customerMaster.setTxt_phone_number_1(dtoCustomerMaster.getTxt_phone_number_1());
				customerMaster.setTxt_phone_number_2(dtoCustomerMaster.getTxt_phone_number_2());
				customerMaster.setTxtEmail(dtoCustomerMaster.getTxtEmail());
				customerMaster.setComments(dtoCustomerMaster.getComments());
				customerMaster.setNum_longitude(dtoCustomerMaster.getNum_longitude());
				customerMaster.setNumLatitude(dtoCustomerMaster.getNumLatitude());
				customerMaster.setTxtGMapUrl(dtoCustomerMaster.getTxtGMapUrl());
				customerMaster.setTxtStreetName(dtoCustomerMaster.getTxtStreetName());
				customerMaster.setTxtBuildingName(dtoCustomerMaster.getTxtBuildingName());
				customerMaster.setTxtBuildingNumber(dtoCustomerMaster.getTxtBuildingNumber());
				customerMaster.setTxtPostalCode(dtoCustomerMaster.getTxtPostalCode());
				customerMaster.setTxtDistrict(dtoCustomerMaster.getTxtDistrict());
				customerMaster.setTxtCountryCode(dtoCustomerMaster.getTxtCountryCode());
				customerMaster.setTxtFirstName(dtoCustomerMaster.getTxtFirstName());
				customerMaster.setTxtLastName(dtoCustomerMaster.getTxtLastName());
				customerMaster.setUpdatedDate(UtilDateAndTime.getCurrentDate());
				LOGGER.info("Updating existing customer with email: " + email);
			} else {
				// Create new
				customerMaster = MapperCustomerMaster.toEntity(dtoCustomerMaster);
				customerMaster.setTxtCustCode(this.generateCustomerCode());
				customerMaster.setCreatedDate(UtilDateAndTime.getCurrentDate());
				customerMaster.setBlnIsActive(true);
				customerMaster.setBlnIsDeleted(false);
				customerMaster.setBlnIsApproved(true);
				LOGGER.info("Creating new customer with email: " + email);
			}

			customerMaster = repositoryCustomerMaster.saveAndFlush(customerMaster);
			res.setMessage("Saved Successfully");
			res.setResult(customerMaster);
			return res;

		} catch (Exception e) {
			LOGGER.error("Error in saveAndUpdate: " + e.getMessage(), e);
			res.setMessage("Error: " + e.getMessage());
			return res;
		}
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

	@Override
	public DtoResult getByEmail(String txtEmail) {
		DtoResult dtoResult = new DtoResult();

		if (txtEmail == null || txtEmail.trim().isEmpty()) {
			dtoResult.setTxtMessage("Email cannot be null or empty");
			return dtoResult;
		}

		List<CustomerMaster> optionalCustomer = repositoryCustomerMaster
				.findByTxtEmailIgnoreCaseAndBlnIsDeletedFalse(txtEmail.trim());

		if (!optionalCustomer.isEmpty()) {
			CustomerMaster customer = optionalCustomer.get(0);
			dtoResult.setTxtMessage("Success");
			dtoResult.setResult(customer); // You can map to DTO if needed
		} else {
			dtoResult.setTxtMessage("Customer Not Found With Provided Email");
		}

		return dtoResult;
	}

	@Override
	public CustomerMaster getByPK(Integer serCustId) {
		try {
			Optional<CustomerMaster> entity = repositoryCustomerMaster.findById(serCustId);

			if (entity.isPresent()) {
				return entity.get();
			} else {
				return null;
			}
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			return null;
		}

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

	@Override
	public DtoDashboardCustomer getDashboardStats() {
		long total = repositoryCustomerMaster.countTotalCustomers();
		long thisMonth = repositoryCustomerMaster.countCustomersThisMonth();
		long lastMonth = repositoryCustomerMaster.countCustomersLastMonth();

		double increaseRate = 0.0;
		if (lastMonth > 0) {
			increaseRate = ((double) (thisMonth - lastMonth) / lastMonth) * 100;
		} else if (thisMonth > 0) {
			increaseRate = 100.0;
		}

		DtoDashboardCustomer dto = new DtoDashboardCustomer();
		dto.setTotalCustomers(total);
		dto.setThisMonthCustomers(thisMonth);
		dto.setMonthlyIncreaseRate(Math.round(increaseRate * 100.0) / 100.0); // rounded to 2 decimal places
		return dto;
	}

}
