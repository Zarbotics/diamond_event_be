package com.zbs.de.util.enums;

/**
 * How much a notification matters.
 *
 * <h2>Why three and not five</h2>
 *
 * Because a scale nobody can apply consistently is a scale that ends up all
 * one value. Three is the most anybody reliably distinguishes: this needs
 * dealing with, this is worth knowing, this is here so it can be found later.
 */
public enum EnmNotificationPriority {

	/** Findable, never interrupting. Does not count towards the badge. */
	LOW,

	/** The ordinary case. */
	NORMAL,

	/**
	 * Has to survive being scrolled past.
	 *
	 * <p>
	 * Reserved for things with a deadline attached — a day going full while
	 * somebody is quoting for it, a payment failing. A notification that is
	 * merely important is NORMAL.
	 */
	URGENT;

	public static EnmNotificationPriority of(String s) {
		if (s == null) {
			return null;
		}
		try {
			return EnmNotificationPriority.valueOf(s.trim().toUpperCase());
		} catch (Exception ex) {
			return null;
		}
	}
}
