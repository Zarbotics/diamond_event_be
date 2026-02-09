package com.zbs.de.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "event_budget")
@NamedQuery(name = "EventBudget.findAll", query = "SELECT a FROM EventBudget a")
public class EventBudget extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_event_budget_id")
	private Integer serEventBudgetId;

	// owning side: FK to event_master
	@JsonIgnore
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_event_master_id")
	private EventMaster eventMaster;

	@JsonIgnore
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_delivery_booking_id")
	private CateringDeliveryBooking cateringDeliveryBooking;

	@Column(name = "num_total_budget", precision = 18, scale = 2)
	private BigDecimal numTotalBudget;

	@Column(name = "num_total_expense", precision = 18, scale = 2)
	private BigDecimal numTotalExpense;

	@Column(name = "num_total_profit", precision = 18, scale = 2)
	private BigDecimal numTotalProfit;

	@Column(name = "txt_payment_type")
	private String txtPaymentType;

	@Column(name = "txt_payment_status")
	private String txtPaymentStatus;

	@Column(name = "dte_deal_date")
	private Date dteDealDate;

	@Column(name = "txt_deal_closed_by")
	private String txtDealClosedBy;

	@Column(name = "txt_remarks", columnDefinition = "TEXT")
	private String txtRemarks;

	@Column(name = "num_qouted_price")
	private BigDecimal numQuotedPrice;

	@Column(name = "num_paid_amount", precision = 18, scale = 2)
	private BigDecimal numPaidAmount;

	@Column(name = "txt_status")
	private String txtStatus;

	@OneToMany(mappedBy = "eventBudget", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<EventPayment> payments = new ArrayList<>();

	public Integer getSerEventBudgetId() {
		return serEventBudgetId;
	}

	public void setSerEventBudgetId(Integer serEventBudgetId) {
		this.serEventBudgetId = serEventBudgetId;
	}

	public EventMaster getEventMaster() {
		return eventMaster;
	}

	public void setEventMaster(EventMaster eventMaster) {
		this.eventMaster = eventMaster;
	}

	public BigDecimal getNumTotalBudget() {
		return numTotalBudget;
	}

	public void setNumTotalBudget(BigDecimal numTotalBudget) {
		this.numTotalBudget = numTotalBudget;
	}

	public BigDecimal getNumTotalExpense() {
		return numTotalExpense;
	}

	public void setNumTotalExpense(BigDecimal numTotalExpense) {
		this.numTotalExpense = numTotalExpense;
	}

	public BigDecimal getNumTotalProfit() {
		return numTotalProfit;
	}

	public void setNumTotalProfit(BigDecimal numTotalProfit) {
		this.numTotalProfit = numTotalProfit;
	}

	public String getTxtPaymentType() {
		return txtPaymentType;
	}

	public void setTxtPaymentType(String txtPaymentType) {
		this.txtPaymentType = txtPaymentType;
	}

	public String getTxtPaymentStatus() {
		return txtPaymentStatus;
	}

	public void setTxtPaymentStatus(String txtPaymentStatus) {
		this.txtPaymentStatus = txtPaymentStatus;
	}

	public Date getDteDealDate() {
		return dteDealDate;
	}

	public void setDteDealDate(Date dteDealDate) {
		this.dteDealDate = dteDealDate;
	}

	public String getTxtDealClosedBy() {
		return txtDealClosedBy;
	}

	public void setTxtDealClosedBy(String txtDealClosedBy) {
		this.txtDealClosedBy = txtDealClosedBy;
	}

	public String getTxtRemarks() {
		return txtRemarks;
	}

	public void setTxtRemarks(String txtRemarks) {
		this.txtRemarks = txtRemarks;
	}

	public BigDecimal getNumQuotedPrice() {
		return numQuotedPrice;
	}

	public void setNumQuotedPrice(BigDecimal numQuotedPrice) {
		this.numQuotedPrice = numQuotedPrice;
	}

	public BigDecimal getNumPaidAmount() {
		return numPaidAmount;
	}

	public void setNumPaidAmount(BigDecimal numPaidAmount) {
		this.numPaidAmount = numPaidAmount;
	}

	public String getTxtStatus() {
		return txtStatus;
	}

	public void setTxtStatus(String txtStatus) {
		this.txtStatus = txtStatus;
	}

	public List<EventPayment> getPayments() {
		return payments;
	}

	public void setPayments(List<EventPayment> payments) {
		this.payments = payments;
	}

	public void addPayment(EventPayment p) {
		this.payments.add(p);
		p.setEventBudget(this);
	}

	public void removePayment(EventPayment p) {
		this.payments.remove(p);
		p.setEventBudget(null);
	}

	public CateringDeliveryBooking getCateringDeliveryBooking() {
		return cateringDeliveryBooking;
	}

	public void setCateringDeliveryBooking(CateringDeliveryBooking cateringDeliveryBooking) {
		this.cateringDeliveryBooking = cateringDeliveryBooking;
	}

}
