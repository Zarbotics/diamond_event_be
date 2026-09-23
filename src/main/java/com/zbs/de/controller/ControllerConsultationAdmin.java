package com.zbs.de.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zbs.de.model.CalendarConnection;
import com.zbs.de.model.ConsultationAvailabilityException;
import com.zbs.de.model.ConsultationAvailabilityRule;
import com.zbs.de.model.ConsultationBooking;
import com.zbs.de.model.ConsultationHost;
import com.zbs.de.model.ConsultationType;
import com.zbs.de.repository.RepositoryConsultationAvailabilityException;
import com.zbs.de.repository.RepositoryConsultationAvailabilityRule;
import com.zbs.de.repository.RepositoryConsultationBooking;
import com.zbs.de.repository.RepositoryConsultationHost;
import com.zbs.de.repository.RepositoryConsultationType;
import com.zbs.de.service.ServiceConsultation;
import com.zbs.de.service.ServiceConsultation.BookingOutcome;
import com.zbs.de.service.calendar.ServiceCalendarConnection;
import com.zbs.de.util.ResponseMessage;

/**
 * Managing consultations, from the team's side.
 *
 * <p>
 * Everything about how consultations work is configured here rather than in
 * code: who takes them, when they work, how long a meeting is, how much notice
 * is needed, whether a request has to be agreed, and whether confirming it
 * makes a video link. Seed data exists so a fresh install has something to
 * book, but none of it is a default the portal cannot change.
 *
 * <p>
 * Everything under {@code /admin/**} is administrator-only by default — the
 * security policy denies anything not explicitly opened, so this needs no
 * per-endpoint rule.
 */
