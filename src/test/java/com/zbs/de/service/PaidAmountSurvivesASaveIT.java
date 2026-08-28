package com.zbs.de.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;

import com.zbs.de.model.CustomerMaster;
import com.zbs.de.model.EventMaster;
import com.zbs.de.model.EventType;
import com.zbs.de.model.UserMaster;
import com.zbs.de.model.dto.DtoEventMaster;
import com.zbs.de.model.dto.DtoEventQuoteAndStatus;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryCustomerMaster;
import com.zbs.de.repository.RepositoryEventMaster;
import com.zbs.de.repository.RepositoryEventType;

/**
 * Money taken is not forgotten by the next save. See PLATFORM.md §15.3 stage 3.
 *
 * <h2>What happened</h2>
 *
 * Two things owned {@code event_budget.num_paid_amount}.
 * {@code recalculateBudget} sums the payments recorded against the budget; the
 * four event-save methods set it to whatever the form sent. The form sends the
 * figure it loaded — read before the payment was taken — and it runs last.
 *
 * <p>
 * Production carries the result. Budget 146 reads {@code num_paid_amount = 0.00}
 * against a recorded payment of £500, updated four seconds after the payment.
 * The payment row is still there; the booking simply reads as unpaid, on the
 * screen, on the report and in every total the office works from.
 *
 * <h2>What is worth asserting</h2>
 *
 * The sequence, in order, through the service — take the payment, then save the
 * booking from a form that does not know about it. Asserting the helper in
 * isolation would have passed on the day the fault was live, because the helper
 * is not where the fault was: the fault was that four save paths each wrote the
 * field themselves.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class PaidAmountSurvivesASaveIT {

	private static final String MARKER = "IT-PAID";

	private static final long SOMEBODY = 90_301L;

	private static final DateTimeFormatter WIRE = DateTimeFormatter.ofPattern("dd-MM-yyyy");

	/** Far enough out that the capacity rule has nothing to say. */
	private static final LocalDate FREE_DAY = LocalDate.now(ZoneOffset.UTC).plusYears(8);

	@Autowired
	private ServiceEventMaster serviceEventMaster;

	@Autowired
	private RepositoryEventMaster repositoryEventMaster;

	@Autowired
	private RepositoryCustomerMaster repositoryCustomerMaster;

	@Autowired
	private RepositoryEventType repositoryEventType;

	private CustomerMaster customer;
	private EventType eventType;
	private String run;

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

	@BeforeEach
	void seedTheParties() {
		// Signed in, because the create paths notify the office and that step
		// reads the current user without checking.
		UserMaster user = new UserMaster();
		user.setSerUserId(SOMEBODY);
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(user, null, List.of()));

		run = String.valueOf(System.nanoTime());

		customer = new CustomerMaster();
		customer.setTxtCustCode(MARKER + "-" + run);
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
	}

	/** Same reasoning as BookingCreatedWithEventIT: the catalogue, not a list. */
	@AfterEach
	void removeSeed() throws Exception {
		SecurityContextHolder.clearContext();

		String myEvents = "SELECT ser_event_master_id FROM event_master WHERE txt_event_master_name LIKE '"
				+ MARKER + "%'";

		try (Connection connection = connect(); Statement remove = connection.createStatement()) {

			// Payments hang off budgets, which hang off events, so they go first
			// and by their own route.
			remove.executeUpdate("""
					DELETE FROM event_payment WHERE ser_event_budget_id IN (
					    SELECT ser_event_budget_id FROM event_budget WHERE ser_event_master_id IN (%s))
					""".formatted(myEvents));

			List<String[]> children = new ArrayList<>();
			try (ResultSet found = remove.executeQuery("""
					SELECT child.relname, a.attname
					FROM pg_constraint c
					JOIN pg_class child ON child.oid = c.conrelid
					JOIN pg_class parent ON parent.oid = c.confrelid
					JOIN unnest(c.conkey) k(attnum) ON true
					JOIN pg_attribute a ON a.attrelid = child.oid AND a.attnum = k.attnum
					WHERE c.contype = 'f' AND parent.relname = 'event_master'
					""")) {
				while (found.next()) {
					children.add(new String[] { found.getString(1), found.getString(2) });
				}
			}

			for (String[] child : children) {
				remove.executeUpdate(
						"DELETE FROM " + child[0] + " WHERE " + child[1] + " IN (" + myEvents + ")");
			}

			remove.executeUpdate("DELETE FROM event_master WHERE txt_event_master_name LIKE '" + MARKER + "%'");
			remove.executeUpdate("""
					DELETE FROM booking WHERE ser_cust_id IN (
					    SELECT ser_cust_id FROM customer_master WHERE txt_cust_code LIKE '%s%%')
					""".formatted(MARKER));
			remove.executeUpdate("DELETE FROM customer_master WHERE txt_cust_code LIKE '" + MARKER + "%'");
		}
	}

	// -----------------------------------------------------------------

	/**
	 * The path the admin portal's event form actually takes.
	 *
	 * <p>
	 * Not {@code saveAndUpdate}: that one — {@code /eventMaster/saveOrUpdate} —
	 * writes the event and never touches its budget at all, so a test driving it
	 * would find no budget to assert against and would have proved nothing about
	 * the money.
	 */
	private DtoResult saveThrough(DtoEventMaster dto) {
		try {
			return serviceEventMaster.saveAndUpdateWithDocs(dto, null);
		} catch (Exception e) {
			throw new AssertionError("the save threw", e);
		}
	}

	private String nameOf(String name) {
		return MARKER + "-" + run + " " + name;
	}

	/**
	 * A booking as the event form sends one.
	 *
	 * @param paidAmount what the form believes has been paid, which is the whole
	 *                   point: it is the figure the screen loaded, and it is
	 *                   stale the moment a payment is taken elsewhere
	 */
	private DtoEventMaster eventFormSaying(String name, BigDecimal paidAmount, Integer existingId) {
		DtoEventMaster dto = new DtoEventMaster();
		dto.setSerEventMasterId(existingId);
		dto.setSerCustId(customer.getSerCustId());
		dto.setSerEventTypeId(eventType.getSerEventTypeId());
		dto.setTxtEventMasterName(nameOf(name));
		dto.setDteEventDate(FREE_DAY.format(WIRE));
		dto.setNumNumberOfGuests(120);
		dto.setIsEditAllowed(true);

		DtoEventQuoteAndStatus quote = new DtoEventQuoteAndStatus();
		quote.setNumQuotedPrice(new BigDecimal("2520.00"));
		quote.setNumPaidAmount(paidAmount);
		quote.setTxtStatus("Confirmed");
		dto.setDtoEventQuoteAndStatus(quote);

		return dto;
	}

	private EventMaster savedEvent(DtoResult result, String name) {
		assertThat(result.getTxtMessage())
				.as("the save was refused, so nothing here is being tested")
				.isEqualTo("Success");

		return repositoryEventMaster.findAll().stream()
				.filter(e -> nameOf(name).equals(e.getTxtEventMasterName()))
				.findFirst()
				.orElseThrow(() -> new AssertionError("the save reported Success but created no event"));
	}

	private long budgetIdOf(EventMaster event) throws Exception {
		return scalar("SELECT ser_event_budget_id FROM event_budget WHERE ser_event_master_id = "
				+ event.getSerEventMasterId());
	}

	private BigDecimal paidOn(long budgetId) throws Exception {
		try (Connection connection = connect();
				Statement query = connection.createStatement();
				ResultSet result = query.executeQuery(
						"SELECT num_paid_amount FROM event_budget WHERE ser_event_budget_id = " + budgetId)) {
			result.next();
			return result.getBigDecimal(1);
		}
	}

	private long scalar(String sql) throws Exception {
		try (Connection connection = connect();
				Statement query = connection.createStatement();
				ResultSet result = query.executeQuery(sql)) {
			result.next();
			return result.getLong(1);
		}
	}

	/** A payment taken through the till, while the form sits open elsewhere. */
	private void recordPayment(long budgetId, EventMaster event, String amount) throws Exception {
		try (Connection connection = connect(); Statement take = connection.createStatement()) {
			take.executeUpdate("""
					INSERT INTO event_payment (ser_event_budget_id, ser_event_master_id, num_amount,
					                           dte_payment_date, txt_payment_status, bln_is_deleted)
					VALUES (%d, %d, %s, now(), 'RECEIVED', false)
					""".formatted(budgetId, event.getSerEventMasterId(), amount));

			// What ServiceEventPaymentImpl does next, through recalculateBudget.
			take.executeUpdate(
					"UPDATE event_budget SET num_paid_amount = " + amount
							+ " WHERE ser_event_budget_id = " + budgetId);
		}
	}

	// -----------------------------------------------------------------

	@Test
	@DisplayName("a payment is not written back to zero by the next save of the booking")
	void aPaymentSurvivesTheNextSave() throws Exception {
		/*
		 * The exact sequence that happened in production, four seconds apart:
		 * the booking is open on somebody's screen, a payment is taken, and then
		 * the booking is saved carrying the zero it loaded.
		 */
		EventMaster event = savedEvent(
				saveThrough(eventFormSaying("walima", BigDecimal.ZERO, null)), "walima");

		long budgetId = budgetIdOf(event);
		recordPayment(budgetId, event, "500.00");

		assertThat(paidOn(budgetId))
				.as("the payment was not recorded at all, so the rest of this proves nothing")
				.isEqualByComparingTo("500.00");

		saveThrough(
				eventFormSaying("walima", BigDecimal.ZERO, event.getSerEventMasterId()));

		assertThat(paidOn(budgetId))
				.as("""
						saving the booking wrote the paid amount back to what the form was holding, \
						so £500 that was actually taken has disappeared from every figure the office \
						works from""")
				.isEqualByComparingTo("500.00");
	}

	@Test
	@DisplayName("the payments are the authority even when the form sends a different figure")
	void aWrongFigureFromTheFormDoesNotWin() throws Exception {
		// Not only zero. A form holding a stale £100 must not overwrite £500
		// either — the failure is "the form decides", not "zero is bad".
		EventMaster event = savedEvent(
				saveThrough(eventFormSaying("nikkah", BigDecimal.ZERO, null)), "nikkah");

		long budgetId = budgetIdOf(event);
		recordPayment(budgetId, event, "500.00");

		saveThrough(
				eventFormSaying("nikkah", new BigDecimal("100.00"), event.getSerEventMasterId()));

		assertThat(paidOn(budgetId))
				.as("a figure typed into the form overruled the payments actually recorded")
				.isEqualByComparingTo("500.00");
	}

	@Test
	@DisplayName("with no payments recorded, the figure the form sends is still used")
	void theFormStillDecidesWhereThereAreNoPayments() throws Exception {
		/*
		 * The half that must not break. The catering form has a Paid Amount box
		 * and it is the only record of money taken for a delivery — neither
		 * production payment belongs to one. Narrowing this to "payments only"
		 * would quietly stop that box working.
		 */
		EventMaster event = savedEvent(
				saveThrough(
						eventFormSaying("mehndi", new BigDecimal("250.00"), null)),
				"mehndi");

		assertThat(paidOn(budgetIdOf(event)))
				.as("a paid amount typed in where no payments are recorded was thrown away")
				.isEqualByComparingTo("250.00");
	}
}
