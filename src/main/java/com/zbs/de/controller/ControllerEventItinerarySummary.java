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

import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.service.ServiceEventItinerarySummary;
import com.zbs.de.util.ResponseMessage;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/eventItinerary")
@CrossOrigin(origins = "")
public class ControllerEventItinerarySummary {

	@Autowired
	private ServiceEventItinerarySummary serviceEventItinerarySummary;

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerEventItinerarySummary.class);

	@PostMapping(value = "/calulate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage calulate(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		LOGGER.info("calculating Itinerary Fo Event Master: {}", dtoSearch.getId());
		try {
			serviceEventItinerarySummary.calculateEventItinerary(dtoSearch.getId());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Caclulated", null);
		} catch (Exception e) {
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to Calculate.",
					null);
		}

	}

	@PostMapping(value = "/getPerMenuItemByEventId", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getPerMenuItemByEventId(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		LOGGER.info("Fetching Itinerary For Event Master: {}", dtoSearch.getId());
		try {
			DtoResult result = serviceEventItinerarySummary.getMenuItinerary(dtoSearch.getId());
			if (result != null && result.getResult() != null) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Fetched",
						result.getResult());
			} else {
				return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to Fetch.",
						null);
			}
		} catch (Exception e) {
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to Fetch.",
					null);
		}

	}

	@PostMapping(value = "/getSummaryByEventId", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getSummaryByEventId(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		LOGGER.info("Fetching Itinerary Summary For Event Master: {}", dtoSearch.getId());
		try {
			DtoResult result = serviceEventItinerarySummary.getEventItinerarySummary(dtoSearch.getId());
			if (result != null && result.getResult() != null) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Fetched",
						result.getResult());
			} else {
				return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to Fetch.",
						null);
			}
		} catch (Exception e) {
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to Fetch.",
					null);
		}

	}
}
