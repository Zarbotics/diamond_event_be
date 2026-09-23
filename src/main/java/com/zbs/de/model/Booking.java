package com.zbs.de.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * One negotiation with one family, which may cover several events.
 *
 * <h2>Why this exists</h2>
 *
 * {@link EventMaster} is doing two jobs: it is the event — a date, a venue, a
 * running order, a guest count, a menu — and it is the booking, carrying the
 * customer, the contact, the budget and the payments.
 *
 * <p>
 * For a customer with one event the two coincide and nothing looks wrong. The
 * case this business actually trades on is a wedding: a mehndi on the Friday, a
 * nikkah on the Saturday, a walima on the Sunday. One family, one negotiation,
 * one deposit — and today three unrelated rows, three budgets, no total, and a
 * deposit with no row to live on.
 *
 * <h2>What this is not, yet</h2>
 *
 * Stages 1 and 2 of §15.3. Every event now has one — those that existed when
 * V11 ran were given a parent by the backfill, and every event created since is
 * given one by {@code ServiceEventMasterImpl.giveItABooking} — but a booking
 * still holds nothing that an event does not. No endpoint returns one, no
 * screen shows one, and no two events share one.
 *
 * <p>
 * That is deliberate rather than unfinished, and the ordering is the point. The
 * budget, the payments and the consultation move up in stage 3, one migration
 * at a time and each leaving a read-through on the event so that existing
 * screens keep working. "Add another day to this wedding" — the thing the
 * business actually asked for, and the reason any of this is happening — is
 * stage 4, and it is only safe once the money has somewhere to live.
 *
 * <p>
 * Nothing here navigates from a booking to its events either. That query
 * arrives with the stage that needs it, written against a requirement rather
 * than in anticipation of one.
 */
@Entity
@Table(name = "booking")
public class Booking extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_booking_id")
	private Long serBookingId;

	/**
	 * The reference a person quotes on the telephone.
	 *
	 * <p>
	 * Backfilled from the event's own code, so a booking created by V11 can be
	 * traced back to the event it came from without a join.
	 */
	@Column(name = "txt_booking_code")
	private String txtBookingCode;

	@ManyToOne
	@JoinColumn(name = "ser_cust_id")
	private CustomerMaster customerMaster;

	/**
	 * How many times this booking has been saved.
	 *
	 * <p>
	 * Maintained by Hibernate, as on {@link EventMaster}. A booking will
	 * eventually carry the budget and the payments, which are exactly the fields
	 * two people end up editing at once.
	 */
	@Version
	@Column(name = "num_version")
	private Long numVersion;

	public Long getSerBookingId() {
		return serBookingId;
	}

	public void setSerBookingId(Long serBookingId) {
		this.serBookingId = serBookingId;
	}

	public String getTxtBookingCode() {
		return txtBookingCode;
	}

	public void setTxtBookingCode(String txtBookingCode) {
		this.txtBookingCode = txtBookingCode;
	}

	public CustomerMaster getCustomerMaster() {
		return customerMaster;
	}

	public void setCustomerMaster(CustomerMaster customerMaster) {
		this.customerMaster = customerMaster;
	}

	public Long getNumVersion() {
		return numVersion;
	}

	/** Present for JPA. Nothing in the application should call it. */
	public void setNumVersion(Long numVersion) {
		this.numVersion = numVersion;
	}
}
