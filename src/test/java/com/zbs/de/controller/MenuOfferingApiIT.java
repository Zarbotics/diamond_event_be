package com.zbs.de.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zbs.de.model.UserMaster;
import com.zbs.de.repository.RepositoryUserMaster;
import com.zbs.de.util.JwtTokenUtil;

/**
 * What the menu editor reads.
 *
 * <h2>Why these endpoints exist</h2>
 *
 * Two of the three are lists of <em>decisions</em> rather than lists of data,
 * and both were invisible before the offering model: which duplicated dishes are
 * really the same dish, and which priced dishes have never said whether the
 * price is per head. The first was unaskable because nothing could express a
 * dish appearing twice; the second was unasked because the pricing code quietly
 * assumed an answer.
 *
 * <h2>What is worth asserting</h2>
 *
 * That they are back-office only — a menu with its costs on it is not something
 * to hand out — and that the duplicate report actually groups, because a report
 * that returns one row per placement makes the reader do the work the screen was
 * supposed to do.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class MenuOfferingApiIT {

	private static final String MARKER = "IT-MENUAPI";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private RepositoryUserMaster repositoryUserMaster;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	private final ObjectMapper json = new ObjectMapper();

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

	/** One dish offered in two sections at two prices, and one priced with no rule. */
	@BeforeEach
	void seedAMenu() throws Exception {
		removeSeed();

		try (Connection connection = connect(); Statement seed = connection.createStatement()) {
			seed.executeUpdate("""
					INSERT INTO menu_item (txt_code, txt_name, txt_path, bln_is_selectable, bln_is_deleted)
					VALUES ('%1$s-CAT', '%1$s Desserts', 'it_menuapi', false, false)
					""".formatted(MARKER));
			seed.executeUpdate("""
					INSERT INTO menu_item (txt_code, txt_name, txt_path, parent_menu_item_id, bln_is_selectable, bln_is_deleted)
					VALUES ('%1$s-S1', '%1$s Buffet', 'it_menuapi.s1',
					        (SELECT ser_menu_item_id FROM menu_item WHERE txt_code='%1$s-CAT'), false, false),
					       ('%1$s-S2', '%1$s Stand', 'it_menuapi.s2',
					        (SELECT ser_menu_item_id FROM menu_item WHERE txt_code='%1$s-CAT'), false, false)
					""".formatted(MARKER));
			seed.executeUpdate("""
					INSERT INTO menu_item (txt_code, txt_name, txt_path, parent_menu_item_id, bln_is_selectable, bln_is_deleted)
					VALUES ('%1$s-D1', '%1$s Brownie', 'it_menuapi.s1.d1',
					        (SELECT ser_menu_item_id FROM menu_item WHERE txt_code='%1$s-S1'), true, false),
					       ('%1$s-D2', '%1$s Brownie', 'it_menuapi.s2.d2',
					        (SELECT ser_menu_item_id FROM menu_item WHERE txt_code='%1$s-S2'), true, false),
					       ('%1$s-D3', '%1$s Sweet Cart', 'it_menuapi.s2.d3',
					        (SELECT ser_menu_item_id FROM menu_item WHERE txt_code='%1$s-S2'), true, false)
					""".formatted(MARKER));
			seed.executeUpdate("""
					INSERT INTO menu_offering (ser_menu_item_id, ser_section_id, num_price, txt_price_rule, num_position)
					VALUES
					 ((SELECT ser_menu_item_id FROM menu_item WHERE txt_code='%1$s-D1'),
					  (SELECT ser_menu_item_id FROM menu_item WHERE txt_code='%1$s-S1'), 3.50, 'PER_GUEST', 1),
					 ((SELECT ser_menu_item_id FROM menu_item WHERE txt_code='%1$s-D2'),
					  (SELECT ser_menu_item_id FROM menu_item WHERE txt_code='%1$s-S2'), 4.00, 'FLAT', 1),
					 ((SELECT ser_menu_item_id FROM menu_item WHERE txt_code='%1$s-D3'),
					  (SELECT ser_menu_item_id FROM menu_item WHERE txt_code='%1$s-S2'), 2.00, NULL, 2)
					""".formatted(MARKER));
		}
	}

	@AfterEach
	void removeSeed() throws Exception {
		try (Connection connection = connect(); Statement remove = connection.createStatement()) {
			remove.executeUpdate("""
					DELETE FROM menu_offering WHERE ser_menu_item_id IN
					  (SELECT ser_menu_item_id FROM menu_item WHERE txt_code LIKE '%1$s%%')
					 OR ser_section_id IN
					  (SELECT ser_menu_item_id FROM menu_item WHERE txt_code LIKE '%1$s%%')
					""".formatted(MARKER));
			remove.executeUpdate("DELETE FROM menu_item WHERE txt_code LIKE '" + MARKER + "-D%'");
			remove.executeUpdate("DELETE FROM menu_item WHERE txt_code LIKE '" + MARKER + "-S%'");
			remove.executeUpdate("DELETE FROM menu_item WHERE txt_code = '" + MARKER + "-CAT'");
		}
	}

	private long dishId(String code) throws Exception {
		try (Connection connection = connect();
				Statement query = connection.createStatement();
				ResultSet found = query.executeQuery(
						"SELECT ser_menu_item_id FROM menu_item WHERE txt_code = '" + code + "'")) {
			found.next();
			return found.getLong(1);
		}
	}

	/**
	 * A member of staff, seeded rather than borrowed.
	 *
	 * <p>
	 * The first version looked for an existing {@code ROLE_ADMIN} row and gave up
	 * with an assumption when it found none — and the test database has none, so
	 * five of these six tests quietly excused themselves while reporting success.
	 * A suite that skips the assertions is worse than one that has none, because
	 * it is counted.
	 */
	private String staffToken() {
		UserMaster admin = repositoryUserMaster.findByTxtEmail(MARKER + "@example.invalid")
				.orElseGet(() -> {
					UserMaster seeded = new UserMaster();
					seeded.setTxtEmail(MARKER + "@example.invalid");
					seeded.setTxtName(MARKER + " administrator");
					seeded.setTxtRole("ROLE_ADMIN");
					seeded.setTxtPassword("not-used-here");
					seeded.setBlnEmailVerified(true);
					seeded.setBlnIsActive(true);
					seeded.setBlnIsDeleted(false);
					return repositoryUserMaster.save(seeded);
				});

		return jwtTokenUtil.generateToken(admin.getSerUserId().intValue(), admin.getTxtEmail(), admin.getTxtRole());
	}

	private MvcResult call(String path, String bearer) throws Exception {
		var request = get(path);
		if (bearer != null) {
			request = request.header("Authorization", "Bearer " + bearer);
		}
		return mockMvc.perform(request).andReturn();
	}

	// -----------------------------------------------------------------

	@Test
	@DisplayName("a menu with its costs on it is not handed out to anybody who asks")
	void theyAreBackOfficeOnly() throws Exception {
		/*
		 * Not a formality. What these return is the catalogue with every price
		 * beside it, which is a competitor's shopping list. The default-deny chain
		 * covers them because they are absent from the allowlists — this is what
		 * proves that is actually so rather than assumed.
		 */
		for (String path : new String[] { "/menu/offerings/duplicates", "/menu/offerings/unstated-price-rule",
				"/menu/offerings/dish/1" }) {
			assertThat(call(path, null).getResponse().getStatus())
					.as("%s answered an unauthenticated caller", path)
					.isIn(401, 403);
		}
	}

	@Test
	@DisplayName("a dish knows everywhere it is offered")
	void aDishKnowsWhereItIsOffered() throws Exception {
		// The question the whole offering model exists to make askable. Before it
		// the answer was "one place, by construction", which is how the catalogue
		// grew five chocolate brownies.
		MvcResult result = call("/menu/offerings/dish/" + dishId(MARKER + "-D1"), staffToken());

		assertThat(result.getResponse().getStatus()).isEqualTo(200);

		JsonNode offerings = json.readTree(result.getResponse().getContentAsString());
		assertThat(offerings).hasSize(1);
		assertThat(offerings.get(0).path("txtSectionName").asText()).isEqualTo(MARKER + " Buffet");
		assertThat(offerings.get(0).path("numPrice").asDouble()).isEqualTo(3.50);
	}

	@Test
	@DisplayName("the duplicate report groups by dish rather than listing placements")
	void duplicatesAreGrouped() throws Exception {
		/*
		 * A report that returned one row per placement would make the reader do
		 * the grouping the screen exists to do — and the question being asked is
		 * "is this one dish or two", which cannot be answered without seeing both
		 * placements together with their prices.
		 */
		MvcResult result = call("/menu/offerings/duplicates", staffToken());

		assertThat(result.getResponse().getStatus()).isEqualTo(200);

		JsonNode seeded = null;
		for (JsonNode dish : json.readTree(result.getResponse().getContentAsString())) {
			if ((MARKER + " Brownie").equals(dish.path("txtName").asText())) {
				seeded = dish;
			}
		}

		assertThat(seeded).as("the duplicated dish was not reported at all").isNotNull();
		assertThat(seeded.path("numCopies").asInt()).isEqualTo(2);
		assertThat(seeded.path("offerings")).hasSize(2);

		// Both prices, because that is the evidence somebody needs to decide
		// whether these are the same dish or two priced differently on purpose.
		assertThat(seeded.path("offerings").findValuesAsText("txtSectionName"))
				.containsExactlyInAnyOrder(MARKER + " Buffet", MARKER + " Stand");
	}

	@Test
	@DisplayName("a dish offered once is not reported as a duplicate")
	void singlePlacementsAreNotReported() throws Exception {
		MvcResult result = call("/menu/offerings/duplicates", staffToken());

		for (JsonNode dish : json.readTree(result.getResponse().getContentAsString())) {
			assertThat(dish.path("txtName").asText())
					.as("a dish that appears once was reported as duplicated")
					.isNotEqualTo(MARKER + " Sweet Cart");
		}
	}

	@Test
	@DisplayName("the priced dishes that never said whether the price is per head are listed")
	void unstatedPriceRulesAreListed() throws Exception {
		/*
		 * Twenty of these in the live catalogue, twelve carrying money, all
		 * charged per guest because that is what the pricing code assumes. The
		 * figures happen to be right; the point is that nobody decided them, and a
		 * number in a log is not a job anybody can finish. This is the list that
		 * makes it one.
		 */
		MvcResult result = call("/menu/offerings/unstated-price-rule", staffToken());

		assertThat(result.getResponse().getStatus()).isEqualTo(200);

		assertThat(json.readTree(result.getResponse().getContentAsString()).findValuesAsText("txtDishName"))
				.as("a priced dish with no stated rule is missing from the list somebody has to work through")
				.contains(MARKER + " Sweet Cart");
	}

	@Test
	@DisplayName("a dish that stated its rule is not on the list")
	void statedRulesAreNotListed() throws Exception {
		// Otherwise the list never empties and stops being a job.
		MvcResult result = call("/menu/offerings/unstated-price-rule", staffToken());

		assertThat(json.readTree(result.getResponse().getContentAsString()).findValuesAsText("txtDishName"))
				.as("a dish that has said how it is priced is still being asked")
				.doesNotContain(MARKER + " Brownie");
	}
}
