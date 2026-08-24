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
 * Stage 1 of §15.3. The table exists and every event that existed when V11 ran
 * has a parent, but <strong>nothing reads it</strong>: no association is mapped
 * on {@code EventMaster}, no endpoint returns a booking, no screen shows one.
 *
 * <p>
 * That is deliberate rather than unfinished. This stage changes no behaviour
 * and is undone by dropping a column, so it can go to production on its own
 * while the stages that do change behaviour are built and reviewed separately.
 *
 * <p>
 * In particular the association is <em>not</em> mapped on {@code EventMaster}
 * on purpose. Hibernate writes every mapped column on an update, so an
 * association nothing populates would write NULL over the backfill on the first
 * save of each event — quietly undoing the migration one booking at a time.
 * Stage 2 adds the mapping together with the code that maintains it.
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
