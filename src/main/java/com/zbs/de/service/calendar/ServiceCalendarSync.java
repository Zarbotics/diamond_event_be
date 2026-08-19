package com.zbs.de.service.calendar;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.zbs.de.model.CalendarConnection;
import com.zbs.de.model.ConsultationBooking;
import com.zbs.de.model.ConsultationType;
import com.zbs.de.repository.RepositoryCalendarConnection;
import com.zbs.de.repository.RepositoryConsultationBooking;
import com.zbs.de.repository.RepositoryConsultationType;
import com.zbs.de.service.calendar.CalendarProvider.ExternalEvent;

/**
 * Putting a confirmed consultation into the host's own calendar.
 *
 * <p>
 * Sits between {@link com.zbs.de.service.ServiceConsultation} and whichever
 * {@link CalendarProvider} the host has connected, and its main job is to make
 * sure that <strong>nothing a calendar provider does can affect whether a
 * customer has a booking</strong>. The booking is already committed when this
 * runs; Google being down, a revoked token or an expired refresh token are all
 * ordinary traffic, and none of them is a reason to tell a customer their
 * meeting did not happen.
 *
 * <p>
 * So every failure here is caught, recorded on the booking as a sync state, and
 * left for a retry. The row says {@code SYNC_FAILED} rather than the request
 * saying "error", which is the difference between something a person can chase
 * and something a customer suffers.
 *
 * <h2>REQUIRES_NEW, and why it is not decoration</h2>
 *
 * Both public methods start their own transaction. They are called from
 * {@code afterCommit}, which is after the caller's transaction has committed —
 * and a {@code save()} there quietly does nothing. The EntityManager is still
 * bound to the thread, so there is no error; its transaction has simply already
 * finished, and the flush never reaches the database.
 *
 * <p>
 * The symptom is nastier than a failure would be: the provider is called, the
 * calendar entry really is created, the joining link really is returned — and
 * the column stays null, so the customer's email goes out without the link and
 * a retry later creates a second calendar entry because the row still looks
 * unwritten. A test caught it; by hand it would have looked like the provider
 * misbehaving.
 *
 * <h2>Why on confirmation rather than on booking</h2>
 *
 * A link for a meeting nobody has agreed to is a link to nothing, and sending
 * one before confirmation tells the customer they have a meeting when what they
 * have is a request. Instant bookings confirm the moment they are made, so both
 * modes land here at the right time without a special case.
 */
