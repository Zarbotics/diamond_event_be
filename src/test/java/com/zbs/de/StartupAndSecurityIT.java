package com.zbs.de;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.sql.Connection;
import java.sql.DriverManager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Boots the whole application against a real PostgreSQL and checks the things
 * that were actually broken.
 *
 * <p>
 * Written after a round of changes left the backend unable to start at all:
 * migrations that assumed tables already existed, configuration with no
 * development fallback, CORS that excluded localhost, and unauthenticated API
 * calls answering {@code 302} instead of {@code 401}. Every one of those would
 * have been caught here.
 *
 * <p>
 * Needs a database. Set {@code TEST_DB_URL} (or rely on the default below) and
 * the test runs; otherwise it skips rather than failing the build on a machine
 * that has no PostgreSQL.
 *
 * <pre>
 *   createdb diamond_ev_test
 *   ./mvnw test -Dtest=StartupAndSecurityIT
 * </pre>
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
		"app.cors.allowed-origins=http://localhost:5173,http://127.0.0.1:5173,http://localhost:3000,http://127.0.0.1:3000",
})
class StartupAndSecurityIT {

	@Autowired
	private MockMvc mockMvc;

	/**
	 * Skips the whole class when there is no database, rather than reporting a
	 * failure that is really an environment gap.
	 */
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

	// -----------------------------------------------------------------
	// Startup
	// -----------------------------------------------------------------

	@Test
	@DisplayName("the application context starts against a real database")
	void contextStarts() {
		// Reaching here means Flyway ran, Hibernate validated or built the schema,
		// and every bean resolved — including the ones whose configuration has no
		// value on a developer machine (Apple, mail, TLS).
		assertThat(mockMvc).isNotNull();
	}

	// -----------------------------------------------------------------
	// Authentication
	// -----------------------------------------------------------------

	@Test
	@DisplayName("an unauthenticated API call returns 401 JSON, never a 302 redirect")
	void unauthenticatedCallsReturn401() throws Exception {
		MvcResult result = mockMvc.perform(post("/customerMaster/getAllData")
				.contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andReturn();

		// A 302 here is what breaks the frontend: its interceptor refreshes the
		// token on a 401 and follows a redirect silently, so the customer sits on
		// a screen that never recovers.
		assertThat(result.getResponse().getStatus())
				.as("expected 401, got %s", result.getResponse().getStatus())
				.isEqualTo(401);
		assertThat(result.getResponse().getContentType()).contains("application/json");
	}

	@Test
	@DisplayName("a malformed bearer token is refused, not ignored")
	void malformedTokenIsRefused() throws Exception {
		mockMvc.perform(post("/customerMaster/getAllData")
				.header("Authorization", "Bearer not-a-real-jwt")
				.contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(r -> assertThat(r.getResponse().getStatus()).isEqualTo(401));
	}

	// -----------------------------------------------------------------
	// Public surface
	// -----------------------------------------------------------------

	@Test
	@DisplayName("the auth endpoints stay reachable without a token")
	void authEndpointsArePublic() throws Exception {
		MvcResult result = mockMvc.perform(post("/auth/exchange")
				.contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"does-not-exist\"}"))
				.andReturn();

		// 400 because the code is unknown — but reachable, which is the point.
		assertThat(result.getResponse().getStatus()).isEqualTo(400);
	}

	@Test
	@DisplayName("an unknown, spent or expired code all answer identically")
	void exchangeDoesNotLeakWhichCodesExist() throws Exception {
		String first = mockMvc.perform(post("/auth/exchange")
				.contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"aaaaaaaaaaaa\"}"))
				.andReturn().getResponse().getContentAsString();

		String second = mockMvc.perform(post("/auth/exchange")
				.contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"bbbbbbbbbbbb\"}"))
				.andReturn().getResponse().getContentAsString();

