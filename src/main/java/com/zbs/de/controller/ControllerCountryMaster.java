package com.zbs.de.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.zbs.de.util.ResponseMessage;
import com.zbs.de.model.dto.DtoCountryMaster;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.service.ServiceCountryMaster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/countryMaster")
@CrossOrigin(origins = "")
public class ControllerCountryMaster {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerCountryMaster.class);

	@Autowired
	private ServiceCountryMaster serviceCountryMaster;

	@RequestMapping(value = "/getAllData", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=application/json")
	public ResponseMessage getAllData(HttpServletRequest request) throws Exception {
		LOGGER.info("Fetching all CountryMaster data");
		ResponseMessage responseMessage;
		responseMessage = serviceCountryMaster.getAllData();

		if (responseMessage != null && responseMessage.getResult() != null) {
			responseMessage = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "GET ALL COUNTRIES",
					responseMessage.getResult());
		} else {
			responseMessage = new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
					"NO COUNTRIES FOUND", null);
		}
		LOGGER.debug("CountryMaster list fetched: " + responseMessage.getResult());
		return responseMessage;
	}

	@RequestMapping(value = "/saveOrUpdate", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=application/json")
	public ResponseMessage saveOrUpdate(@RequestBody DtoCountryMaster dtoCountryMaster, HttpServletRequest request)
			throws Exception {
		LOGGER.info("Save or Update CountryMaster: " + dtoCountryMaster);
		ResponseMessage responseMessage = serviceCountryMaster.saveAndUpdate(dtoCountryMaster);

		if (responseMessage.getResult() != null) {
			responseMessage = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK,
					"Successfully Saved The Country Master", responseMessage.getResult());
		} else {
			responseMessage = new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
					"Unable To Save Or Update CountryMaster", dtoCountryMaster);
		}
		LOGGER.debug("Saved or Updated CountryMaster: " + dtoCountryMaster);
		return responseMessage;
	}

	@RequestMapping(value = "/getById", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=application/json")
	public ResponseMessage getById(@RequestBody DtoSearch dtoSearch) {
		LOGGER.info("Fetching CountryMaster by ID: " + dtoSearch);
		ResponseMessage responseMessage;
		try {
			ResponseMessage res = serviceCountryMaster.getById(dtoSearch.getId());
			responseMessage = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched successfully",
					res.getResult());
		} catch (Exception e) {
			LOGGER.error("Error fetching CountryMaster", e);
			responseMessage = new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
					"CountryMaster not found", null);
		}
		LOGGER.debug("Fetched CountryMaster: " + responseMessage);
		return responseMessage;
	}
}
