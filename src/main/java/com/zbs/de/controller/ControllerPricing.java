package com.zbs.de.controller;

import com.zbs.de.model.dto.*;
import com.zbs.de.service.*;
import com.zbs.de.util.ResponseMessage;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pricing")
@CrossOrigin(origins = "")
public class ControllerPricing {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerPricing.class);

	@Autowired
	private ServicePriceVersion versionService;

	@Autowired
	private ServicePriceEntry entryService;

	@Autowired
	private ServicePricingRule ruleService;

	@Autowired
	private ServicePricingEngine engine;

	// ============================
	// PRICE VERSION CRUD
	// ============================

	@PostMapping(value = "/version/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage create(@RequestBody DtoPriceVersion dto, HttpServletRequest request) {
		LOGGER.info("Saving Price Version: {}", dto);
		try {
			DtoPriceVersion result = versionService.create(dto);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Saved", result);
		} catch (Exception e) {
			LOGGER.error("Error saving Price Version", e);
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"Failed to save Price Version", dto);
		}
	}

	@PostMapping(value = "/version/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage update(@RequestBody DtoPriceVersion dto, HttpServletRequest request) {
		LOGGER.info("Updating Price Version: {}", dto);
		try {
			DtoPriceVersion result = versionService.update(dto.getSerPriceVersionId(), dto);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Updated", result);
		} catch (Exception e) {
			LOGGER.error("Error updating Price Version", e);
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"Failed to update Price Version", dto);
		}
	}

	@PostMapping(value = "/version/publish", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage publishVersion(@RequestBody Long id) {
		LOGGER.info("Publishing Price Version: ", id);
		try {
			DtoPriceVersion result = versionService.publish(id);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Published Successfully", result);
		} catch (Exception e) {
			LOGGER.error("Error publishing version", e);
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"Failed to publish version", id);
		}
	}

	@PostMapping(value = "/version/getAll", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllVersions() {
		LOGGER.info("Fetching all Price Versions");
		try {
			List<DtoPriceVersion> list = versionService.listAll();
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Fetched", list);
		} catch (Exception e) {
			LOGGER.error("Error fetching versions", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch versions", null);
		}
	}

	// ============================
	// PRICE ENTRY CRUD
	// ============================

	@PostMapping(value = "/entry/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage create(@RequestBody DtoPriceEntry dto) {
		LOGGER.info("Saving Price Entry: {}", dto);
		try {
			DtoPriceEntry result = entryService.create(dto);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Saved", result);
		} catch (Exception e) {
			LOGGER.error("Error saving Price Entry", e);
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"Failed to save Price Entry", dto);
		}
	}

	@PostMapping(value = "/entry/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage update(@RequestBody DtoPriceEntry dto) {
		LOGGER.info("Updating Price Entry: {}", dto);
		try {
			DtoPriceEntry result = entryService.update(dto.getSerPriceEntryId(), dto);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Updated", result);
		} catch (Exception e) {
			LOGGER.error("Error updating Price Entry", e);
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"Failed to update Price Entry", dto);
		}
	}

	@PostMapping(value = "/entry/getByVersion", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage listEntriesByVersion(@RequestBody Long id) {
		LOGGER.info("Fetching Entries for Version ID: ", id);
		try {
			List<DtoPriceEntry> list = entryService.listByVersion(id);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Fetched", list);
		} catch (Exception e) {
			LOGGER.error("Error fetching Price Entries", e);
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"Failed to fetch Price Entries", null);
		}
	}

	// ============================
	// PRICING RULE CRUD
	// ============================

	@PostMapping(value = "/rule/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage create(@RequestBody DtoPricingRule dto) {
		LOGGER.info("Saving Pricing Rule: {}", dto);
		try {
			DtoPricingRule result = ruleService.create(dto);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Saved", result);
		} catch (Exception e) {
			LOGGER.error("Error saving Pricing Rule", e);
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"Failed to save Pricing Rule", dto);
		}
	}

	@PostMapping(value = "/rule/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage update(@RequestBody DtoPricingRule dto) {
		LOGGER.info("Updating Pricing Rule: {}", dto);
		try {
			DtoPricingRule result = ruleService.update(dto.getSerPricingRuleId(), dto);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Updated", result);
		} catch (Exception e) {
			LOGGER.error("Error updating Pricing Rule", e);
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"Failed to update Pricing Rule", dto);
		}
	}

	@PostMapping(value = "/rule/getByVersion", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage rulesByVersion(@RequestBody Long id) {
		LOGGER.info("Fetching Pricing Rules by Version ID: ", id);
		try {
			List<DtoPricingRule> list = ruleService.listByVersion(id);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Fetched", list);
		} catch (Exception e) {
			LOGGER.error("Error fetching Pricing Rules", e);
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"Failed to fetch Pricing Rules", null);
		}
	}

	// ============================
	// PRICING PREVIEW ENGINE
	// ============================

	@PostMapping(value = "/preview", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage preview(@RequestBody DtoPricePreviewRequest request) {
		LOGGER.info("Pricing Preview Request: {}", request);
		try {
			DtoPricePreviewRequest resp = engine.preview(request);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Preview Calculated Successfully", resp);
		} catch (Exception e) {
			LOGGER.error("Error generating preview", e);
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"Failed to calculate preview", null);
		}
	}

}
