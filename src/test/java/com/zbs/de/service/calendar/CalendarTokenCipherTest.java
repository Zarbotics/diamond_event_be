package com.zbs.de.service.calendar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Base64;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.zbs.de.service.calendar.CalendarTokenCipher.TokenCipherException;

/**
 * Encrypting the calendar tokens.
 *
 * <p>
 * Pure unit tests — no Spring, no database. The value being protected is a
 * refresh token, which is not a password that expires or a session that ends
 * but a standing grant to read and write the whole team's calendars until
 * somebody revokes it. That makes the two failure modes worth pinning down
 * precisely: storing it in the clear, and appearing to store it safely while
 * doing something useless.
 */
class CalendarTokenCipherTest {

	/** A key of the shape the documentation tells an administrator to generate. */
	private static final String KEY = Base64.getEncoder()
			.encodeToString("a-32-byte-key-for-testing-only!!".getBytes());

	private CalendarTokenCipher cipherWith(String key) {
		CalendarTokenCipher cipher = new CalendarTokenCipher();
		ReflectionTestUtils.setField(cipher, "configuredKey", key);
		return cipher;
	}

	@Test
	@DisplayName("what goes in comes back out")
	void roundTrips() {
		CalendarTokenCipher cipher = cipherWith(KEY);
		String token = "1//0eXaMpLe-refresh-token_value.with~punctuation";

		assertThat(cipher.decrypt(cipher.encrypt(token))).isEqualTo(token);
	}

	@Test
	@DisplayName("the stored value does not contain the token")
	void theStoredValueIsNotTheToken() {
		// The whole point. Worth asserting rather than assuming, because a cipher
		// that silently passed its input through would satisfy the round-trip
		// test above perfectly.
		CalendarTokenCipher cipher = cipherWith(KEY);
		String token = "1//0eXaMpLe-refresh-token";

		assertThat(cipher.encrypt(token)).doesNotContain(token);
	}

	@Test
	@DisplayName("encrypting the same token twice gives different stored values")
	void encryptionIsNotDeterministic() {
		/*
		 * A fresh initialisation vector per encryption, which GCM requires — the
		 * one failure that breaks it completely is reusing an IV with the same
		 * key. It also means somebody reading the table cannot tell that two
		 * hosts connected the same account.
		 */
		CalendarTokenCipher cipher = cipherWith(KEY);

		assertThat(cipher.encrypt("same-token")).isNotEqualTo(cipher.encrypt("same-token"));
	}

	@Test
	@DisplayName("a tampered value fails to decrypt rather than decrypting into something else")
	void tamperingIsDetected() {
		// GCM authenticates as well as encrypts. Without that, altering the
		// stored bytes would produce a different token rather than an error.
		CalendarTokenCipher cipher = cipherWith(KEY);
		byte[] stored = Base64.getDecoder().decode(cipher.encrypt("a-token"));
		stored[stored.length - 1] ^= 0x01;

		assertThatThrownBy(() -> cipher.decrypt(Base64.getEncoder().encodeToString(stored)))
				.isInstanceOf(TokenCipherException.class);
	}

	@Test
	@DisplayName("no key means refusing to store, never storing in the clear")
	void anUnsetKeyRefuses() {
		/*
		 * The tempting alternative is to fall back to storing the value as it
		 * is, which turns a missing setting into a silent permanent leak that
		 * nothing will ever report. Failing at the moment somebody connects a
		 * calendar is far cheaper: they see an error, an administrator sets the
		 * key, and nothing sensitive was ever written.
		 */
		CalendarTokenCipher cipher = cipherWith("");

		assertThat(cipher.isConfigured()).isFalse();
		assertThatThrownBy(() -> cipher.encrypt("a-token"))
				.isInstanceOf(TokenCipherException.class)
				.hasMessageContaining("openssl rand -base64 32");
	}

	@Test
	@DisplayName("a key of the wrong length says what length it actually was")
	void aShortKeySaysSo() {
		// Usually a truncated paste. "Invalid key" sends somebody looking at the
		// wrong thing; the actual byte count points straight at it.
		CalendarTokenCipher cipher = cipherWith(Base64.getEncoder().encodeToString("too-short".getBytes()));

		assertThatThrownBy(() -> cipher.encrypt("a-token"))
				.isInstanceOf(TokenCipherException.class)
				.hasMessageContaining("9 bytes");
	}

	@Test
	@DisplayName("a key that is not base64 says that, rather than failing later")
	void aMalformedKeySaysSo() {
		CalendarTokenCipher cipher = cipherWith("not base64 at all !!!");

		assertThatThrownBy(() -> cipher.encrypt("a-token"))
				.isInstanceOf(TokenCipherException.class)
				.hasMessageContaining("base64");
	}

	@Test
	@DisplayName("a value encrypted under a different key reports that it needs reconnecting")
	void aRotatedKeyExplainsItself() {
		/*
		 * Rotating the key without re-encrypting what is stored under the old one
		 * is a thing people do. The tokens are replaceable — their owner
		 * reconnects — so the message needs to say that rather than reading like
		 * data loss.
		 */
		String stored = cipherWith(KEY).encrypt("a-token");
		CalendarTokenCipher rotated = cipherWith(
				Base64.getEncoder().encodeToString("a-DIFFERENT-32-byte-key-for-test".getBytes()));

		assertThatThrownBy(() -> rotated.decrypt(stored))
				.isInstanceOf(TokenCipherException.class)
				.hasMessageContaining("reconnected");
	}

	@Test
	@DisplayName("null passes through, so an unconnected calendar is not an error")
	void nullIsNotAFailure() {
		CalendarTokenCipher cipher = cipherWith(KEY);

		assertThat(cipher.encrypt(null)).isNull();
		assertThat(cipher.decrypt(null)).isNull();
	}
}
