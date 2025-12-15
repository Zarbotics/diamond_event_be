package com.zbs.de.controller;

import com.zbs.de.model.dto.DtoItineraryItem;
import com.zbs.de.model.dto.DtoMenuItemItineraryMap;
import com.zbs.de.service.ServiceItineraryItem;
import com.zbs.de.service.ServiceMenuItemItineraryMap;
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
@RequestMapping("/admin/itinerary")
@CrossOrigin(origins = "*") // adjust for your environment
public class ControllerItinerary {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerItinerary.class);

	@Autowired
	private ServiceItineraryItem itemService;

	@Autowired
	private ServiceMenuItemItineraryMap mapService;

	@PostMapping(value = "/item/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage createItem(@RequestBody DtoItineraryItem dto, HttpServletRequest request) {
		try {
			LOGGER.info("Creating Itinerary Item: {}", dto);
			DtoItineraryItem created = itemService.create(dto);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Created", created);
		} catch (Exception e) {
			LOGGER.error("Failed to create Itinerary Item", e);
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to create", dto);
		}
	}

	@PostMapping(value = "/item/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage updateItem(@RequestBody DtoItineraryItem dto, HttpServletRequest request) {
		try {
			LOGGER.info("Creating Itinerary Item: {}", dto);
			DtoItineraryItem updated = itemService.update(dto.getSerItineraryItemId(), dto);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Update", updated);
		} catch (Exception e) {
			LOGGER.error("Failed to create Itinerary Item", e);
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to update", dto);
		}
	}

	@GetMapping(value = "/item/getAll", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage items(HttpServletRequest request) {
		try {
			LOGGER.info("Fetching all Itinerary Items");
			List<DtoItineraryItem> list = itemService.getAll();
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "OK", list);
		} catch (Exception e) {
			LOGGER.error("Failed to fetch Itinerary Items", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch", null);
		}
	}

	@PostMapping(value = "/map/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage createMap(@RequestBody DtoMenuItemItineraryMap dto, HttpServletRequest request) {
		try {
			LOGGER.info("Creating MenuItem-Itinerary Map: {}", dto);
			DtoMenuItemItineraryMap created = mapService.create(dto);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Created", created);
		} catch (Exception e) {
			LOGGER.error("Failed to create Map", e);
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to create", dto);
		}
	}

	@GetMapping(value = "/map/by-menu/{menuItemId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage maps(@PathVariable Long menuItemId, HttpServletRequest request) {
		try {
			LOGGER.info("Fetching Maps for menuItemId {}", menuItemId);
			List<DtoMenuItemItineraryMap> list = mapService.findByMenuItem(menuItemId);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "OK", list);
		} catch (Exception e) {
			LOGGER.error("Failed to fetch Maps for menuItemId {}", menuItemId, e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch", null);
		}
	}

	@DeleteMapping(value = "/map/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage deleteMap(@PathVariable Long id) {
		try {
			LOGGER.info("Deleting Map with id {}", id);
			mapService.delete(id);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Deleted", null);
		} catch (Exception e) {
			LOGGER.error("Failed to delete Map with id {}", id, e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to delete", null);
		}
	}
}
