package com.zbs.de.controller;

import java.io.IOException;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zbs.de.model.dto.DtoDecorExtrasMaster;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.service.ServiceDecorExtrasMaster;
import com.zbs.de.util.ResponseMessage;

import jakarta.servlet.http.HttpServletRequest;

@RequestMapping("/decorExtras")
@CrossOrigin(origins = "")
@RestController
public class ControllerDecorExtrasMaster {

	@Autowired
	private ServiceDecorExtrasMaster serviceDecorExtrasMaster;

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerEventType.class);

	@PostMapping(value = "/saveWithDocs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseMessage saveWithDocs(@RequestPart("decorExtrasMaster") String decorExtrasMaster,
			@RequestPart(value = "files", required = false) List<MultipartFile> files) throws IOException {
		LOGGER.info("Saving Event Master: {}", decorExtrasMaster);
		DtoDecorExtrasMaster dto = new ObjectMapper().readValue(decorExtrasMaster, DtoDecorExtrasMaster.class);
		DtoResult result = serviceDecorExtrasMaster.saveWithListOptions(dto, files);
		if (result != null && result.getTxtMessage().equalsIgnoreCase("success")) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully saved", result.getResult());
		}
		return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to save",
				decorExtrasMaster);
	}
	
	

	@PostMapping(value = "/saveAndUpdate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseMessage saveWithDocs(@RequestBody DtoDecorExtrasMaster dtoDecorExtrasMaster) {
		LOGGER.info("Saving Event Master: {}", dtoDecorExtrasMaster);
		DtoResult result = serviceDecorExtrasMaster.saveAndUpdate(dtoDecorExtrasMaster);
		if (result != null && result.getTxtMessage().equalsIgnoreCase("success")) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully saved", result.getResult());
		}
		return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to save",
				dtoDecorExtrasMaster);
	}
	

	@PostMapping(value = "/getAllData", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllData(HttpServletRequest request) {
		LOGGER.info("Searching Decor Extras");
		DtoResult result = serviceDecorExtrasMaster.getAll();
		if (result.getResult() != null && result.getTxtMessage().equalsIgnoreCase("success")) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Fetched",
					result.getResult());
		}
		return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
				result.getTxtMessage(), null);
	}

	@PostMapping(value = "/deleteById", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage deleteById(@RequestBody DtoSearch dtoSearch) {
		LOGGER.info("Deleting Decor Extra by ID: " + dtoSearch);
		try {
			DtoResult result = serviceDecorExtrasMaster.deleteById(dtoSearch.getId());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(), null);
		} catch (Exception e) {
			LOGGER.error("Error Deleting Decor Extra", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}

	}

	@PostMapping(value = "/deleteExtrasOptionById", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage deleteExtrasOptionById(@RequestBody DtoSearch dtoSearch) {
		LOGGER.info("Deleting Decor Extra by ID: " + dtoSearch);
		try {
			DtoResult result = serviceDecorExtrasMaster.deleteExtrasOptionById(dtoSearch.getId());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(), null);
		} catch (Exception e) {
			LOGGER.error("Error Deleting Decor Extra", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}

	}
}
