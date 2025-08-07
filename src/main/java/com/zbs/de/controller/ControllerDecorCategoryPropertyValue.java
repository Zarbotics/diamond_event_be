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
import com.zbs.de.model.dto.DtoDecorCategoryPropertyMaster;
import com.zbs.de.model.dto.DtoDecorCategoryPropertyValue;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.service.ServiceDecorCategoryPropertyValue;
import com.zbs.de.util.ResponseMessage;

@RequestMapping("/decorCategoryPropertyValue")
@CrossOrigin(origins = "")
@RestController
public class ControllerDecorCategoryPropertyValue {

	@Autowired
	ServiceDecorCategoryPropertyValue serviceDecorCategoryPropertyValue;

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerEventType.class);

	@PostMapping(value = "/saveOrUpdate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage saveOrUpdate(@RequestBody DtoDecorCategoryPropertyValue dto) {
		DtoResult result = serviceDecorCategoryPropertyValue.saveOrUpdate(dto);
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(), result.getResult());
	}

	@PostMapping(value = "/saveWithListValues", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage saveWithListValues(@RequestBody DtoDecorCategoryPropertyMaster dto) {
		DtoResult result = serviceDecorCategoryPropertyValue.saveWithListValues(dto);
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(), result.getResult());
	}

	@PostMapping(value = "/getAllData", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllData() {
		DtoResult result = serviceDecorCategoryPropertyValue.getAll();
		if (result.getResulList() != null && !result.getResulList().isEmpty()) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched successfully",
					result.getResulList());
		}
		return new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND, "No data found", null);
	}

	@PostMapping(value = "/getByPropertyId", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getByPropertyId(@RequestBody DtoSearch dtoSearch) {
		DtoResult result = serviceDecorCategoryPropertyValue.getByPropertyId(dtoSearch.getId());
		if (result.getResulList() != null && !result.getResulList().isEmpty()) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched by PropertyId",
					result.getResulList());
		}
		return new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND, "No values found for property",
				null);
	}

	@PostMapping(value = "/getById", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getById(@RequestBody DtoSearch dtoSearch) {
		DtoResult result = serviceDecorCategoryPropertyValue.getById(dtoSearch.getId());
		if (result.getResult() != null) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		}
		return new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND, result.getTxtMessage(), null);
	}

	@PostMapping(value = "/deleteById", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage deleteById(@RequestBody DtoSearch dtoSearch) {
		DtoResult result = serviceDecorCategoryPropertyValue.deleteById(dtoSearch.getId());
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(), null);
	}

	@PostMapping(value = "/saveValuesWithDocuments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseMessage saveWithDocs(@RequestPart("dtoDecorCategoryProperty") String dtoDecorCategoryProperty,
			@RequestPart(value = "files", required = false) List<MultipartFile> files) throws IOException {
		LOGGER.info("Saving Property Values: {}", dtoDecorCategoryProperty);
		DtoDecorCategoryPropertyMaster dtoDecorCategoryPropertyMaster = new ObjectMapper()
				.readValue(dtoDecorCategoryProperty, DtoDecorCategoryPropertyMaster.class);
		DtoResult result = serviceDecorCategoryPropertyValue.saveListValuesWithDocuments(dtoDecorCategoryPropertyMaster,
				files);
		if (result != null && result.getTxtMessage().equalsIgnoreCase("success")) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully saved", result.getResult());
		}
		return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to save",
				dtoDecorCategoryProperty);
	}
}