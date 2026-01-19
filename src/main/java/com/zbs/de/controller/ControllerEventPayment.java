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
import com.zbs.de.model.dto.DtoPayment;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.service.ServiceEventPayment;
import com.zbs.de.util.ResponseMessage;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/eventPayment")
@CrossOrigin(origins = "")
public class ControllerEventPayment {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerEventPayment.class);

	@Autowired
	private ServiceEventPayment serviceEventPayment;

	/*
	 * ========================= SAVE / UPDATE (JSON) =========================
	 */
	@PostMapping(value = "/saveOrUpdate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage saveOrUpdate(@RequestBody DtoPayment dtoPayment, HttpServletRequest request) {

		LOGGER.info("Saving Event Payment: {}", dtoPayment);
		DtoResult result = serviceEventPayment.savePayment(dtoPayment);

		if (result != null && "success".equalsIgnoreCase(result.getTxtMessage())) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully saved", result.getResult());
		}

		return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to save payment",
				dtoPayment);
	}

	/*
	 * ========================= SAVE WITH FILES (MULTIPART)
	 * =========================
	 */
	@PostMapping(value = "/saveOrUpdate/WithDocs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseMessage saveWithDocs(@RequestPart("payment") String payment,
			@RequestPart(value = "files", required = false) List<MultipartFile> files) throws IOException {

		LOGGER.info("Saving Event Payment with documents: {}", payment);

		DtoPayment dtoPayment = new ObjectMapper().readValue(payment, DtoPayment.class);

		DtoResult result = serviceEventPayment.savePaymentWithFiles(dtoPayment, files);

		if (result != null && !"Failure".equalsIgnoreCase(result.getTxtMessage())) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		}

		return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to save payment",
				dtoPayment);
	}

	/*
	 * ========================= GET PAYMENTS BY BUDGET ID =========================
	 */
	@PostMapping(value = "/getByBudgetId", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getByBudgetId(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {

		LOGGER.info("Fetching Payments by Budget ID: {}", dtoSearch.getId());
		try {
			List<DtoPayment> result = serviceEventPayment.getPaymentsByBudgetId(dtoSearch.getId());

			if (result != null) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Fetched", result);
			} else {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Data Not Found", null);
			}
		} catch (Exception e) {
			LOGGER.error("Error Fetching Event Payments", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}

	}

	/*
	 * ========================= GET PAYMENTS BY EventMaster ID
	 * =========================
	 */
	@PostMapping(value = "/getByEventMasterId", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getByEventMasterId(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {

		LOGGER.info("Fetching Payments by Budget ID: {}", dtoSearch.getId());
		try {
			List<DtoPayment> result = serviceEventPayment.getPaymentsByEventMasterId(dtoSearch.getId());

			if (result != null) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Fetched", result);
			} else {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Data Not Found", null);
			}
		} catch (Exception e) {
			LOGGER.error("Error Fetching Event Payments", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}

	}

	/*
	 * ========================= DELETE BY ID =========================
	 */
	@PostMapping(value = "/deleteById", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage deleteById(@RequestBody DtoSearch dtoSearch) {

		LOGGER.info("Deleting Event Payment by ID: {}", dtoSearch.getId());

		try {
			DtoResult result = serviceEventPayment.deletePayment(dtoSearch.getId());

			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(), null);
		} catch (Exception e) {
			LOGGER.error("Error deleting Event Payment", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// Get All Payment Methods
	// -------------------------------------------------------------
	@PostMapping(value = "/getAllPaymentMethods", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllPaymentMethods(HttpServletRequest request) {
		try {
			LOGGER.info("getAllPaymentMethods");
			List<String> methods = serviceEventPayment.getAllPaymentMethods();
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched Successfully", methods);
		} catch (Exception e) {
			LOGGER.error("Error generating assignment code", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// Get All Payment Status
	// -------------------------------------------------------------
	@PostMapping(value = "/getAllPaymentStatuses", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllPaymentStatuses(HttpServletRequest request) {
		try {
			LOGGER.info("getAllPaymentStatuses");
			List<String> methods = serviceEventPayment.getAllPaymentStatuses();
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched Successfully", methods);
		} catch (Exception e) {
			LOGGER.error("Error generating assignment code", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch: " + e.getMessage(), null);
		}
	}
}
