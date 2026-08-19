package com.zbs.de.service.calendar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zbs.de.model.CalendarConnection;
import com.zbs.de.model.ConsultationBooking;
import com.zbs.de.service.calendar.CalendarProvider.BusyPeriod;
import com.zbs.de.service.calendar.CalendarProvider.ExternalEvent;

/**
 * What the Google and Microsoft adapters send, and what they make of the answer.
 *
 * <p>
 * Neither provider can be reached from here — that needs real credentials, an
 * OAuth consent screen and a person to click through it — so what is tested is
 * everything on this side of the socket: the request that would go out, and the
 * reading of a response shaped like theirs.
 *
 * <p>
 * That is not a token gesture. Most of what goes wrong with these integrations
 * is here rather than in the network: a missing query parameter that silently
 * produces no meeting link, a time sent without its zone so the provider
 * interprets it in the mailbox's own, a rotated refresh token thrown away, an
 * error reported inside a 200 response and read as "free all week". Each of
 * those is a real failure of this kind, and each one is checkable without a
 * network.
 */
class CalendarProviderRequestTest {

	private static final ObjectMapper JSON = new ObjectMapper();

	private static final String KEY = Base64.getEncoder()
			.encodeToString("a-32-byte-key-for-testing-only!!".getBytes());

	/** One request the adapter tried to make. */
	record Call(String url, String body) {
	}

	/**
	 * Stands in for the network.
	 *
	 * <p>
	 * Records what was asked for and replies with whatever the test queued.
	 */
	static class StubHttp extends CalendarHttp {

		final List<Call> calls = new ArrayList<>();
		final List<String> deleted = new ArrayList<>();
		final List<String> responses = new ArrayList<>();

		void willReply(String json) {
			responses.add(json);
		}

		private JsonNode reply(String url, String body) {
			calls.add(new Call(url, body));
			try {
				return JSON.readTree(responses.isEmpty() ? "{}" : responses.remove(0));
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}

		@Override
		public JsonNode postJson(String url, String bearerToken, String body) {
			return reply(url, body);
		}

		@Override
		public JsonNode postForm(String url, String bearerToken, String body) {
			return reply(url, body);
		}

		@Override
		public JsonNode getJson(String url, String bearerToken) {
			return reply(url, null);
		}

		@Override
		public void delete(String url, String bearerToken) {
			deleted.add(url);
		}

		/** The last non-token call — the one the test is usually about. */
		Call lastCall() {
			return calls.get(calls.size() - 1);
		}
	}

	/** readTree(String) is checked, and a malformed body here is a test bug. */
	private static JsonNode parse(String json) {
		try {
			return JSON.readTree(json);
		} catch (Exception e) {
			throw new IllegalStateException("Test JSON did not parse", e);
		}
	}

	private StubHttp http;
	private CalendarTokenCipher cipher;

	@BeforeEach
	void setUp() {
		http = new StubHttp();
		cipher = new CalendarTokenCipher();
		ReflectionTestUtils.setField(cipher, "configuredKey", KEY);
	}

	/** A connection with a live access token, so no refresh is attempted. */
	private CalendarConnection connection(String provider) {
		CalendarConnection connection = new CalendarConnection();
		connection.setSerCalendarConnectionId(1);
		connection.setSerHostId(1);
		connection.setTxtProvider(provider);
		connection.setTxtAccountEmail("host@example.com");
		connection.setTxtAccessTokenEncrypted(cipher.encrypt("live-access-token"));
		connection.setTxtRefreshTokenEncrypted(cipher.encrypt("a-refresh-token"));
		connection.setDteTokenExpiresAt(Instant.now().plusSeconds(3600));
		return connection;
	}

	private ConsultationBooking booking() {
		ConsultationBooking booking = new ConsultationBooking();
		booking.setSerConsultationBookingId(77);
		booking.setSerHostId(1);
		booking.setTxtCustomerName("Sara Ahmed");
		booking.setTxtCustomerEmail("sara@example.com");
		booking.setTxtCustomerTimeZone("Asia/Dubai");
		booking.setDteStartsAt(Instant.parse("2026-09-07T09:00:00Z"));
		booking.setDteEndsAt(Instant.parse("2026-09-07T10:00:00Z"));
		return booking;
	}

	// =================================================================

	@Nested
	@DisplayName("Google")
	class Google {

		private GoogleCalendarProvider google;

