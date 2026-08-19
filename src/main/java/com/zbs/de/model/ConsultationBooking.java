package com.zbs.de.model;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * A booked consultation.
 *
 * <p>
 * The database holds an exclusion constraint that forbids one host having two
 * live bookings that overlap — see {@code V6__consultation_scheduling.sql}. It
 * is not expressible in JPA, which is why that schema is written by hand: with
 * {@code ddl-auto} alone the table would exist without it and the system would
 * work perfectly until the first time two customers pressed the same slot at
 * once.
 *
 * <p>
 * {@code txtManagementToken} is what makes the cancel and reschedule links in
 * the confirmation email work without the customer signing in. It is single-use
 * and unguessable for that reason: it is the only thing standing between a
 * stranger and somebody else's meeting.
 */
@Entity
@Table(name = "consultation_booking")
@Getter
@Setter
public class ConsultationBooking {

	/** A booking that is still going to happen. Only these reserve a slot. */
	public static final String STATUS_BOOKED = "BOOKED";
	public static final String STATUS_CANCELLED = "CANCELLED";
	public static final String STATUS_COMPLETED = "COMPLETED";
	public static final String STATUS_NO_SHOW = "NO_SHOW";

	/** The booking exists here but has not reached the host's own calendar yet. */
	public static final String SYNC_PENDING = "PENDING";
	public static final String SYNC_SYNCED = "SYNCED";
	public static final String SYNC_FAILED = "FAILED";
	/** No calendar is connected, so there is nothing to sync it to. */
	public static final String SYNC_NOT_APPLICABLE = "NOT_APPLICABLE";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_consultation_booking_id")
	private Integer serConsultationBookingId;

	@Column(name = "ser_host_id", nullable = false)
	private Integer serHostId;

	@Column(name = "ser_consultation_type_id", nullable = false)
	private Integer serConsultationTypeId;

	@Column(name = "ser_cust_id")
	private Integer serCustId;

	/** The event this is about, where there is one. Staff can book without. */
	@Column(name = "ser_event_master_id")
	private Integer serEventMasterId;

	@Column(name = "txt_customer_name", nullable = false)
	private String txtCustomerName;

	@Column(name = "txt_customer_email", nullable = false)
	private String txtCustomerEmail;

	@Column(name = "txt_customer_phone", length = 64)
	private String txtCustomerPhone;

	@Column(name = "txt_notes")
	private String txtNotes;

	@Column(name = "dte_starts_at", nullable = false)
	private Instant dteStartsAt;

	@Column(name = "dte_ends_at", nullable = false)
	private Instant dteEndsAt;

	/**
	 * The zone the customer was looking at when they booked, so a confirmation
	 * can be re-rendered in their time rather than ours.
	 */
	@Column(name = "txt_customer_time_zone", length = 64)
	private String txtCustomerTimeZone;

	@Column(name = "txt_status", nullable = false, length = 32)
	private String txtStatus = STATUS_BOOKED;

	@Column(name = "txt_cancellation_reason")
	private String txtCancellationReason;

	@Column(name = "dte_cancelled_at")
	private Instant dteCancelledAt;

	@Column(name = "txt_management_token", length = 64)
	private String txtManagementToken;

	/** The event created in the host's own calendar, if one was. */
	@Column(name = "txt_external_event_id", length = 512)
	private String txtExternalEventId;

	@Column(name = "txt_external_sync_status", nullable = false, length = 32)
	private String txtExternalSyncStatus = SYNC_PENDING;

	@Column(name = "txt_external_sync_error")
	private String txtExternalSyncError;

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

	public boolean isLive() {
		return STATUS_BOOKED.equals(txtStatus) && !Boolean.TRUE.equals(blnIsDeleted);
	}
}
