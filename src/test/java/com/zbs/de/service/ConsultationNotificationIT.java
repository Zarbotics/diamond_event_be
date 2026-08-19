package com.zbs.de.service;

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
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.zbs.de.model.ConsultationAvailabilityRule;
import com.zbs.de.model.ConsultationBooking;
import com.zbs.de.model.ConsultationHost;
import com.zbs.de.model.ConsultationType;
import com.zbs.de.repository.RepositoryConsultationAvailabilityRule;
import com.zbs.de.repository.RepositoryConsultationBooking;
import com.zbs.de.repository.RepositoryConsultationHost;
import com.zbs.de.repository.RepositoryConsultationType;
import com.zbs.de.service.ServiceConsultation.BookingOutcome;

import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeMessage;

/**
 * What gets emailed about a consultation, and what happens when it cannot be.
 *
 * <p>
 * Two separate things are being checked here and the second is the important
 * one. The first is that the right message goes to the right person saying the
 * right thing. The second is that <strong>the mail server cannot cost a
 * customer their booking</strong> — it is somebody else's machine, reached over
 * the network, with credentials that are simply absent in development, so it
 * failing is not an edge case but the ordinary state of affairs on a developer
 * laptop.
 *
 * <p>
 * {@link JavaMailSender} is mocked and every message it is handed is recorded,
 * so the assertions are about the message that would actually have been sent.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class ConsultationNotificationIT {

	private static final ZoneId LONDON = ZoneId.of("Europe/London");
	private static final String MARKER = "IT-NOTIFY";

	/** One email that actually reached the mail server. */
	record Sent(String to, String subject, String body) {
	}

	/*
	 * The mail server itself is mocked, not the service in front of it.
	 *
	 * Two reasons. ServiceEmailSenderImpl is a single bean implementing both
	 * ServiceEmailSender and ServiceEmailVerification, and AuthController needs
	 * the second — replacing it with a mock of the first stops the whole context
	 * from starting. And mocking at this boundary leaves the real MIME building
	 * under test, so what is recorded here is what would have gone down the
	 * wire rather than what was handed to a stub.
	 */
	@MockitoBean
	private JavaMailSender mailSender;

	private final List<Sent> sent = new ArrayList<>();

	private List<Sent> to(String address) {
		return sent.stream().filter(s -> address.equals(s.to())).toList();
	}

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
	private LocalDate bookingDay;

	private static final String HOST_EMAIL = "it-notify-host@example.com";
	private static final String CUSTOMER_EMAIL = "it-notify-customer@example.com";

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
		host.setTxtDisplayName(MARKER + " Amina");
		host.setTxtEmail(HOST_EMAIL);
		host.setTxtTimeZone("Europe/London");
		hostId = repositoryHost.saveAndFlush(host).getSerHostId();

		ConsultationType type = new ConsultationType();
		type.setTxtName(MARKER + " Initial consultation");
		type.setNumDurationMinutes(60);
		type.setNumMinimumNoticeHours(0);
		type.setNumMaximumAdvanceDays(365);
		typeId = repositoryType.saveAndFlush(type).getSerConsultationTypeId();

		bookingDay = LocalDate.now(LONDON).plusDays(21).with(DayOfWeek.MONDAY);

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
				.filter(b -> hostId != null && hostId.equals(b.getSerHostId()))
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

	private void requireConfirmation() {
		ConsultationType type = repositoryType.findById(typeId).orElseThrow();
		type.setBlnRequiresConfirmation(true);
		type.setNumConfirmationWindowHours(48);
		repositoryType.saveAndFlush(type);
	}

	private BookingOutcome book(String customerZone) {
		return serviceConsultation.book(typeId, hostId,
				bookingDay.atTime(10, 0).atZone(LONDON).toInstant(),
				"Sara Ahmed", CUSTOMER_EMAIL, "07700 900000", customerZone, null, null, null);
	}

	// -----------------------------------------------------------------

	@Test
	@DisplayName("booking outright tells the customer it is booked, and tells the host")
	void bookingEmailsBothSides() {
		recordMail();

		BookingOutcome outcome = book("Europe/London");
		assertThat(outcome.accepted()).isTrue();

		assertThat(to(CUSTOMER_EMAIL)).hasSize(1);
		assertThat(to(CUSTOMER_EMAIL).get(0).subject()).contains("booked");
		assertThat(to(HOST_EMAIL))
				.as("the host was not told a meeting had been put in their diary")
				.hasSize(1);
	}

	@Test
	@DisplayName("the customer's confirmation carries the link that cancels it")
	void theConfirmationCarriesACancelLink() {
		recordMail();

		BookingOutcome outcome = book("Europe/London");

		/*
		 * The single-use token goes to the customer exactly once, in this email.
		 * Without it in the body they have no way to cancel except ringing up,
		 * which is the thing the whole management link exists to avoid.
		 */
		assertThat(to(CUSTOMER_EMAIL).get(0).body())
				.contains(outcome.booking().getTxtManagementToken());
	}

	@Test
	@DisplayName("a request does not read like a booking")
	void aRequestSaysItIsNotABookingYet() {
		requireConfirmation();
		recordMail();

		book("Europe/London");

		/*
		 * The distinction the customer acts on. Somebody who reads "booked" and
		 * turns up to a meeting nobody has agreed to has had a wasted journey,
		 * and it is our wording that sent them.
		 */
		String body = to(CUSTOMER_EMAIL).get(0).body();
		assertThat(body).contains("not a booking yet");
		assertThat(to(CUSTOMER_EMAIL).get(0).subject()).doesNotContain("booked");
		assertThat(body)
				.as("the customer was not told how long the time is held for")
				.containsIgnoringCase("hold it until");
	}

	@Test
	@DisplayName("confirming a request emails the customer that it is now confirmed")
	void confirmingEmailsTheCustomer() {
		requireConfirmation();
		recordMail();

		BookingOutcome requested = book("Europe/London");
		sent.clear();

		serviceConsultation.confirm(requested.booking().getSerConsultationBookingId());

		assertThat(to(CUSTOMER_EMAIL)).hasSize(1);
		assertThat(to(CUSTOMER_EMAIL).get(0).subject()).contains("confirmed");
	}

	@Test
	@DisplayName("declining a request sends the reason, because that is the useful part")
	void decliningSendsTheReason() {
		requireConfirmation();
		recordMail();

		BookingOutcome requested = book("Europe/London");
		sent.clear();

		serviceConsultation.decline(requested.booking().getSerConsultationBookingId(),
				"We are at a wedding that afternoon — any morning that week would suit.");

		assertThat(to(CUSTOMER_EMAIL).get(0).body())
				.contains("any morning that week would suit");
	}

	@Test
	@DisplayName("times are written in the customer's zone, not ours")
	void timesAreInTheCustomersZone() {
		recordMail();

		// 10:00 in London is 13:00 in Dubai, all year — the UAE does not change
		// its clocks, so this holds whichever side of March the test runs.
		book("Asia/Dubai");

		String body = to(CUSTOMER_EMAIL).get(0).body();
		assertThat(body)
				.as("the customer in Dubai was told a London time, which is a missed meeting")
				.contains("13:00");
		assertThat(body).doesNotContain("10:00");
	}

	@Test
	@DisplayName("the zone label is an offset anybody can check, not a made-up abbreviation")
	void theZoneLabelIsAnOffset() {
		/*
		 * The JDK's short zone names are not dependable: on this JVM Asia/Dubai
		 * formats as "GTS", which is not an abbreviation anybody uses — the real
		 * one is GST — while America/New_York formats as "GMT-04:00", so the
		 * shape is not even consistent between zones.
		 *
		 * That matters more than it looks. The zone is printed precisely so the
		 * reader can catch a mistake, and a label they do not recognise cannot
		 * do that job. An offset needs no locale data to be right.
		 */
		recordMail();

		book("Asia/Dubai");

		String body = to(CUSTOMER_EMAIL).get(0).body();
		assertThat(body).contains("GMT+4");
		assertThat(body)
				.as("the JDK's unrecognisable short name reached the customer")
				.doesNotContain("GTS");
	}

	@Test
	@DisplayName("a half-hour offset keeps its minutes")
	void halfHourOffsetsSurvive() {
		// India is GMT+5:30. Trimming ":00" off an offset is right; trimming the
		// minutes off this one would put every Indian customer half an hour out.
		recordMail();

		book("Asia/Kolkata");

		assertThat(to(CUSTOMER_EMAIL).get(0).body()).contains("GMT+5:30");
	}

	@Test
	@DisplayName("the host's copy carries both clocks when they differ")
	void theHostSeesBothClocks() {
		recordMail();

		book("Asia/Dubai");

		// Theirs, because that is the clock they will act on; and the customer's,
		// because ringing somebody at their midnight is the other half of the
		// same mistake.
		String body = to(HOST_EMAIL).get(0).body();
		assertThat(body).contains("10:00");
		assertThat(body).contains("13:00");
		assertThat(body).contains("where they are");
	}

	@Test
	@DisplayName("a mail server that is down does not cost the customer their booking")
	void aBrokenMailerDoesNotBreakTheBooking() {
		/*
		 * The one that matters most. In development there are no SMTP
		 * credentials at all, so this is not a rare failure — it is what happens
		 * every single time. If it propagated, the whole end of the customer
		 * journey would be broken on every developer machine, and in production
		 * a slow mail server would start refusing bookings.
		 */
		breakMail();

		BookingOutcome outcome = book("Europe/London");

		assertThat(outcome.accepted())
				.as("a failing mail server refused a booking")
				.isTrue();
		assertThat(repositoryBooking.findById(outcome.booking().getSerConsultationBookingId()))
				.as("the booking was not written")
				.isPresent();
	}

	@Test
	@DisplayName("cancelling from the customer's link tells both of them")
	void cancellingByLinkTellsBothSides() {
		recordMail();

		BookingOutcome outcome = book("Europe/London");
		String token = outcome.booking().getTxtManagementToken();
		sent.clear();

		serviceConsultation.cancelByToken(token, "Something has come up");

		assertThat(to(CUSTOMER_EMAIL)).as("no receipt for the customer").hasSize(1);
		assertThat(to(HOST_EMAIL))
				.as("the host was left with a meeting in their diary that is not happening")
				.hasSize(1);
	}

	@Test
	@DisplayName("a cancellation by the team apologises and offers another time")
	void aTeamCancellationOffersAnotherTime() {
		recordMail();

		BookingOutcome outcome = book("Europe/London");
		sent.clear();

		serviceConsultation.cancel(outcome.booking().getSerConsultationBookingId(),
				"Our venue visit overran.");

		String body = to(CUSTOMER_EMAIL).get(0).body();
		assertThat(body).contains("Our venue visit overran.");
		assertThat(body)
				.as("a cancellation with no way back is where a customer is lost")
				.containsIgnoringCase("pick another time");
	}

	// -----------------------------------------------------------------

	/**
	 * Records every message handed to the mail server.
	 *
	 * <p>
	 * Called at the start of each test rather than in a {@code @BeforeEach},
	 * because {@code @MockitoBean} resets between tests and one test needs the
	 * opposite behaviour.
	 */
	private void recordMail() {
		sent.clear();
		org.mockito.Mockito.when(mailSender.createMimeMessage())
				.thenAnswer(invocation -> new MimeMessage((jakarta.mail.Session) null));
		org.mockito.Mockito.doAnswer(invocation -> {
			sent.add(describe(invocation.getArgument(0)));
			return null;
		}).when(mailSender).send(org.mockito.ArgumentMatchers.any(MimeMessage.class));
	}

	/** And the opposite: a mail server that is not answering. */
	private void breakMail() {
		sent.clear();
		org.mockito.Mockito.when(mailSender.createMimeMessage())
				.thenAnswer(invocation -> new MimeMessage((jakarta.mail.Session) null));
		org.mockito.Mockito.doThrow(new MailSendException("The mail server is not answering"))
				.when(mailSender).send(org.mockito.ArgumentMatchers.any(MimeMessage.class));
	}

	private Sent describe(MimeMessage message) throws Exception {
		return new Sent(
				message.getAllRecipients()[0].toString(),
				message.getSubject(),
				textOf(message));
	}

	/**
	 * The text of a message.
	 *
	 * <p>
	 * ServiceEmailSenderImpl builds every message as multipart, so the body is
	 * not the content of the message itself but of a part inside it. Walking for
	 * it rather than casting keeps this working if a message ever gains an
	 * attachment.
	 */
	private String textOf(Part part) throws Exception {
		Object content = part.getContent();
		if (content instanceof String text) {
			return text;
		}
		if (content instanceof Multipart multipart) {
			StringBuilder all = new StringBuilder();
			for (int i = 0; i < multipart.getCount(); i++) {
				all.append(textOf(multipart.getBodyPart(i)));
			}
			return all.toString();
		}
		return "";
	}
}
