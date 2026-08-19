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

	/** The live consultation for an event, if there is one. */
	ConsultationBooking liveBookingForEvent(Integer serEventMasterId);
}
