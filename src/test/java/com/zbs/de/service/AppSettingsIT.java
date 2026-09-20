package com.zbs.de.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

import com.zbs.de.controller.ControllerAppSetting;
import com.zbs.de.model.AppSetting;
import com.zbs.de.model.dto.DtoAppSetting;
import com.zbs.de.repository.RepositoryAppSetting;
import com.zbs.de.service.impl.ServiceAppSettings;
import com.zbs.de.util.ResponseMessage;

/**
 * The things the business configures.
 *
 * <h2>What is worth pinning</h2>
 *
 * Settings are configuration, not data, and the failure mode that matters is
 * not "the value is wrong" — it is "the value is wrong and something important
 * stopped working because of it". So the two halves pinned here are the two
 * that keep that from happening.
 *
 * <p>
 * Reading never throws. A missing row, a value somebody typed "ten" into, a
 * horizon of zero that would refuse every booking the business has — each one
 * falls back to something workable and says so in the log. The alternative
 * turns a configuration mistake into an outage, and the mistake is already
 * visible on the screen where it was made.
 *
 * <p>
 * Writing refuses early. A value that will not parse is rejected at the screen
 * with a sentence naming the setting, rather than accepted and discovered
 * hours later by whatever finally reads it.
 *
 * <p>
 * And the public list stays public-only. A settings table is exactly the sort
 * of place a connection string ends up in eventually, and the journey reads
 * from it.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class AppSettingsIT {

	private static final String MARKER = "it.test.";

	@Autowired
	private ServiceAppSettings settings;

	@Autowired
	private ControllerAppSetting controller;

	@Autowired
	private RepositoryAppSetting repository;

	@BeforeAll
	static void requireDatabase() {
		String url = System.getenv().getOrDefault("TEST_DB_URL",
				"jdbc:postgresql://localhost:5432/diamond_ev_test");
		String user = System.getenv().getOrDefault("TEST_DB_USERNAME", "postgres");
		String password = System.getenv().getOrDefault("TEST_DB_PASSWORD", "postgres");

		try (Connection ignored = DriverManager.getConnection(url, user, password)) {
			// reachable
		} catch (Exception e) {
			Assumptions.abort("No test database at " + url + " — skipping. (" + e.getMessage() + ")");
		}
	}

	@AfterEach
	void removeSeed() {
		repository.findAll().stream()
				.filter(s -> s.getTxtKey() != null && s.getTxtKey().startsWith(MARKER))
				.forEach(repository::delete);
	}

	// ── reading ──────────────────────────────────────────────────────────

	@Test
	@DisplayName("a setting nobody has added falls back rather than throwing")
	void missingSettingsFallBack() {
		assertThat(settings.getBoolean(MARKER + "nothing.here", true)).isTrue();
		assertThat(settings.getInt(MARKER + "nothing.here", 42)).isEqualTo(42);
		assertThat(settings.getString(MARKER + "nothing.here", "unset")).isEqualTo("unset");
	}

	/**
	 * Not {@code Boolean.parseBoolean}.
	 *
	 * <p>
	 * That answers false to everything it does not recognise, so "yes", "1"
	 * and the typo "treu" would each silently turn a feature off — and the
	 * last of those is one nobody would ever find by reading the table.
	 */
	@Test
	@DisplayName("yes and no are read the way people write them, and a typo does not mean no")
	void booleansAreReadGenerously() {
		for (String yes : new String[] { "true", "TRUE", " true ", "1", "yes" }) {
			seed(MARKER + "flag", yes, "BOOLEAN");
			assertThat(settings.getBoolean(MARKER + "flag", false))
					.as("%s should read as on", yes)
					.isTrue();
			removeSeed();
		}

		seed(MARKER + "flag", "treu", "BOOLEAN");
		assertThat(settings.getBoolean(MARKER + "flag", true))
				.as("a typo must fall back, not quietly mean no")
				.isTrue();
	}

	@Test
	@DisplayName("a number somebody typed words into falls back")
	void unreadableNumbersFallBack() {
		seed(MARKER + "count", "ten", "INTEGER");
		assertThat(settings.getInt(MARKER + "count", 7)).isEqualTo(7);
	}

	/**
	 * A horizon of zero would refuse every booking the business has.
	 *
	 * <p>
	 * The bounds on the row stop it being typed, but a row edited straight in
	 * the database has no such protection, and somebody would find out on a
	 * Saturday.
	 */
	@Test
	@DisplayName("a booking horizon that would refuse everything is ignored")
	void anImpossibleHorizonIsIgnored() {
		seed(ServiceAppSettings.BOOKING_HORIZON_MONTHS + ".unused", "0", "INTEGER");
		// The real key, saved through the repository directly to bypass the screen.
		AppSetting real = repository.findByKey(ServiceAppSettings.BOOKING_HORIZON_MONTHS).orElseThrow();
		String was = real.getTxtValue();
		real.setTxtValue("0");
		repository.saveAndFlush(real);

		try {
			assertThat(settings.getBookingHorizonMonths()).isEqualTo(120);
		} finally {
			real.setTxtValue(was);
			repository.saveAndFlush(real);
		}
	}

	// ── writing ──────────────────────────────────────────────────────────

	@Test
	@DisplayName("a value that will not parse is refused with the setting's own name")
	void badValuesAreRefusedAtTheScreen() {
		AppSetting horizon = repository.findByKey(ServiceAppSettings.BOOKING_HORIZON_MONTHS).orElseThrow();

		DtoAppSetting request = new DtoAppSetting();
		request.setSerSettingId(horizon.getSerSettingId());
		request.setTxtValue("soon");

		ResponseMessage response = controller.save(request);

		assertThat(response.getCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
		assertThat(response.getMessage())
				.contains(horizon.getTxtLabel())
				.contains("whole number");
	}

	@Test
	@DisplayName("a value outside the setting's own bounds is refused")
	void outOfRangeValuesAreRefused() {
		AppSetting horizon = repository.findByKey(ServiceAppSettings.BOOKING_HORIZON_MONTHS).orElseThrow();

		DtoAppSetting request = new DtoAppSetting();
		request.setSerSettingId(horizon.getSerSettingId());
		request.setTxtValue("99999");

		assertThat(controller.save(request).getCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}

	@Test
	@DisplayName("a good value is stored and read back by the service")
	void aGoodValueTakes() {
		AppSetting horizon = repository.findByKey(ServiceAppSettings.BOOKING_HORIZON_MONTHS).orElseThrow();
		String was = horizon.getTxtValue();

		DtoAppSetting request = new DtoAppSetting();
		request.setSerSettingId(horizon.getSerSettingId());
		request.setTxtValue("24");

		try {
			assertThat(controller.save(request).getCode()).isEqualTo(HttpStatus.OK.value());
			assertThat(settings.getBookingHorizonMonths()).isEqualTo(24);
		} finally {
			request.setTxtValue(was);
			controller.save(request);
		}
	}

	// ── who may read what ────────────────────────────────────────────────

	/**
	 * The journey gets the public rows and nothing else.
	 *
	 * <p>
	 * Enforced by its own query rather than by filtering the office's list,
	 * so that adding a private setting cannot start leaking it because
	 * somebody reused the wrong method.
	 */
	@Test
	@DisplayName("the journey's list contains only the settings marked public")
	@SuppressWarnings("unchecked")
	void theJourneySeesOnlyPublicSettings() {
		seed(MARKER + "private.thing", "a secret", "STRING");

		Map<String, String> forJourney = (Map<String, String>) controller.forJourney().getResult();

		assertThat(forJourney).doesNotContainKey(MARKER + "private.thing");
		assertThat(forJourney)
				.as("the journey needs to know whether catering is on sale")
				.containsKey(ServiceAppSettings.CATERING_BOOKING_ENABLED);
	}

	@Test
	@DisplayName("catering is off until somebody says otherwise")
	void cateringIsOffByDefault() {
		assertThat(settings.isCateringBookingEnabled())
				.as("a feature nobody has switched on must not be on")
				.isFalse();
	}

	private void seed(String key, String value, String type) {
		AppSetting setting = repository.findByKey(key).orElseGet(AppSetting::new);
		setting.setTxtKey(key);
		setting.setTxtValue(value);
		setting.setTxtValueType(type);
		setting.setTxtLabel("Test setting");
		setting.setBlnIsPublic(false);
		setting.setBlnIsActive(true);
		setting.setBlnIsDeleted(false);
		setting.setCreatedDate(new Date());
		repository.saveAndFlush(setting);
	}
}
