package com.zbs.de.model;

import java.math.BigDecimal;
import java.util.Date;

import com.zbs.de.util.enums.EnmPaymentMethod;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

public class PaymentTransaction {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_payment_id")
	private Integer serPaymentId;

	@ManyToOne
	@JoinColumn(name = "ser_event_master_id")
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
}
