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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zbs.de.model.dto.DtoDecorExtrasOption;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.service.ServiceDecorExtrasOption;
import com.zbs.de.util.ResponseMessage;

import jakarta.servlet.http.HttpServletRequest;

@RequestMapping("/extrasOption")
@CrossOrigin(origins = "")
@RestController
public class ControllerDecorExtrasOption {
	

	@Autowired
	private ServiceDecorExtrasOption serviceDecorExtrasOption;

	private static final Logger LOGGER = // Was ControllerEventType.class, copied in with the field: every line
	// this controller logged was filed under a different controller's name.
	LoggerFactory.getLogger(ControllerDecorExtrasOption.class);

	@PostMapping(value = "/saveWithDocs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseMessage saveWithDocs(@RequestPart("decorExtrasOption") String decorExtrasOption,
			@RequestPart(value = "files", required = false) List<MultipartFile> files) throws IOException {
		DtoDecorExtrasOption dto = new ObjectMapper().readValue(decorExtrasOption, DtoDecorExtrasOption.class);
		/*
		 * What was saved, not the body that said so. These payloads are
		 * several kilobytes each and the log is more useful with a name in
		 * it than a blob. Bodies stay out of the log everywhere, at every
		 * level, so that the rule is one a person can follow without having
		 * to judge whether this particular one carries personal data —
		 * RequestPayloadLoggingTest holds it.
		 */
		LOGGER.info("Saving decor extras option {} ({})", dto.getSerExtraOptionId(), dto.getTxtOptionName());
		DtoResult result = serviceDecorExtrasOption.saveExtrasOptionWithDoc(dto, files);
		if (result != null && result.getTxtMessage().equalsIgnoreCase("success")) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully saved", result.getResult());
		}
		return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to save",
				decorExtrasOption);
	}
	

	@PostMapping(value = "/getAllData", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllData(HttpServletRequest request) {
		LOGGER.info("Searching Decor Extras Options");
		DtoResult result = serviceDecorExtrasOption.getAll();
		if (result.getResult() != null && result.getTxtMessage().equalsIgnoreCase("success")) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Fetched",
					result.getResult());
		}
		return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
				result.getTxtMessage(), null);
	}

}
