package com.zbs.de.model.dto;

import java.math.BigDecimal;

public class DtoDailyDateEventSale {
	private String date; // yyyy-MM-dd (dte_event_date)
	private long events;
	private BigDecimal sale;

	public DtoDailyDateEventSale() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DtoDailyDateEventSale(String date, long events, BigDecimal sale) {
		super();
		this.date = date;
		this.events = events;
		this.sale = sale;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public long getEvents() {
		return events;
	}

	public void setEvents(long events) {
		this.events = events;
	}

	public BigDecimal getSale() {
		return sale;
	}

	public void setSale(BigDecimal sale) {
		this.sale = sale;
	}

}
