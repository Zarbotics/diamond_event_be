package com.zbs.de.controller;

import com.zbs.de.model.dto.DtoItineraryCsvImportResult;
import com.zbs.de.model.dto.DtoItineraryItem;
import com.zbs.de.model.dto.DtoMenuItemItineraryMap;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.service.ServiceItineraryItem;
import com.zbs.de.service.ServiceMenuItemItineraryMap;
import com.zbs.de.util.ResponseMessage;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/admin/itinerary")
@CrossOrigin(origins = "") // adjust for your environment
public class ControllerItinerary {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerItinerary.class);

	@Autowired
	private ServiceItineraryItem itemService;

	@Autowired
	private ServiceMenuItemItineraryMap mapService;

	// -------------------------------------------------------------
	// CREATE
	// -------------------------------------------------------------
	@PostMapping(value = "/item/save", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage save(@RequestBody DtoItineraryItem dto, HttpServletRequest request) {
		try {
			LOGGER.info("Saving Itinerary Item: {}", dto);
			DtoResult result = itemService.create(dto);
			if (result.getResult() != null) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
						result.getResult());
			}
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, result.getTxtMessage(),
					null);
		} catch (Exception e) {
			LOGGER.error("Error saving Itinerary Item", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to save Itinerary Item: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// UPDATE
	// -------------------------------------------------------------
	@PostMapping(value = "/item/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage update(@RequestBody DtoItineraryItem dto, HttpServletRequest request) {
		try {
			LOGGER.info("Updating Itinerary Item: {}", dto);
			DtoResult result = itemService.update(dto);
			if (result.getResult() != null) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
						result.getResult());
			}
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, result.getTxtMessage(),
					null);
		} catch (Exception e) {
			LOGGER.error("Error updating Itinerary Item", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to update Itinerary Item: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// FETCH ALL
	// -------------------------------------------------------------
	@PostMapping(value = "/item/getAll", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAll(HttpServletRequest request) {
		try {
			LOGGER.info("Fetching all Itinerary Items");
			DtoResult result = itemService.getAll();
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error fetching all Itinerary Items", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch all Itinerary Items: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// FETCH ALL ACTIVE
	// -------------------------------------------------------------
	@PostMapping(value = "/item/getAllActive", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllActive(HttpServletRequest request) {
		try {
			LOGGER.info("Fetching all active Itinerary Items");
			DtoResult result = itemService.getAllActive();
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error fetching active Itinerary Items", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch active Itinerary Items: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// FETCH BY ID
	// -------------------------------------------------------------
	@PostMapping(value = "/item/getById", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getById(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Fetching Itinerary Item by ID: {}", dtoSearch.getIdL());
			DtoResult result = itemService.getById(dtoSearch.getIdL());
			if (result.getResult() != null) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
						result.getResult());
			}
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, result.getTxtMessage(),
					null);
		} catch (Exception e) {
			LOGGER.error("Error fetching Itinerary Item by ID", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch Itinerary Item: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GENERATE NEXT CODE
	// -------------------------------------------------------------
	@PostMapping(value = "/item/generateCode", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage generateCode(HttpServletRequest request) {
		try {
			LOGGER.info("Generating next Itinerary Item Code");
			DtoResult result = itemService.generateNextCode();
			if (result.getResult() != null) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
						result.getResult());
			}
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					result.getTxtMessage(), null);
		} catch (Exception e) {
			LOGGER.error("Error generating next code", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to generate next code: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// FETCH ACTIVE ITEMS BY TYPE
	// -------------------------------------------------------------
	@PostMapping(value = "/item/getAllActiveByType", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllActiveByType(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Fetching all active Itinerary Items by Type ID: {}", dtoSearch.getId());
			DtoResult result = itemService.getAllActiveItineraryItemsByType(dtoSearch.getId());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error fetching active items by type", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch active items by type: " + e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// FETCH ALL ITEMS BY TYPE
	// -------------------------------------------------------------
	@PostMapping(value = "/item/getAllByType", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllByType(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		try {
			LOGGER.info("Fetching all Itinerary Items by Type ID: {}", dtoSearch.getId());
			DtoResult result = itemService.getAllItineraryItemsByType(dtoSearch.getId());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getTxtMessage(),
					result.getResult());
		} catch (Exception e) {
			LOGGER.error("Error fetching items by type", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch items by type: " + e.getMessage(), null);
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
	

	@PostMapping(path = "/item/uploadcsv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DtoItineraryCsvImportResult> importCsv(@RequestPart("file") MultipartFile file,
			HttpServletRequest request) {
		DtoItineraryCsvImportResult result = itemService.importCsv(file);
		return ResponseEntity.ok(result);
	}
}
