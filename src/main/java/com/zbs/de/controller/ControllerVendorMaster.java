package com.zbs.de.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.zbs.de.util.ResponseMessage;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.model.dto.DtoVendorMaster;
import com.zbs.de.service.ServiceVendorMaster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/vendorMaster")
@CrossOrigin(origins = "")
public class ControllerVendorMaster {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerVendorMaster.class);

	@Autowired
	ServiceVendorMaster serviceVendorMaster;

	@RequestMapping(value = "/getAllData", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllData(HttpServletRequest request) {
		LOGGER.info("Fetching all vendor data");
		List<DtoVendorMaster> data = serviceVendorMaster.getAllData();
		if (data != null && !data.isEmpty()) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "All vendors fetched", data);
		}
		return new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND, "No vendors found", null);
	}

	@RequestMapping(value = "/saveOrUpdate", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage saveOrUpdate(@RequestBody DtoVendorMaster dto, HttpServletRequest request) {
		LOGGER.info("Saving or updating vendor: {}", dto);
		ResponseMessage res = serviceVendorMaster.saveAndUpdate(dto);
		if (res.getResult() != null) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Vendor saved", res.getResult());
		}
		return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, res.getMessage(), null);
	}

	@RequestMapping(value = "/getById", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getById(@RequestBody DtoSearch dtoSearch) {
		LOGGER.info("Fetching vendor by ID: {}", dtoSearch.getId());
		ResponseMessage res = serviceVendorMaster.getById(dtoSearch.getId());
		if (res.getResult() != null) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Vendor fetched", res.getResult());
		}
		return new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND, res.getMessage(), null);
	}

	@PostMapping(value = "/deleteById", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage deleteById(@RequestBody DtoSearch dtoSearch) {
		LOGGER.info("Deleting VendorMaster by ID: " + dtoSearch);
		try {
			DtoResult result = serviceVendorMaster.deleteById(dtoSearch.getId());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(), null);
		} catch (Exception e) {
			LOGGER.error("Error Deleting VendorMaster", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}

	}

}