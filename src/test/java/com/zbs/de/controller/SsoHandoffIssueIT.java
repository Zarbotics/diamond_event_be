package com.zbs.de.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.sql.Connection;
import java.sql.DriverManager;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zbs.de.model.UserMaster;
import com.zbs.de.repository.RepositoryUserMaster;
import com.zbs.de.util.JwtTokenUtil;

/**
 * Handing a signed-in session from one application to the other.
 *
 * <h2>What this replaced</h2>
 *
 * The admin portal's "open the client portal" button built a link carrying
 * {@code ?accessToken=…&refreshToken=…} out of localStorage, so that somebody
 * taking a booking over the telephone could walk the customer's own screens.
 * Those are an administrator's credentials, and a URL is not a private place:
 * browser history, server access logs, every proxy in between, and the
 * {@code Referer} header of the next request the page makes. A leaked refresh
 * token is a standing key to the back office that nothing revokes.
 *
 * <p>
 * It now asks for the same single-use code the SSO redirect uses, which is
 * redeemed over POST and destroyed on use.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class SsoHandoffIssueIT {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private RepositoryUserMaster repositoryUserMaster;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	private final ObjectMapper json = new ObjectMapper();

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

	/** A real signed token for somebody who actually exists. */
	private String tokenForSomebody() {
		UserMaster user = repositoryUserMaster.findAll().stream().findFirst().orElse(null);
		Assumptions.assumeTrue(user != null, "no users seeded — skipping");

		return jwtTokenUtil.generateToken(user.getSerUserId().intValue(), user.getTxtEmail(), user.getTxtRole());
	}

	private MvcResult askForACode(String bearer) throws Exception {
		var request = post("/auth/handoff").contentType(MediaType.APPLICATION_JSON).content("{}");
		if (bearer != null) {
			request = request.header("Authorization", "Bearer " + bearer);
		}
		return mockMvc.perform(request).andReturn();
	}

	@Test
	@DisplayName("a signed-in caller gets a one-time code")
	void aSignedInCallerGetsACode() throws Exception {
		MvcResult result = askForACode(tokenForSomebody());

		assertThat(result.getResponse().getStatus()).isEqualTo(200);

		String code = json.readTree(result.getResponse().getContentAsString()).path("code").asText();
		assertThat(code).as("no code was issued, so the portal button cannot work").isNotBlank();
	}

	@Test
	@DisplayName("a signed-out caller gets nothing")
	void aSignedOutCallerGetsNothing() throws Exception {
		/*
		 * The whole point of carving this out of the otherwise-public /auth/**.
		 * A public endpoint that mints a signed-in session on request is an open
		 * door, and it would be an easy one to leave open by accident — the
		 * wildcard above it already covers the path.
		 */
		MvcResult result = askForACode(null);

		assertThat(result.getResponse().getStatus())
				.as("anybody can mint a sign-in code without being signed in")
				.isIn(401, 403);
	}

	@Test
	@DisplayName("the code is exchangeable exactly once")
	void theCodeIsSingleUse() throws Exception {
		// Same property the SSO handoff relies on. A code that could be redeemed
		// twice would still be in browser history after the first use.
		String code = json.readTree(askForACode(tokenForSomebody()).getResponse().getContentAsString())
				.path("code").asText();

		MvcResult first = mockMvc.perform(post("/auth/exchange")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"code\":\"" + code + "\"}")).andReturn();

		assertThat(first.getResponse().getStatus()).isEqualTo(200);
		assertThat(json.readTree(first.getResponse().getContentAsString()).path("accessToken").asText())
				.isNotBlank();

		MvcResult second = mockMvc.perform(post("/auth/exchange")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"code\":\"" + code + "\"}")).andReturn();

		assertThat(second.getResponse().getStatus())
				.as("the same handoff code was redeemed twice")
				.isEqualTo(400);
	}

	@Test
	@DisplayName("each request gets a different code")
	void codesAreNotReused() throws Exception {
		// A code that repeated for the same person would be as durable as the
		// token it replaced, and just as present in browser history.
		String bearer = tokenForSomebody();

		String first = json.readTree(askForACode(bearer).getResponse().getContentAsString()).path("code").asText();
		String second = json.readTree(askForACode(bearer).getResponse().getContentAsString()).path("code").asText();

		assertThat(first).isNotEqualTo(second);
	}
}