@RestController
@RequestMapping("/admin/consultation")
public class ControllerConsultationAdmin {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerConsultationAdmin.class);

	@Autowired
	private ServiceConsultation serviceConsultation;

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
	private ServiceCalendarConnection serviceCalendarConnection;

	// -----------------------------------------------------------------
	// Hosts
	// -----------------------------------------------------------------

	@PostMapping(value = "/hosts", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage hosts() {
		List<Map<String, Object>> hosts = repositoryHost.findAll().stream()
				.filter(h -> !Boolean.TRUE.equals(h.getBlnIsDeleted()))
				.map(this::describe)
				.toList();
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Hosts", hosts);
	}

	@PostMapping(value = "/hosts/save", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage saveHost(@RequestBody Map<String, Object> request) {
		/*
		 * Checked before anything is set. With open-in-view on, a findById result
		 * is still associated with the request's session; validating after
		 * mutating it risks a rejected request having quietly changed the row
		 * anyway. Read, judge, then write.
		 */
		String name = asString(request.get("txtDisplayName"));
		String email = asString(request.get("txtEmail"));
		if (name == null || name.isBlank() || email == null || email.isBlank()) {
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"A name and an email address are needed", null);
		}
		String zone = asString(request.get("txtTimeZone"));
		if (zone != null && !isKnownZone(zone)) {
			// A typo here silently moves somebody's whole working week, because
			// the entity falls back to UK time rather than failing.
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"'" + zone + "' is not a time zone we recognise", null);
		}

		Integer id = asInteger(request.get("serHostId"));
		ConsultationHost host = id != null
				? repositoryHost.findById(id).orElse(new ConsultationHost())
				: new ConsultationHost();

		host.setTxtDisplayName(name);
		host.setTxtEmail(email);
		if (zone != null) {
			host.setTxtTimeZone(zone);
		}
		setIfPresent(request, "blnIsActive", v -> host.setBlnIsActive(asBoolean(v)));
		host.setUpdatedDate(Instant.now());

		repositoryHost.save(host);
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Saved", describe(host));
	}

	@PostMapping(value = "/hosts/delete", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage deleteHost(@RequestBody Map<String, Object> request) {
		Integer id = asInteger(request.get("serHostId"));
		ConsultationHost host = id == null ? null : repositoryHost.findById(id).orElse(null);
		if (host == null) {
			return new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
					"No such host", null);
		}

		/*
		 * Refused while they still have meetings in the diary. Removing somebody
		 * with consultations booked would leave customers holding an appointment
		 * with nobody, and they would find out on the day. Cancel or reassign
		 * them first — deliberately a decision somebody has to make, not one
		 * this endpoint makes for them.
		 */
		List<ConsultationBooking> upcoming = repositoryBooking.liveBookingsOverlapping(
				id, Instant.now(), Instant.now().plus(java.time.Duration.ofDays(3650)));
		if (!upcoming.isEmpty()) {
			return new ResponseMessage(HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT,
					"That person still has " + upcoming.size()
							+ " consultation(s) booked. Cancel or move those first.",
					null);
		}

		host.setBlnIsDeleted(true);
		host.setBlnIsActive(false);
		host.setUpdatedDate(Instant.now());
		repositoryHost.save(host);
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Removed", null);
	}

	private Map<String, Object> describe(ConsultationHost host) {
		Map<String, Object> described = new LinkedHashMap<>();
		described.put("serHostId", host.getSerHostId());
		described.put("txtDisplayName", host.getTxtDisplayName());
		described.put("txtEmail", host.getTxtEmail());
		described.put("txtTimeZone", host.getTxtTimeZone());
		described.put("blnIsActive", host.getBlnIsActive());
		return described;
	}

	// -----------------------------------------------------------------
	// Consultation types
	// -----------------------------------------------------------------

	@PostMapping(value = "/types", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage types() {
		List<Map<String, Object>> types = repositoryType.findAll().stream()
				.filter(t -> !Boolean.TRUE.equals(t.getBlnIsDeleted()))
				.map(this::describe)
				.toList();
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Consultation types", types);
	}

	@PostMapping(value = "/types/save", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage saveType(@RequestBody Map<String, Object> request) {
		String name = asString(request.get("txtName"));
		if (name == null || name.isBlank()) {
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"A name and a duration of at least five minutes are needed", null);
		}

		Integer id = asInteger(request.get("serConsultationTypeId"));
		ConsultationType type = id != null
				? repositoryType.findById(id).orElse(new ConsultationType())
				: new ConsultationType();

		/*
		 * A duration has to end up set: on a new type there is nothing to fall
		 * back to, and five minutes is the floor because anything shorter is a
		 * mistake rather than a meeting.
		 */
		Integer duration = asInteger(request.get("numDurationMinutes"));
		if (duration == null) {
			duration = type.getNumDurationMinutes();
		}
		if (duration == null || duration < 5) {
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"A name and a duration of at least five minutes are needed", null);
		}

		// Zero would be an infinite loop in the slot finder rather than an error.
		Integer interval = asInteger(request.get("numSlotIntervalMinutes"));
		if (interval != null && interval < 5) {
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"Start times have to be at least five minutes apart", null);
		}

		type.setTxtName(name);
		type.setTxtDescription(asString(request.get("txtDescription")));
		type.setNumDurationMinutes(duration);
		setIfPresent(request, "numSlotIntervalMinutes", v -> type.setNumSlotIntervalMinutes(asInteger(v)));
		setIfPresent(request, "numBufferBeforeMinutes", v -> type.setNumBufferBeforeMinutes(asInteger(v)));
		setIfPresent(request, "numBufferAfterMinutes", v -> type.setNumBufferAfterMinutes(asInteger(v)));
		setIfPresent(request, "numMinimumNoticeHours", v -> type.setNumMinimumNoticeHours(asInteger(v)));
		setIfPresent(request, "numMaximumAdvanceDays", v -> type.setNumMaximumAdvanceDays(asInteger(v)));
		setIfPresent(request, "numConfirmationWindowHours",
				v -> type.setNumConfirmationWindowHours(asInteger(v)));
		setIfPresent(request, "txtLocationKind", v -> type.setTxtLocationKind(asString(v)));
		setIfPresent(request, "blnRequiresConfirmation", v -> type.setBlnRequiresConfirmation(asBoolean(v)));
		setIfPresent(request, "blnCreateVideoLink", v -> type.setBlnCreateVideoLink(asBoolean(v)));
		setIfPresent(request, "blnIsActive", v -> type.setBlnIsActive(asBoolean(v)));
		type.setUpdatedDate(Instant.now());

		repositoryType.save(type);
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Saved", describe(type));
	}

	private Map<String, Object> describe(ConsultationType type) {
		Map<String, Object> described = new LinkedHashMap<>();
		described.put("serConsultationTypeId", type.getSerConsultationTypeId());
		described.put("txtName", type.getTxtName());
		described.put("txtDescription", type.getTxtDescription());
		described.put("numDurationMinutes", type.getNumDurationMinutes());
		described.put("numSlotIntervalMinutes", type.getNumSlotIntervalMinutes());
		described.put("numBufferBeforeMinutes", type.getNumBufferBeforeMinutes());
		described.put("numBufferAfterMinutes", type.getNumBufferAfterMinutes());
		described.put("numMinimumNoticeHours", type.getNumMinimumNoticeHours());
		described.put("numMaximumAdvanceDays", type.getNumMaximumAdvanceDays());
		described.put("numConfirmationWindowHours", type.getNumConfirmationWindowHours());
		described.put("txtLocationKind", type.getTxtLocationKind());
		described.put("blnRequiresConfirmation", type.getBlnRequiresConfirmation());
		described.put("blnCreateVideoLink", type.getBlnCreateVideoLink());
		described.put("blnIsActive", type.getBlnIsActive());
		return described;
	}

	// -----------------------------------------------------------------
	// Availability
	// -----------------------------------------------------------------

	@PostMapping(value = "/availability", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage availability(@RequestBody Map<String, Object> request) {
		Integer hostId = asInteger(request.get("serHostId"));
		if (hostId == null) {
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"A host is needed", null);
		}

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("rules", repositoryRule.findBySerHostIdAndBlnIsDeletedFalse(hostId).stream()
				.map(this::describe).toList());
		result.put("exceptions", repositoryException
				.findBySerHostIdAndDteOnDateBetweenAndBlnIsDeletedFalse(
						hostId, LocalDate.now().minusMonths(1), LocalDate.now().plusYears(1))
				.stream().map(this::describe).toList());

		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Availability", result);
	}

	@PostMapping(value = "/availability/saveRule", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage saveRule(@RequestBody Map<String, Object> request) {
		Integer hostId = asInteger(request.get("serHostId"));
		Integer dayOfWeek = asInteger(request.get("numDayOfWeek"));
		LocalTime start = asTime(request.get("tmeStartTime"));
		LocalTime end = asTime(request.get("tmeEndTime"));

		if (hostId == null || dayOfWeek == null || start == null || end == null) {
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"A host, a day and both times are needed", null);
		}
		if (dayOfWeek < 1 || dayOfWeek > 7) {
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"A day of the week runs 1 (Monday) to 7 (Sunday)", null);
		}
		if (!end.isAfter(start)) {
			// Checked here as well as by the database, so the message is about
			// working hours rather than about a constraint name.
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"The finish time has to be after the start time", null);
		}

		Integer id = asInteger(request.get("serAvailabilityRuleId"));
		ConsultationAvailabilityRule rule = id != null
				? repositoryRule.findById(id).orElse(new ConsultationAvailabilityRule())
				: new ConsultationAvailabilityRule();

		rule.setSerHostId(hostId);
		rule.setNumDayOfWeek(dayOfWeek);
		rule.setTmeStartTime(start);
		rule.setTmeEndTime(end);
		rule.setUpdatedDate(Instant.now());

		repositoryRule.save(rule);
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Saved", describe(rule));
	}

	@PostMapping(value = "/availability/deleteRule", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage deleteRule(@RequestBody Map<String, Object> request) {
		Integer id = asInteger(request.get("serAvailabilityRuleId"));
		return repositoryRule.findById(id == null ? -1 : id).map(rule -> {
			rule.setBlnIsDeleted(true);
			rule.setUpdatedDate(Instant.now());
			repositoryRule.save(rule);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Removed", null);
		}).orElseGet(() -> new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
				"No such rule", null));
	}

	@PostMapping(value = "/availability/saveException", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage saveException(@RequestBody Map<String, Object> request) {
		Integer hostId = asInteger(request.get("serHostId"));
		LocalDate onDate = asDate(request.get("dteOnDate"));
		Boolean available = asBoolean(request.get("blnIsAvailable"));
		LocalTime start = asTime(request.get("tmeStartTime"));
		LocalTime end = asTime(request.get("tmeEndTime"));

		if (hostId == null || onDate == null) {
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"A host and a date are needed", null);
		}
		if (Boolean.TRUE.equals(available)) {
			if (start == null || end == null) {
				return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
						"Opening a day needs a start and finish time", null);
			}
			if (!end.isAfter(start)) {
				return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
						"The finish time has to be after the start time", null);
			}
		}

		Integer id = asInteger(request.get("serAvailabilityExceptionId"));
		ConsultationAvailabilityException exception = id != null
				? repositoryException.findById(id).orElse(new ConsultationAvailabilityException())
				: new ConsultationAvailabilityException();

		exception.setSerHostId(hostId);
		exception.setDteOnDate(onDate);
		exception.setBlnIsAvailable(available);
		exception.setTmeStartTime(start);
		exception.setTmeEndTime(end);
		exception.setTxtReason(asString(request.get("txtReason")));
		exception.setUpdatedDate(Instant.now());

		repositoryException.save(exception);
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Saved", describe(exception));
	}

	@PostMapping(value = "/availability/deleteException", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage deleteException(@RequestBody Map<String, Object> request) {
		Integer id = asInteger(request.get("serAvailabilityExceptionId"));
		return repositoryException.findById(id == null ? -1 : id).map(exception -> {
			exception.setBlnIsDeleted(true);
			exception.setUpdatedDate(Instant.now());
			repositoryException.save(exception);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Removed", null);
		}).orElseGet(() -> new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
				"No such exception", null));
	}

	private Map<String, Object> describe(ConsultationAvailabilityRule rule) {
		Map<String, Object> described = new LinkedHashMap<>();
		described.put("serAvailabilityRuleId", rule.getSerAvailabilityRuleId());
		described.put("serHostId", rule.getSerHostId());
		described.put("numDayOfWeek", rule.getNumDayOfWeek());
		described.put("tmeStartTime", String.valueOf(rule.getTmeStartTime()));
		described.put("tmeEndTime", String.valueOf(rule.getTmeEndTime()));
		return described;
	}

	private Map<String, Object> describe(ConsultationAvailabilityException exception) {
		Map<String, Object> described = new LinkedHashMap<>();
		described.put("serAvailabilityExceptionId", exception.getSerAvailabilityExceptionId());
		described.put("serHostId", exception.getSerHostId());
		described.put("dteOnDate", String.valueOf(exception.getDteOnDate()));
		described.put("blnIsAvailable", exception.getBlnIsAvailable());
		described.put("tmeStartTime", exception.getTmeStartTime() == null
				? null : String.valueOf(exception.getTmeStartTime()));
		described.put("tmeEndTime", exception.getTmeEndTime() == null
				? null : String.valueOf(exception.getTmeEndTime()));
		described.put("txtReason", exception.getTxtReason());
		return described;
	}

	// -----------------------------------------------------------------
	// Connected calendars
	// -----------------------------------------------------------------

	/** A host's connected calendars, and whether connecting is possible at all. */
	@PostMapping(value = "/calendars", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage calendars(@RequestBody Map<String, Object> request) {
		Integer hostId = asInteger(request.get("serHostId"));
		if (hostId == null) {
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"A host is needed", null);
		}

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("connections", serviceCalendarConnection.forHost(hostId).stream()
				.map(this::describe).toList());

		/*
		 * What the server can actually do, so the screen offers "Connect Google"
		 * only where that will work. Without this the administrator presses a
		 * button and is told, after a redirect, that the server was never set up
		 * for it.
		 */
		Map<String, Object> available = new LinkedHashMap<>();
		available.put(CalendarConnection.GOOGLE,
				serviceCalendarConnection.isConfigured(CalendarConnection.GOOGLE));
		available.put(CalendarConnection.MICROSOFT,
				serviceCalendarConnection.isConfigured(CalendarConnection.MICROSOFT));
		result.put("available", available);

		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Calendars", result);
	}

	/**
	 * Where to send the administrator to grant access.
	 *
	 * <p>
	 * Returns the URL rather than redirecting, because this is called by the
	 * admin portal's own fetch — a redirect would be followed by the fetch
	 * instead of by the browser, and the consent screen would arrive as CORS-
	 * blocked HTML in a JavaScript variable rather than as a page somebody can
	 * use.
	 */
	@PostMapping(value = "/calendars/connect", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage connectCalendar(@RequestBody Map<String, Object> request) {
		Integer hostId = asInteger(request.get("serHostId"));
		String provider = asString(request.get("txtProvider"));

		if (hostId == null || provider == null) {
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"A host and a provider are needed", null);
		}

		try {
			Map<String, Object> result = new LinkedHashMap<>();
			result.put("txtAuthorisationUrl",
					serviceCalendarConnection.authorisationUrl(hostId, provider));
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Sign in to continue", result);
		} catch (ServiceCalendarConnection.NotConfiguredException e) {
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					e.getMessage(), null);
		}
	}

	@PostMapping(value = "/calendars/disconnect", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage disconnectCalendar(@RequestBody Map<String, Object> request) {
		Integer id = asInteger(request.get("serCalendarConnectionId"));
		if (id == null) {
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"A calendar is needed", null);
		}
		serviceCalendarConnection.disconnect(id);
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Disconnected", null);
	}

	/** Chooses which calendar this host's consultations are written to. */
	@PostMapping(value = "/calendars/writeTo", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage chooseWriteTarget(@RequestBody Map<String, Object> request) {
		Integer id = asInteger(request.get("serCalendarConnectionId"));
		if (id == null) {
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"A calendar is needed", null);
		}
		try {
			serviceCalendarConnection.chooseWriteTarget(id);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Saved", null);
		} catch (ServiceCalendarConnection.NotConfiguredException e) {
			return new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND,
					e.getMessage(), null);
		}
	}

	/**
	 * What the screen shows about one connection.
	 *
	 * <p>
	 * Never the tokens, encrypted or otherwise. They are of no use on a screen
	 * and every response they appear in is another place they can be captured.
	 */
	private Map<String, Object> describe(CalendarConnection connection) {
		Map<String, Object> described = new LinkedHashMap<>();
		described.put("serCalendarConnectionId", connection.getSerCalendarConnectionId());
		described.put("txtProvider", connection.getTxtProvider());
		described.put("txtAccountEmail", connection.getTxtAccountEmail());
		described.put("txtCalendarName", connection.getTxtCalendarName());
		described.put("blnIsWriteTarget", connection.getBlnIsWriteTarget());
		described.put("txtSyncStatus", connection.getTxtSyncStatus());
		described.put("txtSyncError", connection.getTxtSyncError());
		described.put("dteLastSyncedAt", connection.getDteLastSyncedAt() == null
				? null : connection.getDteLastSyncedAt().toString());
		return described;
	}

	// -----------------------------------------------------------------
	// The diary
	// -----------------------------------------------------------------

	@PostMapping(value = "/bookings", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage bookings(@RequestBody Map<String, Object> request) {
		int page = asIntegerOr(request.get("pageNumber"), 0);
		int size = Math.min(asIntegerOr(request.get("pageSize"), 20), 250);

		var found = repositoryBooking.search(
				asInteger(request.get("serHostId")),
				asString(request.get("txtStatus")),
				asInstant(request.get("dteFrom")),
				asInstant(request.get("dteTo")),
				// No Sort here: the query orders already, and a Pageable sort is
				// appended to that ORDER BY rather than replacing it.
				PageRequest.of(page, size));

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("content", found.getContent().stream().map(this::describe).toList());
		result.put("totalElements", found.getTotalElements());
		result.put("totalPages", found.getTotalPages());
		result.put("size", found.getSize());
		result.put("number", found.getNumber());

		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Bookings", result);
	}

	/** Requests waiting on somebody. The queue the team works from. */
	@PostMapping(value = "/bookings/pending", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage pending() {
		List<Map<String, Object>> waiting = new ArrayList<>();
		for (ConsultationBooking booking : serviceConsultation.awaitingConfirmation()) {
			waiting.add(describe(booking));
		}
		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Awaiting confirmation", waiting);
	}

	@PostMapping(value = "/bookings/confirm", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage confirm(@RequestBody Map<String, Object> request) {
		BookingOutcome outcome = serviceConsultation.confirm(
				asInteger(request.get("serConsultationBookingId")));
		return outcomeResponse(outcome);
	}

	@PostMapping(value = "/bookings/decline", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage decline(@RequestBody Map<String, Object> request) {
		BookingOutcome outcome = serviceConsultation.decline(
				asInteger(request.get("serConsultationBookingId")),
				asString(request.get("txtReason")));
		return outcomeResponse(outcome);
	}

	@PostMapping(value = "/bookings/cancel", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage cancel(@RequestBody Map<String, Object> request) {
		BookingOutcome outcome = serviceConsultation.cancel(
				asInteger(request.get("serConsultationBookingId")),
				asString(request.get("txtReason")));
		return outcomeResponse(outcome);
	}

	/**
	 * Books a meeting by hand — somebody rang up, or it was arranged by email.
	 *
	 * <p>
	 * Goes through the same service as a customer booking, so it meets the same
	 * exclusion constraint. An administrator can book outside published hours,
	 * which is often the whole reason they are doing it by hand, but they
	 * cannot book over somebody who already has that time.
	 */
	@PostMapping(value = "/bookings/add", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage addBooking(@RequestBody Map<String, Object> request) {
		BookingOutcome outcome = serviceConsultation.book(
				asInteger(request.get("serConsultationTypeId")),
				asInteger(request.get("serHostId")),
				asInstant(request.get("dteStartsAt")),
				asString(request.get("txtCustomerName")),
				asString(request.get("txtCustomerEmail")),
				asString(request.get("txtCustomerPhone")),
				asString(request.get("txtCustomerTimeZone")),
				asString(request.get("txtNotes")),
				asInteger(request.get("serCustId")),
				asInteger(request.get("serEventMasterId")));

		LOGGER.info("Consultation added by hand: accepted={}", outcome.accepted());
		return outcomeResponse(outcome);
	}

	private ResponseMessage outcomeResponse(BookingOutcome outcome) {
		return new ResponseMessage(
				outcome.accepted() ? HttpStatus.OK.value() : HttpStatus.CONFLICT.value(),
				outcome.accepted() ? HttpStatus.OK : HttpStatus.CONFLICT,
				outcome.message(),
				outcome.booking() == null ? null : describe(outcome.booking()));
	}

	/** The full picture, which is fine here — this is the team's own screen. */
	private Map<String, Object> describe(ConsultationBooking booking) {
		Map<String, Object> described = new LinkedHashMap<>();
		described.put("serConsultationBookingId", booking.getSerConsultationBookingId());
		described.put("serHostId", booking.getSerHostId());
		described.put("serConsultationTypeId", booking.getSerConsultationTypeId());
		described.put("serEventMasterId", booking.getSerEventMasterId());
		described.put("txtCustomerName", booking.getTxtCustomerName());
		described.put("txtCustomerEmail", booking.getTxtCustomerEmail());
		described.put("txtCustomerPhone", booking.getTxtCustomerPhone());
		described.put("txtNotes", booking.getTxtNotes());
		described.put("dteStartsAt", booking.getDteStartsAt().toString());
		described.put("dteEndsAt", booking.getDteEndsAt().toString());
		described.put("txtCustomerTimeZone", booking.getTxtCustomerTimeZone());
		described.put("txtStatus", booking.getTxtStatus());
		described.put("dteHoldExpiresAt", booking.getDteHoldExpiresAt() == null
				? null : booking.getDteHoldExpiresAt().toString());
		described.put("txtVideoJoinUrl", booking.getTxtVideoJoinUrl());
		described.put("txtExternalSyncStatus", booking.getTxtExternalSyncStatus());
		return described;
	}

	// -----------------------------------------------------------------

	private interface Setter {
		void set(Object value);
	}

	/** Only touches a field the request actually mentioned. */
	private void setIfPresent(Map<String, Object> request, String key, Setter setter) {
		if (request.containsKey(key) && request.get(key) != null) {
			setter.set(request.get(key));
		}
	}

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

	private int asIntegerOr(Object value, int fallback) {
		Integer parsed = asInteger(value);
		return parsed == null ? fallback : parsed;
	}

	private Boolean asBoolean(Object value) {
		if (value instanceof Boolean bool) {
			return bool;
		}
		return value != null && Boolean.parseBoolean(String.valueOf(value));
	}

	private String asString(Object value) {
		return value == null ? null : String.valueOf(value);
	}

	private LocalDate asDate(Object value) {
		try {
			return value == null ? null : LocalDate.parse(String.valueOf(value));
		} catch (Exception e) {
			return null;
		}
	}

	private LocalTime asTime(Object value) {
		try {
			return value == null ? null : LocalTime.parse(String.valueOf(value));
		} catch (Exception e) {
			return null;
		}
	}

	private Instant asInstant(Object value) {
		try {
			return value == null ? null : Instant.parse(String.valueOf(value));
		} catch (Exception e) {
			return null;
		}
	}

	private boolean isKnownZone(String zone) {
		try {
			java.time.ZoneId.of(zone);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
