package com.zbs.de.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Lets databases that predate the V1 baseline start.
 *
 * <h2>The situation this exists for</h2>
 *
 * V1 captures the schema as it actually is, taken from a production dump. It
 * was written <em>after</em> V2 to V9 had already been applied to real
 * databases, because until then there was no V1 at all — Flyway did not own the
 * schema and {@code ddl-auto=update} created it.
 *
 * <p>
 * Flyway will not tolerate that on its own. A database whose history starts at
 * version 2 now has a resolved migration, V1, that it never applied and never
 * will, and Flyway refuses to start:
 *
 * <pre>Detected resolved migration not applied to database: 1</pre>
 *
 * <h2>Who is affected, and who is not</h2>
 *
 * <ul>
 * <li><strong>Production is not.</strong> It has no {@code flyway_schema_history}
 * at all — these migrations have never been deployed to it — so
 * {@code baseline-on-migrate} stamps it at version 1 by itself and V1 is
 * skipped, exactly as intended.</li>
 * <li><strong>Every developer and CI database is.</strong> They were created
 * while V2 to V9 existed and V1 did not, so their history begins at 2.</li>
 * </ul>
 *
 * <h2>Why automate it rather than write it in a README</h2>
 *
 * The alternative to this class is a one-line SQL statement in the release
 * notes and a hard startup failure for anybody who does not read them, with an
 * error message about resolved migrations that means nothing unless you already
 * know this history. The alternative to <em>both</em> is
 * {@code ignore-migration-patterns=*:ignored}, which would silence this and
 * also silence a genuinely forgotten migration for ever afterwards. That is the
 * one option worth refusing.
 *
 * <p>
 * So: a tightly scoped, self-disabling fix. It acts only when there is a
 * history table, it has no row for version 1, and it does have rows for later
 * versions — which is precisely and only the state this change created. It
 * inserts a baseline marker and nothing else; no DDL is run, skipped or
 * altered. Once stamped, it never acts on that database again.
 *
 * <p>
 * <strong>This is transitional.</strong> It can be deleted once every database
 * that matters has started at least once against this version.
 */
@Configuration
public class FlywayBaselineStamp {

	private static final Logger LOGGER = LoggerFactory.getLogger(FlywayBaselineStamp.class);

	private static final String HISTORY_TABLE = "flyway_schema_history";

	@Bean
	public FlywayMigrationStrategy flywayMigrationStrategy() {
		return flyway -> {
			stampIfNeeded(flyway);
			flyway.migrate();
		};
	}

	private void stampIfNeeded(Flyway flyway) {
		DataSource dataSource = flyway.getConfiguration().getDataSource();
		if (dataSource == null) {
			return;
		}

		try (Connection connection = dataSource.getConnection()) {
			if (!historyExists(connection) || alreadyStamped(connection) || nothingApplied(connection)) {
				return;
			}

			/*
			 * installed_rank is the primary key and Flyway allocates it
			 * ascending from 1, so the baseline goes at one below the lowest —
			 * ordinarily 0. Ordering matters to Flyway's own reporting rather
			 * than to correctness, but a baseline that appears after the
			 * migrations it precedes is a confusing thing to leave behind.
			 */
			try (PreparedStatement insert = connection.prepareStatement("""
					INSERT INTO flyway_schema_history
					    (installed_rank, version, description, type, script,
					     checksum, installed_by, execution_time, success)
					SELECT MIN(installed_rank) - 1, '1', '<< Flyway Baseline >>', 'BASELINE',
					       '<< Flyway Baseline >>', NULL, current_user, 0, true
					FROM flyway_schema_history
					""")) {

				int stamped = insert.executeUpdate();
				if (stamped > 0) {
					LOGGER.info("Stamped this database with the Flyway baseline at version 1. "
							+ "Its schema predates V1, which captures what was already there, "
							+ "so V1 is recorded as applied rather than run.");
				}
			}

		} catch (Exception e) {
			/*
			 * Never fatal. If this cannot run, Flyway's own validation will
			 * refuse to start a moment later with a far more precise message
			 * than anything invented here — and on a healthy database this
			 * class has nothing to do in the first place.
			 */
			LOGGER.warn("Could not check the Flyway baseline: {}", e.getMessage());
		}
	}

	private boolean historyExists(Connection connection) throws Exception {
		try (ResultSet tables = connection.getMetaData()
				.getTables(null, null, HISTORY_TABLE, new String[] { "TABLE" })) {
			return tables.next();
		}
	}

	private boolean alreadyStamped(Connection connection) throws Exception {
		try (Statement query = connection.createStatement();
				ResultSet found = query.executeQuery(
						"SELECT 1 FROM " + HISTORY_TABLE + " WHERE version = '1' LIMIT 1")) {
			return found.next();
		}
	}

	/**
	 * An empty history table means a database in the middle of its very first
	 * migration run, which needs no stamping — V1 is about to be applied to it
	 * properly.
	 */
	private boolean nothingApplied(Connection connection) throws Exception {
		try (Statement query = connection.createStatement();
				ResultSet found = query.executeQuery(
						"SELECT 1 FROM " + HISTORY_TABLE + " LIMIT 1")) {
			return !found.next();
		}
	}
}
