package com.zbs.de.model;

import java.time.Instant;
import java.time.ZoneId;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Somebody who takes consultations.
 *
 * <p>
 * The time zone is stored per host because working hours are a statement about
 * their local clock — "nine to five" stays nine to five when the clocks change,
 * which means the UTC instants it maps to move by an hour twice a year.
 */
@Entity
@Table(name = "consultation_host")
@Getter
@Setter
public class ConsultationHost {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_host_id")
	private Integer serHostId;

	@Column(name = "ser_user_id")
	private Integer serUserId;

	@Column(name = "txt_display_name", nullable = false, length = 160)
	private String txtDisplayName;

	@Column(name = "txt_email", nullable = false)
	private String txtEmail;

	@Column(name = "txt_time_zone", nullable = false, length = 64)
	private String txtTimeZone = "Europe/London";

	@Column(name = "bln_is_active", nullable = false)
	private Boolean blnIsActive = true;

	@Column(name = "bln_is_deleted", nullable = false)
	private Boolean blnIsDeleted = false;

	/** Round-robin assignment picks whoever has waited longest. */
	@Column(name = "dte_last_assigned")
	private Instant dteLastAssigned;

	@Column(name = "created_date", nullable = false)
	private Instant createdDate = Instant.now();

	@Column(name = "updated_date", nullable = false)
	private Instant updatedDate = Instant.now();

	@Column(name = "created_by", nullable = false)
	private Integer createdBy = 0;

	@Column(name = "updated_by", nullable = false)
	private Integer updatedBy = 0;

	/** Falls back to UK time rather than throwing on a bad stored value. */
	public ZoneId zone() {
		try {
			return ZoneId.of(txtTimeZone);
		} catch (Exception e) {
			return ZoneId.of("Europe/London");
		}
	}
}
