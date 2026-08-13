package com.zbs.de.config.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.zbs.de.model.SsoHandoffCode;
import com.zbs.de.repository.RepositorySsoHandoffCode;

/**
 * The one-time SSO handoff code.
 *
 * <p>
 * This code is a bearer credential for the couple of minutes it lives, so the
 * cases that matter are the abusive ones: replay, races, expiry and guessing.
 * A fake repository stands in for the database, with the delete behaving the
 * way a single SQL statement does — it either claims a row or it does not.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SsoHandoffServiceTest {

	private static final String ACCESS = "access-token-value";
	private static final String REFRESH = "refresh-token-value";

	@Mock
	private RepositorySsoHandoffCode repository;

	private SsoHandoffService service;

	/** Rows by code hash, standing in for the table. */
	private final Map<String, SsoHandoffCode> rows = new HashMap<>();

	@BeforeEach
	void setUp() {
		rows.clear();
		service = new SsoHandoffService(repository);

		when(repository.save(any(SsoHandoffCode.class))).thenAnswer(inv -> {
			SsoHandoffCode saved = inv.getArgument(0);
			rows.put(saved.getCodeHash(), saved);
			return saved;
		});
		when(repository.findByCodeHash(anyString()))
				.thenAnswer(inv -> Optional.ofNullable(rows.get(inv.getArgument(0, String.class))));
		// Atomic claim: exactly one caller can remove a given row.
		when(repository.deleteByCodeHash(anyString()))
				.thenAnswer(inv -> rows.remove(inv.getArgument(0, String.class)) != null ? 1 : 0);
	}

	@Test
	@DisplayName("a freshly issued code exchanges for the tokens")
	void redeemsOnce() {
		String code = service.issue(ACCESS, REFRESH);

		Optional<SsoHandoffService.Tokens> tokens = service.redeem(code);

		assertThat(tokens).isPresent();
		assertThat(tokens.get().accessToken()).isEqualTo(ACCESS);
		assertThat(tokens.get().refreshToken()).isEqualTo(REFRESH);
	}

	@Test
	@DisplayName("a code cannot be replayed")
	void cannotBeReplayed() {
		String code = service.issue(ACCESS, REFRESH);

		assertThat(service.redeem(code)).isPresent();
		// Anyone who lifts the code from a log or from history arrives second.
		assertThat(service.redeem(code)).isEmpty();
		assertThat(service.redeem(code)).isEmpty();
	}

	@Test
	@DisplayName("only one of several simultaneous exchanges wins")
	void concurrentExchangesResolveToOneWinner() throws Exception {
		String code = service.issue(ACCESS, REFRESH);

		int attempts = 16;
		ExecutorService pool = Executors.newFixedThreadPool(attempts);
		try {
			List<Callable<Optional<SsoHandoffService.Tokens>>> tasks = java.util.stream.IntStream.range(0, attempts)
					.<Callable<Optional<SsoHandoffService.Tokens>>>mapToObj(i -> () -> service.redeem(code)).toList();

			AtomicInteger successes = new AtomicInteger();
			for (Future<Optional<SsoHandoffService.Tokens>> future : pool.invokeAll(tasks)) {
				if (future.get().isPresent()) {
					successes.incrementAndGet();
				}
			}

			// Two tabs, or a replay racing the real browser: the database decides,
			// and exactly one caller gets the tokens.
			assertThat(successes.get()).isEqualTo(1);
		} finally {
			pool.shutdownNow();
		}
	}

	@Test
	@DisplayName("an expired code is refused, and is consumed rather than left to retry")
	void expiredCodeIsRefusedAndConsumed() {
		String code = service.issue(ACCESS, REFRESH);

		// Age the stored row past its expiry.
		String onlyHash = rows.keySet().iterator().next();
		SsoHandoffCode expired = new SsoHandoffCode(onlyHash, ACCESS, REFRESH, Instant.now().minusSeconds(1));
		rows.put(onlyHash, expired);

		assertThat(service.redeem(code)).isEmpty();
		assertThat(rows).as("an expired code is still cleared, not left behind").isEmpty();
	}

	@Test
	@DisplayName("null, blank and unknown codes are refused without error")
	void rejectsMissingAndUnknownCodes() {
		assertThat(service.redeem(null)).isEmpty();
		assertThat(service.redeem("")).isEmpty();
		assertThat(service.redeem("   ")).isEmpty();
		assertThat(service.redeem("not-a-real-code")).isEmpty();
	}

	@Test
	@DisplayName("the code is never stored in plaintext")
	void storesOnlyAHash() {
		String code = service.issue(ACCESS, REFRESH);

		assertThat(rows).hasSize(1);
		String storedHash = rows.keySet().iterator().next();
		assertThat(storedHash).isNotEqualTo(code);
		// SHA-256, hex.
		assertThat(storedHash).hasSize(64).matches("[0-9a-f]{64}");
	}

	@Test
	@DisplayName("codes are unguessable and never repeat")
	void codesAreRandomAndUrlSafe() {
		java.util.Set<String> seen = new java.util.HashSet<>();
		for (int i = 0; i < 500; i++) {
			String code = service.issue(ACCESS, REFRESH);
			assertThat(seen.add(code)).as("issued a duplicate code").isTrue();
			// 256 bits, base64url — must survive a redirect without escaping.
			assertThat(code).hasSizeGreaterThanOrEqualTo(43).matches("[A-Za-z0-9_-]+");
		}
	}

	@Test
	@DisplayName("two sign-ins in flight at once do not interfere")
	void independentCodesAreIndependent() {
		String first = service.issue("access-1", "refresh-1");
		String second = service.issue("access-2", "refresh-2");

		assertThat(service.redeem(second).get().accessToken()).isEqualTo("access-2");
		// Redeeming one must not consume the other — a shared browser, or one
		// person signing in on a phone and a laptop.
		assertThat(service.redeem(first).get().accessToken()).isEqualTo("access-1");
	}
}
