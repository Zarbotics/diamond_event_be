package com.zbs.de.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.zbs.de.service.ServiceMenuItem;

/**
 * The next menu-item code is one nobody has.
 *
 * <h2>What was wrong</h2>
 *
 * {@code generateNextCode} took the highest code by <em>text</em> order and
 * parsed whatever followed its last dash. That is right only while every code
 * with a given prefix is the prefix, a dash and digits — true of production by
 * luck, and false the moment somebody types a code by hand, which the old menu
 * screen let them do.
 *
 * <p>
 * When it was false, the parse failed, the counter restarted at 1, and every
 * call answered the same code. The first dish somebody added saved; the second
 * came back as a 500 with no explanation and nothing on the screen to suggest
 * why. One code like {@code MI-SET-STARTERS} sitting beside {@code MI-1417} was
 * enough to do it.
 *
 * <h2>What is asserted</h2>
 *
 * That two calls in a row differ, and that a code which is not a number does
 * not reset the sequence. Not the formatting — that is cosmetic, and it was
 * never what broke.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class MenuItemCodesAreUniqueIT {

	/** Its own prefix, so it cannot be disturbed by or disturb anything else. */
	private static final String PREFIX = "ITC";

	@Autowired
	private ServiceMenuItem serviceMenuItem;

	@BeforeAll
	static void requireDatabase() {
		try (Connection ignored = DriverManager.getConnection(url(), user(), password())) {
			// reachable
		} catch (Exception e) {
			Assumptions.abort("No test database at " + url() + " — skipping. (" + e.getMessage() + ")");
		}
	}

	private static String url() {
		return System.getenv().getOrDefault("TEST_DB_URL", "jdbc:postgresql://localhost:5432/diamond_ev_test");
	}

	private static String user() {
		return System.getenv().getOrDefault("TEST_DB_USERNAME", "postgres");
	}

	private static String password() {
		return System.getenv().getOrDefault("TEST_DB_PASSWORD", "postgres");
	}

	private Connection connect() throws Exception {
		return DriverManager.getConnection(url(), user(), password());
	}

	private void seed(String... codes) throws Exception {
		try (Connection connection = connect(); Statement insert = connection.createStatement()) {
			for (String code : codes) {
				insert.executeUpdate("""
						INSERT INTO menu_item (txt_code, txt_name, txt_path, bln_is_selectable, bln_is_active, bln_is_deleted)
						VALUES ('%1$s', '%1$s', '%2$s'::ltree, true, true, false)
						""".formatted(code, code.replaceAll("[^A-Za-z0-9]", "_")));
			}
		}
	}

	@BeforeEach
	@AfterEach
	void removeSeed() throws Exception {
		try (Connection connection = connect(); Statement remove = connection.createStatement()) {
			remove.executeUpdate("DELETE FROM menu_item WHERE txt_code LIKE '" + PREFIX + "%'");
		}
	}

	// -----------------------------------------------------------------

	@Test
	@DisplayName("the next code follows the highest number already used")
	void itContinuesTheSequence() throws Exception {
		seed(PREFIX + "-001", PREFIX + "-002", PREFIX + "-003");

		assertThat(serviceMenuItem.generateNextCode(PREFIX)).isEqualTo(PREFIX + "-004");
	}

	@Test
	@DisplayName("a code that is not a number does not reset the sequence")
	void aHandTypedCodeDoesNotResetIt() throws Exception {
		/*
		 * The fault, exactly. Sorted as text, "ITC-STARTERS" beats "ITC-003";
		 * the parser then fails on "STARTERS", restarts at 1, and hands back a
		 * code that already exists.
		 */
		seed(PREFIX + "-001", PREFIX + "-002", PREFIX + "-003", PREFIX + "-STARTERS");

		assertThat(serviceMenuItem.generateNextCode(PREFIX))
				.as("a hand-typed code sent the counter back to the beginning, so this "
						+ "code already belongs to something")
				.isEqualTo(PREFIX + "-004");
	}

	@Test
	@DisplayName("numbers are compared as numbers, not as text")
	void tenIsAfterNine() throws Exception {
		// "ITC-009" sorts after "ITC-010" only if you sort as text, which is
		// how a sequence quietly starts handing out codes it has already given.
		seed(PREFIX + "-009", PREFIX + "-010");

		assertThat(serviceMenuItem.generateNextCode(PREFIX)).isEqualTo(PREFIX + "-011");
	}

	@Test
	@DisplayName("the first code under a prefix nobody has used yet")
	void anUnusedPrefixStartsSomewhere() {
		assertThat(serviceMenuItem.generateNextCode(PREFIX)).isEqualTo(PREFIX + "-001");
	}
}
