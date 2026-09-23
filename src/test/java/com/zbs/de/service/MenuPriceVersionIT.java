package com.zbs.de.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
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

import com.zbs.de.model.PriceVersion;
import com.zbs.de.repository.RepositoryMenuOfferingPrice;

/**
 * Stage M5 of the food menu work. See PLATFORM.md §17.
 *
 * <h2>What this stage is for</h2>
 *
 * "What did this dish cost when the Khans booked in March?" and "put next
 * year's prices in now, to take effect on the first of April." Neither was
 * possible: an offering carried one price, so changing it overwrote what the old
 * one was, and a price rise had to be typed in on the morning it started.
 *
 * <h2>What is worth asserting</h2>
 *
 * The choosing, mostly. Recording a price against a list is bookkeeping; picking
 * the right list for a given day is where a booking gets quoted the wrong figure
 * — and it is quiet when it goes wrong, because every candidate answer looks
 * like a real price.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class MenuPriceVersionIT {

	private static final String MARKER = "IT-PRICEV";

	@Autowired
	private RepositoryMenuOfferingPrice repositoryMenuOfferingPrice;

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

	private static Date on(String isoDate) {
		return Date.from(LocalDate.parse(isoDate).atStartOfDay(ZoneOffset.UTC).toInstant());
	}

	/**
	 * Four lists with the overlaps that actually happen.
	 *
	 * <p>
	 * A standing open-ended list, next year's not yet published, a short
	 * high-priority Christmas list sitting on top of the standing one, and a
	 * retired one from two years ago. Every rule in {@code findVersionsEffectiveOn}
	 * is exercised by one of them.
	 */
	@BeforeEach
	void seedPriceLists() throws Exception {
		removeSeed();

		try (Connection connection = connect(); Statement seed = connection.createStatement()) {
			seed.executeUpdate("""
					INSERT INTO price_version (txt_version_code, txt_name, dte_effective_from, dte_effective_to,
					                           bln_is_default, num_priority, price_version_status, bln_is_deleted)
					VALUES
					 ('%1$s-STANDING', '%1$s Standing',  TIMESTAMP '2020-01-01', NULL,                     true,  1, 'PUBLISHED', false),
					 ('%1$s-NEXTYEAR', '%1$s Next year', TIMESTAMP '2030-01-01', NULL,                     false, 1, 'DRAFT',     false),
					 ('%1$s-XMAS',     '%1$s Christmas', TIMESTAMP '2027-12-01', TIMESTAMP '2027-12-31',   false, 5, 'PUBLISHED', false),
					 ('%1$s-OLD',      '%1$s Retired',   TIMESTAMP '2019-01-01', TIMESTAMP '2019-12-31',   false, 9, 'RETIRED',   false)
					""".formatted(MARKER));
		}
	}

	@AfterEach
	void removeSeed() throws Exception {
		try (Connection connection = connect(); Statement remove = connection.createStatement()) {
			remove.executeUpdate("""
					DELETE FROM menu_offering_price WHERE ser_price_version_id IN
					  (SELECT ser_price_version_id FROM price_version WHERE txt_version_code LIKE '%s%%')
					""".formatted(MARKER));
			remove.executeUpdate("DELETE FROM price_version WHERE txt_version_code LIKE '" + MARKER + "%'");
		}
	}

	private long countWhere(String sql) throws Exception {
		try (Connection connection = connect();
				Statement query = connection.createStatement();
				ResultSet result = query.executeQuery(sql)) {
			result.next();
			return result.getLong(1);
		}
	}

	private List<String> namesOfVersionsOn(String isoDate) {
		return repositoryMenuOfferingPrice.findVersionsEffectiveOn(on(isoDate)).stream()
				.map(PriceVersion::getTxtVersionCode)
				.filter(code -> code != null && code.startsWith(MARKER))
				.toList();
	}

	// -----------------------------------------------------------------

	@Test
	@DisplayName("the application starts, so the entity and the table agree")
	void theSchemaValidates() {
		/*
		 * ddl-auto is validate, so reaching this line means Hibernate compared
		 * MenuOfferingPrice against the real table at startup and found every
		 * column it expects, with the types it expects.
		 */
		assertThat(repositoryMenuOfferingPrice.count()).isNotNegative();
	}

	@Test
	@DisplayName("the backfill put every offering on a price list")
	void everyOfferingIsPriced() throws Exception {
		/*
		 * The invariant M5b will stand on. An offering absent from every list is
		 * a dish that silently comes to nothing the moment the reads move onto
		 * versions — the same shape of fault as a dish offered nowhere.
		 */
		assertThat(repositoryMenuOfferingPrice.countOfferingsOnNoPriceList())
				.as("""
						these offerings are on no price list at all, so they will come to nothing \
						as soon as prices are read from versions""")
				.isZero();
	}

	@Test
	@DisplayName("an unpriced offering is recorded as unpriced, not left out")
	void unpricedOfferingsAreStillOnTheList() throws Exception {
		/*
		 * 334 of the live catalogue's offerings have no price. Leaving them out
		 * of the list would make "this dish had no price in March"
		 * indistinguishable from "this dish did not exist in March", and the
		 * second of those is a very different conversation with a customer.
		 */
		long offerings = countWhere("SELECT COUNT(*) FROM menu_offering");
		long onTheCurrentList = countWhere("""
				SELECT COUNT(*) FROM menu_offering_price p
				JOIN price_version v ON v.ser_price_version_id = p.ser_price_version_id
				WHERE v.txt_version_code = 'PV-CURRENT'
				""");

		assertThat(onTheCurrentList)
				.as("the backfill skipped offerings — most likely the ones with no price")
				.isEqualTo(offerings);
	}

	@Test
	@DisplayName("the price a list records is the price the offering had")
	void theBackfillCopiedThePrices() throws Exception {
		// A backfill that created rows and left them blank would satisfy every
		// count above and quote everybody nothing.
		assertThat(countWhere("""
				SELECT COUNT(*) FROM menu_offering_price p
				JOIN price_version v ON v.ser_price_version_id = p.ser_price_version_id
				JOIN menu_offering o ON o.ser_offering_id = p.ser_offering_id
				WHERE v.txt_version_code = 'PV-CURRENT'
				  AND (p.num_price IS DISTINCT FROM o.num_price
				       OR p.txt_price_rule IS DISTINCT FROM o.txt_price_rule)
				"""))
				.as("a recorded price does not match the offering it was copied from")
				.isZero();
	}

	@Test
	@DisplayName("an ordinary day is quoted from the standing list")
	void theStandingListApplies() {
		assertThat(namesOfVersionsOn("2027-06-15"))
				.as("no list covers an ordinary day, so nothing could be quoted at all")
				.containsExactly(MARKER + "-STANDING");
	}

	@Test
	@DisplayName("a short list sits on top of the standing one where they overlap")
	void thePriorityListWins() {
		/*
		 * What lets a Christmas list exist without either editing the standing
		 * one or ending it. Both apply in December; the one with the higher
		 * priority is first, and first is what M5b will take.
		 */
		assertThat(namesOfVersionsOn("2027-12-15"))
				.as("the seasonal list did not take precedence over the standing one")
				.containsExactly(MARKER + "-XMAS", MARKER + "-STANDING");
	}

	@Test
	@DisplayName("a draft is never quoted from")
	void draftsAreNotQuotedFrom() {
		/*
		 * The one that costs money. A draft is next year's prices being prepared
		 * — half-entered, unapproved — and quoting a customer from one charges
		 * them a figure nobody has agreed. Dated from 2030 and asked for on a day
		 * it covers, so only its status can be keeping it out.
		 */
		assertThat(namesOfVersionsOn("2030-06-15"))
				.as("a draft price list was offered as something to quote from")
				.doesNotContain(MARKER + "-NEXTYEAR");
	}

	@Test
	@DisplayName("a retired list stays readable but is not quoted from")
	void retiredListsAreNotQuotedFrom() {
		// Kept, because "what did this cost in 2019" is the question the whole
		// stage exists to answer. Not offered, because 2019 is over.
		assertThat(namesOfVersionsOn("2019-06-15"))
				.as("a retired price list was offered as something to quote from")
				.isEmpty();
	}

	@Test
	@DisplayName("a day no list covers gets nothing, rather than the nearest guess")
	void anUncoveredDayGetsNothing() {
		/*
		 * Before the standing list begins. Returning the closest available
		 * version would be the kind of helpfulness that shows up months later as
		 * a quote nobody can explain — and it would hide the real fault, which is
		 * that somebody needs to make a price list for that period.
		 */
		assertThat(namesOfVersionsOn("2015-06-15"))
				.as("a price was offered for a day no list covers")
				.isEmpty();
	}
}
