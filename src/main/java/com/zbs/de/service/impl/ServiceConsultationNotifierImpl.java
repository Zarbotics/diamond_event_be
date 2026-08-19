package com.zbs.de.service.impl;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.zbs.de.model.ConsultationBooking;
import com.zbs.de.model.ConsultationHost;
import com.zbs.de.model.ConsultationType;
import com.zbs.de.repository.RepositoryConsultationHost;
import com.zbs.de.repository.RepositoryConsultationType;
import com.zbs.de.service.ServiceConsultationNotifier;
import com.zbs.de.service.ServiceEmailSender;

/**
 * The consultation emails.
 *
 * <h2>Times are written in the customer's own zone</h2>
 *
 * Every booking stores the zone the customer was in when they made it, and it
 * exists for this. An email that says "your consultation is at 10:00" to
 * somebody in Dubai, meaning 10:00 in London, is a missed meeting and a
 * customer who believes they were stood up. So each time is rendered in their
 * zone and carries the zone's name, and the host's copy carries both — theirs,
 * because that is the clock they will act on, and the customer's, because
 * ringing somebody at their midnight is the other half of the same mistake.
 *
 * <h2>Nothing here throws</h2>
 *
 * Every send is wrapped. The booking is committed before any of this runs, and
 * an unreachable mail server must not turn a successful booking into a failed
 * request. In development there are no SMTP credentials at all — the properties
 * default to empty — so this failing is the <em>normal</em> case there, and it
 * has to be a log line rather than a broken journey.
 */
