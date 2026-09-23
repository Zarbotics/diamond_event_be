package com.zbs.de.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

import com.zbs.de.repository.RepositoryConsultationAvailabilityException;
import com.zbs.de.repository.RepositoryConsultationAvailabilityRule;
import com.zbs.de.repository.RepositoryConsultationBooking;
import com.zbs.de.repository.RepositoryConsultationHost;
import com.zbs.de.repository.RepositoryConsultationType;
import com.zbs.de.util.ResponseMessage;

/**
 * Configuring consultations from the admin portal.
 *
 * <p>
 * The point of these endpoints is that <em>nothing</em> about how consultations
 * work is fixed in code: who takes them, when they work, how long a meeting
 * runs, how much notice is needed, whether a request has to be agreed and
 * whether agreeing it makes a video link are all rows in tables. So the thing
 * worth testing is the whole loop — configure something here, and see the
 * customer-facing calendar change because of it.
 *
 * <p>
 * Driven through the controller beans rather than over HTTP. The security
 * chain is covered separately in {@code PortalEndpointPolicyTest}; what needs a
 * real database here is that saving a working week actually changes what is on
 * offer, and that the guards refuse the things they claim to.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class ConsultationAdminIT {

	private static final ZoneId LONDON = ZoneId.of("Europe/London");
	private static final String MARKER = "IT-ADMIN-CONSULT";

	@Autowired
	private ControllerConsultationAdmin admin;

	@Autowired
	private ControllerConsultation customerFacing;

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

	@AfterEach
	void removeSeed() {
		List<Integer> mine = repositoryHost.findAll().stream()
				.filter(h -> h.getTxtDisplayName() != null && h.getTxtDisplayName().startsWith(MARKER))
				.map(h -> h.getSerHostId())
				.toList();

		repositoryBooking.findAll().stream()
				.filter(b -> mine.contains(b.getSerHostId()))
				.forEach(repositoryBooking::delete);
		repositoryRule.findAll().stream()
				.filter(r -> mine.contains(r.getSerHostId()))
				.forEach(repositoryRule::delete);
		repositoryException.findAll().stream()
				.filter(e -> mine.contains(e.getSerHostId()))
				.forEach(repositoryException::delete);
		repositoryHost.findAll().stream()
				.filter(h -> h.getTxtDisplayName() != null && h.getTxtDisplayName().startsWith(MARKER))
				.forEach(repositoryHost::delete);
		repositoryType.findAll().stream()
				.filter(t -> t.getTxtName() != null && t.getTxtName().startsWith(MARKER))
				.forEach(repositoryType::delete);
	}

	// -----------------------------------------------------------------
	// Fixtures, built the way the portal builds them: through the endpoints.
	// -----------------------------------------------------------------

	private Integer newHost() {
		ResponseMessage saved = admin.saveHost(Map.of(
				"txtDisplayName", MARKER + " Host",
				"txtEmail", "it-admin-consult@example.com",
				"txtTimeZone", "Europe/London"));
		assertThat(saved.getCode()).isEqualTo(HttpStatus.OK.value());
		return (Integer) body(saved).get("serHostId");
	}

	private Integer newType(int durationMinutes) {
		ResponseMessage saved = admin.saveType(Map.of(
				"txtName", MARKER + " Consultation",
				"numDurationMinutes", durationMinutes,
				"numMinimumNoticeHours", 0,
				"numMaximumAdvanceDays", 365));
		assertThat(saved.getCode()).isEqualTo(HttpStatus.OK.value());
		return (Integer) body(saved).get("serConsultationTypeId");
	}

	/** A Monday far enough out that minimum notice never interferes. */
	private LocalDate aMondayAhead() {
		return LocalDate.now(LONDON).plusDays(21).with(DayOfWeek.MONDAY);
	}

	private ResponseMessage openMondayMornings(Integer hostId) {
		return admin.saveRule(Map.of(
				"serHostId", hostId,
				"numDayOfWeek", DayOfWeek.MONDAY.getValue(),
				"tmeStartTime", "09:00",
				"tmeEndTime", "12:00"));
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> body(ResponseMessage message) {
		return (Map<String, Object>) message.getResult();
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> list(ResponseMessage message) {
		return (List<Map<String, Object>>) message.getResult();
	}

	private List<Map<String, Object>> slotsFor(Integer typeId, LocalDate day) {
		ResponseMessage slots = customerFacing.slots(Map.of(
				"serConsultationTypeId", typeId,
				"dteFrom", day.toString(),
				"dteTo", day.plusDays(1).toString()));
		return list(slots);
	}

	// -----------------------------------------------------------------

	@Nested
	@DisplayName("who takes consultations")
	class Hosts {

		@Test
		@DisplayName("a host saved in the portal appears in the list")
		void savingAHostListsIt() {
			Integer hostId = newHost();

			assertThat(list(admin.hosts()))
					.extracting(h -> h.get("serHostId"))
					.contains(hostId);
		}

		@Test
		@DisplayName("a host needs a name and an email address")
		void aHostNeedsANameAndEmail() {
			ResponseMessage refused = admin.saveHost(Map.of("txtDisplayName", MARKER + " Host"));

			assertThat(refused.getCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
			assertThat(refused.getMessage()).contains("email");
		}

		@Test
		@DisplayName("a time zone that does not exist is refused, not quietly replaced")
		void anUnknownZoneIsRefused() {
			/*
			 * The entity falls back to Europe/London on a bad value rather than
			 * throwing, which is right at read time and disastrous at write time:
			 * a typo would move somebody's entire working week by hours and
			 * nothing would say so.
			 */
			ResponseMessage refused = admin.saveHost(Map.of(
					"txtDisplayName", MARKER + " Host",
					"txtEmail", "it-admin-consult@example.com",
					"txtTimeZone", "Europe/Birmingham"));

			assertThat(refused.getCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
			assertThat(refused.getMessage()).contains("time zone");
		}

		@Test
		@DisplayName("removing a host with meetings in the diary is refused")
		void aHostWithBookingsCannotBeRemoved() {
			Integer hostId = newHost();
			Integer typeId = newType(60);
			openMondayMornings(hostId);

			ResponseMessage booked = admin.addBooking(Map.of(
					"serConsultationTypeId", typeId,
					"serHostId", hostId,
					"dteStartsAt", aMondayAhead().atTime(10, 0).atZone(LONDON).toInstant().toString(),
					"txtCustomerName", "Rang Up",
					"txtCustomerEmail", "rang.up@example.com",
					"txtCustomerTimeZone", "Europe/London"));
			assertThat(booked.getCode()).isEqualTo(HttpStatus.OK.value());

			ResponseMessage refused = admin.deleteHost(Map.of("serHostId", hostId));

			assertThat(refused.getCode()).isEqualTo(HttpStatus.CONFLICT.value());
			assertThat(refused.getMessage()).contains("Cancel or move those first");

			// And it really is still there — a refusal that half-happened would be
			// worse than either outcome.
			assertThat(list(admin.hosts()))
					.extracting(h -> h.get("serHostId"))
					.contains(hostId);
		}

		@Test
		@DisplayName("a host with a clear diary is removed and stops being offered")
		void aFreeHostIsRemoved() {
			Integer hostId = newHost();
			Integer typeId = newType(60);
			openMondayMornings(hostId);
			assertThat(slotsFor(typeId, aMondayAhead())).isNotEmpty();

			assertThat(admin.deleteHost(Map.of("serHostId", hostId)).getCode())
					.isEqualTo(HttpStatus.OK.value());

			assertThat(list(admin.hosts()))
					.extracting(h -> h.get("serHostId"))
					.doesNotContain(hostId);
			assertThat(slotsFor(typeId, aMondayAhead()))
					.as("a removed host must not still be on offer to customers")
					.isEmpty();
		}
	}

	@Nested
	@DisplayName("what a consultation is")
	class Types {

		@Test
		@DisplayName("the meeting length set in the portal is the length customers are offered")
		void durationDrivesTheSlots() {
			Integer hostId = newHost();
			Integer typeId = newType(30);
			openMondayMornings(hostId);

			/*
			 * Nine to twelve, thirty-minute meetings on a thirty-minute interval:
			 * 9:00, 9:30, 10:00, 10:30, 11:00, 11:30.
			 */
			assertThat(slotsFor(typeId, aMondayAhead()))
					.extracting(s -> s.get("dteEndsAt"))
					.allSatisfy(end -> assertThat(end).isNotNull());
			assertThat(slotsFor(typeId, aMondayAhead())).hasSize(6);

			admin.saveType(Map.of(
					"serConsultationTypeId", typeId,
					"txtName", MARKER + " Consultation",
					"numDurationMinutes", 90));

			/*
			 * The same three hours as ninety-minute meetings, still offered every
			 * half hour: 9:00, 9:30, 10:00, 10:30. Four, not two — the candidates
			 * overlap, and booking one takes its neighbours with it. That is how
			 * these systems normally behave, and it is why the interval is a
			 * separate setting from the duration.
			 */
			assertThat(slotsFor(typeId, aMondayAhead()))
					.as("changing the meeting length in the portal must change the calendar")
					.hasSize(4);
		}

		@Test
		@DisplayName("the interval between start times is a setting of its own")
		void theIntervalIsConfigurable() {
			Integer hostId = newHost();
			Integer typeId = newType(60);
			openMondayMornings(hostId);

			// Hour-long meetings every half hour: 9:00 … 11:00.
			assertThat(slotsFor(typeId, aMondayAhead())).hasSize(5);

			admin.saveType(Map.of(
					"serConsultationTypeId", typeId,
					"txtName", MARKER + " Consultation",
					"numSlotIntervalMinutes", 60));

			// The same hour-long meetings, now only on the hour: 9:00, 10:00, 11:00.
			assertThat(slotsFor(typeId, aMondayAhead()))
					.as("the portal must be able to say 'on the hour only'")
					.hasSize(3);
		}

		@Test
		@DisplayName("start times cannot be set nought minutes apart")
		void theIntervalHasAFloor() {
			// Not fussiness: the slot finder walks forward by this amount, so
			// zero is a request that never returns rather than one that fails.
			ResponseMessage refused = admin.saveType(Map.of(
					"txtName", MARKER + " Consultation",
					"numDurationMinutes", 60,
					"numSlotIntervalMinutes", 0));

			assertThat(refused.getCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
		}

		@Test
		@DisplayName("a five-minute floor stops a nought-length meeting")
		void aDurationHasAFloor() {
			ResponseMessage refused = admin.saveType(Map.of(
					"txtName", MARKER + " Consultation",
					"numDurationMinutes", 0));

			assertThat(refused.getCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
		}

		@Test
		@DisplayName("editing a type leaves fields the request did not mention alone")
		void anEditIsPartial() {
			Integer typeId = newType(45);
			admin.saveType(Map.of(
					"serConsultationTypeId", typeId,
					"txtName", MARKER + " Consultation",
					"blnRequiresConfirmation", true));

			Map<String, Object> after = list(admin.types()).stream()
					.filter(t -> typeId.equals(t.get("serConsultationTypeId")))
					.findFirst().orElseThrow();

			assertThat(after.get("blnRequiresConfirmation")).isEqualTo(true);
			assertThat(after.get("numDurationMinutes"))
					.as("a request that said nothing about duration must not reset it")
					.isEqualTo(45);
		}

		@Test
		@DisplayName("turning a type off takes it off the customer's list")
		void deactivatingHidesIt() {
			Integer typeId = newType(45);
			assertThat(list(customerFacing.types()))
					.extracting(t -> t.get("serConsultationTypeId"))
					.contains(typeId);

			admin.saveType(Map.of(
					"serConsultationTypeId", typeId,
					"txtName", MARKER + " Consultation",
					"blnIsActive", false));

			assertThat(list(customerFacing.types()))
					.extracting(t -> t.get("serConsultationTypeId"))
					.doesNotContain(typeId);
		}
	}

	@Nested
	@DisplayName("working hours")
	class Availability {

		@Test
		@DisplayName("no hours means nothing on offer; adding hours puts times on the calendar")
		void hoursDriveTheCalendar() {
			Integer hostId = newHost();
			Integer typeId = newType(60);

			assertThat(slotsFor(typeId, aMondayAhead()))
					.as("a host with no working hours has nothing to book")
					.isEmpty();

			assertThat(openMondayMornings(hostId).getCode()).isEqualTo(HttpStatus.OK.value());

			// Nine to twelve, hour-long meetings every half hour: 9:00 … 11:00.
			assertThat(slotsFor(typeId, aMondayAhead())).hasSize(5);
		}

		@Test
		@DisplayName("a finish before the start is refused with a message about hours")
		void backwardsHoursAreRefused() {
			Integer hostId = newHost();

			ResponseMessage refused = admin.saveRule(Map.of(
					"serHostId", hostId,
					"numDayOfWeek", DayOfWeek.MONDAY.getValue(),
					"tmeStartTime", "17:00",
					"tmeEndTime", "09:00"));

			assertThat(refused.getCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
			assertThat(refused.getMessage()).contains("after the start time");
		}

		@Test
		@DisplayName("a day of the week outside 1-7 is refused")
		void anImpossibleDayIsRefused() {
			Integer hostId = newHost();

			assertThat(admin.saveRule(Map.of(
					"serHostId", hostId,
					"numDayOfWeek", 0,
					"tmeStartTime", "09:00",
					"tmeEndTime", "17:00")).getCode())
					.isEqualTo(HttpStatus.BAD_REQUEST.value());
		}

		@Test
		@DisplayName("closing a single day empties that day and leaves the rest")
		void aClosureEmptiesOneDay() {
			Integer hostId = newHost();
			Integer typeId = newType(60);
			openMondayMornings(hostId);

			LocalDate closed = aMondayAhead();
			LocalDate stillOpen = closed.plusWeeks(1);
			assertThat(slotsFor(typeId, closed)).isNotEmpty();

			ResponseMessage saved = admin.saveException(Map.of(
					"serHostId", hostId,
					"dteOnDate", closed.toString(),
					"blnIsAvailable", false,
					"txtReason", "Bank holiday"));
			assertThat(saved.getCode()).isEqualTo(HttpStatus.OK.value());

			assertThat(slotsFor(typeId, closed)).isEmpty();
			assertThat(slotsFor(typeId, stillOpen))
					.as("closing one day must not close the same weekday for ever")
					.isNotEmpty();
		}

		@Test
		@DisplayName("a one-off opening works on a host with no weekly hours at all")
		void anOpeningStandsOnItsOwn() {
			/*
			 * "We are not normally available, but we are open this Sunday for the
			 * wedding fair." The finder used to give up the moment a host had no
			 * weekly rules, so this produced an empty calendar and nothing
			 * anywhere said why — the opening had saved perfectly well.
			 */
			Integer hostId = newHost();
			Integer typeId = newType(60);
			LocalDate sunday = aMondayAhead().minusDays(1);

			admin.saveException(Map.of(
					"serHostId", hostId,
					"dteOnDate", sunday.toString(),
					"blnIsAvailable", true,
					"tmeStartTime", "10:00",
					"tmeEndTime", "13:00",
					"txtReason", "Wedding fair"));

			// Ten to one, hour-long meetings every half hour: 10:00 … 12:00.
			assertThat(slotsFor(typeId, sunday)).hasSize(5);
			// And it really is only that day.
			assertThat(slotsFor(typeId, sunday.minusDays(7))).isEmpty();
		}

		@Test
		@DisplayName("an opening replaces the weekly hours for that day rather than adding to them")
		void anOpeningReplacesTheWeeklyPattern() {
			Integer hostId = newHost();
			Integer typeId = newType(60);
			openMondayMornings(hostId);
			LocalDate monday = aMondayAhead();

			admin.saveException(Map.of(
					"serHostId", hostId,
					"dteOnDate", monday.toString(),
					"blnIsAvailable", true,
					"tmeStartTime", "14:00",
					"tmeEndTime", "17:00",
					"txtReason", "Morning blocked out"));

			/*
			 * "This Monday, two till five" means those hours — not those hours
			 * plus the usual morning. Somebody moving their day is not offering
			 * twice as much of it.
			 */
			assertThat(slotsFor(typeId, monday)).hasSize(5);
			assertThat(slotsFor(typeId, monday))
					.extracting(s -> s.get("dteStartsAt"))
					.allSatisfy(start -> assertThat(
							Instant.parse((String) start).atZone(LONDON).getHour())
							.isGreaterThanOrEqualTo(14));
		}

		@Test
		@DisplayName("opening a day without times is refused")
		void anOpeningNeedsTimes() {
			Integer hostId = newHost();

			ResponseMessage refused = admin.saveException(Map.of(
					"serHostId", hostId,
					"dteOnDate", aMondayAhead().toString(),
					"blnIsAvailable", true));

			assertThat(refused.getCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
			assertThat(refused.getMessage()).contains("start and finish time");
		}

		@Test
		@DisplayName("removing a rule takes the hours back off the calendar")
		void deletingARuleClosesTheDay() {
			Integer hostId = newHost();
			Integer typeId = newType(60);
			ResponseMessage rule = openMondayMornings(hostId);
			assertThat(slotsFor(typeId, aMondayAhead())).isNotEmpty();

			admin.deleteRule(Map.of("serAvailabilityRuleId", body(rule).get("serAvailabilityRuleId")));

			assertThat(slotsFor(typeId, aMondayAhead())).isEmpty();
		}

		@Test
		@DisplayName("the availability screen shows back what was saved")
		void availabilityReadsBack() {
			Integer hostId = newHost();
			openMondayMornings(hostId);
			admin.saveException(Map.of(
					"serHostId", hostId,
					"dteOnDate", aMondayAhead().toString(),
					"blnIsAvailable", false,
					"txtReason", "Bank holiday"));

			Map<String, Object> availability = body(admin.availability(Map.of("serHostId", hostId)));

			assertThat((List<?>) availability.get("rules")).hasSize(1);
			assertThat((List<?>) availability.get("exceptions")).hasSize(1);
		}
	}

	@Nested
	@DisplayName("the diary")
	class Diary {

		@Test
		@DisplayName("a meeting added by hand cannot be put on top of an existing one")
		void manualBookingsMeetTheSameConstraint() {
			Integer hostId = newHost();
			Integer typeId = newType(60);
			openMondayMornings(hostId);
			String at = aMondayAhead().atTime(10, 0).atZone(LONDON).toInstant().toString();

			Map<String, Object> first = Map.of(
					"serConsultationTypeId", typeId,
					"serHostId", hostId,
					"dteStartsAt", at,
					"txtCustomerName", "Rang Up",
					"txtCustomerEmail", "rang.up@example.com",
					"txtCustomerTimeZone", "Europe/London");
			assertThat(admin.addBooking(first).getCode()).isEqualTo(HttpStatus.OK.value());

			/*
			 * Somebody in the office typing the same time in twice, or two people
			 * on the phone at once. The exclusion constraint is what stops it,
			 * and it has to stop it here as well — a manual booking that bypassed
			 * the service would be the one way to double-book.
			 */
			assertThat(admin.addBooking(first).getCode()).isEqualTo(HttpStatus.CONFLICT.value());
		}

		@Test
		@DisplayName("a request waits in the queue, and confirming it takes it out")
		void theConfirmationQueue() {
			Integer hostId = newHost();
			Integer typeId = newType(60);
			openMondayMornings(hostId);
			admin.saveType(Map.of(
					"serConsultationTypeId", typeId,
					"txtName", MARKER + " Consultation",
					"blnRequiresConfirmation", true,
					"numConfirmationWindowHours", 48));

			ResponseMessage requested = admin.addBooking(Map.of(
					"serConsultationTypeId", typeId,
					"serHostId", hostId,
					"dteStartsAt", aMondayAhead().atTime(10, 0).atZone(LONDON).toInstant().toString(),
					"txtCustomerName", "Asked Politely",
					"txtCustomerEmail", "asked@example.com",
					"txtCustomerTimeZone", "Europe/London"));
			Integer bookingId = (Integer) body(requested).get("serConsultationBookingId");
			assertThat(body(requested).get("txtStatus")).isEqualTo("PENDING");

			assertThat(list(admin.pending()))
					.extracting(b -> b.get("serConsultationBookingId"))
					.contains(bookingId);

			assertThat(admin.confirm(Map.of("serConsultationBookingId", bookingId)).getCode())
					.isEqualTo(HttpStatus.OK.value());

			assertThat(list(admin.pending()))
					.extracting(b -> b.get("serConsultationBookingId"))
					.doesNotContain(bookingId);
		}

		@Test
		@DisplayName("declining a request gives the slot back")
		void decliningReleasesTheSlot() {
			Integer hostId = newHost();
			Integer typeId = newType(60);
			openMondayMornings(hostId);
			admin.saveType(Map.of(
					"serConsultationTypeId", typeId,
					"txtName", MARKER + " Consultation",
					"blnRequiresConfirmation", true));

			Instant at = aMondayAhead().atTime(10, 0).atZone(LONDON).toInstant();
			ResponseMessage requested = admin.addBooking(Map.of(
					"serConsultationTypeId", typeId,
					"serHostId", hostId,
					"dteStartsAt", at.toString(),
					"txtCustomerName", "Asked Politely",
					"txtCustomerEmail", "asked@example.com",
					"txtCustomerTimeZone", "Europe/London"));

			assertThat(slotsFor(typeId, aMondayAhead()))
					.as("a pending request holds its time")
					.extracting(s -> s.get("dteStartsAt"))
					.doesNotContain(at.toString());

			admin.decline(Map.of(
					"serConsultationBookingId", body(requested).get("serConsultationBookingId"),
					"txtReason", "Away that week"));

			assertThat(slotsFor(typeId, aMondayAhead()))
					.extracting(s -> s.get("dteStartsAt"))
					.contains(at.toString());
		}

		@Test
		@DisplayName("the diary pages, and a page size cannot be talked into loading everything")
		void theDiaryPages() {
			Integer hostId = newHost();
			Integer typeId = newType(60);
			openMondayMornings(hostId);
			for (int hour = 9; hour < 12; hour++) {
				admin.addBooking(Map.of(
						"serConsultationTypeId", typeId,
						"serHostId", hostId,
						"dteStartsAt", aMondayAhead().atTime(hour, 0).atZone(LONDON).toInstant().toString(),
						"txtCustomerName", "Booking " + hour,
						"txtCustomerEmail", "booking" + hour + "@example.com",
						"txtCustomerTimeZone", "Europe/London"));
			}

			Map<String, Object> page = body(admin.bookings(Map.of(
					"serHostId", hostId, "pageNumber", 0, "pageSize", 2)));

			assertThat((List<?>) page.get("content")).hasSize(2);
			assertThat(page.get("totalElements")).isEqualTo(3L);

			Map<String, Object> huge = body(admin.bookings(Map.of(
					"serHostId", hostId, "pageSize", 100_000)));
			assertThat(huge.get("size"))
					.as("the page size cap is what stops one screen pulling the whole diary")
					.isEqualTo(250);
		}

		@Test
		@DisplayName("the diary filters by status")
		void theDiaryFiltersByStatus() {
			Integer hostId = newHost();
			Integer typeId = newType(60);
			openMondayMornings(hostId);
			ResponseMessage booked = admin.addBooking(Map.of(
					"serConsultationTypeId", typeId,
					"serHostId", hostId,
					"dteStartsAt", aMondayAhead().atTime(10, 0).atZone(LONDON).toInstant().toString(),
					"txtCustomerName", "Rang Up",
					"txtCustomerEmail", "rang.up@example.com",
					"txtCustomerTimeZone", "Europe/London"));

			assertThat((List<?>) body(admin.bookings(Map.of(
					"serHostId", hostId, "txtStatus", "BOOKED"))).get("content")).hasSize(1);
			assertThat((List<?>) body(admin.bookings(Map.of(
					"serHostId", hostId, "txtStatus", "CANCELLED"))).get("content")).isEmpty();

			admin.cancel(Map.of(
					"serConsultationBookingId", body(booked).get("serConsultationBookingId"),
					"txtReason", "Customer rang back"));

			assertThat((List<?>) body(admin.bookings(Map.of(
					"serHostId", hostId, "txtStatus", "BOOKED"))).get("content")).isEmpty();
			assertThat((List<?>) body(admin.bookings(Map.of(
					"serHostId", hostId, "txtStatus", "CANCELLED"))).get("content")).hasSize(1);
		}
	}
}
