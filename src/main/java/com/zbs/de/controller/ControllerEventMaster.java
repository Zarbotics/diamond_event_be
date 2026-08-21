package com.zbs.de.controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
import com.zbs.de.config.security.AccessGuard;
import com.zbs.de.model.EventMaster;
import com.zbs.de.model.dto.DtoEventMaster;
import com.zbs.de.model.dto.DtoEventMasterAdminPortal;
import com.zbs.de.model.dto.DtoEventMasterSearch;
import com.zbs.de.model.dto.DtoEventMasterStats;
import com.zbs.de.model.dto.DtoEventMasterTableView;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.service.ServiceEventMaster;
import com.zbs.de.util.ResponseMessage;
import com.zbs.de.util.UtilDateAndTime;
import com.zbs.de.util.UtilRandomKey;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/eventMaster")
@CrossOrigin(origins = "")
public class ControllerEventMaster {

	@Autowired
	ServiceEventMaster serviceEventMaster;

	@Autowired
	AccessGuard accessGuard;

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerEventMaster.class);

	/**
	 * Upcoming days holding more events than the capacity rule allows.
	 *
	 * <p>
	 * Administrator-only, by being absent from the customer allowlist. These days
	 * exist because the rule was not applied on every save path until recently,
	 * and the bookings that resulted are commitments to real customers — they are
	 * grandfathered rather than corrected. The team cannot act on a day they
	 * cannot see, and staffing one is the action that matters.
	 */
	@PostMapping(value = "/daysOverCapacity", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage daysOverCapacity() {
		DtoResult result = serviceEventMaster.getDaysOverCapacity();

		if (!"Success".equalsIgnoreCase(result.getTxtMessage())) {
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
					HttpStatus.INTERNAL_SERVER_ERROR, "Could not work out which days are over capacity",
					null);
		}
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK,
				"Days over capacity", result.getResult());
	}

	/**
	 * Every event, in the shape the admin calendar draws.
	 *
	 * <p>
	 * Administrator-only, by being absent from the customer allowlist. The
	 * calendar used to call {@code getAllDataAdminPortal}, which answers with the
	 * whole of every event that has ever existed — 624 KB across 296 events on a
	 * development database — to draw boxes carrying a reference and a name.
	 */
	@PostMapping(value = "/calendarEntries", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage calendarEntries() {
		DtoResult result = serviceEventMaster.getCalendarEntries();

		if (!"Success".equalsIgnoreCase(result.getTxtMessage())) {
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					result.getTxtMessage(), null);
		}
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Fetched",
				result.getResulList());
	}

	@PostMapping(value = "/saveOrUpdate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage saveOrUpdate(@RequestBody DtoEventMaster dtoEventMaster, HttpServletRequest request) {
		LOGGER.info("Saving Event Master: {}", dtoEventMaster);
		DtoResult result = serviceEventMaster.saveAndUpdate(dtoEventMaster);
		if (result.getResult() != null && result.getTxtMessage().equalsIgnoreCase("success")) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully saved", result.getResult());
		}
		return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to save",
				dtoEventMaster);
	}

	@PostMapping(value = "/saveWithDocs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseMessage saveWithDocs(@RequestPart("eventMaster") String eventMaster,
			@RequestPart(value = "files", required = false) List<MultipartFile> files) throws IOException {
		DtoEventMaster dtoEventMaster = new ObjectMapper().readValue(eventMaster, DtoEventMaster.class);
		/*
		 * The booking's identity, not its contents. This logged the whole
		 * request body — around eight kilobytes per save, twelve saves per
		 * booking, carrying the customer's name, their contact's name and
		 * phone number, the couple's names and the full menu. Personal data
		 * in an application log is processing nobody has accounted for: logs
		 * are shipped to aggregators, copied onto laptops and kept
		 * indefinitely, none of it covered by the retention that applies to
		 * the database. The id and the reference are what you actually need
		 * to correlate a log line with a booking.
		 */
		LOGGER.info("Saving event {} ({})", dtoEventMaster.getSerEventMasterId(),
				dtoEventMaster.getTxtEventMasterCode());

		// Ownership is asserted before the try/catch below: an AccessDeniedException
		// must reach the exception handler as a 403, not be flattened into a 400 by
		// the catch-all.
		accessGuard.assertCanAccessCustomer(dtoEventMaster.getSerCustId());
		if (dtoEventMaster.getSerEventMasterId() != null) {
			accessGuard.assertCanAccessEvent(dtoEventMaster.getSerEventMasterId());
		}

		try {
			DtoResult result = serviceEventMaster.saveAndUpdateWithDocs(dtoEventMaster, files);
			if (result != null && "already_booked".equalsIgnoreCase(result.getTxtMessage())) {
				return new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED,
						result.getResult().toString(), result.getResult());
			} else if (result != null && !result.getTxtMessage().equalsIgnoreCase("Failure")) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
						result.getResult());
			} else {
				return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
						result.getTxtMessage(), dtoEventMaster);
			}
		} catch (Exception e) {
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to save",
					dtoEventMaster);
		}

	}

	
	@PostMapping(value = "/saveWithDocsCE", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseMessage saveWithDocsCE(@RequestPart("eventMaster") String eventMaster,
			@RequestPart(value = "files", required = false) List<MultipartFile> files) throws IOException {
		DtoEventMaster dtoEventMaster = new ObjectMapper().readValue(eventMaster, DtoEventMaster.class);
		LOGGER.info("Saving event {} ({})", dtoEventMaster.getSerEventMasterId(),
				dtoEventMaster.getTxtEventMasterCode());
		try {
			DtoResult result = serviceEventMaster.saveAndUpdateWithDocsCE(dtoEventMaster, files);
			if (result != null && "already_booked".equalsIgnoreCase(result.getTxtMessage())) {
				return new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED,
						result.getResult().toString(), result.getResult());
			} else if (result != null && !result.getTxtMessage().equalsIgnoreCase("Failure")) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
						result.getResult());
			} else {
				return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
						result.getTxtMessage(), dtoEventMaster);
			}
		} catch (Exception e) {
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to save",
					dtoEventMaster);
		}

	}
	
	@PostMapping(value = "/generateEventCode", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage generateEventCode(HttpServletRequest request) {
		String txtCode = serviceEventMaster.generateNextEventMasterCode();
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched Event Types", txtCode);
	}

	@PostMapping(value = "/getByEventIdAndCustomerId", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getByEventIdAndCustomerId(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		LOGGER.info("Searching Event Master: {}", dtoSearch);
		// id1 carries the customer id for this endpoint; id carries the event type.
		dtoSearch.setId1(accessGuard.resolveCustomerId(dtoSearch.getId1()));
		DtoResult result = serviceEventMaster.getByEventTypeIdAndCustId(dtoSearch);
		if (result.getResult() != null && result.getTxtMessage().equalsIgnoreCase("success")) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Fetched",
					result.getResult());
		}
		return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to Fetch",
				dtoSearch);
	}

	@PostMapping(value = "/getByCustomerId", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getByCustomerId(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		LOGGER.info("Searching Event Master: {}", dtoSearch);
		// A customer id supplied by the client is never trusted. For a customer it is
		// replaced with their own; for staff it is passed through unchanged.
		dtoSearch.setId(accessGuard.resolveCustomerId(dtoSearch.getId()));
		DtoResult result = serviceEventMaster.getByCustId(dtoSearch);
		if (result.getResulList() != null && result.getTxtMessage().equalsIgnoreCase("success")) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Fetched",
					result.getResulList());
		} else if (result != null) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Data Not Found", result.getResulList());
		}

		return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to Fetch",
				dtoSearch);

	}

	@PostMapping(value = "/getAllData", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllData(HttpServletRequest request) {
		LOGGER.info("Searching Event Masters");
		DtoResult result = serviceEventMaster.getAllEvents();
		if (result.getResulList() != null && result.getTxtMessage().equalsIgnoreCase("success")) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Fetched",
					result.getResulList());
		}
		return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
				result.getTxtMessage(), null);
	}

	@PostMapping(value = "/getAllTableView", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllTableView(HttpServletRequest request) {
		LOGGER.info("Searching Event Masters");
		DtoResult result = serviceEventMaster.getAllEventsTableView();
		if (result.getResulList() != null && result.getTxtMessage().equalsIgnoreCase("success")) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Fetched",
					result.getResulList());
		}
		return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
				result.getTxtMessage(), null);
	}

	@PostMapping(value = "/getEventStats", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getEventStats(HttpServletRequest request) {
		LOGGER.info("Saving Event Master");
		List<DtoEventMasterStats> result = serviceEventMaster.getEventTypeStats();
		if (UtilRandomKey.isNotNull(result)) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully saved", result);
		}
		return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to Calculate", null);
	}

	@PostMapping(value = "/deleteById", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage deleteById(@RequestBody DtoSearch dtoSearch) {
		LOGGER.info("Deleting EventMaster by ID: " + dtoSearch);
		try {
			DtoResult result = serviceEventMaster.deleteById(dtoSearch.getId());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(), null);
		} catch (Exception e) {
			LOGGER.error("Error Deleting EventMaster", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}

	}

	@PostMapping(value = "/saveWithDocsAdminPortal", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseMessage saveWithDocsAdminPortal(@RequestPart("eventMaster") String eventMaster,
			@RequestPart(value = "files", required = false) List<MultipartFile> files) {
		DtoEventMasterAdminPortal dtoEventMaster = null;
		try {
			dtoEventMaster = new ObjectMapper().readValue(eventMaster, DtoEventMasterAdminPortal.class);
			LOGGER.info("Saving event {} ({}) from the admin portal",
					dtoEventMaster.getSerEventMasterId(), dtoEventMaster.getTxtEventMasterCode());
			DtoResult result = serviceEventMaster.saveAndUpdateWithDocsAdminPortal(dtoEventMaster, files);
			if (result != null && "already_booked".equalsIgnoreCase(result.getTxtMessage())) {
				return new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED,
						result.getResult().toString(),
						result.getResult());
			} else if (result != null && !result.getTxtMessage().equalsIgnoreCase("Failure")) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
						result.getResult());
			} else {
				return new ResponseMessage(HttpStatus.METHOD_FAILURE.value(), HttpStatus.METHOD_FAILURE,
						result.getTxtMessage(), dtoEventMaster);
			}
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to save",
					dtoEventMaster);
		}

	}

	@PostMapping(value = "/getAllDataAdminPortal", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllDataAdminPortal(HttpServletRequest request) {
		LOGGER.info("Searching Event Masters");
		DtoResult result = serviceEventMaster.getAllEventsAdminPortal();
		if (result.getResulList() != null && result.getTxtMessage().equalsIgnoreCase("success")) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Fetched",
					result.getResulList());
		}
		return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
				result.getTxtMessage(), null);
	}

	@PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage search(@RequestBody DtoEventMasterSearch dtoEventMaster, HttpServletRequest request) {
		LOGGER.info("Searching Event Masters with filters: {}", dtoEventMaster);
		try {
			// call service (expects Page<DtoEventMasterTableView>)
			Page<DtoEventMasterTableView> page = serviceEventMaster.search(dtoEventMaster);

			// If service returns null, treat as empty page (safer for clients)
			if (page == null) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "No records found", Page.empty());
			}

			if (page.hasContent()) {
				// return the full Page so front-end can access content + paging metadata
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Fetched", page);
			} else {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "No records found", page);
			}
		} catch (Exception e) {
			LOGGER.error("Error while searching Event Masters", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Error while searching events: " + e.getMessage(), null);
		}
	}

	@PostMapping(value = "/searchByBudgetStatus", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage searchByBudgetStatus(@RequestBody DtoEventMasterSearch dtoEventMaster) {
	    LOGGER.info("Searching Event Masters by Budget Status: {}", dtoEventMaster.getTxtBudgetStatus());
	    try {
	        Page<DtoEventMasterTableView> page = serviceEventMaster.searchByBudgetStatus(
	                dtoEventMaster.getTxtBudgetStatus(),
	                dtoEventMaster.getPage() != null ? dtoEventMaster.getPage() : 0,
	                dtoEventMaster.getSize() != null ? dtoEventMaster.getSize() : 20
	        );
	        return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Fetched", page);
	    } catch (Exception e) {
	        LOGGER.error("Error searching Event Masters by budget status", e);
	        return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
	                "Error while searching: " + e.getMessage(), null);
	    }
	}

	
	@PostMapping(value = "/searchEntity", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage searchEntity(@RequestBody DtoEventMasterSearch dtoEventMaster, HttpServletRequest request) {
		LOGGER.info("Searching Event Masters with filters: {}", dtoEventMaster);
		try {
			// call service (expects Page<DtoEventMasterTableView>)
			Page<EventMaster> page = serviceEventMaster.searchEntity(dtoEventMaster);

			// If service returns null, treat as empty page (safer for clients)
			if (page == null) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "No records found", Page.empty());
			}

			if (page.hasContent()) {
				// return the full Page so front-end can access content + paging metadata
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Fetched", page);
			} else {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "No records found", page);
			}
		} catch (Exception e) {
			LOGGER.error("Error while searching Event Masters", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Error while searching events: " + e.getMessage(), null);
		}
	}
	
	
	@PostMapping(value = "/searchInEntityAndEventBudget", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage searchInEntityAndEventBudget(@RequestBody DtoEventMasterSearch dtoEventMaster, HttpServletRequest request) {
		LOGGER.info("Searching Event Masters with filters: {}", dtoEventMaster);
		try {
			// call service (expects Page<DtoEventMasterTableView>)
			Page<DtoEventMasterAdminPortal> page = serviceEventMaster.searchInEntityAndEventBudget(dtoEventMaster);

			// If service returns null, treat as empty page (safer for clients)
			if (page == null) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "No records found", Page.empty());
			}

			if (page.hasContent()) {
				// return the full Page so front-end can access content + paging metadata
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Fetched", page);
			} else {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "No records found", page);
			}
		} catch (Exception e) {
			LOGGER.error("Error while searching Event Masters", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Error while searching events: " + e.getMessage(), null);
		}
	}
	
	@PostMapping(value = "/getAlreadyBookedDates", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAlreadyBookedDates(HttpServletRequest request) {
		try {
			LOGGER.info("getAlreadyBookedDates");
			DtoResult result = serviceEventMaster.getAlreadyBookedDates();
			if (result != null && result.getResult() != null) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
						result.getResult());
			} else if (result != null && !result.getTxtMessage().equalsIgnoreCase("Success")) {
				return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
						result.getTxtMessage(), null);
			} else {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(), null);
			}
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Unable To Fetch Dates",
					null);
		}

	}
	
	@PostMapping(value = "/isDateAlreadyBooked", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage isDateAlreadyBooked(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("getAlreadyBookedDates");
			Date dteDate= UtilDateAndTime.ddMMyyyyDashedStringToDate(dtoSearch.getSearchKeyword());
			DtoResult result = serviceEventMaster
					.validateEventDateAvailability(dteDate);
			if (result != null && result.getResult() != null) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
						result.getResult());
			} else if (result != null && result.getTxtMessage() != null
					&& result.getTxtMessage().equalsIgnoreCase("No event is registered at this date.")) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(), null);
			} else {
				return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
						result.getTxtMessage(), null);
			}
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Unable To Validate Date",
					null);
		}

	}
	
	@PostMapping(value = "/getEventById", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getEventById(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		LOGGER.info("Searching Event Master: {}", dtoSearch);
		// Asserted outside the try/catch so a denial surfaces as 403, not 400.
		accessGuard.assertCanAccessEvent(dtoSearch.getId());
		try {
			DtoEventMaster result = serviceEventMaster.getEventById(dtoSearch.getId());
			if (result != null) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Fetched", result);
			} else {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "not found", result);
			}
		} catch (Exception e) {
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to Fetch",
					dtoSearch);
		}

	}

}