		@BeforeEach
		void setUp() {
			google = new GoogleCalendarProvider();
			ReflectionTestUtils.setField(google, "http", http);
			ReflectionTestUtils.setField(google, "cipher", cipher);
			ReflectionTestUtils.setField(google, "clientId", "test-client-id");
			ReflectionTestUtils.setField(google, "clientSecret", "test-client-secret");
		}

		@Test
		@DisplayName("busy periods are read out of a freeBusy answer")
		void busyPeriodsAreRead() {
			http.willReply("""
					{ "calendars": { "primary": { "busy": [
					  { "start": "2026-09-07T09:00:00Z", "end": "2026-09-07T10:00:00Z" },
					  { "start": "2026-09-07T14:00:00Z", "end": "2026-09-07T15:30:00Z" }
					] } } }
					""");

			List<BusyPeriod> busy = google.busyPeriods(connection(CalendarConnection.GOOGLE),
					Instant.parse("2026-09-07T00:00:00Z"), Instant.parse("2026-09-08T00:00:00Z"));

			assertThat(busy).hasSize(2);
			assertThat(busy.get(0).startsAt()).isEqualTo(Instant.parse("2026-09-07T09:00:00Z"));
			assertThat(busy.get(1).endsAt()).isEqualTo(Instant.parse("2026-09-07T15:30:00Z"));
		}

		@Test
		@DisplayName("an error reported inside a 200 is not read as 'free all week'")
		void perCalendarErrorsAreNotSilentlyFree() {
			/*
			 * Google reports a calendar that was deleted or unshared as an
			 * "errors" array with an HTTP 200. Taking the empty busy list at face
			 * value would mark the host free for the whole window and start
			 * offering times they are committed to — the exact failure that
			 * double-books somebody.
			 */
			http.willReply("""
					{ "calendars": { "primary": {
					  "busy": [],
					  "errors": [ { "domain": "global", "reason": "notFound" } ]
					} } }
					""");

			assertThatThrownBy(() -> google.busyPeriods(connection(CalendarConnection.GOOGLE),
					Instant.parse("2026-09-07T00:00:00Z"), Instant.parse("2026-09-08T00:00:00Z")))
					.isInstanceOf(CalendarHttp.CalendarApiException.class)
					.hasMessageContaining("notFound");
		}

		@Test
		@DisplayName("asking for a Meet link sets conferenceDataVersion, or Google ignores it")
		void aMeetLinkNeedsTheQueryParameter() {
			/*
			 * Without conferenceDataVersion=1 the call succeeds, the event
			 * appears, and there is simply no Meet link. A silent no-op that
			 * looks like Google ignoring the request.
			 */
			http.willReply("""
					{ "id": "abc123", "hangoutLink": "https://meet.google.com/xyz-abcd-efg" }
					""");

			ExternalEvent event = google.createEvent(connection(CalendarConnection.GOOGLE),
					booking(), true);

			assertThat(http.lastCall().url()).contains("conferenceDataVersion=1");
			assertThat(http.lastCall().body()).contains("hangoutsMeet");
			assertThat(event.joinUrl()).isEqualTo("https://meet.google.com/xyz-abcd-efg");
			assertThat(event.externalEventId()).isEqualTo("abc123");
		}

		@Test
		@DisplayName("not asking for one sends no conference block at all")
		void noVideoLinkMeansNoConferenceRequest() {
			http.willReply("""
					{ "id": "abc123" }
					""");

			ExternalEvent event = google.createEvent(connection(CalendarConnection.GOOGLE),
					booking(), false);

			assertThat(http.lastCall().url()).doesNotContain("conferenceDataVersion");
			assertThat(http.lastCall().body()).doesNotContain("conferenceData");
			assertThat(event.joinUrl()).isNull();
		}

		@Test
		@DisplayName("a link under conferenceData is found as well as hangoutLink")
		void theNewerLinkShapeIsRead() {
			// Newer responses carry it under conferenceData.entryPoints instead.
			http.willReply("""
					{ "id": "abc123", "conferenceData": { "entryPoints": [
					  { "entryPointType": "more", "uri": "https://meet.google.com/tel" },
					  { "entryPointType": "video", "uri": "https://meet.google.com/xyz-abcd-efg" }
					] } }
					""");

			assertThat(google.createEvent(connection(CalendarConnection.GOOGLE), booking(), true)
					.joinUrl())
					.isEqualTo("https://meet.google.com/xyz-abcd-efg");
		}

