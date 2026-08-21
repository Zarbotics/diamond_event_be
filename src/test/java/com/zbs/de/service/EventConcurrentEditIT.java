package com.zbs.de.service;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;

import com.zbs.de.model.CustomerMaster;
import com.zbs.de.model.EventMaster;
import com.zbs.de.model.EventType;
import com.zbs.de.model.UserMaster;
import com.zbs.de.model.dto.DtoEventMaster;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryCustomerMaster;
import com.zbs.de.repository.RepositoryEventMaster;
import com.zbs.de.repository.RepositoryEventType;
import com.zbs.de.util.UtilDateAndTime;

/**
 * Two people editing one booking.
 *
 * <h2>The failure</h2>
 *
 * An administrator opens a booking to change the guest count. The customer, in
 * the journey, is picking their menu on the same booking. Both press save. One
 * set of changes is simply gone — nothing in either interface says so, nothing
 * in the log says so, and it is found weeks later when the kitchen cooks for
 * the wrong number of people.
 *
 * <p>
 * This is not a rare race. The two saves are minutes apart, which is why
 * {@code @Version} alone does not catch it: there is no overlapping transaction
 * for the database to notice. What catches it is the version the client fetched
 * travelling back with the save.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class EventConcurrentEditIT {

	private static final String MARKER = "IT-CONCURRENT";

	/** Two different people, which is the whole point of the guard. */
	private static final long ADMINISTRATOR = 90_001L;
	private static final long CUSTOMER = 90_002L;

	/** Far enough out that nothing else in the suite is using it. */
	private static final LocalDate DAY = LocalDate.now(ZoneOffset.UTC).plusYears(6);

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
		repositoryEventMaster.findAll().stream()
				.filter(e -> e.getTxtEventMasterName() != null && e.getTxtEventMasterName().startsWith(MARKER))
				.forEach(repositoryEventMaster::delete);

		repositoryCustomerMaster.findAll().stream()
				.filter(c -> c.getTxtCustCode() != null && c.getTxtCustCode().startsWith(MARKER))
				.forEach(repositoryCustomerMaster::delete);
	}

	private EventMaster seedEvent() {
		customer = new CustomerMaster();
		customer.setTxtCustCode(MARKER + "-" + System.nanoTime());
		customer.setTxtCustName(MARKER + " customer");
		customer = repositoryCustomerMaster.save(customer);

		/*
		 * Seeded rather than borrowed. An empty event_type table would otherwise
		 * skip this class silently, and a concurrency guard that quietly stops
		 * being exercised is worse than no guard at all.
		 */
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


	/**
	 * Save as a particular person.
	 *
	 * <p>
	 * The guard asks who saved last, not only how old the caller's copy is — so
	 * these tests have to be two different people for it to mean anything. A
	 * suite that ran everything as nobody would pass whether the check worked or
	 * not.
	 */
	private void savingAs(long userId) {
		UserMaster user = new UserMaster();
		user.setSerUserId(userId);
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(user, null, java.util.List.of()));
	}

	@org.junit.jupiter.api.AfterEach
	void clearWhoIsSaving() {
		SecurityContextHolder.clearContext();
	}

	/** The event as a client holds it: the fields it edits, plus the version. */
	private DtoEventMaster asClientCopy(EventMaster event, Integer guests) {
		DtoEventMaster dto = new DtoEventMaster();
		dto.setSerEventMasterId(event.getSerEventMasterId());
		dto.setSerCustId(customer.getSerCustId());
		dto.setSerEventTypeId(eventType.getSerEventTypeId());
		dto.setTxtEventMasterName(event.getTxtEventMasterName());
		dto.setDteEventDate(UtilDateAndTime.mmddyyyyDateToString(event.getDteEventDate()));
		dto.setNumNumberOfGuests(guests);
		dto.setIsEditAllowed(true);
		dto.setNumVersion(event.getNumVersion());
		return dto;
	}

	@Test
	@DisplayName("a version is issued with the event and moves on when it is saved")
	void theVersionIsIssuedAndAdvances() {
		EventMaster event = seedEvent();

		DtoEventMaster asFetched = serviceEventMaster.getEventById(event.getSerEventMasterId());
		assertThat(asFetched.getNumVersion())
				.as("the event was handed out with no version, so no client can send one back")
				.isNotNull();

		serviceEventMaster.saveAndUpdate(asClientCopy(event, 150));

		DtoEventMaster afterSave = serviceEventMaster.getEventById(event.getSerEventMasterId());
		assertThat(afterSave.getNumVersion())
				.as("the version did not move, so a stale copy is indistinguishable from a fresh one")
				.isGreaterThan(asFetched.getNumVersion());
	}

	@Test
	@DisplayName("the second person to save a booking is told, rather than winning")
	void theSecondSaveIsRefused() {
		EventMaster event = seedEvent();

		// Both open the same booking. Same version in both hands.
		DtoEventMaster administrator = asClientCopy(event, 250);
		DtoEventMaster customerCopy = asClientCopy(event, 300);

		savingAs(ADMINISTRATOR);
		DtoResult first = serviceEventMaster.saveAndUpdate(administrator);
		assertThat(first.getTxtMessage()).isNotEqualTo("changed_elsewhere");

		savingAs(CUSTOMER);
		DtoResult second = serviceEventMaster.saveAndUpdate(customerCopy);

		assertThat(second.getTxtMessage())
				.as("the second save overwrote the first without anybody being told")
				.isEqualTo("changed_elsewhere");
		assertThat(String.valueOf(second.getResult()))
				.as("the refusal says nothing a person could act on")
				.contains("Refresh");

		// And the first person's change is still there.
		assertThat(repositoryEventMaster.findById(event.getSerEventMasterId()).orElseThrow()
				.getNumNumberOfGuests())
				.as("the refused save was applied anyway")
				.isEqualTo(250);
	}

	@Test
	@DisplayName("re-fetching after a refusal lets the change be made properly")
	void refetchingResolvesIt() {
		/*
		 * The refusal is only reasonable if the way out of it is obvious and it
		 * works. Somebody who reloads and reapplies their change must not hit the
		 * same wall a second time.
		 */
		EventMaster event = seedEvent();

		savingAs(ADMINISTRATOR);
		serviceEventMaster.saveAndUpdate(asClientCopy(event, 250));

		savingAs(CUSTOMER);
		EventMaster refetched = repositoryEventMaster.findById(event.getSerEventMasterId()).orElseThrow();
		DtoResult retried = serviceEventMaster.saveAndUpdate(asClientCopy(refetched, 300));

		assertThat(retried.getTxtMessage()).isNotEqualTo("changed_elsewhere");
		assertThat(repositoryEventMaster.findById(event.getSerEventMasterId()).orElseThrow()
				.getNumNumberOfGuests()).isEqualTo(300);
	}

	@Test
	@DisplayName("saving twice in a row works, the way every step of the journey does")
	void consecutiveSavesFromOneClientAllSucceed() {
		/*
		 * The same thing from the customer's side. One person, one booking, three
		 * steps in a row — no second editor anywhere. If the version handed back
		 * is ever stale this fails on the second save, which is what a customer
		 * would hit on the second screen.
		 */
		EventMaster event = seedEvent();
		DtoEventMaster held = asClientCopy(event, 120);
		savingAs(CUSTOMER);

		for (int step = 0; step < 3; step++) {
			held.setNumNumberOfGuests(120 + step * 10);
			DtoResult outcome = serviceEventMaster.saveAndUpdate(held);

			assertThat(outcome.getTxtMessage())
					.as("save number %d was refused as a conflict, with nobody else editing", step + 1)
					.isNotEqualTo("changed_elsewhere");

			/*
			 * And deliberately without refreshing the version. This is the whole
			 * point: the copy in the browser falls behind through its own
			 * progress, and that must not read as a second editor.
			 */
		}
	}

	@Test
	@DisplayName("a client that sends no version is still allowed to save")
	void anOlderClientIsNotLockedOut() {
		/*
		 * Deliberate. A caller that predates this field means an older build, not
		 * a conflict — and refusing those saves would break working screens to
		 * guard against a rarer fault than the one it caused. Both portals send
		 * the version; this is the fallback, not the intent.
		 */
		EventMaster event = seedEvent();

		DtoEventMaster noVersion = asClientCopy(event, 400);
		noVersion.setNumVersion(null);

		assertThat(serviceEventMaster.saveAndUpdate(noVersion).getTxtMessage())
				.isNotEqualTo("changed_elsewhere");
	}
}
