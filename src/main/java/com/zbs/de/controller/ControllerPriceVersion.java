package com.zbs.de.controller;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.model.dto.price.DtoPriceVersion;
import com.zbs.de.service.ServicePriceVersion;
import com.zbs.de.util.ResponseMessage;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/menu/price-version")
@CrossOrigin(origins = "")
public class ControllerPriceVersion {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerPriceVersion.class);

	@Autowired
	private ServicePriceVersion service;

	// -------------------------------------------------------------
	// CREATE
	// -------------------------------------------------------------
	@PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage create(@RequestBody DtoPriceVersion dto, HttpServletRequest request) {
		try {
			LOGGER.info("Creating Price Version: {}", dto.getTxtName());
			DtoResult result = service.create(dto);
			if (result.getResult() != null) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
						result.getResult());
			}
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, result.getTxtMessage(),
					null);
		} catch (Exception e) {
			LOGGER.error("Error creating Price Version", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to create Price Version: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// UPDATE
	// -------------------------------------------------------------
	@PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage update(@RequestBody DtoPriceVersion dto, HttpServletRequest request) {
		try {
			LOGGER.info("Updating Price Version ID: {}", dto.getSerPriceVersionId());
			DtoResult result = service.update(dto);
			if (result.getResult() != null) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
						result.getResult());
			}
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, result.getTxtMessage(),
					null);
		} catch (Exception e) {
			LOGGER.error("Error updating Price Version", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to update Price Version: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// DELETE
	// -------------------------------------------------------------
	@PostMapping(value = "/delete", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage delete(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Deleting Price Version ID: {}", dtoSearch.getIdL());
			DtoResult result = service.delete(dtoSearch.getIdL());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error deleting Price Version", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to delete Price Version: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GET BY ID
	// -------------------------------------------------------------
	@PostMapping(value = "/getById", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getById(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Fetching Price Version by ID: {}", dtoSearch.getIdL());
			DtoResult result = service.getById(dtoSearch.getIdL());
			if (result.getResult() != null) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
						result.getResult());
			}
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, result.getTxtMessage(),
					null);
		} catch (Exception e) {
			LOGGER.error("Error fetching Price Version by ID", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch Price Version: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GET ALL
	// -------------------------------------------------------------
	@PostMapping(value = "/getAll", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAll(HttpServletRequest request) {
		try {
			LOGGER.info("Fetching all Price Versions");
			DtoResult result = service.getAll();
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error fetching all Price Versions", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch Price Versions: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GET ALL ACTIVE
	// -------------------------------------------------------------
	@PostMapping(value = "/getAllActive", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllActive(HttpServletRequest request) {
		try {
			LOGGER.info("Fetching all active Price Versions");
			DtoResult result = service.getAllActive();
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error fetching active Price Versions", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch active Price Versions: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GET DEFAULT
	// -------------------------------------------------------------
	@PostMapping(value = "/getDefault", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getDefault(HttpServletRequest request) {
		try {
			LOGGER.info("Fetching default Price Version");
			DtoResult result = service.getDefault();
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error fetching default Price Version", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch default Price Version: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// ACTIVATE
	// -------------------------------------------------------------
	@PostMapping(value = "/activate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage activate(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Activating Price Version ID: {}", dtoSearch.getIdL());
			DtoResult result = service.activate(dtoSearch.getIdL());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error activating Price Version", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to activate Price Version: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// DEACTIVATE
	// -------------------------------------------------------------
	@PostMapping(value = "/deactivate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage deactivate(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Deactivating Price Version ID: {}", dtoSearch.getIdL());
			DtoResult result = service.deactivate(dtoSearch.getIdL());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error deactivating Price Version", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to deactivate Price Version: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// SET AS DEFAULT
	// -------------------------------------------------------------
	@PostMapping(value = "/setAsDefault", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage setAsDefault(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Setting Price Version as default ID: {}", dtoSearch.getIdL());
			DtoResult result = service.setAsDefault(dtoSearch.getIdL());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error setting Price Version as default", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to set Price Version as default: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// DUPLICATE
	// -------------------------------------------------------------
	@PostMapping(value = "/duplicate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage duplicate(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Duplicating Price Version ID: {} with new name: {}", dtoSearch.getIdL(),
					dtoSearch.getSearchKeyword());
			DtoResult result = service.duplicate(dtoSearch.getIdL(), dtoSearch.getSearchKeyword());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error duplicating Price Version", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to duplicate Price Version: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GET ACTIVE FOR DATE
	// -------------------------------------------------------------
	@PostMapping(value = "/getActiveForDate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getActiveForDate(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Fetching active Price Versions for date");
			Date date = dtoSearch.getDate() != null ? dtoSearch.getDate() : new Date();
			DtoResult result = service.getActiveForDate(date);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error fetching active Price Versions for date", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch active Price Versions: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GET BY CODE
	// -------------------------------------------------------------
	@PostMapping(value = "/getByCode", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getByCode(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Fetching Price Version by code: {}", dtoSearch.getSearchKeyword());
			DtoResult result = service.getByCode(dtoSearch.getSearchKeyword());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error fetching Price Version by code", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch Price Version: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GENERATE ASSIGNMENT CODE
	// -------------------------------------------------------------
	@PostMapping(value = "/generateCode", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage generateCode(HttpServletRequest request) {
		try {
			LOGGER.info("Generating assignment code");
			String code = service.generatePriceVersionCode();
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Code generated successfully", code);
		} catch (Exception e) {
			LOGGER.error("Error generating assignment code", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to generate assignment code: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// Get Status List
	// -------------------------------------------------------------
	@PostMapping(value = "/getStatusList", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getStatusList(HttpServletRequest request) {
		try {
			DtoResult result = service.getAllPriceVersionStatusValues();
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error fetching Price Version by code", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch Price Version: " + e.getMessage(), null);
		}
	}
}