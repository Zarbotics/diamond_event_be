package com.zbs.de.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;

import jakarta.annotation.PostConstruct;

/**
 * Refuses to start production on development defaults.
 *
 * <p>
 * Every setting in {@code application.properties} has a development fallback so
 * that a clean checkout runs with nothing but a local database. That
 * convenience is only safe if it cannot reach production by accident — a
 * deploy that quietly falls back to a shared signing secret, or that trusts
 * {@code localhost} as a CORS origin, is worse than a deploy that does not
 * start.
 *
 * <p>
 * Active only under the {@code prod} profile. The messages name the
 * environment variable to set, because the person reading them is usually
 * mid-deploy.
 */
@Configuration
@Profile("prod")
public class ProductionConfigCheck {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductionConfigCheck.class);

	/** The development JWT secret from application.properties. */
	private static final String DEV_JWT_SECRET =
			"ZGV2LW9ubHktc2lnbmluZy1zZWNyZXQtbm90LWZvci1wcm9kdWN0aW9uLXVzZS0xMjM0";

	private static final int MIN_JWT_SECRET_BYTES = 32;

	@Value("${app.jwt.secret:}")
	private String jwtSecret;

	@Value("${app.cors.allowed-origins:}")
	private String corsOrigins;

	@Value("${spring.datasource.url:}")
	private String datasourceUrl;

	@Value("${spring.datasource.password:}")
	private String datasourcePassword;

	@Value("${spring.security.oauth2.client.registration.google.client-id:}")
	private String googleClientId;

	@Value("${spring.jpa.hibernate.ddl-auto:}")
	private String ddlAuto;

	@Value("${apple.enabled:false}")
	private boolean appleEnabled;

	@Value("${apple.team-id:}")
	private String appleTeamId;

	@Value("${apple.key-id:}")
	private String appleKeyId;

	@Value("${apple.private-key-file:}")
	private String applePrivateKeyFile;

	@PostConstruct
	public void verify() {
		List<String> problems = new ArrayList<>();

		// --- Signing secret -------------------------------------------------
		if (isBlank(jwtSecret)) {
			problems.add("JWT_SECRET is not set.");
		} else if (DEV_JWT_SECRET.equals(jwtSecret)) {
			problems.add("JWT_SECRET is still the development default. "
					+ "Generate one with: openssl rand -base64 48");
		} else if (jwtSecret.getBytes().length < MIN_JWT_SECRET_BYTES) {
			problems.add("JWT_SECRET is too short for HS256 — it needs at least "
					+ MIN_JWT_SECRET_BYTES + " bytes.");
		}

		// --- CORS -----------------------------------------------------------
		if (isBlank(corsOrigins)) {
			problems.add("CORS_ALLOWED_ORIGINS is not set.");
		} else if (corsOrigins.contains("localhost") || corsOrigins.contains("127.0.0.1")) {
			problems.add("CORS_ALLOWED_ORIGINS still allows localhost: " + corsOrigins);
		} else if (corsOrigins.contains("*")) {
			problems.add("CORS_ALLOWED_ORIGINS contains a wildcard, which cannot be used "
					+ "with credentialed requests.");
		}

		// --- Database -------------------------------------------------------
		if (isBlank(datasourceUrl)) {
			problems.add("DB_URL is not set.");
		}
		if (isBlank(datasourcePassword)) {
			problems.add("DB_PASSWORD is not set.");
		} else if ("postgres".equals(datasourcePassword) || "abcd".equals(datasourcePassword)) {
			problems.add("DB_PASSWORD is a default or previously-committed value and must be rotated.");
		}

		// --- Google ---------------------------------------------------------
		if (isBlank(googleClientId) || googleClientId.startsWith("local-dev-")) {
			problems.add("GOOGLE_CLIENT_ID is not set to a real client id — sign-in would not work.");
		}

		// --- Apple, only when it is switched on ------------------------------
		// Apple sign-in exists in production only: there is a single developer
		// account and a single signing key. When the profile is on, everything it
		// needs must actually be present, or the failure surfaces to a customer
		// mid-sign-in instead of here.
		if (appleEnabled) {
			if (isBlank(appleTeamId)) {
				problems.add("Apple sign-in is enabled but APPLE_TEAM_ID is not set.");
			}
			if (isBlank(appleKeyId)) {
				problems.add("Apple sign-in is enabled but APPLE_KEY_ID is not set.");
			}
			if (isBlank(applePrivateKeyFile)) {
				problems.add("Apple sign-in is enabled but APPLE_PRIVATE_KEY_FILE is not set.");
			} else if (!Files.isReadable(Path.of(applePrivateKeyFile))) {
				problems.add("Apple sign-in is enabled but the signing key is not readable at "
						+ applePrivateKeyFile);
			}
		}

		// --- Schema ----------------------------------------------------------
		if ("create".equals(ddlAuto) || "create-drop".equals(ddlAuto)) {
			problems.add("DDL_AUTO is '" + ddlAuto + "', which would DROP the production schema.");
		}

		if (!problems.isEmpty()) {
			String message = "Refusing to start in the 'prod' profile:\n"
					+ problems.stream().map(p -> "  - " + p).reduce("", (a, b) -> a + b + "\n")
					+ "\nSee .env.example for the full list of required variables.";
			throw new IllegalStateException(message);
		}
	}

	@EventListener(ApplicationReadyEvent.class)
	void reportPosture() {
		LOGGER.info("Production configuration verified. Apple sign-in: {}. Schema management: {}.",
				appleEnabled ? "enabled" : "disabled", ddlAuto);
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	/** Exposed for testing. */
	static List<String> knownDevDefaults() {
		return Arrays.asList(DEV_JWT_SECRET, "local-dev-google-client-id");
	}
}
