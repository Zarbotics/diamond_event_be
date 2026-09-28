package com.zbs.de.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
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
import com.zbs.de.model.DecorCategoryMaster;
import com.zbs.de.model.EventType;
import com.zbs.de.model.dto.DtoEventDecorCategorySelection;
import com.zbs.de.model.dto.DtoEventMaster;
import com.zbs.de.model.dto.DtoEventPricePreview;
import com.zbs.de.model.dto.DtoEventQuoteAndStatus;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryCustomerMaster;
import com.zbs.de.repository.RepositoryDecorCategoryMaster;
import com.zbs.de.repository.RepositoryEventType;

/**
 * What a booking would come to, worked out without saving it.
 *
 * <h2>Why the server answers this</h2>
 *
 * So there is one implementation of what anything costs. The control panel adds
 * the booking up itself, so the total pinned beside the form moves while
 * somebody is changing the menu — a second set of pricing rules, written at a
 * different time from the server's. Two sets that both look right is the
 * problem the whole pricing thread has been unwinding; this is what lets the
 * second one go.
 *
 * <h2>What is asserted</h2>
 *
 * That it prices, that it explains itself, that it refuses to pretend when it
 * cannot price, and — the one that matters most — that it writes nothing.
 * Somebody moving the guest count up and down to see what happens is not
 * editing the booking.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class PricePreviewIT {

	private static final String MARKER = "IT-PREVIEW";

	@Autowired
	private ServiceEventMaster serviceEventMaster;

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

	@AfterEach
	void removeSeed() {
		jdbcTemplate.update("DELETE FROM customer_master WHERE txt_cust_code LIKE ?", MARKER + "%");
		jdbcTemplate.update("DELETE FROM decor_category_master WHERE txt_decor_category_code LIKE ?", MARKER + "%");

		/*
		 * And the event type. Leaving it behind broke RunningOrderMomentsIT,
		 * which asserts that every event type has the full set of running order
		 * moments — a type seeded straight into the table after V26 has none,
		 * and that assertion is the guarantee that nothing narrows and no
		 * stored time is lost. The test was right and this fixture was wrong.
		 */
		jdbcTemplate.update("DELETE FROM event_type_running_order_moment WHERE ser_event_type_id IN ("
				+ "  SELECT ser_event_type_id FROM event_type WHERE txt_event_type_code LIKE ?)", MARKER + "%");
		jdbcTemplate.update("DELETE FROM event_type WHERE txt_event_type_code LIKE ?", MARKER + "%");
		eventType = null;
		customer = null;
	}

	// ── the tests ────────────────────────────────────────────────────────

	@Test
	@DisplayName("prices a booking that has never been saved")
	void pricesADraft() {
		DtoEventPricePreview preview = priceOf(aDraftWithDecor("1000.00", 200, 20, null));

		assertThat(preview.getNumDecor()).isEqualByComparingTo("1000.00");
		assertThat(preview.getNumVat())
				.as("décor carries VAT at 20%, food does not")
				.isEqualByComparingTo("200.00");
		assertThat(preview.getNumTotal()).isEqualByComparingTo("1200.00");
	}

	@Test
	@DisplayName("takes off a discount that has not been saved either")
	void appliesADiscount() {
		DtoEventPricePreview preview = priceOf(aDraftWithDecor("1000.00", 200, 20, new BigDecimal("150.00")));

		assertThat(preview.getNumDiscount()).isEqualByComparingTo("150.00");
		assertThat(preview.getNumTotal())
				.as("£1,000 plus £200 VAT, less £150")
				.isEqualByComparingTo("1050.00");
	}

	/**
	 * A total nobody can take apart is a total nobody can defend to a customer
	 * who queries it.
	 */
	@Test
	@DisplayName("shows its working, with the reason in words")
	void showsItsWorking() {
		DtoEventPricePreview preview = priceOf(aDraftWithDecor("1000.00", 200, 20, null));

		assertThat(preview.getLines()).isNotEmpty();
		assertThat(preview.getLines()).allSatisfy(line -> {
			assertThat(line.getTxtSection()).isNotBlank();
			assertThat(line.getTxtDescription()).isNotBlank();
			assertThat(line.getNumLineTotal()).isNotNull();
		});
	}

	/**
	 * The same guard the save has. A per-guest price multiplied by a guest count
	 * nobody entered is zero, and £0.00 looks exactly like a quote — so the
	 * preview says what it could not work out rather than answering nothing and
	 * letting the screen show it as a price.
	 */
	@Test
	@DisplayName("says what it could not price rather than answering nothing")
	void refusesToPretend() {
		DtoEventPricePreview preview = priceOf(aDraftWithDecor("1000.00", null, null, null));

		/* Décor is flat, so it still prices — what matters is that nothing
		   claims to be a finished figure when a count is missing. */
		assertThat(preview.getTxtCannotPrice()).isNotNull();
	}

	/**
	 * The one that matters most.
	 *
	 * <p>
	 * A preview is a question, not an instruction. Somebody moving the guest
	 * count up and down to see what happens is not editing the booking, and a
	 * preview that wrote would turn every keystroke into a revision of a
	 * customer's quote.
	 */
	@Test
	@DisplayName("writes nothing at all")
	void writesNothing() {
		long eventsBefore = countOf("event_master");
		long budgetsBefore = countOf("event_budget");
		long linesBefore = countOf("event_price_line");
		long decorBefore = countOf("event_decor_category_selection");

		priceOf(aDraftWithDecor("1000.00", 200, 20, null));
		priceOf(aDraftWithDecor("2000.00", 400, 40, null));

		assertThat(countOf("event_master")).isEqualTo(eventsBefore);
		assertThat(countOf("event_budget")).isEqualTo(budgetsBefore);
		assertThat(countOf("event_price_line"))
				.as("priceAndRecord writes lines; priceFor must not")
				.isEqualTo(linesBefore);
		assertThat(countOf("event_decor_category_selection")).isEqualTo(decorBefore);
	}

	// ── helpers ──────────────────────────────────────────────────────────

	private long countOf(String table) {
		return jdbcTemplate.queryForObject("SELECT count(*) FROM " + table, Long.class);
	}

	private DtoEventPricePreview priceOf(DtoEventMaster draft) {
		DtoResult result = serviceEventMaster.pricePreview(draft);

		assertThat(result.getTxtMessage())
				.as("the preview failed: %s", result.getResult())
				.isEqualTo("Success");

		return (DtoEventPricePreview) result.getResult();
	}

	private DtoEventMaster aDraftWithDecor(String price, Integer guests, Integer tables, BigDecimal discount) {
		seedCustomerAndType();

		DecorCategoryMaster category = new DecorCategoryMaster();
		category.setTxtDecorCategoryCode(MARKER + "-" + System.nanoTime());
		category.setTxtDecorCategoryName(MARKER + " Stage");
		category.setNumPrice(new BigDecimal(price));
		category.setBlnIsActive(true);
		category.setBlnIsDeleted(false);
		category = repositoryDecorCategoryMaster.saveAndFlush(category);

		DtoEventDecorCategorySelection chosen = new DtoEventDecorCategorySelection();
		chosen.setSerDecorCategoryId(category.getSerDecorCategoryId());
		chosen.setNumPrice(new BigDecimal(price));

		List<DtoEventDecorCategorySelection> decor = new ArrayList<>();
		decor.add(chosen);

		DtoEventMaster draft = new DtoEventMaster();
		draft.setSerCustId(customer.getSerCustId());
		draft.setSerEventTypeId(eventType.getSerEventTypeId());
		draft.setNumNumberOfGuests(guests);
		draft.setNumNumberOfTables(tables);
		draft.setDtoEventDecorSelections(decor);

		if (discount != null) {
			DtoEventQuoteAndStatus money = new DtoEventQuoteAndStatus();
			money.setNumDiscount(discount);
			draft.setDtoEventQuoteAndStatus(money);
		}

		return draft;
	}

	private void seedCustomerAndType() {
		if (customer == null) {
			customer = new CustomerMaster();
			customer.setTxtCustCode(MARKER + "-" + System.nanoTime());
			customer.setTxtCustName(MARKER + " customer");
			customer = repositoryCustomerMaster.save(customer);
		}

		if (eventType == null) {
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
		}
	}
}
