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

import com.zbs.de.model.dto.menu.DtoCustomerMenuCategory;
import com.zbs.de.model.dto.menu.DtoCustomerMenuSubCategory;
import com.zbs.de.service.ServiceMenuSelection;

/**
 * A removed dish leaves the menu, and takes nothing else with it.
 *
 * <h2>What happened</h2>
 *
 * Deleting one stand emptied the customer's entire food menu.
 *
 * <p>
 * {@code ServiceMenuItemImpl.delete} is a soft delete: it sets
 * {@code bln_is_deleted} and leaves {@code bln_is_active} alone.
 * {@code findByParentId} — the query the menu walk uses for a section's
 * children — filtered on active and <em>not</em> on deleted, so the removed row
 * still came back. The walk then asked {@code getCompositeWithComponents} for
 * it; that lookup does exclude deleted rows, so it threw "Menu item not found";
 * and the controller answered the whole menu as an error.
 *
 * <p>
 * The customer saw no food at all. Nothing on the screen suggested that one
 * removed item was the reason, and the row that caused it was invisible by
 * definition.
 *
 * <h2>What is asserted</h2>
 *
 * Both halves: the deleted dish is gone from the menu, and everything beside it
 * is still there. The second is the one that matters — the first was never in
 * doubt.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class DeletedDishesLeaveTheMenuIT {

	private static final String MARKER = "IT-DEL";

	@Autowired
	private ServiceMenuSelection serviceMenuSelection;

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
	 * A category, a section, a live dish, and a removed stand beside it.
	 *
	 * <p>
	 * The stand is what breaks it: an ordinary deleted dish is merely shown when
	 * it should not be, but a deleted <em>composite</em> sends the walk looking
	 * for something that is not there and the whole request fails.
	 */
	@BeforeEach
	void seedAMenu() throws Exception {
		removeSeed();

		try (Connection connection = connect(); Statement seed = connection.createStatement()) {
			seed.executeUpdate("""
					INSERT INTO menu_item_role (ser_menu_item_role_id, txt_code, txt_name, bln_is_active, bln_is_deleted)
					SELECT 1, '%1$s-ROLE', '%1$s Role', true, false
					WHERE NOT EXISTS (SELECT 1 FROM menu_item_role WHERE ser_menu_item_role_id = 1)
					""".formatted(MARKER));

			seed.executeUpdate("""
					INSERT INTO menu_item (txt_code, txt_name, txt_path, ser_menu_item_role_id,
					                       bln_is_selectable, bln_is_active, bln_is_deleted, num_display_order)
					VALUES ('%1$s-CAT', '%1$s Drinks', 'it_del', 1, false, true, false, 1)
					""".formatted(MARKER));

			seed.executeUpdate("""
					INSERT INTO menu_item (txt_code, txt_name, txt_path, parent_menu_item_id,
					                       bln_is_selectable, bln_is_active, bln_is_deleted, num_display_order)
					VALUES ('%1$s-SEC', '%1$s Soft', 'it_del.s'::ltree,
					        (SELECT ser_menu_item_id FROM menu_item WHERE txt_code = '%1$s-CAT'),
					        false, true, false, 1)
					""".formatted(MARKER));

			/*
			 * The removed stand keeps bln_is_active = true, because that is what
			 * the delete leaves behind. Setting it false here would seed the
			 * state the fault could not occur in.
			 */
			seed.executeUpdate("""
					INSERT INTO menu_item (txt_code, txt_name, txt_path, parent_menu_item_id, bln_is_selectable,
					                       bln_is_composite, bln_is_active, bln_is_deleted, num_display_order, num_price)
					VALUES
					 ('%1$s-LIVE', '%1$s Mango Lassi', 'it_del.s.live'::ltree,
					  (SELECT ser_menu_item_id FROM menu_item WHERE txt_code = '%1$s-SEC'),
					  true, false, true, false, 1, 2.75),
					 ('%1$s-GONE', '%1$s Removed Stand', 'it_del.s.gone'::ltree,
					  (SELECT ser_menu_item_id FROM menu_item WHERE txt_code = '%1$s-SEC'),
					  true, true, true, true, 2, 2.25)
					""".formatted(MARKER));
		}
	}

	@AfterEach
	void removeSeed() throws Exception {
		try (Connection connection = connect(); Statement remove = connection.createStatement()) {
			remove.executeUpdate("DELETE FROM menu_item WHERE txt_code LIKE '" + MARKER + "%'");
			remove.executeUpdate("DELETE FROM menu_item_role WHERE txt_code LIKE '" + MARKER + "%'");
		}
	}

	private DtoCustomerMenuSubCategory theSection() {
		List<DtoCustomerMenuCategory> menu = serviceMenuSelection.getCustomerMenu();

		return menu.stream()
				.filter(c -> (MARKER + " Drinks").equals(c.getCategoryName()))
				.flatMap(c -> c.getSubCategories().stream())
				.findFirst()
				.orElse(null);
	}

	// -----------------------------------------------------------------

	@Test
	@DisplayName("the rest of the menu survives a removed stand")
	void oneRemovedStandDoesNotEmptyTheMenu() {
		/*
		 * The whole fault. Before this, the request threw and the controller
		 * answered the entire menu as an error — every category, every section,
		 * every dish, gone, because one row had been removed.
		 */
		assertThat(serviceMenuSelection.getCustomerMenu())
				.as("a removed stand emptied the whole customer menu")
				.isNotEmpty();

		assertThat(theSection())
				.as("the section holding the removed stand disappeared with it")
				.isNotNull();
	}

	@Test
	@DisplayName("the dish beside it is still on the menu")
	void theLiveDishIsStillThere() {
		// The half that says the fix removed the right thing.
		assertThat(theSection().getItems())
				.extracting("txtName")
				.contains(MARKER + " Mango Lassi");
	}

	@Test
	@DisplayName("the removed stand is not on the menu")
	void theRemovedStandIsGone() {
		assertThat(theSection().getCompositeItems())
				.as("a removed stand was still offered to customers")
				.isEmpty();

		assertThat(theSection().getItems())
				.extracting("txtName")
				.doesNotContain(MARKER + " Removed Stand");
	}
}
