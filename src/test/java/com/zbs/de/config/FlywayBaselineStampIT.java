package com.zbs.de.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * The Flyway baseline, checked against the database the tests actually use.
 *
 * <p>
 * V1 captures the schema as it really is, taken from a production dump, and it
 * was written after V2 to V9 had already been applied to real databases. That
 * left three different shapes of database in the world, and all three have to
 * start:
 *
 * <ul>
 * <li><strong>Empty</strong> — runs V1 and builds the schema properly.</li>
 * <li><strong>Production</strong> — full schema, no Flyway history at all, so
 * {@code baseline-on-migrate} adopts it at version 1 and skips V1. Running V1
 * there would try to create tables holding live data.</li>
 * <li><strong>Created during this branch's development</strong> — history
 * starting at 2, because V1 did not exist yet. Flyway refuses to start on those
 * without help: <em>"Detected resolved migration not applied to database: 1"</em>.
 * {@link FlywayBaselineStamp} is that help.</li>
 * </ul>
 *
 * <p>
 * The context starting at all is most of what this asserts, and it is not a
 * weak assertion: {@code ddl-auto} is {@code validate} now, so a context that
 * starts is a schema Flyway built and Hibernate agrees with. The rest checks the
 * history table is in the state that makes future migrations behave.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class FlywayBaselineStampIT {

	/*
	 * Static and before anything else, so that a machine with no database skips
	 * this class rather than failing to load the context. Every other
	 * integration test in the suite does the same; this one only guarded inside
	 * connect(), which is far too late — the context is built first.
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

	private Connection connect() throws Exception {
		String url = System.getenv().getOrDefault("TEST_DB_URL",
				"jdbc:postgresql://localhost:5432/diamond_ev_test");
		String user = System.getenv().getOrDefault("TEST_DB_USERNAME", "postgres");
		String password = System.getenv().getOrDefault("TEST_DB_PASSWORD", "postgres");

		try {
			return DriverManager.getConnection(url, user, password);
		} catch (Exception e) {
			Assumptions.abort("No test database at " + url + " — skipping. (" + e.getMessage() + ")");
			throw e;
		}
	}

	@Test
	@DisplayName("the application starts, which means Flyway built a schema Hibernate agrees with")
	void theSchemaValidates() {
		/*
		 * With ddl-auto=validate, reaching this line at all is the assertion.
		 * Hibernate compared every entity against the real schema during startup
		 * and found no missing table, no missing column and no wrong type — the
		 * thing `update` used to paper over by silently adding whatever was
		 * absent.
		 */
		assertThat(true).isTrue();
	}

	@Test
	@DisplayName("version 1 is recorded, however this database came to exist")
	void versionOneIsAccountedFor() throws Exception {
		/*
		 * As a BASELINE on a database that predates V1, or as an ordinary SQL
		 * migration on one built from empty. Either is correct; what must not
		 * happen is neither, because Flyway then refuses to start.
		 */
		try (Connection connection = connect();
				Statement query = connection.createStatement();
				ResultSet found = query.executeQuery(
						"SELECT type FROM flyway_schema_history WHERE version = '1'")) {

			assertThat(found.next())
					.as("nothing records version 1, so Flyway would refuse to start")
					.isTrue();
			assertThat(found.getString("type")).isIn("BASELINE", "SQL");
		}
	}

	@Test
	@DisplayName("the baseline is stamped once, not on every startup")
	void theStampIsNotRepeated() throws Exception {
		// The context has started at least once by now, and the integration
		// suite starts several. A stamp that ran again each time would break the
		// primary key on installed_rank, or quietly fill the table.
		try (Connection connection = connect();
				Statement query = connection.createStatement();
				ResultSet count = query.executeQuery(
						"SELECT COUNT(*) AS n FROM flyway_schema_history WHERE version = '1'")) {

			count.next();
			assertThat(count.getInt("n")).isEqualTo(1);
		}
	}

	@Test
	@DisplayName("every migration in the history succeeded")
	void nothingIsRecordedAsFailed() throws Exception {
		// A failed row left behind blocks every later migration until somebody
		// repairs it, and it is easy to miss in a startup log.
		try (Connection connection = connect();
				Statement query = connection.createStatement();
				ResultSet failed = query.executeQuery(
						"SELECT version FROM flyway_schema_history WHERE success = false")) {

			assertThat(failed.next())
					.as("a failed migration is recorded in the history table")
					.isFalse();
		}
	}

	@Test
	@DisplayName("the schema really was built by the migrations, not by Hibernate")
	void theSchemaCameFromTheMigrations() throws Exception {
		/*
		 * ux_event_master_code comes from V5, and it is the one worth checking:
		 * before V1 existed, V5 ran against a fresh database that had no
		 * event_master yet, so the guard skipped it and the uniqueness index was
		 * silently never created. Its presence means the ordering now works.
		 */
		try (Connection connection = connect();
				Statement query = connection.createStatement();
				ResultSet index = query.executeQuery(
						"SELECT 1 FROM pg_indexes WHERE schemaname = 'public' "
								+ "AND indexname = 'ux_event_master_code'")) {

			assertThat(index.next())
					.as("V5's uniqueness index is missing, so the migrations ran before the tables existed")
					.isTrue();
		}
	}
}
