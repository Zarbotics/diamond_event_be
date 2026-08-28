package com.zbs.de.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.zbs.de.model.dto.DtoMenuItem;
import com.zbs.de.service.ServiceMenuItem;

/**
 * {@code /menu/item/tree} returns a tree.
 *
 * <h2>What was wrong</h2>
 *
 * It did not. {@code ServiceTreeUtilityImpl.buildTreeDto} assembled a map of
 * children and then returned the roots <em>without it</em>, and
 * {@code DtoMenuItem} had no {@code children} field to attach them to — so the
 * endpoint answered twelve categories with nothing under any of them, on every
 * call since it was written.
 *
 * <p>
 * Nothing complained, because nothing had ever looked. The menu screen built on
 * this endpoint showed twelve rows and no dishes, which left the flat 436-row
 * list as the only way to see the catalogue — and is most of why the menu
 * screens still felt unchanged after the work meant to replace them.
 *
 * <h2>Why this seeds its own menu</h2>
 *
 * Same reason as the other menu suites: the test database holds no menu, so a
 * test written against "whatever is there" asserts nothing and passes.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class MenuTreeIsATreeIT {

	private static final String MARKER = "IT-TREE";

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

	/**
	 * Three levels, an out-of-order pair, and a deleted section.
	 *
	 * <p>
	 * The deleted one is the case that decides what "root" means: its child is
	 * live, and dropping it silently would hide a dish that is still on the menu
	 * as far as every other query is concerned.
	 */
	@BeforeEach
	void seedAMenu() throws Exception {
		removeSeed();

		try (Connection connection = connect(); Statement seed = connection.createStatement()) {
			seed.executeUpdate("""
					INSERT INTO menu_item (txt_code, txt_name, txt_path, num_display_order,
					                       bln_is_selectable, bln_is_active, bln_is_deleted)
					VALUES ('%1$s-CAT', '%1$s Desserts', 'it_tree', 1, false, true, false)
					""".formatted(MARKER));

			// Second before first, so the ordering is doing something.
			seed.executeUpdate("""
					INSERT INTO menu_item (txt_code, txt_name, txt_path, parent_menu_item_id, num_display_order,
					                       bln_is_selectable, bln_is_active, bln_is_deleted)
					VALUES
					 ('%1$s-SEC-B', '%1$s Stand',  'it_tree.b'::ltree,
					  (SELECT ser_menu_item_id FROM menu_item WHERE txt_code = '%1$s-CAT'), 2, false, true, false),
					 ('%1$s-SEC-A', '%1$s Buffet', 'it_tree.a'::ltree,
					  (SELECT ser_menu_item_id FROM menu_item WHERE txt_code = '%1$s-CAT'), 1, false, true, false),
					 ('%1$s-SEC-X', '%1$s Retired', 'it_tree.x'::ltree,
					  (SELECT ser_menu_item_id FROM menu_item WHERE txt_code = '%1$s-CAT'), 3, false, true, true)
					""".formatted(MARKER));

			seed.executeUpdate("""
					INSERT INTO menu_item (txt_code, txt_name, txt_path, parent_menu_item_id, num_display_order,
					                       bln_is_selectable, bln_is_active, bln_is_deleted, num_price)
					VALUES
					 ('%1$s-D1', '%1$s Brownie', 'it_tree.a.d1'::ltree,
					  (SELECT ser_menu_item_id FROM menu_item WHERE txt_code = '%1$s-SEC-A'), 1, true, true, false, 3.50),
					 ('%1$s-D2', '%1$s Orphan',  'it_tree.x.d2'::ltree,
					  (SELECT ser_menu_item_id FROM menu_item WHERE txt_code = '%1$s-SEC-X'), 1, true, true, false, 4.00)
					""".formatted(MARKER));
		}
	}

	@AfterEach
	void removeSeed() throws Exception {
		try (Connection connection = connect(); Statement remove = connection.createStatement()) {
			remove.executeUpdate("DELETE FROM menu_item WHERE txt_code LIKE '" + MARKER + "%'");
		}
	}

	private DtoMenuItem find(List<DtoMenuItem> where, String name) {
		return where.stream()
				.filter(node -> (MARKER + " " + name).equals(node.getTxtName()))
				.findFirst()
				.orElse(null);
	}

	// -----------------------------------------------------------------

	@Test
	@DisplayName("a category carries the sections under it")
	void theTreeHasBranches() {
		/*
		 * The whole fault, in one assertion. Before this, every root came back
		 * with no children at all and the screen reading it showed twelve rows.
		 */
		DtoMenuItem desserts = find(serviceMenuItem.getTree(), "Desserts");

		assertThat(desserts)
				.as("the seeded category is not in the tree at all")
				.isNotNull();

		assertThat(desserts.getChildren())
				.as("the tree endpoint returned a category with nothing under it — which is what it always did")
				.hasSize(2);
	}

	@Test
	@DisplayName("and the sections carry their dishes")
	void theBranchesReachTheLeaves() {
		// Two levels, not one: a fix that attached children only to roots would
		// pass the test above and still leave every dish invisible.
		DtoMenuItem desserts = find(serviceMenuItem.getTree(), "Desserts");
		DtoMenuItem buffet = find(desserts.getChildren(), "Buffet");

		assertThat(buffet).isNotNull();
		assertThat(buffet.getChildren().stream().map(DtoMenuItem::getTxtName))
				.containsExactly(MARKER + " Brownie");
	}

	@Test
	@DisplayName("children come back in the order somebody set")
	void theOrderIsTheDisplayOrder() {
		/*
		 * Without an explicit sort the order is the map's, which is to say the
		 * ids' — so a section renamed or moved to sort first would still appear
		 * wherever it happened to be created.
		 */
		DtoMenuItem desserts = find(serviceMenuItem.getTree(), "Desserts");

		assertThat(desserts.getChildren().stream().map(DtoMenuItem::getTxtName))
				.containsExactly(MARKER + " Buffet", MARKER + " Stand");
	}

	@Test
	@DisplayName("a deleted section is left out")
	void deletedSectionsAreNotShown() {
		// findAll returns them. Hanging a live dish under a deleted heading puts
		// it on the screen under something nobody can see.
		DtoMenuItem desserts = find(serviceMenuItem.getTree(), "Desserts");

		assertThat(desserts.getChildren().stream().map(DtoMenuItem::getTxtName))
				.doesNotContain(MARKER + " Retired");
	}

	@Test
	@DisplayName("a dish whose section was deleted appears at the top rather than vanishing")
	void anOrphanIsVisible() {
		/*
		 * The reason a root is "one whose parent is not here" rather than "one
		 * with no parent". This dish is live and every other query returns it;
		 * dropping it from the tree would make the screen disagree with the
		 * database about what is on the menu, silently.
		 */
		assertThat(serviceMenuItem.getTree().stream().map(DtoMenuItem::getTxtName))
				.as("a live dish under a deleted section disappeared from the menu entirely")
				.contains(MARKER + " Orphan");
	}
}
