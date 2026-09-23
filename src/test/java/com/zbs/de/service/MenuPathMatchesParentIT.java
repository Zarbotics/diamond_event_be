package com.zbs.de.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * A menu item's place in the tree, recorded twice, agreeing.
 *
 * <h2>The fault this exists to stop coming back</h2>
 *
 * {@code parent_menu_item_id} is what every query in the application walks.
 * {@code txt_path} is an {@code ltree} saying the same thing, which nothing
 * maintains — and in the live catalogue <strong>fourteen rows disagreed</strong>
 * before V13 repaired them. Eight raitas and chutneys claimed to live under
 * MI_1007 while their parent said MI_1006; six items under Reception Displays
 * claimed to be four levels deep when they were three.
 *
 * <h2>Why it mattered even though nothing read the path</h2>
 *
 * That is the danger rather than the reassurance. The first query written
 * against {@code txt_path} — "everything under Desserts", which is the natural
 * way to ask an ltree — returns the wrong answer for those rows, and returns it
 * confidently. A duplicate representation that nobody checks is a trap laid for
 * whoever writes the query that finally uses it.
 *
 * <p>
 * The column was repaired rather than dropped because it does earn its keep:
 * one operator against an index answers a subtree question that otherwise needs
 * a recursive query. This is what makes keeping it honest instead of
 * decorative.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class MenuPathMatchesParentIT {

	private static final String MARKER = "IT-PATH";

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
		return DriverManager.getConnection(
				System.getenv().getOrDefault("TEST_DB_URL", "jdbc:postgresql://localhost:5432/diamond_ev_test"),
				System.getenv().getOrDefault("TEST_DB_USERNAME", "postgres"),
				System.getenv().getOrDefault("TEST_DB_PASSWORD", "postgres"));
	}

	/**
	 * A three-level subtree, so the assertion is never vacuous.
	 *
	 * <p>
	 * A test database with no menu in it would let an invariant over "every row"
	 * pass by having no rows — the same way the first version of the offering
	 * tests passed while checking nothing.
	 */
	@BeforeEach
	void seedASubtree() throws Exception {
		removeSeed();

		try (Connection connection = connect(); Statement seed = connection.createStatement()) {
			seed.executeUpdate("""
					INSERT INTO menu_item (txt_code, txt_name, txt_path, bln_is_selectable, bln_is_deleted)
					VALUES ('%1$s-ROOT', '%1$s Root', 'it_path', false, false)
					""".formatted(MARKER));
			seed.executeUpdate("""
					INSERT INTO menu_item (txt_code, txt_name, txt_path, parent_menu_item_id, bln_is_selectable, bln_is_deleted)
					VALUES ('%1$s-MID', '%1$s Middle', 'it_path.mid',
					        (SELECT ser_menu_item_id FROM menu_item WHERE txt_code = '%1$s-ROOT'), false, false)
					""".formatted(MARKER));
			seed.executeUpdate("""
					INSERT INTO menu_item (txt_code, txt_name, txt_path, parent_menu_item_id, bln_is_selectable, bln_is_deleted)
					VALUES ('%1$s-LEAF', '%1$s Leaf', 'it_path.mid.leaf',
					        (SELECT ser_menu_item_id FROM menu_item WHERE txt_code = '%1$s-MID'), true, false)
					""".formatted(MARKER));
		}
	}

	@AfterEach
	void removeSeed() throws Exception {
		try (Connection connection = connect(); Statement remove = connection.createStatement()) {
			remove.executeUpdate("""
					DELETE FROM menu_offering WHERE ser_menu_item_id IN
					  (SELECT ser_menu_item_id FROM menu_item WHERE txt_code LIKE '%s%%')
					   OR ser_section_id IN
					  (SELECT ser_menu_item_id FROM menu_item WHERE txt_code LIKE '%s%%')
					""".formatted(MARKER, MARKER));
			// Deepest first: a parent cannot go while a child still points at it.
			remove.executeUpdate("DELETE FROM menu_item WHERE txt_code = '" + MARKER + "-LEAF'");
			remove.executeUpdate("DELETE FROM menu_item WHERE txt_code = '" + MARKER + "-MID'");
			remove.executeUpdate("DELETE FROM menu_item WHERE txt_code = '" + MARKER + "-ROOT'");
		}
	}

	private List<String> disagreeing() throws Exception {
		List<String> found = new ArrayList<>();

		try (Connection connection = connect();
				Statement query = connection.createStatement();
				ResultSet rows = query.executeQuery("""
						SELECT c.ser_menu_item_id, c.txt_name, c.txt_path::text, p.txt_path::text
						FROM menu_item c
						JOIN menu_item p ON p.ser_menu_item_id = c.parent_menu_item_id
						WHERE subpath(c.txt_path, 0, nlevel(c.txt_path) - 1) <> p.txt_path
						ORDER BY c.ser_menu_item_id
						""")) {
			while (rows.next()) {
				found.add("#%d %s: path says %s, parent says %s"
						.formatted(rows.getInt(1), rows.getString(2), rows.getString(3), rows.getString(4)));
			}
		}

		return found;
	}

	private long countWhere(String sql) throws Exception {
		try (Connection connection = connect();
				Statement query = connection.createStatement();
				ResultSet result = query.executeQuery(sql)) {
			result.next();
			return result.getLong(1);
		}
	}

	// -----------------------------------------------------------------

	@Test
	@DisplayName("every item's path agrees with its parent")
	void thePathAndTheParentSayTheSameThing() throws Exception {
		assertThat(disagreeing())
				.as("""
						These items are in two places at once: the parent pointer the application \
						walks says one thing and the ltree path says another. Nothing reads the \
						path today, which is why this is easy to miss and expensive to find — the \
						first subtree query written against it returns the wrong rows and says \
						nothing. Recompute the path from the parent; see V13.""")
				.isEmpty();
	}

	@Test
	@DisplayName("there is a tree here to check, so the assertion is not vacuous")
	void thereIsSomethingToCheck() throws Exception {
		// The failure mode of every invariant over "all rows": no rows, no
		// failures, no value. The seeded subtree guarantees three of them.
		assertThat(countWhere("""
				SELECT COUNT(*) FROM menu_item c
				JOIN menu_item p ON p.ser_menu_item_id = c.parent_menu_item_id
				"""))
				.as("no parented menu items at all, so the check above proved nothing")
				.isGreaterThanOrEqualTo(2);
	}

	@Test
	@DisplayName("a path is a real ltree, so subtree queries can be trusted")
	void theSubtreeQueryFindsTheSubtree() throws Exception {
		/*
		 * The whole reason the column was repaired rather than dropped. If this
		 * cannot be relied on there is no argument for keeping a second
		 * representation of the tree at all.
		 */
		assertThat(countWhere("SELECT COUNT(*) FROM menu_item WHERE txt_path <@ 'it_path'"))
				.as("the ltree operator did not find the seeded subtree")
				.isEqualTo(3);

		assertThat(countWhere("SELECT COUNT(*) FROM menu_item WHERE txt_path <@ 'it_path.mid'"))
				.as("the ltree operator did not find the seeded branch")
				.isEqualTo(2);
	}
}
