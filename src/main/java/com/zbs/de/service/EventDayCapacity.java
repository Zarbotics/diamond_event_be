package com.zbs.de.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;

/**
 * How many events one day can hold.
 *
 * <p>
 * The rule the business works to: two on an ordinary day, three on a Sunday
 * unless the Monday after it is used, and a Monday closed entirely when its
 * Sunday is full. Written once here so that anything reporting on capacity
 * agrees with anything enforcing it.
 *
 * <p>
 * {@code ServiceEventMasterImpl.canBookEvent} still carries its own copy, and
 * deliberately so for now: it has to reason about the event being edited — a
 * count that excludes it, an increment only when the date actually changes —
 * and folding that in would mean rewriting the check that guards the customer
 * journey in order to tidy a report. Recorded as B5.
 */
public final class EventDayCapacity {

	/** An ordinary day, and the ceiling on any day. */
	public static final int ORDINARY_DAY = 2;

	/** A Sunday with a free Monday after it. */
	public static final int QUIET_SUNDAY = 3;

	private EventDayCapacity() {
	}

	/**
	 * What this day can hold, given how busy the days around it are.
	 *
	 * @param counts how many events fall on each day, as
	 *               {@code getEventDateCounts} reports them. Days absent from the
	 *               map have none.
	 */
	public static int of(LocalDate day, Map<LocalDate, Integer> counts) {
		DayOfWeek dayOfWeek = day.getDayOfWeek();

		if (dayOfWeek == DayOfWeek.SUNDAY) {
			/*
			 * A Sunday can take a third only when the Monday after it is clear.
			 * The third event runs late and the team need the Monday to break
			 * down and reset, so a Monday booking and a third Sunday booking
			 * cannot both exist.
			 */
			int mondayAfter = counts.getOrDefault(day.plusDays(1), 0);
			return mondayAfter > 0 ? ORDINARY_DAY : QUIET_SUNDAY;
		}

		if (dayOfWeek == DayOfWeek.MONDAY && counts.getOrDefault(day.minusDays(1), 0) >= QUIET_SUNDAY) {
			// The Sunday before took its third, so this Monday is the break-down
			// day and cannot hold anything.
			return 0;
		}

		return ORDINARY_DAY;
	}

	/** Whether this day holds more than the rule allows. */
	public static boolean isOverCapacity(LocalDate day, Map<LocalDate, Integer> counts) {
		return counts.getOrDefault(day, 0) > of(day, counts);
	}
}
