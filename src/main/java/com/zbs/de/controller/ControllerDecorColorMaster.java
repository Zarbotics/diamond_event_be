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

import com.zbs.de.model.dto.DtoDecorColorMaster;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.service.ServiceDecorColorMaster;
import com.zbs.de.util.ResponseMessage;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/decorColorMaster")
@CrossOrigin("")
public class ControllerDecorColorMaster {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerDecorItemMaster.class);

	@Autowired
	ServiceDecorColorMaster serviceDecorColorMaster;

	@PostMapping(value = "/getAllData", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllData(HttpServletRequest request) {
		LOGGER.info("Fetching all decor items");
		DtoResult dtoResult = serviceDecorColorMaster.getAll();
		if (dtoResult.getResulList() != null && !dtoResult.getResulList().isEmpty()) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched successfully",
					dtoResult.getResulList());
		}
		return new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND, "No data found", null);
	}

	@PostMapping(value = "/saveOrUpdate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage saveOrUpdate(@RequestBody DtoDecorColorMaster dto, HttpServletRequest request) {
		LOGGER.info("Saving or updating decor item: {}", dto);
		DtoResult dtoResult = serviceDecorColorMaster.saveOrUpdate(dto);
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
		DtoResult dtoResult = serviceDecorColorMaster.getById(dtoSearch.getId());
		if (dtoResult.getResult() != null) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched successfully",
					dtoResult.getResult());
		}
		return new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND, dtoResult.getTxtMessage(), null);
	}
}
