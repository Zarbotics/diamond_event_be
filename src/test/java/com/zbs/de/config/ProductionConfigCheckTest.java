package com.zbs.de.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * The production configuration gate.
 *
 * <p>
 * Every setting has a development fallback so a clean checkout runs with
 * nothing but a local database. That is only safe if the fallbacks cannot
 * reach production: a deploy that quietly signs tokens with the shared
 * development secret, or that trusts {@code localhost} as a CORS origin, is
 * worse than a deploy that refuses to start.
 */
class ProductionConfigCheckTest {

	private ProductionConfigCheck check;

	@BeforeEach
	void setUp() {
		check = new ProductionConfigCheck();
		// A configuration that should pass, which each test then breaks in one way.
		set("jwtSecret", "a-real-production-secret-long-enough-for-hs256-signing");
		set("corsOrigins", "https://diamondevents.uk");
		set("datasourceUrl", "jdbc:postgresql://db:5432/live_diamond_ev");
		set("datasourcePassword", "a-rotated-production-password");
		set("googleClientId", "910542917624-real.apps.googleusercontent.com");
		set("ddlAuto", "update");
		set("appleEnabled", false);
		set("contextPath", "/api/diamond");
		set("uploadPublicBaseUrl", "https://api.diamondevents.uk/api/diamond/deimg");
	}

	@Test
	@DisplayName("a fully configured production environment starts")
	void validConfigurationPasses() {
		assertThatCode(() -> check.verify()).doesNotThrowAnyException();
	}

	/**
	 * The one that is invisible until a customer sees it.
	 *
	 * <p>
	 * The URL is stored against the picture and handed to the front end, which
	 * is on a different origin. A relative URL resolves against the front end's
	 * own host, where the file does not exist — so every decor photograph, menu
	 * picture and uploaded document 404s, and nothing in the deploy says so.
	 */
	@Test
	@DisplayName("a relative upload URL is refused, because the front end is elsewhere")
	void relativeUploadUrlIsRefused() {
		set("uploadPublicBaseUrl", "/diamond/deimg");

		assertThatThrownBy(() -> check.verify())
				.hasMessageContaining("UPLOAD_PUBLIC_BASE_URL must be absolute");
	}

	/**
	 * And wrong by one path segment is still wrong.
	 *
	 * <p>
	 * The code's default is `/diamond/deimg`, from when the context path was
	 * `/diamond`. A deployment serving on `/api/diamond` that leaves it alone
	 * points every picture at a path nothing is listening on.
	 */
	@Test
	@DisplayName("an upload URL missing the context path is refused")
	void uploadUrlWithoutContextPathIsRefused() {
		set("uploadPublicBaseUrl", "https://api.diamondevents.uk/diamond/deimg");

		assertThatThrownBy(() -> check.verify())
				.hasMessageContaining("does not include the context path");
	}

	@Test
	@DisplayName("an unset upload URL is refused")
	void unsetUploadUrlIsRefused() {
		set("uploadPublicBaseUrl", "");

		assertThatThrownBy(() -> check.verify())
				.hasMessageContaining("UPLOAD_PUBLIC_BASE_URL is not set");
	}

	@Test
	@DisplayName("the development signing secret is refused")
	void refusesDevelopmentJwtSecret() {
		set("jwtSecret", "ZGV2LW9ubHktc2lnbmluZy1zZWNyZXQtbm90LWZvci1wcm9kdWN0aW9uLXVzZS0xMjM0");

		assertThatThrownBy(() -> check.verify())
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("JWT_SECRET")
				// The message has to tell whoever is mid-deploy what to do.
				.hasMessageContaining("openssl rand");
	}

	@Test
	@DisplayName("a signing secret too short for HS256 is refused")
	void refusesShortJwtSecret() {
		set("jwtSecret", "too-short");

		assertThatThrownBy(() -> check.verify()).hasMessageContaining("too short");
	}

	@Test
	@DisplayName("localhost is not an acceptable production CORS origin")
	void refusesLocalhostCors() {
		set("corsOrigins", "https://diamondevents.uk,http://localhost:5173");

		assertThatThrownBy(() -> check.verify()).hasMessageContaining("localhost");
	}

