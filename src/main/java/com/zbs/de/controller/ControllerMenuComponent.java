package com.zbs.de.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.zbs.de.model.dto.*;
import com.zbs.de.service.ServiceMenuComponent;
import com.zbs.de.util.ResponseMessage;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/menu-component")
public class ControllerMenuComponent {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerMenuComponent.class);

	@Autowired
	private ServiceMenuComponent service;

	// -------------------------------------------------------------
	// CREATE COMPONENT
	// -------------------------------------------------------------
	@PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage create(@RequestBody DtoMenuComponent request, HttpServletRequest httpRequest) {
		try {
			LOGGER.info("Creating menu component for parent: {}", request.getParentMenuItemId());
			DtoResult result = service.createComponent(request);
			if (result.getResult() != null) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
						result.getResult());
			}
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, result.getTxtMessage(),
					null);
		} catch (Exception e) {
			LOGGER.error("Error creating menu component", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to create menu component: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// CREATE SELECTION GROUP
	// -------------------------------------------------------------
	@PostMapping(value = "/create-selection-group", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage createSelectionGroup(@RequestBody DtoSelectionGroupRequest request,
			HttpServletRequest httpRequest) {
		try {
			LOGGER.info("Creating selection group for parent: {}", request.getParentMenuItemId());
			DtoResult result = service.createSelectionGroup(request);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error creating selection group", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to create selection group: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// UPDATE COMPONENT
	// -------------------------------------------------------------
	@PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage update(@RequestBody DtoMenuComponent dto, HttpServletRequest request) {
		try {
			LOGGER.info("Updating menu component ID: {}", dto.getSerComponentId());
			DtoResult result = service.updateComponent(dto);
			if (result.getResult() != null) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
						result.getResult());
			}
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, result.getTxtMessage(),
					null);
		} catch (Exception e) {
			LOGGER.error("Error updating menu component", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to update menu component: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// DELETE COMPONENT
	// -------------------------------------------------------------
	@PostMapping(value = "/delete", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage delete(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Deleting menu component ID: {}", dtoSearch.getIdL());
			DtoResult result = service.deleteComponent(dtoSearch.getIdL());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error deleting menu component", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to delete menu component: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// DELETE ALL COMPONENTS BY PARENT
	// -------------------------------------------------------------
	@PostMapping(value = "/delete-by-parent", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage deleteByParent(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Deleting components for parent menu item ID: {}", dtoSearch.getIdL());
			DtoResult result = service.deleteComponentsByParent(dtoSearch.getIdL());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error deleting components by parent", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to delete components: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GET COMPONENT BY ID
	// -------------------------------------------------------------
	@PostMapping(value = "/getById", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getById(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Fetching menu component by ID: {}", dtoSearch.getIdL());
			DtoResult result = service.getComponentById(dtoSearch.getIdL());
			if (result.getResult() != null) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
						result.getResult());
			}
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, result.getTxtMessage(),
					null);
		} catch (Exception e) {
			LOGGER.error("Error fetching menu component by ID", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch menu component: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GET COMPONENTS BY MENU ITEM
	// -------------------------------------------------------------
	@PostMapping(value = "/get-by-menu-item", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getByMenuItem(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Fetching components for menu item ID: {}", dtoSearch.getIdL());
			DtoResult result = service.getComponentsByMenuItem(dtoSearch.getIdL());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error fetching components by menu item", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch components: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GET ACTIVE COMPONENTS BY MENU ITEM
	// -------------------------------------------------------------
	@PostMapping(value = "/get-active-by-menu-item", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getActiveByMenuItem(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Fetching active components for menu item ID: {}", dtoSearch.getIdL());
			DtoResult result = service.getActiveComponentsByMenuItem(dtoSearch.getIdL());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error fetching active components", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch active components: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GET COMPONENTS GROUPED
	// -------------------------------------------------------------
	@PostMapping(value = "/get-grouped", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getGrouped(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Fetching grouped components for menu item ID: {}", dtoSearch.getIdL());
			DtoResult result = service.getComponentsGrouped(dtoSearch.getIdL());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error fetching grouped components", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch grouped components: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GET MENU ITEM WITH COMPONENTS
	// -------------------------------------------------------------
	@PostMapping(value = "/get-with-components", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getWithComponents(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Fetching menu item with components ID: {}", dtoSearch.getIdL());
			DtoResult result = service.getMenuItemWithComponents(dtoSearch.getIdL());
			if (result.getResult() != null) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
						result.getResult());
			}
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, result.getTxtMessage(),
					null);
		} catch (Exception e) {
			LOGGER.error("Error fetching menu item with components", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch menu item with components: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// UPDATE COMPONENT SEQUENCE
	// -------------------------------------------------------------
	@PostMapping(value = "/update-sequence", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage updateSequence(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Updating component sequence");
			List<Long> componentIds = dtoSearch.getListIdLs();
			if (componentIds == null || componentIds.isEmpty()) {
				return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
						"Component IDs list is required", null);
			}

			DtoResult result = service.updateComponentSequence(componentIds);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error updating component sequence", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to update sequence: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// COPY COMPONENTS
	// -------------------------------------------------------------
	@PostMapping(value = "/copy", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage copyComponents(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Copying components from {} to {}", dtoSearch.getIdL(), dtoSearch.getIdL1());
			DtoResult result = service.copyComponents(dtoSearch.getIdL(), dtoSearch.getIdL1());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error copying components", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to copy components: " + e.getMessage(), null);
		}
	}
}