package com.zbs.de.model.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

/**
 * What the office needs to know when it opens the portal.
 *
 * <h3>Why this is not {@link DtoDashboard}</h3>
 *
 * That one exists, and answers a different question: how many customers and
 * events were <em>created</em> this month against last. Those are creation
 * counts — a number that only ever goes up, and which tells nobody what to do
 * today. It is also unused: nothing in either client calls
 * {@code /analytics/summary}.
 *
 * <p>
 * This one is about the business rather than the database. An event company
 * runs on three questions:
 *
 * <ul>
 *   <li><b>What is in the pipeline, and what is it worth?</b> Enquiries,
 *       quotes and confirmed bookings, counted and valued. The thing a
 *       manager looks at before anything else.</li>
 *   <li><b>What does the year look like?</b> Bookings by month ahead with the
 *       covers that come with them, which is what tells a kitchen whether
 *       August is a problem.</li>
 *   <li><b>What is waiting on somebody?</b> Bookings with no date and
 *       bookings nobody has priced. Both are ordinary states on the way to a
 *       booking, and both go wrong quietly.</li>
 * </ul>
 *
 * <h3>Where the money comes from</h3>
 *
 * A booking is worth the sum of its priced lines — {@code event_price_line},
 * which the pricing engine writes at save — plus their VAT. Not
 * {@code event_budget.num_total_budget}, which is hand-entered and which the
 * engine does not maintain, and not {@code num_itinerary_price}, which nothing
 * computes.
 *
 * <p>
 * A booking with no lines is worth nothing <em>yet</em>, which is a different
 * statement from being worth zero — hence {@code numWithoutPrice}.
 */
@Data
public class DtoBusinessSummary {

	/** One stage of the pipeline. */
	@Data
	public static class Stage {
		private String txtStage;
		private long numBookings;
		private BigDecimal numValue = BigDecimal.ZERO;
	}

	/** One month ahead. */
	@Data
	public static class Month {
		/** yyyy-MM. The client formats it; parsing prose is not its job. */
		private String txtMonth;
		private long numBookings;
		private long numGuests;
		private BigDecimal numValue = BigDecimal.ZERO;
	}

	/** One kind of event, and how much of the business it is. */
	@Data
	public static class TypeShare {
		private String txtEventType;
		private long numBookings;
		private long numGuests;
	}

	private List<Stage> pipeline;
	private List<Month> monthsAhead;
	private List<TypeShare> byType;

	/** Confirmed only — what the business has actually won. */
	private BigDecimal numConfirmedValue = BigDecimal.ZERO;

	/** Everything not yet confirmed. What is still to play for. */
	private BigDecimal numPipelineValue = BigDecimal.ZERO;

	private long numBookingsAhead;
	private long numGuestsAhead;

	/** Confirmed as a share of every booking taken, to one decimal place. */
	private BigDecimal numConversionRate = BigDecimal.ZERO;

	/** Across confirmed bookings that have been priced. */
	private BigDecimal numAverageBookingValue = BigDecimal.ZERO;

	private long numWithoutDate;
	private long numWithoutPrice;
}
