package com.zbs.de.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zbs.de.model.ExternalSupplierCategory;
import com.zbs.de.model.dto.DtoExternalSupplierCategory;
import com.zbs.de.repository.RepositoryExternalSupplierCategory;
import com.zbs.de.service.impl.ServiceCurrentUser;
import com.zbs.de.util.ResponseMessage;

/**
 * The kinds of supplier a customer may declare.
 *
 * <h3>Two audiences, two endpoints</h3>
 *
 * {@code /offered} is what the journey's dropdown reads: active categories
 * only, no counts, no retired ones. {@code /all} is the office's list, which
 * needs the retired ones in order to bring one back and the usage counts in
 * order to make "delete" a decision rather than a reflex.
 *
 * <p>
 * Keeping them apart means the customer-facing call cannot start leaking
 * administrative detail because somebody added a field for the office.
 */
@RestController
@RequestMapping("/externalSupplierCategory")
public class ControllerExternalSupplierCategory {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerExternalSupplierCategory.class);

	@Autowired
	private RepositoryExternalSupplierCategory repository;

	/** What the journey offers. Readable by any signed-in customer. */
	@PostMapping(value = "/offered", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage offered() {
		List<DtoExternalSupplierCategory> offered = repository.findOffered().stream()
				.map(ControllerExternalSupplierCategory::describeForCustomer)
				.toList();

		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Supplier categories", offered);
	}

	/** The office's list, with retired categories and usage counts. */
	@PostMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage all() {
		Map<Long, Long> counts = new HashMap<>();
		for (Object[] row : repository.countSuppliersByCategory()) {
			counts.put((Long) row[0], (Long) row[1]);
		}

		List<DtoExternalSupplierCategory> categories = new ArrayList<>();
		for (ExternalSupplierCategory category : repository.findAllForOffice()) {
			DtoExternalSupplierCategory dto = describeForCustomer(category);
			dto.setBlnIsActive(category.getBlnIsActive());
			dto.setNumSupplierCount(counts.getOrDefault(category.getSerSupplierCategoryId(), 0L));
			categories.add(dto);
		}

		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Supplier categories", categories);
	}

	/**
	 * Adds one, or changes one.
	 *
	 * <p>
	 * The name is checked here rather than left to the unique index, because a
	 * constraint violation reaches the office as a stack trace about
	 * {@code ux_external_supplier_category_name} and this reaches them as a
	 * sentence about the category they were trying to add.
	 */
	@PostMapping(value = "/save", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage save(@RequestBody DtoExternalSupplierCategory request) {
		String name = request.getTxtName() == null ? "" : request.getTxtName().trim();
		if (name.isEmpty()) {
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"A category needs a name.", null);
		}

		if (repository.findByName(name, request.getSerSupplierCategoryId()).isPresent()) {
			return new ResponseMessage(HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT,
					"There is already a category called \"" + name + "\".", null);
		}

		ExternalSupplierCategory category = request.getSerSupplierCategoryId() == null
				? new ExternalSupplierCategory()
				: repository.findById(request.getSerSupplierCategoryId()).orElse(null);

		if (category == null) {
			return new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
					"That category no longer exists.", null);
		}

		category.setTxtName(name);
		category.setTxtDescription(trimToNull(request.getTxtDescription()));
		category.setNumDisplayOrder(request.getNumDisplayOrder());
		category.setBlnIsActive(request.getBlnIsActive() == null || request.getBlnIsActive());
		category.setBlnIsDeleted(false);

		if (category.getSerSupplierCategoryId() == null) {
			category.setCreatedDate(new Date());
			category.setCreatedBy(ServiceCurrentUser.getCurrentUserId());
		} else {
			category.setUpdatedDate(new Date());
			category.setUpdatedBy(ServiceCurrentUser.getCurrentUserId());
		}

		ExternalSupplierCategory saved = repository.save(category);
		LOGGER.info("Supplier category {} saved", saved.getSerSupplierCategoryId());

		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Saved",
				describeForCustomer(saved));
	}

	/**
	 * Retires a category, keeping every booking that used it.
	 *
	 * <h3>Why it is never actually deleted</h3>
	 *
	 * Because a supplier declared against it is a fact about a day that
	 * happened. Removing the row would either orphan those suppliers or, with a
	 * cascade, delete them — and a photographer vanishing from next Saturday's
	 * run sheet because somebody tidied a lookup list is not a risk worth
	 * carrying for a list of twelve words.
	 *
	 * <p>
	 * So "delete" marks it deleted and it stops being offered. The office's
	 * list shows how many suppliers use one before they press it.
	 */
	@PostMapping(value = "/delete", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage delete(@RequestBody DtoExternalSupplierCategory request) {
		ExternalSupplierCategory category = request.getSerSupplierCategoryId() == null ? null
				: repository.findById(request.getSerSupplierCategoryId()).orElse(null);

		if (category == null) {
			return new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
					"That category no longer exists.", null);
		}

		category.setBlnIsDeleted(true);
		category.setBlnIsActive(false);
		category.setUpdatedDate(new Date());
		category.setUpdatedBy(ServiceCurrentUser.getCurrentUserId());
		repository.save(category);

		LOGGER.info("Supplier category {} retired", category.getSerSupplierCategoryId());
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK,
				"Removed. Suppliers already declared under it keep it.", null);
	}

	private static DtoExternalSupplierCategory describeForCustomer(ExternalSupplierCategory category) {
		DtoExternalSupplierCategory dto = new DtoExternalSupplierCategory();
		dto.setSerSupplierCategoryId(category.getSerSupplierCategoryId());
		dto.setTxtName(category.getTxtName());
		dto.setTxtDescription(category.getTxtDescription());
		dto.setNumDisplayOrder(category.getNumDisplayOrder());
		return dto;
	}

	private static String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
