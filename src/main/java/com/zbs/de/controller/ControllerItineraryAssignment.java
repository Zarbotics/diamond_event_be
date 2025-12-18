package com.zbs.de.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zbs.de.model.dto.DtoItineraryBulkAssignmentRequest;
import com.zbs.de.model.dto.DtoItineraryAssignment;
import com.zbs.de.model.dto.DtoRemoveItineraryItemsRequest;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.service.ServiceItineraryAssignment;
import com.zbs.de.util.ResponseMessage;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/itinerary/assignment")
public class ControllerItineraryAssignment {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerItineraryAssignment.class);

	@Autowired
	private ServiceItineraryAssignment service;

	// -------------------------------------------------------------
	// CREATE
	// -------------------------------------------------------------
	@PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage create(@RequestBody DtoItineraryAssignment dto, HttpServletRequest request) {
		try {
			LOGGER.info("Creating Itinerary Assignment");
			DtoResult result = service.create(dto);
			if (result.getResult() != null) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
						result.getResult());
			}
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, result.getTxtMessage(),
					null);
		} catch (Exception e) {
			LOGGER.error("Error creating Itinerary Assignment", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to create Itinerary Assignment: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// UPDATE
	// -------------------------------------------------------------
	@PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage update(@RequestBody DtoItineraryAssignment dto, HttpServletRequest request) {
		try {
			LOGGER.info("Updating Itinerary Assignment ID: {}", dto.getSerItineraryAssignmentId());
			DtoResult result = service.update(dto);
			if (result.getResult() != null) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
						result.getResult());
			}
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, result.getTxtMessage(),
					null);
		} catch (Exception e) {
			LOGGER.error("Error updating Itinerary Assignment", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to update Itinerary Assignment: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GET BY ID
	// -------------------------------------------------------------
	@PostMapping(value = "/getById", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getById(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Fetching Itinerary Assignment by ID: {}", dtoSearch.getIdL());
			DtoResult result = service.getById(dtoSearch.getIdL());
			if (result.getResult() != null) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
						result.getResult());
			}
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, result.getTxtMessage(),
					null);
		} catch (Exception e) {
			LOGGER.error("Error fetching Itinerary Assignment by ID", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch Itinerary Assignment: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GET ALL
	// -------------------------------------------------------------
	@PostMapping(value = "/getAll", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAll(HttpServletRequest request) {
		try {
			LOGGER.info("Fetching all Itinerary Assignments");
			DtoResult result = service.getAll();
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error fetching all Itinerary Assignments", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch Itinerary Assignments: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GET ALL ACTIVE
	// -------------------------------------------------------------
	@PostMapping(value = "/getAllActive", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllActive(HttpServletRequest request) {
		try {
			LOGGER.info("Fetching all active Itinerary Assignments");
			DtoResult result = service.getAllActive();
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error fetching active Itinerary Assignments", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch active Itinerary Assignments: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GET BY MENU ITEM ID
	// -------------------------------------------------------------
	@PostMapping(value = "/getByMenuItemId", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getByMenuItemId(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Fetching Itinerary Assignments by Menu Item ID: {}", dtoSearch.getIdL());
			DtoResult result = service.getByMenuItemId(dtoSearch.getIdL());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error fetching Itinerary Assignments by Menu Item ID", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch Itinerary Assignments: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GET BY MENU ITEM IDs (Multiple)
	// -------------------------------------------------------------
	@PostMapping(value = "/getByMenuItemIds", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getByMenuItemIds(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Fetching Itinerary Assignments by Menu Item IDs");
			List<Long> menuItemIds = dtoSearch.getListIdLs();
			DtoResult result = service.getByMenuItemIds(menuItemIds);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error fetching Itinerary Assignments by Menu Item IDs", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch Itinerary Assignments: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// BULK ASSIGN ITINERARY ITEMS TO MULTIPLE MENU ITEMS
	// -------------------------------------------------------------
	@PostMapping(value = "/bulkAssign", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage bulkAssign(@RequestBody DtoItineraryBulkAssignmentRequest request,
			HttpServletRequest httpRequest) {
		try {
			LOGGER.info("Bulk assigning itinerary items to multiple menu items");
			DtoResult result = service.assignListOfItineraryItemsToMultipleItems(request);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error in bulk assignment", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to perform bulk assignment: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// REMOVE ITINERARY ITEM FROM ASSIGNMENT
	// -------------------------------------------------------------
	@PostMapping(value = "/removeItems", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage removeItems(@RequestBody DtoRemoveItineraryItemsRequest request,
			HttpServletRequest httpRequest) {
		try {
			LOGGER.info("Removing itinerary items from assignment ID: {}", request.getSerItineraryAssignmentId());
			DtoResult result = service.removeItineraryItemFromAssignment(request);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error removing itinerary items", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to remove itinerary items: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// REMOVE ALL ITINERARY ITEMS FROM ASSIGNMENT
	// -------------------------------------------------------------
	@PostMapping(value = "/removeAllItemsByAssignmentId", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage removeAllItems(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Removing all itinerary items from assignment ID: {}", dtoSearch.getIdL());
			DtoResult result = service.removeAllItineraryItemsFromAssignment(dtoSearch.getIdL());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error removing all itinerary items", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to remove all itinerary items: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// DELETE ASSIGNMENT
	// -------------------------------------------------------------
	@PostMapping(value = "/deleteAssignmentByItineraryAssignmentId", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage delete(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Deleting Itinerary Assignment ID: {}", dtoSearch.getIdL());
			DtoResult result = service.delete(dtoSearch.getIdL());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error deleting Itinerary Assignment", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to delete Itinerary Assignment: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// DEACTIVATE ASSIGNMENT
	// -------------------------------------------------------------
	@PostMapping(value = "/deactivate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage deactivate(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Deactivating Itinerary Assignment ID: {}", dtoSearch.getIdL());
			DtoResult result = service.deactivate(dtoSearch.getIdL());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error deactivating Itinerary Assignment", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to deactivate Itinerary Assignment: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// ACTIVATE ASSIGNMENT
	// -------------------------------------------------------------
	@PostMapping(value = "/activate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage activate(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Activating Itinerary Assignment ID: {}", dtoSearch.getIdL());
			DtoResult result = service.activate(dtoSearch.getIdL());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error activating Itinerary Assignment", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to activate Itinerary Assignment: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// APPROVE ASSIGNMENT
	// -------------------------------------------------------------
	@PostMapping(value = "/approve", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage approve(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Approving Itinerary Assignment ID: {}", dtoSearch.getIdL());
			DtoResult result = service.approve(dtoSearch.getIdL());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error approving Itinerary Assignment", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to approve Itinerary Assignment: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GET ASSIGNMENT ENTITY BY ID (Returns Entity, not DTO)
	// -------------------------------------------------------------
	@PostMapping(value = "/getEntityById", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getEntityById(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Fetching Itinerary Assignment Entity by ID: {}", dtoSearch.getIdL());
			com.zbs.de.model.ItineraryAssignment entity = service.getAssignmentEntityById(dtoSearch.getIdL());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched successfully", entity);
		} catch (Exception e) {
			LOGGER.error("Error fetching Itinerary Assignment Entity by ID", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch Itinerary Assignment Entity: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GENERATE ASSIGNMENT CODE
	// -------------------------------------------------------------
	@PostMapping(value = "/generateCode", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage generateCode(HttpServletRequest request) {
		try {
			LOGGER.info("Generating assignment code");
			String code = service.generateAssignmentCode();
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Code generated successfully", code);
		} catch (Exception e) {
			LOGGER.error("Error generating assignment code", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to generate assignment code: " + e.getMessage(), null);
		}
	}
}