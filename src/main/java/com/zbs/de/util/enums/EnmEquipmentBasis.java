package com.zbs.de.util.enums;

/**
 * What a quantity of equipment is counted against.
 *
 * <h3>Why four and not three</h3>
 *
 * Because a catering operation genuinely counts in four different ways, and
 * collapsing any of them loses a real rule.
 *
 * <ul>
 * <li>{@code PER_GUEST} — a dinner plate each. Scales with covers.</li>
 * <li>{@code PER_TABLE} — a tablecloth, two candelabra. Scales with tables,
 * which is not the guest count divided by ten: a venue seats eight on some
 * tables and twelve on others, and it already stores the number.</li>
 * <li>{@code PER_STATION} — tongs and boards on a grazing bar. A station is
 * not one thing for 300 people; the venue puts out another every so many
 * guests, and {@code numGuestsPerStation} says how often.</li>
 * <li>{@code PER_EVENT} — one cake knife, however many come.</li>
 * </ul>
 *
 * <h3>What happened to the old vocabulary</h3>
 *
 * The itinerary model this replaces had two: an enum of {@code PER_GUEST,
 * PER_TABLE, PER_ITEM, PER_PORTION, FLAT} on one table, and a free-text
 * column documented as {@code PER_GUEST|PER_DISH|PER_STATION|FIXED} on
 * another. Neither was a subset of the other and no rule said which won —
 * and {@code PER_STATION}, the one a grazing bar needs, existed only in the
 * half that was not type-checked.
 *
 * <p>
 * {@code PER_ITEM}, {@code PER_PORTION} and {@code PER_DISH} are gone
 * deliberately. "Per portion" and "per guest" are the same number in a
 * catering context — you plate one portion per cover — and having both means
 * two people writing the same rule two ways.
 */
public enum EnmEquipmentBasis {

	PER_GUEST, PER_TABLE, PER_STATION, PER_EVENT;

	/** The basis a stored string names, or null if it names none. */
	public static EnmEquipmentBasis of(String s) {
		if (s == null) {
			return null;
		}
		try {
			return EnmEquipmentBasis.valueOf(s.trim().toUpperCase());
		} catch (Exception ex) {
			return null;
		}
	}
}
