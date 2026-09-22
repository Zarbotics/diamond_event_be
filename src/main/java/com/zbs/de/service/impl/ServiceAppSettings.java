package com.zbs.de.service.impl;

import java.math.BigDecimal;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zbs.de.model.AppSetting;
import com.zbs.de.repository.RepositoryAppSetting;
import com.zbs.de.util.enums.EnmVatMode;

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

	// ── Pricing ──────────────────────────────────────────────────────────
	//
	// Every default here reproduces what the code did before the pricing
	// engine existed, so that installing it changes no figure on any booking
	// until somebody decides otherwise on the settings screen.

	public static final String PRICING_SERVER_AUTHORITATIVE = "pricing.server.authoritative";
	public static final String PRICING_VAT_MODE = "pricing.vat.mode";
	public static final String PRICING_VAT_PERCENT = "pricing.vat.percent";
	public static final String PRICING_DECOR_CATEGORY_REPLACES = "pricing.decor.category.price.replaces.properties";

	/**
	 * Whether the figures the engine works out are the ones used.
	 *
	 * <p>
	 * Off by default, and deliberately so. The engine computes and records
	 * either way; while this is off, the figures the booking screens send are
	 * still the ones that count. That is what makes it possible to watch the
	 * two agree on real bookings before anything depends on the answer.
	 *
	 * <p>
	 * Turning it on has a second effect worth saying out loud: a customer's
	 * browser stops being able to influence what a booking costs.
	 */
	public boolean isServerPricingAuthoritative() {
		return getBoolean(PRICING_SERVER_AUTHORITATIVE, false);
	}

	/**
	 * How VAT is charged, as the office has set it.
	 *
	 * <p>
	 * An unreadable value falls back to charging by section rather than to
	 * charging nothing. A typo in this setting should not quietly take VAT off
	 * every quote the business issues — the failure that costs money is the one
	 * nobody notices, and an invoice missing its VAT is exactly that.
	 */
	public EnmVatMode getVatMode() {
		String raw = getString(PRICING_VAT_MODE, EnmVatMode.SECTIONS.name());
		EnmVatMode mode = EnmVatMode.of(raw);
		if (mode == null) {
			LOGGER.warn("VAT mode is set to '{}', which is not a mode — charging by section", raw);
			return EnmVatMode.SECTIONS;
		}
		return mode;
	}

	/**
	 * The VAT rate as a percentage.
	 *
	 * <p>
	 * A negative rate would credit every quote, so it is refused rather than
	 * obeyed. Zero is allowed and is not a mistake: a business below the
	 * threshold charges none.
	 */
	public BigDecimal getVatPercent() {
		BigDecimal percent = getDecimal(PRICING_VAT_PERCENT, new BigDecimal("20"));
		if (percent.signum() < 0) {
			LOGGER.warn("VAT is set to {}%, which would credit every quote — using 20%", percent);
			return new BigDecimal("20");
		}
		return percent;
	}

	/**
	 * Whether VAT is charged on one part of the bill.
	 *
	 * <p>
	 * Answers for every mode, not just {@code SECTIONS}, so that no caller has
	 * to remember to check the mode first — {@code TOTAL} says yes to
	 * everything and {@code NONE} says no to everything. A rule that lives in
	 * one place cannot be applied inconsistently in another.
	 *
	 * @param section one of the sections a price line can belong to
	 */
	public boolean isVatChargedOn(String section) {
		switch (getVatMode()) {
			case NONE:
				return false;
			case TOTAL:
				return true;
			case SECTIONS:
			default:
				return getBoolean(vatKeyFor(section), defaultVatFor(section));
		}
	}

	/**
	 * Which setting holds the answer for a section.
	 *
	 * <p>
	 * Décor properties follow their category. They are not a thing the business
	 * sells separately — an option on a stage is part of the stage — so giving
	 * them a switch of their own would invite somebody to set the two
	 * differently and produce a quote where half a stage carried VAT.
	 */
	private static String vatKeyFor(String section) {
		switch (section) {
			case "FOOD":
				return "pricing.vat.on.food";
			case "DECOR":
			case "DECOR_PROPERTY":
				return "pricing.vat.on.decor";
			case "EXTRA":
				return "pricing.vat.on.extras";
			case "SERVICE":
				return "pricing.vat.on.services";
			case "ITINERARY":
				return "pricing.vat.on.itinerary";
			case "SERVING_DISHES":
				return "pricing.vat.on.servingdishes";
			default:
				return "pricing.vat.on." + section.toLowerCase();
		}
	}

	/** Today's behaviour: VAT on décor and extras, and on nothing else. */
	private static boolean defaultVatFor(String section) {
		return "DECOR".equals(section) || "DECOR_PROPERTY".equals(section) || "EXTRA".equals(section);
	}

	/**
	 * Whether a priced décor category swallows the prices of its options.
	 *
	 * <p>
	 * A category can carry a price and so can each option beneath it. Charging
	 * both bills the customer twice for the same stage.
	 */
	public boolean doesDecorCategoryPriceReplaceProperties() {
		return getBoolean(PRICING_DECOR_CATEGORY_REPLACES, true);
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
