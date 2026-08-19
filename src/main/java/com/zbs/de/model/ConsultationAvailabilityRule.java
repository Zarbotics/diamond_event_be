package com.zbs.de.model;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * A host's recurring weekly availability, in that host's own time zone.
 *
 * <p>
 * The day is stored ISO-8601 — Monday 1 through Sunday 7 — to match
 * {@link DayOfWeek}, because the arithmetic happens in Java. PostgreSQL's own
 * {@code extract(dow)} counts Sunday as 0, and a system with both conventions
 * in it eventually puts a Monday meeting on a Sunday.
 */
@Entity
@Table(name = "consultation_availability_rule")
@Getter
@Setter
public class ConsultationAvailabilityRule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_availability_rule_id")
	private Integer serAvailabilityRuleId;

	@Column(name = "ser_host_id", nullable = false)
	private Integer serHostId;

	@Column(name = "num_day_of_week", nullable = false)
	private Integer numDayOfWeek;

	@Column(name = "tme_start_time", nullable = false)
	private LocalTime tmeStartTime;

	@Column(name = "tme_end_time", nullable = false)
	private LocalTime tmeEndTime;

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

	public DayOfWeek dayOfWeek() {
		return DayOfWeek.of(numDayOfWeek);
	}
}
