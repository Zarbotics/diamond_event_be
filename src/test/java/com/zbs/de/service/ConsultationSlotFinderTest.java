package com.zbs.de.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.zbs.de.model.ConsultationAvailabilityException;
import com.zbs.de.model.ConsultationAvailabilityRule;
import com.zbs.de.model.ConsultationType;
import com.zbs.de.service.ConsultationSlotFinder.Busy;
import com.zbs.de.service.ConsultationSlotFinder.Slot;

/**
 * The rules a slot has to satisfy before it can be offered.
 *
 * <p>
 * {@link ConsultationSlotFinder} takes its clock as a parameter and touches no
 * database, so every case here is stated at a fixed date — including the two
 * days a year when local time in the UK is not what arithmetic on UTC would
 * suggest. Those are the days a scheduling system quietly offers meetings an
 * hour out, and they are impossible to test at all if the code reads the clock
 * itself.
 */
class ConsultationSlotFinderTest {

	private static final ZoneId LONDON = ZoneId.of("Europe/London");

	private ConsultationType type(int minutes, int noticeHours, int bufferBefore, int bufferAfter) {
		ConsultationType t = new ConsultationType();
		t.setTxtName("Consultation");
		t.setNumDurationMinutes(minutes);
		t.setNumMinimumNoticeHours(noticeHours);
		t.setNumMaximumAdvanceDays(365);
		t.setNumBufferBeforeMinutes(bufferBefore);
		t.setNumBufferAfterMinutes(bufferAfter);
		return t;
	}

	private ConsultationAvailabilityRule rule(DayOfWeek day, String from, String to) {
		ConsultationAvailabilityRule r = new ConsultationAvailabilityRule();
		r.setSerHostId(1);
		r.setNumDayOfWeek(day.getValue());
		r.setTmeStartTime(LocalTime.parse(from));
		r.setTmeEndTime(LocalTime.parse(to));
		return r;
	}

	private ConsultationAvailabilityException closed(String date, String reason) {
		ConsultationAvailabilityException e = new ConsultationAvailabilityException();
		e.setSerHostId(1);
		e.setDteOnDate(LocalDate.parse(date));
		e.setBlnIsAvailable(false);
		e.setTxtReason(reason);
		return e;
	}

	private ConsultationAvailabilityException opened(String date, String from, String to) {
		ConsultationAvailabilityException e = new ConsultationAvailabilityException();
		e.setSerHostId(1);
		e.setDteOnDate(LocalDate.parse(date));
		e.setBlnIsAvailable(true);
		e.setTmeStartTime(LocalTime.parse(from));
		e.setTmeEndTime(LocalTime.parse(to));
		return e;
	}

	/** An instant from a London wall-clock reading. */
	private Instant london(String isoLocal) {
		return ZonedDateTime.of(java.time.LocalDateTime.parse(isoLocal), LONDON).toInstant();
	}

	/** What a slot looks like on a London clock, for readable assertions. */
	private String asLondon(Instant instant) {
		return instant.atZone(LONDON).toLocalDateTime().toString();
	}

	private List<Slot> find(ConsultationType type, List<ConsultationAvailabilityRule> rules,
			List<ConsultationAvailabilityException> exceptions, List<Busy> busy,
			String fromLocal, String toLocal, String nowLocal) {
		return ConsultationSlotFinder.findSlots(type, LONDON, rules, exceptions, busy,
				london(fromLocal), london(toLocal), london(nowLocal), 30);
	}

	// =================================================================

	@Nested
	@DisplayName("working hours")
	class WorkingHours {

		@Test
		@DisplayName("slots come from the host's weekly hours, at the step asked for")
		void slotsFollowTheWeeklyRules() {
			// Wednesday 2 September 2026, 09:00-12:00, hour-long meetings.
			List<Slot> slots = find(type(60, 0, 0, 0),
					List.of(rule(DayOfWeek.WEDNESDAY, "09:00", "12:00")),
					List.of(), List.of(),
					"2026-09-02T00:00", "2026-09-03T00:00", "2026-09-01T09:00");

			assertThat(slots).extracting(s -> asLondon(s.startsAt()))
					.containsExactly("2026-09-02T09:00", "2026-09-02T09:30",
							"2026-09-02T10:00", "2026-09-02T10:30", "2026-09-02T11:00");
		}

		@Test
		@DisplayName("a meeting never runs past closing time")
		void theLastSlotFitsInsideTheDay() {
			// 09:00-10:00 with a 45-minute meeting leaves room for one start.
			List<Slot> slots = find(type(45, 0, 0, 0),
					List.of(rule(DayOfWeek.WEDNESDAY, "09:00", "10:00")),
					List.of(), List.of(),
					"2026-09-02T00:00", "2026-09-03T00:00", "2026-09-01T09:00");

			assertThat(slots).extracting(s -> asLondon(s.startsAt()))
					.containsExactly("2026-09-02T09:00");
			assertThat(asLondon(slots.get(0).endsAt())).isEqualTo("2026-09-02T09:45");
		}

