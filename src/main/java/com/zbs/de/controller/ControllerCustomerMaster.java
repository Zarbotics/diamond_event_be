package com.zbs.de.controller;

import java.util.ArrayList;
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

import com.zbs.de.config.security.AccessGuard;
import com.zbs.de.config.security.CurrentUser;
import com.zbs.de.util.ResponseMessage;
import com.zbs.de.util.UtilRandomKey;
import com.zbs.de.model.dto.DtoCustomerMasterDropDown;
import com.zbs.de.model.dto.DtoCustomerMaster;
import com.zbs.de.model.dto.DtoDashboardCustomer;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.service.ServiceCustomerMaster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ControllerCustomerMaintenance.
 */
@RestController
@RequestMapping("/customerMaster")
@CrossOrigin(origins = "")
public class ControllerCustomerMaster {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerCustomerMaster.class);

	/** The service main acct. */
	@Autowired
	ServiceCustomerMaster serviceCustomerMaster;

	@Autowired
	AccessGuard accessGuard;

	@Autowired
	CurrentUser currentUser;

	/**
	 * Gets the all data.
	 *
	 * @param request the request
	 * @return the all data
	 * @throws Exception the exception
	 */
	@RequestMapping(value = "/getAllData", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=application/json")
	public ResponseMessage getAllData(HttpServletRequest request) throws Exception {
		LOGGER.info("Search ItemClassAccountSetup Method");
		ResponseMessage responseMessage = null;
		List<DtoCustomerMaster> dtoSearch = new ArrayList<>();
		dtoSearch = this.serviceCustomerMaster.getAllData();
		if (dtoSearch != null) {
			responseMessage = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "GET ALL CUSTOMERS", dtoSearch);
		} else {
			responseMessage = new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
					"UNABLE TO GET ALL CUSTOMERS", dtoSearch);
		}

		LOGGER.debug("Search ItemClassAccountSetup Method:" + dtoSearch);
		return responseMessage;
	}
	
	
	@RequestMapping(value = "/getAllActiveDropDown", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=application/json")
	public ResponseMessage getAllActiveDropDown(HttpServletRequest request) throws Exception {
		LOGGER.info("Search ItemClassAccountSetup Method");
		ResponseMessage responseMessage = null;
		List<DtoCustomerMasterDropDown> dtoSearch = new ArrayList<>();
		dtoSearch = this.serviceCustomerMaster.getAllActiveDropDown();
		if (dtoSearch != null) {
			responseMessage = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "GET ALL CUSTOMERS", dtoSearch);
		} else {
			responseMessage = new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
					"UNABLE TO GET ALL CUSTOMERS", dtoSearch);
		}

		LOGGER.debug("Search ItemClassAccountSetup Method:" + dtoSearch);
		return responseMessage;
	}

	@RequestMapping(value = "/saveOrUpdate", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=application/json")
	public ResponseMessage saveOrUpdate(@RequestBody DtoCustomerMaster dtoCustomerMaster, HttpServletRequest request)
			throws Exception {
		LOGGER.info("Save or update customer");

		if (!currentUser.isAdmin()) {
			// Editing an existing record requires owning it.
			if (dtoCustomerMaster.getSerCustId() != null) {
				accessGuard.assertCanAccessCustomer(dtoCustomerMaster.getSerCustId());
			}
			// The email is the only link between a login and a customer record, so a
			// customer must not be able to set it to somebody else's — doing so would
			// hand them ownership of that person's events.
			currentUser.email().ifPresent(dtoCustomerMaster::setTxtEmail);
		}

		ResponseMessage responseMessage = null;
		responseMessage = this.serviceCustomerMaster.saveAndUpdate(dtoCustomerMaster);
		if (responseMessage != null) {
			responseMessage = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK,
					"Successfully Saved The Customer Masters", responseMessage.getResult());
		} else {
			responseMessage = new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
					"Unable To Save And Update", dtoCustomerMaster);
		}

		LOGGER.debug("Save Or Update  CustomerMaster Method:" + dtoCustomerMaster);
		return responseMessage;
	}

	@RequestMapping(value = "/getById", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=application/json")
	public ResponseMessage getById(@RequestBody DtoSearch dtoSearch) {
		LOGGER.info("Fetching customer by ID" + dtoSearch);
		ResponseMessage responseMessage = new ResponseMessage();
		try {
			ResponseMessage res = serviceCustomerMaster.getById(dtoSearch.getId());
			responseMessage = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched successfully",
					res.getResult());
		} catch (Exception e) {
			LOGGER.error("Error fetching customer", e);
			responseMessage = new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
					"Customer not found", null);
		}
		LOGGER.info("Fetching customer by ID" + dtoSearch);
		return responseMessage;
	}

	@RequestMapping(value = "/getByEmail", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=application/json")
	public ResponseMessage getByEmail(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		LOGGER.info("Fetching customer by email");
		ResponseMessage responseMessage = new ResponseMessage();

		// A customer may only look themselves up. Without this, the endpoint is an
		// email-address oracle over the whole customer base: pass any address, get
		// back that person's name, phone number and home address.
		String requestedEmail = dtoSearch.getSearchKeyword();
		if (!currentUser.isAdmin()) {
			String ownEmail = currentUser.email()
					.orElseThrow(() -> new org.springframework.security.access.AccessDeniedException(
							"No account is linked to this session."));
			if (requestedEmail != null && !ownEmail.equalsIgnoreCase(requestedEmail.trim())) {
				throw new org.springframework.security.access.AccessDeniedException(
						"You may only look up your own account.");
			}
			requestedEmail = ownEmail;
		}

		try {
			DtoResult dtoResult = serviceCustomerMaster.getByEmail(requestedEmail);
			if (dtoResult.getTxtMessage().equalsIgnoreCase("success")) {
				responseMessage = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched successfully",
						dtoResult.getResult());
			} else {
				responseMessage = new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
						dtoResult.getTxtMessage(), null);
			}

		} catch (Exception e) {
			LOGGER.error("Error fetching customer", e);
			responseMessage = new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
					"Customer not found", null);
		}
		LOGGER.info("Fetching customer by ID" + dtoSearch);
		return responseMessage;
	}

	@RequestMapping(value = "/getDashboardStats", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Accept=application/json")
	public ResponseMessage getDashboardStats(HttpServletRequest request) {
		ResponseMessage responseMessage = new ResponseMessage();

		DtoDashboardCustomer dto = serviceCustomerMaster.getDashboardStats();
		if (UtilRandomKey.isNotNull(dto)) {
			responseMessage = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched successfully", dto);
		} else {
			responseMessage = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Could Not Fetch successfully",
					null);
		}
		return responseMessage;
	}

	@PostMapping(value = "/deleteById", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage deleteById(@RequestBody DtoSearch dtoSearch) {
		LOGGER.info("Deleting CustomerMaster by ID: " + dtoSearch);
		try {
			DtoResult result = serviceCustomerMaster.deleteById(dtoSearch.getId());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(), null);
		} catch (Exception e) {
			LOGGER.error("Error Deleting CustomerMaster", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}

	}
	
	
	@PostMapping(value = "/generateCustomerCode", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage generateCustomerCode(HttpServletRequest request) {
		String txtCode = serviceCustomerMaster.generateCustomerCode();
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched Customer Code", txtCode);
	}

}
