package com.zbs.de.service.calendar;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The HTTP calls the calendar adapters make.
 *
 * <p>
 * Java's own {@code HttpClient} rather than the Google and Microsoft SDKs. Each
 * SDK brings a large dependency tree, its own HTTP stack and its own auth
 * abstractions, in exchange for four REST calls per provider that are perfectly
 * readable written out.
 *
 * <p>
 * A separate bean rather than a static helper so that a test can replace it and
 * exercise an adapter's request-building and response-reading without a network
 * — which is the part worth testing, and the only part that can be tested
 * without real credentials.
 *
 * <h2>Timeouts are the point</h2>
 *
 * Both a connect and a request timeout, and they are short. This runs while
 * confirming a consultation, and a provider that accepts the connection then
 * never answers would otherwise hold a request thread until the container gives
 * up. The caller treats a timeout as an ordinary failure and the booking stands
 * regardless, so failing quickly costs nothing and waiting costs a thread.
 */
@Component
public class CalendarHttp {

	private static final ObjectMapper JSON = new ObjectMapper();

	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

	private final HttpClient client = HttpClient.newBuilder()
			.connectTimeout(CONNECT_TIMEOUT)
			// Google and Microsoft both redirect between regional endpoints.
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();

	/** A provider said no, or could not be reached. Always caught by the caller. */
	public static class CalendarApiException extends RuntimeException {
		public CalendarApiException(String message) {
			super(message);
		}

		public CalendarApiException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	public JsonNode postJson(String url, String bearerToken, String body) {
		return send(request(url, bearerToken)
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
				.build());
	}

	public JsonNode postForm(String url, String bearerToken, String body) {
		return send(request(url, bearerToken)
				.header("Content-Type", "application/x-www-form-urlencoded")
				.POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
				.build());
	}

	public JsonNode getJson(String url, String bearerToken) {
		return send(request(url, bearerToken).GET().build());
	}

	/**
	 * Deletes, treating "already gone" as success.
	 *
	 * <p>
	 * 404 and 410 mean the event is not there, which is the state we were trying
	 * to reach. The host may well have deleted it themselves, and that is
	 * agreement rather than an error.
	 */
	public void delete(String url, String bearerToken) {
		HttpResponse<String> response = exchange(request(url, bearerToken).DELETE().build());

		int status = response.statusCode();
		if (status == 404 || status == 410 || (status >= 200 && status < 300)) {
			return;
		}
		throw new CalendarApiException(describeFailure(response));
	}

	// -----------------------------------------------------------------

	private HttpRequest.Builder request(String url, String bearerToken) {
		HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
				.timeout(REQUEST_TIMEOUT)
				.header("Accept", "application/json");

		if (bearerToken != null) {
			builder.header("Authorization", "Bearer " + bearerToken);
		}
		return builder;
	}

	private JsonNode send(HttpRequest request) {
		HttpResponse<String> response = exchange(request);

		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			throw new CalendarApiException(describeFailure(response));
		}
		try {
			return JSON.readTree(response.body());
		} catch (Exception e) {
			throw new CalendarApiException("The calendar provider sent something that is not JSON");
		}
	}

	private HttpResponse<String> exchange(HttpRequest request) {
		try {
			return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new CalendarApiException("Interrupted while calling the calendar provider", e);
		} catch (Exception e) {
			throw new CalendarApiException(
					"Could not reach the calendar provider: " + e.getMessage(), e);
		}
	}

	/**
	 * What went wrong, in a form safe to keep.
	 *
	 * <p>
	 * The status and the provider's own message, truncated. The message is
	 * stored on the booking for somebody to read later, and a full response body
	 * can be long and can echo request content — so it is trimmed rather than
	 * kept whole.
	 */
	private String describeFailure(HttpResponse<String> response) {
		String detail = "";
		try {
			JsonNode error = JSON.readTree(response.body()).path("error");
			detail = error.isTextual()
					? error.asText()
					: error.path("message").asText(error.path("error_description").asText(""));
		} catch (Exception ignored) {
			// Not JSON. The status alone is still worth reporting.
		}

		if (detail.length() > 300) {
			detail = detail.substring(0, 300) + "…";
		}

		String hint = switch (response.statusCode()) {
			// The two worth naming, because the remedy is a person reconnecting
			// rather than a retry that will never succeed.
			case 401, 403 -> " — this calendar probably needs reconnecting";
			case 429 -> " — too many requests; this will be retried";
			default -> "";
		};

		return "The calendar provider returned " + response.statusCode()
				+ (detail.isBlank() ? "" : ": " + detail) + hint;
	}

	// -----------------------------------------------------------------

	/** A JSON string literal, with the escaping done properly. */
	public static String quote(String value) {
		if (value == null) {
			return "null";
		}
		try {
			return JSON.writeValueAsString(value);
		} catch (Exception e) {
			throw new CalendarApiException("Could not encode a value for the calendar provider");
		}
	}

	/**
	 * A path segment.
	 *
	 * <p>
	 * Calendar ids are email addresses often enough that this matters: an
	 * unescaped {@code @} or {@code +} in a path is a different URL from the one
	 * intended.
	 */
	public static String encodePath(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
	}

	public static String encodeForm(String value) {
		return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
	}
}
