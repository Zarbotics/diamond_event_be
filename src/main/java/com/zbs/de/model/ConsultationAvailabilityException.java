package com.zbs.de.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * A one-off departure from the weekly pattern.
 *
 * <p>
 * {@code blnIsAvailable = false} closes the day entirely — a bank holiday, or
 * leave. {@code true} opens hours the weekly rules do not cover, such as a
 * single Saturday. Both are needed: a business that can only close cannot open
 * for a wedding fair, and one that can only open cannot shut for Christmas.
 */
@Entity
@Table(name = "consultation_availability_exception")
@Getter
@Setter
public class ConsultationAvailabilityException {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_availability_exception_id")
	private Integer serAvailabilityExceptionId;

	@Column(name = "ser_host_id", nullable = false)
	private Integer serHostId;

	@Column(name = "dte_on_date", nullable = false)
	private LocalDate dteOnDate;

	@Column(name = "bln_is_available", nullable = false)
	private Boolean blnIsAvailable = false;

	@Column(name = "tme_start_time")
	private LocalTime tmeStartTime;

	@Column(name = "tme_end_time")
	private LocalTime tmeEndTime;

	@Column(name = "txt_reason")
	private String txtReason;

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
