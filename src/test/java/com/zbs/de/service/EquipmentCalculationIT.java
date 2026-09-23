package com.zbs.de.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

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
import com.zbs.de.model.EquipmentItem;
import com.zbs.de.model.EquipmentRequirement;
import com.zbs.de.model.EventMaster;
import com.zbs.de.model.EventMenuFoodSelection;
import com.zbs.de.model.EventType;
import com.zbs.de.model.MenuItem;
import com.zbs.de.model.dto.DtoEquipmentLine;
import com.zbs.de.model.dto.DtoEquipmentRequirementList;
import com.zbs.de.repository.RepositoryCustomerMaster;
import com.zbs.de.repository.RepositoryEquipmentItem;
import com.zbs.de.repository.RepositoryEquipmentRequirement;
import com.zbs.de.repository.RepositoryEventMaster;
import com.zbs.de.repository.RepositoryEventMenuFoodSelection;
import com.zbs.de.repository.RepositoryEventType;
import com.zbs.de.repository.RepositoryMenuItem;
import com.zbs.de.service.impl.ServiceEquipmentCalculation;
import com.zbs.de.util.enums.EnmEquipmentBasis;

/**
 * Working out what an event needs on the day.
 *
 * <h2>Why the arithmetic is the whole test</h2>
 *
 * This feature is arithmetic and nothing else — the value is entirely in
 * getting the numbers right, and every way of getting them wrong costs the
 * business something real on a Saturday morning. Sending 300 plates to a
 * 300-cover wedding leaves a table short when one is dropped. Counting three
 * stations where four are needed leaves thirty guests queueing.
 *
 * <p>
 * So each case here is one arithmetic rule, written as the smallest booking
 * that exercises it.
 *
 * <h2>The two that are easy to get wrong</h2>
 *
 * Rounding happens once, at the end, after everything asking for an item has
 * been added. Round per rule instead and three rules wanting 0.4 each become
 * three plates where the arithmetic said two.
 *
 * <p>
 * And stations round up. 180 guests at one station per 50 is four, not three
 * and a half, and certainly not three — the fourth is exactly the one the
 * last thirty guests are standing at.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class EquipmentCalculationIT {

	private static final String MARKER = "IT-EQUIP";
	private static final LocalDate DAY = LocalDate.now(ZoneOffset.UTC).plusYears(8);

	@Autowired
	private ServiceEquipmentCalculation calculation;

	@Autowired
	private RepositoryEquipmentItem repositoryEquipmentItem;

	@Autowired
	private RepositoryEquipmentRequirement repositoryEquipmentRequirement;

	@Autowired
	private RepositoryEventMaster repositoryEventMaster;

	@Autowired
	private RepositoryEventMenuFoodSelection repositoryEventMenuFoodSelection;

	@Autowired
	private RepositoryCustomerMaster repositoryCustomerMaster;

	@Autowired
	private RepositoryEventType repositoryEventType;

	@Autowired
	private RepositoryMenuItem repositoryMenuItem;

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

	/*
	  In SQL and in dependency order, for the reason the décor suite found:
	  tearing down through the repositories in the same persistence context
	  the service just used throws before it deletes anything.
	*/
	@AfterEach
	void removeSeed() {
		jdbcTemplate.update("DELETE FROM equipment_requirement WHERE ser_equipment_item_id IN ("
				+ "  SELECT ser_equipment_item_id FROM equipment_item WHERE txt_name LIKE ?)", MARKER + "%");
		jdbcTemplate.update("DELETE FROM equipment_requirement WHERE ser_menu_item_id IN ("
				+ "  SELECT ser_menu_item_id FROM menu_item WHERE txt_name LIKE ?)", MARKER + "%");
		jdbcTemplate.update("DELETE FROM equipment_item WHERE txt_name LIKE ?", MARKER + "%");

		jdbcTemplate.update("DELETE FROM event_menu_food_selection WHERE ser_event_master_id IN ("
				+ "  SELECT e.ser_event_master_id FROM event_master e"
				+ "  JOIN customer_master c ON c.ser_cust_id = e.ser_cust_id"
				+ "  WHERE c.txt_cust_code LIKE ?)", MARKER + "%");
		for (String child : List.of("event_budget", "event_quote", "event_decor_category_selection",
				"event_external_supplier")) {
			jdbcTemplate.update("DELETE FROM " + child + " WHERE ser_event_master_id IN ("
					+ "  SELECT e.ser_event_master_id FROM event_master e"
					+ "  JOIN customer_master c ON c.ser_cust_id = e.ser_cust_id"
					+ "  WHERE c.txt_cust_code LIKE ?)", MARKER + "%");
		}
		jdbcTemplate.update("DELETE FROM event_master WHERE ser_cust_id IN ("
				+ "  SELECT ser_cust_id FROM customer_master WHERE txt_cust_code LIKE ?)", MARKER + "%");
		jdbcTemplate.update("DELETE FROM customer_master WHERE txt_cust_code LIKE ?", MARKER + "%");
		jdbcTemplate.update("DELETE FROM menu_item WHERE txt_name LIKE ?", MARKER + "%");
	}

	// ── the four bases ───────────────────────────────────────────────────

	@Test
	@DisplayName("per guest multiplies by the guest count")
	void perGuest() {
		EventMaster event = seedEvent(300, 30);
		MenuItem dish = seedDish("Chicken Karahi");
		putOnMenu(event, dish);
		EquipmentItem plate = seedEquipment("Dinner plate");
		rule(dish, plate, "1", EnmEquipmentBasis.PER_GUEST, null, null);

		assertThat(quantityOf(event, plate)).isEqualTo(300);
	}

	/**
	 * Tables are counted, not inferred.
	 *
	 * <p>
	 * A venue seats eight on some tables and twelve on others, so guests
	 * divided by ten is a guess. The booking already carries the real number.
	 */
	@Test
	@DisplayName("per table multiplies by the table count, not the guest count")
	void perTable() {
		EventMaster event = seedEvent(300, 30);
		EquipmentItem cloth = seedEquipment("Tablecloth");
		everyEventRule(cloth, "1", EnmEquipmentBasis.PER_TABLE, null, null);

		assertThat(quantityOf(event, cloth)).isEqualTo(30);
	}

	@Test
	@DisplayName("per event is a flat number however many come")
	void perEvent() {
		EventMaster event = seedEvent(300, 30);
		EquipmentItem knife = seedEquipment("Cake knife");
		everyEventRule(knife, "1", EnmEquipmentBasis.PER_EVENT, null, null);

		assertThat(quantityOf(event, knife)).isEqualTo(1);
	}

	/**
	 * The one the old model could not express at all.
	 *
	 * <p>
	 * 180 guests at one station per 50 is four stations. Three and a half is
	 * not a thing you can put in a room, and three leaves the last thirty
	 * guests queueing at somebody else's.
	 */
	@Test
	@DisplayName("stations round up, because a part of a station serves nobody")
	void perStationRoundsUp() {
		EventMaster event = seedEvent(180, 18);
		MenuItem grazing = seedDish("Grazing bar");
		putOnMenu(event, grazing);
		EquipmentItem board = seedEquipment("Grazing board");
		rule(grazing, board, "2", EnmEquipmentBasis.PER_STATION, 50, null);

		// ceil(180 / 50) = 4 stations, 2 boards each.
		assertThat(quantityOf(event, board)).isEqualTo(8);
	}

	@Test
	@DisplayName("a station rule with no station size is one station however many come")
	void stationWithoutASizeIsOne() {
		EventMaster event = seedEvent(300, 30);
		MenuItem cake = seedDish("Cake table");
		putOnMenu(event, cake);
		EquipmentItem stand = seedEquipment("Cake stand");
		rule(cake, stand, "1", EnmEquipmentBasis.PER_STATION, null, null);

		assertThat(quantityOf(event, stand)).isEqualTo(1);
	}

	// ── spares and rounding ──────────────────────────────────────────────

	@Test
	@DisplayName("spares are added before rounding")
	void sparesAreAdded() {
		EventMaster event = seedEvent(300, 30);
		MenuItem dish = seedDish("Chicken Karahi");
		putOnMenu(event, dish);
		EquipmentItem plate = seedEquipment("Dinner plate");
		rule(dish, plate, "1", EnmEquipmentBasis.PER_GUEST, null, "5");

		// 300 + 5% = 315.
		assertThat(quantityOf(event, plate)).isEqualTo(315);
	}

	/**
	 * Rounded once, at the end.
	 *
	 * <p>
	 * Three dishes each wanting 0.4 of a serving spoon per table want 1.2 per
	 * table — 12 spoons across 10 tables, not 30. Rounding inside the loop
	 * would turn each 4 into 10 and send three times what is needed.
	 */
	@Test
	@DisplayName("fractions are added up before rounding, not rounded one by one")
	void fractionsAreAddedBeforeRounding() {
		EventMaster event = seedEvent(100, 10);
		EquipmentItem spoon = seedEquipment("Serving spoon");

		for (String dishName : List.of("Rice", "Curry", "Salad")) {
			MenuItem dish = seedDish(dishName);
			putOnMenu(event, dish);
			rule(dish, spoon, "0.4", EnmEquipmentBasis.PER_TABLE, null, null);
		}

		// 3 x 0.4 x 10 tables = 12. Rounding each rule first would give 30.
		assertThat(quantityOf(event, spoon)).isEqualTo(12);
	}

	@Test
	@DisplayName("a fraction that does not divide evenly rounds up, never down")
	void fractionsRoundUp() {
		EventMaster event = seedEvent(100, 10);
		MenuItem dish = seedDish("Canapes");
		putOnMenu(event, dish);
		EquipmentItem tray = seedEquipment("Canape tray");
		// One tray per 30 guests: 100/30 = 3.33 trays. Three leaves ten guests unserved.
		rule(dish, tray, "0.0334", EnmEquipmentBasis.PER_GUEST, null, null);

		assertThat(quantityOf(event, tray)).isEqualTo(4);
	}

	// ── what fires and what does not ─────────────────────────────────────

	@Test
	@DisplayName("a rule for a dish nobody ordered does not fire")
	void rulesOnlyFireForWhatWasChosen() {
		EventMaster event = seedEvent(300, 30);
		MenuItem ordered = seedDish("Chicken Karahi");
		MenuItem notOrdered = seedDish("Lamb Biryani");
		putOnMenu(event, ordered);

		EquipmentItem plate = seedEquipment("Dinner plate");
		EquipmentItem bowl = seedEquipment("Serving bowl");
		rule(ordered, plate, "1", EnmEquipmentBasis.PER_GUEST, null, null);
		rule(notOrdered, bowl, "1", EnmEquipmentBasis.PER_GUEST, null, null);

		DtoEquipmentRequirementList list = calculation.calculateFor(event.getSerEventMasterId());

		assertThat(namesIn(list)).contains(MARKER + " Dinner plate");
		assertThat(namesIn(list))
				.as("equipment for a dish nobody ordered was included")
				.doesNotContain(MARKER + " Serving bowl");
	}

	@Test
	@DisplayName("two dishes asking for the same thing make one line, not two")
	void sameItemFromTwoDishesIsOneLine() {
		EventMaster event = seedEvent(200, 20);
		EquipmentItem plate = seedEquipment("Dinner plate");

		for (String dishName : List.of("Starter", "Main")) {
			MenuItem dish = seedDish(dishName);
			putOnMenu(event, dish);
			rule(dish, plate, "1", EnmEquipmentBasis.PER_GUEST, null, null);
		}

		DtoEquipmentRequirementList list = calculation.calculateFor(event.getSerEventMasterId());
		List<DtoEquipmentLine> plates = list.getLines().stream()
				.filter(l -> (MARKER + " Dinner plate").equals(l.getTxtItemName()))
				.toList();

		assertThat(plates).hasSize(1);
		assertThat(plates.get(0).getNumQuantity()).isEqualTo(400);
		assertThat(plates.get(0).getSources())
				.as("a number nobody can explain is a number nobody can correct")
				.hasSize(2);
	}

	// ── nothing to work out ──────────────────────────────────────────────

	/**
	 * A booking with no guest count is an ordinary state, not an error.
	 *
	 * <p>
	 * And it must say so. A sheet of zeroes looks like a calculation that ran
	 * and found nothing, which is a different and much more alarming thing.
	 */
	@Test
	@DisplayName("a booking with no guest count explains itself instead of showing zeroes")
	void noGuestCountIsExplained() {
		EventMaster event = seedEvent(0, 0);

		DtoEquipmentRequirementList list = calculation.calculateFor(event.getSerEventMasterId());

		assertThat(list.getLines()).isEmpty();
		assertThat(list.getTxtExplanation()).contains("no guest count");
	}

	@Test
	@DisplayName("a booking that does not exist says so rather than throwing")
	void unknownBookingIsExplained() {
		DtoEquipmentRequirementList list = calculation.calculateFor(-1);

		assertThat(list.getTxtExplanation()).contains("could not be found");
	}

	// ── fixture ──────────────────────────────────────────────────────────

	private int quantityOf(EventMaster event, EquipmentItem item) {
		return calculation.calculateFor(event.getSerEventMasterId()).getLines().stream()
				.filter(l -> item.getSerEquipmentItemId().equals(l.getSerEquipmentItemId()))
				.map(DtoEquipmentLine::getNumQuantity)
				.findFirst()
				.orElseThrow(() -> new AssertionError("no line for " + item.getTxtName()));
	}

	private static List<String> namesIn(DtoEquipmentRequirementList list) {
		return list.getLines().stream().map(DtoEquipmentLine::getTxtItemName).toList();
	}

	private EventMaster seedEvent(int guests, int tables) {
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
		event.setNumNumberOfGuests(guests);
		event.setNumNumberOfTables(tables);
		event.setBlnIsActive(true);
		event.setBlnIsDeleted(false);
		return repositoryEventMaster.saveAndFlush(event);
	}

	private MenuItem seedDish(String name) {
		MenuItem dish = new MenuItem();
		dish.setTxtName(MARKER + " " + name);
		dish.setTxtCode(MARKER + "-" + System.nanoTime());
		/*
		  The ltree path is NOT NULL and its labels may hold only letters,
		  digits and underscores — no spaces, no hyphens. The dish name cannot
		  be used as-is, so the path is built from the code with everything
		  else stripped out.
		*/
		dish.setTxtPath("IT_EQUIP." + name.replaceAll("[^A-Za-z0-9]", "") + "_" + System.nanoTime());
		dish.setBlnIsSelectable(true);
		dish.setBlnIsActive(true);
		dish.setBlnIsDeleted(false);
		return repositoryMenuItem.saveAndFlush(dish);
	}

	private void putOnMenu(EventMaster event, MenuItem dish) {
		EventMenuFoodSelection selection = new EventMenuFoodSelection();
		selection.setEventMaster(event);
		selection.setMenuItem(dish);
		selection.setBlnIsActive(true);
		selection.setBlnIsDeleted(false);
		selection.setCreatedDate(new Date());
		repositoryEventMenuFoodSelection.saveAndFlush(selection);
	}

	private EquipmentItem seedEquipment(String name) {
		EquipmentItem item = new EquipmentItem();
		item.setTxtName(MARKER + " " + name);
		item.setTxtUnit("each");
		item.setBlnIsActive(true);
		item.setBlnIsDeleted(false);
		item.setCreatedDate(new Date());
		return repositoryEquipmentItem.saveAndFlush(item);
	}

	private void rule(MenuItem dish, EquipmentItem item, String quantity, EnmEquipmentBasis basis,
			Integer guestsPerStation, String sparePercent) {
		save(dish, false, item, quantity, basis, guestsPerStation, sparePercent);
	}

	private void everyEventRule(EquipmentItem item, String quantity, EnmEquipmentBasis basis,
			Integer guestsPerStation, String sparePercent) {
		save(null, true, item, quantity, basis, guestsPerStation, sparePercent);
	}

	private void save(MenuItem dish, boolean everyEvent, EquipmentItem item, String quantity,
			EnmEquipmentBasis basis, Integer guestsPerStation, String sparePercent) {
		EquipmentRequirement rule = new EquipmentRequirement();
		rule.setMenuItem(dish);
		rule.setBlnAppliesToEveryEvent(everyEvent);
		rule.setEquipmentItem(item);
		rule.setNumQuantity(new BigDecimal(quantity));
		rule.setEnmBasis(basis);
		rule.setNumGuestsPerStation(guestsPerStation);
		rule.setNumSparePercent(sparePercent == null ? null : new BigDecimal(sparePercent));
		rule.setBlnIsActive(true);
		rule.setBlnIsDeleted(false);
		rule.setCreatedDate(new Date());
		repositoryEquipmentRequirement.saveAndFlush(rule);
	}
}
