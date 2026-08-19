package com.zbs.de.model;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * A kind of meeting, and the rules about booking one.
 *
 * <p>
 * The buffers, the notice and the advance limit are all here rather than in
 * code because they are the settings a business changes without wanting a
 * deployment: a busier month wants more notice, a quieter one less.
 */
@Entity
@Table(name = "consultation_type")
@Getter
@Setter
public class ConsultationType {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_consultation_type_id")
	private Integer serConsultationTypeId;

	@Column(name = "txt_name", nullable = false, length = 160)
	private String txtName;

	@Column(name = "txt_description")
	private String txtDescription;

	@Column(name = "num_duration_minutes", nullable = false)
	private Integer numDurationMinutes;

	/** Kept clear before the meeting and counted as busy. */
	@Column(name = "num_buffer_before_minutes", nullable = false)
	private Integer numBufferBeforeMinutes = 0;

	@Column(name = "num_buffer_after_minutes", nullable = false)
	private Integer numBufferAfterMinutes = 0;

	/** How soon from now a meeting may start. */
	@Column(name = "num_minimum_notice_hours", nullable = false)
	private Integer numMinimumNoticeHours = 24;

	/** And how far ahead. */
	@Column(name = "num_maximum_advance_days", nullable = false)
	private Integer numMaximumAdvanceDays = 90;

	@Column(name = "txt_location_kind", nullable = false, length = 32)
	private String txtLocationKind = "VIDEO";

	@Column(name = "bln_is_active", nullable = false)
	private Boolean blnIsActive = true;

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
}
