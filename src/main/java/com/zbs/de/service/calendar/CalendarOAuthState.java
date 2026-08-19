package com.zbs.de.service.calendar;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The {@code state} parameter that protects the OAuth callback.
 *
 * <h2>Why this is not optional</h2>
 *
 * The callback endpoint has to be publicly reachable. Google and Microsoft
 * redirect the administrator's <em>browser</em> to it, so it arrives as a plain
 * GET with no bearer token and nothing else identifying who asked for it. That
 * makes {@code state} the only thing standing between the endpoint and anybody
 * on the internet.
 *
 * <p>
 * Without it the attack is straightforward and quiet: someone completes an
 * OAuth flow against <em>their own</em> Google account, takes the resulting
 * {@code code}, and sends an administrator a link to the callback carrying it
 * and a chosen {@code serHostId}. The system dutifully connects the attacker's
 * calendar to a member of the team. From then on every consultation booked with
 * that person is written into a calendar the attacker controls — names, email
 * addresses, phone numbers and times, arriving automatically.
 *
 * <p>
 * So the state says who asked, for which provider, when, and carries a
 * signature proving this application issued it.
 *
 * <h2>What it deliberately does not do</h2>
 *
 * It is not single-use. Making it so needs a table and a sweep, and buys little
 * here: the window is ten minutes, and a replay reconnects the same host to the
 * same provider — the operation is idempotent and the outcome is the one that
 * was intended anyway. The signature and the expiry are what stop the attack
 * above; single-use would only stop a repeat of an action that was legitimate.
 */
@Component
public class CalendarOAuthState {

	/**
	 * How long an administrator has to finish signing in at the provider.
	 *
	 * <p>
	 * Long enough to read a consent screen properly, pick between two accounts
	 * and type a password with a second factor. Short enough that a state
	 * captured from a browser history or a proxy log is no longer usable.
	 */
	private static final long VALID_FOR_SECONDS = 600;

	private static final SecureRandom RANDOM = new SecureRandom();

	/**
	 * Signed with the application's own secret.
	 *
	 * <p>
	 * Shared with JWT signing deliberately: it is already required to be set and
	 * already required to be secret, and a second key that installations forget
	 * to configure would be a weaker one.
	 */
	@Value("${app.jwt.secret}")
	private String secret;

	/** What a valid state was asking for. */
	public record Intent(Integer serHostId, String provider) {
	}

	public static class InvalidStateException extends RuntimeException {
		public InvalidStateException(String message) {
			super(message);
		}
	}

	/** A state for one host connecting one provider, valid for ten minutes. */
	public String issue(Integer serHostId, String provider) {
		byte[] nonce = new byte[12];
		RANDOM.nextBytes(nonce);

		String payload = serHostId + ":" + provider
				+ ":" + (Instant.now().getEpochSecond() + VALID_FOR_SECONDS)
				+ ":" + Base64.getUrlEncoder().withoutPadding().encodeToString(nonce);

		return Base64.getUrlEncoder().withoutPadding()
				.encodeToString(payload.getBytes(StandardCharsets.UTF_8))
				+ "." + sign(payload);
	}

	/** What this state asked for, or a refusal. Never returns something unverified. */
	public Intent verify(String state) {
		if (state == null || state.isBlank()) {
			throw new InvalidStateException("That sign-in did not carry a state parameter.");
		}

		int dot = state.lastIndexOf('.');
		if (dot < 0) {
			throw new InvalidStateException("That sign-in link is malformed.");
		}

		String encodedPayload = state.substring(0, dot);
		String signature = state.substring(dot + 1);

		String payload;
		try {
			payload = new String(Base64.getUrlDecoder().decode(encodedPayload), StandardCharsets.UTF_8);
		} catch (IllegalArgumentException e) {
			throw new InvalidStateException("That sign-in link is malformed.");
		}

		/*
		 * Constant-time. A byte-by-byte comparison that returns early leaks how
		 * much of a guessed signature was right, which is enough to find a valid
		 * one by repetition.
		 */
		if (!MessageDigest.isEqual(
				sign(payload).getBytes(StandardCharsets.UTF_8),
				signature.getBytes(StandardCharsets.UTF_8))) {
			throw new InvalidStateException("That sign-in did not come from this application.");
		}

		String[] parts = payload.split(":");
		if (parts.length != 4) {
			throw new InvalidStateException("That sign-in link is malformed.");
		}

		long expiresAt;
		try {
			expiresAt = Long.parseLong(parts[2]);
		} catch (NumberFormatException e) {
			throw new InvalidStateException("That sign-in link is malformed.");
		}

		if (Instant.now().getEpochSecond() > expiresAt) {
			// Said plainly, because the remedy is simply to start again and
			// somebody who reads "invalid" will think something is broken.
			throw new InvalidStateException(
					"That sign-in took too long and has expired. Please start again.");
		}

		try {
			return new Intent(Integer.valueOf(parts[0]), parts[1]);
		} catch (NumberFormatException e) {
			throw new InvalidStateException("That sign-in link is malformed.");
		}
	}

	private String sign(String payload) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			return Base64.getUrlEncoder().withoutPadding()
					.encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception e) {
			throw new IllegalStateException("Could not sign the OAuth state", e);
		}
	}
}
