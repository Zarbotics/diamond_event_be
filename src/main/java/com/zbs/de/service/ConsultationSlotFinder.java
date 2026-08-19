package com.zbs.de.service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.zbs.de.model.ConsultationAvailabilityException;
import com.zbs.de.model.ConsultationAvailabilityRule;
import com.zbs.de.model.ConsultationType;

/**
 * Works out which slots a host can actually offer.
 *
 * <p>
 * Deliberately has no Spring, no repositories and no clock of its own —
 * everything it needs is passed in. This is where every awkward part of
 * scheduling lives, so it is the part that most needs to be testable without a
 * database, at any date, in any time zone, including the two days a year when
 * local time misbehaves.
 *
 * <h2>How a slot survives</h2>
 *
 * A candidate slot has to clear all of:
 *
 * <ol>
 * <li>inside the host's working hours for that day, in the host's own zone;</li>
 * <li>not on a day an exception closes, and inside the hours an exception
 * opens;</li>
 * <li>far enough ahead to satisfy the minimum notice, and not beyond the
 * maximum advance;</li>
 * <li>not overlapping a live booking or an imported busy period, counting the
 * buffers either side as part of the meeting.</li>
 * </ol>
 *
 * <h2>Why the arithmetic is done in the host's zone</h2>
 *
 * "Nine to five" is a statement about a local clock. In the UK the offset from
 * UTC changes twice a year, so those hours are 09:00–17:00Z for part of the
 * year and 08:00–16:00Z for the rest. Generating candidates in local time and
 * converting each one to an instant gets that right without knowing it is
 * happening; generating them in UTC and adding a fixed offset is wrong for half
 * the year.
 */
public final class ConsultationSlotFinder {

	/**
	 * A period that cannot be booked over — an existing meeting, or something in
	 * the host's own calendar.
	 */
	public record Busy(Instant startsAt, Instant endsAt) {

		public boolean overlaps(Instant otherStart, Instant otherEnd) {
			// Half-open: touching at the boundary is not overlapping, so a
			// meeting may begin exactly when the previous one ends.
			return startsAt.isBefore(otherEnd) && endsAt.isAfter(otherStart);
		}
	}

	/** One offerable start time. */
	public record Slot(Instant startsAt, Instant endsAt) {
	}

	private ConsultationSlotFinder() {
	}

	/**
	 * Every slot the host can offer between {@code from} and {@code to}.
	 *
	 * @param now       the current instant — passed rather than read, so tests
	 *                  can sit on a specific date rather than hoping today is a
	 *                  convenient one
	 * @param stepMinutes how far apart candidate start times are placed. Thirty
	 *                  gives half-hour starts for an hour-long meeting, which is
	 *                  what people expect to be offered.
	 */
	public static List<Slot> findSlots(
			ConsultationType type,
			ZoneId hostZone,
			List<ConsultationAvailabilityRule> rules,
			List<ConsultationAvailabilityException> exceptions,
			List<Busy> busy,
			Instant from,
			Instant to,
			Instant now,
			int stepMinutes) {

		if (type == null || rules == null || rules.isEmpty()) {
			return List.of();
		}

		Duration meeting = Duration.ofMinutes(type.getNumDurationMinutes());
		Duration bufferBefore = Duration.ofMinutes(orZero(type.getNumBufferBeforeMinutes()));
		Duration bufferAfter = Duration.ofMinutes(orZero(type.getNumBufferAfterMinutes()));

		// The window the customer may book in, independent of the window asked
		// for. Asking for next year does not make next year bookable.
		Instant earliest = now.plus(Duration.ofHours(orZero(type.getNumMinimumNoticeHours())));
		Instant latest = now.plus(Duration.ofDays(orDefault(type.getNumMaximumAdvanceDays(), 90)));

		Instant windowStart = from.isAfter(earliest) ? from : earliest;
		Instant windowEnd = to.isBefore(latest) ? to : latest;
		if (!windowStart.isBefore(windowEnd)) {
			return List.of();
		}

		Map<Integer, List<ConsultationAvailabilityRule>> rulesByDay = rules.stream()
				.filter(r -> !Boolean.TRUE.equals(r.getBlnIsDeleted()))
				.collect(Collectors.groupingBy(ConsultationAvailabilityRule::getNumDayOfWeek));

		Map<LocalDate, List<ConsultationAvailabilityException>> exceptionsByDate =
				exceptions == null ? Map.of()
						: exceptions.stream()
								.filter(e -> !Boolean.TRUE.equals(e.getBlnIsDeleted()))
								.collect(Collectors.groupingBy(ConsultationAvailabilityException::getDteOnDate));

		List<Slot> slots = new ArrayList<>();

		/*
		 * Walk local dates rather than instants. A day in the host's zone is not
		 * always 24 hours long — in March one is 23 and in October one is 25 —
		 * and stepping by a fixed duration would drift across those.
		 */
		LocalDate day = windowStart.atZone(hostZone).toLocalDate();
		LocalDate lastDay = windowEnd.atZone(hostZone).toLocalDate();

		while (!day.isAfter(lastDay)) {
			for (LocalTime[] hours : openHoursFor(day, rulesByDay, exceptionsByDate)) {
				slots.addAll(slotsWithin(day, hours[0], hours[1], hostZone, meeting,
						bufferBefore, bufferAfter, busy, windowStart, windowEnd, stepMinutes));
			}
			day = day.plusDays(1);
		}

		slots.sort(Comparator.comparing(Slot::startsAt));
		return slots;
	}