		@Test
		@DisplayName("a name with a quote in it does not break the request body")
		void namesAreEscaped() {
			// O'Brien, and anybody who types a double quote into a name field.
			// Hand-built JSON makes this a real risk rather than a theoretical one.
			http.willReply("""
					{ "id": "abc123" }
					""");
			ConsultationBooking awkward = booking();
			awkward.setTxtCustomerName("Sara \"Sal\" O'Brien");

			google.createEvent(connection(CalendarConnection.GOOGLE), awkward, false);

			assertThat(parse(http.lastCall().body()).path("summary").asText())
					.contains("Sara \"Sal\" O'Brien");
		}

		@Test
		@DisplayName("an expired access token is refreshed before the call")
		void anExpiredTokenIsRefreshed() {
			CalendarConnection expired = connection(CalendarConnection.GOOGLE);
			expired.setDteTokenExpiresAt(Instant.now().minusSeconds(60));

			http.willReply("""
					{ "access_token": "a-fresh-token", "expires_in": 3599 }
					""");
			http.willReply("""
					{ "calendars": { "primary": { "busy": [] } } }
					""");

			google.busyPeriods(expired, Instant.parse("2026-09-07T00:00:00Z"),
					Instant.parse("2026-09-08T00:00:00Z"));

			assertThat(http.calls.get(0).url()).contains("oauth2.googleapis.com/token");
			assertThat(http.calls.get(0).body()).contains("grant_type=refresh_token");
			// And the new one is kept, encrypted, so the next call does not refresh again.
			assertThat(cipher.decrypt(expired.getTxtAccessTokenEncrypted())).isEqualTo("a-fresh-token");
			assertThat(expired.getDteTokenExpiresAt()).isAfter(Instant.now());
		}

		@Test
		@DisplayName("a calendar id that is an email address is escaped into the path")
		void calendarIdsAreEscaped() {
			// Calendar ids are email addresses often enough that this matters: an
			// unescaped @ or + is a different URL from the one intended.
			http.willReply("{ \"id\": \"abc123\" }");
			CalendarConnection named = connection(CalendarConnection.GOOGLE);
			named.setTxtCalendarId("team+events@example.com");

			google.createEvent(named, booking(), false);

			assertThat(http.lastCall().url()).doesNotContain("team+events@example.com");
			assertThat(http.lastCall().url()).contains("%40");
		}
	}

	// =================================================================

	@Nested
	@DisplayName("Microsoft")
	class Microsoft {

		private MicrosoftCalendarProvider microsoft;

		@BeforeEach
		void setUp() {
			microsoft = new MicrosoftCalendarProvider();
			ReflectionTestUtils.setField(microsoft, "http", http);
			ReflectionTestUtils.setField(microsoft, "cipher", cipher);
			ReflectionTestUtils.setField(microsoft, "clientId", "test-client-id");
			ReflectionTestUtils.setField(microsoft, "clientSecret", "test-client-secret");
			ReflectionTestUtils.setField(microsoft, "tenant", "common");
		}

		@Test
		@DisplayName("the availability string becomes periods, with runs merged")
		void availabilityIsDecoded() {
			/*
			 * Graph answers with one character per five minutes rather than a
			 * list of periods. Twelve free, twelve busy, twelve free: a one-hour
			 * meeting from 10:00, as one period rather than twelve.
			 */
			Instant windowStart = Instant.parse("2026-09-07T09:00:00Z");
			List<BusyPeriod> busy = MicrosoftCalendarProvider.decodeAvailability(
					"000000000000222222222222000000000000", windowStart);

			assertThat(busy).hasSize(1);
			assertThat(busy.get(0).startsAt()).isEqualTo(Instant.parse("2026-09-07T10:00:00Z"));
			assertThat(busy.get(0).endsAt()).isEqualTo(Instant.parse("2026-09-07T11:00:00Z"));
		}

		@Test
		@DisplayName("tentative counts as busy")
		void tentativeBlocksTheSlot() {
			// '1' is tentative. Somebody with a tentative hold has a claim on
			// that time, and offering it produces a clash they have to sort out.
			assertThat(MicrosoftCalendarProvider.decodeAvailability(
					"111", Instant.parse("2026-09-07T09:00:00Z"))).hasSize(1);
		}

		@Test
		@DisplayName("busy right to the end of the window closes at the window's end")
		void aRunAtTheEndIsClosed() {
			// The off-by-one that drops the last meeting of the window entirely.
			Instant windowStart = Instant.parse("2026-09-07T09:00:00Z");

			List<BusyPeriod> busy = MicrosoftCalendarProvider.decodeAvailability("000222", windowStart);

			assertThat(busy).hasSize(1);
			assertThat(busy.get(0).endsAt()).isEqualTo(Instant.parse("2026-09-07T09:30:00Z"));
		}

