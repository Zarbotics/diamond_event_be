package com.zbs.de.service.calendar;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.zbs.de.model.CalendarConnection;
import com.zbs.de.model.ConsultationAvailabilityRule;
import com.zbs.de.model.ConsultationBooking;
import com.zbs.de.model.ConsultationHost;
import com.zbs.de.model.ConsultationType;
import com.zbs.de.repository.RepositoryCalendarConnection;
import com.zbs.de.repository.RepositoryConsultationAvailabilityRule;
import com.zbs.de.repository.RepositoryConsultationBooking;
import com.zbs.de.repository.RepositoryConsultationHost;
import com.zbs.de.repository.RepositoryConsultationType;
import com.zbs.de.service.ServiceConsultation;
import com.zbs.de.service.ServiceConsultation.BookingOutcome;

/**
 * Writing a confirmed consultation into a host's own calendar.
 *
 * <p>
 * Google and Microsoft cannot be reached from here — the adapters need real
 * credentials — so what is tested is everything up to the network boundary,
 * against a provider that stands in for one. That is the part with the
 * decisions in it: which calendar gets written to, when the write happens,
 * whether a joining link is asked for, and above all what happens when the
 * provider fails.
 *
 * <p>
 * The last of those is the reason this exists. Google being down, a revoked
 * token or an expired refresh token are ordinary traffic, and none of them may
 * be allowed to cost a customer their booking.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class CalendarSyncIT {

	private static final ZoneId LONDON = ZoneId.of("Europe/London");
	private static final String MARKER = "IT-CALSYNC";

	/**
	 * Stands in for Google.
	 *
	 * <p>
	 * Registered as a bean so it is discovered exactly as a real adapter would
	 * be — through the injected {@code List<CalendarProvider>} — rather than
	 * being handed to the service by the test. That way the discovery is under
	 * test too.
	 */
	static class FakeGoogle implements CalendarProvider {

		final List<ConsultationBooking> created = new ArrayList<>();
		final List<String> deleted = new ArrayList<>();
		boolean broken = false;
		boolean askedForVideoLink = false;

		@Override
		public String name() {
			return CalendarConnection.GOOGLE;
		}

		@Override
		public List<BusyPeriod> busyPeriods(CalendarConnection connection, Instant from, Instant to) {
			return List.of();
		}

		@Override
		public ExternalEvent createEvent(CalendarConnection connection, ConsultationBooking booking,
				boolean withVideoLink) {
			if (broken) {
				throw new IllegalStateException("Google is not answering");
			}
			askedForVideoLink = withVideoLink;
			created.add(booking);
			return new ExternalEvent("google-event-" + booking.getSerConsultationBookingId(),
					withVideoLink ? "https://meet.example/abc-defg-hij" : null);
		}

		@Override
		public void deleteEvent(CalendarConnection connection, String externalEventId) {
			if (broken) {
				throw new IllegalStateException("Google is not answering");
			}
			deleted.add(externalEventId);
		}

		void reset() {
			created.clear();
			deleted.clear();
			broken = false;
			askedForVideoLink = false;
		}
	}

	@TestConfiguration
	static class Providers {
		@Bean
		FakeGoogle fakeGoogle() {
			return new FakeGoogle();
		}
	}

	/** No SMTP in a test environment, and the emails are covered elsewhere. */
	@MockitoBean
	private JavaMailSender mailSender;

	@Autowired
	private FakeGoogle google;

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

	@Autowired
	private RepositoryCalendarConnection repositoryConnection;

	private Integer hostId;
	private Integer typeId;
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
		google.reset();

		ConsultationHost host = new ConsultationHost();
		host.setTxtDisplayName(MARKER + " Host");
		host.setTxtEmail("it-calsync-host@example.com");
		host.setTxtTimeZone("Europe/London");
		hostId = repositoryHost.saveAndFlush(host).getSerHostId();

		ConsultationType type = new ConsultationType();
		type.setTxtName(MARKER + " Consultation");
		type.setNumDurationMinutes(60);
		type.setNumMinimumNoticeHours(0);
		type.setNumMaximumAdvanceDays(365);
		type.setBlnCreateVideoLink(true);
		typeId = repositoryType.saveAndFlush(type).getSerConsultationTypeId();

		bookingDay = LocalDate.now(LONDON).plusDays(28).with(DayOfWeek.TUESDAY);

		ConsultationAvailabilityRule rule = new ConsultationAvailabilityRule();
		rule.setSerHostId(hostId);
		rule.setNumDayOfWeek(DayOfWeek.TUESDAY.getValue());
		rule.setTmeStartTime(LocalTime.of(9, 0));
		rule.setTmeEndTime(LocalTime.of(12, 0));
		repositoryRule.saveAndFlush(rule);
	}

	@AfterEach
	void removeSeed() {
		if (hostId != null) {
			repositoryBooking.findAll().stream()
					.filter(b -> hostId.equals(b.getSerHostId()))
					.forEach(repositoryBooking::delete);
			repositoryConnection.findBySerHostIdAndBlnIsDeletedFalse(hostId)
					.forEach(repositoryConnection::delete);
			repositoryRule.findAll().stream()
					.filter(r -> hostId.equals(r.getSerHostId()))
					.forEach(repositoryRule::delete);
		}
		repositoryHost.findAll().stream()
				.filter(h -> h.getTxtDisplayName() != null && h.getTxtDisplayName().startsWith(MARKER))
				.forEach(repositoryHost::delete);
		repositoryType.findAll().stream()
				.filter(t -> t.getTxtName() != null && t.getTxtName().startsWith(MARKER))
				.forEach(repositoryType::delete);
	}

	/** Connects a calendar for the host, optionally as the one written to. */
	private CalendarConnection connect(String provider, boolean writeTarget) {
		CalendarConnection connection = new CalendarConnection();
		connection.setSerHostId(hostId);
		connection.setTxtProvider(provider);
		connection.setTxtAccountEmail(provider.toLowerCase() + "-" + hostId + "@example.com");
		connection.setTxtCalendarId("primary");
		connection.setBlnIsWriteTarget(writeTarget);
		return repositoryConnection.saveAndFlush(connection);
	}

	private BookingOutcome book(int hour) {
		return serviceConsultation.book(typeId, hostId,
				bookingDay.atTime(hour, 0).atZone(LONDON).toInstant(),
				"Sara Ahmed", "it-calsync-customer@example.com", null, "Europe/London",
				null, null, null);
	}

	private ConsultationBooking reload(BookingOutcome outcome) {
		return repositoryBooking.findById(outcome.booking().getSerConsultationBookingId()).orElseThrow();
	}

	// -----------------------------------------------------------------

	@Test
	@DisplayName("a booking is written to the host's nominated calendar, with a joining link")
	void aBookingReachesTheCalendar() {
		connect(CalendarConnection.GOOGLE, true);

		BookingOutcome outcome = book(10);
		assertThat(outcome.accepted()).isTrue();

		assertThat(google.created).hasSize(1);
		assertThat(google.askedForVideoLink)
				.as("the type asks for a video link, so the provider should have been told to make one")
				.isTrue();

		ConsultationBooking stored = reload(outcome);
		assertThat(stored.getTxtExternalEventId()).isNotNull();
		assertThat(stored.getTxtVideoJoinUrl()).contains("meet.example");
		assertThat(stored.getTxtExternalSyncStatus()).isEqualTo(ConsultationBooking.SYNC_SYNCED);
	}

	@Test
	@DisplayName("the video link is a setting, not a habit")
	void theVideoLinkIsConfigurable() {
		// A venue visit is somewhere you drive to. A link to a video call for it
		// is at best noise and at worst somebody sitting waiting in one.
		ConsultationType type = repositoryType.findById(typeId).orElseThrow();
		type.setBlnCreateVideoLink(false);
		repositoryType.saveAndFlush(type);
		connect(CalendarConnection.GOOGLE, true);

		BookingOutcome outcome = book(10);

		assertThat(google.askedForVideoLink).isFalse();
		assertThat(reload(outcome).getTxtVideoJoinUrl()).isNull();
	}

	@Test
	@DisplayName("a host with nothing connected still takes bookings")
	void nothingConnectedIsNotAFailure() {
		/*
		 * The state every installation is in before anybody connects anything,
		 * and the state a small team may stay in for ever. Consultations work;
		 * they simply do not appear in anybody's Google or Outlook.
		 */
		BookingOutcome outcome = book(10);

		assertThat(outcome.accepted()).isTrue();
		assertThat(google.created).isEmpty();
		assertThat(reload(outcome).getTxtExternalSyncStatus())
				.isEqualTo(ConsultationBooking.SYNC_NOT_APPLICABLE);
	}

	@Test
	@DisplayName("a calendar that is only connected for reading is not written to")
	void onlyTheWriteTargetIsWrittenTo() {
		/*
		 * Busy times are read from everything somebody has connected; the
		 * consultation goes in exactly one. Writing to both would put the same
		 * meeting in twice, and every later change would have to find and match
		 * the copies.
		 */
		connect(CalendarConnection.GOOGLE, false);

		BookingOutcome outcome = book(10);

		assertThat(google.created).isEmpty();
		assertThat(reload(outcome).getTxtExternalSyncStatus())
				.isEqualTo(ConsultationBooking.SYNC_NOT_APPLICABLE);
	}

	@Test
	@DisplayName("a provider that is down does not cost the customer their booking")
	void aBrokenProviderDoesNotBreakTheBooking() {
		/*
		 * The one that matters. Google being unreachable, a revoked token and an
		 * expired refresh token all land here, and none of them is a reason to
		 * tell somebody their meeting did not happen. The failure is recorded on
		 * the row for a person to chase.
		 */
		connect(CalendarConnection.GOOGLE, true);
		google.broken = true;

		BookingOutcome outcome = book(10);

		assertThat(outcome.accepted())
				.as("an unreachable calendar provider refused a booking")
				.isTrue();

		ConsultationBooking stored = reload(outcome);
		assertThat(stored.getTxtExternalSyncStatus()).isEqualTo(ConsultationBooking.SYNC_FAILED);
		assertThat(stored.getTxtExternalSyncError()).contains("not answering");
	}

	@Test
	@DisplayName("a request is not put in the diary until it is confirmed")
	void aPendingRequestIsNotWritten() {
		/*
		 * A request is not a meeting. Putting it in the host's calendar before
		 * they have agreed would have them turning down other work for something
		 * they may well decline.
		 */
		ConsultationType type = repositoryType.findById(typeId).orElseThrow();
		type.setBlnRequiresConfirmation(true);
		repositoryType.saveAndFlush(type);
		connect(CalendarConnection.GOOGLE, true);

		BookingOutcome requested = book(10);
		assertThat(requested.booking().getTxtStatus()).isEqualTo(ConsultationBooking.STATUS_PENDING);
		assertThat(google.created).as("a request was written to the calendar").isEmpty();

		serviceConsultation.confirm(requested.booking().getSerConsultationBookingId());

		assertThat(google.created).hasSize(1);
		assertThat(reload(requested).getTxtVideoJoinUrl()).contains("meet.example");
	}

	@Test
	@DisplayName("cancelling takes it back out of the calendar")
	void cancellingWithdrawsTheEvent() {
		connect(CalendarConnection.GOOGLE, true);
		BookingOutcome outcome = book(10);
		String externalId = reload(outcome).getTxtExternalEventId();

		serviceConsultation.cancel(outcome.booking().getSerConsultationBookingId(), "Changed plans");

		assertThat(google.deleted).containsExactly(externalId);
	}

	@Test
	@DisplayName("publishing twice does not put the meeting in twice")
	void publishingIsNotRepeated() {
		// Retries are expected — a failed write is retried later — so the write
		// has to be safe to attempt again on a booking that already succeeded.
		connect(CalendarConnection.GOOGLE, true);
		BookingOutcome outcome = book(10);
		assertThat(google.created).hasSize(1);

		// Confirming something already booked is a no-op, but it exercises the
		// same publish path a retry would.
		serviceConsultation.confirm(outcome.booking().getSerConsultationBookingId());

		assertThat(google.created).hasSize(1);
	}
}
