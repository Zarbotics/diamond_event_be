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
import java.util.LinkedHashMap;
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
import com.zbs.de.model.dto.DtoEventDecorCategorySelection;
import com.zbs.de.model.dto.DtoEventMaster;
import com.zbs.de.model.dto.DtoEventMasterAdminPortal;
import com.zbs.de.model.dto.DtoEventRunningOrder;
import com.zbs.de.repository.RepositoryCustomerMaster;
import com.zbs.de.repository.RepositoryDecorCategoryMaster;
import com.zbs.de.repository.RepositoryEventMaster;
import com.zbs.de.repository.RepositoryEventType;

/**
 * What each save path writes, recorded before anything is moved.
 *
 * <h2>Why this exists</h2>
 *
 * The journey's save and the office's save are about 85% the same code, and
 * that shared part is about to be written once instead of twice. A refactor
 * like that is only safe if "nothing changed" can be demonstrated rather than
 * asserted, and nothing in this repository could demonstrate it: the suites
 * that touch these methods each pin one narrow thing — a date, a paid amount,
 * a décor selection, a version clash — and between them they leave most of the
 * booking unobserved.
 *
 * <p>
 * So this suite is deliberately broad rather than deep. It fills in a booking,
 * saves it, and reads back every column and child row the save is responsible
 * for. It is not trying to say those values are <em>correct</em> — several of
 * them are merely what the code happens to do today. It is saying they are
 * what the code does today, so that if the extraction changes any of them, a
 * test goes red and names the field.
 *
 * <h2>Why it covers both portals separately</h2>
 *
 * Because the two paths are not supposed to end up identical. The office sets
 * three pricing fields the journey never touches
 * ({@code numDiscount}, {@code numItineraryPrice}, {@code numServingDishesPrice}),
 * the journey sets a step counter the office never touches
 * ({@code numFormState}), and the journey understands a legacy flat food list
 * that the office does not send. Those differences are real and are meant to
 * survive. Pinning them here is what stops a tidy-up from quietly removing
 * one.
 *
 * <h2>How to read a failure</h2>
 *
 * A failure here means the refactor changed behaviour. It does not, on its
 * own, mean the new behaviour is worse — but it does mean the change was not
 * the pure extraction it was supposed to be, and it needs a decision rather
 * than a fix-up.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class BothSavePathsKeepTheirBehaviourIT {

	private static final String MARKER = "IT-CHARACTERISE";
	private static final LocalDate DAY = LocalDate.now(ZoneOffset.UTC).plusYears(7);
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

	/*
	 * Deleted with SQL in foreign-key order rather than through the
	 * repositories. The save attaches child collections to the entity
	 * instances it was given, so a repository teardown trips Hibernate's
	 * auto-flush and throws TransientObjectException before it deletes
	 * anything.
	 */
	@AfterEach
	void removeSeed() {
		String minesEvents = "  SELECT e.ser_event_master_id FROM event_master e"
				+ "  JOIN customer_master c ON c.ser_cust_id = e.ser_cust_id"
				+ "  WHERE c.txt_cust_code LIKE ?";

		/*
		 * The two décor property tables hang off the category selection, not
		 * off the event, so they are reached through it rather than by an
		 * event id they do not have.
		 */
		jdbcTemplate.update("DELETE FROM event_decor_property_value_selection WHERE ser_event_decor_property_id IN ("
				+ "  SELECT p.ser_event_decor_property_id FROM event_decor_property_selection p"
				+ "  WHERE p.ser_event_decor_category_selection_id IN ("
				+ "    SELECT s.ser_event_decor_category_selection_id FROM event_decor_category_selection s"
				+ "    WHERE s.ser_event_master_id IN (" + minesEvents + ")))", MARKER + "%");
		jdbcTemplate.update("DELETE FROM event_decor_property_selection WHERE ser_event_decor_category_selection_id IN ("
				+ "  SELECT s.ser_event_decor_category_selection_id FROM event_decor_category_selection s"
				+ "  WHERE s.ser_event_master_id IN (" + minesEvents + "))", MARKER + "%");

		/*
		 * event_payment carries a legacy ser_event_id alongside the current
		 * ser_event_master_id. Both are cleared, because a row written
		 * through the older column would otherwise survive and hold a
		 * foreign key against the event this is trying to delete.
		 */
		jdbcTemplate.update("DELETE FROM event_payment WHERE ser_event_master_id IN (" + minesEvents + ")",
				MARKER + "%");
		jdbcTemplate.update("DELETE FROM event_payment WHERE ser_event_id IN (" + minesEvents + ")",
				MARKER + "%");

		for (String child : List.of("event_decor_category_selection", "event_external_supplier",
				"event_menu_food_selection", "event_budget", "event_quote")) {
			jdbcTemplate.update("DELETE FROM " + child + " WHERE ser_event_master_id IN ("
					+ minesEvents + ")", MARKER + "%");
		}
		jdbcTemplate.update("DELETE FROM event_master WHERE ser_cust_id IN ("
				+ "  SELECT ser_cust_id FROM customer_master WHERE txt_cust_code LIKE ?)", MARKER + "%");
		jdbcTemplate.update("DELETE FROM customer_master WHERE txt_cust_code LIKE ?", MARKER + "%");
		jdbcTemplate.update("DELETE FROM decor_category_master WHERE txt_decor_category_code LIKE ?", MARKER + "%");
	}

	// ── the journey ──────────────────────────────────────────────────────

	/**
	 * Everything the journey writes onto the booking itself.
	 *
	 * <p>
	 * One test rather than thirty, because the point is coverage of the field
	 * set, and thirty tests that each save a booking would take thirty times
	 * as long to say the same thing. The assertion names every field, so a
	 * failure still says which one moved.
	 */
	@Test
	@DisplayName("the journey's save writes the whole booking")
	void theJourneyWritesEveryField() throws Exception {
		EventMaster seeded = seedEvent();

		DtoEventMaster dto = richJourneySave(seeded);
		serviceEventMaster.saveAndUpdateWithDocs(dto, null);

		assertThat(bookingRow(seeded)).containsAllEntriesOf(expectedSharedColumns());
	}

	/**
	 * The step counter is the journey's, and only the journey's.
	 *
	 * <p>
	 * The office's save does not read {@code numFormState}, so a member of
	 * staff editing a booking cannot rewind the customer's progress through
	 * the journey. That asymmetry is deliberate and is pinned here because it
	 * is exactly the kind of thing a "make both paths the same" change removes
	 * without noticing.
	 */
	@Test
	@DisplayName("the journey records how far through the form the customer is")
	void theJourneyRecordsFormState() throws Exception {
		EventMaster seeded = seedEvent();

		DtoEventMaster dto = richJourneySave(seeded);
		dto.setNumFormState(4);
		serviceEventMaster.saveAndUpdateWithDocs(dto, null);

		assertThat(bookingRow(seeded).get("num_form_state"))
				.as("the journey stopped recording the customer's progress")
				.isEqualTo(4);
	}

	@Test
	@DisplayName("the journey stores the running order it was given")
	void theJourneyStoresTheRunningOrder() throws Exception {
		EventMaster seeded = seedEvent();

		DtoEventMaster dto = richJourneySave(seeded);
		serviceEventMaster.saveAndUpdateWithDocs(dto, null);

		assertThat(runningOrderOf(seeded))
				.as("the running order the customer typed was not stored")
				.containsEntry("txt_guest_arrival", "18:00")
				.containsEntry("txt_meal", "19:30")
				.containsEntry("dte_end_of_night", "23:00");
	}

	@Test
	@DisplayName("the journey stores the décor the customer chose")
	void theJourneyStoresDecor() throws Exception {
		EventMaster seeded = seedEvent();

		DtoEventMaster dto = richJourneySave(seeded);
		serviceEventMaster.saveAndUpdateWithDocs(dto, null);

		assertThat(decorCategoryIdsOf(seeded))
				.as("the décor the customer chose was not stored")
				.hasSize(1);
	}

	// ── the office ───────────────────────────────────────────────────────

	@Test
	@DisplayName("the office's save writes the whole booking")
	void theOfficeWritesEveryField() throws Exception {
		EventMaster seeded = seedEvent();

		DtoEventMasterAdminPortal dto = richOfficeSave(seeded);
		serviceEventMaster.saveAndUpdateWithDocsAdminPortal(dto, null);

		assertThat(bookingRow(seeded)).containsAllEntriesOf(expectedSharedColumns());
	}

	/**
	 * The three pricing fields only the office can set.
	 *
	 * <p>
	 * A customer does not get to name their own discount, so the journey's
	 * save ignores all three even though its DTO carries them. That is the
	 * other half of the asymmetry, and the same tidy-up risk applies.
	 */
	@Test
	@DisplayName("the office sets the pricing the customer cannot")
	void theOfficeSetsPricing() throws Exception {
		EventMaster seeded = seedEvent();

		DtoEventMasterAdminPortal dto = richOfficeSave(seeded);
		dto.setNumDiscount(new BigDecimal("150.00"));
		dto.setNumItineraryPrice(new BigDecimal("75.00"));
		dto.setNumServingDishesPrice(new BigDecimal("40.00"));
		serviceEventMaster.saveAndUpdateWithDocsAdminPortal(dto, null);

		Map<String, Object> row = bookingRow(seeded);
		assertThat(asDecimal(row.get("num_discount")))
				.as("the office's discount was not stored").isEqualByComparingTo("150.00");
		assertThat(asDecimal(row.get("num_itinerary_price")))
				.as("the office's itinerary price was not stored").isEqualByComparingTo("75.00");
		assertThat(asDecimal(row.get("num_serving_dishes_price")))
				.as("the office's serving dishes price was not stored").isEqualByComparingTo("40.00");
	}

	@Test
	@DisplayName("the office stores the running order it was given")
	void theOfficeStoresTheRunningOrder() throws Exception {
		EventMaster seeded = seedEvent();

		DtoEventMasterAdminPortal dto = richOfficeSave(seeded);
		serviceEventMaster.saveAndUpdateWithDocsAdminPortal(dto, null);

		assertThat(runningOrderOf(seeded))
				.as("the running order the office typed was not stored")
				.containsEntry("txt_guest_arrival", "18:00")
				.containsEntry("txt_meal", "19:30")
				.containsEntry("dte_end_of_night", "23:00");
	}

	@Test
	@DisplayName("the office stores the décor it was given")
	void theOfficeStoresDecor() throws Exception {
		EventMaster seeded = seedEvent();

		DtoEventMasterAdminPortal dto = richOfficeSave(seeded);
		serviceEventMaster.saveAndUpdateWithDocsAdminPortal(dto, null);

		assertThat(decorCategoryIdsOf(seeded))
				.as("the décor the office chose was not stored")
				.hasSize(1);
	}

	/**
	 * The office does not touch the customer's progress through the journey.
	 *
	 * <p>
	 * Stated as an expectation rather than left implicit, because "the office
	 * save ignores a field its own DTO carries" reads like an oversight to
	 * anybody tidying up, and the guard has to be louder than the temptation.
	 */
	@Test
	@DisplayName("an office save leaves the customer's progress alone")
	void theOfficeLeavesFormStateAlone() throws Exception {
		EventMaster seeded = seedEvent();

		DtoEventMaster fromJourney = richJourneySave(seeded);
		fromJourney.setNumFormState(6);
		serviceEventMaster.saveAndUpdateWithDocs(fromJourney, null);

		DtoEventMasterAdminPortal fromOffice = richOfficeSave(reload(seeded));
		fromOffice.setNumFormState(1);
		serviceEventMaster.saveAndUpdateWithDocsAdminPortal(fromOffice, null);

		assertThat(bookingRow(seeded).get("num_form_state"))
				.as("an office save rewound the customer's progress through the journey")
				.isEqualTo(6);
	}

	// ── what both are expected to write ──────────────────────────────────

	/**
	 * The columns both paths set from the same payload, with the same values.
	 *
	 * <p>
	 * This map is the actual subject of the refactor: every entry in it is a
	 * line of code that currently exists twice and is meant to end up existing
	 * once.
	 */
	private static Map<String, Object> expectedSharedColumns() {
		Map<String, Object> expected = new LinkedHashMap<>();
		expected.put("txt_event_master_name", MARKER + " renamed");
		expected.put("num_number_of_guests", 250);
		expected.put("num_number_of_tables", 25);
		expected.put("txt_bride_name", "Aisha Rahman");
		expected.put("txt_bride_first_name", "Aisha");
		expected.put("txt_bride_last_name", "Rahman");
		expected.put("txt_groom_name", "Yusuf Ali");
		expected.put("txt_groom_first_name", "Yusuf");
		expected.put("txt_groom_last_name", "Ali");
		expected.put("txt_birthday_celebrant", "n/a");
		expected.put("txt_age_category", "Adult");
		expected.put("txt_chief_guest", "Imam Sahib");
		expected.put("txt_catering_remarks", "No nuts on any table");
		expected.put("txt_decore_remarks", "Ivory and gold");
		expected.put("txt_event_extras_remarks", "Sparklers at the entrance");
		expected.put("txt_event_remarks", "Parking for forty cars");
		expected.put("txt_external_supplier_remarks", "Photographer arrives at five");
		expected.put("txt_venue_remarks", "Main hall only");
		expected.put("txt_event_services_remarks", "Two extra waiting staff");
		expected.put("txt_contact_person_first_name", "Fatima");
		expected.put("txt_contact_person_last_name", "Rahman");
		expected.put("txt_contact_person_phone_no", "07700 900321");
		return expected;
	}

	// ── reading the database back ────────────────────────────────────────

	private Map<String, Object> bookingRow(EventMaster event) {
		return jdbcTemplate.queryForMap(
				"SELECT * FROM event_master WHERE ser_event_master_id = ?",
				event.getSerEventMasterId());
	}

	private Map<String, Object> runningOrderOf(EventMaster event) {
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(
				"SELECT r.* FROM event_running_order r"
						+ " JOIN event_master e ON e.ser_event_running_order_id = r.ser_event_running_order_id"
						+ " WHERE e.ser_event_master_id = ?",
				event.getSerEventMasterId());
		return rows.isEmpty() ? Map.of() : rows.get(0);
	}

	private List<Map<String, Object>> decorCategoryIdsOf(EventMaster event) {
		return jdbcTemplate.queryForList(
				"SELECT ser_decor_category_id FROM event_decor_category_selection"
						+ " WHERE ser_event_master_id = ? AND bln_is_deleted = false",
				event.getSerEventMasterId());
	}

	private static BigDecimal asDecimal(Object value) {
		return value == null ? null : new BigDecimal(value.toString());
	}

	// ── the payloads ─────────────────────────────────────────────────────

	private DtoEventMaster richJourneySave(EventMaster event) {
		DtoEventMaster dto = new DtoEventMaster();
		dto.setSerEventMasterId(event.getSerEventMasterId());
		dto.setSerCustId(customer.getSerCustId());
		dto.setSerEventTypeId(eventType.getSerEventTypeId());
		dto.setIsEditAllowed(true);
		dto.setNumVersion(event.getNumVersion());
		dto.setDteEventDate(DAY_AS_POSTED);

		dto.setTxtEventMasterName(MARKER + " renamed");
		dto.setNumNumberOfGuests(250);
		dto.setNumNumberOfTables(25);
		dto.setTxtBrideName("Aisha Rahman");
		dto.setTxtBrideFirstName("Aisha");
		dto.setTxtBrideLastName("Rahman");
		dto.setTxtGroomName("Yusuf Ali");
		dto.setTxtGroomFirstName("Yusuf");
		dto.setTxtGroomLastName("Ali");
		dto.setTxtBirthDayCelebrant("n/a");
		dto.setTxtAgeCategory("Adult");
		dto.setTxtChiefGuest("Imam Sahib");
		dto.setTxtCateringRemarks("No nuts on any table");
		dto.setTxtDecoreRemarks("Ivory and gold");
		dto.setTxtEventExtrasRemarks("Sparklers at the entrance");
		dto.setTxtEventRemarks("Parking for forty cars");
		dto.setTxtExternalSupplierRemarks("Photographer arrives at five");
		dto.setTxtVenueRemarks("Main hall only");
		dto.setTxtEventServicesRemarks("Two extra waiting staff");
		dto.setTxtContactPersonFirstName("Fatima");
		dto.setTxtContactPersonLastName("Rahman");
		dto.setTxtContactPersonPhoneNo("07700 900321");

		dto.setDtoEventRunningOrder(aRunningOrder());
		dto.setDtoEventDecorSelections(someDecor());
		return dto;
	}

	private DtoEventMasterAdminPortal richOfficeSave(EventMaster event) {
		DtoEventMasterAdminPortal dto = new DtoEventMasterAdminPortal();
		dto.setSerEventMasterId(event.getSerEventMasterId());
		dto.setSerCustId(customer.getSerCustId());
		dto.setSerEventTypeId(eventType.getSerEventTypeId());
		dto.setIsEditAllowed(true);
		dto.setNumVersion(event.getNumVersion());
		dto.setDteEventDate(DAY_AS_POSTED);

		dto.setTxtEventMasterName(MARKER + " renamed");
		dto.setNumNumberOfGuests(250);
		dto.setNumNumberOfTables(25);
		dto.setTxtBrideName("Aisha Rahman");
		dto.setTxtBrideFirstName("Aisha");
		dto.setTxtBrideLastName("Rahman");
		dto.setTxtGroomName("Yusuf Ali");
		dto.setTxtGroomFirstName("Yusuf");
		dto.setTxtGroomLastName("Ali");
		dto.setTxtBirthDayCelebrant("n/a");
		dto.setTxtAgeCategory("Adult");
		dto.setTxtChiefGuest("Imam Sahib");
		dto.setTxtCateringRemarks("No nuts on any table");
		dto.setTxtDecoreRemarks("Ivory and gold");
		dto.setTxtEventExtrasRemarks("Sparklers at the entrance");
		dto.setTxtEventRemarks("Parking for forty cars");
		dto.setTxtExternalSupplierRemarks("Photographer arrives at five");
		dto.setTxtVenueRemarks("Main hall only");
		dto.setTxtEventServicesRemarks("Two extra waiting staff");
		dto.setTxtContactPersonFirstName("Fatima");
		dto.setTxtContactPersonLastName("Rahman");
		dto.setTxtContactPersonPhoneNo("07700 900321");

		dto.setDtoEventRunningOrder(aRunningOrder());
		dto.setDtoEventDecorSelections(someDecor());
		return dto;
	}

	private static DtoEventRunningOrder aRunningOrder() {
		DtoEventRunningOrder order = new DtoEventRunningOrder();
		order.setTxtGuestArrival("18:00");
		order.setTxtBaratArrival("18:30");
		order.setTxtBrideEntrance("19:00");
		order.setTxtNikah("19:15");
		order.setTxtMeal("19:30");
		order.setTxtEndOfNight("23:00");
		return order;
	}

	/*
	 * Seeds its own category rather than borrowing one from the catalogue.
	 * The test database's décor catalogue is empty, and a fixture that depends
	 * on seed data somebody else owns fails for reasons that have nothing to
	 * do with what it is testing.
	 */
	private List<DtoEventDecorCategorySelection> someDecor() {
		DecorCategoryMaster category = new DecorCategoryMaster();
		category.setTxtDecorCategoryCode(MARKER + "-" + System.nanoTime());
		category.setTxtDecorCategoryName(MARKER + " Stage");
		category.setBlnIsActive(true);
		category.setBlnIsDeleted(false);
		category = repositoryDecorCategoryMaster.saveAndFlush(category);

		DtoEventDecorCategorySelection selection = new DtoEventDecorCategorySelection();
		selection.setSerDecorCategoryId(category.getSerDecorCategoryId());
		selection.setNumPrice(new BigDecimal("500.00"));

		List<DtoEventDecorCategorySelection> selections = new ArrayList<>();
		selections.add(selection);
		return selections;
	}

	// ── fixture ──────────────────────────────────────────────────────────

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
		event.setNumNumberOfGuests(100);
		event.setBlnIsActive(true);
		event.setBlnIsDeleted(false);
		event.setIsEditAllowed(true);
		return repositoryEventMaster.saveAndFlush(event);
	}
}
