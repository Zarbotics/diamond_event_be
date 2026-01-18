package com.zbs.de.controller;

import java.io.IOException;
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
import com.zbs.de.util.UtilRandomKey;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/eventMaster")
@CrossOrigin(origins = "")
public class ControllerEventMaster {

	@Autowired
	ServiceEventMaster serviceEventMaster;

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerEventType.class);

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
		LOGGER.info("Saving Event Master: {}", eventMaster);
		DtoEventMaster dtoEventMaster = new ObjectMapper().readValue(eventMaster, DtoEventMaster.class);
		DtoResult result = serviceEventMaster.saveAndUpdateWithDocs(dtoEventMaster, files);
		if (result != null && !result.getTxtMessage().equalsIgnoreCase("Failure")) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		}
		return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to save",
				dtoEventMaster);
	}

	@PostMapping(value = "/generateEventCode", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage generateEventCode(HttpServletRequest request) {
		String txtCode = serviceEventMaster.generateNextEventMasterCode();
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched Event Types", txtCode);
	}

	@PostMapping(value = "/getByEventIdAndCustomerId", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getByEventIdAndCustomerId(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		LOGGER.info("Searching Event Master: {}", dtoSearch);
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
			LOGGER.info("Saving Event Master: {}", eventMaster);
			dtoEventMaster = new ObjectMapper().readValue(eventMaster, DtoEventMasterAdminPortal.class);
			DtoResult result = serviceEventMaster.saveAndUpdateWithDocsAdminPortal(dtoEventMaster, files);
			if (result != null && !result.getTxtMessage().equalsIgnoreCase("Failure")) {
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

}
