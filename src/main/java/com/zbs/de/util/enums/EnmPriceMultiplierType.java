package com.zbs.de.util.enums;

public enum EnmPriceMultiplierType {
	PER_GUEST, FLAT;

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
