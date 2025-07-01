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

import com.zbs.de.model.dto.DtoDecorCategoryColorMapping;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.service.ServiceDecorCategoryColorMapping;
import com.zbs.de.util.ResponseMessage;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/decorCategoryColorMapping")
@CrossOrigin("")
public class ControllerDecorCategoryColorMapping {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerDecorItemMaster.class);

	@Autowired
	ServiceDecorCategoryColorMapping serviceDecorCategoryColorMapping;

	@PostMapping(value = "/getByCategoryId", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getByCategoryId(HttpServletRequest request,@RequestBody DtoSearch dtoSearch) {
		LOGGER.info("Fetching all Category Color");
		DtoResult dtoResult = serviceDecorCategoryColorMapping.getByCategoryId(dtoSearch.getId());
		if (dtoResult.getResulList() != null && !dtoResult.getResulList().isEmpty()) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched successfully",
					dtoResult.getResulList());
		}
		return new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND, "No data found", null);
	}

	@PostMapping(value = "/saveOrUpdate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage saveOrUpdate(@RequestBody DtoDecorCategoryColorMapping dto, HttpServletRequest request) {
		LOGGER.info("Saving or updating Decor Color Mapping : {}", dto);
		DtoResult dtoResult = serviceDecorCategoryColorMapping.saveOrUpdate(dto);
		if (dtoResult.getResult() != null) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Saved successfully",
					dtoResult.getResult());
		}
		return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, dtoResult.getTxtMessage(),
				null);
	}
}
