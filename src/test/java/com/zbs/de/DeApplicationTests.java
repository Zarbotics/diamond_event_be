package com.zbs.de;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Full-context smoke test.
 *
 * <p>
 * Disabled because it needs a live PostgreSQL instance and the full set of
 * runtime secrets, so it cannot run in CI as written. Re-enable it together
 * with Testcontainers, which gives the context a real throwaway database — see
 * the testing strategy in the platform review.
 *
 * <p>
 * The security rules this application depends on are covered instead by
 * {@code AccessGuardTest} and {@code PortalEndpointPolicyTest}, which are pure
 * unit tests and run everywhere.
 */
@SpringBootTest
@Disabled("Requires a PostgreSQL instance and runtime secrets; replace with a Testcontainers-backed test.")
class DeApplicationTests {

	@Test
	void contextLoads() {
	}
}
