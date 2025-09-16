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

	@Column(name = "ser_event_master_id")
	private Integer serEventMasterId; // optional duplicate for faster queries

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
}
