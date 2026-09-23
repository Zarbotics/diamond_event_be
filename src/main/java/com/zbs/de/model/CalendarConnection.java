package com.zbs.de.model;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * One host's connected Google or Microsoft account.
 *
 * <p>
 * Connected per person rather than per company. A team does not all use the
 * same thing, and the alternative — a domain-wide installation — can read every
 * mailbox in the organisation whether or not its owner agreed to that. It needs
 * an administrator's blessing, is a far larger thing to be responsible for, and
 * buys nothing here.
 *
 * <p>
 * The tokens are stored encrypted. A refresh token in plaintext is a standing
 * grant to read and write the whole team's calendars, handed to anybody who
 * reaches the database or one of its backups.
 */
@Entity
@Table(name = "calendar_connection")
@Getter
@Setter
public class CalendarConnection {

	public static final String GOOGLE = "GOOGLE";
	public static final String MICROSOFT = "MICROSOFT";

	/** Working normally. */
	public static final String SYNC_HEALTHY = "HEALTHY";

	/** The owner revoked access, or the refresh token expired. Needs a person. */
	public static final String SYNC_NEEDS_RECONNECT = "NEEDS_RECONNECT";

	/** Something else went wrong. Worth showing, not worth panicking about. */
	public static final String SYNC_ERROR = "ERROR";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_calendar_connection_id")
	private Integer serCalendarConnectionId;

	@Column(name = "ser_host_id", nullable = false)
	private Integer serHostId;

	@Column(name = "txt_provider", nullable = false, length = 32)
	private String txtProvider;

	@Column(name = "txt_account_email", nullable = false)
	private String txtAccountEmail;

	/** Which calendar in that account. A person routinely has several. */
	@Column(name = "txt_calendar_id", length = 512)
	private String txtCalendarId;

	@Column(name = "txt_calendar_name")
	private String txtCalendarName;

	/**
	 * Whether consultations are written here.
	 *
	 * <p>
	 * At most one per host, and the database enforces that with a partial unique
	 * index rather than trusting anyone to clear the old one first. Two write
	 * targets would mean every consultation created twice.
	 */
	@Column(name = "bln_is_write_target", nullable = false)
	private Boolean blnIsWriteTarget = false;

	@Column(name = "txt_access_token_encrypted")
	private String txtAccessTokenEncrypted;

	@Column(name = "txt_refresh_token_encrypted")
	private String txtRefreshTokenEncrypted;

	@Column(name = "dte_token_expires_at")
	private Instant dteTokenExpiresAt;

	/** The provider's cursor, so a sync fetches changes rather than everything. */
	@Column(name = "txt_sync_token")
	private String txtSyncToken;

	@Column(name = "dte_last_synced_at")
	private Instant dteLastSyncedAt;

	@Column(name = "txt_sync_status", nullable = false, length = 32)
	private String txtSyncStatus = SYNC_HEALTHY;

	@Column(name = "txt_sync_error")
	private String txtSyncError;

	@Column(name = "bln_is_deleted", nullable = false)
	private Boolean blnIsDeleted = false;

	@Column(name = "created_date", nullable = false)
	private Instant createdDate = Instant.now();

	@Column(name = "updated_date", nullable = false)
	private Instant updatedDate = Instant.now();

	@Column(name = "created_by", nullable = false)
	private Integer createdBy = 0;

	@Column(name = "updated_by", nullable = false)
	private Integer updatedBy = 0;

	/** Whether this connection can currently be relied on for busy times. */
	public boolean isUsable() {
		return !Boolean.TRUE.equals(blnIsDeleted) && !SYNC_NEEDS_RECONNECT.equals(txtSyncStatus);
	}
}
