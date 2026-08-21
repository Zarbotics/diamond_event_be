package com.zbs.de.controller;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zbs.de.model.dto.DtoClientError;
import com.zbs.de.util.ResponseMessage;
import com.zbs.de.util.exception.GlobalExceptionHandler;

/**
 * Somewhere for a browser error to go.
 *
 * <h2>Why this exists</h2>
 *
 * A failure on the server leaves a stack trace in a log somebody can search. A
 * failure in the browser leaves a line in a console on a customer's phone, and
 * then it is gone. The only evidence that reaches the business is a customer
 * saying the site did not work, weeks later, on a device nobody can reproduce.
 *
 * <p>
 * Both frontends now report what they catch here, so a broken screen shows up
 * in the same log as everything else, with a reference the customer can be given
 * if they are on the telephone at the time.
 *
 * <h2>What is deliberately guarded</h2>
 *
 * This is reachable without signing in — an error on the sign-in screen is
 * exactly the kind worth knowing about — which makes it a way for anybody to
 * write into the application log. Three things bound that:
 *
 * <ul>
 * <li>every field is truncated, so a report cannot be a megabyte;</li>
 * <li>newlines and control characters are stripped, so a report cannot forge
 * additional log lines around itself;</li>
 * <li>reports are counted per minute across the whole application and dropped
 * above a ceiling, so a script cannot fill the disk.</li>
 * </ul>
 */
@RestController
@RequestMapping("/clientError")
public class ControllerClientError {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerClientError.class);

	/** Enough for a message and a stack, nowhere near enough to be a payload. */
	private static final int MAX_MESSAGE = 500;
	private static final int MAX_STACK = 2_000;
	private static final int MAX_SHORT = 200;

	/**
	 * How many reports to accept in a minute, across everybody.
	 *
	 * <p>
	 * A genuinely broken screen produces a handful. A hundred in a minute is
	 * either a loop in our own code or somebody having fun, and in both cases the
	 * hundred-and-first tells you nothing the first ten did not.
	 */
	private static final int PER_MINUTE = 60;

	private final AtomicLong windowStartedAt = new AtomicLong(System.currentTimeMillis());
	private final AtomicInteger inThisWindow = new AtomicInteger();

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage report(@RequestBody DtoClientError error) {
		String reference = GlobalExceptionHandler.errorReference();

		if (!withinTheLimit()) {
			// Silently accepted rather than refused: a client being throttled must
			// not start retrying, and the browser can do nothing useful about it.
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Noted", reference);
		}

		LOGGER.error("Client error [ref={}] on {} at {}: {} | agent={} | stack={}",
				reference,
				clean(error.getTxtApp(), MAX_SHORT),
				clean(error.getTxtPath(), MAX_SHORT),
				clean(error.getTxtMessage(), MAX_MESSAGE),
				clean(error.getTxtUserAgent(), MAX_SHORT),
				clean(error.getTxtStack(), MAX_STACK));

		return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Noted", reference);
	}

	/**
	 * Strips anything that would let a report forge log structure around itself.
	 *
	 * <p>
	 * A message containing a newline and a plausible-looking timestamp is a fake
	 * log entry, and log entries are what people trust when working out what
	 * happened.
	 */
	private String clean(String value, int limit) {
		if (value == null) {
			return "";
		}
		String flattened = value.replaceAll("[\\p{Cntrl}]+", " ").trim();
		return flattened.length() <= limit ? flattened : flattened.substring(0, limit) + "…";
	}

	private boolean withinTheLimit() {
		long now = System.currentTimeMillis();
		long startedAt = windowStartedAt.get();

		if (now - startedAt > 60_000 && windowStartedAt.compareAndSet(startedAt, now)) {
			inThisWindow.set(0);
		}

		return inThisWindow.incrementAndGet() <= PER_MINUTE;
	}
}
