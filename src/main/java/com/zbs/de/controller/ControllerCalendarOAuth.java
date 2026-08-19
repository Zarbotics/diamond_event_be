package com.zbs.de.controller;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zbs.de.service.calendar.CalendarOAuthState;
import com.zbs.de.service.calendar.ServiceCalendarConnection;

/**
 * Where a calendar provider sends the administrator's browser back to.
 *
 * <p>
 * Necessarily public, and necessarily a {@code GET}: it is a redirect from
 * Google or Microsoft, so there is no bearer token, no session and no way to
 * ask for one. The signed {@code state} parameter is what identifies the
 * request, and it is verified before anything at all is done — see
 * {@link CalendarOAuthState} for what it is defending against.
 *
 * <p>
 * The answer is a redirect back to the admin portal rather than JSON, because
 * what is looking at this response is a browser that the administrator is
 * watching. Leaving them on a page of JSON at an API host would be leaving them
 * stranded.
 */
@RestController
@RequestMapping("/calendar/oauth")
public class ControllerCalendarOAuth {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerCalendarOAuth.class);

	@Autowired
	private ServiceCalendarConnection serviceCalendarConnection;

	@Value("${app.frontend.admin-url:http://localhost:3000}")
	private String adminUrl;

	/**
	 * The provider's redirect, carrying either a code or a refusal.
	 *
	 * @param error sent instead of a code when the administrator pressed cancel
	 *              on the consent screen, or the provider refused. Not a fault:
	 *              somebody changing their mind is an ordinary outcome and must
	 *              not read like a broken system.
	 */
	@GetMapping("/callback")
	public ResponseEntity<Void> callback(
			@RequestParam(required = false) String code,
			@RequestParam(required = false) String state,
			@RequestParam(required = false) String error) {

		if (error != null && !error.isBlank()) {
			// "access_denied" is what pressing cancel produces.
			LOGGER.info("A calendar connection was not completed: {}", error);
			return back("cancelled", "That connection was cancelled, so nothing has changed.");
		}

		try {
			var connection = serviceCalendarConnection.complete(code, state);
			return back("connected", connection.getTxtAccountEmail() + " is now connected.");

		} catch (CalendarOAuthState.InvalidStateException e) {
			/*
			 * Logged at warn: a bad state is either a link that sat around too
			 * long, or somebody trying the attack this parameter exists to stop.
			 * Worth seeing either way. The state itself is not logged — it is a
			 * credential.
			 */
			LOGGER.warn("A calendar callback arrived with an unusable state: {}", e.getMessage());
			return back("failed", e.getMessage());

		} catch (Exception e) {
			LOGGER.warn("A calendar connection failed: {}", e.getMessage());
			return back("failed", "That calendar could not be connected. " + e.getMessage());
		}
	}

	/**
	 * Sends the browser back to the admin portal with something to show.
	 *
	 * <p>
	 * The message is put in the query string rather than a flash message,
	 * because there is no session between these two applications to hold one.
	 */
	private ResponseEntity<Void> back(String outcome, String message) {
		String target = adminUrl
				+ (adminUrl.endsWith("/") ? "" : "/")
				+ "admin/consultations?calendar=" + outcome
				+ "&message=" + URLEncoder.encode(message, StandardCharsets.UTF_8);

		return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(target)).build();
	}
}
