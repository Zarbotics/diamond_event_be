package com.zbs.de.model.dto;

import java.math.BigDecimal;

public class DtoEventQuoteAndStatus {

	private BigDecimal numQuotedPrice;
	private BigDecimal numPaidAmount;
	private String txtStatus;

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

}