		@Test
		@DisplayName("all free is no busy periods, and all busy is one")
		void theEdgesHold() {
			Instant start = Instant.parse("2026-09-07T09:00:00Z");

			assertThat(MicrosoftCalendarProvider.decodeAvailability("0000", start)).isEmpty();
			assertThat(MicrosoftCalendarProvider.decodeAvailability("2222", start)).hasSize(1);
			assertThat(MicrosoftCalendarProvider.decodeAvailability("", start)).isEmpty();
		}

		@Test
		@DisplayName("times are sent with their zone named, or Graph reads them as local")
		void timesCarryTheirZone() {
			/*
			 * A dateTime without a timeZone is interpreted in the mailbox's own
			 * zone, which moves the meeting by that offset — silently, so it
			 * looks like the booking system had the wrong time rather than the
			 * calendar call having lost one.
			 */
			http.willReply("{ \"id\": \"AAMk123\" }");

			microsoft.createEvent(connection(CalendarConnection.MICROSOFT), booking(), false);

			JsonNode body = parse(http.lastCall().body());
			assertThat(body.path("start").path("timeZone").asText()).isEqualTo("UTC");
			assertThat(body.path("start").path("dateTime").asText()).isEqualTo("2026-09-07T09:00:00");
			assertThat(body.path("end").path("timeZone").asText()).isEqualTo("UTC");
		}

		@Test
		@DisplayName("a Teams link is asked for and read back")
		void teamsLinksAreRequested() {
			http.willReply("""
					{ "id": "AAMk123", "onlineMeeting": {
					  "joinUrl": "https://teams.microsoft.com/l/meetup-join/xyz" } }
					""");

			ExternalEvent event = microsoft.createEvent(
					connection(CalendarConnection.MICROSOFT), booking(), true);

			assertThat(parse(http.lastCall().body()).path("isOnlineMeeting").asBoolean())
					.isTrue();
			assertThat(event.joinUrl()).contains("teams.microsoft.com");
		}

		@Test
		@DisplayName("a rotated refresh token is kept, or every connection breaks at once later")
		void rotatedRefreshTokensAreKept() {
			/*
			 * Microsoft rotates refresh tokens: each refresh may return a new one
			 * and the old one stops working. Discarding it is the classic Graph
			 * bug — everything works for a fortnight, and then every connection
			 * fails together when the original finally expires, long after the
			 * change that caused it.
			 */
			CalendarConnection expired = connection(CalendarConnection.MICROSOFT);
			expired.setDteTokenExpiresAt(Instant.now().minusSeconds(60));

			http.willReply("""
					{ "access_token": "fresh", "refresh_token": "a-rotated-refresh-token",
					  "expires_in": 3599 }
					""");
			http.willReply("""
					{ "value": [ { "availabilityView": "0000" } ] }
					""");

			microsoft.busyPeriods(expired, Instant.parse("2026-09-07T09:00:00Z"),
					Instant.parse("2026-09-07T09:20:00Z"));

			assertThat(cipher.decrypt(expired.getTxtRefreshTokenEncrypted()))
					.isEqualTo("a-rotated-refresh-token");
		}

		@Test
		@DisplayName("a refresh that returns no new refresh token keeps the old one")
		void anAbsentRotationKeepsTheExistingToken() {
			// Microsoft does not always rotate. Blanking the stored token when it
			// does not would break the connection just as thoroughly.
			CalendarConnection expired = connection(CalendarConnection.MICROSOFT);
			expired.setDteTokenExpiresAt(Instant.now().minusSeconds(60));

			http.willReply("{ \"access_token\": \"fresh\", \"expires_in\": 3599 }");
			http.willReply("{ \"value\": [ { \"availabilityView\": \"0000\" } ] }");

			microsoft.busyPeriods(expired, Instant.parse("2026-09-07T09:00:00Z"),
					Instant.parse("2026-09-07T09:20:00Z"));

			assertThat(cipher.decrypt(expired.getTxtRefreshTokenEncrypted()))
					.isEqualTo("a-refresh-token");
		}

		@Test
		@DisplayName("no schedule for the account is an error, not an empty diary")
		void anAbsentScheduleIsNotEmptiness() {
			http.willReply("{ \"value\": [] }");

			assertThatThrownBy(() -> microsoft.busyPeriods(connection(CalendarConnection.MICROSOFT),
					Instant.parse("2026-09-07T09:00:00Z"), Instant.parse("2026-09-08T00:00:00Z")))
					.isInstanceOf(CalendarHttp.CalendarApiException.class);
		}
	}
}