		@Test
		@DisplayName("a day with no rule offers nothing")
		void daysWithoutRulesAreEmpty() {
			// Rules for Wednesday only; asking about the Thursday.
			assertThat(find(type(60, 0, 0, 0),
					List.of(rule(DayOfWeek.WEDNESDAY, "09:00", "17:00")),
					List.of(), List.of(),
					"2026-09-03T00:00", "2026-09-04T00:00", "2026-09-01T09:00")).isEmpty();
		}
	}

	@Nested
	@DisplayName("notice and advance limits")
	class Limits {

		@Test
		@DisplayName("a slot inside the minimum notice is not offered")
		void minimumNoticeIsRespected() {
			// 24 hours' notice, asked at 09:00 on the Tuesday: the Wednesday
			// morning is inside the notice period, the afternoon is not.
			List<Slot> slots = find(type(60, 24, 0, 0),
					List.of(rule(DayOfWeek.WEDNESDAY, "08:00", "17:00")),
					List.of(), List.of(),
					"2026-09-02T00:00", "2026-09-03T00:00", "2026-09-01T09:00");

			assertThat(slots).isNotEmpty();
			assertThat(asLondon(slots.get(0).startsAt()))
					.as("nothing before 09:00 the next day")
					.isEqualTo("2026-09-02T09:00");
		}

		@Test
		@DisplayName("a slot beyond the maximum advance is not offered")
		void maximumAdvanceIsRespected() {
			ConsultationType type = type(60, 0, 0, 0);
			type.setNumMaximumAdvanceDays(7);

			// Asking about a date five weeks out.
			assertThat(find(type,
					List.of(rule(DayOfWeek.WEDNESDAY, "09:00", "17:00")),
					List.of(), List.of(),
					"2026-10-07T00:00", "2026-10-08T00:00", "2026-09-01T09:00")).isEmpty();
		}
	}

	@Nested
	@DisplayName("exceptions to the weekly pattern")
	class Exceptions {

		@Test
		@DisplayName("a closed day offers nothing, even though the weekly rule covers it")
		void aClosureBeatsTheWeeklyRule() {
			assertThat(find(type(60, 0, 0, 0),
					List.of(rule(DayOfWeek.WEDNESDAY, "09:00", "17:00")),
					List.of(closed("2026-09-02", "Bank holiday")), List.of(),
					"2026-09-02T00:00", "2026-09-03T00:00", "2026-09-01T09:00")).isEmpty();
		}

		@Test
		@DisplayName("a one-off opening works on a day with no weekly rule at all")
		void anOpeningAddsADay() {
			// Saturday, which the weekly rules never cover.
			List<Slot> slots = find(type(60, 0, 0, 0),
					List.of(rule(DayOfWeek.WEDNESDAY, "09:00", "17:00")),
					List.of(opened("2026-09-05", "10:00", "12:00")), List.of(),
					"2026-09-05T00:00", "2026-09-06T00:00", "2026-09-01T09:00");

			assertThat(slots).extracting(s -> asLondon(s.startsAt()))
					.containsExactly("2026-09-05T10:00", "2026-09-05T10:30", "2026-09-05T11:00");
		}

		@Test
		@DisplayName("a one-off opening works even when there are no weekly rules at all")
		void anOpeningStandsWithoutAnyWeeklyPattern() {
			/*
			 * "We are not normally available, but we are open this Saturday for
			 * the wedding fair."
			 *
			 * The finder used to return nothing the instant the rules list was
			 * empty, so this produced a blank calendar with no error anywhere —
			 * the opening had saved perfectly well and simply had no effect. The
			 * other opening tests all happen to pass a weekly rule as well, which
			 * is what hid it.
			 */
			List<Slot> slots = find(type(60, 0, 0, 0),
					List.of(),
					List.of(opened("2026-09-05", "10:00", "12:00")), List.of(),
					"2026-09-05T00:00", "2026-09-06T00:00", "2026-09-01T09:00");

			assertThat(slots).extracting(s -> asLondon(s.startsAt()))
					.containsExactly("2026-09-05T10:00", "2026-09-05T10:30", "2026-09-05T11:00");
		}

