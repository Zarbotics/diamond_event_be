package com.zbs.de.service.impl;

import java.math.BigDecimal;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zbs.de.model.AppSetting;
import com.zbs.de.repository.RepositoryAppSetting;

/**
 * Reading a setting, safely.
 *
 * <h3>Every read carries a fallback, and that is the point</h3>
 *
 * A setting is configuration, not data: the application has to keep working
 * when one is missing, malformed, or has not been deployed yet. So every
 * accessor here takes the value to use when the row cannot be read, and none
 * of them throws.
 *
 * <p>
 * That is deliberate rather than lazy. The alternative — failing a save
 * because somebody typed "ten" into the booking horizon — turns a
 * configuration mistake into an outage, and the mistake is already visible on
 * the screen where it was made. A bad value is logged once and ignored.
 *
 * <h3>Why there is no cache</h3>
 *
 * Because there is no evidence one is needed, and a stale cache on a switch
 * the office has just flicked is worse than a query. These are single-row
 * lookups on a partial unique index against a table with a handful of rows;
 * PostgreSQL will have them in shared buffers. If a hot path ever reads one
 * per request in a loop, cache it then, with a way to clear it.
 */
@Service
public class ServiceAppSettings {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAppSettings.class);

	/** Whether the journey and the office offer catering-only bookings. */
	public static final String CATERING_BOOKING_ENABLED = "catering.booking.enabled";

	/** How far ahead a date may be, in months. */
	public static final String BOOKING_HORIZON_MONTHS = "booking.horizon.months";

	@Autowired
	private RepositoryAppSetting repository;

	/**
	 * @param fallback what to use when the setting is absent or unreadable.
	 */
	public boolean getBoolean(String key, boolean fallback) {
		String raw = rawValueOf(key);
		if (raw == null) {
			return fallback;
		}

		String trimmed = raw.trim();
		/*
		  Deliberately not Boolean.parseBoolean, which answers false to
		  everything it does not recognise — so "yes", "1" and "treu" would all
		  silently turn a feature off, and the last of those is a typo nobody
		  would find.
		*/
		if (trimmed.equalsIgnoreCase("true") || trimmed.equals("1") || trimmed.equalsIgnoreCase("yes")) {
			return true;
		}
		if (trimmed.equalsIgnoreCase("false") || trimmed.equals("0") || trimmed.equalsIgnoreCase("no")) {
			return false;
		}

		LOGGER.warn("Setting {} is not a yes or no ({}), using {}", key, raw, fallback);
		return fallback;
	}

	public int getInt(String key, int fallback) {
		String raw = rawValueOf(key);
		if (raw == null) {
			return fallback;
		}

		try {
			return Integer.parseInt(raw.trim());
		} catch (NumberFormatException e) {
			LOGGER.warn("Setting {} is not a whole number ({}), using {}", key, raw, fallback);
			return fallback;
		}
	}

	public BigDecimal getDecimal(String key, BigDecimal fallback) {
		String raw = rawValueOf(key);
		if (raw == null) {
			return fallback;
		}

		try {
			return new BigDecimal(raw.trim());
		} catch (NumberFormatException e) {
			LOGGER.warn("Setting {} is not a number ({}), using {}", key, raw, fallback);
			return fallback;
		}
	}

	public String getString(String key, String fallback) {
		String raw = rawValueOf(key);
		return raw == null ? fallback : raw;
	}

	/** Whether catering-only bookings are on sale. Off unless somebody says so. */
	public boolean isCateringBookingEnabled() {
		return getBoolean(CATERING_BOOKING_ENABLED, false);
	}

	/**
	 * How far ahead a booking may be taken, in months.
	 *
	 * <p>
	 * Ten years by default — far beyond anything plausible, so it rejects only
	 * the obviously impossible. A value below one month is ignored, because a
	 * horizon of zero refuses every booking the business has and somebody
	 * would find that out on a Saturday.
	 */
	public int getBookingHorizonMonths() {
		int months = getInt(BOOKING_HORIZON_MONTHS, 120);
		if (months < 1) {
			LOGGER.warn("Booking horizon is {} months, which would refuse everything — using 120", months);
			return 120;
		}
		return months;
	}

	private String rawValueOf(String key) {
		try {
			Optional<AppSetting> setting = repository.findByKey(key);
			return setting.map(AppSetting::getTxtValue).orElse(null);
		} catch (Exception e) {
			// The table may not exist yet on a database mid-migration.
			LOGGER.warn("Could not read setting {}: {}", key, e.getMessage());
			return null;
		}
	}
}
