package com.zbs.de.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.zbs.de.model.dto.DtoPriceCalculationRequest;
import com.zbs.de.model.dto.DtoPriceCalculationResult;
import com.zbs.de.service.ServiceMenuItemPrice;
import com.zbs.de.util.ResponseMessage;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/price-calculator")
public class ControllerPriceCalculator {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerPriceCalculator.class);

	@Autowired
	private ServiceMenuItemPrice service;

	// -------------------------------------------------------------
	// CALCULATE PRICE
	// -------------------------------------------------------------
	@PostMapping(value = "/calculate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage calculate(@RequestBody DtoPriceCalculationRequest request, HttpServletRequest httpRequest) {
		try {
			LOGGER.info("Calculating price for {} menu item(s)",
					request.getMenuItemIds() != null ? request.getMenuItemIds().size() : 0);

			DtoPriceCalculationResult result = service.calculatePrice(request);

			if (result.getErrors() != null && !result.getErrors().isEmpty()) {
				return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
						"Calculation completed with errors", result);
			}

			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Price calculation completed successfully",
					result);

		} catch (Exception e) {
			LOGGER.error("Error calculating price", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to calculate price: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// QUOTE (Simple calculation)
	// -------------------------------------------------------------
	@PostMapping(value = "/quote", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage quote(@RequestBody DtoPriceCalculationRequest request, HttpServletRequest httpRequest) {
		try {
			LOGGER.info("Generating quote for {} menu item(s)",
					request.getMenuItemIds() != null ? request.getMenuItemIds().size() : 0);

			// Simple quote - just calculate total
			DtoPriceCalculationResult result = service.calculatePrice(request);

			// Create simplified response
			if (result.getErrors() != null && !result.getErrors().isEmpty()) {
				return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
						"Cannot generate quote: " + String.join(", ", result.getErrors()), null);
			}

			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Quote generated successfully",
					result.getTotal());

		} catch (Exception e) {
			LOGGER.error("Error generating quote", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to generate quote: " + e.getMessage(), null);
		}
	}
}