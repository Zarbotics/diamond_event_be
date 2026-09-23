package com.zbs.de.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.zbs.de.util.ResponseMessage;

import jakarta.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zbs.de.model.dto.DtoResult;
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
	public ResponseMessage saveOrUpdate(@RequestBody DtoVenueMaster dto, HttpServletRequest request) {
		return serviceVenueMaster.saveOrUpdate(dto);
	}

	@PostMapping(value = "/getAll", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAll(HttpServletRequest request) {
		return serviceVenueMaster.getAllVenues();
	}

	@PostMapping(value = "/getByCityId", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getByCityId(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		return serviceVenueMaster.getVenuesByCityId(dtoSearch.getId());
	}

	@PostMapping(value = "/getAllGroupedByCity", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllGroupedByCity(HttpServletRequest request) {
		return serviceVenueMaster.getAllVenuesGroupedByCity();
	}
	
	@PostMapping(value = "/getAllActiveVenuesGroupedByActiveCities", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllActiveVenuesGroupedByActiveCities(HttpServletRequest request) {
		return serviceVenueMaster.getAllActiveVenuesGroupedByActiveCities();
	}

	@PostMapping(value = "/saveVenue", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseMessage saveVenue(@RequestPart("venueData") String venueJson,
			@RequestPart(value = "files", required = false) List<MultipartFile> files) {

		ResponseMessage responseMessage;

		try {
			DtoVenueMaster dto = new ObjectMapper().readValue(venueJson, DtoVenueMaster.class);
			/*
			 * What was saved, not the body that said so. These payloads are
			 * several kilobytes each and the log is more useful with a name in
			 * it than a blob. Bodies stay out of the log everywhere, at every
			 * level, so that the rule is one a person can follow without having
			 * to judge whether this particular one carries personal data —
			 * RequestPayloadLoggingTest holds it.
			 */
			LOGGER.info("Saving venue {} ({})", dto.getSerVenueMasterId(), dto.getTxtVenueName());
			DtoResult dtoResult = serviceVenueMaster.saveVenueWithDetails(dto, files);
			if (dtoResult != null && dtoResult.getTxtMessage().equalsIgnoreCase("Success")) {
				responseMessage = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Saved successfully", null);
			} else {
				responseMessage = new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
						HttpStatus.INTERNAL_SERVER_ERROR, dtoResult.getTxtMessage(), null);
			}

		} catch (Exception e) {
			LOGGER.error("Error fetching StateMaster", e);
			responseMessage = new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
					"StateMaster not found", null);
		}

		LOGGER.debug("Save Venue: " + responseMessage);
		return responseMessage;
	}

	@PostMapping(value = "/deleteById", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage deleteById(@RequestBody DtoSearch dtoSearch) {
		LOGGER.info("Deleting VenueMaster by ID: " + dtoSearch);
		try {
			DtoResult result = serviceVenueMaster.deleteById(dtoSearch.getId());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(), null);
		} catch (Exception e) {
			LOGGER.error("Error Deleting VenueMaster", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}

	}

	@PostMapping(value = "/generateVenueMasterCode", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage generateEventCode(HttpServletRequest request) {
		String txtCode = serviceVenueMaster.generateNextVenueMasterCode();
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched Venue Master Code.", txtCode);
	}

}
