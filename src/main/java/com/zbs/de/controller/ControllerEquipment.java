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

import com.zbs.de.model.EquipmentCategory;
import com.zbs.de.model.EquipmentItem;
import com.zbs.de.model.EquipmentRequirement;
import com.zbs.de.model.dto.DtoEquipmentItem;
import com.zbs.de.model.dto.DtoEquipmentRequirement;
import com.zbs.de.repository.RepositoryEquipmentCategory;
import com.zbs.de.repository.RepositoryEquipmentItem;
import com.zbs.de.repository.RepositoryEquipmentRequirement;
import com.zbs.de.repository.RepositoryMenuItem;
import com.zbs.de.service.impl.ServiceCurrentUser;
import com.zbs.de.service.impl.ServiceEquipmentCalculation;
import com.zbs.de.util.ResponseMessage;
import com.zbs.de.util.enums.EnmEquipmentBasis;

/**
 * The equipment an event needs, and the rules that work it out.
 *
 * <h3>Administrator-only, all of it</h3>
 *
 * Nothing here is offered to a customer. What the venue puts on a table is an
 * operational matter, and a customer who could see that their wedding needs
 * 315 plates would reasonably start asking why they are paying for 300.
 * Everything falls through to the default deny in {@code PortalEndpoints}.
 */
