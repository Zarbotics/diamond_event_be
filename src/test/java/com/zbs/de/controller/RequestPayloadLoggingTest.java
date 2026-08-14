package com.zbs.de.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * No controller may log a raw request body.
 *
 * <p>
 * The multipart endpoints take their payload as a {@code String} and parse it
 * themselves, and several of them logged that string whole. On the booking
 * endpoint that was around eight kilobytes per save and twelve saves per
 * booking, carrying the customer's name, their contact's name and phone
 * number, the couple's names and the entire menu. On the payment endpoints it
 * carried the transaction reference and a free-text remarks field.
 *
 * <p>
 * Personal data in an application log is processing nobody has accounted for.
 * Logs are shipped to aggregators, copied onto laptops and kept indefinitely,
 * none of it covered by the retention that applies to the database — and none
 * of it is what anyone reading a log actually wants. An identifier is.
 *
 * <p>
 * This is a source check rather than a behavioural one because the fault is a
 * shape, not an outcome: nothing fails, nothing is slow, and the only symptom
 * is in a file nobody reads until it matters. The next {@code saveWithDocs}
 * added to this codebase will follow the ones already here, so the guard has
 * to sit where the pattern is copied from.
 */
class RequestPayloadLoggingTest {

	private static final Path CONTROLLERS =
			Path.of("src/main/java/com/zbs/de/controller");

	/** A parameter that arrives as an unparsed request body. */
	private static final Pattern RAW_BODY_PARAM = Pattern.compile(
			"@RequestPart\\(\\s*(?:value\\s*=\\s*)?\"[^\"]+\"\\s*\\)\\s*String\\s+(\\w+)");

	@Test
	@DisplayName("no controller writes a raw request body to the log")
	void noControllerLogsARawRequestBody() throws IOException {
		List<String> offenders = new ArrayList<>();

		try (Stream<Path> sources = Files.walk(CONTROLLERS)) {
			for (Path source : sources.filter(p -> p.toString().endsWith(".java")).toList()) {
				String text = Files.readString(source);

				Matcher parameter = RAW_BODY_PARAM.matcher(text);
				while (parameter.find()) {
					String name = parameter.group(1);

					/*
					 * Any log call naming that parameter. Deliberately not
					 * restricted to LOGGER.info: a body at DEBUG still reaches
					 * a log file wherever debug is on, which on a machine
					 * somebody is debugging is exactly where the real bookings
					 * are.
					 */
					Pattern logged = Pattern.compile(
							"LOGGER\\.(?:trace|debug|info|warn|error)\\([^;]*\\b" + name + "\\b[^;]*\\)");

					Matcher call = logged.matcher(text);
					while (call.find()) {
						/*
						 * Only the arguments count. The message itself is
						 * allowed to contain the word — "Recording a {} payment
						 * of {}" is not logging the parameter named `payment`,
						 * and a check that cannot tell the difference gets
						 * switched off the first time it cries wolf.
						 */
						String arguments = call.group().replaceAll("\"(?:\\\\.|[^\"\\\\])*\"", "");
						if (!Pattern.compile("\\b" + name + "\\b").matcher(arguments).find()) {
							continue;
						}
						offenders.add(source.getFileName() + " line "
								+ (text.substring(0, call.start()).split("\n", -1).length)
								+ ": " + call.group().trim());
					}
				}
			}
		}

		assertThat(offenders)
				.as("these log the request body itself. Log what identifies it — an id, "
						+ "a reference, an amount — and parse before logging if you need one.")
				.isEmpty();
	}
}
