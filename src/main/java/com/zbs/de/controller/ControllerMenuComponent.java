package com.zbs.de.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zbs.de.model.dto.DtoMenuComponentRequest;
import com.zbs.de.model.dto.DtoMenuComponent;
import com.zbs.de.model.dto.DtoMenuItemRole;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.service.ServiceMenuComponent;
import com.zbs.de.util.ResponseMessage;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/menu/component")
@CrossOrigin(origins = "")
public class ControllerMenuComponent {

	@Autowired
	private ServiceMenuComponent service;

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerMenuComponent.class);

	// -------------------------------------------------------------
	// SAVE or UPDATE COMPONENTS (BULK OPERATION)
	// -------------------------------------------------------------
	@PostMapping(value = "/saveOrUpdateBulk", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage saveOrUpdateComponents(@RequestBody DtoMenuComponentRequest request,
			HttpServletRequest httpRequest) {
		LOGGER.info("Saving/Updating menu components for parent: {}", request.getParentMenuItemId());

		try {
			// Validate all groups before processing
			for (DtoMenuComponent group : request.getComponents()) {
				Map<String, Object> validation = service.validateComponentGroup(group);
				if (!(Boolean) validation.get("isValid")) {
					return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
							"Validation failed for group: " + group.getTxtComponenetKindRoleName(),
							validation.get("errors"));
				}
			}

			DtoResult result = service.saveOrUpdateComponents(request);

			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Components saved successfully",
					result.getResult());

		} catch (Exception e) {
			LOGGER.error("Error saving components", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GET COMPONENTS GROUPED BY KIND
	// -------------------------------------------------------------
	@PostMapping(value = "/getGroupsByParent", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getComponentsByParentId(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		LOGGER.info("Fetching component groups for parent menu item: {}", dtoSearch.getIdL());

		try {
			List<DtoMenuComponent> groups = service.getComponentsByParentId(dtoSearch.getIdL());

			if (groups != null && !groups.isEmpty()) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK,
						"Successfully fetched component groups", groups);
			}

			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "No component groups found", groups);

		} catch (Exception e) {
			LOGGER.error("Error fetching component groups", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// DELETE COMPONENT GROUP
	// -------------------------------------------------------------
	@PostMapping(value = "/deleteGroup", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage deleteComponentsByGroup(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		LOGGER.info("Deleting component group for parent: {} and role: {}", dtoSearch.getIdL(), dtoSearch.getId());

		try {
			DtoResult result = service.deleteComponentsByGroup(dtoSearch.getIdL(), dtoSearch.getId());

			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Component group deleted successfully",
					result.getResult());

		} catch (NumberFormatException e) {
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Invalid role ID format",
					null);
		} catch (Exception e) {
			LOGGER.error("Error deleting component group", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GET AVAILABLE COMPONENT ROLES
	// -------------------------------------------------------------
	@PostMapping(value = "/getComponentRoles", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getComponentRoles(HttpServletRequest request) {
		LOGGER.info("Fetching available component roles");

		try {
			List<DtoMenuItemRole> roles = service.getAvailableComponentRoles();

			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully fetched component roles",
					roles);

		} catch (Exception e) {
			LOGGER.error("Error fetching component roles", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GET USED COMPONENT ROLES FOR PARENT
	// -------------------------------------------------------------
	@PostMapping(value = "/getUsedRoles", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getUsedComponentRoles(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		LOGGER.info("Fetching used component roles for parent: {}", dtoSearch.getIdL());

		try {
			List<DtoMenuItemRole> roles = service.getUsedComponentRoles(dtoSearch.getIdL());

			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK,
					"Successfully fetched used component roles", roles);

		} catch (Exception e) {
			LOGGER.error("Error fetching used component roles", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// VALIDATE COMPONENT GROUP
	// -------------------------------------------------------------
	@PostMapping(value = "/validateGroup", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage validateComponentGroup(@RequestBody DtoMenuComponent group, HttpServletRequest request) {
		LOGGER.info("Validating component group: {}", group.getTxtComponenetKindRoleCode());

		try {
			Map<String, Object> validationResult = service.validateComponentGroup(group);

			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Validation completed", validationResult);

		} catch (Exception e) {
			LOGGER.error("Error validating component group", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}
	}
}