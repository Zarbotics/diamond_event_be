package com.zbs.de.config.security;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Refuses to start a sign-in the provider cannot possibly complete.
 *
 * <p>
 * Google's client id and secret have development placeholders, so the
 * application starts on a machine that has no credentials for them. That is
 * deliberate — somebody working on the booking journey should not need a Google
 * project — but it meant pressing "Continue with Google" sent the browser to
 * Google with a client id of {@code local-dev-google-client-id}, and Google
 * answered with its own page:
 *
 * <pre>
 *   The OAuth client was not found.
 *   Error 401: invalid_client
 * </pre>
 *
 * That is Google being entirely correct and completely unhelpful. Nothing in it
 * says the cause is an unset environment variable on the backend you are
 * running, so it reads as a broken application or a misconfigured Google
 * project — and the one party that knew the truth, this backend, had already
 * handed the problem to somebody who could not diagnose it.
 *
 * <p>
 * Failing at startup was the alternative and it is worse: it would stop the
 * whole application booting over a feature most work never touches, which is
 * the reason the placeholders exist at all. So the check belongs here, at the
 * moment the credential is first needed, where it can say what is actually
 * wrong.
 */
@Component
public class UnconfiguredProviderFilter extends OncePerRequestFilter {

	/**
	 * The placeholders from {@code application.properties}. Matching the literal
	 * keeps the check honest — it cannot mistake a genuine misconfiguration, or
	 * a real credential that happens to be rejected, for an absent one.
	 */
	private static final Map<String, String> PLACEHOLDERS = Map.of(
			"google", "local-dev-google-client-id");

	@Value("${spring.security.oauth2.client.registration.google.client-id:}")
	private String googleClientId;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		String provider = providerBeingStarted(request);

		if (provider != null && isPlaceholder(provider)) {
			explain(request, response, provider);
			return;
		}

		chain.doFilter(request, response);
	}

	/** The provider named by {@code /oauth2/authorization/{provider}}, or null. */
	private String providerBeingStarted(HttpServletRequest request) {
		String path = request.getRequestURI().substring(request.getContextPath().length());
		String prefix = "/oauth2/authorization/";
		if (!path.startsWith(prefix)) {
			return null;
		}
		String provider = path.substring(prefix.length());
		return provider.isEmpty() ? null : provider;
	}

	private boolean isPlaceholder(String provider) {
		String placeholder = PLACEHOLDERS.get(provider);
		return placeholder != null && placeholder.equals(googleClientId);
	}

	/**
	 * Plain text rather than JSON: this lands in the address bar of whoever
	 * pressed the button, so it is read by a person and not parsed by the
	 * frontend.
	 */
	private void explain(HttpServletRequest request, HttpServletResponse response, String provider)
			throws IOException {
		response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
		response.setContentType(MediaType.TEXT_PLAIN_VALUE);
		response.setCharacterEncoding("UTF-8");

		String message = "Sign-in with " + provider + " is not configured on this backend.\n\n"
				+ "It is still using the development placeholder for the client id, so\n"
				+ "sending you to " + provider + " would only get you its \"invalid_client\" page.\n\n"
				+ "Set both of these and restart:\n\n"
				+ "  GOOGLE_CLIENT_ID\n"
				+ "  GOOGLE_CLIENT_SECRET\n\n"
				+ "The authorised redirect URI on the Google client must be exactly:\n\n"
				+ "  " + redirectUriFor(request, provider) + "\n\n"
				+ "See RUNNING.md in diamond_event_be for the whole setup.\n";

		response.getWriter().write(message);
	}

	/**
	 * Built from the request that arrived rather than from a literal. The port
	 * and the context path are both configurable, and telling somebody to
	 * register a URI this backend is not serving would cost more time than
	 * saying nothing at all.
	 */
	private String redirectUriFor(HttpServletRequest request, String provider) {
		return ServletUriComponentsBuilder.fromContextPath(request)
				.path("/login/oauth2/code/" + provider)
				.toUriString();
	}
}