@RestController
@RequestMapping("/equipment")
public class ControllerEquipment {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerEquipment.class);

	@Autowired
	private RepositoryEquipmentCategory repositoryEquipmentCategory;

	@Autowired
	private RepositoryEquipmentItem repositoryEquipmentItem;

	@Autowired
	private RepositoryEquipmentRequirement repositoryEquipmentRequirement;

	@Autowired
	private RepositoryMenuItem repositoryMenuItem;

	@Autowired
	private ServiceEquipmentCalculation serviceEquipmentCalculation;

	// ── what an event needs ──────────────────────────────────────────────

	/** The equipment list for one booking, worked out now rather than stored. */
	@PostMapping(value = "/forEvent", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage forEvent(@RequestBody Map<String, Object> request) {
		Object raw = request.get("serEventMasterId");
		Integer eventId = raw instanceof Number ? ((Number) raw).intValue() : null;

		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Equipment",
				serviceEquipmentCalculation.calculateFor(eventId));
	}

	// ── the catalogue ────────────────────────────────────────────────────

	@PostMapping(value = "/categories", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage categories() {
		List<Map<String, Object>> categories = new ArrayList<>();
		for (EquipmentCategory category : repositoryEquipmentCategory.findOffered()) {
			Map<String, Object> row = new HashMap<>();
			row.put("serEquipmentCategoryId", category.getSerEquipmentCategoryId());
			row.put("txtName", category.getTxtName());
			row.put("txtDescription", category.getTxtDescription());
			categories.add(row);
		}
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Categories", categories);
	}

	@PostMapping(value = "/items", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage items() {
		Map<Integer, Long> ruleCounts = new HashMap<>();
		for (Object[] row : repositoryEquipmentItem.countRulesByItem()) {
			if (row[0] != null) {
				ruleCounts.put((Integer) row[0], (Long) row[1]);
			}
		}

		List<DtoEquipmentItem> items = new ArrayList<>();
		for (EquipmentItem item : repositoryEquipmentItem.findAllForOffice()) {
			DtoEquipmentItem dto = describe(item);
			dto.setNumRuleCount(ruleCounts.getOrDefault(item.getSerEquipmentItemId(), 0L));
			items.add(dto);
		}

		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Equipment", items);
	}

	@PostMapping(value = "/saveItem", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage saveItem(@RequestBody DtoEquipmentItem request) {
		String name = request.getTxtName() == null ? "" : request.getTxtName().trim();
		if (name.isEmpty()) {
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"A piece of equipment needs a name.", null);
		}

		/*
		  Checked here rather than left to the unique index, so a duplicate
		  reaches the office as a sentence about the thing they were adding
		  instead of a stack trace naming ux_equipment_item_name.
		*/
		if (repositoryEquipmentItem.findByName(name, request.getSerEquipmentItemId()).isPresent()) {
			return new ResponseMessage(HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT,
					"There is already something called \"" + name + "\".", null);
		}

		EquipmentItem item = request.getSerEquipmentItemId() == null ? new EquipmentItem()
				: repositoryEquipmentItem.findById(request.getSerEquipmentItemId()).orElse(null);

		if (item == null) {
			return new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
					"That equipment no longer exists.", null);
		}

		item.setTxtName(name);
		item.setTxtCode(trimToNull(request.getTxtCode()));
		item.setTxtDescription(trimToNull(request.getTxtDescription()));
		item.setTxtUnit(request.getTxtUnit() == null || request.getTxtUnit().isBlank() ? "each"
				: request.getTxtUnit().trim());
		item.setNumDisplayOrder(request.getNumDisplayOrder());
		item.setEquipmentCategory(request.getSerEquipmentCategoryId() == null ? null
				: repositoryEquipmentCategory.findById(request.getSerEquipmentCategoryId()).orElse(null));
		item.setBlnIsActive(request.getBlnIsActive() == null || request.getBlnIsActive());
		item.setBlnIsDeleted(false);
		stamp(item.getSerEquipmentItemId() == null, item);

		EquipmentItem saved = repositoryEquipmentItem.save(item);
		LOGGER.info("Equipment item {} saved", saved.getSerEquipmentItemId());
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Saved", describe(saved));
	}

	/**
	 * Retires a piece of equipment, keeping the rules that mention it.
	 *
	 * <p>
	 * Soft, for the same reason supplier categories are: a rule pointing at a
	 * deleted row is either an orphan or, with a cascade, takes the rule with
	 * it — and a rule vanishing means an event quietly stops asking for
	 * something it needs. The office's list shows how many rules use one.
	 */
	@PostMapping(value = "/deleteItem", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage deleteItem(@RequestBody DtoEquipmentItem request) {
		EquipmentItem item = request.getSerEquipmentItemId() == null ? null
				: repositoryEquipmentItem.findById(request.getSerEquipmentItemId()).orElse(null);

		if (item == null) {
			return new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
					"That equipment no longer exists.", null);
		}

		item.setBlnIsDeleted(true);
		item.setBlnIsActive(false);
		stamp(false, item);
		repositoryEquipmentItem.save(item);

		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK,
				"Removed. Rules already written against it keep it.", null);
	}

	// ── the rules ────────────────────────────────────────────────────────

	@PostMapping(value = "/rules", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage rules() {
		List<DtoEquipmentRequirement> rules = new ArrayList<>();
		for (EquipmentRequirement rule : repositoryEquipmentRequirement.findAllForOffice()) {
			rules.add(describe(rule));
		}
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Rules", rules);
	}

	@PostMapping(value = "/saveRule", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage saveRule(@RequestBody DtoEquipmentRequirement request) {
		String refusal = whyRuleIsWrong(request);
		if (refusal != null) {
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, refusal, null);
		}

		EquipmentRequirement rule = request.getSerEquipmentRequirementId() == null ? new EquipmentRequirement()
				: repositoryEquipmentRequirement.findById(request.getSerEquipmentRequirementId()).orElse(null);

		if (rule == null) {
			return new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
					"That rule no longer exists.", null);
		}

		boolean everyEvent = Boolean.TRUE.equals(request.getBlnAppliesToEveryEvent());
		rule.setBlnAppliesToEveryEvent(everyEvent);
		rule.setMenuItem(everyEvent ? null
				: repositoryMenuItem.findById(request.getSerMenuItemId()).orElse(null));
		rule.setEquipmentItem(repositoryEquipmentItem.findById(request.getSerEquipmentItemId()).orElse(null));
		rule.setNumQuantity(request.getNumQuantity());
		rule.setEnmBasis(EnmEquipmentBasis.of(request.getTxtBasis()));
		rule.setNumGuestsPerStation(request.getNumGuestsPerStation());
		rule.setNumSparePercent(request.getNumSparePercent());
		rule.setTxtNotes(trimToNull(request.getTxtNotes()));
		rule.setBlnIsActive(request.getBlnIsActive() == null || request.getBlnIsActive());
		rule.setBlnIsDeleted(false);
		stamp(rule.getSerEquipmentRequirementId() == null, rule);

		if (rule.getEquipmentItem() == null) {
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"That piece of equipment could not be found.", null);
		}
		if (!everyEvent && rule.getMenuItem() == null) {
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"That dish could not be found.", null);
		}

		EquipmentRequirement saved = repositoryEquipmentRequirement.save(rule);
		LOGGER.info("Equipment rule {} saved", saved.getSerEquipmentRequirementId());
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Saved", describe(saved));
	}

	@PostMapping(value = "/deleteRule", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage deleteRule(@RequestBody DtoEquipmentRequirement request) {
		EquipmentRequirement rule = request.getSerEquipmentRequirementId() == null ? null
				: repositoryEquipmentRequirement.findById(request.getSerEquipmentRequirementId()).orElse(null);

		if (rule == null) {
			return new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
					"That rule no longer exists.", null);
		}

		rule.setBlnIsDeleted(true);
		rule.setBlnIsActive(false);
		stamp(false, rule);
		repositoryEquipmentRequirement.save(rule);

		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Removed", null);
	}

	/**
	 * Why the rule will not do, or null if it will.
	 *
	 * <p>
	 * The same checks the database constraints enforce, run here so the office
	 * gets a sentence rather than a constraint violation. The constraints stay
	 * because a check in Java protects one path and a check in the database
	 * protects all of them.
	 */
	private static String whyRuleIsWrong(DtoEquipmentRequirement request) {
		boolean everyEvent = Boolean.TRUE.equals(request.getBlnAppliesToEveryEvent());

		if (!everyEvent && request.getSerMenuItemId() == null) {
			return "Choose a dish, or say the rule applies to every event.";
		}
		if (request.getSerEquipmentItemId() == null) {
			return "Choose the equipment this rule is about.";
		}
		if (request.getNumQuantity() == null || request.getNumQuantity().signum() <= 0) {
			return "How many is needed? A rule for none of something should be deleted instead.";
		}

		EnmEquipmentBasis basis = EnmEquipmentBasis.of(request.getTxtBasis());
		if (basis == null) {
			return "Say whether that is per guest, per table, per station or for the event.";
		}
		if (basis == EnmEquipmentBasis.PER_STATION && request.getNumGuestsPerStation() != null
				&& request.getNumGuestsPerStation() <= 0) {
			return "How many guests does one station serve? Leave it blank for one station however many come.";
		}
		if (request.getNumSparePercent() != null && request.getNumSparePercent().signum() < 0) {
			return "Spares cannot be a negative percentage.";
		}

		return null;
	}

	// ── describing ───────────────────────────────────────────────────────

	private static DtoEquipmentItem describe(EquipmentItem item) {
		DtoEquipmentItem dto = new DtoEquipmentItem();
		dto.setSerEquipmentItemId(item.getSerEquipmentItemId());
		dto.setTxtCode(item.getTxtCode());
		dto.setTxtName(item.getTxtName());
		dto.setTxtDescription(item.getTxtDescription());
		dto.setTxtUnit(item.getTxtUnit());
		dto.setNumDisplayOrder(item.getNumDisplayOrder());
		dto.setBlnIsActive(item.getBlnIsActive());
		if (item.getEquipmentCategory() != null) {
			dto.setSerEquipmentCategoryId(item.getEquipmentCategory().getSerEquipmentCategoryId());
			dto.setTxtCategoryName(item.getEquipmentCategory().getTxtName());
		}
		return dto;
	}

	private static DtoEquipmentRequirement describe(EquipmentRequirement rule) {
		DtoEquipmentRequirement dto = new DtoEquipmentRequirement();
		dto.setSerEquipmentRequirementId(rule.getSerEquipmentRequirementId());
		dto.setBlnAppliesToEveryEvent(rule.getBlnAppliesToEveryEvent());
		dto.setNumQuantity(rule.getNumQuantity());
		dto.setTxtBasis(rule.getEnmBasis() == null ? null : rule.getEnmBasis().name());
		dto.setNumGuestsPerStation(rule.getNumGuestsPerStation());
		dto.setNumSparePercent(rule.getNumSparePercent());
		dto.setTxtNotes(rule.getTxtNotes());
		dto.setBlnIsActive(rule.getBlnIsActive());

		if (rule.getMenuItem() != null) {
			dto.setSerMenuItemId(rule.getMenuItem().getSerMenuItemId());
			dto.setTxtMenuItemName(rule.getMenuItem().getTxtName());
		}
		if (rule.getEquipmentItem() != null) {
			dto.setSerEquipmentItemId(rule.getEquipmentItem().getSerEquipmentItemId());
			dto.setTxtEquipmentItemName(rule.getEquipmentItem().getTxtName());
		}
		return dto;
	}

	private static void stamp(boolean isNew, com.zbs.de.model.BaseEntity entity) {
		if (isNew) {
			entity.setCreatedDate(new Date());
			entity.setCreatedBy(ServiceCurrentUser.getCurrentUserId());
		} else {
			entity.setUpdatedDate(new Date());
			entity.setUpdatedBy(ServiceCurrentUser.getCurrentUserId());
		}
	}

	private static String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
