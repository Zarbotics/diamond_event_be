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
 * Every method that gives an event a date must ask whether the day has room.
 *
 * <h2>Why this is a test and not a note in a review</h2>
 *
 * The capacity rule had five save methods to be applied in and was applied in
 * four. The fifth, {@code saveAndUpdate}, wrote the date and never asked — and
 * nothing anywhere said so. It is reachable through
 * {@code /eventMaster/saveOrUpdate}, and production carries the result: three
 * events on Friday 1 May 2026, on a day that holds two, entered days apart.
 *
 * <p>
 * That is not the kind of mistake code review reliably catches. These methods
 * are hundreds of lines long, nearly identical to one another, and one of them
 * carries a comment saying that changes made in it must be copied by hand into
 * its twin. The next one added will be a copy of one of these, and whether it
 * keeps the check is a matter of which one was copied.
 *
 * <p>
 * So the rule is asserted structurally instead: if a method writes an event
 * date, it must also call {@code canBookEvent}. Adding a sixth save path
 * without the check fails the build rather than reaching production and being
 * found in the data months later.
 *
 * <h2>What this deliberately does not do</h2>
 *
 * It does not check that the call is correct, ordered before the write, or
 * acted upon — a structural test cannot, and {@code EventDateCapacityIT}
 * exercises the behaviour against a real database. This catches the specific
 * failure that actually happened: the check being absent altogether.
 */
class EventDateCapacityIsCheckedEverywhereTest {

	private static final Path SERVICE = Path.of(
			"src/main/java/com/zbs/de/service/impl/ServiceEventMasterImpl.java");

	/**
	 * Ways an event ends up with a date.
	 *
	 * <p>
	 * Both matter. The update branches set it directly; the create branches go
	 * through the mapper, which is why a search for the setter alone found four
	 * of the six places and missed every creation.
	 */
	private static final Pattern WRITES_A_DATE = Pattern.compile(
			"entity\\.setDteEventDate\\(|MapperEventMaster\\.toEntity\\(");

	private static final Pattern ASKS_FIRST = Pattern.compile("canBookEvent\\(");

	/** A method, and the source between its signature and the next one. */
	private record Method(String name, String body) {
	}

	@Test
	@DisplayName("no method gives an event a date without asking whether the day has room")
	void everyDateWriteIsChecked() throws IOException {
		List<Method> methods = methodsOf(SERVICE);

		assertThat(methods)
				.as("no methods were parsed, so this test is asserting nothing")
				.isNotEmpty();

		Set<String> unchecked = new LinkedHashSet<>();
		for (Method method : methods) {
			// canBookEvent is the check itself; it has no date to write.
			if (method.name().equals("canBookEvent")) {
				continue;
			}
			if (WRITES_A_DATE.matcher(method.body()).find()
					&& !ASKS_FIRST.matcher(method.body()).find()) {
				unchecked.add(method.name());
			}
		}

		assertThat(unchecked)
				.as("""
						These methods give an event a date without calling canBookEvent, so they \
						can put more events on a day than it holds — which is how three ended up \
						on Friday 1 May 2026 in production. Call canBookEvent before writing the \
						date and refuse the save when it says no; see saveAndUpdate for the shape.""")
				.isEmpty();
	}

	@Test
	@DisplayName("the check is reached from more than one place, so the scan is finding real calls")
	void theScanFindsRealCalls() throws IOException {
		/*
		 * Guards the guard. A regex that quietly stopped matching — a rename, a
		 * reformat — would make the test above pass by finding nothing at all,
		 * which is the failure mode of every structural test.
		 */
		List<Method> methods = methodsOf(SERVICE);

		long writing = methods.stream()
				.filter(m -> WRITES_A_DATE.matcher(m.body()).find())
				.count();
		long asking = methods.stream()
				.filter(m -> !m.name().equals("canBookEvent"))
				.filter(m -> ASKS_FIRST.matcher(m.body()).find())
				.count();

		/*
		 * Deliberately "at least one" rather than the current count. The point is
		 * only that both patterns still match something; pinning the number would
		 * make a legitimate consolidation of these near-identical save paths fail
		 * a test that has no opinion about how many there should be.
		 */
		assertThat(writing).as("found no method that writes an event date").isPositive();
		assertThat(asking).as("found no method that calls canBookEvent").isPositive();
	}

	// -----------------------------------------------------------------

	/**
	 * Splits the file at method signatures.
	 *
	 * <p>
	 * Crude on purpose. A real parser would be a dependency and a great deal of
	 * machinery to answer one question about one file, and the thing being
	 * detected — a whole call being absent from a method — survives the
	 * imprecision comfortably.
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
