package com.zbs.de.model;

import java.io.Serializable;
import java.util.Date;

import com.zbs.de.util.enums.EnmNotificationCategory;
import com.zbs.de.util.enums.EnmNotificationPriority;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * One person being told that one thing happened.
 *
 * <h3>Why the recipient is on the row</h3>
 *
 * Because read is a fact about a reader rather than about an event. Two
 * members of staff seeing the same booking arrive each have their own state on
 * it, and a single shared row cannot hold that without a second table carrying
 * exactly what this one carries.
 *
 * <p>
 * The predecessor addressed notifications to {@code getCurrentUserId()} — the
 * person who had just done the thing. That is a receipt, not a notification,
 * and where the thing was done in the customer journey it meant the customer
 * was told about their own booking and the office was told nothing.
 *
 * <h3>Why read is a timestamp</h3>
 *
 * "When did the office first see this" is a question worth being able to
 * answer — about a payment, about a cancellation the day before an event — and
 * a boolean throws it away for nothing gained.
 *
 * <h3>Why dismissed is separate from read</h3>
 *
 * They are different acts. Reading something is not finishing with it; the
 * office reads a booking notification and leaves it in the list until the
 * booking has been dealt with.
 */
@Entity
@Table(name = "notification")
public class Notification extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_notification_id")
	private Long serNotificationId;

	/*
	 * Ids rather than mappings.
	 *
	 * A notification is written once and read as a list. It is never navigated
	 * from, and a ManyToOne here would put a join on the one query that runs on
	 * every page load in the portal.
	 */
	@Column(name = "ser_user_id", nullable = false)
	private Long serUserId;

	@Enumerated(EnumType.STRING)
	@Column(name = "txt_category", nullable = false, length = 40)
	private EnmNotificationCategory txtCategory;

	@Enumerated(EnumType.STRING)
	@Column(name = "txt_priority", nullable = false, length = 20)
	private EnmNotificationPriority txtPriority = EnmNotificationPriority.NORMAL;

	@Column(name = "txt_title", nullable = false, length = 200)
	private String txtTitle;

	@Column(name = "txt_body", length = 500)
	private String txtBody;

	@Column(name = "txt_entity_type", length = 40)
	private String txtEntityType;

	@Column(name = "num_entity_id")
	private Long numEntityId;

	/** A route in the portal, not a path on the API. */
	@Column(name = "txt_route", length = 300)
	private String txtRoute;

	/** Null when the system decided on its own rather than a person acting. */
	@Column(name = "ser_actor_user_id")
	private Long serActorUserId;

	@Column(name = "dte_read_on")
	private Date dteReadOn;

	@Column(name = "dte_dismissed_on")
	private Date dteDismissedOn;

	/** Repeats of the same thing collapse on this. */
	@Column(name = "txt_group_key", length = 120)
	private String txtGroupKey;

	public Long getSerNotificationId() {
		return serNotificationId;
	}

	public void setSerNotificationId(Long serNotificationId) {
		this.serNotificationId = serNotificationId;
	}

	public Long getSerUserId() {
		return serUserId;
	}

	public void setSerUserId(Long serUserId) {
		this.serUserId = serUserId;
	}

	public EnmNotificationCategory getTxtCategory() {
		return txtCategory;
	}

	public void setTxtCategory(EnmNotificationCategory txtCategory) {
		this.txtCategory = txtCategory;
	}

	public EnmNotificationPriority getTxtPriority() {
		return txtPriority;
	}

	public void setTxtPriority(EnmNotificationPriority txtPriority) {
		this.txtPriority = txtPriority;
	}

	public String getTxtTitle() {
		return txtTitle;
	}

	public void setTxtTitle(String txtTitle) {
		this.txtTitle = txtTitle;
	}

	public String getTxtBody() {
		return txtBody;
	}

	public void setTxtBody(String txtBody) {
		this.txtBody = txtBody;
	}

	public String getTxtEntityType() {
		return txtEntityType;
	}

	public void setTxtEntityType(String txtEntityType) {
		this.txtEntityType = txtEntityType;
	}

	public Long getNumEntityId() {
		return numEntityId;
	}

	public void setNumEntityId(Long numEntityId) {
		this.numEntityId = numEntityId;
	}

	public String getTxtRoute() {
		return txtRoute;
	}

	public void setTxtRoute(String txtRoute) {
		this.txtRoute = txtRoute;
	}

	public Long getSerActorUserId() {
		return serActorUserId;
	}

	public void setSerActorUserId(Long serActorUserId) {
		this.serActorUserId = serActorUserId;
	}

	public Date getDteReadOn() {
		return dteReadOn;
	}

	public void setDteReadOn(Date dteReadOn) {
		this.dteReadOn = dteReadOn;
	}

	public Date getDteDismissedOn() {
		return dteDismissedOn;
	}

	public void setDteDismissedOn(Date dteDismissedOn) {
		this.dteDismissedOn = dteDismissedOn;
	}

	public String getTxtGroupKey() {
		return txtGroupKey;
	}

	public void setTxtGroupKey(String txtGroupKey) {
		this.txtGroupKey = txtGroupKey;
	}
}
