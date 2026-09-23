package com.zbs.de.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
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

import com.zbs.de.model.AppSetting;
import com.zbs.de.model.dto.DtoAppSetting;
import com.zbs.de.repository.RepositoryAppSetting;
import com.zbs.de.service.impl.ServiceCurrentUser;
import com.zbs.de.util.ResponseMessage;

/**
 * What the business configures.
 *
 * <h3>Two audiences, two endpoints, and the reason they are separate</h3>
 *
 * {@code /all} is the office's screen: every setting, with the type, bounds
 * and wording needed to draw an editor for it. {@code /forJourney} is what a
 * customer-facing caller may read, which is only the rows marked public.
 *
 * <p>
 * The split is enforced by a separate repository query rather than by
 * filtering the office's list, so that adding a private setting cannot start
 * leaking it because somebody reused the wrong method. A settings table is
 * exactly the kind of place a connection string ends up in eventually.
 *
 * <h3>Only the value can be changed</h3>
 *
 * Not the key, the type, the bounds or the wording. Those describe what the
 * setting <em>is</em>, and they belong with the code that reads it — a
 * migration adds a setting, the office decides what it says. Letting the
 * screen edit them would mean somebody could rename a key the application is
 * looking for and turn a feature off by accident.
 */
@RestController
@RequestMapping("/appSetting")
public class ControllerAppSetting {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerAppSetting.class);

	@Autowired
	private RepositoryAppSetting repository;

	/** The office's list, grouped as the screen draws it. */
	@PostMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage all() {
		List<DtoAppSetting> settings = new ArrayList<>();
		for (AppSetting setting : repository.findAllForOffice()) {
			settings.add(describe(setting));
		}

		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Settings", settings);
	}

	/**
	 * What the journey is allowed to know.
	 *
	 * <p>
	 * Returned as a flat map of key to value rather than a list of rows,
	 * because that is what a frontend actually wants — a lookup, not a table.
	 * Nothing here carries the label, the description or the bounds: those are
	 * for the screen that edits settings, and this caller does not edit them.
	 */
	@PostMapping(value = "/forJourney", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage forJourney() {
		Map<String, String> values = new LinkedHashMap<>();
		for (AppSetting setting : repository.findPublic()) {
			values.put(setting.getTxtKey(), setting.getTxtValue());
		}

		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Settings", values);
	}

	/**
	 * Changes one setting's value.
	 *
	 * <p>
	 * Validated against the row's own declared type and bounds, so the refusal
	 * reaches the office as a sentence about what they typed rather than as an
	 * exception somewhere else entirely, hours later, when something finally
	 * reads it.
	 */
	@PostMapping(value = "/save", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage save(@RequestBody DtoAppSetting request) {
		AppSetting setting = request.getSerSettingId() == null ? null
				: repository.findById(request.getSerSettingId()).orElse(null);

		if (setting == null || Boolean.TRUE.equals(setting.getBlnIsDeleted())) {
			return new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
					"That setting no longer exists.", null);
		}

		String value = request.getTxtValue() == null ? "" : request.getTxtValue().trim();
		String refusal = whyValueIsWrong(setting, value);
		if (refusal != null) {
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, refusal, null);
		}

		setting.setTxtValue(value);
		setting.setUpdatedDate(new Date());
		setting.setUpdatedBy(ServiceCurrentUser.getCurrentUserId());
		repository.save(setting);

		LOGGER.info("Setting {} changed to {}", setting.getTxtKey(), value);
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Saved", describe(setting));
	}

	/**
	 * Why the value will not do, or null if it will.
	 *
	 * <p>
	 * Reads from the row rather than from a list of keys, so a setting added
	 * by a later migration is validated without anybody touching this.
	 */
	private static String whyValueIsWrong(AppSetting setting, String value) {
		String type = setting.getTxtValueType() == null ? "STRING" : setting.getTxtValueType();

		switch (type) {
		case "BOOLEAN":
			return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false") ? null
					: "\"" + setting.getTxtLabel() + "\" is a yes or no setting.";

		case "INTEGER":
			try {
				return outsideBounds(setting, new BigDecimal(Long.parseLong(value)));
			} catch (NumberFormatException e) {
				return "\"" + setting.getTxtLabel() + "\" needs a whole number.";
			}

		case "DECIMAL":
			try {
				return outsideBounds(setting, new BigDecimal(value));
			} catch (NumberFormatException e) {
				return "\"" + setting.getTxtLabel() + "\" needs a number.";
			}

		/*
		 * A choice is refused unless it is one of the choices. The VAT mode is
		 * the first of these, and a typo there would take VAT off every invoice
		 * the business issues without a word of complaint — the kind of failure
		 * nobody notices until it is expensive.
		 */
		case "CHOICE":
			for (String allowed : allowedValuesOf(setting)) {
				if (allowed.equalsIgnoreCase(value.trim())) {
					return null;
				}
			}
			return "\"" + setting.getTxtLabel() + "\" has to be one of: "
					+ String.join(", ", allowedValuesOf(setting)) + ".";

		default:
			return null;
		}
	}

	private static List<String> allowedValuesOf(AppSetting setting) {
		if (setting.getTxtAllowedValues() == null || setting.getTxtAllowedValues().isBlank()) {
			return List.of();
		}
		List<String> allowed = new ArrayList<>();
		for (String each : setting.getTxtAllowedValues().split(",")) {
			if (!each.trim().isEmpty()) {
				allowed.add(each.trim());
			}
		}
		return allowed;
	}

	private static String outsideBounds(AppSetting setting, BigDecimal value) {
		if (setting.getNumMin() != null && value.compareTo(setting.getNumMin()) < 0) {
			return "\"" + setting.getTxtLabel() + "\" cannot be less than "
					+ setting.getNumMin().stripTrailingZeros().toPlainString() + ".";
		}
		if (setting.getNumMax() != null && value.compareTo(setting.getNumMax()) > 0) {
			return "\"" + setting.getTxtLabel() + "\" cannot be more than "
					+ setting.getNumMax().stripTrailingZeros().toPlainString() + ".";
		}
		return null;
	}

	private static DtoAppSetting describe(AppSetting setting) {
		DtoAppSetting dto = new DtoAppSetting();
		dto.setSerSettingId(setting.getSerSettingId());
		dto.setTxtKey(setting.getTxtKey());
		dto.setTxtValue(setting.getTxtValue());
		dto.setTxtValueType(setting.getTxtValueType());
		dto.setTxtLabel(setting.getTxtLabel());
		dto.setTxtDescription(setting.getTxtDescription());
		dto.setTxtGroup(setting.getTxtGroup());
		dto.setNumDisplayOrder(setting.getNumDisplayOrder());
		dto.setNumMin(setting.getNumMin());
		dto.setNumMax(setting.getNumMax());
		dto.setTxtAllowedValues(setting.getTxtAllowedValues());
		return dto;
	}
}
