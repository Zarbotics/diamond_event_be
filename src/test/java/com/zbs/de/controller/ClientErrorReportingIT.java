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

/**
 * Where a browser failure goes.
 *
 * <p>
 * This endpoint is reachable without signing in, which is deliberate — the
 * failures most worth hearing about are the ones that stop somebody signing in
 * — and which makes it a way for anybody on the internet to write into the
 * application log. What is asserted here is that the bounds on that hold.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
		"spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/diamond_ev_test}",
		"spring.datasource.username=${TEST_DB_USERNAME:postgres}",
		"spring.datasource.password=${TEST_DB_PASSWORD:postgres}",
		"server.ssl.enabled=false",
})
class ClientErrorReportingIT {

	@Autowired
	private MockMvc mockMvc;

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

	private MvcResult report(String json) throws Exception {
		return mockMvc.perform(post("/clientError")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
				.andReturn();
	}

	@Test
	@DisplayName("a signed-out browser can report a failure")
	void reachableWithoutSigningIn() throws Exception {
		/*
		 * The point of it being public. A customer who cannot sign in is having
		 * exactly the kind of failure worth knowing about, and a report that
		 * required a session would miss every one of them.
		 */
		MvcResult result = report("""
				{"txtApp":"customer-journey","txtPath":"/booking/signIn","txtMessage":"boom"}""");

		assertThat(result.getResponse().getStatus())
				.as("a signed-out browser cannot report the failure that stopped it signing in")
				.isEqualTo(200);
	}

	@Test
	@DisplayName("the reply carries a reference somebody can quote")
	void theReplyCarriesAReference() throws Exception {
		// So that a customer on the telephone at the time can read out eight
		// characters and have the entry found, rather than describing a screen.
		MvcResult result = report("""
				{"txtApp":"admin-portal","txtMessage":"boom"}""");

		assertThat(result.getResponse().getContentAsString())
				.containsPattern("[2-9BCDFGHJKLMNPQRSTVWXYZ]{8}");
	}

	@Test
	@DisplayName("a report cannot forge log lines around itself")
	void newlinesCannotForgeLogEntries() throws Exception {
		/*
		 * The attack this endpoint would otherwise open. A message containing a
		 * newline and a plausible timestamp is a fake log entry, and log entries
		 * are what people trust when working out what happened. Accepted and
		 * flattened rather than refused: a client cannot act on a refusal, and
		 * refusing would lose the genuine reports that merely contain a stray
		 * newline.
		 */
		MvcResult result = report("""
				{"txtMessage":"innocent\\n2026-08-21 12:00:00 ERROR Payment reversed by admin"}""");

		assertThat(result.getResponse().getStatus()).isEqualTo(200);
	}

	@Test
	@DisplayName("an enormous report is accepted and cut down, not refused")
	void anEnormousReportIsTruncated() throws Exception {
		// A client that got a 413 back would have nothing useful to do with it.
		String huge = "x".repeat(50_000);
		MvcResult result = report("{\"txtMessage\":\"" + huge + "\"}");

		assertThat(result.getResponse().getStatus()).isEqualTo(200);
	}

	@Test
	@DisplayName("a flood is absorbed quietly rather than refused")
	void aFloodIsAbsorbed() throws Exception {
		/*
		 * A render loop can report thousands of times a second. The ceiling stops
		 * that filling the disk — and it answers 200 throughout, because a client
		 * being throttled must not start retrying, and the hundred-and-first
		 * report tells you nothing the first ten did not.
		 */
		for (int i = 0; i < 120; i++) {
			report("{\"txtMessage\":\"flood " + i + "\"}");
		}

		assertThat(report("{\"txtMessage\":\"one more\"}").getResponse().getStatus())
				.as("a throttled client was refused, which will make it retry")
				.isEqualTo(200);
	}
}
