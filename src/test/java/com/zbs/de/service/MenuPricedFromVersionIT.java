package com.zbs.de.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.zbs.de.model.dto.DtoMenuPriceCalulationFields;
import com.zbs.de.model.dto.menu.DtoCustomerMenuCategory;
import com.zbs.de.model.dto.menu.DtoCustomerMenuSubCategory;
import com.zbs.de.service.ServiceMenuSelection;

/**
 * Stage M5b of the food menu work. See PLATFORM.md §17.
 *
 * <h2>What changed</h2>
 *
 * Until now the figure charged was {@code menu_item.num_price} — the dish's own
 * price, the same wherever it was offered and whenever the event was. M2 made
 * "a brownie is £3.50 plated and £4.00 on a stand" expressible; M5 made "these
 * are next April's prices" storable. Neither was read. This is where both start
 * being read.
 *
 * <h2>What is worth asserting</h2>
 *
 * Which list is chosen, and that the choice reaches the figure. Every fault in
 * this area is quiet — a wrong price is still a price, and it arrives on a quote
 * looking exactly like a right one — so each test below fixes a different way of
 * choosing wrongly rather than checking the arithmetic, which M3 already covers.
 *
 * <h2>Why it seeds its own catalogue</h2>
 *
 * Same reason as {@code MenuOfferingBackfillIT}: the test database holds no menu
 * at all, so a test written against "whatever is there" passes while checking
 * nothing.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class MenuPricedFromVersionIT {

	private static final String MARKER = "IT-M5B";

	/** Guests, so a per-head price and a flat one cannot be confused. */
	private static final int GUESTS = 100;

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
	 * A category with two sections, one dish offered in both, and two price
	 * lists.
	 *
	 * <p>
	 * The shape is the smallest one that can tell the four ways of pricing
	 * wrongly apart:
	 *
	 * <ul>
	 * <li>the same dish costs £3.50 in the first section and £4.00 in the second
	 * — read the dish instead of the offering and both come out the same;</li>
	 * <li>next year's list charges £5.00 and £6.00 — read today's list for a
	 * September wedding and the customer is quoted this year's;</li>
	 * <li>next year the second section's price becomes a flat hire rather than
	 * per head — read the rule off the dish and it is multiplied by a hundred;
	 * </li>
	 * <li>a second dish is on no list at all — nothing to read, and it must fall
	 * back to its own price rather than to zero.</li>
	 * </ul>
	 */
	@BeforeEach
	void seedAPricedMenu() throws Exception {
		removeSeed();

		try (Connection connection = connect(); Statement seed = connection.createStatement()) {

			/*
			 * Role 1, by that number. The walk starts from
			 * getAllActiveItemsByRoleId(1) — the id is written into the service,
			 * not configured — so a category under any other role is invisible to
			 * it and every assertion below would pass against an empty menu.
			 * Created only if the database has no role 1 of its own.
			 */
			seed.executeUpdate("""
					INSERT INTO menu_item_role (ser_menu_item_role_id, txt_code, txt_name, bln_is_active, bln_is_deleted)
					SELECT 1, '%1$s-ROLE', '%1$s Role', true, false
					WHERE NOT EXISTS (SELECT 1 FROM menu_item_role WHERE ser_menu_item_role_id = 1)
					""".formatted(MARKER));

			// The category. Only this carries the role the walk starts from.
			seed.executeUpdate("""
					INSERT INTO menu_item (txt_code, txt_name, txt_path, ser_menu_item_role_id,
					                       bln_is_selectable, bln_is_active, bln_is_deleted, num_display_order)
					VALUES ('%1$s-CAT', '%1$s Desserts', 'it_m5b', 1, false, true, false, 1)
					""".formatted(MARKER));

			seed.executeUpdate("""
					INSERT INTO menu_item (txt_code, txt_name, txt_path, parent_menu_item_id,
					                       bln_is_selectable, bln_is_active, bln_is_deleted, num_display_order)
					SELECT '%1$s-SEC-' || s.n, '%1$s Section ' || s.n, ('it_m5b.s' || s.n)::ltree,
					       (SELECT ser_menu_item_id FROM menu_item WHERE txt_code = '%1$s-CAT'),
					       false, true, false, s.n
					FROM generate_series(1, 2) AS s(n)
					""".formatted(MARKER));

			/*
			 * One dish, in two sections. Its own price is deliberately different
			 * from both list prices — £9.99 is a figure no correct answer below
			 * can produce, so if it appears the read has fallen back when it
			 * should not have.
			 */
			seed.executeUpdate("""
					INSERT INTO menu_item (txt_code, txt_name, txt_path, parent_menu_item_id, bln_is_selectable,
					                       bln_is_active, bln_is_deleted, num_display_order, num_price,
					                       enm_price_multiplier_type)
					VALUES
					  ('%1$s-D1', '%1$s Brownie', 'it_m5b.s1.d1',
					   (SELECT ser_menu_item_id FROM menu_item WHERE txt_code = '%1$s-SEC-1'),
					   true, true, false, 1, 9.99, 'PER_GUEST'),
					  ('%1$s-D2', '%1$s Brownie', 'it_m5b.s2.d2',
					   (SELECT ser_menu_item_id FROM menu_item WHERE txt_code = '%1$s-SEC-2'),
					   true, true, false, 1, 9.99, 'PER_GUEST'),
					  ('%1$s-D3', '%1$s Unlisted Cake', 'it_m5b.s1.d3',
					   (SELECT ser_menu_item_id FROM menu_item WHERE txt_code = '%1$s-SEC-1'),
					   true, true, false, 2, 7.00, 'FLAT')
					""".formatted(MARKER));

			// The offerings: each dish where it sits.
			seed.executeUpdate("""
					INSERT INTO menu_offering (ser_menu_item_id, ser_section_id, num_price, txt_price_rule,
					                           num_position, bln_is_deleted, bln_is_active)
					SELECT d.ser_menu_item_id, d.parent_menu_item_id, d.num_price,
					       d.enm_price_multiplier_type, 1, false, true
					FROM menu_item d
					WHERE d.txt_code IN ('%1$s-D1', '%1$s-D2', '%1$s-D3')
					""".formatted(MARKER));

			seed.executeUpdate("""
					INSERT INTO price_version (txt_version_code, txt_name, dte_effective_from, dte_effective_to,
					                           bln_is_default, num_priority, price_version_status, bln_is_deleted)
					VALUES
					 ('%1$s-NOW',  '%1$s This year', TIMESTAMP '2020-01-01', TIMESTAMP '2029-12-31', false, 1, 'PUBLISHED', false),
					 ('%1$s-NEXT', '%1$s Next year', TIMESTAMP '2030-01-01', NULL,                   false, 1, 'PUBLISHED', false)
					""".formatted(MARKER));

			// This year: £3.50 plated, £4.00 on the stand, both per head.
			priceOn("NOW", "D1", "3.50", "PER_GUEST");
			priceOn("NOW", "D2", "4.00", "PER_GUEST");

			// Next year: dearer, and the stand becomes a flat hire.
			priceOn("NEXT", "D1", "5.00", "PER_GUEST");
			priceOn("NEXT", "D2", "600.00", "FLAT");

			// D3 is on neither list, on purpose.
		}
	}

	private void priceOn(String version, String dish, String price, String rule) throws Exception {
		try (Connection connection = connect(); Statement seed = connection.createStatement()) {
			seed.executeUpdate("""
					INSERT INTO menu_offering_price (ser_offering_id, ser_price_version_id, num_price, txt_price_rule,
					                                 bln_is_deleted, bln_is_active)
					SELECT o.ser_offering_id,
					       (SELECT ser_price_version_id FROM price_version WHERE txt_version_code = '%1$s-%2$s'),
					       %3$s, '%4$s', false, true
					FROM menu_offering o
					JOIN menu_item d ON d.ser_menu_item_id = o.ser_menu_item_id
					WHERE d.txt_code = '%1$s-%5$s'
					""".formatted(MARKER, version, price, rule, dish));
		}
	}

	@AfterEach
	void removeSeed() throws Exception {
		try (Connection connection = connect(); Statement remove = connection.createStatement()) {
			remove.executeUpdate("""
					DELETE FROM menu_offering_price WHERE ser_offering_id IN
					  (SELECT o.ser_offering_id FROM menu_offering o
					   JOIN menu_item d ON d.ser_menu_item_id = o.ser_menu_item_id
					   WHERE d.txt_code LIKE '%s%%')
					""".formatted(MARKER));
			remove.executeUpdate("DELETE FROM price_version WHERE txt_version_code LIKE '" + MARKER + "%'");
			remove.executeUpdate("""
					DELETE FROM menu_offering WHERE ser_menu_item_id IN
					  (SELECT ser_menu_item_id FROM menu_item WHERE txt_code LIKE '%s%%')
					   OR ser_section_id IN
					  (SELECT ser_menu_item_id FROM menu_item WHERE txt_code LIKE '%s%%')
					""".formatted(MARKER, MARKER));
			remove.executeUpdate("DELETE FROM menu_item WHERE txt_code LIKE '" + MARKER + "%'");
			remove.executeUpdate("DELETE FROM menu_item_role WHERE txt_code LIKE '" + MARKER + "%'");
		}
	}

	// -----------------------------------------------------------------

	/** What the menu says one dish costs, in one of the two sections. */
	private Optional<BigDecimal> priceOf(String dishName, int section, String eventDate) {
		DtoMenuPriceCalulationFields ctx = new DtoMenuPriceCalulationFields();
		ctx.setNumGuests(GUESTS);
		ctx.setNumTables(10);
		ctx.setDteEventDate(eventDate);

		List<DtoCustomerMenuCategory> menu = serviceMenuSelection.getCustomerMenuWithPricing(ctx);

		return menu.stream()
				.filter(c -> (MARKER + " Desserts").equals(c.getCategoryName()))
				.flatMap(c -> c.getSubCategories().stream())
				.filter(s -> (MARKER + " Section " + section).equals(s.getSubCategoryName()))
				.map(DtoCustomerMenuSubCategory::getItems)
				.flatMap(List::stream)
				.filter(i -> (MARKER + " " + dishName).equals(i.getTxtName()))
				.map(i -> i.getNumCalculatedPrice())
				.findFirst();
	}

	@Test
	@DisplayName("the same dish costs different amounts in different sections")
	void theOfferingIsWhatIsPriced() {
		/*
		 * The point of M2, finally being read. Before this stage both of these
		 * came back at the dish's own price, because the dish is what carried it
		 * — the whole distinction existed in the database and nowhere else.
		 */
		assertThat(priceOf("Brownie", 1, "01-06-2026"))
				.as("the plated brownie was not priced from its own offering")
				.contains(new BigDecimal("3.50").multiply(BigDecimal.valueOf(GUESTS)));

		assertThat(priceOf("Brownie", 2, "01-06-2026"))
				.as("the same dish came out at the same price in both sections")
				.contains(new BigDecimal("4.00").multiply(BigDecimal.valueOf(GUESTS)));
	}

	@Test
	@DisplayName("a wedding next year is quoted from next year's list")
	void theEventsDateChoosesTheList() {
		/*
		 * The one the stage exists for, and the one that costs money: a booking
		 * taken today for a date after the new prices start must be quoted at the
		 * new prices, not at today's.
		 */
		assertThat(priceOf("Brownie", 1, "01-06-2031"))
				.as("a 2031 wedding was quoted from the list that ends in 2029")
				.contains(new BigDecimal("5.00").multiply(BigDecimal.valueOf(GUESTS)));
	}

	@Test
	@DisplayName("the rule travels with the price, not with the dish")
	void theRuleComesFromTheListToo() {
		/*
		 * £600 flat next year for what is £4.00 a head this year. Read the rule
		 * off the dish — which still says PER_GUEST — and a hundred guests are
		 * charged sixty thousand pounds. This is why txt_price_rule is stored on
		 * the price and not only on the offering.
		 */
		assertThat(priceOf("Brownie", 2, "01-06-2031"))
				.as("next year's flat hire was multiplied by the guest count")
				.contains(new BigDecimal("600.00"));
	}

	@Test
	@DisplayName("no date given means today's prices, not none")
	void anAbsentDateMeansToday() {
		/*
		 * The journey prices a menu before the customer has chosen a day.
		 * Showing nothing until they do would be a worse answer than showing
		 * this year's — and "this year" is what the seeded NOW list covers.
		 */
		assertThat(priceOf("Brownie", 1, null))
				.as("a menu priced with no date given came back unpriced")
				.contains(new BigDecimal("3.50").multiply(BigDecimal.valueOf(GUESTS)));
	}

	@Test
	@DisplayName("a date that is not a date is treated as no date, not as an error")
	void anUnreadableDateMeansToday() {
		// The menu is a read. Refusing to draw it because a client sent a
		// malformed date would take a screen down over a figure that has a
		// reasonable default; the save path is where a bad date is refused.
		assertThat(priceOf("Brownie", 1, "next Saturday"))
				.contains(new BigDecimal("3.50").multiply(BigDecimal.valueOf(GUESTS)));
	}

	@Test
	@DisplayName("a dish on no price list falls back to its own price rather than to nothing")
	void anUnlistedDishKeepsItsOwnPrice() {
		/*
		 * What makes this stage additive. A dish added since the lists were
		 * built, or one left off a draft, must not silently come to zero on a
		 * customer's quote — it falls back to the catalogue and says so in the
		 * log. That fallback is what M5c removes, once nothing needs it.
		 */
		assertThat(priceOf("Unlisted Cake", 1, "01-06-2026"))
				.as("a dish on no list came out at nothing instead of its own price")
				.contains(new BigDecimal("7.00"));
	}

	@Test
	@DisplayName("a day no published list covers still prices from the catalogue")
	void anUncoveredDayFallsBack() {
		/*
		 * Before either seeded list begins. The choosing is strict on purpose —
		 * findVersionsEffectiveOn returns nothing rather than the nearest guess —
		 * and this is the other half of that decision: nothing to read is not
		 * nothing to charge, it is a warning and the catalogue's own figure.
		 */
		assertThat(priceOf("Brownie", 1, "01-06-2015"))
				.as("a date no list covers produced no price at all")
				.contains(new BigDecimal("9.99").multiply(BigDecimal.valueOf(GUESTS)));
	}
}
