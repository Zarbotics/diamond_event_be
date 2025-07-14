package com.zbs.de.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zbs.de.model.dto.DtoEventMaster;
import com.zbs.de.model.dto.DtoEventMasterStats;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.service.ServiceEventMaster;
import com.zbs.de.util.ResponseMessage;
import com.zbs.de.util.UtilRandomKey;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/eventMaster")
@CrossOrigin(origins = "")
public class ControllerEventMaster {

	@Autowired
	ServiceEventMaster serviceEventMaster;

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerEventType.class);

	@PostMapping(value = "/saveOrUpdate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage saveOrUpdate(@RequestBody DtoEventMaster dtoEventMaster, HttpServletRequest request) {
		LOGGER.info("Saving Event Master: {}", dtoEventMaster);
		DtoResult result = serviceEventMaster.saveAndUpdate(dtoEventMaster);
		if (result.getResult() != null && result.getTxtMessage().equalsIgnoreCase("success")) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully saved", result.getResult());
		}
		return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to save",
				dtoEventMaster);
	}

	@PostMapping(value = "/generateEventCode", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage generateEventCode(HttpServletRequest request) {
		String txtCode = serviceEventMaster.generateNextEventMasterCode();
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched Event Types", txtCode);
	}

	@PostMapping(value = "/getByEventIdAndCustomerId", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage saveOrUpdate(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		LOGGER.info("Searching Event Master: {}", dtoSearch);
		DtoResult result = serviceEventMaster.getByEventTypeIdAndCustId(dtoSearch);
		if (result.getResult() != null && result.getTxtMessage().equalsIgnoreCase("success")) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Fetched",
					result.getResult());
		}
		return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to Fetch",
				dtoSearch);
	}

	@PostMapping(value = "/getAllData", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllData(HttpServletRequest request) {
		LOGGER.info("Searching Event Masters");
		DtoResult result = serviceEventMaster.getAllEvents();
		if (result.getResulList() != null && result.getTxtMessage().equalsIgnoreCase("success")) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Fetched",
					result.getResulList());
		}
		return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
				result.getTxtMessage(), null);
	}

	@PostMapping(value = "/getEventStats", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getEventStats(HttpServletRequest request) {
		LOGGER.info("Saving Event Master");
		List<DtoEventMasterStats> result = serviceEventMaster.getEventTypeStats();
		if (UtilRandomKey.isNotNull(result)) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully saved", result);
		}
		return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to Calculate", null);
	}

}
