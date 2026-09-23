package com.zbs.de.config.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zbs.de.model.SsoHandoffCode;
import com.zbs.de.repository.RepositorySsoHandoffCode;

/**
 * Issues and redeems the one-time code that replaces tokens in the SSO redirect
 * URL.
 */
@Service
public class SsoHandoffService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SsoHandoffService.class);

	/**
	 * How long a code stays valid.
	 *
	 * <p>
	 * Long enough to survive a slow redirect on a poor mobile connection, short
	 * enough that a code captured from a server log is almost always already dead.
	 */
	private static final Duration LIFETIME = Duration.ofSeconds(120);

	private static final SecureRandom RANDOM = new SecureRandom();

	private final RepositorySsoHandoffCode repository;

	public SsoHandoffService(RepositorySsoHandoffCode repository) {
		this.repository = repository;
	}

	/** The tokens handed back by a successful exchange. */
	public record Tokens(String accessToken, String refreshToken) {
	}

	/**
	 * Mints a code for a freshly authenticated session.
	 *
	 * @return the code to put in the redirect URL — the only time it exists in
	 *         plaintext
	 */
	@Transactional
	public String issue(String accessToken, String refreshToken) {
		// 256 bits. Unguessable within the code's lifetime by any margin that
		// matters, and URL-safe so it survives the redirect intact.
		byte[] raw = new byte[32];
		RANDOM.nextBytes(raw);
		String code = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);

		repository.save(new SsoHandoffCode(hash(code), accessToken, refreshToken,
				Instant.now().plus(LIFETIME)));

		return code;
	}

	/**
	 * Redeems a code, exactly once.
	 *
	 * <p>
	 * Returns empty for every failure — unknown, already used, expired — without
	 * distinguishing between them, so the response cannot be used to probe which
	 * codes exist.
	 */
	@Transactional
	public Optional<Tokens> redeem(String code) {
		if (code == null || code.isBlank()) {
			return Optional.empty();
		}

		String codeHash = hash(code);

		Optional<SsoHandoffCode> found = repository.findByCodeHash(codeHash);
		if (found.isEmpty()) {
			LOGGER.warn("SSO handoff code rejected: unknown or already used");
			return Optional.empty();
		}

		SsoHandoffCode handoff = found.get();

		// The delete is the claim on the code. If two requests race — two tabs, or
		// a replay arriving alongside the real browser — exactly one deletes a row
		// and gets the tokens. A check-then-delete would let both through.
		if (repository.deleteByCodeHash(codeHash) != 1) {
			LOGGER.warn("SSO handoff code rejected: lost the race to another exchange");
			return Optional.empty();
		}

		// Expiry is checked after the claim, so an expired code is still consumed
		// rather than left behind for someone to keep retrying.
		if (handoff.isExpired()) {
			LOGGER.warn("SSO handoff code rejected: expired");
			return Optional.empty();
		}

		return Optional.of(new Tokens(handoff.getAccessToken(), handoff.getRefreshToken()));
	}

	/**
	 * Clears codes that were issued but never exchanged — an abandoned sign-in, a
	 * closed tab. Without this the table grows forever.
	 */
	@Scheduled(fixedDelay = 15, initialDelay = 5, timeUnit = java.util.concurrent.TimeUnit.MINUTES)
	@Transactional
	public void purgeExpired() {
		int removed = repository.deleteExpired(Instant.now());
		if (removed > 0) {
			LOGGER.debug("Purged {} expired SSO handoff codes", removed);
		}
	}

	private String hash(String code) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(code.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException e) {
			// SHA-256 is required of every JVM; if it is missing the platform is broken.
			throw new IllegalStateException("SHA-256 unavailable", e);
		}
	}
}
