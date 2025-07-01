package com.zbs.de.controller;

import jakarta.servlet.http.HttpServletRequest;

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
import com.zbs.de.util.UtilRandomKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zbs.de.model.dto.DtoDecorItemMaster;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.service.ServiceDecorItemMaster;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/decorItemMaster")
@CrossOrigin(origins = "")
public class ControllerDecorItemMaster {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerDecorItemMaster.class);

	@Autowired
	ServiceDecorItemMaster serviceDecorItemMaster;

	@PostMapping(value = "/getAllData", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllData(HttpServletRequest request) {
		LOGGER.info("Fetching all decor items");
		DtoResult dtoResult = serviceDecorItemMaster.getAll();
		if (dtoResult.getResulList() != null && !dtoResult.getResulList().isEmpty()) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched successfully",
					dtoResult.getResulList());
		}
		return new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND, "No data found", null);
	}

	@PostMapping(value = "/saveOrUpdate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage saveOrUpdate(@RequestBody DtoDecorItemMaster dto, HttpServletRequest request) {
		LOGGER.info("Saving or updating decor item: {}", dto);
		DtoResult dtoResult = serviceDecorItemMaster.saveOrUpdate(dto);
		if (dtoResult.getResult() != null) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Saved successfully",
					dtoResult.getResult());
		}
		return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, dtoResult.getTxtMessage(),
				null);
	}

	@PostMapping(value = "/getById", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getById(@RequestBody DtoSearch dtoSearch) {
		LOGGER.info("Fetching DecorItemMaster by ID: {}", dtoSearch.getId());
		DtoResult dtoResult = serviceDecorItemMaster.getById(dtoSearch.getId());
		if (dtoResult.getResult() != null) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched successfully",
					dtoResult.getResult());
		}
		return new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND, dtoResult.getTxtMessage(), null);
	}

	@PostMapping(value = "/saveDecorItem", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseMessage saveDecorItem(@RequestPart("decorData") String decorJson,
			@RequestPart("files") List<MultipartFile> files) {
		LOGGER.info("Saving DecorItem with documents");

		try {
			DtoDecorItemMaster dto = new ObjectMapper().readValue(decorJson, DtoDecorItemMaster.class);
			DtoResult saved = serviceDecorItemMaster.saveDecorItemWithDocuments(dto, files);
			if (UtilRandomKey.isNotNull(saved) && saved.getTxtMessage().equalsIgnoreCase("Saved Successfully")) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Saved Successfully", dto);
			} else {
				return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
						"Failed to save decor item", null);
			}
		} catch (Exception e) {
			LOGGER.error("Error saving DecorItem", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to save decor item", null);
		}
	}
}