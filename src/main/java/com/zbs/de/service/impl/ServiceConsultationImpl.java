package com.zbs.de.service.impl;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zbs.de.model.CalendarBusyBlock;
import com.zbs.de.model.ConsultationBooking;
import com.zbs.de.model.ConsultationHost;
import com.zbs.de.model.ConsultationType;
import com.zbs.de.repository.RepositoryCalendarBusyBlock;
import com.zbs.de.repository.RepositoryConsultationAvailabilityException;
import com.zbs.de.repository.RepositoryConsultationAvailabilityRule;
import com.zbs.de.repository.RepositoryConsultationBooking;
import com.zbs.de.repository.RepositoryConsultationHost;
import com.zbs.de.repository.RepositoryConsultationType;
import com.zbs.de.service.ConsultationSlotFinder;
import com.zbs.de.service.ConsultationSlotFinder.Busy;
import com.zbs.de.service.ConsultationSlotFinder.Slot;
import com.zbs.de.service.ServiceConsultation;

/**
 * Booking consultations.
 *
 * <p>
 * The scheduling arithmetic lives in {@link ConsultationSlotFinder}, which has
 * no Spring and no database in it. This class is the part that talks to both:
 * it gathers what the finder needs, and it writes the result down safely.
 */
@Service
public class ServiceConsultationImpl implements ServiceConsultation {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceConsultationImpl.class);

	/** How far apart offered start times are placed. */
	private static final int SLOT_STEP_MINUTES = 30;

	/** How far ahead a request may look, whatever it asks for. */
	private static final int MAX_WINDOW_DAYS = 120;

	private static final SecureRandom RANDOM = new SecureRandom();

	@Autowired
	private RepositoryConsultationHost repositoryHost;

	@Autowired
	private RepositoryConsultationType repositoryType;

	@Autowired
	private RepositoryConsultationAvailabilityRule repositoryRule;

	@Autowired
	private RepositoryConsultationAvailabilityException repositoryException;

	@Autowired
	private RepositoryConsultationBooking repositoryBooking;

	@Autowired
	private RepositoryCalendarBusyBlock repositoryBusyBlock;

	// -----------------------------------------------------------------
	// Listing
	// -----------------------------------------------------------------

	@Override
	@Transactional(readOnly = true)
	public List<OfferedSlot> availableSlots(Integer serConsultationTypeId, Integer serHostId,
			LocalDate from, LocalDate to) {

		ConsultationType type = repositoryType
				.findBySerConsultationTypeIdAndBlnIsDeletedFalse(serConsultationTypeId)
				.orElse(null);
		if (type == null || !Boolean.TRUE.equals(type.getBlnIsActive())) {
			return List.of();
		}

		LocalDate windowFrom = from != null ? from : LocalDate.now();
		LocalDate windowTo = to != null ? to : windowFrom.plusDays(30);

		// A request for five years of slots is a request to generate a very large
		// list nobody will read. Cap it here rather than trusting the caller.
		if (windowTo.isAfter(windowFrom.plusDays(MAX_WINDOW_DAYS))) {
			windowTo = windowFrom.plusDays(MAX_WINDOW_DAYS);
		}
		if (!windowTo.isAfter(windowFrom)) {
			return List.of();
		}

		List<ConsultationHost> hosts = serHostId != null
				? repositoryHost.findBySerHostIdAndBlnIsDeletedFalse(serHostId)
						.filter(h -> Boolean.TRUE.equals(h.getBlnIsActive()))
						.map(List::of).orElse(List.of())
				: repositoryHost.findByBlnIsActiveTrueAndBlnIsDeletedFalseOrderBySerHostIdAsc();

		Instant now = Instant.now();
		List<OfferedSlot> offered = new ArrayList<>();

		for (ConsultationHost host : hosts) {
			ZoneId zone = host.zone();
			Instant windowStart = windowFrom.atStartOfDay(zone).toInstant();
			Instant windowEnd = windowTo.atStartOfDay(zone).toInstant();

			for (Slot slot : ConsultationSlotFinder.findSlots(
					type, zone,
					repositoryRule.findBySerHostIdAndBlnIsDeletedFalse(host.getSerHostId()),
					repositoryException.findBySerHostIdAndDteOnDateBetweenAndBlnIsDeletedFalse(
							host.getSerHostId(), windowFrom, windowTo),
					busyFor(host.getSerHostId(), windowStart, windowEnd),
					windowStart, windowEnd, now, SLOT_STEP_MINUTES)) {

				offered.add(new OfferedSlot(host.getSerHostId(), host.getTxtDisplayName(), slot));
			}
		}

		offered.sort(Comparator.comparing((OfferedSlot o) -> o.slot().startsAt())
				.thenComparing(OfferedSlot::serHostId));
		return offered;
	}

	/**
	 * Everything that makes a host unavailable: consultations already booked
	 * here, and anything imported from their own calendar.
	 */
	private List<Busy> busyFor(Integer serHostId, Instant windowStart, Instant windowEnd) {
		List<Busy> busy = new ArrayList<>();

		for (ConsultationBooking booking : repositoryBooking
				.liveBookingsOverlapping(serHostId, windowStart, windowEnd)) {
			busy.add(new Busy(booking.getDteStartsAt(), booking.getDteEndsAt()));
		}

		for (CalendarBusyBlock block : repositoryBusyBlock
				.overlapping(serHostId, windowStart, windowEnd)) {
			busy.add(new Busy(block.getDteStartsAt(), block.getDteEndsAt()));
		}

		return busy;
	}

	// -----------------------------------------------------------------
	// Booking
	// -----------------------------------------------------------------

	/*
	 * Deliberately NOT @Transactional, and this is the subtle part.
	 *
	 * The insert below can violate the exclusion constraint — that is the whole
	 * point of it. Inside a transaction, a constraint violation marks that
	 * transaction rollback-only, and catching the exception does not undo that:
	 * the method then returns normally and the *commit* fails instead, with
	 * "Transaction silently rolled back because it has been marked as
	 * rollback-only". The caller gets an exception from a method that handled
	 * its error perfectly well. I wrote it with @Transactional first and the
	 * concurrency test found exactly that.
	 *
	 * It is the mirror image of the fault fixed across the service layer
	 * earlier, and it comes from the same wrong assumption — that catching an
	 * exception inside a transaction puts things back. It does not. A
	 * persistence failure poisons the transaction whether or not anybody
	 * catches it; there it meant the partial work committed, here it means
	 * nothing can.
	 *
	 * So the insert runs in its own transaction — Spring Data's save is
	 * transactional by itself — and the handler sits outside it, where there is
	 * no poisoned transaction to commit. Nothing here needs the wider
	 * atomicity: the booking is a single row, and the constraint rather than
	 * the transaction is what makes it safe. The host's round-robin timestamp
	 * is the only other write, and a stale one costs nothing.
	 */
	@Override
	public BookingOutcome book(Integer serConsultationTypeId, Integer serHostId, Instant startsAt,
			String customerName, String customerEmail, String customerPhone,
			String customerTimeZone, String notes, Integer serCustId, Integer serEventMasterId) {

		if (startsAt == null || customerName == null || customerName.isBlank()
				|| customerEmail == null || customerEmail.isBlank()) {
			return BookingOutcome.refused("A name, an email address and a time are all needed.");
		}

		ConsultationType type = repositoryType
				.findBySerConsultationTypeIdAndBlnIsDeletedFalse(serConsultationTypeId)
				.orElse(null);
		if (type == null || !Boolean.TRUE.equals(type.getBlnIsActive())) {
			return BookingOutcome.refused("That kind of consultation is not available.");
		}

		Integer hostId = serHostId != null ? serHostId : chooseHost(type, startsAt);
		if (hostId == null) {
			return BookingOutcome.refused("Nobody is available at that time.");
		}

		ConsultationHost host = repositoryHost.findBySerHostIdAndBlnIsDeletedFalse(hostId).orElse(null);
		if (host == null || !Boolean.TRUE.equals(host.getBlnIsActive())) {
			return BookingOutcome.refused("That person is not taking consultations.");
		}

		Instant endsAt = startsAt.plus(Duration.ofMinutes(type.getNumDurationMinutes()));

		/*
		 * Re-checked here, not trusted from the listing. Between a customer being
		 * shown a slot and pressing it, somebody else may have taken it or the
		 * host may have filled the time in their own calendar. The listing is a
		 * suggestion; this is the decision.
		 */
		if (!isStillOffered(type, host, startsAt)) {
			return BookingOutcome.taken();
		}

		ConsultationBooking booking = new ConsultationBooking();
		booking.setSerHostId(hostId);
		booking.setSerConsultationTypeId(serConsultationTypeId);
		booking.setSerCustId(serCustId);
		booking.setSerEventMasterId(serEventMasterId);
		booking.setTxtCustomerName(customerName.trim());
		booking.setTxtCustomerEmail(customerEmail.trim());
		booking.setTxtCustomerPhone(customerPhone);
		booking.setTxtCustomerTimeZone(customerTimeZone);
		booking.setTxtNotes(notes);
		booking.setDteStartsAt(startsAt);
		booking.setDteEndsAt(endsAt);
		booking.setTxtStatus(ConsultationBooking.STATUS_BOOKED);
		booking.setTxtManagementToken(newManagementToken());
		booking.setTxtExternalSyncStatus(ConsultationBooking.SYNC_PENDING);

		try {
			repositoryBooking.saveAndFlush(booking);
		} catch (DataIntegrityViolationException e) {
			/*
			 * The exclusion constraint in V6 caught a booking that the check above
			 * could not, because it was made in the moment between the two. That
			 * is the constraint doing exactly its job, and it is the only reason
			 * double booking is actually impossible rather than merely unlikely.
			 *
			 * The customer gets the same civil answer either way.
			 */
			LOGGER.info("Slot at {} for host {} was taken concurrently", startsAt, hostId);
			return BookingOutcome.taken();
		}

		host.setDteLastAssigned(Instant.now());
		repositoryHost.save(host);

		LOGGER.info("Consultation {} booked with host {} at {}",
				booking.getSerConsultationBookingId(), hostId, startsAt);
		return BookingOutcome.confirmed(booking);
	}

	/** Whether the finder still offers this exact start for this host. */
	private boolean isStillOffered(ConsultationType type, ConsultationHost host, Instant startsAt) {
		ZoneId zone = host.zone();
		LocalDate day = startsAt.atZone(zone).toLocalDate();

		Instant windowStart = day.atStartOfDay(zone).toInstant();
		Instant windowEnd = day.plusDays(1).atStartOfDay(zone).toInstant();

		return ConsultationSlotFinder.findSlots(
				type, zone,
				repositoryRule.findBySerHostIdAndBlnIsDeletedFalse(host.getSerHostId()),
				repositoryException.findBySerHostIdAndDteOnDateBetweenAndBlnIsDeletedFalse(
						host.getSerHostId(), day, day),
				busyFor(host.getSerHostId(), windowStart, windowEnd),
				windowStart, windowEnd, Instant.now(), SLOT_STEP_MINUTES)
				.stream()
				.anyMatch(slot -> slot.startsAt().equals(startsAt));
	}

	/**
	 * Round-robin: whoever has waited longest gets the next one.
	 *
	 * <p>
	 * Only considers hosts who are actually free then, so a fair rotation never
	 * hands a booking to somebody who cannot take it.
	 */
	private Integer chooseHost(ConsultationType type, Instant startsAt) {
		return repositoryHost.findByBlnIsActiveTrueAndBlnIsDeletedFalseOrderBySerHostIdAsc().stream()
				.filter(host -> isStillOffered(type, host, startsAt))
				.min(Comparator.comparing(
						(ConsultationHost h) -> h.getDteLastAssigned() == null
								? Instant.EPOCH
								: h.getDteLastAssigned()))
				.map(ConsultationHost::getSerHostId)
				.orElse(null);
	}

	// -----------------------------------------------------------------
	// Cancelling
	// -----------------------------------------------------------------

	@Override
	@Transactional
	public BookingOutcome cancel(Integer serConsultationBookingId, String reason) {
		return cancelBooking(repositoryBooking.findById(serConsultationBookingId), reason);
	}

	@Override
	@Transactional
	public BookingOutcome cancelByToken(String managementToken, String reason) {
		if (managementToken == null || managementToken.isBlank()) {
			return BookingOutcome.refused("That link is not valid.");
		}
		return cancelBooking(repositoryBooking.findByTxtManagementToken(managementToken), reason);
	}

	private BookingOutcome cancelBooking(Optional<ConsultationBooking> found, String reason) {
		ConsultationBooking booking = found.orElse(null);
		if (booking == null) {
			return BookingOutcome.refused("That consultation could not be found.");
		}
		if (!booking.isLive()) {
			// Not an error: somebody following a stale link should be told the
			// meeting is already cancelled, not shown a failure.
			return new BookingOutcome(true, "That consultation was already cancelled.", booking);
		}

		booking.setTxtStatus(ConsultationBooking.STATUS_CANCELLED);
		booking.setTxtCancellationReason(reason);
		booking.setDteCancelledAt(Instant.now());
		booking.setUpdatedDate(Instant.now());
		/*
		 * Spent on cancellation. The link is the only thing standing between a
		 * stranger and somebody else's meeting, so it stops working once used.
		 */
		booking.setTxtManagementToken(null);
		repositoryBooking.save(booking);

		LOGGER.info("Consultation {} cancelled", booking.getSerConsultationBookingId());
		return new BookingOutcome(true, "That consultation has been cancelled.", booking);
	}

	@Override
	@Transactional(readOnly = true)
	public ConsultationBooking liveBookingForEvent(Integer serEventMasterId) {
		if (serEventMasterId == null) {
			return null;
		}
		return repositoryBooking.liveBookingsForEvent(serEventMasterId).stream()
				.findFirst().orElse(null);
	}

	/** 256 bits, URL-safe. Guessing one has to be harder than finding a meeting. */
	private String newManagementToken() {
		byte[] bytes = new byte[32];
		RANDOM.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}
}
