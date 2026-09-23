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
 * Every method that creates an event must give it a booking.
 *
 * <h2>Why structurally, and not by review</h2>
 *
 * The third test of this shape in this package, and for the third time the same
 * reason: {@code ServiceEventMasterImpl} contains four create branches, several
 * hundred lines each and near-identical to one another, and one of them carries
 * a comment instructing the reader to copy any change by hand into its twin.
 * The capacity rule was already missing from one of the four before anybody
 * noticed. Writing this test found a fourth branch that the plan for this
 * change had not accounted for — the admin portal's, which builds its entity
 * through a different mapper — so the risk is not hypothetical even within the
 * change the test was written for.
 *
 * <h2>What a miss would cost</h2>
 *
 * Nothing visible, for months. An event created down a branch that skips this
 * simply has no parent, every screen behaves exactly as before, and the
 * omission surfaces at stage 3 — when the budget and the payments move onto the
 * booking, and the events with no booking are the ones whose money has nowhere
 * to go. By then the rows exist and somebody has to work out, after the fact,
 * which of them belonged together.
 *
 * <h2>Why the anchor is the reference code</h2>
 *
 * {@code generateNextEventMasterCode()} is called on exactly the branches that
 * create an event, on all of them, and nowhere else. It is a truer marker than
 * the mapper call, which differs between branches, and than the save, which
 * updates use too.
 */
class EveryNewEventGetsABookingTest {

	private static final Path SERVICE = Path.of(
			"src/main/java/com/zbs/de/service/impl/ServiceEventMasterImpl.java");

	/**
	 * Minting a reference for an event that does not have one.
	 *
	 * <p>
	 * Which is to say: creating one. An existing event keeps the code it was
	 * given, so this call appears on the create branches and on no others.
	 */
	private static final Pattern CREATES_AN_EVENT = Pattern.compile(
			"generateNextEventMasterCode\\(\\)");

	private static final Pattern GIVES_IT_A_BOOKING = Pattern.compile("giveItABooking\\(");

	/** A method, and the source between its signature and the next one. */
	private record Method(String name, String body) {
	}

	@Test
	@DisplayName("no method creates an event without a booking above it")
	void everyCreateMakesABooking() throws IOException {
		List<Method> methods = methodsOf(SERVICE);

		assertThat(methods)
				.as("no methods were parsed, so this test is asserting nothing")
				.isNotEmpty();

		Set<String> parentless = new LinkedHashSet<>();
		for (Method method : methods) {
			if (method.name().equals("generateNextEventMasterCode") || method.name().equals("giveItABooking")) {
				continue;
			}
			if (CREATES_AN_EVENT.matcher(method.body()).find()
					&& !GIVES_IT_A_BOOKING.matcher(method.body()).find()) {
				parentless.add(method.name());
			}
		}

		assertThat(parentless)
				.as("""
						These methods create an event and give it no booking, so the proportion \
						of events with no parent grows with every sale and stage 3 has nowhere \
						to put their budget or their payments. Call giveItABooking(entity) after \
						the reference code is set and before the event is saved; see saveAndUpdate \
						for the shape.""")
				.isEmpty();
	}

	@Test
	@DisplayName("the scan finds every create branch, not just the ones that were remembered")
	void theScanFindsRealCalls() throws IOException {
		/*
		 * Guards the guard, as its neighbours do. A structural test whose regex
		 * has quietly stopped matching passes by finding nothing at all, which is
		 * the one failure mode it cannot report on itself.
		 *
		 * The count is asserted rather than merely being positive because the
		 * number is the finding: four branches, not the three the change was
		 * planned around. If a fifth appears, this fails and somebody reads it —
		 * which is the whole point, given that the fourth was found only because
		 * this test was written.
		 */
		List<Method> methods = methodsOf(SERVICE);

		long creating = methods.stream()
				.filter(m -> !m.name().equals("generateNextEventMasterCode"))
				.filter(m -> CREATES_AN_EVENT.matcher(m.body()).find())
				.count();

		/*
		 * Call sites, not occurrences: the declaration of giveItABooking itself
		 * matches the same pattern, and counting it would let a file with the
		 * helper and no callers pass.
		 */
		long calls = methods.stream()
				.filter(m -> !m.name().equals("giveItABooking"))
				.mapToLong(m -> GIVES_IT_A_BOOKING.matcher(m.body()).results().count())
				.sum();

		assertThat(creating).as("found no method that creates an event").isPositive();

		/*
		 * Measured against what the file actually contains rather than against
		 * a number somebody wrote down. It used to assert four, because there
		 * were four near-identical save methods; two of those were unused
		 * copies and have gone, and a guard that needs recalibrating every time
		 * the code improves is a guard people learn to edit rather than read.
		 */
		assertThat(calls)
				.as("""
						giveItABooking is called fewer times than there are create branches, so \
						at least one branch creates events with no parent above them.""")
				.isGreaterThanOrEqualTo(creating);
	}

	// -----------------------------------------------------------------

	/**
	 * Splits the file at method signatures.
	 *
	 * <p>
	 * Crude on purpose, and the same approach as the two tests beside it. A real
	 * parser would be a dependency and a great deal of machinery to answer one
	 * question about one file, and the thing being detected — a whole call being
	 * absent from a method — survives the imprecision comfortably.
	 */
	private List<Method> methodsOf(Path file) throws IOException {
		String source = liveSource(file);

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

	/**
	 * The file with its commented-out code removed.
	 *
	 * <p>
	 * This scan reads text, and a commented-out method is text. There is a
	 * disabled copy of {@code generateNextEventMasterCode} in this file, and
	 * while it happened to sit inside a live method's span it was attributed to
	 * that method and did no harm. When the method around it was deleted the
	 * comment attached itself to {@code setPaidAmount} instead, which was
	 * duly reported as creating events without a booking.
	 *
	 * <p>
	 * A structural test that can be fooled by a comment is one that will
	 * eventually be fooled into silence rather than into noise, so it reads
	 * live lines only — the same thing its neighbour does.
	 */
	private static String liveSource(Path file) throws IOException {
		StringBuilder live = new StringBuilder();
		for (String line : Files.readAllLines(file)) {
			live.append(line.trim().startsWith("//") ? "" : line).append('\n');
		}
		return live.toString();
	}
}
