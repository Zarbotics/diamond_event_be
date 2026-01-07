package com.zbs.de.controller;

import java.io.PrintWriter;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.model.dto.price.*;
import com.zbs.de.service.ServicePriceEntry;
import com.zbs.de.util.ResponseMessage;

@RestController
@RequestMapping("/menu/price-entry")
@CrossOrigin(origins = "")
public class ControllerPriceEntry {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerPriceEntry.class);

	@Autowired
	private ServicePriceEntry service;

	// -------------------------------------------------------------
	// GET MENU TREE WITH PRICES
	// -------------------------------------------------------------
	@PostMapping(value = "/getMenuTree", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getMenuTreeWithPrices(@RequestBody DtoPriceEntrySearch request,
			HttpServletRequest httpRequest) {
		LOGGER.info("Fetching menu tree with prices for version: {}", request.getSerPriceVersionId());

		try {
			DtoPriceMatrixTree tree = service.getMenuTreeWithPrices(request.getSerPriceVersionId(),
					request.getTxtRole(), request.getTxtType());

			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK,
					"Successfully fetched menu tree with prices", tree);

		} catch (Exception e) {
			LOGGER.error("Error getting menu tree with prices", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GET PRICE ENTRIES BY VERSION
	// -------------------------------------------------------------
	@PostMapping(value = "/getEntriesByVersion", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getPriceEntriesByVersion(@RequestBody DtoSearch dtoSearch, HttpServletRequest request) {
		LOGGER.info("Fetching price entries for version: {}", dtoSearch.getIdL());

		try {
			DtoPriceMatrixEntries entries = service.getPriceEntriesByVersion(dtoSearch.getIdL(),
					dtoSearch.getSearchKeyword());

			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully fetched price entries",
					entries);

		} catch (Exception e) {
			LOGGER.error("Error getting price entries", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GET AVAILABLE FILTERS
	// -------------------------------------------------------------
	@PostMapping(value = "/getFilters", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getAvailableFilters(HttpServletRequest request) {
		LOGGER.info("Fetching available price matrix filters");

		try {
			DtoPriceMatrixFilters filters = service.getAvailableFilters();

			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully fetched filters", filters);

		} catch (Exception e) {
			LOGGER.error("Error getting filters", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// BULK ASSIGN PRICES
	// -------------------------------------------------------------
	@PostMapping(value = "/{versionId}/bulkAssign", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage bulkAssignPrices(@PathVariable Long versionId, @RequestBody DtoBulkAssignRequest request,
			HttpServletRequest httpRequest) {
		LOGGER.info("Bulk assigning prices for version: {}", versionId);

		try {
			DtoBulkAssignResult result = service.bulkAssignPrices(versionId, request);

			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, result.getMessage(), result);

		} catch (Exception e) {
			LOGGER.error("Error in bulk assign", e);
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// BULK UPDATE PRICE ENTRIES
	// -------------------------------------------------------------
	@PostMapping(value = "/{versionId}/bulk-update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage bulkUpdatePriceEntries(@PathVariable Long versionId,
			@RequestBody List<DtoPriceEntryUpdate> updates, HttpServletRequest httpRequest) {
		LOGGER.info("Bulk updating price entries for version: {}", versionId);

		try {
			DtoResult result = service.bulkUpdatePriceEntries(versionId, updates);

			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Bulk update completed",
					result.getResult());

		} catch (Exception e) {
			LOGGER.error("Error in bulk update", e);
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// BULK DELETE PRICE ENTRIES
	// -------------------------------------------------------------
	@PostMapping(value = "/{versionId}/bulkDelete", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage bulkDeletePriceEntries(@PathVariable Long versionId, @RequestBody List<Long> priceEntryIds,
			HttpServletRequest httpRequest) {
		LOGGER.info("Bulk deleting price entries for version: {}", priceEntryIds);

		try {
			DtoResult result = service.bulkDeletePriceEntries(versionId, priceEntryIds);

			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Bulk delete completed",
					result.getResult());

		} catch (Exception e) {
			LOGGER.error("Error in bulk delete", e);
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// EXPORT PRICE MATRIX TO CSV
	// -------------------------------------------------------------
	@PostMapping(value = "/export", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void exportPriceMatrix(@RequestBody DtoPriceEntrySearch request, HttpServletResponse response) {

		LOGGER.info("Exporting price matrix for version: {}", request.getSerPriceVersionId());

		try {
			DtoResult exportResult = service.exportPriceMatrixToCsv(request.getSerPriceVersionId(),
					request.getTxtScope(), request.getBlnIncludeMenuInfo());

			response.setContentType("text/csv");
			response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
					"attachment; filename=\"price-matrix-" + request.getSerPriceVersionId() + ".csv\"");

			PrintWriter writer = response.getWriter();
			writer.write((String) exportResult.getResult());
			writer.flush();

		} catch (Exception e) {
			LOGGER.error("Error exporting price matrix", e);
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

	// -------------------------------------------------------------
	// QUICK PRICE PREVIEW
	// -------------------------------------------------------------
	@PostMapping(value = "/preview", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage quickPricePreview(@RequestBody DtoPricePreviewRequest request,
			HttpServletRequest httpRequest) {
		LOGGER.info("Calculating quick price preview");

		try {
			DtoPricePreviewRequest result = service.quickPricePreview(request);

			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Preview calculated successfully", result);

		} catch (Exception e) {
			LOGGER.error("Error in price preview", e);
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, e.getMessage(), null);
		}
	}

	// -------------------------------------------------------------
	// GET QUANTITY BREAKS FOR ITEM
	// -------------------------------------------------------------
	@PostMapping(value = "/getQuantityBreaks", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage getQuantityBreaks(@RequestBody DtoSearch dtoSearch, HttpServletRequest httpRequest) {
		LOGGER.info("Fetching quantity breaks for item: {}", dtoSearch.getIdL());

		try {
			List<DtoPriceEntryGrid> breaks = service.getQuantityBreaksForItem(dtoSearch.getIdL(), dtoSearch.getIdL1());

			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Quantity breaks fetched successfully",
					breaks);

		} catch (Exception e) {
			LOGGER.error("Error getting quantity breaks", e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage(), null);
		}
	}
}
