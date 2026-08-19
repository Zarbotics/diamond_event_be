package com.zbs.de.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zbs.de.model.ConsultationBooking;
import com.zbs.de.model.ConsultationType;
import com.zbs.de.repository.RepositoryConsultationType;
import com.zbs.de.service.ServiceConsultation;
import com.zbs.de.service.ServiceConsultation.BookingOutcome;
import com.zbs.de.service.ServiceConsultation.OfferedSlot;
import com.zbs.de.util.ResponseMessage;

/**
 * Booking a consultation, from the customer's side.
 *
 * <p>
 * Replaces the Calendly widget at the end of the booking journey. The admin's
 * own screens live in {@link ControllerConsultationAdmin}; these are the three
 * things a customer does — see what is free, take one, and change their mind.
 */
@RestController
@RequestMapping("/consultation")
public class ControllerConsultation {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerConsultation.class);

	@Autowired
	private ServiceConsultation serviceConsultation;

	@Autowired
	private RepositoryConsultationType repositoryConsultationType;

	/** What kinds of consultation are on offer, and how long they take. */
	@PostMapping(value = "/types", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage types() {
		List<Map<String, Object>> types = repositoryConsultationType
				.findByBlnIsActiveTrueAndBlnIsDeletedFalseOrderBySerConsultationTypeIdAsc()
				.stream()
				.map(this::describe)
				.toList();

		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Consultation types", types);
	}

	private Map<String, Object> describe(ConsultationType type) {
		java.util.Map<String, Object> described = new java.util.LinkedHashMap<>();
		described.put("serConsultationTypeId", type.getSerConsultationTypeId());
		described.put("txtName", type.getTxtName());
		described.put("txtDescription", type.getTxtDescription());
		described.put("numDurationMinutes", type.getNumDurationMinutes());
		described.put("txtLocationKind", type.getTxtLocationKind());
		/*
		 * The customer is told whether this needs agreeing before they choose a
		 * time, not after they have committed to one. "Request a time" and "Book
		 * a time" are different promises and the button should say which.
		 */
		described.put("blnRequiresConfirmation", type.getBlnRequiresConfirmation());
		return described;
	}

	/**
	 * Slots on offer between two dates.
	 *
	 * <p>
	 * Instants go out as ISO-8601 in UTC. The browser renders them in whatever
	 * zone the customer is actually in — which is the only party that knows.
	 */
	@PostMapping(value = "/slots", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage slots(@RequestBody Map<String, Object> request) {
		Integer typeId = asInteger(request.get("serConsultationTypeId"));
		Integer hostId = asInteger(request.get("serHostId"));
		LocalDate from = asDate(request.get("dteFrom"));
		LocalDate to = asDate(request.get("dteTo"));

		if (typeId == null) {
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"A consultation type is needed", null);
		}

		List<Map<String, Object>> slots = serviceConsultation
				.availableSlots(typeId, hostId, from, to).stream()
				.map(this::describe)
				.toList();

		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Available slots", slots);
	}

	private Map<String, Object> describe(OfferedSlot offered) {
		java.util.Map<String, Object> described = new java.util.LinkedHashMap<>();
		described.put("serHostId", offered.serHostId());
		described.put("txtHostName", offered.txtHostName());
		described.put("dteStartsAt", offered.slot().startsAt().toString());
		described.put("dteEndsAt", offered.slot().endsAt().toString());
		return described;
	}

	/** Takes a slot. */
	@PostMapping(value = "/book", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage book(@RequestBody Map<String, Object> request) {
		Integer typeId = asInteger(request.get("serConsultationTypeId"));
		Integer hostId = asInteger(request.get("serHostId"));
		Instant startsAt = asInstant(request.get("dteStartsAt"));

		if (typeId == null || startsAt == null) {
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"A consultation type and a start time are needed", null);
		}

		BookingOutcome outcome = serviceConsultation.book(
				typeId, hostId, startsAt,
				asString(request.get("txtCustomerName")),
				asString(request.get("txtCustomerEmail")),
				asString(request.get("txtCustomerPhone")),
				asString(request.get("txtCustomerTimeZone")),
				asString(request.get("txtNotes")),
				asInteger(request.get("serCustId")),
				asInteger(request.get("serEventMasterId")));

		if (!outcome.accepted()) {
			/*
			 * Conflict rather than bad request. The request was well formed and
			 * the slot was free when it was shown; somebody else simply got
			 * there first, so the frontend refreshes the list rather than
			 * telling the customer they did something wrong.
			 *
			 * The code travels in the body, as everywhere else in this API —
			 * the HTTP status stays 200 and the frontends read `code`. Worth
			 * knowing rather than assuming when writing a client.
			 */
			return new ResponseMessage(HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT,
					outcome.message(), null);
		}

		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK,
				outcome.message(), describe(outcome.booking()));
	}

	/** The customer's live consultation for an event, if they have one. */
	@PostMapping(value = "/forEvent", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage forEvent(@RequestBody Map<String, Object> request) {
		ConsultationBooking booking = serviceConsultation
				.liveBookingForEvent(asInteger(request.get("serEventMasterId")));

		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK,
				booking == null ? "No consultation booked" : "Consultation found",
				booking == null ? null : describe(booking));
	}

	/** Cancels using the single-use link from the confirmation email. */
	@PostMapping(value = "/cancel", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage cancel(@RequestBody Map<String, Object> request) {
		BookingOutcome outcome = serviceConsultation.cancelByToken(
				asString(request.get("txtManagementToken")),
				asString(request.get("txtReason")));

		LOGGER.info("Consultation cancellation by link: {}", outcome.accepted());

		return new ResponseMessage(
				outcome.accepted() ? HttpStatus.OK.value() : HttpStatus.NOT_FOUND.value(),
				outcome.accepted() ? HttpStatus.OK : HttpStatus.NOT_FOUND,
				outcome.message(),
				outcome.booking() == null ? null : describe(outcome.booking()));
	}

	/**
	 * What the customer is told about their own booking.
	 *
	 * <p>
	 * Deliberately not the entity. That carries the host's id, the sync state
	 * and the management token, none of which belong in a response the customer
	 * can read.
	 */
	private Map<String, Object> describe(ConsultationBooking booking) {
		java.util.Map<String, Object> described = new java.util.LinkedHashMap<>();
		described.put("serConsultationBookingId", booking.getSerConsultationBookingId());
		described.put("txtStatus", booking.getTxtStatus());
		described.put("dteStartsAt", booking.getDteStartsAt().toString());
		described.put("dteEndsAt", booking.getDteEndsAt().toString());
		described.put("txtCustomerTimeZone", booking.getTxtCustomerTimeZone());
		described.put("txtVideoJoinUrl", booking.getTxtVideoJoinUrl());
		// The token goes to the customer once, so their confirmation can carry a
		// cancel link. It is not readable back from any listing.
		described.put("txtManagementToken", booking.getTxtManagementToken());
		return described;
	}

	// -----------------------------------------------------------------

	private Integer asInteger(Object value) {
		if (value instanceof Number number) {
			return number.intValue();
		}
		if (value instanceof String text && !text.isBlank()) {
			try {
				return Integer.valueOf(text.trim());
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return null;
	}

	private String asString(Object value) {
		return value == null ? null : String.valueOf(value);
	}

	private LocalDate asDate(Object value) {
		try {
			return value == null ? null : LocalDate.parse(String.valueOf(value));
		} catch (DateTimeParseException e) {
			return null;
		}
	}

	private Instant asInstant(Object value) {
		try {
			return value == null ? null : Instant.parse(String.valueOf(value));
		} catch (DateTimeParseException e) {
			return null;
		}
	}
}
