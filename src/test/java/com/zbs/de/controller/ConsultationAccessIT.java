package com.zbs.de.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.sql.Connection;
import java.sql.DriverManager;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.zbs.de.config.security.SecurityRoles;
import com.zbs.de.model.UserMaster;
import com.zbs.de.repository.RepositoryUserMaster;
import com.zbs.de.util.JwtTokenUtil;

/**
 * Who can reach the consultation endpoints, over real HTTP.
 *
 * <p>
 * {@code PortalEndpointPolicyTest} asserts that the administration paths are
 * absent from the customer allowlist, which is the rule. This asserts that the
 * rule is actually enforced by the filter chain, with real signed tokens rather
 * than a mocked security context — the two have come apart before, and the
 * failure mode is silent: a customer able to read every other customer's
 * consultations, or to rewrite the team's working hours.
 *
 * <p>
 * The tokens are minted with the application's own {@link JwtTokenUtil}, so
 * what is being tested is the same code path a browser goes through.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class ConsultationAccessIT {

	/** Every administration endpoint, as the frontend calls them. */
	private static final String[] ADMIN_PATHS = {
			"/admin/consultation/hosts",
			"/admin/consultation/hosts/save",
			"/admin/consultation/hosts/delete",
			"/admin/consultation/types",
			"/admin/consultation/types/save",
			"/admin/consultation/availability",
			"/admin/consultation/availability/saveRule",
			"/admin/consultation/availability/deleteRule",
			"/admin/consultation/availability/saveException",
			"/admin/consultation/availability/deleteException",
			"/admin/consultation/bookings",
			"/admin/consultation/bookings/pending",
			"/admin/consultation/bookings/confirm",
			"/admin/consultation/bookings/decline",
			"/admin/consultation/bookings/cancel",
			"/admin/consultation/bookings/add",
			"/admin/consultation/calendars",
			"/admin/consultation/calendars/connect",
			"/admin/consultation/calendars/disconnect",
			"/admin/consultation/calendars/writeTo",
	};

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private RepositoryUserMaster repositoryUserMaster;

	private static final String ADMIN_EMAIL = "access-check-admin@example.invalid";
	private static final String CUSTOMER_EMAIL = "access-check-customer@example.invalid";

	@BeforeAll
	static void requireDatabase() {
		String url = System.getenv().getOrDefault("TEST_DB_URL",
				"jdbc:postgresql://localhost:5432/diamond_ev_test");
		String user = System.getenv().getOrDefault("TEST_DB_USERNAME", "postgres");
		String password = System.getenv().getOrDefault("TEST_DB_PASSWORD", "postgres");

		try (Connection ignored = DriverManager.getConnection(url, user, password)) {
			// reachable
		} catch (Exception e) {
			Assumptions.abort("No test database at " + url + " — skipping. (" + e.getMessage() + ")");
		}
	}

	/*
	 * Real user rows, because JwtAuthenticationFilter reads the role from the
	 * database rather than from the token claim — deliberately, so that a token
	 * minted before a demotion cannot keep the old privileges and a tampered
	 * claim cannot grant one. A test that only minted tokens would get 401 on
	 * every call and prove nothing about authorisation.
	 */
	@BeforeEach
	void seedUsers() {
		ensureUser(ADMIN_EMAIL, SecurityRoles.ADMIN);
		ensureUser(CUSTOMER_EMAIL, SecurityRoles.USER);
	}

	@AfterAll
	void removeUsers() {
		repositoryUserMaster.findByTxtEmail(ADMIN_EMAIL).ifPresent(repositoryUserMaster::delete);
		repositoryUserMaster.findByTxtEmail(CUSTOMER_EMAIL).ifPresent(repositoryUserMaster::delete);
	}

	private void ensureUser(String email, String role) {
		UserMaster user = repositoryUserMaster.findByTxtEmail(email).orElseGet(UserMaster::new);
		user.setTxtEmail(email);
		user.setTxtName("Access Check");
		user.setTxtRole(role);
		repositoryUserMaster.save(user);
	}

	private String tokenFor(String role) {
		String email = SecurityRoles.ADMIN.equals(role) ? ADMIN_EMAIL : CUSTOMER_EMAIL;
		return jwtTokenUtil.generateToken(1, email, role);
	}

	private int statusOf(String path, String token) throws Exception {
		var request = post(path).contentType(MediaType.APPLICATION_JSON).content("{}");
		if (token != null) {
			request = request.header("Authorization", "Bearer " + token);
		}
		MvcResult result = mockMvc.perform(request).andReturn();
		return result.getResponse().getStatus();
	}

	@Test
	@DisplayName("no consultation administration endpoint answers an unauthenticated caller")
	void administrationRefusesAnonymousCallers() throws Exception {
		for (String path : ADMIN_PATHS) {
			assertThat(statusOf(path, null))
					.as("%s answered an unauthenticated caller", path)
					.isEqualTo(401);
		}
	}

	@Test
	@DisplayName("no consultation administration endpoint answers a signed-in customer")
	void administrationRefusesCustomers() throws Exception {
		/*
		 * The case that matters. A customer has a perfectly valid token — they
		 * signed in through the booking journey — so authentication tells us
		 * nothing here. What has to stop them is authorisation, and if it does
		 * not, they can read every consultation booked by everybody else and
		 * confirm their own requests.
		 */
		String customer = tokenFor(SecurityRoles.USER);

		for (String path : ADMIN_PATHS) {
			assertThat(statusOf(path, customer))
					.as("%s let a signed-in customer through", path)
					.isEqualTo(403);
		}
	}

	@Test
	@DisplayName("an administrator reaches them")
	void administrationAnswersAdministrators() throws Exception {
		// The mirror: default-deny fails silently, so a path that has quietly
		// stopped matching looks exactly like a path that is properly closed.
		int status = statusOf("/admin/consultation/hosts", tokenFor(SecurityRoles.ADMIN));

		assertThat(status)
				.as("an administrator could not reach the consultation screens")
				.isEqualTo(200);
	}

	@Test
	@DisplayName("a customer can see what is on offer and book, but not the diary")
	void customersReachTheirOwnBooking() throws Exception {
		String customer = tokenFor(SecurityRoles.USER);

		for (String path : new String[] { "/consultation/types", "/consultation/slots" }) {
			assertThat(statusOf(path, customer))
					.as("the booking journey could not reach %s", path)
					.isEqualTo(200);
		}
	}

	@Test
	@DisplayName("the OAuth callback is reachable without signing in, and refuses a forged state")
	void theCallbackIsPublicButNotOpen() throws Exception {
		/*
		 * Public of necessity — it is a redirect from Google, so it carries no
		 * bearer token and there is no way to ask for one. That makes the signed
		 * state the only thing standing between it and anybody on the internet,
		 * and the attack it prevents is quiet: complete an OAuth flow against
		 * your own Google account, then hand an administrator a link carrying
		 * that code and a chosen host id. Every consultation booked with that
		 * person would then be written into a calendar you control.
		 */
		MvcResult forged = mockMvc.perform(get("/calendar/oauth/callback")
				.param("code", "a-code-from-the-attackers-own-account")
				.param("state", "1:GOOGLE:9999999999:nonce.forged-signature"))
				.andReturn();

		// Reachable: not a 401, which would mean the real flow could never work.
		assertThat(forged.getResponse().getStatus())
				.as("the callback is not reachable, so no calendar could ever be connected")
				.isNotEqualTo(401);

		// But the forged state got nowhere near connecting anything: the
		// browser is sent back with a failure rather than a success.
		assertThat(forged.getResponse().getHeader("Location"))
				.as("a forged state was not refused")
				.contains("calendar=failed");
	}

	@Test
	@DisplayName("a cancelled consent is not reported as a fault")
	void cancellingConsentIsOrdinary() {
		// Somebody pressing cancel on the consent screen is an ordinary outcome
		// and must not read like something broke.
		try {
			MvcResult cancelled = mockMvc.perform(get("/calendar/oauth/callback")
					.param("error", "access_denied"))
					.andReturn();

			assertThat(cancelled.getResponse().getHeader("Location")).contains("calendar=cancelled");
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	@Test
	@DisplayName("cancelling from an email link needs no sign-in")
	void cancellingByTokenIsPublic() throws Exception {
		/*
		 * Reached from a link in a confirmation email by somebody who may not
		 * have an account at all. The unguessable single-use token in the body
		 * is what authorises it — so the endpoint has to be reachable without a
		 * bearer token, and has to refuse a made-up one.
		 */
		MvcResult result = mockMvc.perform(post("/consultation/cancel")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"txtManagementToken\":\"not-a-real-token\"}"))
				.andReturn();

		assertThat(result.getResponse().getStatus())
				.as("the cancel link is not reachable without signing in")
				.isEqualTo(200);
		assertThat(result.getResponse().getContentAsString())
				.as("a made-up token was accepted")
				.contains("404");
	}
}
