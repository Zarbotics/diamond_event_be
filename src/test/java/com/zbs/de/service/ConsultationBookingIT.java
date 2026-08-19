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

	/** Switches the seeded type to "the team has to agree to this". */
	private void requireConfirmation(int windowHours) {
		ConsultationType type = repositoryType.findById(typeId).orElseThrow();
		type.setBlnRequiresConfirmation(true);
		type.setNumConfirmationWindowHours(windowHours);
		repositoryType.saveAndFlush(type);
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

	// -----------------------------------------------------------------
	// Requested, then confirmed
	// -----------------------------------------------------------------

	@Test
	@DisplayName("with confirmation off, a booking is agreed straight away")
	void instantByDefault() {
		BookingOutcome outcome = book(slotAt(10), "first");

		assertThat(outcome.accepted()).isTrue();
		assertThat(outcome.booking().getTxtStatus()).isEqualTo(ConsultationBooking.STATUS_BOOKED);
		assertThat(outcome.booking().getDteConfirmedAt()).isNotNull();
		assertThat(outcome.booking().getDteHoldExpiresAt()).isNull();
	}

	@Test
	@DisplayName("with confirmation on, a booking starts as a request")
	void confirmationModeCreatesARequest() {
		requireConfirmation(48);

		BookingOutcome outcome = book(slotAt(10), "first");

		assertThat(outcome.accepted()).isTrue();
		assertThat(outcome.booking().getTxtStatus()).isEqualTo(ConsultationBooking.STATUS_PENDING);
		assertThat(outcome.booking().getDteConfirmedAt()).isNull();
		assertThat(outcome.booking().getDteHoldExpiresAt())
				.as("a request has to hold its slot, and the hold has to end")
				.isNotNull();
	}

	@Test
	@DisplayName("a pending request holds the slot against everyone else")
	void aPendingRequestHoldsTheSlot() {
		/*
		 * The reason holds exist. Without this the team could agree to two
		 * meetings in one slot, having each been told it was free.
		 */
		requireConfirmation(48);
		assertThat(book(slotAt(10), "first").accepted()).isTrue();

		assertThat(serviceConsultation.availableSlots(typeId, hostId, bookingDay, bookingDay.plusDays(1)))
				.extracting(s -> s.slot().startsAt())
				.doesNotContain(slotAt(10));

		BookingOutcome second = book(slotAt(10), "second");
		assertThat(second.accepted()).isFalse();
	}

	@Test
	@DisplayName("confirming a request turns it into a meeting")
	void confirmingARequest() {
		requireConfirmation(48);
		BookingOutcome requested = book(slotAt(10), "first");

		BookingOutcome confirmed = serviceConsultation
				.confirm(requested.booking().getSerConsultationBookingId());

		assertThat(confirmed.accepted()).isTrue();
		assertThat(confirmed.booking().getTxtStatus()).isEqualTo(ConsultationBooking.STATUS_BOOKED);
		assertThat(confirmed.booking().getDteConfirmedAt()).isNotNull();
		assertThat(confirmed.booking().getDteHoldExpiresAt())
				.as("once agreed it is not a hold any more")
				.isNull();
	}

	@Test
	@DisplayName("declining a request puts the slot back on sale")
	void decliningReleasesTheSlot() {
		requireConfirmation(48);
		BookingOutcome requested = book(slotAt(10), "first");

		assertThat(serviceConsultation
				.decline(requested.booking().getSerConsultationBookingId(), "Fully booked that week")
				.accepted()).isTrue();

		assertThat(serviceConsultation.availableSlots(typeId, hostId, bookingDay, bookingDay.plusDays(1)))
				.extracting(s -> s.slot().startsAt())
				.contains(slotAt(10));
		assertThat(book(slotAt(10), "second").accepted()).isTrue();
	}

	@Test
	@DisplayName("a request nobody answers stops holding its slot")
	void lapsedHoldsAreReleased() {
		// A window already in the past, so the hold is born expired — which is
		// the same state as one nobody answered for two days.
		requireConfirmation(48);
		BookingOutcome requested = book(slotAt(10), "first");

		ConsultationBooking booking = repositoryBooking
				.findById(requested.booking().getSerConsultationBookingId()).orElseThrow();
		booking.setDteHoldExpiresAt(Instant.now().minusSeconds(60));
		repositoryBooking.saveAndFlush(booking);

		assertThat(serviceConsultation.releaseLapsedHolds()).isEqualTo(1);

		assertThat(serviceConsultation.availableSlots(typeId, hostId, bookingDay, bookingDay.plusDays(1)))
				.extracting(s -> s.slot().startsAt())
				.as("the slot goes back on sale")
				.contains(slotAt(10));
	}

	@Test
	@DisplayName("confirming a request after its hold ran out is refused")
	void confirmingTooLateIsRefused() {
		/*
		 * Answering late has to fail rather than succeed quietly: the slot went
		 * back on sale when the hold lapsed, so somebody else may hold it now.
		 */
		requireConfirmation(48);
		BookingOutcome requested = book(slotAt(10), "first");

		ConsultationBooking booking = repositoryBooking
				.findById(requested.booking().getSerConsultationBookingId()).orElseThrow();
		booking.setDteHoldExpiresAt(Instant.now().minusSeconds(60));
		repositoryBooking.saveAndFlush(booking);

		BookingOutcome tooLate = serviceConsultation
				.confirm(requested.booking().getSerConsultationBookingId());

		assertThat(tooLate.accepted()).isFalse();
		assertThat(tooLate.message()).contains("expired");
	}

	@Test
	@DisplayName("requests waiting on somebody are listed oldest first")
	void pendingRequestsAreListed() {
		requireConfirmation(48);
		book(slotAt(9), "first");
		book(slotAt(10), "second");

		assertThat(serviceConsultation.awaitingConfirmation())
				.extracting(ConsultationBooking::getDteStartsAt)
				.contains(slotAt(9), slotAt(10));
	}
}
