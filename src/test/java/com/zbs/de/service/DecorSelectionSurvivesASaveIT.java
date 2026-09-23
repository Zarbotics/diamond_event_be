package com.zbs.de.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
import org.springframework.test.context.TestPropertySource;

import com.zbs.de.model.CustomerMaster;
import com.zbs.de.model.DecorCategoryMaster;
import com.zbs.de.model.EventMaster;
import com.zbs.de.model.EventType;
import com.zbs.de.model.dto.DtoEventDecorCategorySelection;
import com.zbs.de.model.dto.DtoEventMaster;
import com.zbs.de.repository.RepositoryCustomerMaster;
import com.zbs.de.repository.RepositoryDecorCategoryMaster;
import com.zbs.de.repository.RepositoryEventDecorCategorySelection;
import com.zbs.de.repository.RepositoryEventMaster;
import com.zbs.de.repository.RepositoryEventType;

/**
 * Décor selections reach the database and stay there.
 *
 * <h2>Why this was written</h2>
 *
 * Décor is one of the four things this business sells, and nothing anywhere
 * tested that choosing some of it is remembered. The question came up because
 * all three décor selection tables are empty — 0 category, 0 property, 0
 * extras — against a catalogue of 4 categories, 6 properties, 16 values and 10
 * extras.
 *
 * <p>
 * That is <em>not</em> evidence of a defect, and this suite was not written on
 * the assumption that it is. The journey's décor step is optional and the
 * end-to-end suite walks straight past it, so "nobody in this database has
 * ever chosen any décor" explains the zeros completely. What could not be
 * explained away was that there was no way to tell the difference between that
 * and a feature that silently drops everything it is given.
 *
 * <h2>What it found</h2>
 *
 * The journey is fine. {@code saveAndUpdateWithDocs}, which is what the
 * booking screens actually post to, clears the existing collection and adds to
 * it — the pattern {@code orphanRemoval} requires — and the line that used to
 * do it the wrong way is still commented out beside it.
 *
 * <p>
 * {@code saveAndUpdate} had never been given that fix. It replaced the
 * collection wholesale, {@code entity.setDecorSelections(newList)}, which
 * makes Hibernate throw <em>"A collection with orphan deletion was no longer
 * referenced by the owning entity instance"</em> on every save carrying any
 * décor at all. It backs {@code POST /eventMaster/saveOrUpdate}, which is a
 * live administrator endpoint that neither frontend calls — so this was a real
 * break in code nobody currently reaches, and it is fixed rather than left for
 * whoever reaches it first.
 *
 * <p>
 * None of this explains the empty décor tables, and it was never going to:
 * the journey's décor step is optional and the end-to-end suite walks past it,
 * so "nobody in this database has chosen any décor" remains the explanation.
 *
 * <h2>What is pinned</h2>
 *
 * Three things, against both save paths, in the order they would cost the
 * business money: that a selection is stored at all; that saving again does
 * not quietly lose it; and that a save which never mentions décor leaves what
 * was already chosen alone — the rule that stops a later step in the journey
 * wiping an earlier one.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class DecorSelectionSurvivesASaveIT {

	private static final String MARKER = "IT-DECOR";

	/** Far enough out that nothing else in the suite is using it. */
	private static final LocalDate DAY = LocalDate.now(ZoneOffset.UTC).plusYears(9);

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
	private RepositoryEventDecorCategorySelection repositoryEventDecorCategorySelection;

	@Autowired
	private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

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
	  Cleaned up by owning customer rather than by the event's name, for the
	  reason the neighbouring date suite discovered the hard way: the save
	  replaces rather than patches, so a payload that omits the name writes
	  null over it and the fixture can no longer recognise its own event.
	*/
	/**
	 * Torn down in SQL, deliberately.
	 *
	 * <p>
	 * Through the repositories it cannot be done: the save attaches each
	 * selection to the entity instance it was holding, so by the time the test
	 * ends the persistence context contains a décor row pointing at an
	 * EventMaster it does not know. Hibernate auto-flushes before the first
	 * query a teardown makes, and throws TransientObjectException there —
	 * before the teardown has deleted anything.
	 *
	 * <p>
	 * That is an artefact of clearing up after the service in the same context
	 * the service just used, not something the application does, and it is not
	 * worth contorting the fixture around. Four deletes in the right order,
	 * straight at the database.
	 */
	@AfterEach
	void removeSeed() throws Exception {
		/*
		  Everything that points at an event, then the event, then the customer.

		  The list is not guesswork: it is every table carrying a foreign key to
		  event_master, and saving one booking touches several of them — a
		  budget row and a quote row appear without anybody asking for them.
		  Leaving any behind fails the delete on a constraint rather than
		  quietly, which is the right way round, but it fails in teardown where
		  it reads as six broken tests.
		*/
		Map<String, String> children = new LinkedHashMap<>();
		children.put("event_decor_category_selection", "ser_event_master_id");
		children.put("event_decor_extras_selection", "ser_event_master_id");
		children.put("event_external_supplier", "ser_event_master_id");
		children.put("event_food_selection", "ser_event_master_id");
		children.put("event_menu_category_selection", "ser_event_master_id");
		children.put("event_menu_food_selection", "ser_event_master_id");
		children.put("event_itinerary_result", "ser_event_master_id");
		children.put("event_quote", "ser_event_master_id");
		children.put("event_budget", "ser_event_master_id");
		children.put("decor_event_extras", "ser_event_master_id");
		// Three of them spell the column differently. Read from the catalogue
		// rather than assumed, because assuming it failed on exactly these.
		children.put("event_itinerary_summary", "ser_event_id");
		children.put("event_menu_itinerary", "ser_event_id");
		children.put("event_payment", "ser_event_id");

		children.forEach((table, column) -> jdbcTemplate.update(
				"DELETE FROM " + table + " WHERE " + column + " IN ("
						+ "  SELECT e.ser_event_master_id FROM event_master e"
						+ "  JOIN customer_master c ON c.ser_cust_id = e.ser_cust_id"
						+ "  WHERE c.txt_cust_code LIKE ?)",
				MARKER + "%"));

		jdbcTemplate.update(
				"DELETE FROM event_master WHERE ser_cust_id IN ("
						+ "  SELECT ser_cust_id FROM customer_master WHERE txt_cust_code LIKE ?)",
				MARKER + "%");

		jdbcTemplate.update("DELETE FROM customer_master WHERE txt_cust_code LIKE ?", MARKER + "%");

		jdbcTemplate.update("DELETE FROM decor_category_master WHERE txt_decor_category_code LIKE ?",
				MARKER + "%");
	}

	/**
	 * One way in, now.
	 *
	 * <p>
	 * Every case here used to run twice, against `saveWithDocs` and against
	 * `saveOrUpdate` — separate copies of the same logic, which is how one of
	 * them came to carry the orphan-collection fix and the other did not.
	 *
	 * <p>
	 * `saveOrUpdate` has since been removed: no frontend called it, and a
	 * second copy existing only so that tests can prove it matches the first
	 * is a cost with no benefit. The cases stayed; there is simply one path
	 * for them to exercise.
	 */
	private void save(DtoEventMaster dto) {
		try {
			serviceEventMaster.saveAndUpdateWithDocs(dto, null);
		} catch (java.io.IOException e) {
			throw new IllegalStateException("saving the booking failed", e);
		}
	}

	@Test
	@DisplayName("a chosen décor category is actually stored")
	void aDecorChoiceIsStored() throws Exception {
		EventMaster event = seedEvent();
		DecorCategoryMaster stage = seedDecorCategory("Stage");

		save(withDecor(event, stage, "Ivory and gold, please."));

		assertThat(storedDecorOf(event))
				.as("choosing a décor category stored nothing at all")
				.hasSize(1)
				.first()
				.satisfies(selection -> assertThat(selection.getTxtRemarks())
						.isEqualTo("Ivory and gold, please."));
	}

	/**
	 * The one the collection mapping puts in doubt.
	 *
	 * <p>
	 * The second save is where {@code setDecorSelections} meets a collection
	 * the persistence context is already tracking. If replacing the reference
	 * under {@code orphanRemoval} is going to throw, or is going to delete the
	 * existing rows without writing the new ones, it happens here and not on
	 * the first save.
	 */
	@Test
	@DisplayName("saving a second time replaces the décor rather than losing it")
	void asecondSaveKeepsTheDecor() throws Exception {
		EventMaster event = seedEvent();
		DecorCategoryMaster stage = seedDecorCategory("Stage");

		save(withDecor(event, stage, "Ivory and gold, please."));
		EventMaster afterFirst = reload(event);

		save(withDecor(afterFirst, stage, "Ivory and silver instead."));

		assertThat(storedDecorOf(event))
				.as("saving the décor step twice left the booking with the wrong number of choices")
				.hasSize(1)
				.first()
				.satisfies(selection -> assertThat(selection.getTxtRemarks())
						.as("the second save did not take")
						.isEqualTo("Ivory and silver instead."));
	}

	/**
	 * Décor is not wiped by a step that has never heard of it.
	 *
	 * <p>
	 * Every step of the journey posts the whole booking back, so this is the
	 * rule that stops the notes screen deleting the décor the customer chose
	 * four screens earlier. It is the same rule the date, the suppliers and the
	 * menu selections each follow.
	 */
	@Test
	@DisplayName("a save that never mentions décor leaves what was chosen alone")
	void anOmittedDecorListIsKept() throws Exception {
		EventMaster event = seedEvent();
		DecorCategoryMaster stage = seedDecorCategory("Stage");

		save(withDecor(event, stage, "Ivory and gold, please."));
		assertThat(storedDecorOf(event))
				.as("the fixture stored no décor, so this test would prove nothing")
				.hasSize(1);

		save(barelyEnoughToSave(reload(event)));

		assertThat(storedDecorOf(event))
				.as("a save that said nothing about décor deleted the customer's choices")
				.hasSize(1);
	}

	// ── fixture ──────────────────────────────────────────────────────────

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

	private DecorCategoryMaster seedDecorCategory(String name) {
		DecorCategoryMaster category = new DecorCategoryMaster();
		category.setTxtDecorCategoryCode(MARKER + "-" + System.nanoTime());
		category.setTxtDecorCategoryName(MARKER + " " + name);
		category.setBlnIsActive(true);
		category.setBlnIsDeleted(false);
		return repositoryDecorCategoryMaster.saveAndFlush(category);
	}

	private DtoEventMaster barelyEnoughToSave(EventMaster event) {
		DtoEventMaster dto = new DtoEventMaster();
		dto.setSerEventMasterId(event.getSerEventMasterId());
		dto.setSerCustId(customer.getSerCustId());
		dto.setSerEventTypeId(eventType.getSerEventTypeId());
		dto.setIsEditAllowed(true);
		dto.setNumVersion(event.getNumVersion());
		return dto;
	}

	private DtoEventMaster withDecor(EventMaster event, DecorCategoryMaster category, String remarks) {
		DtoEventDecorCategorySelection choice = new DtoEventDecorCategorySelection();
		choice.setSerDecorCategoryId(category.getSerDecorCategoryId());
		choice.setTxtDecorCategoryCode(category.getTxtDecorCategoryCode());
		choice.setTxtDecorCategoryName(category.getTxtDecorCategoryName());
		choice.setTxtRemarks(remarks);

		DtoEventMaster dto = barelyEnoughToSave(event);
		dto.setDtoEventDecorSelections(List.of(choice));
		return dto;
	}

	private EventMaster reload(EventMaster event) {
		return repositoryEventMaster.findById(event.getSerEventMasterId()).orElseThrow();
	}

	private List<com.zbs.de.model.EventDecorCategorySelection> storedDecorOf(EventMaster event) {
		return repositoryEventDecorCategorySelection.findAll().stream()
				.filter(s -> s.getEventMaster() != null
						&& s.getEventMaster().getSerEventMasterId().equals(event.getSerEventMasterId()))
				.toList();
	}
}
