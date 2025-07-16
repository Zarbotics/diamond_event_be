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
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.model.dto.DtoStateMaster;
import com.zbs.de.service.ServiceStateMaster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/stateMaster")
@CrossOrigin(origins = "")
public class ControllerStateMaster {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerStateMaster.class);

	@Autowired
	private ServiceStateMaster serviceStateMaster;

	@RequestMapping(value = "/getAllData", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=application/json")
	public ResponseMessage getAllData(HttpServletRequest request) throws Exception {
		LOGGER.info("Fetching all StateMaster data");

		ResponseMessage responseMessage;
		responseMessage = serviceStateMaster.getAllData();

		if (responseMessage != null && responseMessage.getResult() != null) {
			responseMessage = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "GET ALL STATES",
					responseMessage.getResult());
		} else {
			responseMessage = new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND, "NO STATES FOUND",
					null);
		}
		LOGGER.debug("StateMaster list fetched: " + responseMessage.getResult());
		return responseMessage;
	}

	@RequestMapping(value = "/saveOrUpdate", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=application/json")
	public ResponseMessage saveOrUpdate(@RequestBody DtoStateMaster dtoStateMaster, HttpServletRequest request)
			throws Exception {
		LOGGER.info("Save or Update StateMaster: " + dtoStateMaster);
		ResponseMessage responseMessage = serviceStateMaster.saveAndUpdate(dtoStateMaster);

		if (responseMessage.getResult() != null) {
			responseMessage = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK,
					"Successfully Saved The State Master", responseMessage.getResult());
		} else {
			responseMessage = new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
					"Unable To Save Or Update StateMaster", dtoStateMaster);
		}
		LOGGER.debug("Saved or Updated StateMaster: " + dtoStateMaster);
		return responseMessage;
	}

	@RequestMapping(value = "/getById", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=application/json")
	public ResponseMessage getById(@RequestBody DtoSearch dtoSearch) {
		LOGGER.info("Fetching StateMaster by ID: " + dtoSearch);
		ResponseMessage responseMessage;
		try {
			ResponseMessage res = serviceStateMaster.getById(dtoSearch.getId());
			responseMessage = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched successfully",
					res.getResult());
		} catch (Exception e) {
			LOGGER.error("Error fetching StateMaster", e);
			responseMessage = new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
					"StateMaster not found", null);
		}
		LOGGER.debug("Fetched StateMaster: " + responseMessage);
		return responseMessage;
	}
	
	@PostMapping(value = "/deleteById", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage deleteById(@RequestBody DtoSearch dtoSearch) {
		LOGGER.info("Deleting StateMaster by ID: " + dtoSearch);
		try {
			DtoResult result = serviceStateMaster.deleteById(dtoSearch.getId());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(), null);
		} catch (Exception e) {
			LOGGER.error("Error Deleting StateMaster", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}

	}

}
