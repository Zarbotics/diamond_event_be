package com.zbs.de.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.zbs.de.model.dto.DtoMenuItemRole;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.service.ServiceMenuItemRole;
import com.zbs.de.util.ResponseMessage;

@RestController
@RequestMapping("/menuItemRole")
@CrossOrigin(origins = "")
public class ControllerMenuItemRole {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerMenuItemRole.class);

	@Autowired
	private ServiceMenuItemRole service;

	@PostMapping(value = "/saveOrUpdate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage saveOrUpdate(@RequestBody DtoMenuItemRole dto) {
		LOGGER.info("Request received to save/update MenuItemRole: {}", dto);
		try {
			DtoMenuItemRole result = service.saveOrUpdate(dto);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Menu Item Role saved successfully",
					result);
		} catch (Exception e) {
			LOGGER.error("Error saving MenuItemRole", e);
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, e.getMessage(), dto);
		}
	}

	@PostMapping(value = "/getById", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getById(@RequestBody DtoSearch dtoSearch) {
		LOGGER.info("Request received to fetch MenuItemRole by ID: {}", dtoSearch.getId());
		try {
			DtoMenuItemRole dto = service.getById(dtoSearch.getId());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched successfully", dto);
		} catch (Exception e) {
			LOGGER.error("Error fetching MenuItemRole by ID", e);
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, e.getMessage(), null);
		}
	}

	@PostMapping(value = "/getAllRoles", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllRoles() {
		LOGGER.info("Request received to fetch all MenuItemRoles");
		try {
			List<DtoMenuItemRole> list = service.getAllRoles();
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched successfully", list);
		} catch (Exception e) {
			LOGGER.error("Error fetching MenuItemRoles", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}
	}

	@PostMapping(value = "/getAllActiveRoles", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllActiveRoles() {
		LOGGER.info("Request received to fetch active MenuItemRoles");
		try {
			List<DtoMenuItemRole> list = service.getAllActiveRoles();
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched successfully", list);
		} catch (Exception e) {
			LOGGER.error("Error fetching active MenuItemRoles", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}
	}

	@PostMapping(value = "/getAllCompositionRoles", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllCompositionRoles() {
		LOGGER.info("Request received to fetch composition roles");
		try {
			List<DtoMenuItemRole> list = service.getAllCompositionRoles();
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched successfully", list);
		} catch (Exception e) {
			LOGGER.error("Error fetching composition roles", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}
	}
	
	@PostMapping(value = "/getAllMenuItemRoles", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllMenuItemRoles() {
		LOGGER.info("Request received to fetch composition roles");
		try {
			List<DtoMenuItemRole> list = service.getAllMenuItemRoles();
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched successfully", list);
		} catch (Exception e) {
			LOGGER.error("Error fetching composition roles", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}
	}

	@PostMapping(value = "/getAllActiveCompositionRoles", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllActiveCompositionRoles() {
		LOGGER.info("Request received to fetch active composition roles");
		try {
			List<DtoMenuItemRole> list = service.getAllActiveCompositionRoles();
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched successfully", list);
		} catch (Exception e) {
			LOGGER.error("Error fetching active composition roles", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}
	}
	
	@PostMapping(value = "/getAllActiveMenuItemRoles", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllActiveMenuItemRoles() {
		LOGGER.info("Request received to fetch active composition roles");
		try {
			List<DtoMenuItemRole> list = service.getAllActiveMenuItemRoles();
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched successfully", list);
		} catch (Exception e) {
			LOGGER.error("Error fetching active composition roles", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}
	}
}
