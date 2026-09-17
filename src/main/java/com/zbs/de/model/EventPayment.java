package com.zbs.de.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "event_payment")
public class EventPayment extends BaseEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_event_payment_id")
	private Integer serEventPaymentId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_event_budget_id", nullable = false)
	private EventBudget eventBudget;

	/**
	 * The booking this payment was taken against.
	 *
	 * <p>
	 * Stage 3 of §15.3. A deposit is paid for a wedding, not for the Saturday:
	 * today the money hangs off one event's budget, so a family paying £2,000
	 * across a mehndi, a nikkah and a walima has it recorded against one of the
	 * three days, and every total the office works from is a day's total rather
	 * than the wedding's.
	 *
	 * <p>
	 * One booking still has one event, so nothing observable changes yet. That
	 * is the method rather than an accident — the column is filled while it
	 * cannot matter, so that stage 4 is a change to behaviour and not a change
	 * to behaviour plus a migration of live payment records.
	 *
	 * <p>
	 * {@code updatable = false}, for the reason {@code EventMaster.serBookingId}
	 * spells out: this codebase saves detached entities built from DTOs, and no
	 * DTO carries a booking id. With the column updatable, the first save of
	 * each payment would write NULL over its parent and undo the backfill one
	 * row at a time, silently, starting with the bookings people touch most.
	 *
	 * <p>
	 * A plain id rather than a {@code @ManyToOne}, because nothing needs to
	 * navigate from a payment to its booking yet — only to group by it. The
	 * association can arrive with stage 4, which is the first thing that reads
	 * payments across more than one event.
	 */
	@Column(name = "ser_booking_id", updatable = false)
	private Long serBookingId;

	@Column(name = "ser_event_master_id")
	private Integer serEventMasterId; // optional duplicate for faster queries

	@Column(name = "ser_delivery_booking_id")
	private Integer serDeliveryBookingId; // optional duplicate for faster queries

	@Column(name = "num_amount", precision = 18, scale = 2, nullable = false)
	private BigDecimal numAmount;

	@Column(name = "txt_payment_mode")
	private String txtPaymentMode;

	@Column(name = "txt_transaction_ref")
	private String txtTransactionRef;

	@Column(name = "dte_payment_date")
	private Date dtePaymentDate;

	@Column(name = "txt_payment_status")
	private String txtPaymentStatus;

	@Column(name = "txt_remarks", columnDefinition = "TEXT")
	private String txtRemarks;

	@OneToMany(mappedBy = "eventPayment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<EventPaymentDocument> documents = new ArrayList<>();

	public Integer getSerEventPaymentId() {
		return serEventPaymentId;
	}

	public void setSerEventPaymentId(Integer serEventPaymentId) {
		this.serEventPaymentId = serEventPaymentId;
	}

	public EventBudget getEventBudget() {
		return eventBudget;
	}

	public void setEventBudget(EventBudget eventBudget) {
		this.eventBudget = eventBudget;
	}

	/**
	 * Files this payment under a budget, and under the booking that budget
	 * belongs to.
	 *
	 * <p>
	 * The two are set together, here, rather than by each caller in turn. This
	 * file has a history of one routine existing in several near-identical
	 * copies that drift apart — the paid-amount field had twelve of them and
	 * one was wrong — and "set the budget, then work out the booking" is exactly
	 * the shape that goes wrong the sixth time somebody adds a payment path.
	 *
	 * <p>
	 * A catering delivery has no event and therefore no booking: its budget
	 * hangs off {@code catering_delivery_booking}, which is a different thing
	 * with a confusingly similar name. Those payments keep a null booking, and
	 * that is correct rather than missing — they are not part of a wedding.
	 *
	 * <p>
	 * Only the insert matters: the column is {@code updatable = false}, so
	 * Hibernate ignores this on a save of an existing payment. That is what
	 * stops a re-save writing null over a backfilled parent.
	 */
	public void attachTo(EventBudget budget) {
		this.eventBudget = budget;

		if (budget != null && budget.getEventMaster() != null) {
			this.serBookingId = budget.getEventMaster().getSerBookingId();
		}
	}

	public Integer getSerEventMasterId() {
		return serEventMasterId;
	}

	public void setSerEventMasterId(Integer serEventMasterId) {
		this.serEventMasterId = serEventMasterId;
	}

	public BigDecimal getNumAmount() {
		return numAmount;
	}

	public void setNumAmount(BigDecimal numAmount) {
		this.numAmount = numAmount;
	}

	public String getTxtPaymentMode() {
		return txtPaymentMode;
	}

	public void setTxtPaymentMode(String txtPaymentMode) {
		this.txtPaymentMode = txtPaymentMode;
	}

	public String getTxtTransactionRef() {
		return txtTransactionRef;
	}

	public void setTxtTransactionRef(String txtTransactionRef) {
		this.txtTransactionRef = txtTransactionRef;
	}

	public Date getDtePaymentDate() {
		return dtePaymentDate;
	}

	public void setDtePaymentDate(Date dtePaymentDate) {
		this.dtePaymentDate = dtePaymentDate;
	}

	public String getTxtPaymentStatus() {
		return txtPaymentStatus;
	}

	public void setTxtPaymentStatus(String txtPaymentStatus) {
		this.txtPaymentStatus = txtPaymentStatus;
	}

	public String getTxtRemarks() {
		return txtRemarks;
	}

	public void setTxtRemarks(String txtRemarks) {
		this.txtRemarks = txtRemarks;
	}

	public List<EventPaymentDocument> getDocuments() {
		return documents;
	}

	public void setDocuments(List<EventPaymentDocument> documents) {
		this.documents = documents;
	}

	public void addDocument(EventPaymentDocument doc) {
		this.documents.add(doc);
		doc.setEventPayment(this);
	}

	public void removeDocument(EventPaymentDocument doc) {
		this.documents.remove(doc);
		doc.setEventPayment(null);
	}

	public Long getSerBookingId() {
		return serBookingId;
	}

	public void setSerBookingId(Long serBookingId) {
		this.serBookingId = serBookingId;
	}

	public Integer getSerDeliveryBookingId() {
		return serDeliveryBookingId;
	}

	public void setSerDeliveryBookingId(Integer serDeliveryBookingId) {
		this.serDeliveryBookingId = serDeliveryBookingId;
	}

}
