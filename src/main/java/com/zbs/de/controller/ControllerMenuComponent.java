package com.zbs.de.controller;

import com.zbs.de.service.ServiceMenuComponent;
import com.zbs.de.model.dto.DtoMenuComponent;
import com.zbs.de.util.ResponseMessage;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/menu/component")
@CrossOrigin(origins = "*") // Adjust origins as needed for security
public class ControllerMenuComponent {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerMenuComponent.class);

	private final ServiceMenuComponent svc;

	public ControllerMenuComponent(ServiceMenuComponent svc) {
		this.svc = svc;
	}

	@PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage create(@RequestBody DtoMenuComponent dto, HttpServletRequest request) {
		try {
			LOGGER.info("Saving or updating MenuComponent: {}", dto);
			DtoMenuComponent res = svc.create(dto);

			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully saved", res);
		} catch (Exception e) {
			LOGGER.error("Failed to save MenuComponent", e);
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to save", dto);
		}
	}

	@PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage update(@RequestBody DtoMenuComponent dto, HttpServletRequest request) {
		try {
			LOGGER.info("Saving or updating MenuComponent: {}", dto);
			DtoMenuComponent res = svc.update(dto.getSerComponentId(), dto);

			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully updated", res);
		} catch (Exception e) {
			LOGGER.error("Failed to update MenuComponent", e);
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to update", dto);
		}
	}

	@PostMapping(value = "/by-parent", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage findByParent(@RequestBody Long parentId, HttpServletRequest request) {
		try {
			LOGGER.info("Fetching MenuComponents by parentId: {}", parentId);
			List<DtoMenuComponent> components = svc.findByParent(parentId);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully fetched", components);
		} catch (Exception e) {
			LOGGER.error("Failed to fetch by parentId {}", parentId, e);
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"Failed to fetch by parentId", null);
		}
	}

	@PostMapping(value = "/delete", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage delete(@RequestBody Long id, HttpServletRequest request) {
		try {
			LOGGER.info("Deleting MenuComponent with id: {}", id);
			svc.delete(id);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Deleted successfully", null);
		} catch (Exception e) {
			LOGGER.error("Failed to delete MenuComponent with id {}", id, e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to delete", null);
		}
	}

}
