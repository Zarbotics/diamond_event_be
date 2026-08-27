package com.zbs.de.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
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

import com.zbs.de.repository.RepositoryMenuOffering;

/**
 * Stage M2 of the food menu work. See PLATFORM.md §17.
 *
 * <h2>What this stage is</h2>
 *
 * A table and a backfill. Every selectable dish was given one offering of
 * itself, under its current parent, carrying its current price and rule — so
 * the catalogue reads exactly as it did, nothing consumes the new table, and
 * the whole thing is undone by dropping it.
 *
 * <h2>Why this seeds its own catalogue</h2>
 *
 * The first version of this class asserted against whatever the database
 * happened to hold, and the test database holds <em>no menu at all</em>: zero
 * selectable items, so every count was zero and every assertion passed while
 * checking nothing. Deleting an offering by hand did not make it fail.
 *
 * <p>
 * So it seeds a small menu with the shapes that matter — a dish offered in two
 * places, a dish priced with no rule stated — and then runs <strong>the
 * migration's own backfill statement, read out of the migration file</strong>.
 * Reading it rather than restating it is what stops the test drifting away from
 * the thing it is testing: change the migration and this runs the new version.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class MenuOfferingBackfillIT {

	private static final String MARKER = "IT-OFFERING";

	private static final Path MIGRATION = Path.of("src/main/resources/db/migration/V12__menu_offering.sql");

	@Autowired
	private RepositoryMenuOffering repositoryMenuOffering;

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
	 * A menu with the two shapes this stage is about.
	 *
	 * <p>
	 * "Chocolate Brownie" exists twice, once in each section — the duplication
	 * that made the live catalogue 368 rows for 238 dishes. "Sweet Cart" is
	 * priced and says nothing about whether that price is per guest, which is
	 * the case that silently becomes £600 at a three hundred guest wedding.
	 */
	@BeforeEach
	void seedAMenu() throws Exception {
		removeSeed();

		try (Connection connection = connect(); Statement seed = connection.createStatement()) {
			seed.executeUpdate("""
					INSERT INTO menu_item (txt_code, txt_name, txt_path, bln_is_selectable, bln_is_deleted, num_display_order)
					VALUES ('%1$s-CAT', '%1$s Desserts', 'it_offering', false, false, 1)
					""".formatted(MARKER));

			seed.executeUpdate("""
					INSERT INTO menu_item (txt_code, txt_name, txt_path, parent_menu_item_id, bln_is_selectable, bln_is_deleted, num_display_order)
					SELECT '%1$s-SEC-' || s.n, '%1$s Section ' || s.n, ('it_offering.s' || s.n)::ltree,
					       (SELECT ser_menu_item_id FROM menu_item WHERE txt_code = '%1$s-CAT'),
					       false, false, s.n
					FROM generate_series(1, 2) AS s(n)
					""".formatted(MARKER));

			// The same dish in both sections, and a priced dish that has not said
			// whether its price is per guest.
			seed.executeUpdate("""
					INSERT INTO menu_item (txt_code, txt_name, txt_path, parent_menu_item_id, bln_is_selectable,
					                       bln_is_deleted, num_display_order, num_price, enm_price_multiplier_type)
					VALUES
					  ('%1$s-D1', '%1$s Chocolate Brownie', 'it_offering.s1.d1',
					   (SELECT ser_menu_item_id FROM menu_item WHERE txt_code = '%1$s-SEC-1'), true, false, 1, 3.50, 'PER_GUEST'),
					  ('%1$s-D2', '%1$s Chocolate Brownie', 'it_offering.s2.d2',
					   (SELECT ser_menu_item_id FROM menu_item WHERE txt_code = '%1$s-SEC-2'), true, false, 1, 4.00, 'FLAT'),
					  ('%1$s-D3', '%1$s Sweet Cart', 'it_offering.s2.d3',
					   (SELECT ser_menu_item_id FROM menu_item WHERE txt_code = '%1$s-SEC-2'), true, false, 2, 2.00, NULL)
					""".formatted(MARKER));

			seed.executeUpdate(backfillStatement());
		}
	}

	@AfterEach
	void removeSeed() throws Exception {
		try (Connection connection = connect(); Statement remove = connection.createStatement()) {
			remove.executeUpdate("""
					DELETE FROM menu_offering WHERE ser_menu_item_id IN
					  (SELECT ser_menu_item_id FROM menu_item WHERE txt_code LIKE '%s%%')
					""".formatted(MARKER));
			remove.executeUpdate("DELETE FROM menu_item WHERE txt_code LIKE '" + MARKER + "%'");
		}
	}

	/**
	 * The migration's backfill, taken from the migration.
	 *
	 * <p>
	 * Restated here it would be a copy that agrees with the original until
	 * somebody changes one of them, and then quietly stops testing anything.
	 */
	private String backfillStatement() throws Exception {
		String sql = Files.readString(MIGRATION);
		int start = sql.indexOf("INSERT INTO menu_offering");

		assertThat(start)
				.as("the backfill statement is no longer in %s — this test is reading the wrong thing", MIGRATION)
				.isNotNegative();

		return sql.substring(start);
	}

	private long countWhere(String sql) throws Exception {
		try (Connection connection = connect();
				Statement query = connection.createStatement();
				ResultSet result = query.executeQuery(sql)) {
			result.next();
			return result.getLong(1);
		}
	}

	/** Only the dishes this test seeded, so a shared database cannot mask a fault. */
	private long seededSelectableWithNoOffering() throws Exception {
		return countWhere("""
				SELECT COUNT(*) FROM menu_item i
				WHERE i.txt_code LIKE '%s%%'
				  AND i.bln_is_selectable IS TRUE
				  AND i.parent_menu_item_id IS NOT NULL
				  AND NOT EXISTS (SELECT 1 FROM menu_offering o WHERE o.ser_menu_item_id = i.ser_menu_item_id)
				""".formatted(MARKER));
	}

	// -----------------------------------------------------------------

	@Test
	@DisplayName("the application starts, so the entity and the table agree")
	void theSchemaValidates() {
		/*
		 * ddl-auto is validate, so reaching this line means Hibernate compared
		 * MenuOffering against the real table at startup and found every column it
		 * expects, with the types it expects.
		 */
		assertThat(repositoryMenuOffering.count()).isNotNegative();
	}

	@Test
	@DisplayName("every dish a customer can choose is offered somewhere")
	void theBackfillLeftNothingBehind() throws Exception {
		/*
		 * The invariant the later stages stand on. The moment a menu is built from
		 * offerings rather than from parent pointers, a dish with no offering
		 * simply stops existing — silently, which is exactly how four soups came
		 * to sit behind a dessert where no customer has ever seen them.
		 */
		assertThat(seededSelectableWithNoOffering())
				.as("""
						these dishes can be chosen but are offered nowhere, so they will vanish \
						from the menu as soon as anything reads offerings""")
				.isZero();

		assertThat(countWhere("""
				SELECT COUNT(*) FROM menu_offering o
				JOIN menu_item i ON i.ser_menu_item_id = o.ser_menu_item_id
				WHERE i.txt_code LIKE '%s%%'
				""".formatted(MARKER)))
				.as("the backfill created no offerings at all, so nothing above is being tested")
				.isEqualTo(3);
	}

	@Test
	@DisplayName("a section that is not selectable is not offered as a dish")
	void sectionsAreNotOffered() throws Exception {
		// The category and its two sections are places, not things anybody orders.
		// Offering them would put "Desserts" on the menu as something to choose.
		assertThat(countWhere("""
				SELECT COUNT(*) FROM menu_offering o
				JOIN menu_item i ON i.ser_menu_item_id = o.ser_menu_item_id
				WHERE i.txt_code LIKE '%s%%' AND i.bln_is_selectable IS NOT TRUE
				""".formatted(MARKER)))
				.as("a category or section was offered as if it were a dish")
				.isZero();
	}

	@Test
	@DisplayName("no dish is offered twice in the same section")
	void eachOfferingIsUnique() throws Exception {
		// A dish listed twice in one place is a data-entry mistake rather than a
		// menu, and the unique constraint is what stops a re-run of the migration
		// producing one. Asserted by running the backfill a second time.
		try (Connection connection = connect(); Statement again = connection.createStatement()) {
			again.executeUpdate(backfillStatement());
		}

		assertThat(countWhere("""
				SELECT COUNT(*) FROM (
				    SELECT ser_menu_item_id, ser_section_id FROM menu_offering
				    GROUP BY ser_menu_item_id, ser_section_id HAVING COUNT(*) > 1
				) duplicated
				"""))
				.as("re-running the migration created a second set of offerings")
				.isZero();
	}

	@Test
	@DisplayName("an offering carries the terms the dish was already on")
	void theBackfillCopiedTheTerms() throws Exception {
		/*
		 * A backfill that created rows and left them blank would satisfy every
		 * count above and be worth nothing — worse than nothing, because the next
		 * stage would read a price of null and quote somebody zero.
		 *
		 * Both seeded brownies are checked, and they are priced differently on
		 * purpose: £3.50 per guest in one section, £4.00 flat in the other. That
		 * is the case the offering exists for, and a backfill that took the price
		 * from the dish rather than from each placement would collapse them.
		 */
		assertThat(countWhere("""
				SELECT COUNT(*) FROM menu_offering o
				JOIN menu_item i ON i.ser_menu_item_id = o.ser_menu_item_id
				WHERE i.txt_code LIKE '%s%%'
				  AND o.ser_section_id = i.parent_menu_item_id
				  AND (o.num_price IS DISTINCT FROM i.num_price
				       OR o.txt_price_rule IS DISTINCT FROM i.enm_price_multiplier_type)
				""".formatted(MARKER)))
				.as("an offering does not match the dish it was created from")
				.isZero();

		assertThat(countWhere("""
				SELECT COUNT(*) FROM menu_offering o
				JOIN menu_item i ON i.ser_menu_item_id = o.ser_menu_item_id
				WHERE i.txt_code = '%s-D2' AND o.num_price = 4.00 AND o.txt_price_rule = 'FLAT'
				""".formatted(MARKER)))
				.as("the second placement of the same dish did not keep its own price")
				.isEqualTo(1);
	}

	@Test
	@DisplayName("the duplicates are reported, not merged")
	void duplicatesAreOnlyReported() throws Exception {
		/*
		 * Stated as a test so that it is a decision rather than an accident.
		 *
		 * Deciding by name that two rows are the same dish is a judgement — one
		 * "Kheer" may be plated and another part of a set menu, priced differently
		 * on purpose — and a wrong guess silently changes what a customer is
		 * charged. So the migration counts them and stops, and a person merges
		 * them one at a time in the admin screen.
		 */
		List<Object[]> duplicates = repositoryMenuOffering.findDuplicateDishNames();

		assertThat(duplicates)
				.as("the duplicate report is what M4 offers a person; it has to be askable")
				.anyMatch(row -> (MARKER + " Chocolate Brownie").equals(row[0]) && ((Number) row[1]).longValue() == 2);

		assertThat(countWhere("SELECT COUNT(*) FROM menu_item WHERE txt_code LIKE '" + MARKER + "-D%'"))
				.as("the migration merged the duplicate dishes, which is not its decision to take")
				.isEqualTo(3);
	}

	@Test
	@DisplayName("how many priced offerings have not said whether the price is per guest")
	void theSizeOfThePricingGapIsKnown() throws Exception {
		/*
		 * The number M3 has to drive to zero, and the reason it cannot be asserted
		 * as zero yet: 238 of the live items have never said, and
		 * getMenuWithPrices multiplies by the guest count regardless. Every one of
		 * these is a figure a customer could be quoted that nobody decided — a
		 * £2.00 sweet cart becoming £600 at three hundred guests.
		 *
		 * The seeded Sweet Cart is exactly that case, so this asserts the report
		 * finds it rather than merely that the query runs.
		 */
		assertThat(countWhere("""
				SELECT COUNT(*) FROM menu_offering o
				JOIN menu_item i ON i.ser_menu_item_id = o.ser_menu_item_id
				WHERE i.txt_code LIKE '%s%%' AND o.txt_price_rule IS NULL AND o.num_price IS NOT NULL
				""".formatted(MARKER)))
				.as("a priced offering with no rule was not counted, so M3 cannot see the gap it has to close")
				.isEqualTo(1);

		assertThat(repositoryMenuOffering.countPricedOfferingsWithNoRule())
				.as("the same gap, asked the way the application asks it")
				.isPositive();
	}
}