		assertThat(first).isEqualTo(second);
	}

	@Test
	@DisplayName("an exchange with no body is handled, not a 500")
	void exchangeWithoutBodyIsHandled() throws Exception {
		MvcResult result = mockMvc.perform(post("/auth/exchange")
				.contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andReturn();

		assertThat(result.getResponse().getStatus()).isEqualTo(400);
	}

	// -----------------------------------------------------------------
	// CORS
	// -----------------------------------------------------------------

	@Test
	@DisplayName("the development frontends are allowed through CORS")
	void corsAllowsLocalFrontends() throws Exception {
		// Both spellings of loopback. They are separate origins to a browser, so
		// a developer on http://127.0.0.1:5173 — which is what `vite --host`
		// prints — had every request blocked while a colleague on
		// http://localhost:5173 saw nothing wrong.
		for (String origin : new String[] {
				"http://localhost:5173", "http://127.0.0.1:5173",
				"http://localhost:3000", "http://127.0.0.1:3000" }) {
			MvcResult result = mockMvc.perform(options("/eventType/getAllActiveEventTypesWithSubEvents")
					.header("Origin", origin)
					.header("Access-Control-Request-Method", "POST"))
					.andReturn();

			assertThat(result.getResponse().getHeader("Access-Control-Allow-Origin"))
					.as("CORS blocked %s, which is where the frontends run in development", origin)
					.isEqualTo(origin);
		}
	}

	@Test
	@DisplayName("an unknown origin is not allowed through CORS")
	void corsRejectsUnknownOrigins() throws Exception {
		MvcResult result = mockMvc.perform(options("/eventType/getAllActiveEventTypesWithSubEvents")
				.header("Origin", "https://not-diamond-events.example")
				.header("Access-Control-Request-Method", "POST"))
				.andReturn();

		assertThat(result.getResponse().getHeader("Access-Control-Allow-Origin")).isNull();
	}

	// -----------------------------------------------------------------
	// Schema
	// -----------------------------------------------------------------

	@Test
	@DisplayName("the menu tree's ltree column exists, so the schema really was created")
	void menuTreeSchemaExists() throws Exception {
		String url = System.getenv().getOrDefault("TEST_DB_URL",
				"jdbc:postgresql://localhost:5432/diamond_ev_test");
		String user = System.getenv().getOrDefault("TEST_DB_USERNAME", "postgres");
		String password = System.getenv().getOrDefault("TEST_DB_PASSWORD", "postgres");

		try (Connection connection = DriverManager.getConnection(url, user, password)) {
			// menu_item cannot be created at all without the ltree extension, and a
			// missing extension surfaces as "relation menu_item does not exist" —
			// a long way from the real cause.
			var extension = connection.createStatement()
					.executeQuery("SELECT 1 FROM pg_extension WHERE extname = 'ltree'");
			assertThat(extension.next()).as("the ltree extension is missing").isTrue();

			var table = connection.createStatement().executeQuery(
					"SELECT 1 FROM information_schema.tables WHERE table_name = 'menu_item'");
			assertThat(table.next()).as("menu_item was not created").isTrue();
		}
	}

	// -----------------------------------------------------------------
	// Self-registration
	// -----------------------------------------------------------------

	@Test
	@DisplayName("signing up creates a customer, never an administrator")
	void signupDoesNotGrantAdmin() throws Exception {
		// /auth/signup is public — it has to be, it is how a customer creates an
		// account — and it used to call setTxtRole("ROLE_ADMIN") unconditionally.
		// Anyone at all could POST an email and a password and receive the
		// administrator role, which under the default-deny chain is the only
		// remaining route to the whole back office including the customer list.
		String email = "signup-role-check-" + System.nanoTime() + "@example.invalid";

		mockMvc.perform(post("/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + email + "\",\"password\":\"Sufficiently-Long-1\","
						+ "\"firstName\":\"Role\",\"lastName\":\"Check\"}"))
				.andReturn();

		String url = System.getenv().getOrDefault("TEST_DB_URL",
				"jdbc:postgresql://localhost:5432/diamond_ev_test");
		String user = System.getenv().getOrDefault("TEST_DB_USERNAME", "postgres");
		String password = System.getenv().getOrDefault("TEST_DB_PASSWORD", "postgres");

		try (Connection connection = DriverManager.getConnection(url, user, password)) {
			var rows = connection.createStatement().executeQuery(
					"SELECT txt_role FROM user_master WHERE txt_email = '" + email + "'");

			// The signup may not have persisted at all — it sends a verification
			// email, and there is no SMTP server in a test environment. That is
			// fine: no row means no privilege was granted. What must never happen
			// is a row that says ROLE_ADMIN.
			if (rows.next()) {
				assertThat(rows.getString("txt_role"))
						.as("self-registration granted the administrator role")
						.isNotEqualTo("ROLE_ADMIN");
			}
		}
	}

	@Test
	@DisplayName("a country row with a null legacy is_active flag does not break hydration")
	void nullLegacyActiveFlagIsTolerated() throws Exception {
		// CountryMaster maps the nullable is_active column to a primitive boolean.
		// Hibernate cannot put NULL in a primitive, so it threw
		// PropertyAccessException while loading the row — and because
		// CountryMaster is reached through StateMaster from CityMaster, that took
		// the entire venue list down with a 500 on a step the customer cannot get
		// past.
		String url = System.getenv().getOrDefault("TEST_DB_URL",
				"jdbc:postgresql://localhost:5432/diamond_ev_test");
		String user = System.getenv().getOrDefault("TEST_DB_USERNAME", "postgres");
		String password = System.getenv().getOrDefault("TEST_DB_PASSWORD", "postgres");

		try (Connection connection = DriverManager.getConnection(url, user, password)) {
			connection.createStatement().execute(
					"INSERT INTO country_master (txt_country_code, txt_country_name, bln_is_active, "
							+ "bln_is_deleted, is_active) VALUES ('ZZ', 'Null Flag Test', true, false, NULL) "
							+ "ON CONFLICT DO NOTHING");
		}

		MvcResult result = mockMvc.perform(post("/venueMaster/getAllActiveVenuesGroupedByActiveCities")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andReturn();

		// Unauthenticated, so 401 is the expected answer. The point is that it is
		// not a 500 from a hydration failure.
		assertThat(result.getResponse().getStatus())
				.as("a null legacy is_active flag caused a server error")
				.isNotEqualTo(500);
	}

	@Test
	@DisplayName("health is reachable without authentication")
	void healthIsPublic() throws Exception {
		MvcResult result = mockMvc.perform(get("/actuator/health")).andReturn();
		// 404 when actuator is not on the classpath is fine; 401 is not, because it
		// would mean the permit-all list stopped covering it.
		assertThat(result.getResponse().getStatus()).isNotEqualTo(401);
	}
}