		@Test
		@DisplayName("a closure on its own is still nothing on offer")
		void aClosureAloneIsNotAvailability() {
			// The mirror of the case above: only an opening creates hours. A day
			// marked closed on a host with no weekly pattern stays empty.
			assertThat(find(type(60, 0, 0, 0),
					List.of(),
					List.of(closed("2026-09-05", "Bank holiday")), List.of(),
					"2026-09-05T00:00", "2026-09-06T00:00", "2026-09-01T09:00")).isEmpty();
		}

		@Test
		@DisplayName("an opening replaces that day's hours rather than adding to them")
		void anOpeningReplacesTheDay() {
			// "This Wednesday, 14:00 to 16:00" means those hours, not those plus
			// the usual nine to five.
			List<Slot> slots = find(type(60, 0, 0, 0),
					List.of(rule(DayOfWeek.WEDNESDAY, "09:00", "17:00")),
					List.of(opened("2026-09-02", "14:00", "16:00")), List.of(),
					"2026-09-02T00:00", "2026-09-03T00:00", "2026-09-01T09:00");

			assertThat(slots).extracting(s -> asLondon(s.startsAt()))
					.containsExactly("2026-09-02T14:00", "2026-09-02T14:30", "2026-09-02T15:00");
		}
	}

	@Nested
	@DisplayName("existing commitments")
	class Busyness {

		@Test
		@DisplayName("a booked meeting removes the slots it covers")
		void bookingsBlockSlots() {
			List<Slot> slots = find(type(60, 0, 0, 0),
					List.of(rule(DayOfWeek.WEDNESDAY, "09:00", "12:00")),
					List.of(),
					List.of(new Busy(london("2026-09-02T10:00"), london("2026-09-02T11:00"))),
					"2026-09-02T00:00", "2026-09-03T00:00", "2026-09-01T09:00");

			assertThat(slots).extracting(s -> asLondon(s.startsAt()))
					.containsExactly("2026-09-02T09:00", "2026-09-02T11:00");
		}

		@Test
		@DisplayName("a meeting may begin exactly when another ends")
		void touchingIsNotOverlapping() {
			List<Slot> slots = find(type(60, 0, 0, 0),
					List.of(rule(DayOfWeek.WEDNESDAY, "09:00", "11:00")),
					List.of(),
					List.of(new Busy(london("2026-09-02T08:00"), london("2026-09-02T09:00"))),
					"2026-09-02T00:00", "2026-09-03T00:00", "2026-09-01T09:00");

			assertThat(slots).extracting(s -> asLondon(s.startsAt()))
					.as("09:00 is free — the previous meeting ended at 09:00")
					.contains("2026-09-02T09:00");
		}

		@Test
		@DisplayName("buffers are treated as part of the meeting")
		void buffersBlockNeighbouringSlots() {
			/*
			 * A 10:00-11:00 meeting with fifteen minutes either side occupies
			 * 09:45 to 11:15, so the 09:30 slot (which would end at 10:30) and
			 * the 11:00 slot are both gone.
			 */
			List<Slot> slots = find(type(60, 0, 15, 15),
					List.of(rule(DayOfWeek.WEDNESDAY, "09:00", "13:00")),
					List.of(),
					List.of(new Busy(london("2026-09-02T10:00"), london("2026-09-02T11:00"))),
					"2026-09-02T00:00", "2026-09-03T00:00", "2026-09-01T09:00");

			assertThat(slots).extracting(s -> asLondon(s.startsAt()))
					.containsExactly("2026-09-02T11:30", "2026-09-02T12:00");
		}

		@Test
		@DisplayName("a meeting that began before the window still blocks inside it")
		void overlapIsNotContainment() {
			// Runs 08:30-09:30, so it started before the working day.
			List<Slot> slots = find(type(60, 0, 0, 0),
					List.of(rule(DayOfWeek.WEDNESDAY, "09:00", "11:00")),
					List.of(),
					List.of(new Busy(london("2026-09-02T08:30"), london("2026-09-02T09:30"))),
					"2026-09-02T00:00", "2026-09-03T00:00", "2026-09-01T09:00");

			assertThat(slots).extracting(s -> asLondon(s.startsAt()))
					.doesNotContain("2026-09-02T09:00")
					.contains("2026-09-02T10:00");
		}
	}

	@Nested
	@DisplayName("the clocks changing")
	class DaylightSaving {

		/*
		 * The two days a year when local time is not a fixed offset from UTC.
		 * A scheduler that adds a constant offset is wrong for half the year and
		 * wrong by an hour on these two days specifically — which is the kind of
		 * fault that shows up as one customer arriving at the wrong time and
		 * nobody being able to reproduce it.
		 */