	/**
	 * The open periods on one local date: the weekly rules, unless an exception
	 * has something to say.
	 */
	private static List<LocalTime[]> openHoursFor(
			LocalDate day,
			Map<Integer, List<ConsultationAvailabilityRule>> rulesByDay,
			Map<LocalDate, List<ConsultationAvailabilityException>> exceptionsByDate) {

		List<ConsultationAvailabilityException> onThisDay =
				exceptionsByDate.getOrDefault(day, List.of());

		// A closure wins outright. Somebody who marked a day off should not find
		// meetings on it because a weekly rule also covered it.
		boolean closed = onThisDay.stream().anyMatch(e -> !Boolean.TRUE.equals(e.getBlnIsAvailable()));
		if (closed) {
			return List.of();
		}

		List<ConsultationAvailabilityException> openings = onThisDay.stream()
				.filter(e -> Boolean.TRUE.equals(e.getBlnIsAvailable()))
				.toList();

		// An opening replaces the weekly pattern for that day rather than adding
		// to it: "this Saturday, 10 till 2" means those hours, not those hours
		// plus whatever Saturday normally is.
		if (!openings.isEmpty()) {
			return openings.stream()
					.map(e -> new LocalTime[] { e.getTmeStartTime(), e.getTmeEndTime() })
					.toList();
		}

		return rulesByDay.getOrDefault(day.getDayOfWeek().getValue(), List.of()).stream()
				.map(r -> new LocalTime[] { r.getTmeStartTime(), r.getTmeEndTime() })
				.toList();
	}

	/** Candidate starts inside one open period, keeping the ones that survive. */
	private static List<Slot> slotsWithin(
			LocalDate day, LocalTime openFrom, LocalTime openTo, ZoneId hostZone,
			Duration meeting, Duration bufferBefore, Duration bufferAfter,
			List<Busy> busy, Instant windowStart, Instant windowEnd, int stepMinutes) {

		List<Slot> found = new ArrayList<>();

		ZonedDateTime opens = day.atTime(openFrom).atZone(hostZone);
		ZonedDateTime closes = day.atTime(openTo).atZone(hostZone);

		Duration step = Duration.ofMinutes(stepMinutes > 0 ? stepMinutes : 30);

		for (ZonedDateTime start = opens; !start.plus(meeting).isAfter(closes); start = start.plus(step)) {
			Instant startsAt = start.toInstant();
			Instant endsAt = start.plus(meeting).toInstant();

			if (startsAt.isBefore(windowStart) || !startsAt.isBefore(windowEnd)) {
				continue;
			}

			/*
			 * The buffers are what has to be free, not just the meeting. A slot
			 * that ends when another begins is fine on paper and useless in
			 * practice if the host needs ten minutes between calls.
			 */
			Instant blockedFrom = startsAt.minus(bufferBefore);
			Instant blockedTo = endsAt.plus(bufferAfter);

			boolean clashes = busy != null
					&& busy.stream().anyMatch(b -> b.overlaps(blockedFrom, blockedTo));

			if (!clashes) {
				found.add(new Slot(startsAt, endsAt));
			}
		}
		return found;
	}

	private static int orZero(Integer value) {
		return value == null ? 0 : value;
	}

	private static int orDefault(Integer value, int fallback) {
		return value == null ? fallback : value;
	}
}
