package com.zbs.de.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.CyclicBarrier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.zbs.de.model.ConsultationAvailabilityRule;
import com.zbs.de.model.ConsultationBooking;
import com.zbs.de.model.ConsultationHost;
import com.zbs.de.model.ConsultationType;
import com.zbs.de.repository.RepositoryConsultationAvailabilityRule;
import com.zbs.de.repository.RepositoryConsultationBooking;
import com.zbs.de.repository.RepositoryConsultationHost;
import com.zbs.de.repository.RepositoryConsultationType;
import com.zbs.de.service.ServiceConsultation.BookingOutcome;
import com.zbs.de.service.ServiceConsultation.OfferedSlot;

/**
 * Booking a consultation, against a real database.
 *
 * <p>
 * The case this exists for is the last one: two customers taking the same slot
 * at the same moment. That cannot be tested with a mock, because what prevents
 * it is a PostgreSQL exclusion constraint rather than anything in Java — and it
 * cannot be tested single-threaded, because the failure only exists in the
 * window between one request checking and the other writing.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class ConsultationBookingIT {

	private static final ZoneId LONDON = ZoneId.of("Europe/London");
	private static final String MARKER = "IT-CONSULT";

	@Autowired
	private ServiceConsultation serviceConsultation;

	@Autowired
	private RepositoryConsultationHost repositoryHost;

	@Autowired
	private RepositoryConsultationType repositoryType;

	@Autowired
	private RepositoryConsultationAvailabilityRule repositoryRule;

	@Autowired
	private RepositoryConsultationBooking repositoryBooking;

	private Integer hostId;
	private Integer typeId;
	/** A Monday comfortably in the future, so notice never interferes. */
	private LocalDate bookingDay;

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

	@BeforeEach
	void seed() {
		removeSeed();

		ConsultationHost host = new ConsultationHost();
		host.setTxtDisplayName(MARKER + " Host");
		host.setTxtEmail(MARKER.toLowerCase() + "-host@example.com");
		host.setTxtTimeZone("Europe/London");
		hostId = repositoryHost.saveAndFlush(host).getSerHostId();

		ConsultationType type = new ConsultationType();
		type.setTxtName(MARKER + " Consultation");
		type.setNumDurationMinutes(60);
		type.setNumMinimumNoticeHours(0);
		type.setNumMaximumAdvanceDays(365);
		typeId = repositoryType.saveAndFlush(type).getSerConsultationTypeId();

		bookingDay = LocalDate.now(LONDON).plusDays(14).with(DayOfWeek.MONDAY);

		ConsultationAvailabilityRule rule = new ConsultationAvailabilityRule();
		rule.setSerHostId(hostId);
		rule.setNumDayOfWeek(DayOfWeek.MONDAY.getValue());
		rule.setTmeStartTime(LocalTime.of(9, 0));
		rule.setTmeEndTime(LocalTime.of(12, 0));
		repositoryRule.saveAndFlush(rule);
	}

	@AfterEach
	void removeSeed() {
		repositoryBooking.findAll().stream()
				.filter(b -> b.getTxtCustomerEmail() != null
						&& b.getTxtCustomerEmail().contains(MARKER.toLowerCase()))
				.forEach(repositoryBooking::delete);
		repositoryRule.findAll().stream()
				.filter(r -> hostId != null && hostId.equals(r.getSerHostId()))
				.forEach(repositoryRule::delete);
		repositoryHost.findAll().stream()
				.filter(h -> h.getTxtDisplayName() != null && h.getTxtDisplayName().startsWith(MARKER))
				.forEach(repositoryHost::delete);
		repositoryType.findAll().stream()
				.filter(t -> t.getTxtName() != null && t.getTxtName().startsWith(MARKER))
				.forEach(repositoryType::delete);
	}

	private Instant slotAt(int hour) {
		return bookingDay.atTime(hour, 0).atZone(LONDON).toInstant();
	}

	private BookingOutcome book(Instant startsAt, String who) {
		return serviceConsultation.book(typeId, hostId, startsAt, who,
				MARKER.toLowerCase() + "-" + who + "@example.com", null, "Europe/London",
				null, null, null);
	}

	// -----------------------------------------------------------------

	@Test
	@DisplayName("slots are offered inside the host's hours")
	void slotsAreOffered() {
		List<OfferedSlot> slots = serviceConsultation.availableSlots(
				typeId, hostId, bookingDay, bookingDay.plusDays(1));

		assertThat(slots).isNotEmpty();
		assertThat(slots).allSatisfy(s -> assertThat(s.serHostId()).isEqualTo(hostId));
		assertThat(slots.get(0).slot().startsAt()).isEqualTo(slotAt(9));
	}

	@Test
	@DisplayName("booking a slot takes it out of the list")
	void bookingRemovesTheSlot() {
		assertThat(book(slotAt(10), "first").accepted()).isTrue();

		assertThat(serviceConsultation.availableSlots(typeId, hostId, bookingDay, bookingDay.plusDays(1)))
				.extracting(s -> s.slot().startsAt())
				.doesNotContain(slotAt(10));
	}

	@Test
	@DisplayName("the same slot cannot be booked twice")
	void theSameSlotIsRefusedTheSecondTime() {
		assertThat(book(slotAt(10), "first").accepted()).isTrue();

		BookingOutcome second = book(slotAt(10), "second");
		assertThat(second.accepted()).isFalse();
		assertThat(second.message()).contains("just been taken");
	}

	@Test
	@DisplayName("cancelling releases the slot")
	void cancellingReleasesTheSlot() {
		BookingOutcome first = book(slotAt(10), "first");
		assertThat(first.accepted()).isTrue();

		serviceConsultation.cancel(first.booking().getSerConsultationBookingId(), "Changed plans");

		assertThat(serviceConsultation.availableSlots(typeId, hostId, bookingDay, bookingDay.plusDays(1)))
				.extracting(s -> s.slot().startsAt())
				.contains(slotAt(10));
		assertThat(book(slotAt(10), "second").accepted())
				.as("the released slot can be taken by somebody else")
				.isTrue();
	}

	@Test
	@DisplayName("the management link works once and then does not")
	void theManagementTokenIsSingleUse() {
		BookingOutcome booked = book(slotAt(10), "first");
		String token = booked.booking().getTxtManagementToken();
		assertThat(token).isNotBlank();

		assertThat(serviceConsultation.cancelByToken(token, "Cannot make it").accepted()).isTrue();

		// Spent. It is the only thing standing between a stranger and somebody
		// else's meeting, so it stops working the moment it is used.
		BookingOutcome again = serviceConsultation.cancelByToken(token, "Again");
		assertThat(again.accepted()).isFalse();
		assertThat(again.message()).contains("could not be found");
	}

	@Test
	@DisplayName("a slot outside the host's hours is refused even if asked for directly")
	void slotsOutsideWorkingHoursAreRefused() {
		// 20:00 is well outside 09:00-12:00. A client that posts it anyway —
		// stale page, altered request — must not get a meeting out of it.
		BookingOutcome outcome = book(slotAt(20), "chancer");

		assertThat(outcome.accepted()).isFalse();
		assertThat(outcome.message()).contains("just been taken");
	}

	@Test
	@DisplayName("two customers booking the same slot at once: exactly one succeeds")
	void concurrentBookingsCannotBothWin() throws Exception {
		/*
		 * The case the whole design is built around. Both threads check, both
		 * find the slot free, and both try to write it — which is precisely the
		 * sequence that application-level checking cannot prevent, because the
		 * gap between reading and writing is where the other booking lands.
		 *
		 * The barrier makes them arrive together rather than hoping they do.
		 */
		Instant contested = slotAt(11);
		int attempts = 2;

		CyclicBarrier startTogether = new CyclicBarrier(attempts);
		ExecutorService pool = Executors.newFixedThreadPool(attempts);

		List<Callable<BookingOutcome>> racers = List.of(
				() -> {
					startTogether.await();
					return book(contested, "racer-a");
				},
				() -> {
					startTogether.await();
					return book(contested, "racer-b");
				});

		List<Future<BookingOutcome>> results = pool.invokeAll(racers);
		pool.shutdown();

		long accepted = 0;
		for (Future<BookingOutcome> result : results) {
			if (result.get().accepted()) {
				accepted++;
			}
		}

		assertThat(accepted)
				.as("exactly one of two simultaneous bookings must win")
				.isEqualTo(1);

		List<ConsultationBooking> live = repositoryBooking.liveBookingsOverlapping(
				hostId, contested, contested.plusSeconds(3600));
		assertThat(live)
				.as("the database must hold one booking for that slot, not two")
				.hasSize(1);
	}
}
