package com.zbs.de.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtoDashboard {
	// Customers
	private long totalCustomers;
	private long customersThisMonth;
	private DtoPercentageChange customersMonthlyRate;

	// Events
	private long totalEvents;
	private long eventsThisMonth;
	private DtoPercentageChange eventsMonthlyRate;

	// Date wise created counts (created_date)
	private List<DtoDailyCreatedCount> createdCountsThisMonth;

	// Total sales (sum of num_paid_amount) overall
	private BigDecimal totalSales;

	// Events grouped by event type with sale
	private List<DtoTypeSales> eventTypeStats;

	// Events by dte_event_date and sale for current month
	private List<DtoDailyDateEventSale> eventsByEventDateThisMonth;

	public DtoDashboard() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DtoDashboard(long totalCustomers, long customersThisMonth, DtoPercentageChange customersMonthlyRate,
			long totalEvents, long eventsThisMonth, DtoPercentageChange eventsMonthlyRate,
			List<DtoDailyCreatedCount> createdCountsThisMonth, BigDecimal totalSales, List<DtoTypeSales> eventTypeStats,
			List<DtoDailyDateEventSale> eventsByEventDateThisMonth) {
		super();
		this.totalCustomers = totalCustomers;
		this.customersThisMonth = customersThisMonth;
		this.customersMonthlyRate = customersMonthlyRate;
		this.totalEvents = totalEvents;
		this.eventsThisMonth = eventsThisMonth;
		this.eventsMonthlyRate = eventsMonthlyRate;
		this.createdCountsThisMonth = createdCountsThisMonth;
		this.totalSales = totalSales;
		this.eventTypeStats = eventTypeStats;
		this.eventsByEventDateThisMonth = eventsByEventDateThisMonth;
	}

	public long getTotalCustomers() {
		return totalCustomers;
	}

	public void setTotalCustomers(long totalCustomers) {
		this.totalCustomers = totalCustomers;
	}

	public long getCustomersThisMonth() {
		return customersThisMonth;
	}

	public void setCustomersThisMonth(long customersThisMonth) {
		this.customersThisMonth = customersThisMonth;
	}

	public DtoPercentageChange getCustomersMonthlyRate() {
		return customersMonthlyRate;
	}

	public void setCustomersMonthlyRate(DtoPercentageChange customersMonthlyRate) {
		this.customersMonthlyRate = customersMonthlyRate;
	}

	public long getTotalEvents() {
		return totalEvents;
	}

	public void setTotalEvents(long totalEvents) {
		this.totalEvents = totalEvents;
	}

	public long getEventsThisMonth() {
		return eventsThisMonth;
	}

	public void setEventsThisMonth(long eventsThisMonth) {
		this.eventsThisMonth = eventsThisMonth;
	}

	public DtoPercentageChange getEventsMonthlyRate() {
		return eventsMonthlyRate;
	}

	public void setEventsMonthlyRate(DtoPercentageChange eventsMonthlyRate) {
		this.eventsMonthlyRate = eventsMonthlyRate;
	}

	public List<DtoDailyCreatedCount> getCreatedCountsThisMonth() {
		return createdCountsThisMonth;
	}

	public void setCreatedCountsThisMonth(List<DtoDailyCreatedCount> createdCountsThisMonth) {
		this.createdCountsThisMonth = createdCountsThisMonth;
	}

	public BigDecimal getTotalSales() {
		return totalSales;
	}

	public void setTotalSales(BigDecimal totalSales) {
		this.totalSales = totalSales;
	}

	public List<DtoTypeSales> getEventTypeStats() {
		return eventTypeStats;
	}

	public void setEventTypeStats(List<DtoTypeSales> eventTypeStats) {
		this.eventTypeStats = eventTypeStats;
	}

	public List<DtoDailyDateEventSale> getEventsByEventDateThisMonth() {
		return eventsByEventDateThisMonth;
	}

	public void setEventsByEventDateThisMonth(List<DtoDailyDateEventSale> eventsByEventDateThisMonth) {
		this.eventsByEventDateThisMonth = eventsByEventDateThisMonth;
	}

}
