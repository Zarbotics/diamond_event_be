package com.zbs.de.model;

import jakarta.persistence.*;

/**
 * One moment a running order has, for one kind of event.
 *
 * <h3>Why this is a table and not a map in the client</h3>
 *
 * It was a map in the control panel's booking form, keyed by
 * {@code ser_event_type_id} — a surrogate key the business assigns by adding
 * rows on the Event Types screen. Behaviour keyed to a business-editable id
 * drifts, and it had: the sixteen wedding moments were filed under the id that
 * is "Corporate Event" in this database, while "Wedding" got a six-moment list
 * with no Nikah and no Barat arrival.
 *
 * <p>
 * It never showed, because the line that read the map used the literal 2 with
 * the line that uses the booking's own type commented out above it. Every
 * booking got the sixteen whatever it was for. Which is the only reason the
 * drift did no harm — and is exactly why uncommenting that line would have.
 *
 * <p>
 * As data, the question "which moments does a Walima have" is answered by the
 * business, on the screen where they already define what a Walima is.
 */
@Entity
@Table(name = "event_type_running_order_moment")
public class EventTypeRunningOrderMoment extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_moment_id")
	private Integer serMomentId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_event_type_id", nullable = false)
	private EventType eventType;

	/** The column on {@code event_running_order} this fills, e.g. "txtNikah". */
	@Column(name = "txt_field", nullable = false, length = 64)
	private String txtField;

	@Column(name = "num_display_order", nullable = false)
	private Integer numDisplayOrder;

	public Integer getSerMomentId() {
		return serMomentId;
	}

	public void setSerMomentId(Integer serMomentId) {
		this.serMomentId = serMomentId;
	}

	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	public String getTxtField() {
		return txtField;
	}

	public void setTxtField(String txtField) {
		this.txtField = txtField;
	}

	public Integer getNumDisplayOrder() {
		return numDisplayOrder;
	}

	public void setNumDisplayOrder(Integer numDisplayOrder) {
		this.numDisplayOrder = numDisplayOrder;
	}
}
