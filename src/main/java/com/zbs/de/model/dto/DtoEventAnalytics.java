package com.zbs.de.model.dto;

import java.math.BigDecimal;

public class DtoEventAnalytics {
	private String txtMonth; // For monthly grouping
	private String txtEventType; // Optional (used in eventType-wise profit)
	private BigDecimal numTotalSales; // Total Revenue (Budget)
	private BigDecimal numTotalProfit; // Total Profit
	private BigDecimal numTotalExpense; // Total Expense

	public DtoEventAnalytics() {
	}

	public DtoEventAnalytics(String month, BigDecimal totalSales) {
		this.txtMonth = month;
		this.numTotalSales = totalSales;
	}

	public DtoEventAnalytics(String month, String eventType, BigDecimal totalProfit) {
		this.txtMonth = month;
		this.txtEventType = eventType;
		this.numTotalProfit = totalProfit;
	}

	public DtoEventAnalytics(BigDecimal totalSales, BigDecimal totalProfit, BigDecimal totalExpense) {
		this.numTotalSales = totalSales;
		this.numTotalProfit = totalProfit;
		this.numTotalExpense = totalExpense;
	}

	public String getTxtMonth() {
		return txtMonth;
	}

	public void setTxtMonth(String txtMonth) {
		this.txtMonth = txtMonth;
	}

	public String getTxtEventType() {
		return txtEventType;
	}

	public void setTxtEventType(String txtEventType) {
		this.txtEventType = txtEventType;
	}

	public BigDecimal getNumTotalSales() {
		return numTotalSales;
	}

	public void setNumTotalSales(BigDecimal numTotalSales) {
		this.numTotalSales = numTotalSales;
	}

	public BigDecimal getNumTotalProfit() {
		return numTotalProfit;
	}

	public void setNumTotalProfit(BigDecimal numTotalProfit) {
		this.numTotalProfit = numTotalProfit;
	}

	public BigDecimal getNumTotalExpense() {
		return numTotalExpense;
	}

	public void setNumTotalExpense(BigDecimal numTotalExpense) {
		this.numTotalExpense = numTotalExpense;
	}

}
