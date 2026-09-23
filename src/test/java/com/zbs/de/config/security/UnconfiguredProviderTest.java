package com.zbs.de.config.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * An unconfigured provider has to be explained here, not by the provider.
 *
 * <p>
 * The application starts without Google credentials on purpose, so pressing
 * "Continue with Google" used to send the browser to Google carrying the
 * placeholder client id. Google replied with {@code Error 401: invalid_client}
 * and "The OAuth client was not found" — correct, and no help at all to
 * somebody who has no idea an environment variable on their own backend is the
 * cause.
 */
class UnconfiguredProviderTest {

	private static final String PLACEHOLDER = "local-dev-google-client-id";

	private UnconfiguredProviderFilter filterWithClientId(String clientId) {
		UnconfiguredProviderFilter filter = new UnconfiguredProviderFilter();
		ReflectionTestUtils.setField(filter, "googleClientId", clientId);
		return filter;
	}

	private MockHttpServletRequest startSignIn(String provider) {
		MockHttpServletRequest request = new MockHttpServletRequest("GET",
				"/diamond/oauth2/authorization/" + provider);
		request.setContextPath("/diamond");
		request.setServerName("localhost");
		request.setServerPort(8080);
		request.setScheme("http");
		return request;
	}

	// -----------------------------------------------------------------

	@Test
	@DisplayName("a placeholder credential is explained rather than sent to the provider")
	void placeholderCredentialsAreExplained() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		filterWithClientId(PLACEHOLDER).doFilter(startSignIn("google"), response, chain);

		assertThat(response.getStatus()).isEqualTo(503);
		assertThat(chain.getRequest())
				.as("the request must not reach the redirect filter, or the browser has already gone")
				.isNull();

		String body = response.getContentAsString();
		assertThat(body)
				.contains("not configured")
				.contains("GOOGLE_CLIENT_ID")
				.contains("GOOGLE_CLIENT_SECRET")
				.contains("RUNNING.md");
	}

	@Test
	@DisplayName("the redirect URI it names is the one this backend actually serves")
	void theRedirectUriComesFromTheRequest() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();

		filterWithClientId(PLACEHOLDER).doFilter(startSignIn("google"), response, new MockFilterChain());

		// Built from the request, because the port and context path are both
		// configurable. Naming a URI the backend does not serve would send
		// somebody to register the wrong thing in the Google console.
		assertThat(response.getContentAsString())
				.contains("http://localhost:8080/diamond/login/oauth2/code/google");
	}

	@Test
	@DisplayName("a real credential is left alone")
	void configuredCredentialsPassThrough() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		filterWithClientId("1234567890-abcdefg.apps.googleusercontent.com")
				.doFilter(startSignIn("google"), response, chain);

		assertThat(chain.getRequest())
				.as("a configured provider must reach the OAuth redirect filter")
				.isNotNull();
		assertThat(response.getStatus()).isEqualTo(200);
	}

	@Test
	@DisplayName("requests that are not starting a sign-in are untouched")
	void otherRequestsPassThrough() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		MockHttpServletRequest ordinary = new MockHttpServletRequest("POST", "/diamond/eventMaster/getAllData");
		ordinary.setContextPath("/diamond");

		filterWithClientId(PLACEHOLDER).doFilter(ordinary, response, chain);

		assertThat(chain.getRequest()).isNotNull();
	}

	@Test
	@DisplayName("a provider with no placeholder registered is not second-guessed")
	void unknownProvidersPassThrough() throws Exception {
		// Apple's credentials are checked by their own configuration; this filter
		// only knows about placeholders it can recognise, and should stay out of
		// the way of anything else rather than guessing.
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		filterWithClientId(PLACEHOLDER).doFilter(startSignIn("apple"), response, chain);

		assertThat(chain.getRequest()).isNotNull();
	}
}
