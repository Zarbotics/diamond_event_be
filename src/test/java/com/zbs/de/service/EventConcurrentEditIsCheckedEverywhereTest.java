package com.zbs.de.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Every method that updates an existing booking must check who else has.
 *
 * <h2>Why structurally, and not by review</h2>
 *
 * The same reason as {@code EventDateCapacityIsCheckedEverywhereTest}, and the
 * same file. There are four update branches in
 * {@code ServiceEventMasterImpl}, hundreds of lines each and near-identical to
 * one another, and one of them carries a comment saying that any change made in
 * it must be copied by hand into its twin. The capacity rule was already
 * missing from one of the four before anybody noticed, and the evidence turned
 * up in production data rather than in a review.
 *
 * <p>
 * A missing concurrency check leaves no evidence at all. There is no wrong row
 * to find later — only somebody's changes that are not there, and no way to
 * tell that they ever were.
 */
class EventConcurrentEditIsCheckedEverywhereTest {

	private static final Path SERVICE = Path.of(
			"src/main/java/com/zbs/de/service/impl/ServiceEventMasterImpl.java");

	/**
	 * Taking hold of an existing booking in order to change it.
	 *
	 * <p>
	 * This is the moment that matters: from here the method writes the caller's
	 * values over whatever is stored, and if somebody else has saved in the
	 * meantime their work goes with it.
	 */
	private static final Pattern UPDATES_AN_EXISTING_EVENT = Pattern.compile(
			"entity = optionalExisting\\.get\\(\\)");

	private static final Pattern CHECKS_THE_VERSION = Pattern.compile("hasChangedElsewhere\\(");

	/** A method, and the source between its signature and the next one. */
	private record Method(String name, String body) {
	}

	@Test
	@DisplayName("no method overwrites an existing booking without checking who else has saved it")
	void everyUpdateChecksTheVersion() throws IOException {
		List<Method> methods = methodsOf(SERVICE);

		assertThat(methods)
				.as("no methods were parsed, so this test is asserting nothing")
				.isNotEmpty();

		Set<String> unchecked = new LinkedHashSet<>();
		for (Method method : methods) {
			if (method.name().equals("hasChangedElsewhere")) {
				continue;
			}
			if (UPDATES_AN_EXISTING_EVENT.matcher(method.body()).find()
					&& !CHECKS_THE_VERSION.matcher(method.body()).find()) {
				unchecked.add(method.name());
			}
		}

		assertThat(unchecked)
				.as("""
						These methods write over an existing booking without calling \
						hasChangedElsewhere, so an administrator and a customer editing the same \
						booking will silently overwrite one another — one set of changes gone, \
						nothing said anywhere. Check the version before applying the caller's \
						values and refuse the save when it has moved; see saveAndUpdate for the \
						shape.""")
				.isEmpty();
	}

	@Test
	@DisplayName("the check is reached from more than one place, so the scan is finding real calls")
	void theScanFindsRealCalls() throws IOException {
		/*
		 * Guards the guard. A rename or a reformat that quietly stopped the regex
		 * matching would make the test above pass by finding nothing at all,
		 * which is the failure mode of every structural test.
		 */
		List<Method> methods = methodsOf(SERVICE);

		long updating = methods.stream()
				.filter(m -> UPDATES_AN_EXISTING_EVENT.matcher(m.body()).find())
				.count();
		long checking = methods.stream()
				.filter(m -> !m.name().equals("hasChangedElsewhere"))
				.filter(m -> CHECKS_THE_VERSION.matcher(m.body()).find())
				.count();

		assertThat(updating).as("found no method that updates an existing booking").isPositive();
		assertThat(checking).as("found no method that checks the version").isPositive();
	}

	// -----------------------------------------------------------------

	/**
	 * Splits the file at method signatures.
	 *
	 * <p>
	 * Crude on purpose, and the same approach as the capacity test next to it. A
	 * real parser would be a dependency and a great deal of machinery to answer
	 * one question about one file, and the thing being detected — a whole call
	 * being absent from a method — survives the imprecision comfortably.
	 */
	private List<Method> methodsOf(Path file) throws IOException {
		String source = Files.readString(file);

		Pattern signature = Pattern.compile(
				"\\n\\t(?:public|private|protected)\\s+[\\w<>,\\[\\]\\. ]+?\\s+(\\w+)\\s*\\(");

		Matcher matcher = signature.matcher(source);
		List<Integer> starts = new ArrayList<>();
		List<String> names = new ArrayList<>();

		while (matcher.find()) {
			starts.add(matcher.start());
			names.add(matcher.group(1));
		}

		List<Method> methods = new ArrayList<>();
		for (int i = 0; i < starts.size(); i++) {
			int end = (i + 1 < starts.size()) ? starts.get(i + 1) : source.length();
			methods.add(new Method(names.get(i), source.substring(starts.get(i), end)));
		}
		return methods;
	}
}
