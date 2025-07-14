package com.zbs.de.controller;

import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.zbs.de.util.ResponseMessage;
import com.zbs.de.model.dto.DtoMenuFoodMaster;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.service.ServiceMenuFoodMaster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/menuFoodMaster")
@CrossOrigin(origins = "")
public class ControllerMenuFoodMaster {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerMenuFoodMaster.class);

	@Autowired
	ServiceMenuFoodMaster serviceMenuFoodMaster;

	@RequestMapping(value = "/getAllData", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllData(HttpServletRequest request) {
		LOGGER.info("Fetching all menu food items");
		List<DtoMenuFoodMaster> data = serviceMenuFoodMaster.getAllData();
		if (data != null && !data.isEmpty()) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched successfully", data);
		}
		return new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND, "No data found", null);
	}

	@RequestMapping(value = "/saveOrUpdate", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage saveOrUpdate(@RequestBody DtoMenuFoodMaster dto, HttpServletRequest request) {
		LOGGER.info("Saving or updating menu food item: {}", dto);
		ResponseMessage res = serviceMenuFoodMaster.saveAndUpdate(dto);
		if (res.getResult() != null) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Saved successfully", res.getResult());
		}
		return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, res.getMessage(), null);
	}

	@RequestMapping(value = "/getById", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getById(@RequestBody DtoSearch dtoSearch) {
		LOGGER.info("Fetching MenuFoodMaster by ID: {}", dtoSearch.getId());
		ResponseMessage res = serviceMenuFoodMaster.getById(dtoSearch.getId());
		if (res.getResult() != null) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched successfully", res.getResult());
		}
		return new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND, res.getMessage(), null);
	}

	@RequestMapping(value = "/getFoodByType", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getFoodByType(@RequestBody DtoSearch dtoSearch) {
		LOGGER.info("Fetching MenuFoodMaster by Type : {}", dtoSearch.getSearchKeyword());
		ResponseMessage res = serviceMenuFoodMaster.getByType(dtoSearch.getSearchKeyword());
		if (res.getResult() != null) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched successfully", res.getResult());
		}
		return new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND, res.getMessage(), null);
	}

	@RequestMapping(value = "/getAllFoodsByType", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllFoodsByType(@RequestBody DtoSearch dtoSearch) {
		LOGGER.info("Fetching AllMenuFoodMaster by Type : {}", dtoSearch.getSearchKeyword());
		Map<String, List<DtoMenuFoodMaster>> result = serviceMenuFoodMaster.getAllFoodGroupedByType();
		if (result != null) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched successfully", result);
		}
		return new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND, "Unable To Fetch", null);
	}

}