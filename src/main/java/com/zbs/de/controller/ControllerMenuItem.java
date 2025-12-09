package com.zbs.de.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.zbs.de.model.dto.DtoMenuItem;
import com.zbs.de.service.ServiceMenuItem;
import com.zbs.de.util.ResponseMessage;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/menu/item")
@CrossOrigin(origins = "")
public class ControllerMenuItem {

	@Autowired
	private ServiceMenuItem service;

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerMenuItem.class);

	// -------------------------------------------------------------
	// CREATE or UPDATE
	// -------------------------------------------------------------
	@PostMapping(value = "/save", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage save(@RequestBody DtoMenuItem dto, HttpServletRequest request) {
		LOGGER.info("Saving Menu Item: {}", dto);

		DtoMenuItem result = service.create(dto);

		if (result != null) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully saved", result);
		}
		return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to save MenuItem",
				dto);
	}

	@PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage update(@RequestBody DtoMenuItem dto, HttpServletRequest request) {
		LOGGER.info("Saving Menu Item: {}", dto);

		DtoMenuItem result = service.update(dto.getSerMenuItemId(), dto);

		if (result != null) {
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully updated", result);
		}
		return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to update MenuItem",
				dto);
	}

	// -------------------------------------------------------------
	// DELETE
	// -------------------------------------------------------------
	@PostMapping(value = "/deleteById", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage deleteById(@RequestBody Long id) {
		LOGGER.info("Deleting MenuItem by ID: {}", id);

		try {
			service.delete(id);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Deleted", null);
		} catch (Exception e) {
			LOGGER.error("Error deleting MenuItem", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// FETCH TREE
	// -------------------------------------------------------------
	@PostMapping(value = "/tree", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getTree(HttpServletRequest request) {
		LOGGER.info("Fetching MenuItem Tree");

		try {
			List<DtoMenuItem> tree = service.getTree();

			if (tree != null && !tree.isEmpty()) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Fetched", tree);
			}

			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "No Menu Items Found", tree);

		} catch (Exception e) {
			LOGGER.error("Error fetching MenuItem Tree", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GET BY ID
	// -------------------------------------------------------------
	@PostMapping(value = "/getById", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getById(@RequestBody Long id, HttpServletRequest request) {
		LOGGER.info("Fetching MenuItem by ID: {}", id);

		try {
			DtoMenuItem result = service.getById(id);

			if (result != null) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Fetched", result);
			}

			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "MenuItem Not Found",
					id);

		} catch (Exception e) {
			LOGGER.error("Error fetching MenuItem", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}
	}

}
