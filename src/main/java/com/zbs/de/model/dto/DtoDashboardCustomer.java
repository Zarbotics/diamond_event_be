package com.zbs.de.model.dto;

public class DtoDashboardCustomer {
	private long totalCustomers;
	private long thisMonthCustomers;
	private double monthlyIncreaseRate;

	public long getTotalCustomers() {
		return totalCustomers;
	}

	public void setTotalCustomers(long totalCustomers) {
		this.totalCustomers = totalCustomers;
	}

	public long getThisMonthCustomers() {
		return thisMonthCustomers;
	}

	public void setThisMonthCustomers(long thisMonthCustomers) {
		this.thisMonthCustomers = thisMonthCustomers;
	}

	public double getMonthlyIncreaseRate() {
		return monthlyIncreaseRate;
	}

	public void setMonthlyIncreaseRate(double monthlyIncreaseRate) {
		this.monthlyIncreaseRate = monthlyIncreaseRate;
	}

}