@Service
public class ServiceCalendarSync {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCalendarSync.class);

	@Autowired
	private RepositoryCalendarConnection repositoryConnection;

	@Autowired
	private RepositoryConsultationBooking repositoryBooking;

	@Autowired
	private RepositoryConsultationType repositoryType;

	/**
	 * Every provider Spring found.
	 *
	 * <p>
	 * A list rather than a pair of named beans, so that adding a provider is
	 * adding a class. Empty is a valid state and the ordinary one in
	 * development: nothing is connected, so nothing is written, and
	 * consultations work exactly as they do now.
	 */
	@Autowired(required = false)
	private List<CalendarProvider> providers = List.of();

	/**
	 * Writes a confirmed consultation to the host's nominated calendar, and
	 * stores the joining link if one was made.
	 *
	 * <p>
	 * Safe to call for a host who has connected nothing, and safe to call twice
	 * — a booking that already carries an external event is left alone rather
	 * than duplicated.
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void publish(ConsultationBooking booking) {
		if (booking == null || booking.getSerHostId() == null) {
			return;
		}
		if (booking.getTxtExternalEventId() != null) {
			// Already in a calendar. Writing again would put the same meeting
			// in twice, which is the one thing the single-writer rule exists
			// to prevent.
			return;
		}

		Optional<CalendarConnection> target = repositoryConnection.writeTargetFor(booking.getSerHostId());
		if (target.isEmpty()) {
			/*
			 * Not an error and not worth a warning. A host who has connected
			 * nothing still takes consultations; they simply do not appear in
			 * Google or Outlook. This is the state every installation is in
			 * before anybody connects anything.
			 */
			markSync(booking, ConsultationBooking.SYNC_NOT_APPLICABLE, null);
			return;
		}

		CalendarConnection connection = target.get();
		Optional<CalendarProvider> provider = providerFor(connection.getTxtProvider());
		if (provider.isEmpty()) {
			LOGGER.warn("No adapter for calendar provider {} — consultation {} not written",
					connection.getTxtProvider(), booking.getSerConsultationBookingId());
			markSync(booking, ConsultationBooking.SYNC_FAILED, "No adapter for " + connection.getTxtProvider());
			return;
		}

		try {
			ExternalEvent event = provider.get().createEvent(connection, booking, wantsVideoLink(booking));

			booking.setTxtExternalEventId(event.externalEventId());
			if (event.joinUrl() != null && !event.joinUrl().isBlank()) {
				booking.setTxtVideoJoinUrl(event.joinUrl());
			}
			markSync(booking, ConsultationBooking.SYNC_SYNCED, null);

			LOGGER.info("Consultation {} written to {} calendar",
					booking.getSerConsultationBookingId(), connection.getTxtProvider());

		} catch (Exception e) {
			/*
			 * Caught, always. The customer has their booking either way, and a
			 * failure here is something for a person to chase rather than
			 * something for a customer to suffer.
			 */
			LOGGER.warn("Could not write consultation {} to the {} calendar: {}",
					booking.getSerConsultationBookingId(), connection.getTxtProvider(), e.getMessage());
			markSync(booking, ConsultationBooking.SYNC_FAILED, e.getMessage());
		}
	}

	/** Removes a cancelled consultation from the calendar it was written to. */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void withdraw(ConsultationBooking booking) {
		if (booking == null || booking.getTxtExternalEventId() == null) {
			return;
		}

		repositoryConnection.writeTargetFor(booking.getSerHostId())
				.flatMap(connection -> providerFor(connection.getTxtProvider())
						.map(provider -> new Object[] { connection, provider }))
				.ifPresent(pair -> {
					CalendarConnection connection = (CalendarConnection) pair[0];
					CalendarProvider provider = (CalendarProvider) pair[1];
					try {
						provider.deleteEvent(connection, booking.getTxtExternalEventId());
						booking.setTxtExternalEventId(null);
						markSync(booking, ConsultationBooking.SYNC_SYNCED, null);
					} catch (Exception e) {
						// The meeting is cancelled in this system regardless. A
						// stale entry in somebody's calendar is untidy, not
						// harmful, and the retry will clear it.
						LOGGER.warn("Could not remove consultation {} from the calendar: {}",
								booking.getSerConsultationBookingId(), e.getMessage());
						markSync(booking, ConsultationBooking.SYNC_FAILED, e.getMessage());
					}
				});
	}

	/**
	 * Whether this kind of meeting should carry a Meet or Teams link.
	 *
	 * <p>
	 * A setting on the consultation type, not a constant: a video call wants one
	 * and a visit to a venue does not, and which is which is the business's
	 * decision rather than this code's.
	 */
	private boolean wantsVideoLink(ConsultationBooking booking) {
		if (booking.getSerConsultationTypeId() == null) {
			return false;
		}
		return repositoryType.findById(booking.getSerConsultationTypeId())
				.map(ConsultationType::getBlnCreateVideoLink)
				.map(Boolean.TRUE::equals)
				.orElse(false);
	}

	private Optional<CalendarProvider> providerFor(String name) {
		List<CalendarProvider> matching = providers.stream()
				.filter(p -> p.name().equals(name))
				.toList();

		if (matching.size() > 1) {
			/*
			 * Two adapters claiming the same provider means whichever the bean
			 * ordering happens to put first wins, and that ordering is not
			 * something to rely on. Refusing is better than writing somebody's
			 * consultation to an arbitrary one of two Googles.
			 */
			throw new IllegalStateException(
					"More than one calendar adapter claims to be " + name
							+ ". Exactly one bean may return that from name().");
		}
		return matching.stream().findFirst();
	}

	private void markSync(ConsultationBooking booking, String status, String error) {
		booking.setTxtExternalSyncStatus(status);
		booking.setTxtExternalSyncError(error);
		repositoryBooking.save(booking);
	}
}
