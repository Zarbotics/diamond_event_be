package com.zbs.de.service;

import static org.assertj.core.api.Assertions.assertThat;

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
import com.zbs.de.model.EventMaster;
import com.zbs.de.model.EventType;
import com.zbs.de.model.ExternalSupplierCategory;
import com.zbs.de.model.dto.DtoEventExternalSupplier;
import com.zbs.de.model.dto.DtoEventMaster;
import com.zbs.de.model.dto.DtoEventMasterAdminPortal;
import com.zbs.de.repository.RepositoryCustomerMaster;
import com.zbs.de.repository.RepositoryEventMaster;
import com.zbs.de.repository.RepositoryEventType;
import com.zbs.de.repository.RepositoryExternalSupplierCategory;

/**
 * A supplier saves from the office as well as from the journey.
 *
 * <h2>What this was written from</h2>
 *
 * A bug shipped earlier in this same session. The office's event form gained an
 * External Suppliers panel, wired in and covered by a passing test — and it
 * could neither show a supplier nor save one.
 *
 * <p>
 * {@code DtoEventMasterAdminPortal} had no {@code externalSuppliers} field, so
 * Jackson dropped the rows as an unknown property on the way in;
 * {@code saveAndUpdateWithDocsAdminPortal} never called
 * {@code replaceExternalSuppliers}; and {@code getEventByIdAdminPortal} never
 * called {@code readExternalSuppliers}. Three omissions, all invisible, all in
 * the copy of the save logic that nobody had changed.
 *
 * <p>
 * The test that passed rendered the React component on its own with values it
 * supplied itself. It proved the panel put {@code externalSuppliers} into the
 * form — which was true, and useless, because nothing downstream wanted them.
 *
 * <h2>Why it is written against both paths</h2>
 *
 * Because the defect was not "suppliers are broken". It was "the two save
 * paths disagree, and only one of them was changed". A test that exercised the
 * journey would have passed throughout. The thing worth pinning is that both
 * portals do the same thing with the same payload — which is the invariant
 * every drift bug in this file has broken.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class SuppliersSaveFromBothPortalsIT {

	private static final String MARKER = "IT-BOTHSAVE";
	private static final LocalDate DAY = LocalDate.now(ZoneOffset.UTC).plusYears(6);

	@Autowired
	private ServiceEventMaster serviceEventMaster;

	@Autowired
	private RepositoryEventMaster repositoryEventMaster;

	@Autowired
	private RepositoryCustomerMaster repositoryCustomerMaster;

	@Autowired
	private RepositoryEventType repositoryEventType;

	@Autowired
	private RepositoryExternalSupplierCategory repositoryExternalSupplierCategory;

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
		for (String child : List.of("event_external_supplier", "event_budget", "event_quote",
				"event_decor_category_selection", "event_menu_food_selection")) {
			jdbcTemplate.update("DELETE FROM " + child + " WHERE ser_event_master_id IN ("
					+ "  SELECT e.ser_event_master_id FROM event_master e"
					+ "  JOIN customer_master c ON c.ser_cust_id = e.ser_cust_id"
					+ "  WHERE c.txt_cust_code LIKE ?)", MARKER + "%");
		}
		jdbcTemplate.update("DELETE FROM event_master WHERE ser_cust_id IN ("
				+ "  SELECT ser_cust_id FROM customer_master WHERE txt_cust_code LIKE ?)", MARKER + "%");
		jdbcTemplate.update("DELETE FROM customer_master WHERE txt_cust_code LIKE ?", MARKER + "%");
	}

	/**
	 * The office can record a supplier taken over the telephone.
	 *
	 * <p>
	 * This is the one that was broken, and it is the ordinary case: most
	 * suppliers are declared to a member of staff a fortnight before the day,
	 * not typed by the customer into the journey.
	 */
	@Test
	@DisplayName("a supplier added in the office is stored")
	void theOfficeCanSaveASupplier() throws Exception {
		EventMaster event = seedEvent();
		ExternalSupplierCategory photographer = aCategory();

		DtoEventMasterAdminPortal dto = new DtoEventMasterAdminPortal();
		dto.setSerEventMasterId(event.getSerEventMasterId());
		dto.setSerCustId(customer.getSerCustId());
		dto.setSerEventTypeId(eventType.getSerEventTypeId());
		dto.setIsEditAllowed(true);
		dto.setNumVersion(event.getNumVersion());
		dto.setExternalSuppliers(List.of(
				supplier(photographer, "Noor Photography", "07700 900123")));

		serviceEventMaster.saveAndUpdateWithDocsAdminPortal(dto, null);

		assertThat(storedSupplierNames(event))
				.as("a supplier added in the office was silently discarded")
				.containsExactly("Noor Photography");
	}

	/**
	 * And the office can read back what the journey stored.
	 *
	 * <p>
	 * The other half of the same bug: the panel opened empty over a booking
	 * that had suppliers on it, because the office's read never asked for them.
	 */
	@Test
	@DisplayName("the office sees the suppliers the customer declared")
	void theOfficeCanReadTheJourneysSuppliers() throws Exception {
		EventMaster event = seedEvent();
		ExternalSupplierCategory photographer = aCategory();

		DtoEventMaster fromJourney = new DtoEventMaster();
		fromJourney.setSerEventMasterId(event.getSerEventMasterId());
		fromJourney.setSerCustId(customer.getSerCustId());
		fromJourney.setSerEventTypeId(eventType.getSerEventTypeId());
		fromJourney.setIsEditAllowed(true);
		fromJourney.setNumVersion(event.getNumVersion());
		fromJourney.setExternalSuppliers(List.of(
				supplier(photographer, "Henna by Sana", "07700 900456")));

		serviceEventMaster.saveAndUpdateWithDocs(fromJourney, null);

		DtoEventMasterAdminPortal seenByOffice = (DtoEventMasterAdminPortal) serviceEventMaster
				.saveAndUpdateWithDocsAdminPortal(untouchedAdminSave(event), null)
				.getResult();

		assertThat(seenByOffice.getExternalSuppliers())
				.as("the office's copy of the booking showed none of its suppliers")
				.isNotNull()
				.extracting(DtoEventExternalSupplier::getTxtSupplierName)
				.containsExactly("Henna by Sana");
	}

	/**
	 * The rule both paths must follow: silence is not deletion.
	 *
	 * <p>
	 * Every screen posts the whole booking back, so a save that says nothing
	 * about suppliers must leave them alone — otherwise the office changing a
	 * guest count deletes the photographer.
	 */
	@Test
	@DisplayName("an office save that never mentions suppliers leaves them alone")
	void anOmittedListIsKeptByTheOfficeToo() throws Exception {
		EventMaster event = seedEvent();
		ExternalSupplierCategory photographer = aCategory();

		DtoEventMasterAdminPortal withSupplier = untouchedAdminSave(event);
		withSupplier.setExternalSuppliers(List.of(
				supplier(photographer, "Noor Photography", "07700 900123")));
		serviceEventMaster.saveAndUpdateWithDocsAdminPortal(withSupplier, null);
		assertThat(storedSupplierNames(event)).hasSize(1);

		serviceEventMaster.saveAndUpdateWithDocsAdminPortal(untouchedAdminSave(reload(event)), null);

		assertThat(storedSupplierNames(event))
				.as("an office save that said nothing about suppliers deleted them")
				.hasSize(1);
	}

	// ── fixture ──────────────────────────────────────────────────────────

	private DtoEventMasterAdminPortal untouchedAdminSave(EventMaster event) {
		DtoEventMasterAdminPortal dto = new DtoEventMasterAdminPortal();
		dto.setSerEventMasterId(event.getSerEventMasterId());
		dto.setSerCustId(customer.getSerCustId());
		dto.setSerEventTypeId(eventType.getSerEventTypeId());
		dto.setIsEditAllowed(true);
		dto.setNumVersion(event.getNumVersion());
		return dto;
	}

	private static DtoEventExternalSupplier supplier(ExternalSupplierCategory category, String name,
			String phone) {
		DtoEventExternalSupplier dto = new DtoEventExternalSupplier();
		dto.setSerSupplierCategoryId(category.getSerSupplierCategoryId());
		dto.setTxtSupplierName(name);
		dto.setTxtContactPhone(phone);
		return dto;
	}

	private ExternalSupplierCategory aCategory() {
		return repositoryExternalSupplierCategory.findOffered().stream().findFirst()
				.orElseThrow(() -> new AssertionError(
						"V19 seeds twelve categories; none were found, so the fixture is wrong"));
	}

	private List<String> storedSupplierNames(EventMaster event) {
		return jdbcTemplate.queryForList(
				"SELECT txt_supplier_name FROM event_external_supplier "
						+ "WHERE ser_event_master_id = ? AND bln_is_deleted = false "
						+ "ORDER BY ser_event_external_supplier_id",
				String.class, event.getSerEventMasterId());
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
		event.setNumNumberOfGuests(100);
		event.setBlnIsActive(true);
		event.setBlnIsDeleted(false);
		event.setIsEditAllowed(true);
		return repositoryEventMaster.saveAndFlush(event);
	}
}
