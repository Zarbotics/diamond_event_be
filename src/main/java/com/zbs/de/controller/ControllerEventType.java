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
import com.zbs.de.model.dto.DtoEventTypeRunningOrder;
import com.zbs.de.util.ResponseMessage;
import com.zbs.de.util.UtilRandomKey;

import jakarta.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zbs.de.model.dto.DtoEventType;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.service.ServiceEventType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/eventType")
@CrossOrigin(origins = "")
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
	
	
	@PostMapping(value = "/getAllActiveEventTypesWithSubEvents", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllActiveEventTypesWithSubEvents() {
		List<DtoEventType> list = serviceEventType.getAllActiveEventTypesWithSubEvents();
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched Event Types", list);
	}
	
	
	@PostMapping(value = "/getAllActiveSubEventsOnlyCP", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllActiveSubEventsOnlyCP() {
		List<DtoEventType> list = serviceEventType.getAllActiveSubEventsOnlyCP();
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched Event Types", list);
	}

	/**
	 * Which moments this kind of event's running order asks for.
	 *
	 * <h4>What this does not do</h4>
	 *
	 * Touch a booking. Unticking a moment changes what the booking screen asks
	 * for from here on; a time already recorded against a booking stays in the
	 * database and the screen goes on showing it, marked as not usually asked
	 * for this kind of event.
	 *
	 * <p>
	 * That is the point. A form submits only the fields it has rendered, so
	 * hiding a moment some booking already has a time in would clear it on that
	 * booking's next save — for an unrelated reason, months later, by somebody
	 * who did nothing wrong.
	 */
	@PostMapping(value = "/setRunningOrderMoments", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage setRunningOrderMoments(@RequestBody DtoEventTypeRunningOrder request) {
		DtoResult result = serviceEventType.setRunningOrderMoments(
				request.getSerEventTypeId(), request.getTxtRunningOrderMoments());

		if (!"Success".equalsIgnoreCase(result.getTxtMessage())) {
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					String.valueOf(result.getResult()), null);
		}

		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Saved", result.getResult());
	}

	@PostMapping(value = "/saveEventType", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseMessage saveVenue(@RequestPart("eventTypeData") String eventTypeJson,
			@RequestPart("files") List<MultipartFile> files) {

		ResponseMessage responseMessage;

		try {
			DtoEventType dto = new ObjectMapper().readValue(eventTypeJson, DtoEventType.class);
			/*
			 * What was saved, not the body that said so. These payloads are
			 * several kilobytes each and the log is more useful with a name in
			 * it than a blob. Bodies stay out of the log everywhere, at every
			 * level, so that the rule is one a person can follow without having
			 * to judge whether this particular one carries personal data —
			 * RequestPayloadLoggingTest holds it.
			 */
			LOGGER.info("Saving event type {} ({})", dto.getSerEventTypeId(), dto.getTxtEventTypeName());
			DtoResult dtoResult = serviceEventType.saveEventTypeWithDocuments(dto, files);
			if (UtilRandomKey.isNotNull(dtoResult.getResult())) {
				responseMessage = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Saved successfully",
						dtoResult.getResult());
			} else {
				responseMessage = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, dtoResult.getTxtMessage(),
						dtoResult.getResult());
			}

		} catch (Exception e) {
			LOGGER.error("Internal Server Error", e);
			responseMessage = new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
					"Internal Server Error", null);
		}

		LOGGER.debug("Save Venue: " + responseMessage);
		return responseMessage;
	}
	
	@PostMapping(value = "/deleteById", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage deleteById(@RequestBody DtoSearch dtoSearch) {
		LOGGER.info("Deleting EventType by ID: " + dtoSearch);
		try {
			DtoResult result = serviceEventType.deleteById(dtoSearch.getId());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(), null);
		} catch (Exception e) {
			LOGGER.error("Error Deleting EventType", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}

	}
	
	@PostMapping(value = "/generateEventCode", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage generateEventCode(HttpServletRequest request) {
		String txtCode = serviceEventType.generateNextEventTypeCode();
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched Event Types", txtCode);
	}



}