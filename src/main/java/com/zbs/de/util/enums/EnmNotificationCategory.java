package com.zbs.de.util.enums;

/**
 * What kind of thing happened.
 *
 * <h2>Why a fixed vocabulary</h2>
 *
 * The column it replaces was free text, and with two callers it had already
 * acquired one value written two ways. A notification type that cannot be
 * relied on is one the interface cannot group by, filter on, or give an icon
 * to — which is most of what makes a list of them readable.
 */
public enum EnmNotificationCategory {

	/** A customer submitted an enquiry through the journey. */
	BOOKING_TAKEN,

	/** An existing booking was edited by somebody. */
	BOOKING_CHANGED,

	PAYMENT_RECEIVED,

	CONSULTATION_BOOKED,

	CONSULTATION_CANCELLED,

	/** A day has reached the number of events the business will take. */
	CAPACITY_REACHED,

	/** The application telling the office about itself. */
	SYSTEM;

	public static EnmNotificationCategory of(String s) {
		if (s == null) {
			return null;
		}
		try {
			return EnmNotificationCategory.valueOf(s.trim().toUpperCase());
		} catch (Exception ex) {
			return null;
		}
	}
}