@Service
public class ServiceConsultationNotifierImpl implements ServiceConsultationNotifier {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceConsultationNotifierImpl.class);

	private static final DateTimeFormatter WHEN = DateTimeFormatter
			.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.SHORT)
			.withLocale(Locale.UK);

	/** Named so the reader can check it, rather than shown as "Europe/London". */
	private static final DateTimeFormatter ZONE_NAME = DateTimeFormatter.ofPattern("zzz", Locale.UK);

	@Autowired
	private ServiceEmailSender serviceEmailSender;

	@Autowired
	private RepositoryConsultationHost repositoryHost;

	@Autowired
	private RepositoryConsultationType repositoryType;

	@Value("${app.frontend.base-url:http://localhost:5173}")
	private String frontendBaseUrl;

	@Value("${app.consultation.contact-phone:0121 200 0000}")
	private String contactPhone;

	// -----------------------------------------------------------------

	@Override
	public void bookingConfirmed(ConsultationBooking booking) {
		String when = forCustomer(booking);
		String host = hostName(booking);

		send(booking.getTxtCustomerEmail(),
				"Your consultation is booked — " + when,
				greeting(booking)
						+ "Your " + typeName(booking) + " with " + host + " is booked for:\n\n"
						+ "    " + when + "\n\n"
						+ joiningDetails(booking)
						+ "If you need to change or cancel it, use this link:\n"
						+ "    " + cancelLink(booking) + "\n\n"
						+ "Or ring us on " + contactPhone + ".\n\n"
						+ signOff());

		notifyHost(booking, "New consultation booked",
				"A " + typeName(booking) + " has been booked with you.\n\n"
						+ bookingSummaryForHost(booking));
	}

	@Override
	public void bookingRequested(ConsultationBooking booking) {
		String when = forCustomer(booking);

		/*
		 * The wording matters more here than anywhere else. This is not a
		 * confirmation and must not read like one — the customer has to know
		 * that turning up at that time without hearing from us would be a
		 * wasted journey.
		 */
		send(booking.getTxtCustomerEmail(),
				"We have your request — " + when,
				greeting(booking)
						+ "Thank you for asking for a " + typeName(booking) + " with "
						+ hostName(booking) + ":\n\n"
						+ "    " + when + "\n\n"
						+ "We are holding that time while we check it. "
						+ "This is not a booking yet — we will email you as soon as it is confirmed, "
						+ "and if we cannot make it we will offer you another time.\n\n"
						+ holdNotice(booking)
						+ "If you have changed your mind in the meantime:\n"
						+ "    " + cancelLink(booking) + "\n\n"
						+ signOff());

		notifyHost(booking, "A consultation request is waiting for you",
				"Somebody has asked for a " + typeName(booking) + " with you. "
						+ "It is holding the slot until it is confirmed or declined.\n\n"
						+ bookingSummaryForHost(booking)
						+ "\nConfirm or decline it under Consultations → Requests in the admin portal.\n");
	}

	@Override
	public void requestApproved(ConsultationBooking booking) {
		String when = forCustomer(booking);

		send(booking.getTxtCustomerEmail(),
				"Your consultation is confirmed — " + when,
				greeting(booking)
						+ "Your " + typeName(booking) + " with " + hostName(booking)
						+ " is now confirmed for:\n\n"
						+ "    " + when + "\n\n"
						+ joiningDetails(booking)
						+ "If you need to change or cancel it:\n"
						+ "    " + cancelLink(booking) + "\n\n"
						+ "Or ring us on " + contactPhone + ".\n\n"
						+ signOff());
	}

	@Override
	public void requestDeclined(ConsultationBooking booking, String reason) {
		/*
		 * The reason is the whole email. "Your request was declined" on its own
		 * leaves the customer with nothing to do next, and a customer at the end
		 * of the booking journey with nothing to do next is a lost one.
		 */
		String because = reason == null || reason.isBlank()
				? "Unfortunately we are not free then."
				: reason.trim();

		send(booking.getTxtCustomerEmail(),
				"About your consultation request",
				greeting(booking)
						+ "We are sorry — we cannot make " + forCustomer(booking) + ".\n\n"
						+ because + "\n\n"
						+ "That time is back on the calendar, and you are very welcome to pick another:\n"
						+ "    " + frontendBaseUrl + "\n\n"
						+ "Or ring us on " + contactPhone + " and we will find one with you.\n\n"
						+ signOff());
	}

	@Override
	public void bookingCancelled(ConsultationBooking booking, String reason, boolean byCustomer) {
		String when = forCustomer(booking);

		if (byCustomer) {
			// A receipt, so they know it worked, and a note to the team.
			send(booking.getTxtCustomerEmail(),
					"Your consultation has been cancelled",
					greeting(booking)
							+ "Your " + typeName(booking) + " on " + when + " has been cancelled.\n\n"
							+ "If that was not what you meant, or you would like another time, "
							+ "ring us on " + contactPhone + ".\n\n"
							+ signOff());

			notifyHost(booking, "A consultation was cancelled",
					booking.getTxtCustomerName() + " has cancelled their "
							+ typeName(booking) + ".\n\n"
							+ bookingSummaryForHost(booking));
			return;
		}

		String because = reason == null || reason.isBlank() ? "" : reason.trim() + "\n\n";

		send(booking.getTxtCustomerEmail(),
				"Your consultation on " + when + " has been cancelled",
				greeting(booking)
						+ "We are sorry — we have had to cancel your " + typeName(booking)
						+ " on " + when + ".\n\n"
						+ because
						+ "Please do pick another time:\n"
						+ "    " + frontendBaseUrl + "\n\n"
						+ "Or ring us on " + contactPhone + " and we will sort it out with you.\n\n"
						+ signOff());
	}

	// -----------------------------------------------------------------
	// Wording
	// -----------------------------------------------------------------

	private String greeting(ConsultationBooking booking) {
		String name = booking.getTxtCustomerName();
		String first = name == null || name.isBlank() ? null : name.trim().split("\\s+")[0];
		return (first == null ? "Hello," : "Hello " + first + ",") + "\n\n";
	}

	private String signOff() {
		return "Diamond Events\n";
	}

	private String joiningDetails(ConsultationBooking booking) {
		if (booking.getTxtVideoJoinUrl() != null && !booking.getTxtVideoJoinUrl().isBlank()) {
			return "Join here at that time:\n    " + booking.getTxtVideoJoinUrl() + "\n\n";
		}
		return "";
	}

	private String holdNotice(ConsultationBooking booking) {
		if (booking.getDteHoldExpiresAt() == null) {
			return "";
		}
		/*
		 * Said plainly, because it is the one thing that can silently cost the
		 * customer the slot. A hold that lapses without warning reads as the
		 * system having lost the request.
		 */
		return "We will hold it until " + inZone(booking.getDteHoldExpiresAt(), customerZone(booking))
				+ ". After that the time goes back on the calendar.\n\n";
	}

	/** The host's copy: their clock first, and the customer's beside it. */
	private String bookingSummaryForHost(ConsultationBooking booking) {
		ZoneId hostZone = hostZone(booking);
		ZoneId customerZone = customerZone(booking);

		StringBuilder summary = new StringBuilder()
				.append("  When:  ").append(inZone(booking.getDteStartsAt(), hostZone)).append('\n');

		if (!hostZone.equals(customerZone)) {
			summary.append("         (")
					.append(inZone(booking.getDteStartsAt(), customerZone))
					.append(" where they are)\n");
		}

		summary.append("  Who:   ").append(booking.getTxtCustomerName()).append('\n')
				.append("  Email: ").append(booking.getTxtCustomerEmail()).append('\n');

		if (booking.getTxtCustomerPhone() != null && !booking.getTxtCustomerPhone().isBlank()) {
			summary.append("  Phone: ").append(booking.getTxtCustomerPhone()).append('\n');
		}
		if (booking.getTxtNotes() != null && !booking.getTxtNotes().isBlank()) {
			summary.append("  Notes: ").append(booking.getTxtNotes()).append('\n');
		}
		return summary.toString();
	}

	private String cancelLink(ConsultationBooking booking) {
		return frontendBaseUrl + "/consultation/manage?token=" + booking.getTxtManagementToken();
	}

	// -----------------------------------------------------------------
	// Times
	// -----------------------------------------------------------------

	private String forCustomer(ConsultationBooking booking) {
		return inZone(booking.getDteStartsAt(), customerZone(booking));
	}

	private String inZone(Instant instant, ZoneId zone) {
		var zoned = instant.atZone(zone);
		return WHEN.format(zoned) + " (" + ZONE_NAME.format(zoned) + ")";
	}

	/**
	 * The zone the customer was in when they booked, falling back to the host's.
	 *
	 * <p>
	 * Never silently UTC: a UTC time shown to somebody in London is an hour out
	 * for seven months of the year, which is exactly wrong enough to be missed.
	 */
	private ZoneId customerZone(ConsultationBooking booking) {
		try {
			return ZoneId.of(booking.getTxtCustomerTimeZone());
		} catch (Exception e) {
			return hostZone(booking);
		}
	}

	private ZoneId hostZone(ConsultationBooking booking) {
		return host(booking).map(ConsultationHost::zone).orElse(ZoneId.of("Europe/London"));
	}

	// -----------------------------------------------------------------
	// Lookups and sending
	// -----------------------------------------------------------------

	private Optional<ConsultationHost> host(ConsultationBooking booking) {
		return booking.getSerHostId() == null
				? Optional.empty()
				: repositoryHost.findById(booking.getSerHostId());
	}

	private String hostName(ConsultationBooking booking) {
		return host(booking).map(ConsultationHost::getTxtDisplayName).orElse("our team");
	}

	private String typeName(ConsultationBooking booking) {
		if (booking.getSerConsultationTypeId() == null) {
			return "consultation";
		}
		return repositoryType.findById(booking.getSerConsultationTypeId())
				.map(ConsultationType::getTxtName)
				.map(name -> name.toLowerCase(Locale.UK))
				.orElse("consultation");
	}

	private void notifyHost(ConsultationBooking booking, String subject, String body) {
		host(booking)
				.map(ConsultationHost::getTxtEmail)
				.filter(email -> email != null && !email.isBlank())
				.ifPresent(email -> send(email, subject, body + "\n" + signOff()));
	}

	/**
	 * Sends, and never lets a failure escape.
	 *
	 * <p>
	 * The booking is committed by the time this runs. Letting a
	 * {@code MessagingException} out would turn "booked, but we could not email
	 * you" into "your booking failed", which is the worse of the two outcomes by
	 * a long way — the customer has the slot either way, and can be told about
	 * it by a person.
	 *
	 * <p>
	 * The address is not logged. It is the customer's, and it has no business in
	 * a log file that gets shipped somewhere.
	 */
	private void send(String to, String subject, String body) {
		if (to == null || to.isBlank()) {
			LOGGER.warn("No address to send \"{}\" to — nothing sent", subject);
			return;
		}
		try {
			serviceEmailSender.sendEmail(to, subject, body);
		} catch (Exception e) {
			LOGGER.warn("Could not send the consultation email \"{}\": {}", subject, e.getMessage());
		}
	}
}
