package com.zbs.de.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.zbs.de.service.impl.ServiceDecorCategoryColorMappingImpl;
import com.zbs.de.util.ResponseMessage;
import com.zbs.de.util.UtilRandomKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zbs.de.DeApplication;
import com.zbs.de.model.VenueMaster;
import com.zbs.de.model.dto.DtoEventType;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.model.dto.DtoVenueMaster;
import com.zbs.de.service.ServiceEventType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/eventType")
@CrossOrigin(origins = "")
public class ControllerEventType {

	private final DeApplication deApplication;

	private final ServiceDecorCategoryColorMappingImpl serviceDecorCategoryColorMappingImpl;

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerEventType.class);

	@Autowired
	ServiceEventType serviceEventType;

	ControllerEventType(ServiceDecorCategoryColorMappingImpl serviceDecorCategoryColorMappingImpl,
			DeApplication deApplication) {
		this.serviceDecorCategoryColorMappingImpl = serviceDecorCategoryColorMappingImpl;
		this.deApplication = deApplication;
	}

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

	@PostMapping(value = "/saveEventType", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseMessage saveVenue(@RequestPart("eventTypeData") String eventTypeJson,
			@RequestPart("files") List<MultipartFile> files) {

		LOGGER.info("Save Event Type EventTypeMaster by Dto: " + eventTypeJson);

		ResponseMessage responseMessage;

		try {
			DtoEventType dto = new ObjectMapper().readValue(eventTypeJson, DtoEventType.class);
			DtoResult dtoResult = serviceEventType.saveEventTypeWithDocuments(dto, files);
			if (UtilRandomKey.isNotNull(dtoResult.getResult())) {
				responseMessage = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Saved successfully",
						dtoResult.getResult());
			} else {
				responseMessage = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, dtoResult.getTxtMessage(),
						dtoResult.getResult());
			}

		} catch (Exception e) {
			LOGGER.error("Error fetching StateMaster", e);
			responseMessage = new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
					"StateMaster not found", null);
		}

		LOGGER.debug("Save Venue: " + responseMessage);
		return responseMessage;
	}

}