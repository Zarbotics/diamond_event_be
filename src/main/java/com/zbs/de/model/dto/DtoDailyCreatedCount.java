package com.zbs.de.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtoDailyCreatedCount {
	private String date; // yyyy-MM-dd
	private int customers;
	private int events;

	public DtoDailyCreatedCount(String date, int customers, int events) {
		super();
		this.date = date;
		this.customers = customers;
		this.events = events;
	}

	public DtoDailyCreatedCount() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public int getCustomers() {
		return customers;
	}

	public void setCustomers(int customers) {
		this.customers = customers;
	}

	public int getEvents() {
		return events;
	}

	public void setEvents(int events) {
		this.events = events;
	}

}
