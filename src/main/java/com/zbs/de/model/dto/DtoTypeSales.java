package com.zbs.de.model.dto;

import java.math.BigDecimal;

public class DtoTypeSales {
	private Integer eventTypeId;
	private String eventTypeName;
	private long totalEvents;
	private BigDecimal totalSale;

	public DtoTypeSales() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DtoTypeSales(Integer eventTypeId, String eventTypeName, long totalEvents, BigDecimal totalSale) {
		super();
		this.eventTypeId = eventTypeId;
		this.eventTypeName = eventTypeName;
		this.totalEvents = totalEvents;
		this.totalSale = totalSale;
	}

	public Integer getEventTypeId() {
		return eventTypeId;
	}

	public void setEventTypeId(Integer eventTypeId) {
		this.eventTypeId = eventTypeId;
	}

	public String getEventTypeName() {
		return eventTypeName;
	}

	public void setEventTypeName(String eventTypeName) {
		this.eventTypeName = eventTypeName;
	}

	public long getTotalEvents() {
		return totalEvents;
	}

	public void setTotalEvents(long totalEvents) {
		this.totalEvents = totalEvents;
	}

	public BigDecimal getTotalSale() {
		return totalSale;
	}

	public void setTotalSale(BigDecimal totalSale) {
		this.totalSale = totalSale;
	}

}
