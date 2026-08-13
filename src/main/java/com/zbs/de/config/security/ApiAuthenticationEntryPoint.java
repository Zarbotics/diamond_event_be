package com.zbs.de.config.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Makes unauthenticated and forbidden API calls answer with JSON, not a
 * redirect.
 *
 * <p>
 * Enabling {@code oauth2Login} installs a login entry point that answers an
 * unauthenticated request with a {@code 302} to the OAuth provider. That is
 * right for a browser following links and wrong for an API: the frontend's
 * axios interceptor refreshes the access token when it sees a {@code 401}, and
 * a {@code 302} is followed transparently to an HTML login page, so the refresh
 * never fires and the customer is stuck on a screen that quietly does nothing.
 *
 * <p>
 * Both handlers are deliberately terse. A denial should say that it happened,
 * not why.
 */
@Component
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint, AccessDeniedHandler {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException {
		write(response, HttpStatus.UNAUTHORIZED, "Please sign in to continue.");
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException {
		write(response, HttpStatus.FORBIDDEN, "You do not have access to this.");
	}

	private void write(HttpServletResponse response, HttpStatus status, String message) throws IOException {
		if (response.isCommitted()) {
			return;
		}
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write("{\"code\":" + status.value() + ",\"status\":\"" + status.name()
				+ "\",\"message\":\"" + message + "\"}");
	}
}