	@Test
	@DisplayName("a wildcard CORS origin is refused")
	void refusesWildcardCors() {
		set("corsOrigins", "*");

		assertThatThrownBy(() -> check.verify()).hasMessageContaining("wildcard");
	}

	@Test
	@DisplayName("the previously committed database password is refused")
	void refusesLeakedDatabasePassword() {
		// The value that sat in application.properties in git history.
		set("datasourcePassword", "abcd");

		assertThatThrownBy(() -> check.verify()).hasMessageContaining("rotated");
	}

	@Test
	@DisplayName("a placeholder Google client id is refused")
	void refusesPlaceholderGoogleClientId() {
		set("googleClientId", "local-dev-google-client-id");

		assertThatThrownBy(() -> check.verify()).hasMessageContaining("GOOGLE_CLIENT_ID");
	}

	@Test
	@DisplayName("a ddl-auto that would drop the schema is refused")
	void refusesDestructiveDdlAuto() {
		set("ddlAuto", "create-drop");

		assertThatThrownBy(() -> check.verify()).hasMessageContaining("DROP");
	}

	// -----------------------------------------------------------------
	// Apple — production only, and only when switched on
	// -----------------------------------------------------------------

	@Test
	@DisplayName("Apple disabled needs no Apple configuration at all")
	void appleDisabledNeedsNothing() {
		set("appleEnabled", false);
		set("appleTeamId", "");
		set("appleKeyId", "");
		set("applePrivateKeyFile", "");

		// This is the development-branch case: Google only, and nothing about
		// Apple should stop the application starting.
		assertThatCode(() -> check.verify()).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("Apple enabled without its signing key is refused at startup, not at sign-in")
	void appleEnabledWithoutKeyIsRefused() {
		set("appleEnabled", true);
		set("appleTeamId", "PG2K36MM95");
		set("appleKeyId", "3HAK9XZ2TV");
		set("applePrivateKeyFile", "/nowhere/AuthKey.p8");

		// Better to fail the deploy than to let a customer discover it halfway
		// through signing in.
		assertThatThrownBy(() -> check.verify())
				.hasMessageContaining("not readable");
	}

	@Test
	@DisplayName("Apple enabled with a readable key passes")
	void appleEnabledWithKeyPasses(@TempDir Path tempDir) throws Exception {
		Path key = tempDir.resolve("AuthKey.p8");
		Files.writeString(key, "-----BEGIN PRIVATE KEY-----\nnot-a-real-key\n-----END PRIVATE KEY-----");

		set("appleEnabled", true);
		set("appleTeamId", "PG2K36MM95");
		set("appleKeyId", "3HAK9XZ2TV");
		set("applePrivateKeyFile", key.toString());

		assertThatCode(() -> check.verify()).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("Apple enabled with a missing team id is refused")
	void appleEnabledWithoutTeamIdIsRefused() {
		set("appleEnabled", true);
		set("appleTeamId", "");
		set("appleKeyId", "3HAK9XZ2TV");
		set("applePrivateKeyFile", "/nowhere/AuthKey.p8");

		assertThatThrownBy(() -> check.verify()).hasMessageContaining("APPLE_TEAM_ID");
	}

	@Test
	@DisplayName("every problem is reported at once, not one per deploy attempt")
	void reportsAllProblemsTogether() {
		set("jwtSecret", "ZGV2LW9ubHktc2lnbmluZy1zZWNyZXQtbm90LWZvci1wcm9kdWN0aW9uLXVzZS0xMjM0");
		set("corsOrigins", "http://localhost:5173");
		set("googleClientId", "local-dev-google-client-id");

		assertThatThrownBy(() -> check.verify())
				.satisfies(e -> {
					String message = e.getMessage();
					assertThat(message).contains("JWT_SECRET");
					assertThat(message).contains("localhost");
					assertThat(message).contains("GOOGLE_CLIENT_ID");
				});
	}

	private void set(String field, Object value) {
		ReflectionTestUtils.setField(check, field, value);
	}
}
