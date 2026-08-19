package com.zbs.de.service.calendar;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.zbs.de.model.CalendarConnection;
import com.zbs.de.model.ConsultationBooking;

/**
 * Google Calendar.
 *
 * <p>
 * Two calls do the whole job, and both are deliberately the narrow ones:
 *
 * <ul>
 * <li>{@code freeBusy.query} — when is this person busy? It answers with
 * periods and nothing else, so the team's own meetings never enter this
 * database. There is nothing sensitive here to leak because nothing sensitive
 * was ever asked for.</li>
 * <li>{@code events.insert} — put the consultation in, optionally asking Google
 * to attach a Meet link.</li>
 * </ul>
 *
 * <h2>Scopes</h2>
 *
 * {@code calendar.freebusy} to read and {@code calendar.app.created} to write.
 * The second is worth the specific mention: Google added it so that a
 * scheduling application need not ask for access to everything in somebody's
 * calendar. It creates a calendar this application owns and can only touch what
 * it put there — which is both the honest permission to ask for and the one
 * that keeps this out of the heavier verification review the broad
 * {@code calendar} scope now attracts.
 *
 * <h2>What is not here</h2>
 *
 * No Google SDK. These are four REST calls, and the SDK brings a large
 * dependency tree, its own HTTP stack and its own auth abstractions for very
 * little. Java's own {@code HttpClient} is enough and adds nothing to the build.
 *
 * <h2>Only a bean when it is configured</h2>
 *
 * Without a client id this adapter cannot do anything — every call needs
 * credentials, and nobody can have connected a calendar in the first place.
 * Registering it anyway would put a provider in the list that fails on use
 * rather than being absent, and absent is a state the caller already handles
 * correctly: it leaves the booking alone and records that there was no calendar
 * to write to.
 *
 * <p>
 * It also keeps development honest. With nothing configured the provider list
 * is empty, which is exactly the situation every installation starts in.
 *
 * <p>
 * Note this is <em>not</em> {@code @ConditionalOnProperty}. That matches a
 * property which exists but is empty — and the property does exist, because
 * {@code application.properties} declares it with an empty default. The adapter
 * registered anyway, collided with the stand-in provider in the tests, and
 * would in production have been present-and-failing rather than absent: exactly
 * what this condition is here to prevent.
 */
