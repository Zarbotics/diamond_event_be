package com.zbs.de.model;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * A period a host is busy according to their own connected calendar.
 *
 * <p>
 * Times only. Blocking a slot needs to know that the host is unavailable and
 * nothing whatsoever about why — importing subjects and attendees would put
 * the team's private meetings in this database for no gain.
 */
@Entity
@Table(name = "calendar_busy_block")
@Getter
@Setter
public class CalendarBusyBlock {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_busy_block_id")
	private Integer serBusyBlockId;

	@Column(name = "ser_calendar_connection_id", nullable = false)
	private Integer serCalendarConnectionId;

	@Column(name = "ser_host_id", nullable = false)
	private Integer serHostId;

	@Column(name = "dte_starts_at", nullable = false)
	private Instant dteStartsAt;

	@Column(name = "dte_ends_at", nullable = false)
	private Instant dteEndsAt;

	@Column(name = "txt_external_id", length = 512)
	private String txtExternalId;

	@Column(name = "dte_imported_at", nullable = false)
	private Instant dteImportedAt = Instant.now();
}
