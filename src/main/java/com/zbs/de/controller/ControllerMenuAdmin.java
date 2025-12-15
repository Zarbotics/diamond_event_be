package com.zbs.de.controller;

import com.zbs.de.model.dto.DtoMenuItem;
import com.zbs.de.service.ServiceMenuItem;
import com.zbs.de.util.ResponseMessage;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/menu")
public class ControllerMenuAdmin {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerMenuAdmin.class);

	@Autowired
	private ServiceMenuItem svc;
	
	
	@PostMapping(value = "/getAllRoles", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllRoles(HttpServletRequest request) {
		LOGGER.info("Fetching Menu Roles");
		try {
			List<String> list = svc.getRoles();
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Fetched", list);
		} catch (Exception e) {
			LOGGER.error("Error fetching Menu roles", e);
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"Failed to fetch menu roles", null);
		}
	}
	
	
	
	@PostMapping(value = "/getAllTypes", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllTypes(@RequestBody String txtRole, HttpServletRequest request) {
		LOGGER.info("Fetching Menu Types:", txtRole);
		try {
			List<String> list = svc.getTypes();
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Fetched", list);
		} catch (Exception e) {
			LOGGER.error("Error fetching Menu Types", e);
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"Failed to fetch menu types", null);
		}
	}

	// ================================
	// GET MENU ITEMS BY ROLE
	// ================================
	@PostMapping(value = "/getByRole", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getByRole(@RequestBody String txtRole, HttpServletRequest request) {
		LOGGER.info("Fetching Menu Items by Role:", txtRole);
		try {
			List<DtoMenuItem> list = svc.findByRole(txtRole);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Fetched", list);
		} catch (Exception e) {
			LOGGER.error("Error fetching Menu Items by role", e);
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"Failed to fetch menu items", null);
		}
	}
	
	
	// ================================
	// GET MENU ITEMS BY Type
	// ================================
	@PostMapping(value = "/getByType", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getByType(@RequestBody String txtType, HttpServletRequest request) {
		LOGGER.info("Fetching Menu Items by Type:", txtType);
		try {
			List<DtoMenuItem> list = svc.findByType(txtType);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Fetched", list);
		} catch (Exception e) {
			LOGGER.error("Error fetching Menu Items by type", e);
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"Failed to fetch menu items", null);
		}
	}

	// ================================
	// GET ALL ITEMS UNDER BUNDLE
	// ================================
	@PostMapping(value = "/getBundleItems", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getBundleItems(@RequestBody Long bundleId, HttpServletRequest request) {
		LOGGER.info("Fetching Bundle Items for Bundle ID: ", bundleId);
		try {
			List<DtoMenuItem> list = svc.getBundleItems(bundleId);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Fetched", list);
		} catch (Exception e) {
			LOGGER.error("Error fetching bundle items", e);
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"Failed to fetch bundle items", null);
		}
	}

	// ================================
	// GET DESCENDANTS BY PATH
	// ================================
	@PostMapping(value = "/getDescendantsByPath", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getDescendantsByPath(@RequestBody String txtPath) {
		LOGGER.info("Fetching Descendants for Path:", txtPath);
		try {
			List<DtoMenuItem> list = svc.findDescendantsByPath(txtPath);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Fetched", list);
		} catch (Exception e) {
			LOGGER.error("Error fetching descendants by path", e);
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"Failed to fetch descendants", null);
		}
	}

	// ================================
	// GET SELECTABLE CHILDREN OF PARENT
	// ================================
	@PostMapping(value = "/getSelectableChildren", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getSelectableChildren(@RequestBody Long parentId, HttpServletRequest request) {
		LOGGER.info("Fetching Selectable Children for Parent ID: ", parentId);
		try {
			List<DtoMenuItem> list = svc.getSelectableItemsUnderParent(parentId);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Fetched", list);
		} catch (Exception e) {
			LOGGER.error("Error fetching selectable children", e);
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"Failed to fetch selectable children", null);
		}
	}

}
