package com.zbs.de.service.calendar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.zbs.de.service.calendar.CalendarOAuthState.InvalidStateException;

/**
 * The state parameter that protects the OAuth callback.
 *
 * <p>
 * Worth testing thoroughly because of what it is the only defence against. The
 * callback has to be publicly reachable — the provider redirects a browser to
 * it, so it arrives with no bearer token — and without a verified state anyone
 * can complete an OAuth flow against their own Google account and hand an
 * administrator a link that connects <em>the attacker's calendar</em> to a
 * member of the team. Every consultation booked with that person would then be
 * written into a calendar the attacker controls, complete with names, email
 * addresses, phone numbers and times.
 */
class CalendarOAuthStateTest {

	private static final String SECRET =
			"a-long-enough-development-signing-secret-for-hmac-sha256";

	private CalendarOAuthState state;

	@BeforeEach
	void setUp() {
		state = new CalendarOAuthState();
		ReflectionTestUtils.setField(state, "secret", SECRET);
	}

	@Test
	@DisplayName("a state we issued verifies back to what it asked for")
	void roundTrips() {
		var intent = state.verify(state.issue(7, "GOOGLE"));

		assertThat(intent.serHostId()).isEqualTo(7);
		assertThat(intent.provider()).isEqualTo("GOOGLE");
	}

	@Test
	@DisplayName("a state nobody signed is refused")
	void anUnsignedStateIsRefused() {
		// The attack in its simplest form: a hand-written state naming whichever
		// host the attacker would like their calendar attached to.
		String forged = Base64.getUrlEncoder().withoutPadding()
				.encodeToString(("1:GOOGLE:" + (Instant.now().getEpochSecond() + 600) + ":nonce")
						.getBytes(StandardCharsets.UTF_8))
				+ ".not-a-real-signature";

		assertThatThrownBy(() -> state.verify(forged))
				.isInstanceOf(InvalidStateException.class)
				.hasMessageContaining("did not come from this application");
	}

	@Test
	@DisplayName("changing the host id after signing is refused")
	void aTamperedHostIdIsRefused() {
		/*
		 * The subtler version: take a state legitimately issued for host 7 and
		 * edit it to say host 1. The signature covers the whole payload, so the
		 * edit invalidates it.
		 */
		String issued = state.issue(7, "GOOGLE");
		String signature = issued.substring(issued.lastIndexOf('.'));
		String payload = new String(Base64.getUrlDecoder()
				.decode(issued.substring(0, issued.lastIndexOf('.'))), StandardCharsets.UTF_8);

		String tampered = Base64.getUrlEncoder().withoutPadding()
				.encodeToString(payload.replaceFirst("^7:", "1:").getBytes(StandardCharsets.UTF_8))
				+ signature;

		assertThatThrownBy(() -> state.verify(tampered))
				.isInstanceOf(InvalidStateException.class);
	}

	@Test
	@DisplayName("a state signed with a different secret is refused")
	void anotherInstallationsStateIsRefused() {
		CalendarOAuthState elsewhere = new CalendarOAuthState();
		ReflectionTestUtils.setField(elsewhere, "secret", "a-completely-different-secret-value");

		assertThatThrownBy(() -> state.verify(elsewhere.issue(7, "GOOGLE")))
				.isInstanceOf(InvalidStateException.class);
	}

	@Test
	@DisplayName("an expired state says so, and says to start again")
	void anExpiredStateExplainsItself() {
		// Signed correctly but stale — captured from a browser history, or an
		// administrator who wandered off mid-consent. The remedy is to start
		// again, and somebody who reads "invalid" will think something is broken.
		String payload = "7:GOOGLE:" + (Instant.now().getEpochSecond() - 1) + ":nonce";
		String expired = Base64.getUrlEncoder().withoutPadding()
				.encodeToString(payload.getBytes(StandardCharsets.UTF_8))
				+ "." + ReflectionTestUtils.invokeMethod(state, "sign", payload);

		assertThatThrownBy(() -> state.verify(expired))
				.isInstanceOf(InvalidStateException.class)
				.hasMessageContaining("start again");
	}

	@Test
	@DisplayName("a state that is still in date is accepted")
	void aFreshStateIsAccepted() {
		// The mirror of the test above: an expiry check that rejected everything
		// would pass that one and break the feature entirely.
		String payload = "7:GOOGLE:" + (Instant.now().getEpochSecond() + 60) + ":nonce";
		String fresh = Base64.getUrlEncoder().withoutPadding()
				.encodeToString(payload.getBytes(StandardCharsets.UTF_8))
				+ "." + ReflectionTestUtils.invokeMethod(state, "sign", payload);

		assertThat(state.verify(fresh).serHostId()).isEqualTo(7);
	}

	@Test
	@DisplayName("a missing or malformed state is refused rather than crashing")
	void rubbishIsRefusedCleanly() {
		// This endpoint is public, so anything at all can arrive at it. Each of
		// these must be a civil refusal, not a stack trace.
		for (String rubbish : new String[] { null, "", "   ", "no-dot", ".", "....",
				"!!!not-base64!!!.sig", "dG9vLWZldy1wYXJ0cw.sig" }) {
			assertThatThrownBy(() -> state.verify(rubbish))
					.as("state %s was not refused cleanly", rubbish)
					.isInstanceOf(InvalidStateException.class);
		}
	}

	@Test
	@DisplayName("two states issued for the same thing are different")
	void statesAreNotPredictable() {
		// A nonce, so a state cannot be guessed by knowing the host, the provider
		// and roughly the time.
		assertThat(state.issue(7, "GOOGLE")).isNotEqualTo(state.issue(7, "GOOGLE"));
	}
}