@Conditional(CalendarProviderConfigured.Google.class)
@Component
public class GoogleCalendarProvider implements CalendarProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(GoogleCalendarProvider.class);

	private static final String FREE_BUSY_URL = "https://www.googleapis.com/calendar/v3/freeBusy";
	private static final String EVENTS_URL = "https://www.googleapis.com/calendar/v3/calendars/%s/events";
	private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";

	@Autowired
	private CalendarHttp http;

	@Autowired
	private CalendarTokenCipher cipher;

	@Value("${app.calendar.google.client-id:}")
	private String clientId;

	@Value("${app.calendar.google.client-secret:}")
	private String clientSecret;

	@Override
	public String name() {
		return CalendarConnection.GOOGLE;
	}

	@Override
	public List<BusyPeriod> busyPeriods(CalendarConnection connection, Instant from, Instant to) {
		String calendarId = calendarIdOf(connection);

		JsonNode response = http.postJson(
				FREE_BUSY_URL,
				accessToken(connection),
				"""
						{
						  "timeMin": "%s",
						  "timeMax": "%s",
						  "items": [ { "id": %s } ]
						}
						""".formatted(from, to, CalendarHttp.quote(calendarId)));

		List<BusyPeriod> busy = new ArrayList<>();
		JsonNode periods = response.path("calendars").path(calendarId).path("busy");
		for (JsonNode period : periods) {
			busy.add(new BusyPeriod(
					Instant.parse(period.path("start").asText()),
					Instant.parse(period.path("end").asText())));
		}

		/*
		 * Google reports per-calendar errors in the body with a 200 status —
		 * a calendar that was deleted or unshared comes back as an "errors"
		 * array rather than an HTTP failure. Left unchecked, that reads as
		 * "free all week" and the system starts offering times the host is
		 * committed to.
		 */
		JsonNode errors = response.path("calendars").path(calendarId).path("errors");
		if (errors.isArray() && !errors.isEmpty()) {
			throw new CalendarHttp.CalendarApiException(
					"Google would not report busy times for this calendar: "
							+ errors.get(0).path("reason").asText("unknown"));
		}

		return busy;
	}

	@Override
	public ExternalEvent createEvent(CalendarConnection connection, ConsultationBooking booking,
			boolean withVideoLink) {

		String calendarId = calendarIdOf(connection);

		/*
		 * conferenceDataVersion=1 is required for Google to act on a
		 * createRequest at all. Without it the call succeeds, the event appears,
		 * and there is simply no Meet link — a silent no-op that looks like
		 * Google ignoring the request.
		 */
		String url = EVENTS_URL.formatted(CalendarHttp.encodePath(calendarId))
				+ (withVideoLink ? "?conferenceDataVersion=1" : "");

		String conference = withVideoLink
				? """
						,
						  "conferenceData": {
						    "createRequest": {
						      "requestId": %s,
						      "conferenceSolutionKey": { "type": "hangoutsMeet" }
						    }
						  }
						""".formatted(CalendarHttp.quote(
						"consult-" + booking.getSerConsultationBookingId()))
				: "";

		JsonNode created = http.postJson(url, accessToken(connection), """
				{
				  "summary": %s,
				  "description": %s,
				  "start": { "dateTime": "%s" },
				  "end":   { "dateTime": "%s" },
				  "attendees": [ { "email": %s, "displayName": %s } ]%s
				}
				""".formatted(
				CalendarHttp.quote("Consultation — " + booking.getTxtCustomerName()),
				CalendarHttp.quote(describe(booking)),
				booking.getDteStartsAt(),
				booking.getDteEndsAt(),
				CalendarHttp.quote(booking.getTxtCustomerEmail()),
				CalendarHttp.quote(booking.getTxtCustomerName()),
				conference));

		String joinUrl = created.path("hangoutLink").asText(null);
		if (joinUrl == null) {
			// Newer responses carry it under conferenceData instead.
			for (JsonNode entry : created.path("conferenceData").path("entryPoints")) {
				if ("video".equals(entry.path("entryPointType").asText())) {
					joinUrl = entry.path("uri").asText(null);
					break;
				}
			}
		}

		return new ExternalEvent(created.path("id").asText(), joinUrl);
	}

	@Override
	public void deleteEvent(CalendarConnection connection, String externalEventId) {
		String url = EVENTS_URL.formatted(CalendarHttp.encodePath(calendarIdOf(connection)))
				+ "/" + CalendarHttp.encodePath(externalEventId);

		// 404 and 410 both mean "not there", which is agreement rather than
		// failure — the host may well have deleted it themselves.
		http.delete(url, accessToken(connection));
	}

	// -----------------------------------------------------------------

	private String describe(ConsultationBooking booking) {
		StringBuilder description = new StringBuilder()
				.append("Consultation with ").append(booking.getTxtCustomerName()).append(".\n");

		if (booking.getTxtCustomerPhone() != null && !booking.getTxtCustomerPhone().isBlank()) {
			description.append("Phone: ").append(booking.getTxtCustomerPhone()).append('\n');
		}
		if (booking.getTxtCustomerTimeZone() != null) {
			description.append("Their time zone: ").append(booking.getTxtCustomerTimeZone()).append('\n');
		}
		if (booking.getTxtNotes() != null && !booking.getTxtNotes().isBlank()) {
			description.append('\n').append(booking.getTxtNotes()).append('\n');
		}
		// No host name: the event is in their own calendar, so whose it is
		// needs no saying — and looking it up would mean this HTTP adapter
		// carrying a repository for one line of prose.
		return description.append("\nBooked through Diamond Events.").toString();
	}

	private String calendarIdOf(CalendarConnection connection) {
		// "primary" is Google's alias for whatever the account's own calendar is,
		// and the right default for somebody who never chose one.
		String calendarId = connection.getTxtCalendarId();
		return calendarId == null || calendarId.isBlank() ? "primary" : calendarId;
	}

	/**
	 * A usable access token, refreshing it first if it has expired.
	 *
	 * <p>
	 * Access tokens last an hour, so most consultations are booked with an
	 * expired one and refreshing is the normal path rather than the exception.
	 */
	private String accessToken(CalendarConnection connection) {
		boolean expired = connection.getDteTokenExpiresAt() == null
				|| connection.getDteTokenExpiresAt().isBefore(Instant.now().plusSeconds(60));

		if (!expired && connection.getTxtAccessTokenEncrypted() != null) {
			return cipher.decrypt(connection.getTxtAccessTokenEncrypted());
		}

		String refreshToken = cipher.decrypt(connection.getTxtRefreshTokenEncrypted());
		if (refreshToken == null) {
			throw new CalendarHttp.CalendarApiException(
					"This Google calendar has no refresh token and must be reconnected.");
		}

		JsonNode refreshed = http.postForm(TOKEN_URL, null,
				"client_id=" + CalendarHttp.encodeForm(clientId)
						+ "&client_secret=" + CalendarHttp.encodeForm(clientSecret)
						+ "&refresh_token=" + CalendarHttp.encodeForm(refreshToken)
						+ "&grant_type=refresh_token");

		String accessToken = refreshed.path("access_token").asText(null);
		if (accessToken == null) {
			throw new CalendarHttp.CalendarApiException(
					"Google refused to refresh this connection. It probably needs reconnecting.");
		}

		connection.setTxtAccessTokenEncrypted(cipher.encrypt(accessToken));
		connection.setDteTokenExpiresAt(
				Instant.now().plusSeconds(refreshed.path("expires_in").asLong(3600)));

		LOGGER.debug("Refreshed the Google access token for connection {}",
				connection.getSerCalendarConnectionId());

		return accessToken;
	}
}
