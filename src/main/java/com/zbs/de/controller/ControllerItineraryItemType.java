package com.zbs.de.controller;

import com.zbs.de.model.dto.DtoItineraryItemType;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.service.ServiceItineraryItemType;
import com.zbs.de.util.ResponseMessage;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/itinerary/item-type")
@CrossOrigin(origins = "")
public class ControllerItineraryItemType {

	@Autowired
	private ServiceItineraryItemType service;

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerItineraryItemType.class);

	@PostMapping("/save")
	public ResponseMessage save(@RequestBody DtoItineraryItemType dto, HttpServletRequest request) {
		LOGGER.info("Saving ItineraryItemType: {}", dto);
		DtoResult result = service.create(dto);
		if (result.getResult() != null) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		}
		return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, result.getTxtMessage(), dto);
	}

	@PostMapping("/update")
	public ResponseMessage update(@RequestBody DtoItineraryItemType dto, HttpServletRequest request) {
		LOGGER.info("Updating ItineraryItemType: {}", dto);
		DtoResult result = service.update(dto);
		if (result.getResult() != null) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		}
		return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, result.getTxtMessage(), dto);
	}

	@PostMapping("/getAll")
	public ResponseMessage getAll(HttpServletRequest request) {
		LOGGER.info("Fetching all ItineraryItemTypes");
		DtoResult result = service.getAll();
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(), result.getResult());
	}

	@PostMapping("/getAllActive")
	public ResponseMessage getAllActive(HttpServletRequest request) {
		LOGGER.info("Fetching all active ItineraryItemTypes");
		DtoResult result = service.getAllActive();
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(), result.getResult());
	}

	@PostMapping("/getById")
	public ResponseMessage getById(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		LOGGER.info("Fetching ItineraryItemType by ID: {}", dtoSearch.getId());
		DtoResult result = service.getById(dtoSearch.getId());
		if (result.getResult() != null) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		}
		return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, result.getTxtMessage(),
				dtoSearch.getId());
	}

	@PostMapping(value = "/generateCode", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage generateMenuItemCode(HttpServletRequest request) {
		try {

			DtoResult result = service.generateNextCode();
			String txtCode = null;
			if (result != null && result.getResult() != null) {
				txtCode = result.getResult().toString();
			}

			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched MenuItem Code.", txtCode);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to generate code.", null);
		}
	}
}
