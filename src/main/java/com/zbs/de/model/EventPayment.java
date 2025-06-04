package com.zbs.de.model;

import jakarta.persistence.Column;
import jakarta.persistence.Id;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.hibernate.annotations.DynamicInsert;

import com.zbs.de.util.enums.EnmPaymentMethod;

@Entity
@DynamicInsert
@Data
@Getter
@Setter
@Table(name = "event_payment")
@NamedQuery(name = "EventPayment.findAll", query = "SELECT a FROM EventPayment a")
public class EventPayment extends BaseEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_payment_id")
	private Integer serPaymentId;

	@ManyToOne
	@JoinColumn(name = "ser_event_id")
	private EventMaster eventMaster;

	@Column(name = "num_amount")
	private BigDecimal numAmount;

	@Column(name = "dte_due_date")
	private Date dteDueDate;

	@Column(name = "dte_paid_date")
	private Date dtePaidDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "enm_payment_method")
	private EnmPaymentMethod enmPaymentMethod;

	@Column(name = "txt_reference")
	private String txtReference;

	public Integer getSerPaymentId() {
		return serPaymentId;
	}

	public void setSerPaymentId(Integer serPaymentId) {
		this.serPaymentId = serPaymentId;
	}

	public EventMaster getEventMaster() {
		return eventMaster;
	}

	public void setEventMaster(EventMaster eventMaster) {
		this.eventMaster = eventMaster;
	}

	public BigDecimal getNumAmount() {
		return numAmount;
	}

	public void setNumAmount(BigDecimal numAmount) {
		this.numAmount = numAmount;
	}

	public Date getDteDueDate() {
		return dteDueDate;
	}

	public void setDteDueDate(Date dteDueDate) {
		this.dteDueDate = dteDueDate;
	}

	public Date getDtePaidDate() {
		return dtePaidDate;
	}

	public void setDtePaidDate(Date dtePaidDate) {
		this.dtePaidDate = dtePaidDate;
	}

	public EnmPaymentMethod getEnmPaymentMethod() {
		return enmPaymentMethod;
	}

	public void setEnmPaymentMethod(EnmPaymentMethod enmPaymentMethod) {
		this.enmPaymentMethod = enmPaymentMethod;
	}

	public String getTxtReference() {
		return txtReference;
	}

	public void setTxtReference(String txtReference) {
		this.txtReference = txtReference;
	}

}
