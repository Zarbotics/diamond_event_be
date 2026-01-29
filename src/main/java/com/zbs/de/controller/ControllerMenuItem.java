package com.zbs.de.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.zbs.de.model.dto.DtoMenuCsvImportResult;
import com.zbs.de.model.dto.DtoMenuItem;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;
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
	public ResponseMessage deleteById(@RequestBody Long id, HttpServletRequest request) {
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

	@PostMapping(value = "/getAll", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAll(HttpServletRequest request) {
		LOGGER.info("Fetching MenuItem Tree");

		try {
			List<DtoMenuItem> tree = service.getAll();

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

	@PostMapping(path = "/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DtoMenuCsvImportResult> importCsv(@RequestPart("file") MultipartFile file,
			HttpServletRequest request) {
		DtoMenuCsvImportResult result = service.importCsv(file);
		return ResponseEntity.ok(result);
	}

	@PostMapping(value = "/generateCode", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage generateMenuItemCode(HttpServletRequest request, @RequestBody DtoSearch dtoSearch) {
		try {

			String txtCode = service.generateNextCode(dtoSearch.getSearchKeyword());

			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched MenuItem Code.", txtCode);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to generate code.", null);
		}
	}

	@PostMapping(value = "/getValidParentsByRole", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getValidParentsByRole(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {

		LOGGER.info("Fetching valid parent MenuItems for role: {}", dtoSearch.getSearchKeyword());

		try {
			List<DtoMenuItem> parents = service.getValidParentsByRole(dtoSearch.getSearchKeyword());

			if (parents != null && !parents.isEmpty()) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK,
						"Successfully fetched valid parent menu items", parents);
			}

			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "No valid parent menu items found",
					parents);

		} catch (Exception e) {
			LOGGER.error("Error fetching valid parent MenuItems", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}
	}

	@PostMapping(value = "/getAllByRoleId", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllByRoleId(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {

		LOGGER.info("Fetching valid parent MenuItems for role: {}", dtoSearch.getId());

		try {
			DtoResult dtoResult = service.getAllByRoleId(dtoSearch.getId());

			if (dtoResult != null && dtoResult.getResult() != null) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK,
						"Successfully fetched valid parent menu items", dtoResult.getResult());
			}

			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, dtoResult.getTxtMessage(), null);

		} catch (Exception e) {
			LOGGER.error("Error fetching valid parent MenuItems", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}
	}

	@PostMapping(value = "/getAllRoles", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllRoles(HttpServletRequest request) {
		LOGGER.info("Fetching Menu Roles");
		try {
			List<String> list = service.getRoles();
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully Fetched", list);
		} catch (Exception e) {
			LOGGER.error("Error fetching Menu roles", e);
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"Failed to fetch menu roles", null);
		}
	}

	@PostMapping(value = "/getAllActiveCompositeItems", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllCompositeItems(HttpServletRequest request) {

		LOGGER.info("Fetching all composite Items");

		try {
			DtoResult dtoResult = service.getAllActiveCompositeItems();

			if (dtoResult != null && dtoResult.getResult() != null) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK,
						"Successfully fetched all composite Items", dtoResult.getResult());
			}

			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, dtoResult.getTxtMessage(), null);

		} catch (Exception e) {
			LOGGER.error("Error fetching all composite Items", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}
	}

	@PostMapping(value = "/getAllActive", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllActive(HttpServletRequest request) {

		LOGGER.info("Fetching all active Items");

		try {
			DtoResult dtoResult = service.getAllActive();

			if (dtoResult != null && dtoResult.getResult() != null) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK,
						"Successfully fetched all active Items", dtoResult.getResult());
			}

			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, dtoResult.getTxtMessage(), null);

		} catch (Exception e) {
			LOGGER.error("Error fetching all active Items", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}
	}

	@PostMapping(value = "/getValidParentsByRoleId", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getValidParentsByRoleId(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {

		LOGGER.info("Fetching valid parent MenuItems for role: {}", dtoSearch.getId());

		try {
			List<DtoMenuItem> parents = service.getValidParentsByRoleID(dtoSearch.getId());

			if (parents != null && !parents.isEmpty()) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK,
						"Successfully fetched valid parent menu items", parents);
			}

			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "No valid parent menu items found",
					parents);

		} catch (Exception e) {
			LOGGER.error("Error fetching valid parent MenuItems", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// SEARCH MENU ITEMS
	// -------------------------------------------------------------
	@PostMapping(value = "/searchMenuItems", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage searchMenuItems(@RequestBody DtoSearch dtoSearch, HttpServletRequest httpRequest) {
		LOGGER.info("Searching menu items with query: {}", dtoSearch.getSearchKeyword());

		try {
			List<DtoMenuItem> items = service.searchMenuItems(dtoSearch.getTxtQuery(), dtoSearch.getTxtRole(), // role
					dtoSearch.getTxtType(), // type
					dtoSearch.getNumlimit());

			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Search results", items);

		} catch (Exception e) {
			LOGGER.error("Error searching menu items", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}
	}
	
	
	@PostMapping(value = "/getAllActiveItemsOfOtherSubCategory", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAllActiveItemsOfOtherSubCategory(HttpServletRequest request) {

		LOGGER.info("Fetching all active Items");

		try {
			DtoResult dtoResult = service.getAllNonCompositeActiveItemsByParentItemCode("other");

			if (dtoResult != null && dtoResult.getResult() != null) {
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK,
						"Successfully fetched all active Items", dtoResult.getResult());
			}

			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, dtoResult.getTxtMessage(), null);

		} catch (Exception e) {
			LOGGER.error("Error fetching all active Items", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}
	}
	
	 @PostMapping(path = "/upload-subCategory", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
		public ResponseEntity<String> uploadsubCategories(@RequestPart("file") MultipartFile file,
				HttpServletRequest request) {
			String result = service.readMenuItemCsv(file);
			return ResponseEntity.ok(result);
		}
	 
		// -------------------------------------------------------------
		// GENERATE ASSIGNMENT CODE
		// -------------------------------------------------------------
		@PostMapping(value = "/getAllPriceUnitTypes", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
		public ResponseMessage getAllPriceUnitTypes(HttpServletRequest request) {
			try {
				LOGGER.info("Generating assignment code");
				List<String> units= service.getAllPriceUnitTypes();
				return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Code generated successfully", units);
			} catch (Exception e) {
				LOGGER.error("Error generating assignment code", e);
				return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
						"Failed to generate assignment code: " + e.getMessage(), null);
			}
		}

}
