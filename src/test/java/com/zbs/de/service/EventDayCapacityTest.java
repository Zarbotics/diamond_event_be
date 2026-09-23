package com.zbs.de.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * The capacity rule, on its own.
 *
 * <p>
 * No Spring and no database — the rule is arithmetic about a calendar, and the
 * awkward parts are the couplings between adjacent days rather than anything to
 * do with persistence. Those are far easier to get wrong than to test, so they
 * are tested exhaustively here and the rest of the system can rely on one
 * answer.
 */
class EventDayCapacityTest {

	/** A known Sunday, so the day-of-week reasoning is not guesswork. */
	private static final LocalDate SUNDAY = LocalDate.of(2026, 5, 3);
	private static final LocalDate MONDAY = SUNDAY.plusDays(1);
	private static final LocalDate FRIDAY = LocalDate.of(2026, 5, 1);

	@Nested
	@DisplayName("an ordinary day")
	class OrdinaryDays {

		@Test
		@DisplayName("holds two")
		void holdsTwo() {
			assertThat(EventDayCapacity.of(FRIDAY, Map.of())).isEqualTo(2);
		}

		@Test
		@DisplayName("is over capacity at three")
		void isOverAtThree() {
			// The state production is actually in, on this exact date.
			assertThat(EventDayCapacity.isOverCapacity(FRIDAY, Map.of(FRIDAY, 3))).isTrue();
			assertThat(EventDayCapacity.isOverCapacity(FRIDAY, Map.of(FRIDAY, 2))).isFalse();
		}
	}

	@Nested
	@DisplayName("a Sunday")
	class Sundays {

		@Test
		@DisplayName("holds three when the Monday after it is clear")
		void holdsThreeWithAFreeMonday() {
			assertThat(EventDayCapacity.of(SUNDAY, Map.of(SUNDAY, 2))).isEqualTo(3);
		}

		@Test
		@DisplayName("drops to two as soon as the Monday is used")
		void dropsToTwoWhenTheMondayIsUsed() {
			/*
			 * The third Sunday event runs late and the team need the Monday to
			 * break down. One booking on the Monday takes the third Sunday place
			 * away, which is the coupling most likely to be missed.
			 */
			assertThat(EventDayCapacity.of(SUNDAY, Map.of(SUNDAY, 2, MONDAY, 1))).isEqualTo(2);
		}

		@Test
		@DisplayName("with three booked and a Monday booking is over capacity")
		void threeAndABookedMondayIsOver() {
			// Each is legal alone; together they are not, and neither day looks
			// wrong on its own.
			assertThat(EventDayCapacity.isOverCapacity(SUNDAY, Map.of(SUNDAY, 3, MONDAY, 1))).isTrue();
			assertThat(EventDayCapacity.isOverCapacity(SUNDAY, Map.of(SUNDAY, 3))).isFalse();
		}
	}

	@Nested
	@DisplayName("a Monday")
	class Mondays {

		@Test
		@DisplayName("holds two when the Sunday before it is not full")
		void holdsTwoNormally() {
			assertThat(EventDayCapacity.of(MONDAY, Map.of(SUNDAY, 2))).isEqualTo(2);
		}

		@Test
		@DisplayName("holds nothing when the Sunday before it took a third")
		void isClosedAfterAFullSunday() {
			// The break-down day. Not "fewer" — none.
			assertThat(EventDayCapacity.of(MONDAY, Map.of(SUNDAY, 3))).isZero();
		}

		@Test
		@DisplayName("with anything booked after a full Sunday is over capacity")
		void anythingAfterAFullSundayIsOver() {
			assertThat(EventDayCapacity.isOverCapacity(MONDAY, Map.of(SUNDAY, 3, MONDAY, 1))).isTrue();
		}
	}

	@Test
	@DisplayName("an empty day is never over capacity")
	void anEmptyDayIsFine() {
		// Including the Monday after a full Sunday, whose capacity is zero — zero
		// events against a capacity of zero is not an exception to report.
		assertThat(EventDayCapacity.isOverCapacity(MONDAY, Map.of(SUNDAY, 3))).isFalse();
		assertThat(EventDayCapacity.isOverCapacity(FRIDAY, Map.of())).isFalse();
	}
}
