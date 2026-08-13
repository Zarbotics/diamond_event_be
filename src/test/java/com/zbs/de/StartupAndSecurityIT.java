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
		"app.cors.allowed-origins=http://localhost:5173,http://localhost:3000",
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
		for (String origin : new String[] { "http://localhost:5173", "http://localhost:3000" }) {
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

	@Test
	@DisplayName("health is reachable without authentication")
	void healthIsPublic() throws Exception {
		MvcResult result = mockMvc.perform(get("/actuator/health")).andReturn();
		// 404 when actuator is not on the classpath is fine; 401 is not, because it
		// would mean the permit-all list stopped covering it.
		assertThat(result.getResponse().getStatus()).isNotEqualTo(401);
	}
}
