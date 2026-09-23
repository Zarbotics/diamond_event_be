package com.zbs.de.service.calendar;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.zbs.de.model.CalendarConnection;
import com.zbs.de.repository.RepositoryCalendarConnection;

/**
 * Connecting and disconnecting a host's calendar.
 *
 * <p>
 * The OAuth dance, and what happens to the tokens afterwards. Two providers
 * with the same shape: send the administrator to the provider, receive a code
 * back, exchange it for tokens, store them encrypted.
 */
@Service
public class ServiceCalendarConnection {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCalendarConnection.class);

	private static final String GOOGLE_AUTH = "https://accounts.google.com/o/oauth2/v2/auth";
	private static final String GOOGLE_TOKEN = "https://oauth2.googleapis.com/token";

	private static final String MICROSOFT_AUTH =
			"https://login.microsoftonline.com/%s/oauth2/v2.0/authorize";
	private static final String MICROSOFT_TOKEN =
			"https://login.microsoftonline.com/%s/oauth2/v2.0/token";

	/**
	 * Availability and nothing else, plus the narrow write scope.
	 *
	 * <p>
	 * {@code calendar.app.created} gives access to a calendar this application
	 * creates and nothing that was already there. Asking for the broad
	 * {@code calendar} scope would let it read every appointment the person has,
	 * which this does not need and which nobody should agree to lightly.
	 */
	private static final String GOOGLE_SCOPES = String.join(" ",
			"https://www.googleapis.com/auth/calendar.freebusy",
			"https://www.googleapis.com/auth/calendar.app.created",
			"openid", "email");

	/** Delegated: acting as the person who connected, never as the organisation. */
	private static final String MICROSOFT_SCOPES = String.join(" ",
			"offline_access", "Calendars.ReadBasic", "Calendars.ReadWrite", "User.Read");

	@Autowired
	private CalendarHttp http;

	@Autowired
	private CalendarTokenCipher cipher;

	@Autowired
	private CalendarOAuthState oauthState;

	@Autowired
	private RepositoryCalendarConnection repositoryConnection;

	@Value("${app.calendar.google.client-id:}")
	private String googleClientId;

	@Value("${app.calendar.google.client-secret:}")
	private String googleClientSecret;

	@Value("${app.calendar.microsoft.client-id:}")
	private String microsoftClientId;

	@Value("${app.calendar.microsoft.client-secret:}")
	private String microsoftClientSecret;

	@Value("${app.calendar.microsoft.tenant:common}")
	private String microsoftTenant;

	/**
	 * Where the provider sends the browser back to.
	 *
	 * <p>
	 * Must match what was registered in the provider's console exactly,
	 * including the scheme and any trailing path. A mismatch is the single most
	 * common cause of a failed setup, and both providers report it as an opaque
	 * {@code redirect_uri_mismatch}.
	 */
	@Value("${app.calendar.redirect-uri:http://localhost:8080/diamond/calendar/oauth/callback}")
	private String redirectUri;

	public static class NotConfiguredException extends RuntimeException {
		public NotConfiguredException(String message) {
			super(message);
		}
	}

	/** Whether a provider can be connected at all, for the admin screen to show. */
	public boolean isConfigured(String provider) {
		return switch (provider) {
			case CalendarConnection.GOOGLE -> notBlank(googleClientId) && cipher.isConfigured();
			case CalendarConnection.MICROSOFT -> notBlank(microsoftClientId) && cipher.isConfigured();
			default -> false;
		};
	}

	/**
	 * Where to send the administrator to grant access.
	 *
	 * <p>
	 * {@code access_type=offline} and {@code prompt=consent} on Google are both
	 * needed to be given a refresh token: without them a second connection of an
	 * account that has been connected before returns an access token only, and
	 * everything works for an hour and then stops.
	 */
	public String authorisationUrl(Integer serHostId, String provider) {
		requireConfigured(provider);
		String state = oauthState.issue(serHostId, provider);

		return switch (provider) {
			case CalendarConnection.GOOGLE -> GOOGLE_AUTH
					+ "?client_id=" + CalendarHttp.encodeForm(googleClientId)
					+ "&redirect_uri=" + CalendarHttp.encodeForm(redirectUri)
					+ "&response_type=code"
					+ "&scope=" + CalendarHttp.encodeForm(GOOGLE_SCOPES)
					+ "&access_type=offline"
					+ "&prompt=consent"
					+ "&include_granted_scopes=true"
					+ "&state=" + CalendarHttp.encodeForm(state);

			case CalendarConnection.MICROSOFT -> MICROSOFT_AUTH.formatted(microsoftTenant)
					+ "?client_id=" + CalendarHttp.encodeForm(microsoftClientId)
					+ "&redirect_uri=" + CalendarHttp.encodeForm(redirectUri)
					+ "&response_type=code"
					+ "&response_mode=query"
					+ "&scope=" + CalendarHttp.encodeForm(MICROSOFT_SCOPES)
					+ "&state=" + CalendarHttp.encodeForm(state);

			default -> throw new NotConfiguredException("Unknown calendar provider: " + provider);
		};
	}

	/**
	 * Completes the connection from the code the provider sent back.
	 *
	 * @param state verified before anything else happens — it is the only thing
	 *              identifying who asked for this, because the callback arrives
	 *              as a browser redirect with no session
	 */
	@Transactional
	public CalendarConnection complete(String code, String state) {
		CalendarOAuthState.Intent intent = oauthState.verify(state);
		requireConfigured(intent.provider());

		if (code == null || code.isBlank()) {
			throw new NotConfiguredException("The provider did not send an authorisation code.");
		}

		JsonNode tokens = exchange(intent.provider(), code);

		String refreshToken = tokens.path("refresh_token").asText(null);
		String accessToken = tokens.path("access_token").asText(null);
		if (accessToken == null) {
			throw new NotConfiguredException("The provider did not return an access token.");
		}

		String accountEmail = accountEmailFor(intent.provider(), accessToken, tokens);

		CalendarConnection connection = repositoryConnection
				.findBySerHostIdAndBlnIsDeletedFalse(intent.serHostId()).stream()
				.filter(c -> c.getTxtProvider().equals(intent.provider())
						&& c.getTxtAccountEmail().equalsIgnoreCase(accountEmail))
				.findFirst()
				.orElseGet(CalendarConnection::new);

		connection.setSerHostId(intent.serHostId());
		connection.setTxtProvider(intent.provider());
		connection.setTxtAccountEmail(accountEmail);
		connection.setTxtAccessTokenEncrypted(cipher.encrypt(accessToken));
		connection.setDteTokenExpiresAt(
				Instant.now().plusSeconds(tokens.path("expires_in").asLong(3600)));

		/*
		 * Only overwrite the refresh token when one came back. Reconnecting an
		 * account that has been connected before can return an access token
		 * alone, and blanking the stored refresh token then would break the
		 * connection an hour later — with the reconnection looking like the
		 * thing that fixed it.
		 */
		if (notBlank(refreshToken)) {
			connection.setTxtRefreshTokenEncrypted(cipher.encrypt(refreshToken));
		}

		connection.setTxtSyncStatus(CalendarConnection.SYNC_HEALTHY);
		connection.setTxtSyncError(null);
		connection.setBlnIsDeleted(false);
		connection.setUpdatedDate(Instant.now());

		/*
		 * The first calendar a host connects becomes the one their consultations
		 * are written to. Somebody who connects exactly one and never visits the
		 * setting should not find that nothing was ever written.
		 */
		boolean hasWriteTarget = repositoryConnection.writeTargetFor(intent.serHostId()).isPresent();
		connection.setBlnIsWriteTarget(!hasWriteTarget);

		CalendarConnection saved = repositoryConnection.save(connection);
		LOGGER.info("Connected a {} calendar for host {}", intent.provider(), intent.serHostId());
		return saved;
	}

	/** Stops using a calendar. The tokens go with it. */
	@Transactional
	public void disconnect(Integer serCalendarConnectionId) {
		repositoryConnection.findById(serCalendarConnectionId).ifPresent(connection -> {
			/*
			 * The tokens are cleared, not merely orphaned. A soft-deleted row
			 * still carrying a live refresh token is a standing grant to the
			 * team's calendars that nothing will ever use and nobody will think
			 * to look at.
			 */
			connection.setTxtAccessTokenEncrypted(null);
			connection.setTxtRefreshTokenEncrypted(null);
			connection.setBlnIsWriteTarget(false);
			connection.setBlnIsDeleted(true);
			connection.setUpdatedDate(Instant.now());
			repositoryConnection.save(connection);

			LOGGER.info("Disconnected calendar {}", serCalendarConnectionId);
		});
	}

	/** Chooses which of a host's calendars their consultations are written to. */
	@Transactional
	public void chooseWriteTarget(Integer serCalendarConnectionId) {
		CalendarConnection chosen = repositoryConnection.findById(serCalendarConnectionId)
				.orElseThrow(() -> new NotConfiguredException("No such calendar connection."));

		/*
		 * The old one is cleared first and flushed, because the database enforces
		 * at most one write target per host with a partial unique index. Setting
		 * the new one first would collide with the old.
		 */
		repositoryConnection.writeTargetFor(chosen.getSerHostId()).ifPresent(previous -> {
			if (!previous.getSerCalendarConnectionId().equals(serCalendarConnectionId)) {
				previous.setBlnIsWriteTarget(false);
				repositoryConnection.saveAndFlush(previous);
			}
		});

		chosen.setBlnIsWriteTarget(true);
		chosen.setUpdatedDate(Instant.now());
		repositoryConnection.save(chosen);
	}

	public List<CalendarConnection> forHost(Integer serHostId) {
		return repositoryConnection.findBySerHostIdAndBlnIsDeletedFalse(serHostId);
	}

	// -----------------------------------------------------------------

	private JsonNode exchange(String provider, String code) {
		String body = "code=" + CalendarHttp.encodeForm(code)
				+ "&redirect_uri=" + CalendarHttp.encodeForm(redirectUri)
				+ "&grant_type=authorization_code";

		return switch (provider) {
			case CalendarConnection.GOOGLE -> http.postForm(GOOGLE_TOKEN, null,
					body + "&client_id=" + CalendarHttp.encodeForm(googleClientId)
							+ "&client_secret=" + CalendarHttp.encodeForm(googleClientSecret));

			case CalendarConnection.MICROSOFT -> http.postForm(
					MICROSOFT_TOKEN.formatted(microsoftTenant), null,
					body + "&client_id=" + CalendarHttp.encodeForm(microsoftClientId)
							+ "&client_secret=" + CalendarHttp.encodeForm(microsoftClientSecret)
							+ "&scope=" + CalendarHttp.encodeForm(MICROSOFT_SCOPES));

			default -> throw new NotConfiguredException("Unknown calendar provider: " + provider);
		};
	}

	/**
	 * Which account was actually connected.
	 *
	 * <p>
	 * Asked of the provider rather than assumed from the host's own email. They
	 * are routinely different — somebody whose work address is on the team page
	 * may keep their diary in a personal account — and getting it wrong means
	 * reading availability from a mailbox nobody uses.
	 */
	private String accountEmailFor(String provider, String accessToken, JsonNode tokens) {
		try {
			if (CalendarConnection.GOOGLE.equals(provider)) {
				return http.getJson("https://www.googleapis.com/oauth2/v3/userinfo", accessToken)
						.path("email").asText("unknown");
			}
			JsonNode me = http.getJson("https://graph.microsoft.com/v1.0/me", accessToken);
			return me.path("mail").asText(me.path("userPrincipalName").asText("unknown"));
		} catch (Exception e) {
			// Not worth failing the connection over. The address is a label on a
			// screen; everything else works without it.
			LOGGER.warn("Could not read the connected account's address: {}", e.getMessage());
			return "unknown";
		}
	}

	private void requireConfigured(String provider) {
		if (!cipher.isConfigured()) {
			throw new NotConfiguredException(
					"app.calendar.token-key is not set, so calendars cannot be connected. "
							+ "Generate one with: openssl rand -base64 32");
		}
		if (!isConfigured(provider)) {
			throw new NotConfiguredException(
					provider + " is not configured on this server. Set its client id and secret first.");
		}
	}

	private boolean notBlank(String value) {
		return value != null && !value.isBlank();
	}
}
