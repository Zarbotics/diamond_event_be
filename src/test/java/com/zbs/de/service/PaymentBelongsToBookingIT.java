package com.zbs.de.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.zbs.de.model.Booking;
import com.zbs.de.model.CustomerMaster;
import com.zbs.de.model.EventBudget;
import com.zbs.de.model.EventMaster;
import com.zbs.de.model.EventPayment;
import com.zbs.de.model.EventType;
import com.zbs.de.repository.RepositoryBooking;
import com.zbs.de.repository.RepositoryCustomerMaster;
import com.zbs.de.repository.RepositoryEventBudget;
import com.zbs.de.repository.RepositoryEventMaster;
import com.zbs.de.repository.RepositoryEventPaymentMaster;
import com.zbs.de.repository.RepositoryEventType;

/**
 * A payment belongs to the booking, not to one day of it.
 *
 * <h2>What this is for</h2>
 *
 * A deposit is paid for a wedding. Today the money hangs off {@code
 * event_budget}, which hangs off one {@code event_master} row, so a family
 * paying £2,000 across a mehndi, a nikkah and a walima has it recorded against
 * one of the three days — and every total the office works from is a day's
 * total rather than the wedding's.
 *
 * <p>
 * Stage 3 of §15.3 adds the second parent and fills it. One booking still has
 * one event, so nothing observable changes; that is the method, not an
 * accident. The column is filled while it cannot matter, so that stage 4 —
 * "add another day to this wedding" — is a change to behaviour rather than a
 * change to behaviour plus a migration of live payment records.
 *
 * <h2>The failure these guard</h2>
 *
 * {@code EventMaster.serBookingId} carries a long comment about why its column
 * is {@code updatable = false}: this codebase saves detached entities built
 * from DTOs, no DTO carries a booking id, and an updatable column would have
 * Hibernate write NULL over the parent on the first save of each row — undoing
 * the migration one booking at a time, silently, starting with the ones people
 * touch most. The payment column has the same property for the same reason,
 * and {@link #resavingAPaymentKeepsItsBooking()} is that failure written down.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class PaymentBelongsToBookingIT {

	private static final String MARKER = "IT-PAYBOOK";

	private static final LocalDate DAY = LocalDate.now(ZoneOffset.UTC).plusYears(8);

	@Autowired
	private RepositoryEventPaymentMaster repositoryEventPayment;

	@Autowired
	private RepositoryEventBudget repositoryEventBudget;

	@Autowired
	private RepositoryEventMaster repositoryEventMaster;

	@Autowired
	private RepositoryCustomerMaster repositoryCustomerMaster;

	@Autowired
	private RepositoryEventType repositoryEventType;

	@Autowired
	private RepositoryBooking repositoryBooking;

	private CustomerMaster customer;

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
		java.util.List<Integer> mine = repositoryCustomerMaster.findAll().stream()
				.filter(c -> c.getTxtCustCode() != null && c.getTxtCustCode().startsWith(MARKER))
				.map(CustomerMaster::getSerCustId)
				.toList();

		/*
		  Events first, by id, then budgets by that id.

		  The obvious teardown — filter budgets on
		  `b.getEventMaster().getCustomerMaster()` — reads through two lazy
		  proxies outside any session and threw LazyInitializationException on
		  every test in the class. Asking a proxy for its *identifier* does not
		  initialise it, so the ids are collected first and the budgets matched
		  against those.
		*/
		java.util.Set<Integer> myEvents = repositoryEventMaster.findAll().stream()
				.filter(e -> e.getCustomerMaster() != null
						&& mine.contains(e.getCustomerMaster().getSerCustId()))
				.map(EventMaster::getSerEventMasterId)
				.collect(java.util.stream.Collectors.toSet());

		repositoryEventBudget.findAll().stream()
				.filter(b -> b.getEventMaster() != null
						&& myEvents.contains(b.getEventMaster().getSerEventMasterId()))
				.forEach(b -> {
					repositoryEventPayment.findAll().stream()
							.filter(pay -> pay.getEventBudget() != null && b.getSerEventBudgetId()
									.equals(pay.getEventBudget().getSerEventBudgetId()))
							.forEach(repositoryEventPayment::delete);
					repositoryEventBudget.delete(b);
				});

		repositoryEventMaster.findAll().stream()
				.filter(e -> myEvents.contains(e.getSerEventMasterId()))
				.forEach(repositoryEventMaster::delete);

		// Bookings before customers: booking.ser_cust_id references the customer.
		repositoryBooking.findAll().stream()
				.filter(b -> b.getTxtBookingCode() != null && b.getTxtBookingCode().startsWith(MARKER))
				.forEach(repositoryBooking::delete);

		repositoryCustomerMaster.findAll().stream()
				.filter(c -> c.getTxtCustCode() != null && c.getTxtCustCode().startsWith(MARKER))
				.forEach(repositoryCustomerMaster::delete);
	}

	/** A booking, one event under it, and a budget on that event. */
	private EventBudget seedBudget() {
		customer = new CustomerMaster();
		customer.setTxtCustCode(MARKER + "-" + System.nanoTime());
		customer.setTxtCustName(MARKER + " customer");
		customer = repositoryCustomerMaster.save(customer);

		EventType eventType = repositoryEventType.findAll().stream()
				.filter(t -> t.getTxtEventTypeCode() != null && t.getTxtEventTypeCode().startsWith(MARKER))
				.findFirst()
				.orElseGet(() -> {
					EventType seeded = new EventType();
					seeded.setTxtEventTypeCode(MARKER + "-TYPE");
					seeded.setTxtEventTypeName(MARKER + " walima");
					seeded.setBlnIsMainEvent(true);
					return repositoryEventType.save(seeded);
				});

		Booking booking = new Booking();
		booking.setTxtBookingCode(MARKER + "-" + System.nanoTime());
		booking.setCustomerMaster(customer);
		booking = repositoryBooking.save(booking);

		EventMaster event = new EventMaster();
		event.setTxtEventMasterName(MARKER + " walima");
		event.setTxtEventMasterCode(MARKER + "-" + System.nanoTime());
		event.setDteEventDate(Date.from(DAY.atStartOfDay(ZoneOffset.UTC).toInstant()));
		event.setCustomerMaster(customer);
		event.setEventType(eventType);
		event.setSerBookingId(booking.getSerBookingId());
		event.setBlnIsActive(true);
		event.setBlnIsDeleted(false);
		event = repositoryEventMaster.saveAndFlush(event);

		EventBudget budget = new EventBudget();
		budget.setEventMaster(event);
		budget.setNumPaidAmount(BigDecimal.ZERO);
		return repositoryEventBudget.saveAndFlush(budget);
	}

	private EventPayment aPaymentOf(EventBudget budget, String amount) {
		EventPayment payment = new EventPayment();
		payment.attachTo(budget);
		payment.setNumAmount(new BigDecimal(amount));
		payment.setDtePaymentDate(new Date());
		return repositoryEventPayment.saveAndFlush(payment);
	}

	@Test
	@DisplayName("a payment taken against an event is filed under that event's booking")
	void aNewPaymentCarriesTheBooking() {
		EventBudget budget = seedBudget();
		Long booking = budget.getEventMaster().getSerBookingId();
		assertThat(booking).as("the fixture made no booking, so this proves nothing").isNotNull();

		EventPayment saved = aPaymentOf(budget, "500.00");

		assertThat(repositoryEventPayment.findById(saved.getSerEventPaymentId()).orElseThrow()
				.getSerBookingId())
				.as("the payment was not filed under the booking its event belongs to")
				.isEqualTo(booking);
	}

	@Test
	@DisplayName("re-saving a payment does not write null over its booking")
	void resavingAPaymentKeepsItsBooking() {
		/*
			The failure the column's `updatable = false` exists to prevent, and
			the reason this test is worth more than the one above.

			Every save in this codebase writes a detached entity assembled from a
			DTO — the id, and the fields the caller sent. No DTO carries a
			booking id. With the column updatable, Hibernate would include it in
			the UPDATE as null, and the backfill would come undone one payment at
			a time, starting with the bookings the office edits most.
		*/
		EventBudget budget = seedBudget();
		Long booking = budget.getEventMaster().getSerBookingId();
		EventPayment saved = aPaymentOf(budget, "500.00");

		// A payment as a save path builds one: the id, and what the form sent.
		EventPayment asTheFormSendsIt = new EventPayment();
		asTheFormSendsIt.setSerEventPaymentId(saved.getSerEventPaymentId());
		asTheFormSendsIt.setEventBudget(budget);
		asTheFormSendsIt.setNumAmount(new BigDecimal("750.00"));
		asTheFormSendsIt.setDtePaymentDate(new Date());
		repositoryEventPayment.saveAndFlush(asTheFormSendsIt);

		EventPayment reloaded = repositoryEventPayment.findById(saved.getSerEventPaymentId()).orElseThrow();
		assertThat(reloaded.getNumAmount())
				.as("the save did not take effect, so this test is not exercising a save")
				.isEqualByComparingTo("750.00");
		assertThat(reloaded.getSerBookingId())
				.as("re-saving a payment cleared the booking it belongs to")
				.isEqualTo(booking);
	}

	@Test
	@DisplayName("a budget with no event leaves the booking alone rather than guessing")
	void aBudgetWithNoEventHasNoBooking() {
		/*
			A catering delivery's budget hangs off catering_delivery_booking — a
			different thing with a confusingly similar name — and has no event
			and therefore no wedding. Its payments keep a null booking, and that
			is correct rather than missing.
		*/
		EventPayment payment = new EventPayment();
		payment.attachTo(null);

		assertThat(payment.getSerBookingId())
				.as("a payment with no event was given a booking from somewhere")
				.isNull();
	}

	@Test
	@DisplayName("the payment still reads through its budget, which nothing has moved off yet")
	void theBudgetIsStillTheParent() {
		/*
			Stage 3 adds a parent; it does not move anything. Every read path in
			both frontends still goes through the budget, and stage 5 is the only
			stage that removes it. A change here that quietly detached payments
			from budgets would take the paid-amount total with it.
		*/
		EventBudget budget = seedBudget();
		EventPayment saved = aPaymentOf(budget, "500.00");

		EventPayment reloaded = repositoryEventPayment.findById(saved.getSerEventPaymentId()).orElseThrow();
		assertThat(reloaded.getEventBudget().getSerEventBudgetId())
				.as("the payment lost its budget, which is what every total is still read from")
				.isEqualTo(budget.getSerEventBudgetId());

		assertThat(repositoryEventPayment.sumPaidByBudgetId(budget.getSerEventBudgetId()))
				.as("the budget's paid total no longer sees the payment")
				.isEqualByComparingTo("500.00");
	}
}
