package com.zbs.de.service.calendar;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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
 * Microsoft 365 and Outlook, through Graph.
 *
 * <p>
 * The same two jobs as the Google adapter, and the same principle: ask for
 * availability rather than for the calendar. {@code getSchedule} returns a
 * string of availability codes and nothing about what the meetings are, so the
 * team's own appointments never enter this database.
 *
 * <h2>Scopes</h2>
 *
 * {@code Calendars.ReadBasic} to read availability and {@code Calendars.ReadWrite}
 * to write the consultation, both delegated — acting as the person who
 * connected their account, never as the organisation. {@code offline_access} is
 * what makes a refresh token available at all.
 *
 * <h2>Two things Graph does differently from Google</h2>
 *
 * <ul>
 * <li><strong>Availability comes back as a string of digits</strong>, one per
 * time slot, rather than as a list of periods. "0" is free and anything else is
 * some degree of busy. Runs of non-zero digits have to be walked and turned
 * back into periods.</li>
 * <li><strong>Times are local plus a named zone</strong>, not instants. Sending
 * a UTC instant without saying so gets it interpreted in the mailbox's own zone,
 * which silently moves every meeting by the offset.</li>
 * </ul>
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
@Conditional(CalendarProviderConfigured.Microsoft.class)
@Component
public class MicrosoftCalendarProvider implements CalendarProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(MicrosoftCalendarProvider.class);

	private static final String GRAPH = "https://graph.microsoft.com/v1.0";
	private static final String TOKEN_URL =
			"https://login.microsoftonline.com/%s/oauth2/v2.0/token";

	/**
	 * Graph's availability granularity, in minutes.
	 *
	 * <p>
	 * The smallest {@code getSchedule} accepts. A coarser interval would round
	 * short meetings away and report somebody free during them.
	 */
	private static final int AVAILABILITY_INTERVAL_MINUTES = 5;

	private static final DateTimeFormatter GRAPH_LOCAL =
			DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

	@Autowired
	private CalendarHttp http;

	@Autowired
	private CalendarTokenCipher cipher;

	@Value("${app.calendar.microsoft.client-id:}")
	private String clientId;

	@Value("${app.calendar.microsoft.client-secret:}")
	private String clientSecret;

	/**
	 * "common" lets people on any Microsoft tenant connect, which is what a
	 * consultant working with several organisations needs. A single-tenant
	 * installation can pin its own tenant id here instead.
	 */
	@Value("${app.calendar.microsoft.tenant:common}")
	private String tenant;

	@Override
	public String name() {
		return CalendarConnection.MICROSOFT;
	}

	@Override
	public List<BusyPeriod> busyPeriods(CalendarConnection connection, Instant from, Instant to) {
		JsonNode response = http.postJson(
				GRAPH + "/me/calendar/getSchedule",
				accessToken(connection),
				"""
						{
						  "schedules": [ %s ],
						  "startTime": { "dateTime": "%s", "timeZone": "UTC" },
						  "endTime":   { "dateTime": "%s", "timeZone": "UTC" },
						  "availabilityViewInterval": %d
						}
						""".formatted(
						CalendarHttp.quote(connection.getTxtAccountEmail()),
						GRAPH_LOCAL.format(LocalDateTime.ofInstant(from, ZoneOffset.UTC)),
						GRAPH_LOCAL.format(LocalDateTime.ofInstant(to, ZoneOffset.UTC)),
						AVAILABILITY_INTERVAL_MINUTES));

		JsonNode schedule = response.path("value").path(0);
		if (schedule.isMissingNode()) {
			throw new CalendarHttp.CalendarApiException(
					"Microsoft returned no schedule for " + connection.getTxtAccountEmail());
		}

		return decodeAvailability(schedule.path("availabilityView").asText(""), from);
	}

	/**
	 * Turns Graph's availability string back into periods.
	 *
	 * <p>
	 * One character per {@link #AVAILABILITY_INTERVAL_MINUTES}: '0' free, and
	 * '1' to '4' tentative, busy, out of office, or working elsewhere. Anything
	 * other than free blocks a slot — someone marked tentative has a claim on
	 * that time, and offering it produces a double booking they then have to
	 * sort out.
	 *
	 * <p>
	 * Adjacent busy intervals are merged rather than emitted one by one, so a
	 * two-hour meeting is one period instead of twenty-four.
	 */
	static List<BusyPeriod> decodeAvailability(String availabilityView, Instant windowStart) {
		List<BusyPeriod> busy = new ArrayList<>();
		int runStart = -1;

		for (int i = 0; i < availabilityView.length(); i++) {
			boolean isBusy = availabilityView.charAt(i) != '0';

			if (isBusy && runStart < 0) {
				runStart = i;
			} else if (!isBusy && runStart >= 0) {
				busy.add(periodOf(windowStart, runStart, i));
				runStart = -1;
			}
		}
		// A run reaching the end of the window closes at the end of the window.
		if (runStart >= 0) {
			busy.add(periodOf(windowStart, runStart, availabilityView.length()));
		}

		return busy;
	}

	private static BusyPeriod periodOf(Instant windowStart, int fromIndex, int toIndex) {
		return new BusyPeriod(
				windowStart.plusSeconds((long) fromIndex * AVAILABILITY_INTERVAL_MINUTES * 60),
				windowStart.plusSeconds((long) toIndex * AVAILABILITY_INTERVAL_MINUTES * 60));
	}

	@Override
	public ExternalEvent createEvent(CalendarConnection connection, ConsultationBooking booking,
			boolean withVideoLink) {

		String calendarPath = connection.getTxtCalendarId() == null
				|| connection.getTxtCalendarId().isBlank()
						? "/me/events"
						: "/me/calendars/" + CalendarHttp.encodePath(connection.getTxtCalendarId())
								+ "/events";

		/*
		 * Times go as local-plus-named-zone, and the zone is stated as UTC.
		 * Graph interprets a dateTime without one in the mailbox's own zone,
		 * which would move every consultation by that offset — and do it
		 * silently, so it would look like the booking system had the wrong time
		 * rather than the calendar call having lost one.
		 */
		JsonNode created = http.postJson(GRAPH + calendarPath, accessToken(connection), """
				{
				  "subject": %s,
				  "body": { "contentType": "text", "content": %s },
				  "start": { "dateTime": "%s", "timeZone": "UTC" },
				  "end":   { "dateTime": "%s", "timeZone": "UTC" },
				  "attendees": [ {
				    "emailAddress": { "address": %s, "name": %s },
				    "type": "required"
				  } ],
				  "isOnlineMeeting": %s,
				  "onlineMeetingProvider": "teamsForBusiness"
				}
				""".formatted(
				CalendarHttp.quote("Consultation — " + booking.getTxtCustomerName()),
				CalendarHttp.quote(describe(booking)),
				GRAPH_LOCAL.format(LocalDateTime.ofInstant(booking.getDteStartsAt(), ZoneOffset.UTC)),
				GRAPH_LOCAL.format(LocalDateTime.ofInstant(booking.getDteEndsAt(), ZoneOffset.UTC)),
				CalendarHttp.quote(booking.getTxtCustomerEmail()),
				CalendarHttp.quote(booking.getTxtCustomerName()),
				withVideoLink));

		String joinUrl = created.path("onlineMeeting").path("joinUrl").asText(null);
		return new ExternalEvent(created.path("id").asText(), joinUrl);
	}

	@Override
	public void deleteEvent(CalendarConnection connection, String externalEventId) {
		http.delete(GRAPH + "/me/events/" + CalendarHttp.encodePath(externalEventId),
				accessToken(connection));
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
		return description.append("\nBooked through Diamond Events.").toString();
	}

	private String accessToken(CalendarConnection connection) {
		boolean expired = connection.getDteTokenExpiresAt() == null
				|| connection.getDteTokenExpiresAt().isBefore(Instant.now().plusSeconds(60));

		if (!expired && connection.getTxtAccessTokenEncrypted() != null) {
			return cipher.decrypt(connection.getTxtAccessTokenEncrypted());
		}

		String refreshToken = cipher.decrypt(connection.getTxtRefreshTokenEncrypted());
		if (refreshToken == null) {
			throw new CalendarHttp.CalendarApiException(
					"This Microsoft calendar has no refresh token and must be reconnected.");
		}

		JsonNode refreshed = http.postForm(TOKEN_URL.formatted(tenant), null,
				"client_id=" + CalendarHttp.encodeForm(clientId)
						+ "&client_secret=" + CalendarHttp.encodeForm(clientSecret)
						+ "&refresh_token=" + CalendarHttp.encodeForm(refreshToken)
						+ "&grant_type=refresh_token"
						+ "&scope=" + CalendarHttp.encodeForm(
								"offline_access Calendars.ReadBasic Calendars.ReadWrite"));

		String accessToken = refreshed.path("access_token").asText(null);
		if (accessToken == null) {
			throw new CalendarHttp.CalendarApiException(
					"Microsoft refused to refresh this connection. It probably needs reconnecting.");
		}

		connection.setTxtAccessTokenEncrypted(cipher.encrypt(accessToken));
		connection.setDteTokenExpiresAt(
				Instant.now().plusSeconds(refreshed.path("expires_in").asLong(3600)));

		/*
		 * Microsoft rotates refresh tokens: each refresh may return a new one,
		 * and the old one stops working. Missing this is the classic Graph bug —
		 * everything works for a fortnight, then every connection breaks at once
		 * when the original token finally expires.
		 */
		String rotated = refreshed.path("refresh_token").asText(null);
		if (rotated != null && !rotated.isBlank()) {
			connection.setTxtRefreshTokenEncrypted(cipher.encrypt(rotated));
		}

		LOGGER.debug("Refreshed the Microsoft access token for connection {}",
				connection.getSerCalendarConnectionId());

		return accessToken;
	}
}
