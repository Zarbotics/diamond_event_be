package com.zbs.de.controller;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.zbs.de.util.ResponseMessage;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zbs.de.model.VenueMaster;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.model.dto.DtoVenueMaster;
import com.zbs.de.service.ServiceVenueMaster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/venueMaster")
@CrossOrigin(origins = "")
public class ControllerVenueMaster {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerStateMaster.class);

	@Autowired
	private ServiceVenueMaster serviceVenueMaster;

	@PostMapping(value = "/saveOrUpdate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage saveOrUpdate(@RequestBody DtoVenueMaster dto) {
		return serviceVenueMaster.saveOrUpdate(dto);
	}

	@PostMapping(value = "/getAll", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAll() {
		return serviceVenueMaster.getAllVenues();
	}

	@PostMapping(value = "/getByCityId", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getByCityId(@RequestBody DtoSearch dtoSearch) {
		return serviceVenueMaster.getVenuesByCityId(dtoSearch.getId());
	}

	@PostMapping(value = "/getAllGroupedByCity", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllGroupedByCity() {
		return serviceVenueMaster.getAllVenuesGroupedByCity();
	}

	@PostMapping(value = "/saveVenue", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseMessage saveVenue(@RequestPart("venueData") String venueJson,
			@RequestPart("files") List<MultipartFile> files) {

		LOGGER.info("Save Venue VenueMaster by Dto: " + venueJson);

		ResponseMessage responseMessage;

		try {
			DtoVenueMaster dto = new ObjectMapper().readValue(venueJson, DtoVenueMaster.class);
			VenueMaster venueMaster = serviceVenueMaster.saveVenueWithDetails(dto, files);
			responseMessage = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Saved successfully",
					venueMaster);
		} catch (Exception e) {
			LOGGER.error("Error fetching StateMaster", e);
			responseMessage = new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
					"StateMaster not found", null);
		}

		LOGGER.debug("Save Venue: " + responseMessage);
		return responseMessage;
	}

}
