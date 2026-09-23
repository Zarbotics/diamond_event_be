package com.zbs.de.service.calendar;

import java.time.Instant;
import java.util.List;

import com.zbs.de.model.CalendarConnection;
import com.zbs.de.model.ConsultationBooking;

/**
 * One person's calendar, at somebody else's company.
 *
 * <p>
 * Google and Microsoft sit behind this. The interface exists so that nothing
 * above it knows which — the booking rules, the slot finder and the admin
 * screens are the same either way, and a team with people on both is the
 * ordinary case rather than a special one.
 *
 * <h2>What is asked for, and what is deliberately not</h2>
 *
 * Only <em>busy periods</em> are read: when somebody is unavailable, never what
 * they are doing. Both providers have an API for exactly this — Google's
 * {@code freeBusy.query} and Microsoft's {@code getSchedule} — which returns
 * periods and nothing else. That means the team's private meetings never enter
 * this database, there is nothing sensitive here to leak, and the permission
 * being requested is one a person can reasonably agree to.
 *
 * <h2>Read from all, write to one</h2>
 *
 * Busy times are read from every calendar a host has connected: somebody with
 * work in Outlook and their own life in Google is genuinely unavailable for
 * both, and reading only one produces a system that books over their dentist.
 * Consultations are written to exactly one nominated calendar, because writing
 * to several means the same meeting exists two or three times and every later
 * change has to find and match all the copies.
 *
 * <h2>Failure is expected, not exceptional</h2>
 *
 * Every method here reaches across the network to a service this system does
 * not control, with credentials that expire and can be revoked by their owner
 * at any moment. Callers must treat failure as normal traffic: a provider being
 * down is never a reason to refuse a customer a booking, and the contract on
 * each method says what to do instead.
 */
public interface CalendarProvider {

	/** Matches {@link CalendarConnection#getTxtProvider()}. */
	String name();

	/** A period somebody is unavailable. No detail about why. */
	record BusyPeriod(Instant startsAt, Instant endsAt) {
	}

	/**
	 * A consultation as it now exists in somebody's calendar.
	 *
	 * @param externalEventId what the provider called it, so it can be changed
	 *                        or removed later
	 * @param joinUrl         the Meet or Teams link, if one was asked for and
	 *                        the provider made one. Null is a legitimate answer:
	 *                        an in-person venue visit has no link, and a
	 *                        provider that declined to make one has not failed.
	 */
	record ExternalEvent(String externalEventId, String joinUrl) {
	}

	/**
	 * When this person is busy, between two instants.
	 *
	 * <p>
	 * Throws on failure rather than returning empty. The difference matters: an
	 * empty list means "free all week" and would have the system offer times the
	 * host is already committed to. A caller that cannot tell the two apart will
	 * double-book somebody, so this refuses to let them be confused.
	 */
	List<BusyPeriod> busyPeriods(CalendarConnection connection, Instant from, Instant to);

	/**
	 * Puts a confirmed consultation in the host's calendar.
	 *
	 * @param withVideoLink whether to ask the provider for a Meet or Teams link.
	 *                      Configured per kind of meeting in the admin portal —
	 *                      a video call wants one and a visit to a venue does
	 *                      not.
	 */
	ExternalEvent createEvent(CalendarConnection connection, ConsultationBooking booking,
			boolean withVideoLink);

	/**
	 * Removes one, after a cancellation.
	 *
	 * <p>
	 * Must not throw when the event is already gone. The host may well have
	 * deleted it themselves, and that is agreement rather than an error.
	 */
	void deleteEvent(CalendarConnection connection, String externalEventId);
}
