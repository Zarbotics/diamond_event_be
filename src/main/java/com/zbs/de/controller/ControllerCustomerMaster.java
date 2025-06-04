package com.zbs.de.controller;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.zbs.de.util.ResponseMessage;
import com.zbs.de.model.dto.DtoCustomerMaster;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.service.ServiceCustomerMaster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ControllerCustomerMaintenance.
 */
@RestController
@RequestMapping("/customerMaster")
public class ControllerCustomerMaster {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerCustomerMaster.class);

	/** The service main acct. */
	@Autowired
	ServiceCustomerMaster serviceCustomerMaster;

	/**
	 * Gets the all data.
	 *
	 * @param request the request
	 * @return the all data
	 * @throws Exception the exception
	 */
	@RequestMapping(value = "/getAllData", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=application/json")
	public ResponseMessage getAllData(HttpServletRequest request) throws Exception {
		LOGGER.info("Search ItemClassAccountSetup Method");
		ResponseMessage responseMessage = null;
		List<DtoCustomerMaster> dtoSearch = new ArrayList<>();
		dtoSearch = this.serviceCustomerMaster.getAllData();
		if (dtoSearch != null) {
			responseMessage = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "GET ALL CUSTOMERS", dtoSearch);
		} else {
			responseMessage = new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
					"UNABLE TO GET ALL CUSTOMERS", dtoSearch);
		}

		LOGGER.debug("Search ItemClassAccountSetup Method:" + dtoSearch);
		return responseMessage;
	}

	@RequestMapping(value = "/saveOrUpdate", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=application/json")
	public ResponseMessage saveOrUpdate(@RequestBody DtoCustomerMaster dtoCustomerMaster, HttpServletRequest request)
			throws Exception {
		LOGGER.info("Save Or Update CustomerMaster Mehod" + dtoCustomerMaster);
		ResponseMessage responseMessage = null;
		responseMessage = this.serviceCustomerMaster.saveAndUpdate(dtoCustomerMaster);
		if (responseMessage != null) {
			responseMessage = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK,
					"Successfully Saved The Customer Masters", responseMessage.getResult());
		} else {
			responseMessage = new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
					"Unable To Save And Update", dtoCustomerMaster);
		}

		LOGGER.debug("Save Or Update  CustomerMaster Method:" + dtoCustomerMaster);
		return responseMessage;
	}

	@RequestMapping(value = "/getById", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=application/json")
	public ResponseMessage getById(@RequestBody DtoSearch dtoSearch) {
		LOGGER.info("Fetching customer by ID" + dtoSearch);
		ResponseMessage responseMessage = new ResponseMessage();
		try {
			ResponseMessage res = serviceCustomerMaster.getById(dtoSearch.getId());
			responseMessage = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched successfully",
					res.getResult());
		} catch (Exception e) {
			LOGGER.error("Error fetching customer", e);
			responseMessage = new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
					"Customer not found", null);
		}
		LOGGER.info("Fetching customer by ID" + dtoSearch);
		return responseMessage;
	}

}
