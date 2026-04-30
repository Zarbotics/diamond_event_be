package com.zbs.de.model.dto;

import java.math.BigDecimal;

public class DtoEventQuoteAndStatus {

	private BigDecimal numQuotedPrice;
	private BigDecimal numPaidAmount;
	private BigDecimal numPendingAmount;
	private BigDecimal numDiscount;
	private String txtStatus;
	private BigDecimal numFoodAmount;
	private BigDecimal numServicesAmount;
	private BigDecimal numDecorAmount;
	private BigDecimal numDecorExtrasVat;
	private BigDecimal numFinalAmount;

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

	public BigDecimal getNumPendingAmount() {
		return numPendingAmount;
	}

	public void setNumPendingAmount(BigDecimal numPendingAmount) {
		this.numPendingAmount = numPendingAmount;
	}

	public BigDecimal getNumDiscount() {
		return numDiscount;
	}

	public void setNumDiscount(BigDecimal numDiscount) {
		this.numDiscount = numDiscount;
	}

	public BigDecimal getNumFoodAmount() {
		return numFoodAmount;
	}

	public void setNumFoodAmount(BigDecimal numFoodAmount) {
		this.numFoodAmount = numFoodAmount;
	}

	public BigDecimal getNumServicesAmount() {
		return numServicesAmount;
	}

	public void setNumServicesAmount(BigDecimal numServicesAmount) {
		this.numServicesAmount = numServicesAmount;
	}

	public BigDecimal getNumDecorAmount() {
		return numDecorAmount;
	}

	public void setNumDecorAmount(BigDecimal numDecorAmount) {
		this.numDecorAmount = numDecorAmount;
	}

	public BigDecimal getNumDecorExtrasVat() {
		return numDecorExtrasVat;
	}

	public void setNumDecorExtrasVat(BigDecimal numDecorExtrasVat) {
		this.numDecorExtrasVat = numDecorExtrasVat;
	}

	public BigDecimal getNumFinalAmount() {
		return numFinalAmount;
	}

	public void setNumFinalAmount(BigDecimal numFinalAmount) {
		this.numFinalAmount = numFinalAmount;
	}

}
