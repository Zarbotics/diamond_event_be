package com.zbs.de.service.calendar;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Encrypts the calendar tokens before they reach the database.
 *
 * <p>
 * The schema has said {@code txt_refresh_token_encrypted} since V6; this is what
 * makes that column name true rather than aspirational. It matters more than
 * most encryption-at-rest does: a refresh token is not a password that expires
 * or a session that ends, it is a standing grant to read and write the whole
 * team's calendars, valid until somebody revokes it. Anyone who reaches this
 * database — or one of its backups, or a dump attached to a support ticket —
 * would otherwise have exactly that.
 *
 * <h2>AES-GCM, and why the IV travels with the value</h2>
 *
 * GCM authenticates as well as encrypts, so a token that has been tampered with
 * fails to decrypt rather than decrypting into something else. It needs a fresh
 * initialisation vector for every encryption — reusing one with the same key is
 * the failure that breaks GCM completely — so one is generated per call and
 * stored in front of the ciphertext. It is not secret; it only has to be
 * unique.
 *
 * <h2>No key means no storage, not plaintext storage</h2>
 *
 * If the key is not configured this refuses to encrypt. The tempting
 * alternative — fall back to storing the value as it is — turns a missing
 * setting into a silent, permanent leak that nothing will ever report. Failing
 * loudly at the point somebody connects a calendar is far cheaper: they see an
 * error, an administrator sets the key, and nothing sensitive was ever written.
 *
 * <p>
 * Development is unaffected, because with nothing connected there are no tokens
 * to store.
 */
@Component
public class CalendarTokenCipher {

	private static final String ALGORITHM = "AES";
	private static final String TRANSFORMATION = "AES/GCM/NoPadding";

	/** 96 bits is the size GCM is defined around; other sizes are slower and no safer. */
	private static final int IV_LENGTH = 12;

	private static final int TAG_LENGTH_BITS = 128;

	private static final SecureRandom RANDOM = new SecureRandom();

	/**
	 * Base64 of 32 random bytes. Generate one with:
	 *
	 * <pre>openssl rand -base64 32</pre>
	 *
	 * Empty by default so that a development machine starts without it. Nothing
	 * needs it until a calendar is actually connected.
	 */
	@Value("${app.calendar.token-key:}")
	private String configuredKey;

	/** Thrown rather than returning something that looks like it worked. */
	public static class TokenCipherException extends RuntimeException {
		public TokenCipherException(String message) {
			super(message);
		}

		public TokenCipherException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	/** Whether a calendar can be connected at all. */
	public boolean isConfigured() {
		return configuredKey != null && !configuredKey.isBlank();
	}

	public String encrypt(String plaintext) {
		if (plaintext == null) {
			return null;
		}
		SecretKeySpec key = key();

		byte[] iv = new byte[IV_LENGTH];
		RANDOM.nextBytes(iv);

		try {
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
			byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

			byte[] combined = new byte[iv.length + ciphertext.length];
			System.arraycopy(iv, 0, combined, 0, iv.length);
			System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

			return Base64.getEncoder().encodeToString(combined);
		} catch (Exception e) {
			// Deliberately does not include the plaintext or the exception's own
			// message, either of which can carry fragments of the token.
			throw new TokenCipherException("Could not encrypt the calendar token");
		}
	}

	public String decrypt(String stored) {
		if (stored == null) {
			return null;
		}
		SecretKeySpec key = key();

		try {
			byte[] combined = Base64.getDecoder().decode(stored);
			if (combined.length <= IV_LENGTH) {
				throw new TokenCipherException("Stored calendar token is too short to be valid");
			}

			byte[] iv = new byte[IV_LENGTH];
			System.arraycopy(combined, 0, iv, 0, IV_LENGTH);

			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));

			byte[] plaintext = cipher.doFinal(combined, IV_LENGTH, combined.length - IV_LENGTH);
			return new String(plaintext, StandardCharsets.UTF_8);
		} catch (TokenCipherException e) {
			throw e;
		} catch (Exception e) {
			/*
			 * Almost always one of two things: the key was rotated without
			 * re-encrypting what was stored under the old one, or the value was
			 * altered. Both mean the connection has to be made again, which is
			 * a nuisance but not a loss — the tokens are replaceable by asking
			 * their owner to reconnect.
			 */
			throw new TokenCipherException(
					"Could not decrypt a stored calendar token. If app.calendar.token-key was "
							+ "changed, the affected calendars must be reconnected.", e);
		}
	}

	private SecretKeySpec key() {
		if (!isConfigured()) {
			throw new TokenCipherException(
					"app.calendar.token-key is not set, so calendar tokens cannot be stored. "
							+ "Generate one with: openssl rand -base64 32");
		}

		byte[] raw;
		try {
			raw = Base64.getDecoder().decode(configuredKey.trim());
		} catch (IllegalArgumentException e) {
			throw new TokenCipherException("app.calendar.token-key is not valid base64");
		}

		if (raw.length != 16 && raw.length != 24 && raw.length != 32) {
			// Said with the actual length, because "invalid key" sends people
			// looking at the wrong thing. 32 bytes is what the docs tell them to
			// generate, so anything else is usually a truncated paste.
			throw new TokenCipherException(
					"app.calendar.token-key decodes to " + raw.length
							+ " bytes; AES needs 16, 24 or 32. Generate one with: openssl rand -base64 32");
		}

		return new SecretKeySpec(raw, ALGORITHM);
	}
}
