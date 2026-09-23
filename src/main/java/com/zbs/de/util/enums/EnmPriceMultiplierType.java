package com.zbs.de.util.enums;

/**
 * How the price of a thing turns into a figure on a quote.
 *
 * <h2>Why these four, and why these names</h2>
 *
 * They are the same four the equipment rules use. A grazing bar that needs one
 * station per fifty guests for its boards and tongs is charged for on that same
 * basis, and a business that has learned one vocabulary should not have to
 * learn a second one for money.
 *
 * <p>
 * {@code PER_TABLE} and {@code PER_STATION} are new. The enum held only
 * {@code PER_GUEST} and {@code FLAT}, which is why a dish priced by the table
 * or by the station had nowhere to say so and was priced per head instead.
 */
public enum EnmPriceMultiplierType {

	/** Multiply by the number of guests. The usual case for a plated course. */
	PER_GUEST,

	/** Multiply by the number of tables. Centrepieces, linen, table service. */
	PER_TABLE,

	/**
	 * One station per so many guests, rounded up, and multiply by that.
	 *
	 * <p>
	 * How far a station stretches is on the item
	 * ({@code menu_item.num_guests_per_station}). With nothing there it is one
	 * station, which is the safe reading: a business that has not said how many
	 * guests a grazing bar serves has not said it needs two of them.
	 */
	PER_STATION,

	/** The price is the price, whoever comes. */
	FLAT;

	/**
	 * The rule a stored string names, or {@code null} if it names none.
	 *
	 * <p>
	 * This returned {@link EnmItineraryUnitType} — a different enum, from a
	 * different feature, which happens to have members named {@code PER_GUEST}
	 * and {@code FLAT} as well as three others. So it compiled, and it would have
	 * answered an itinerary unit to a caller asking how a dish is priced.
	 * Nothing called it, which is the only reason it never mattered. M5b is the
	 * first caller.
	 */
	public static EnmPriceMultiplierType of(String s) {
		if (s == null)
			return null;
		try {
			return EnmPriceMultiplierType.valueOf(s.trim().toUpperCase());
		} catch (Exception ex) {
			return null;
		}
	}
}
