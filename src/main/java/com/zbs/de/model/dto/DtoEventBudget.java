package com.zbs.de.model.dto;

import java.math.BigDecimal;

public class DtoEventBudget {

	private Integer serEventBudgetId;
	private Integer serEventMasterId;

	private BigDecimal numTotalBudget;
	private BigDecimal numTotalExpense;
	private BigDecimal numTotalProfit;

	private String txtPaymentType;
	private String txtPaymentStatus;
	private String dteDealDate;
	private String txtDealClosedBy;
	private String txtRemarks;

	public Integer getSerEventBudgetId() {
		return serEventBudgetId;
	}

	public void setSerEventBudgetId(Integer serEventBudgetId) {
		this.serEventBudgetId = serEventBudgetId;
	}

	public Integer getSerEventMasterId() {
		return serEventMasterId;
	}

	public void setSerEventMasterId(Integer serEventMasterId) {
		this.serEventMasterId = serEventMasterId;
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

	public String getDteDealDate() {
		return dteDealDate;
	}

	public void setDteDealDate(String dteDealDate) {
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

}
