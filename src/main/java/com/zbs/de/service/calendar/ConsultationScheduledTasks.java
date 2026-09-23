package com.zbs.de.service.calendar;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.zbs.de.service.ServiceConsultation;

/**
 * The two things consultations need doing to them on a timer.
 *
 * <h2>Releasing holds that ran out</h2>
 *
 * This is the half of "requested, then confirmed" that makes the other half
 * safe. A pending request holds its slot against everybody else, which is
 * right — the team must not be able to confirm a meeting into a time somebody
 * else has since taken. But without something to release it, one request nobody
 * answers takes that slot off sale for good.
 *
 * <p>
 * {@code releaseLapsedHolds} existed from the start and <strong>nothing ever
 * called it</strong>. The design was documented, the code was written and
 * tested, and the behaviour simply did not happen: every unanswered request
 * held its slot for ever. Worth stating plainly, because it is the failure mode
 * of a scheduled job in general — nothing errors, nothing is logged, and the
 * absence is only visible as slots that quietly never come back.
 *
 * <h2>Importing busy times</h2>
 *
 * Polling, deliberately. Both providers can push notifications instead, and
 * both need a publicly reachable HTTPS endpoint to push to — which development
 * machines do not have and which is a lapsed subscription away from silently
 * stopping in production. Polling underneath is the fallback that makes push
 * an optimisation rather than a dependency.
 */
@Component
public class ConsultationScheduledTasks {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConsultationScheduledTasks.class);

	@Autowired
	private ServiceConsultation serviceConsultation;

	@Autowired
	private ServiceCalendarBusyImport serviceCalendarBusyImport;

	/**
	 * Puts lapsed requests back on sale.
	 *
	 * <p>
	 * Every five minutes. The hold window is measured in hours, so this only has
	 * to be fine-grained enough that a slot does not sit unavailable for
	 * noticeably longer than it was promised for.
	 */
	@Scheduled(fixedDelay = 5, initialDelay = 2, timeUnit = TimeUnit.MINUTES)
	public void releaseLapsedHolds() {
		try {
			int released = serviceConsultation.releaseLapsedHolds();
			if (released > 0) {
				LOGGER.info("Released {} consultation hold(s) that ran out", released);
			}
		} catch (Exception e) {
			// A scheduled method that throws is silently not rescheduled by some
			// executors. Catching keeps the timer alive through a transient
			// database problem.
			LOGGER.warn("Could not release lapsed consultation holds: {}", e.getMessage());
		}
	}

	/**
	 * Refreshes the imported busy times.
	 *
	 * <p>
	 * Ten minutes. Frequent enough that a meeting somebody accepts this morning
	 * blocks a slot before an afternoon customer is offered it, and infrequent
	 * enough to stay well inside both providers' rate limits with a team of any
	 * plausible size.
	 *
	 * <p>
	 * Does nothing at all when no calendar is connected, which is the ordinary
	 * state in development and on a fresh installation.
	 */
	@Scheduled(fixedDelay = 10, initialDelay = 1, timeUnit = TimeUnit.MINUTES)
	public void importBusyTimes() {
		try {
			serviceCalendarBusyImport.syncAll();
		} catch (Exception e) {
			LOGGER.warn("Could not import calendar busy times: {}", e.getMessage());
		}
	}
}
