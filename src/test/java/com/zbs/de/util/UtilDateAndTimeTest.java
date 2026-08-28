package com.zbs.de.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Reading a date out of a request.
 *
 * <h2>What went wrong</h2>
 *
 * {@link java.text.SimpleDateFormat} is lenient unless told otherwise, and every
 * parser in {@code UtilDateAndTime} took the default. Lenient does not mean
 * "forgiving about whitespace"; it means the fields are added up as numbers and
 * whatever day they land on is returned. Asked for {@code dd-MM-yyyy}, it read
 * {@code "2033-08-01"} as day 2033 of month 8 of year 1 and handed back
 * <em>Wednesday 23 February in the year 7</em>.
 *
 * <p>
 * Nothing downstream can tell that from a day a customer chose. It is a real
 * {@link Date}: the capacity rule counts it, the price list effective on it is
 * looked up, the report prints it.
 *
 * <h2>Why the tests are shaped this way</h2>
 *
 * Two halves. The first is that a valid date still parses — the whole change is
 * worthless if it narrows what staff are able to type. The second is the two
 * ways leniency invents a day: a string in the wrong format, and a date that is
 * written correctly but does not exist.
 */
class UtilDateAndTimeTest {

	private static Date theDay(int year, int month, int day) {
		return Date.from(LocalDate.of(year, month, day).atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	@Nested
	@DisplayName("dd-MM-yyyy, the format both frontends send")
	class DashedDates {

		@Test
		@DisplayName("a date staff actually type is read as that day")
		void anOrdinaryDateStillParses() {
			assertThat(UtilDateAndTime.ddMMyyyyDashedStringToDate("01-08-2033"))
					.as("the format the admin portal and the journey both send no longer parses")
					.isEqualTo(theDay(2033, 8, 1));
		}

		@Test
		@DisplayName("an ISO date is refused rather than read as the year 7")
		void theWrongFormatIsRefused() {
			/*
			 * The one that started this. Leniently, "2033-08-01" is day 2033 of
			 * month 8 of year 1 — 23 February in the year 7 — and it saved, and
			 * it reported success.
			 */
			assertThat(UtilDateAndTime.ddMMyyyyDashedStringToDate("2033-08-01"))
					.as("a date in the wrong format was turned into a real day nearly two thousand years ago")
					.isNull();
		}

		@Test
		@DisplayName("a day that does not exist is refused rather than rolled forward")
		void theThirtyFirstOfFebruaryIsRefused() {
			/*
			 * Quieter than the above and likelier: leniency reads the 31st of
			 * February as the 3rd of March, so a typo becomes an event booked
			 * three days out with nothing anywhere saying so.
			 */
			assertThat(UtilDateAndTime.ddMMyyyyDashedStringToDate("31-02-2026"))
					.as("the 31st of February was silently rolled forward into March")
					.isNull();
		}

		@Test
		@DisplayName("a date with something after it is refused")
		void trailingRubbishIsRefused() {
			// parse() stops the moment the pattern is satisfied, so without an
			// explicit check this is read as the first of August.
			assertThat(UtilDateAndTime.ddMMyyyyDashedStringToDate("01-08-2033 or thereabouts"))
					.as("a string that only begins with a date was accepted as that date")
					.isNull();
		}

		@Test
		@DisplayName("surrounding space is not the customer's fault")
		void spaceIsTrimmed() {
			assertThat(UtilDateAndTime.ddMMyyyyDashedStringToDate("  01-08-2033 "))
					.isEqualTo(theDay(2033, 8, 1));
		}

		@Test
		@DisplayName("no date at all is not an error")
		void absentIsNull() {
			// The journey creates an event before the customer has picked a day.
			assertThat(UtilDateAndTime.ddMMyyyyDashedStringToDate(null)).isNull();
			assertThat(UtilDateAndTime.ddMMyyyyDashedStringToDate("   ")).isNull();
		}

		@Test
		@DisplayName("a search keyword is not a date, and that is fine")
		void aKeywordIsNotADate() {
			// The events search box hands its keyword to this parser to see
			// whether the user typed a date. Usually they typed a name.
			assertThat(UtilDateAndTime.ddMMyyyyDashedStringToDate("Khan")).isNull();
		}
	}

	@Nested
	@DisplayName("the parser the event save paths share")
	class ClientDates {

		@Test
		@DisplayName("the three formats a client may send all mean the same day")
		void everyAcceptedFormatAgrees() {
			/*
			 * The fault this parser exists to end. There are four ways to save an
			 * event and they did not agree on what a date looks like: three read
			 * dashes, and saveOrUpdate read slashes falling back to ISO. Both
			 * frontends send dashes, so the same payload meant one day at three
			 * endpoints and the seventh century at the fourth.
			 */
			Date expected = theDay(2033, 8, 1);

			assertThat(UtilDateAndTime.parseDateFromClient("01-08-2033")).isEqualTo(expected);
			assertThat(UtilDateAndTime.parseDateFromClient("01/08/2033")).isEqualTo(expected);
			assertThat(UtilDateAndTime.parseDateFromClient("2033-08-01")).isEqualTo(expected);
		}

		@Test
		@DisplayName("a four-digit year is never mistaken for a day of the month")
		void theFormatsCannotBeConfusedWithOneAnother() {
			// What makes accepting three formats safe rather than ambiguous. If
			// yyyy-MM-dd could also read as dd-MM-yyyy, the order of the list
			// would silently decide what day a customer gets.
			assertThat(UtilDateAndTime.parseDateFromClient("2026-03-04"))
					.as("an ISO date was read as the 2026th of March")
					.isEqualTo(theDay(2026, 3, 4));
		}

		@Test
		@DisplayName("something that is not a date in any of them is refused")
		void rubbishIsRefused() {
			assertThat(UtilDateAndTime.parseDateFromClient("next Saturday")).isNull();
			assertThat(UtilDateAndTime.parseDateFromClient("01-13-2033")).isNull();
			assertThat(UtilDateAndTime.parseDateFromClient("29-02-2027")).isNull();
		}

		@Test
		@DisplayName("a leap day in a leap year is a real day")
		void leapDaysAreKept() {
			// The counterweight to the test above: strictness must refuse the
			// 29th of February in a common year without refusing it in a leap one.
			assertThat(UtilDateAndTime.parseDateFromClient("29-02-2028"))
					.isEqualTo(theDay(2028, 2, 29));
		}

		@Test
		@DisplayName("no date given is no date, not a refusal")
		void absentIsNull() {
			assertThat(UtilDateAndTime.parseDateFromClient(null)).isNull();
			assertThat(UtilDateAndTime.parseDateFromClient("")).isNull();
		}
	}

	@Nested
	@DisplayName("the older parsers, which the same leniency applied to")
	class TheOtherInboundParsers {

		@Test
		@DisplayName("dd/MM/yyyy still parses, and still falls back to ISO")
		void slashedDatesKeepTheirFallback() {
			assertThat(UtilDateAndTime.ddmmyyyyStringToDate("01/08/2033")).isEqualTo(theDay(2033, 8, 1));
			assertThat(UtilDateAndTime.ddmmyyyyStringToDate("2033-08-01")).isEqualTo(theDay(2033, 8, 1));
		}

		@Test
		@DisplayName("dd/MM/yyyy no longer invents a day out of an impossible one")
		void slashedDatesAreStrict() {
			assertThat(UtilDateAndTime.ddmmyyyyStringToDate("32/08/2033"))
					.as("the 32nd of August was rolled forward into September")
					.isNull();
		}

		@Test
		@DisplayName("yyyy-MM-dd no longer invents a day out of an impossible one")
		void isoDatesAreStrict() {
			assertThat(UtilDateAndTime.yyyymmddStringToDate("2026-02-31"))
					.as("the 31st of February was rolled forward into March")
					.isNull();
			assertThat(UtilDateAndTime.yyyymmddStringToDate("2026-02-28"))
					.isEqualTo(theDay(2026, 2, 28));
		}
	}
}
