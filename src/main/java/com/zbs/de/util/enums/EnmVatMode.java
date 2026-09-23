package com.zbs.de.util.enums;

/**
 * How the business charges VAT.
 *
 * <h2>Why this is the office's decision and not the code's</h2>
 *
 * The rate was a constant in a JavaScript file, and what it applied to was
 * decided by which variable it happened to be multiplied by — décor and extras,
 * and nothing else. That is a defensible answer for some businesses and wrong
 * for others, it changes when a business crosses the registration threshold or
 * changes what it sells, and getting it wrong has consequences with HMRC rather
 * than with a customer.
 *
 * <p>
 * None of that belongs in a constant only a developer can reach.
 */
public enum EnmVatMode {

	/** No VAT on anything. A business below the registration threshold. */
	NONE,

	/**
	 * VAT on the whole bill.
	 *
	 * <p>
	 * The simple case, and the one most standard-rated businesses want. The
	 * per-section switches are ignored entirely — not overridden one at a time,
	 * which would leave somebody wondering why a switch marked off was charging.
	 */
	TOTAL,

	/**
	 * VAT on whichever parts of the bill are switched on.
	 *
	 * <p>
	 * The default, because it is what the system did before this existed: décor
	 * and extras carried VAT and nothing else did.
	 */
	SECTIONS;

	/** The mode a stored string names, or {@code null} if it names none. */
	public static EnmVatMode of(String s) {
		if (s == null) {
			return null;
		}
		try {
			return EnmVatMode.valueOf(s.trim().toUpperCase());
		} catch (Exception ex) {
			return null;
		}
	}
}
