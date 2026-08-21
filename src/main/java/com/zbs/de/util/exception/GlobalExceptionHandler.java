package com.zbs.de.util.exception;

import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.zbs.de.util.ResponseMessage;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;

/**
 * Translates exceptions into HTTP responses.
 *
 * <p>
 * Two rules govern everything here.
 *
 * <p>
 * <strong>Security exceptions must not be swallowed.</strong>
 * {@code AccessDeniedException} is a {@code RuntimeException}, so the previous
 * catch-all turned every authorisation failure into a 500. That hid real denials
 * from monitoring and told the caller nothing useful. The specific handlers
 * below now take precedence.
 *
 * <p>
 * <strong>Internal detail stays internal.</strong> Raw exception messages carry
 * table names, SQL fragments and file paths. They are logged in full and
 * replaced in the response by something a user can act on.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ResponseMessage> handleAccessDenied(AccessDeniedException ex) {
		// Expected in normal operation when someone probes another customer's data.
		// Logged at WARN without a stack trace so it is greppable but not noise.
		LOGGER.warn("Access denied: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseMessage(HttpStatus.FORBIDDEN.value(),
				HttpStatus.FORBIDDEN, ex.getMessage()));
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ResponseMessage> handleAuthentication(AuthenticationException ex) {
		LOGGER.warn("Authentication failed: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseMessage(HttpStatus.UNAUTHORIZED.value(),
				HttpStatus.UNAUTHORIZED, "Please sign in again."));
	}

	@ExceptionHandler({ EntityNotFoundException.class, NotFoundException.class })
	public ResponseEntity<ResponseMessage> handleNotFound(RuntimeException ex) {
		LOGGER.debug("Not found: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND, ex.getMessage()));
	}

	/**
	 * Bean-validation failures on a request body.
	 *
	 * <p>
	 * Returns every violation keyed by field name rather than only the first, so the
	 * frontend can render each message inline against the control that caused it
	 * instead of showing one transient toast.
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ResponseMessage> handleValidation(MethodArgumentNotValidException ex) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		ex.getBindingResult().getFieldErrors()
				.forEach(error -> fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));
		ex.getBindingResult().getGlobalErrors()
				.forEach(error -> fieldErrors.putIfAbsent(error.getObjectName(), error.getDefaultMessage()));

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST, "Please check the highlighted fields.", fieldErrors));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ResponseMessage> handleConstraintViolation(ConstraintViolationException ex) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		ex.getConstraintViolations()
				.forEach(v -> fieldErrors.putIfAbsent(v.getPropertyPath().toString(), v.getMessage()));

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST, "Please check the highlighted fields.", fieldErrors));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ResponseMessage> handleIllegalArgument(IllegalArgumentException ex) {
		LOGGER.debug("Bad request: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, ex.getMessage()));
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ResponseMessage> handleMaxSizeException(MaxUploadSizeExceededException ex) {
		return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(new ResponseMessage(
				HttpStatus.PAYLOAD_TOO_LARGE.value(), HttpStatus.PAYLOAD_TOO_LARGE, "File must not exceed 3MB"));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ResponseMessage> handleAllOtherExceptions(Exception ex) {
		/*
		 * A reference, so that "something went wrong" can be followed up.
		 *
		 * Without one, a customer reporting a failure gives you a screen and an
		 * approximate time, and finding the matching stack trace in a day's log is
		 * guesswork. With one they read out eight characters and it is a single
		 * grep.
		 */
		String reference = errorReference();

		// Full detail to the log, nothing internal to the caller.
		LOGGER.error("Unhandled exception [ref={}]", reference, ex);

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
						"Something went wrong at our end. Please try again, or contact us quoting reference "
								+ reference + "."));
	}

	/**
	 * Eight characters somebody can read out over the telephone.
	 *
	 * <p>
	 * No vowels, so it cannot accidentally spell a word; no 0, O, 1 or I, so
	 * nothing in it is ambiguous spoken aloud or written down in a hurry.
	 */
	public static String errorReference() {
		final String alphabet = "23456789BCDFGHJKLMNPQRSTVWXYZ";
		StringBuilder reference = new StringBuilder(8);
		for (int i = 0; i < 8; i++) {
			reference.append(alphabet.charAt(RANDOM.nextInt(alphabet.length())));
		}
		return reference.toString();
	}

	private static final SecureRandom RANDOM = new SecureRandom();
}