		@Test
		@DisplayName("British Summer Time: 10:00 local is 09:00 UTC")
		void slotsAreCorrectDuringSummerTime() {
			// 2 September 2026 is BST, UTC+1.
			List<Slot> slots = find(type(60, 0, 0, 0),
					List.of(rule(DayOfWeek.WEDNESDAY, "10:00", "11:00")),
					List.of(), List.of(),
					"2026-09-02T00:00", "2026-09-03T00:00", "2026-09-01T09:00");

			assertThat(slots).hasSize(1);
			assertThat(slots.get(0).startsAt())
					.isEqualTo(Instant.parse("2026-09-02T09:00:00Z"));
		}

		@Test
		@DisplayName("Greenwich Mean Time: the same 10:00 local is 10:00 UTC")
		void slotsAreCorrectDuringWinter() {
			// 2 December 2026 is GMT, UTC+0 — same rule, different instant.
			List<Slot> slots = find(type(60, 0, 0, 0),
					List.of(rule(DayOfWeek.WEDNESDAY, "10:00", "11:00")),
					List.of(), List.of(),
					"2026-12-02T00:00", "2026-12-03T00:00", "2026-12-01T09:00");

			assertThat(slots).hasSize(1);
			assertThat(slots.get(0).startsAt())
					.as("the same local hour is a different instant in winter")
					.isEqualTo(Instant.parse("2026-12-02T10:00:00Z"));
		}

		@Test
		@DisplayName("the day the clocks go back has 25 hours and still works")
		void theLongDayIsHandled() {
			// 25 October 2026, a Sunday: 01:00 BST happens, then 01:00 GMT.
			List<Slot> slots = ConsultationSlotFinder.findSlots(
					type(60, 0, 0, 0),
					LONDON,
					List.of(rule(DayOfWeek.SUNDAY, "09:00", "12:00")),
					List.of(), List.of(),
					london("2026-10-25T00:00"), london("2026-10-26T00:00"),
					london("2026-10-01T09:00"), 60);

			// Working hours are after the change, so these are plain GMT.
			assertThat(slots).extracting(Slot::startsAt).containsExactly(
					Instant.parse("2026-10-25T09:00:00Z"),
					Instant.parse("2026-10-25T10:00:00Z"),
					Instant.parse("2026-10-25T11:00:00Z"));
		}

		@Test
		@DisplayName("the day the clocks go forward has 23 hours and skips the missing one")
		void theShortDayIsHandled() {
			// 29 March 2026: 01:00 GMT is followed by 03:00 BST. Nothing may be
			// offered in the hour that does not exist.
			List<Slot> slots = ConsultationSlotFinder.findSlots(
					type(60, 0, 0, 0),
					LONDON,
					List.of(rule(DayOfWeek.SUNDAY, "00:00", "05:00")),
					List.of(), List.of(),
					london("2026-03-29T00:00"), london("2026-03-30T00:00"),
					london("2026-03-01T09:00"), 60);

			/*
			 * Every offered slot must be an hour long in real time. Local
			 * arithmetic alone would produce a 02:00 start that either does not
			 * exist or silently becomes 03:00.
			 */
			assertThat(slots).isNotEmpty();
			assertThat(slots).allSatisfy(slot ->
					assertThat(Duration.between(slot.startsAt(), slot.endsAt()))
							.as("slot at %s is not one real hour", slot.startsAt())
							.isEqualTo(Duration.ofHours(1)));

			assertThat(slots).extracting(Slot::startsAt).doesNotHaveDuplicates();
		}
	}

	@Nested
	@DisplayName("degenerate input")
	class Degenerate {

		@Test
		@DisplayName("no rules means no slots, rather than an exception")
		void noRulesIsEmpty() {
			assertThat(ConsultationSlotFinder.findSlots(type(60, 0, 0, 0), LONDON,
					List.of(), List.of(), List.of(),
					london("2026-09-02T00:00"), london("2026-09-03T00:00"),
					london("2026-09-01T09:00"), 30)).isEmpty();
		}

		@Test
		@DisplayName("a window that ends before it starts is empty, not backwards")
		void invertedWindowIsEmpty() {
			assertThat(find(type(60, 0, 0, 0),
					List.of(rule(DayOfWeek.WEDNESDAY, "09:00", "17:00")),
					List.of(), List.of(),
					"2026-09-03T00:00", "2026-09-02T00:00", "2026-09-01T09:00")).isEmpty();
		}

		@Test
		@DisplayName("a meeting longer than the working day is never offered")
		void tooLongToFitIsEmpty() {
			assertThat(find(type(480, 0, 0, 0),
					List.of(rule(DayOfWeek.WEDNESDAY, "09:00", "12:00")),
					List.of(), List.of(),
					"2026-09-02T00:00", "2026-09-03T00:00", "2026-09-01T09:00")).isEmpty();
		}
	}
}
