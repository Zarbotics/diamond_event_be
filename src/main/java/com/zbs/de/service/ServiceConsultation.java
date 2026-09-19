package com.zbs.de.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.zbs.de.model.ConsultationBooking;
import com.zbs.de.service.ConsultationSlotFinder.Slot;

/**
 * Booking a consultation, and everything the two front ends need to do it.
 */
public interface ServiceConsultation {

	/** A slot on offer, with the host who would take it. */
	record OfferedSlot(Integer serHostId, String txtHostName, Slot slot) {
	}

	/** What a booking attempt produced. */
	record BookingOutcome(boolean accepted, String message, ConsultationBooking booking) {

		public static BookingOutcome taken() {
			return new BookingOutcome(false,
					"That time has just been taken. Please choose another.", null);
		}

		public static BookingOutcome refused(String why) {
			return new BookingOutcome(false, why, null);
		}

		public static BookingOutcome confirmed(ConsultationBooking booking) {
			return new BookingOutcome(true, "Booked", booking);
		}
	}

	/**
	 * Slots on offer across every active host, between two dates.
	 *
	 * @param serHostId optional — a specific host, or null for whoever is free
	 */
	List<OfferedSlot> availableSlots(Integer serConsultationTypeId, Integer serHostId,
			LocalDate from, LocalDate to);

	/**
	 * Takes a slot.
	 *
	 * <p>
	 * Re-checks the moment it is asked, rather than trusting what was listed:
	 * between a customer seeing a slot and pressing it, the host may have been
	 * booked by somebody else or filled the time in their own calendar.
	 */
	BookingOutcome book(Integer serConsultationTypeId, Integer serHostId, Instant startsAt,
			String customerName, String customerEmail, String customerPhone,
			String customerTimeZone, String notes, Integer serCustId, Integer serEventMasterId);

	/**
	 * Agrees a pending request.
	 *
	 * <p>
	 * This is where a video link is created, if the type asks for one, and
	 * where the customer is told — a request they made is not news until
	 * somebody has said yes to it.
	 */
	BookingOutcome confirm(Integer serConsultationBookingId);

	/** Declines a pending request, releasing the slot. */
	BookingOutcome decline(Integer serConsultationBookingId, String reason);

	/** Requests still waiting on somebody, oldest first. */
	List<ConsultationBooking> awaitingConfirmation();

	/**
	 * Releases requests nobody answered in time.
	 *
	 * @return how many were released
	 */
	int releaseLapsedHolds();

	/** Cancels, releasing the slot. */
	BookingOutcome cancel(Integer serConsultationBookingId, String reason);

	/** Cancels using the single-use link from a confirmation email. */
	BookingOutcome cancelByToken(String managementToken, String reason);

	/**
	 * The booking a management link refers to, or null.
	 *
	 * <p>
	 * So the page that link opens can say which meeting it is about before
	 * asking the customer to do anything to it. Without it the page can only
	 * offer to cancel a consultation it cannot name, which is a poor thing to
	 * ask somebody to press.
	 */
	ConsultationBooking findByToken(String managementToken);

	/**
	 * Moves a booking to another time, using the link from its email.
	 *
	 * <h3>Why this is not cancel-then-book</h3>
	 *
	 * Because that leaves a gap. Between the two calls the customer has no
	 * consultation, and if the second half fails — the slot went while they
	 * were choosing, the connection dropped — they are left with nothing,
	 * having asked only to move it. The office's diary would also show a
	 * cancellation and a separate new booking where one meeting moved, which is
	 * a worse record of what happened.
	 *
	 * <p>
	 * So the row moves. Same booking, same event, same history; a new time, and
	 * a new management token, because the old one has travelled through an
	 * email and whoever holds it should not keep power over the new
	 * arrangement.
	 *
	 * @param serHostId optional — the host whose slot was offered, or null to
	 *                  let the same rules that pick one for a new booking pick.
	 */
	BookingOutcome rescheduleByToken(String managementToken, Instant newStartsAt, Integer serHostId);

	/** The live consultation for an event, if there is one. */
	ConsultationBooking liveBookingForEvent(Integer serEventMasterId);
}
