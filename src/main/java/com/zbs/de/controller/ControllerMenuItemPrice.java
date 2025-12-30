package com.zbs.de.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.zbs.de.model.dto.*;
import com.zbs.de.service.ServiceMenuItemPrice;
import com.zbs.de.util.ResponseMessage;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/menu-item-price")
public class ControllerMenuItemPrice {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerMenuItemPrice.class);

	@Autowired
	private ServiceMenuItemPrice service;

	// -------------------------------------------------------------
	// CREATE
	// -------------------------------------------------------------
	@PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage create(@RequestBody DtoMenuItemPrice dto, HttpServletRequest request) {
		try {
			LOGGER.info("Creating Menu Item Price for menu item: {}", dto.getSerMenuItemId());
			DtoResult result = service.create(dto);
			if (result.getResult() != null) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
						result.getResult());
			}
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, result.getTxtMessage(),
					null);
		} catch (Exception e) {
			LOGGER.error("Error creating Menu Item Price", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to create Menu Item Price: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// UPDATE
	// -------------------------------------------------------------
	@PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage update(@RequestBody DtoMenuItemPrice dto, HttpServletRequest request) {
		try {
			LOGGER.info("Updating Menu Item Price ID: {}", dto.getSerPriceId());
			DtoResult result = service.update(dto);
			if (result.getResult() != null) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
						result.getResult());
			}
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, result.getTxtMessage(),
					null);
		} catch (Exception e) {
			LOGGER.error("Error updating Menu Item Price", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to update Menu Item Price: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// DELETE
	// -------------------------------------------------------------
	@PostMapping(value = "/delete", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage delete(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Deleting Menu Item Price ID: {}", dtoSearch.getIdL());
			DtoResult result = service.delete(dtoSearch.getIdL());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error deleting Menu Item Price", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to delete Menu Item Price: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GET BY ID
	// -------------------------------------------------------------
	@PostMapping(value = "/getById", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getById(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Fetching Menu Item Price by ID: {}", dtoSearch.getIdL());
			DtoResult result = service.getById(dtoSearch.getIdL());
			if (result.getResult() != null) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
						result.getResult());
			}
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, result.getTxtMessage(),
					null);
		} catch (Exception e) {
			LOGGER.error("Error fetching Menu Item Price by ID", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch Menu Item Price: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GET ALL
	// -------------------------------------------------------------
	@PostMapping(value = "/getAll", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAll(HttpServletRequest request) {
		try {
			LOGGER.info("Fetching all Menu Item Prices");
			DtoResult result = service.getAll();
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error fetching all Menu Item Prices", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch Menu Item Prices: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GET ALL ACTIVE
	// -------------------------------------------------------------
	@PostMapping(value = "/getAllActive", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllActive(HttpServletRequest request) {
		try {
			LOGGER.info("Fetching all active Menu Item Prices");
			DtoResult result = service.getAllActive();
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error fetching active Menu Item Prices", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch active Menu Item Prices: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GET BY MENU ITEM
	// -------------------------------------------------------------
	@PostMapping(value = "/getByMenuItem", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getByMenuItem(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Fetching prices for Menu Item ID: {}", dtoSearch.getIdL());
			DtoResult result = service.getByMenuItem(dtoSearch.getIdL());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error fetching prices by menu item", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch prices: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GET BY PRICE VERSION
	// -------------------------------------------------------------
	@PostMapping(value = "/getByPriceVersion", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getByPriceVersion(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Fetching prices for Price Version ID: {}", dtoSearch.getIdL());
			DtoResult result = service.getByPriceVersion(dtoSearch.getIdL());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error fetching prices by price version", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch prices: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GET BY MENU ITEM AND VERSION
	// -------------------------------------------------------------
	@PostMapping(value = "/getByMenuItemAndVersion", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getByMenuItemAndVersion(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Fetching price for Menu Item ID: {} and Version ID: {}", dtoSearch.getIdL(),
					dtoSearch.getIdL2());
			DtoResult result = service.getByMenuItemAndVersion(dtoSearch.getIdL(), dtoSearch.getIdL2());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error fetching price by menu item and version", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch price: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GET DEFAULT BY MENU ITEM
	// -------------------------------------------------------------
	@PostMapping(value = "/getDefaultByMenuItem", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getDefaultByMenuItem(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Fetching default price for Menu Item ID: {}", dtoSearch.getIdL());
			DtoResult result = service.getDefaultByMenuItem(dtoSearch.getIdL());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error fetching default price by menu item", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch default price: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GET APPLICABLE PRICE
	// -------------------------------------------------------------
	@PostMapping(value = "/getApplicablePrice", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getApplicablePrice(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Fetching applicable price for Menu Item ID: {}", dtoSearch.getIdL());
			DtoResult result = service.getApplicablePrice(dtoSearch.getIdL(), dtoSearch.getId(), dtoSearch.getDate(),
					dtoSearch.getIdL2());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error fetching applicable price", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch applicable price: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// BULK SET PRICES
	// -------------------------------------------------------------
	@PostMapping(value = "/bulkSet", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage bulkSet(@RequestBody DtoBulkPriceRequest request, HttpServletRequest httpRequest) {
		try {
			LOGGER.info("Bulk setting prices for {} menu items",
					request.getMenuItemIds() != null ? request.getMenuItemIds().size() : 0);
			DtoResult result = service.bulkSetPrices(request);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error in bulk price setting", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to bulk set prices: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// COPY PRICES
	// -------------------------------------------------------------
	@PostMapping(value = "/copy", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage copyPrices(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Copying prices from Version ID: {} to Version ID: {}", dtoSearch.getIdL(),
					dtoSearch.getIdL2());
			DtoResult result = service.copyPrices(dtoSearch.getIdL(), dtoSearch.getIdL2());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error copying prices", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to copy prices: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// UPDATE PRICES STATUS
	// -------------------------------------------------------------
	@PostMapping(value = "/updateStatus", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage updateStatus(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Updating status for {} price(s)",
					dtoSearch.getListIdLs() != null ? dtoSearch.getListIdLs().size() : 0);
			DtoResult result = service.updatePricesStatus(dtoSearch.getListIdLs(), dtoSearch.isBlnFlag());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error updating prices status", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to update prices status: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// CALCULATE PRICE FOR MENU ITEM
	// -------------------------------------------------------------
	@PostMapping(value = "/calculateForMenuItem", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage calculateForMenuItem(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Calculating price for Menu Item ID: {}", dtoSearch.getIdL());
			DtoResult result = service.calculatePriceForMenuItem(dtoSearch.getIdL(), dtoSearch.getId(),
					dtoSearch.getId1(), dtoSearch.getDate());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error calculating price for menu item", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to calculate price: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// VALIDATE PRICE
	// -------------------------------------------------------------
	@PostMapping(value = "/validate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage validate(@RequestBody DtoMenuItemPrice dto, HttpServletRequest request) {
		try {
			LOGGER.info("Validating price data");
			DtoResult result = service.validatePrice(dto);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error validating price", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to validate price: " + e.getMessage(), null);
		}
	}
}