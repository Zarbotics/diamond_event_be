package com.zbs.de.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.zbs.de.util.ResponseMessage;
import com.zbs.de.model.dto.DtoCityMaster;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.service.ServiceCityMaster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/cityMaster")
@CrossOrigin(origins = "")
public class ControllerCityMaster {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerCityMaster.class);

	@Autowired
	private ServiceCityMaster serviceCityMaster;

	@RequestMapping(value = "/getAllData", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=application/json")
	public ResponseMessage getAllData(HttpServletRequest request) throws Exception {
		LOGGER.info("Fetching all CityMaster data");
		ResponseMessage responseMessage;
		responseMessage = serviceCityMaster.getAllData();
		if (responseMessage != null && responseMessage.getResult() != null) {
			responseMessage = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "GET ALL CITIES",
					responseMessage.getResult());
		} else {
			responseMessage = new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND, "NO CITIES FOUND",
					null);
		}
		LOGGER.debug("CityMaster list fetched: " + responseMessage.getResult());
		return responseMessage;
	}

	@RequestMapping(value = "/getAllActive", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=application/json")
	public ResponseMessage getAllActive(HttpServletRequest request) throws Exception {
		LOGGER.info("Fetching all Active CityMaster data");
		DtoResult dtoResult;
		dtoResult = serviceCityMaster.getAllActive();
		if (dtoResult != null && dtoResult.getResult() != null) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, dtoResult.getTxtMessage(),
					dtoResult.getResult());
		} else {
			return new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND, dtoResult.getTxtMessage(),
					null);
		}
	}

	@RequestMapping(value = "/saveOrUpdate", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=application/json")
	public ResponseMessage saveOrUpdate(@RequestBody DtoCityMaster dtoCityMaster, HttpServletRequest request)
			throws Exception {
		LOGGER.info("Save or Update CityMaster: " + dtoCityMaster);
		ResponseMessage responseMessage = serviceCityMaster.saveAndUpdate(dtoCityMaster);

		if (responseMessage.getResult() != null) {
			responseMessage = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK,
					"Successfully Saved The City Master", responseMessage.getResult());
		} else {
			responseMessage = new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
					"Unable To Save Or Update CityMaster", dtoCityMaster);
		}
		LOGGER.debug("Saved or Updated CityMaster: " + dtoCityMaster);
		return responseMessage;
	}

	@RequestMapping(value = "/getById", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=application/json")
	public ResponseMessage getById(@RequestBody DtoSearch dtoSearch) {
		LOGGER.info("Fetching CityMaster by ID: " + dtoSearch);
		ResponseMessage responseMessage;
		try {
			ResponseMessage res = serviceCityMaster.getById(dtoSearch.getId());
			responseMessage = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched successfully",
					res.getResult());
		} catch (Exception e) {
			LOGGER.error("Error fetching CityMaster", e);
			responseMessage = new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
					"CityMaster not found", null);
		}
		LOGGER.info("Fetched CityMaster response: " + responseMessage);
		return responseMessage;
	}

	@PostMapping(value = "/deleteById", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage deleteById(@RequestBody DtoSearch dtoSearch) {
		LOGGER.info("Deleting CityMaster by ID: " + dtoSearch);
		try {
			DtoResult result = serviceCityMaster.deleteById(dtoSearch.getId());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(), null);
		} catch (Exception e) {
			LOGGER.error("Error Deleting CityMaster", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}

	}

}
