package com.zbs.de.controller;

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
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.service.ServiceEventMaster;
import com.zbs.de.util.ResponseMessage;

@RestController
@RequestMapping("/eventMaster")
@CrossOrigin(origins = "")
public class ControllerEventMaster {

	@Autowired
	ServiceEventMaster serviceEventMaster;

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerEventType.class);

	@PostMapping(value = "/saveOrUpdate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage saveOrUpdate(@RequestBody DtoEventMaster dtoEventMaster) {
		LOGGER.info("Saving Event Master: {}", dtoEventMaster);
		DtoResult result = serviceEventMaster.saveAndUpdate(dtoEventMaster);
		if (result.getResult() != null && result.getTxtMessage().equalsIgnoreCase("success")) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully saved", result.getResult());
		}
		return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to save",
				dtoEventMaster);
	}

	@PostMapping(value = "/generateEventCode", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage generateEventCode() {
		String txtCode = serviceEventMaster.generateNextEventMasterCode();
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched Event Types", txtCode);
	}

}
