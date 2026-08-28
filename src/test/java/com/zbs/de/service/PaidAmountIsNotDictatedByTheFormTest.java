package com.zbs.de.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * No save path writes a booking's paid amount straight from the request.
 *
 * <h2>What happened</h2>
 *
 * Two things owned {@code event_budget.num_paid_amount} and the wrong one won.
 * {@code ServiceEventBudgetImpl.recalculateBudget} sums the payments recorded
 * against the budget. The four event-save methods set it to whatever the form
 * sent — and the form sends the figure it loaded, which was read before the
 * payment was taken.
 *
 * <p>
 * Production carries the result. Budget 146 reads {@code num_paid_amount = 0.00}
 * against a recorded payment of £500, and its {@code updated_date} is four
 * seconds after the payment's: the save that followed, carrying the zero it had
 * loaded. Nothing announced it. The payment row is still there and the booking
 * simply reads as unpaid.
 *
 * <h2>Why structurally</h2>
 *
 * Twelve places wrote that field, in four near-identical methods of several
 * hundred lines each, and the next save path added will be a copy of one of
 * them. This is the sixth time in this codebase that copies of one routine have
 * turned out to disagree, and every previous one was found by accident. A
 * scanning test turns "somebody pasted the old line back" into a failed build.
 *
 * <p>
 * It does not check that {@code setPaidAmount} is right — {@code
 * PaidAmountSurvivesASaveIT} drives that against a real database. It catches the
 * specific fault that happened: the figure being taken from the request at all.
 */
class PaidAmountIsNotDictatedByTheFormTest {

	private static final Path SERVICE = Path.of(
			"src/main/java/com/zbs/de/service/impl/ServiceEventMasterImpl.java");

	/**
	 * Live lines only.
	 *
	 * <p>
	 * This file carries a great deal of commented-out history — thirty-five of
	 * the lines matching the pattern below are inside comments, left in place
	 * deliberately rather than churned. A scan that counted them would fail
	 * permanently and be deleted by whoever inherited it.
	 */
	private static List<String> liveLines() throws IOException {
		List<String> live = new ArrayList<>();
		for (String line : Files.readAllLines(SERVICE)) {
			if (!line.trim().startsWith("//")) {
				live.add(line.trim());
			}
		}
		return live;
	}

	@Test
	@DisplayName("the file being scanned is the one that saves events")
	void theScanIsLookingAtSomething() throws IOException {
		// A scan of a moved or renamed file passes silently while protecting
		// nothing. This is the guard on the guard.
		List<String> live = liveLines();

		assertThat(live).isNotEmpty();
		assertThat(live).anyMatch(l -> l.contains("public DtoResult saveAndUpdateWithDocs("));
		assertThat(live).anyMatch(l -> l.contains("public DtoResult saveAndUpdateWithDocsAdminPortal("));
		assertThat(live).anyMatch(l -> l.contains("public DtoResult saveAndUpdateWithDocsCE("));
	}

	@Test
	@DisplayName("nothing sets a budget's paid amount straight from the request")
	void thePaidAmountIsNeverTakenFromTheForm() throws IOException {

		List<String> offenders = liveLines().stream()
				.filter(l -> l.contains("setNumPaidAmount("))
				// Reads are fine: these copy the stored figure out to a DTO.
				.filter(l -> !l.contains("quote.setNumPaidAmount("))
				.filter(l -> !l.contains("dtoEventQuoteAndStatus.setNumPaidAmount("))
				// The one place allowed to write it, which is the fix itself.
				.filter(l -> !l.contains("budget.setNumPaidAmount("))
				.toList();

		assertThat(offenders)
				.as("""
						these write a booking's paid amount directly. It is derived from the payments \
						recorded against the budget, and a form that loaded before a payment was taken \
						will quietly write the money back to zero — call setPaidAmount(budget, figure) \
						instead""")
				.isEmpty();
	}

	@Test
	@DisplayName("every save path goes through the helper")
	void allFourSavePathsUseIt() throws IOException {
		/*
		 * The other half. A path that stopped writing the field altogether would
		 * pass the test above while leaving the paid amount stale on that path —
		 * so the twelve call sites are counted, not merely permitted.
		 *
		 * Twelve: four save methods, each with a create branch and an update
		 * branch, each branch with a figure and an else that means nothing was
		 * sent.
		 */
		long calls = liveLines().stream()
				.filter(l -> l.contains("setPaidAmount(eventBudget,"))
				.count();

		assertThat(calls)
				.as("a save path has stopped recording what has been paid, or a new one has appeared "
						+ "— check it goes through setPaidAmount")
				.isEqualTo(12);
	}
}
