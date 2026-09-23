package com.zbs.de.service.calendar;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zbs.de.model.CalendarBusyBlock;
import com.zbs.de.model.CalendarConnection;
import com.zbs.de.repository.RepositoryCalendarBusyBlock;
import com.zbs.de.repository.RepositoryCalendarConnection;

/**
 * Bringing in when the team are already busy.
 *
 * <p>
 * Reads from <em>every</em> calendar a host has connected, because somebody
 * with work in Outlook and their own life in Google is genuinely unavailable
 * for both, and reading one produces a system that books meetings over their
 * dentist. Only periods come back — never what the meetings are — so nothing
 * private about the team's own diary is stored here.
 *
 * <h2>Replace, do not merge</h2>
 *
 * Each sync deletes what it previously imported for that connection and writes
 * the answer afresh. Merging would need to work out which stored block
 * corresponds to which returned period, and getting that wrong leaves a block
 * behind for a meeting that was cancelled — a host blocked out for something
 * that is not happening, with nothing to show why.
 *
 * <p>
 * The window is bounded. Importing everything a person has ever had would be
 * slow, large, and pointless: consultations cannot be booked further ahead than
 * the type allows, so busy periods beyond that horizon can block nothing.
 */
@Service
public class ServiceCalendarBusyImport {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCalendarBusyImport.class);

	/**
	 * How far ahead to import.
	 *
	 * <p>
	 * Comfortably beyond the 90-day default maximum advance, so that raising
	 * that setting does not silently produce a stretch of calendar the import
	 * never covers.
	 */
	private static final Duration HORIZON = Duration.ofDays(120);

	@Autowired
	private RepositoryCalendarConnection repositoryConnection;

	@Autowired
	private RepositoryCalendarBusyBlock repositoryBusyBlock;

	@Autowired(required = false)
	private List<CalendarProvider> providers = List.of();

	/** Syncs every usable connection. Returns how many succeeded. */
	public int syncAll() {
		List<CalendarConnection> connections = repositoryConnection.syncable();
		int succeeded = 0;

		for (CalendarConnection connection : connections) {
			if (syncOne(connection)) {
				succeeded++;
			}
		}

		if (!connections.isEmpty()) {
			LOGGER.info("Imported busy times from {} of {} connected calendars",
					succeeded, connections.size());
		}
		return succeeded;
	}

	/**
	 * One connection.
	 *
	 * <p>
	 * Its own transaction, so one broken calendar does not roll back the
	 * successful import of the others alongside it.
	 */
	@Transactional
	public boolean syncOne(CalendarConnection connection) {
		CalendarProvider provider = providers.stream()
				.filter(p -> p.name().equals(connection.getTxtProvider()))
				.findFirst()
				.orElse(null);

		if (provider == null) {
			// Configured off, or an unknown provider. Not worth a failure state:
			// there is nothing wrong with the connection, only with what this
			// server can currently do about it.
			return false;
		}

		Instant from = Instant.now();
		Instant to = from.plus(HORIZON);

		try {
			List<CalendarProvider.BusyPeriod> periods = provider.busyPeriods(connection, from, to);

			repositoryBusyBlock.deleteBySerCalendarConnectionId(
					connection.getSerCalendarConnectionId());

			for (CalendarProvider.BusyPeriod period : periods) {
				CalendarBusyBlock block = new CalendarBusyBlock();
				block.setSerCalendarConnectionId(connection.getSerCalendarConnectionId());
				block.setSerHostId(connection.getSerHostId());
				block.setDteStartsAt(period.startsAt());
				block.setDteEndsAt(period.endsAt());
				repositoryBusyBlock.save(block);
			}

			connection.setDteLastSyncedAt(Instant.now());
			connection.setTxtSyncStatus(CalendarConnection.SYNC_HEALTHY);
			connection.setTxtSyncError(null);
			repositoryConnection.save(connection);
			return true;

		} catch (Exception e) {
			/*
			 * What was imported before is deliberately left in place. It is
			 * stale, but stale busy times block slots the host probably still
			 * cannot take — whereas deleting them would offer those times out
			 * the moment a provider had a bad five minutes.
			 *
			 * Erring towards a host looking busier than they are is the right
			 * way round: the cost is a missed booking, and the alternative costs
			 * somebody a double-booked afternoon.
			 */
			LOGGER.warn("Could not import busy times from calendar {}: {}",
					connection.getSerCalendarConnectionId(), e.getMessage());

			connection.setTxtSyncStatus(needsReconnecting(e)
					? CalendarConnection.SYNC_NEEDS_RECONNECT
					: CalendarConnection.SYNC_ERROR);
			connection.setTxtSyncError(e.getMessage());
			repositoryConnection.save(connection);
			return false;
		}
	}

	/**
	 * Whether this needs a person rather than another attempt.
	 *
	 * <p>
	 * A revoked grant or an expired refresh token will fail identically for ever,
	 * and marking it as merely an error means it is retried every ten minutes
	 * until somebody notices the slots are wrong. NEEDS_RECONNECT takes it out
	 * of the sweep and puts it on the screen instead.
	 */
	private boolean needsReconnecting(Exception e) {
		String message = e.getMessage() == null ? "" : e.getMessage();
		return message.contains("needs reconnecting")
				|| message.contains("must be reconnected")
				|| message.contains("refused to refresh");
	}
}
