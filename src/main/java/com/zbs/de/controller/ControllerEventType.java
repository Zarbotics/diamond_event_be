package com.zbs.de.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.zbs.de.util.ResponseMessage;
import com.zbs.de.model.dto.DtoEventType;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.service.ServiceEventType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/eventType")
public class ControllerEventType {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerEventType.class);

	@Autowired
	ServiceEventType serviceEventType;

	@PostMapping(value = "/getAllData", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllData() {
		List<DtoEventType> list = serviceEventType.getAllData();
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched Event Types", list);
	}

	@PostMapping(value = "/saveOrUpdate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage saveOrUpdate(@RequestBody DtoEventType dtoEventType) {
		LOGGER.info("Saving EventType: {}", dtoEventType);
		ResponseMessage response = serviceEventType.saveAndUpdate(dtoEventType);
		if (response.getResult() != null) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully saved",
					response.getResult());
		}
		return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to save",
				dtoEventType);
	}

	@PostMapping(value = "/getById", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getById(@RequestBody DtoSearch dtoSearch) {
		return serviceEventType.getById(dtoSearch.getId());
	}

	@PostMapping(value = "/getAllEventsWithSubEvents", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllEventsWithSubEvents() {
		List<DtoEventType> list = serviceEventType.getAllEventTypesWithSubEvents();
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched Event Types", list);
	}

}