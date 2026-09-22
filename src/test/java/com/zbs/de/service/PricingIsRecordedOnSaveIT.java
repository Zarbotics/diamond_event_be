package com.zbs.de.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import com.zbs.de.model.CustomerMaster;
import com.zbs.de.model.DecorCategoryMaster;
import com.zbs.de.model.EventMaster;
import com.zbs.de.model.EventType;
import com.zbs.de.model.MenuItem;
import com.zbs.de.model.dto.DtoEventDecorCategorySelection;
import com.zbs.de.model.dto.DtoEventMaster;
import com.zbs.de.model.dto.DtoMenuItem;
import com.zbs.de.model.dto.menu.DtoCustomerMenuCategory;
import com.zbs.de.model.dto.menu.DtoCustomerMenuSubCategory;
import com.zbs.de.repository.RepositoryCustomerMaster;
import com.zbs.de.repository.RepositoryDecorCategoryMaster;
import com.zbs.de.repository.RepositoryEventMaster;
import com.zbs.de.repository.RepositoryEventType;
import com.zbs.de.repository.RepositoryMenuItem;
import com.zbs.de.util.enums.EnmPriceMultiplierType;

/**
 * Saving a booking prices it.
 *
 * <h2>What this pins</h2>
 *
 * That the price is worked out and written down as part of the save, which is
 * the whole point of the change. Every figure on a booking used to arrive from
 * a browser; the journey never sent a total at all, so a customer's enquiry was
 * stored quoted at zero and re-priced by hand afterwards.
 *
 * <h2>Why it goes through the real save rather than calling the engine</h2>
 *
 * Because the engine being right is already covered by unit tests, and it is
 * not what was in doubt. What was in doubt is whether the engine can see the
 * booking at the moment it is asked: the menu is saved three levels deep, and
 * pricing the entity still in hand rather than the one in the database would
 * miss every dish just chosen and quietly return zero for the food.
 *
 * <p>
 * A test that called the engine directly with a hand-built event would have
 * passed throughout that fault.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class PricingIsRecordedOnSaveIT {

	private static final String MARKER = "IT-PRICING";
	private static final LocalDate DAY = LocalDate.now(ZoneOffset.UTC).plusYears(8);
	private static final String DAY_AS_POSTED = DAY.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

	@Autowired
	private ServiceEventMaster serviceEventMaster;

	@Autowired
	private RepositoryEventMaster repositoryEventMaster;

	@Autowired
	private RepositoryCustomerMaster repositoryCustomerMaster;

	@Autowired
	private RepositoryEventType repositoryEventType;

	@Autowired
	private RepositoryMenuItem repositoryMenuItem;

	@Autowired
	private RepositoryDecorCategoryMaster repositoryDecorCategoryMaster;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private CustomerMaster customer;
	private EventType eventType;

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

	@AfterEach
	void removeSeed() {
		String mine = "  SELECT e.ser_event_master_id FROM event_master e"
				+ "  JOIN customer_master c ON c.ser_cust_id = e.ser_cust_id"
				+ "  WHERE c.txt_cust_code LIKE ?";

		jdbcTemplate.update("DELETE FROM event_price_line WHERE ser_event_master_id IN (" + mine + ")",
				MARKER + "%");
		jdbcTemplate.update("DELETE FROM event_menu_food_selection WHERE ser_event_master_id IN (" + mine + ")",
				MARKER + "%");
		jdbcTemplate.update("DELETE FROM event_menu_subcategory_selection WHERE ser_event_menu_category_id IN ("
				+ "  SELECT c.ser_event_menu_category_id FROM event_menu_category_selection c"
				+ "  WHERE c.ser_event_master_id IN (" + mine + "))", MARKER + "%");
		jdbcTemplate.update("DELETE FROM event_menu_category_selection WHERE ser_event_master_id IN (" + mine + ")",
				MARKER + "%");
		jdbcTemplate.update("DELETE FROM event_decor_category_selection WHERE ser_event_master_id IN (" + mine + ")",
				MARKER + "%");
		jdbcTemplate.update("DELETE FROM event_payment WHERE ser_event_master_id IN (" + mine + ")", MARKER + "%");
		jdbcTemplate.update("DELETE FROM event_payment WHERE ser_event_id IN (" + mine + ")", MARKER + "%");
		jdbcTemplate.update("DELETE FROM event_budget WHERE ser_event_master_id IN (" + mine + ")", MARKER + "%");
		jdbcTemplate.update("DELETE FROM event_quote WHERE ser_event_master_id IN (" + mine + ")", MARKER + "%");
		jdbcTemplate.update("DELETE FROM event_master WHERE ser_cust_id IN ("
				+ "  SELECT ser_cust_id FROM customer_master WHERE txt_cust_code LIKE ?)", MARKER + "%");
		jdbcTemplate.update("DELETE FROM customer_master WHERE txt_cust_code LIKE ?", MARKER + "%");
		jdbcTemplate.update("DELETE FROM menu_item WHERE txt_code LIKE ?", MARKER + "%");
		jdbcTemplate.update("DELETE FROM decor_category_master WHERE txt_decor_category_code LIKE ?", MARKER + "%");
	}

	/**
	 * The enquiry that used to be quoted at zero.
	 *
	 * <p>
	 * A customer chooses two dishes at £25 a head for 200 people. Before this,
	 * the journey posted no total and the booking was stored quoted at nothing.
	 */
	@Test
	@DisplayName("a customer's enquiry is priced when it is saved")
	void theJourneysEnquiryIsPriced() throws Exception {
		EventMaster seeded = seedEvent();

		DtoEventMaster dto = journeySave(seeded);
		dto.setMenuCategoriesSelection(aMenuAt("25.00", EnmPriceMultiplierType.PER_GUEST, null));
		serviceEventMaster.saveAndUpdateWithDocs(dto, null);

		assertThat(calculatedTotalOf(seeded))
				.as("200 guests × two dishes at £25 = £10,000, and the food carries no VAT by default")
				.isEqualByComparingTo("10000.00");
	}

	/**
	 * The working is kept, not just the answer.
	 *
	 * <p>
	 * A total nobody can take apart is a total nobody can defend to a customer
	 * who queries it.
	 */
	@Test
	@DisplayName("the working behind the total is written down")
	void theWorkingIsKept() throws Exception {
		EventMaster seeded = seedEvent();

		DtoEventMaster dto = journeySave(seeded);
		dto.setMenuCategoriesSelection(aMenuAt("25.00", EnmPriceMultiplierType.PER_GUEST, null));
		serviceEventMaster.saveAndUpdateWithDocs(dto, null);

		List<Map<String, Object>> lines = priceLinesOf(seeded);
		assertThat(lines).as("two dishes should have produced two lines").hasSize(2);
		assertThat(lines).allSatisfy(line -> {
			assertThat(line.get("txt_section")).isEqualTo("FOOD");
			assertThat(String.valueOf(line.get("txt_reason")))
					.as("every figure has to carry its reason")
					.isEqualTo("£25.00 per guest × 200 guests");
		});
	}

	/**
	 * Décor is priced too, and carries VAT where food does not.
	 *
	 * <p>
	 * The default the business runs on today: 20% on décor, nothing on food.
	 */
	@Test
	@DisplayName("décor is priced and carries VAT, food does not")
	void decorCarriesVat() throws Exception {
		EventMaster seeded = seedEvent();

		DtoEventMaster dto = journeySave(seeded);
		dto.setDtoEventDecorSelections(decorAt("1000.00"));
		serviceEventMaster.saveAndUpdateWithDocs(dto, null);

		Map<String, Object> budget = budgetOf(seeded);
		assertThat(asDecimal(budget.get("num_calculated_decor"))).isEqualByComparingTo("1000.00");
		assertThat(asDecimal(budget.get("num_calculated_vat")))
				.as("20% of the décor")
				.isEqualByComparingTo("200.00");
		assertThat(asDecimal(budget.get("num_calculated_total"))).isEqualByComparingTo("1200.00");
	}

	/**
	 * Saving twice does not double the working.
	 *
	 * <p>
	 * The lines are the current answer, rewritten each time, not a history. A
	 * booking saved four times would otherwise carry four sets of them with
	 * nothing able to tell which was current.
	 */
	@Test
	@DisplayName("saving again replaces the working rather than adding to it")
	void theWorkingIsReplaced() throws Exception {
		EventMaster seeded = seedEvent();

		DtoEventMaster first = journeySave(seeded);
		first.setMenuCategoriesSelection(aMenuAt("25.00", EnmPriceMultiplierType.PER_GUEST, null));
		serviceEventMaster.saveAndUpdateWithDocs(first, null);
		assertThat(priceLinesOf(seeded)).hasSize(2);

		DtoEventMaster again = journeySave(reload(seeded));
		again.setMenuCategoriesSelection(aMenuAt("25.00", EnmPriceMultiplierType.PER_GUEST, null));
		serviceEventMaster.saveAndUpdateWithDocs(again, null);

		assertThat(priceLinesOf(seeded))
				.as("a second save left the first set of lines behind")
				.hasSize(2);
	}

	/**
	 * The office's save prices the booking the same way.
	 *
	 * <p>
	 * Both portals through one engine is the point. The two used to compute
	 * prices separately, in different languages, with no way to compare them.
	 */
	@Test
	@DisplayName("the office's save prices the booking too")
	void theOfficeSavePricesToo() throws Exception {
		EventMaster seeded = seedEvent();

		com.zbs.de.model.dto.DtoEventMasterAdminPortal dto = new com.zbs.de.model.dto.DtoEventMasterAdminPortal();
		dto.setSerEventMasterId(seeded.getSerEventMasterId());
		dto.setSerCustId(customer.getSerCustId());
		dto.setSerEventTypeId(eventType.getSerEventTypeId());
		dto.setIsEditAllowed(true);
		dto.setNumVersion(seeded.getNumVersion());
		dto.setDteEventDate(DAY_AS_POSTED);
		dto.setNumNumberOfGuests(200);
		dto.setNumNumberOfTables(20);
		dto.setDtoEventDecorSelections(decorAt("500.00"));

		serviceEventMaster.saveAndUpdateWithDocsAdminPortal(dto, null);

		assertThat(calculatedTotalOf(seeded))
				.as("£500 of décor plus 20% VAT")
				.isEqualByComparingTo("600.00");
	}

	/**
	 * The figures the screens send are still the ones used.
	 *
	 * <p>
	 * Until {@code pricing.server.authoritative} is switched on. This is the
	 * guarantee that installing the engine changes no invoice: it computes,
	 * records, and stays out of the way.
	 */
	@Test
	@DisplayName("the engine records its answer without yet imposing it")
	void theEngineDoesNotYetOverrule() throws Exception {
		EventMaster seeded = seedEvent();

		DtoEventMaster dto = journeySave(seeded);
		dto.setDtoEventDecorSelections(decorAt("1000.00"));
		serviceEventMaster.saveAndUpdateWithDocs(dto, null);

		Map<String, Object> budget = budgetOf(seeded);
		assertThat(asDecimal(budget.get("num_calculated_total")))
				.as("the engine's answer is recorded")
				.isEqualByComparingTo("1200.00");
		assertThat(asDecimal(budget.get("num_final_amount")))
				.as("but the live figure is still the one the screen sent, which sent nothing")
				.isEqualByComparingTo("0.00");
	}

	// ── reading back ─────────────────────────────────────────────────────

	private BigDecimal calculatedTotalOf(EventMaster event) {
		return asDecimal(budgetOf(event).get("num_calculated_total"));
	}

	private Map<String, Object> budgetOf(EventMaster event) {
		return jdbcTemplate.queryForMap(
				"SELECT * FROM event_budget WHERE ser_event_master_id = ?", event.getSerEventMasterId());
	}

	private List<Map<String, Object>> priceLinesOf(EventMaster event) {
		return jdbcTemplate.queryForList(
				"SELECT * FROM event_price_line WHERE ser_event_master_id = ? ORDER BY ser_event_price_line_id",
				event.getSerEventMasterId());
	}

	private static BigDecimal asDecimal(Object value) {
		return value == null ? null : new BigDecimal(value.toString());
	}

	// ── payloads ─────────────────────────────────────────────────────────

	private DtoEventMaster journeySave(EventMaster event) {
		DtoEventMaster dto = new DtoEventMaster();
		dto.setSerEventMasterId(event.getSerEventMasterId());
		dto.setSerCustId(customer.getSerCustId());
		dto.setSerEventTypeId(eventType.getSerEventTypeId());
		dto.setIsEditAllowed(true);
		dto.setNumVersion(event.getNumVersion());
		dto.setDteEventDate(DAY_AS_POSTED);
		dto.setNumNumberOfGuests(200);
		dto.setNumNumberOfTables(20);
		return dto;
	}

	private List<DtoCustomerMenuCategory> aMenuAt(String unitPrice, EnmPriceMultiplierType basis,
			BigDecimal guestsPerStation) {

		MenuItem category = seedDish("Mains", null, null, null);
		MenuItem course = seedDish("Curries", null, null, null);
		MenuItem first = seedDish("ChickenKarahi", unitPrice, basis, guestsPerStation);
		MenuItem second = seedDish("LambBiryani", unitPrice, basis, guestsPerStation);

		List<DtoMenuItem> dishes = new ArrayList<>();
		for (MenuItem dish : List.of(first, second)) {
			DtoMenuItem chosen = new DtoMenuItem();
			chosen.setSerMenuItemId(dish.getSerMenuItemId());
			dishes.add(chosen);
		}

		/*
		 * The category and course carry prices because the columns behind them
		 * are NOT NULL and the save writes the payload's figures straight into
		 * them. A client that posts a menu without prices does not get a
		 * booking priced at zero — it gets a constraint violation that loses
		 * the whole save. That is worth knowing and is recorded as P2; it is
		 * not what this suite is testing, so the fixture supplies them.
		 */
		DtoCustomerMenuSubCategory subCategory = new DtoCustomerMenuSubCategory();
		subCategory.setSubCategoryId(course.getSerMenuItemId());
		subCategory.setNumPrice(BigDecimal.ZERO);
		subCategory.setNumFinalPrice(BigDecimal.ZERO);
		subCategory.setItems(dishes);

		DtoCustomerMenuCategory chosenCategory = new DtoCustomerMenuCategory();
		chosenCategory.setCategoryId(category.getSerMenuItemId());
		chosenCategory.setNumPrice(BigDecimal.ZERO);
		chosenCategory.setNumFinalPrice(BigDecimal.ZERO);
		chosenCategory.setSubCategories(List.of(subCategory));

		return List.of(chosenCategory);
	}

	private List<DtoEventDecorCategorySelection> decorAt(String price) {
		DecorCategoryMaster category = new DecorCategoryMaster();
		category.setTxtDecorCategoryCode(MARKER + "-" + System.nanoTime());
		category.setTxtDecorCategoryName(MARKER + " Stage");
		category.setNumPrice(new BigDecimal(price));
		category.setBlnIsActive(true);
		category.setBlnIsDeleted(false);
		category = repositoryDecorCategoryMaster.saveAndFlush(category);

		DtoEventDecorCategorySelection selection = new DtoEventDecorCategorySelection();
		selection.setSerDecorCategoryId(category.getSerDecorCategoryId());
		selection.setNumPrice(new BigDecimal(price));

		List<DtoEventDecorCategorySelection> selections = new ArrayList<>();
		selections.add(selection);
		return selections;
	}

	// ── fixture ──────────────────────────────────────────────────────────

	private MenuItem seedDish(String name, String price, EnmPriceMultiplierType basis,
			BigDecimal guestsPerStation) {
		MenuItem dish = new MenuItem();
		dish.setTxtName(MARKER + " " + name);
		dish.setTxtCode(MARKER + "-" + System.nanoTime());
		dish.setTxtPath("IT_PRICE." + name + "_" + System.nanoTime());
		dish.setBlnIsSelectable(true);
		dish.setBlnIsActive(true);
		dish.setBlnIsDeleted(false);
		dish.setNumPrice(price == null ? null : new BigDecimal(price));
		if (basis != null) {
			dish.setEnmPriceMultiplierType(basis);
		}
		dish.setNumGuestsPerStation(guestsPerStation);
		return repositoryMenuItem.saveAndFlush(dish);
	}

	private EventMaster reload(EventMaster event) {
		return repositoryEventMaster.findById(event.getSerEventMasterId()).orElseThrow();
	}

	private EventMaster seedEvent() {
		customer = new CustomerMaster();
		customer.setTxtCustCode(MARKER + "-" + System.nanoTime());
		customer.setTxtCustName(MARKER + " customer");
		customer = repositoryCustomerMaster.save(customer);

		eventType = repositoryEventType.findAll().stream()
				.filter(t -> t.getTxtEventTypeCode() != null && t.getTxtEventTypeCode().startsWith(MARKER))
				.findFirst()
				.orElseGet(() -> {
					EventType seeded = new EventType();
					seeded.setTxtEventTypeCode(MARKER + "-TYPE");
					seeded.setTxtEventTypeName(MARKER + " walima");
					seeded.setBlnIsMainEvent(true);
					return repositoryEventType.save(seeded);
				});

		EventMaster event = new EventMaster();
		event.setTxtEventMasterName(MARKER + " walima");
		event.setTxtEventMasterCode(MARKER + "-" + System.nanoTime());
		event.setDteEventDate(Date.from(DAY.atStartOfDay(ZoneOffset.UTC).toInstant()));
		event.setCustomerMaster(customer);
		event.setEventType(eventType);
		event.setNumNumberOfGuests(200);
		event.setNumNumberOfTables(20);
		event.setBlnIsActive(true);
		event.setBlnIsDeleted(false);
		event.setIsEditAllowed(true);
		return repositoryEventMaster.saveAndFlush(event);
	}
}
