package com.zbs.de.model.dto;

import java.util.Date;
import java.util.List;

public class DtoPriceCalculationRequest {

	private List<Long> menuItemIds;
	private Integer guestCount;
	private Integer quantity;
	private Integer hours;
	private Date eventDate;
	private Long priceVersionId;
	private String customerTier;
	private String eventType;

	// Getters and Setters
	public List<Long> getMenuItemIds() {
		return menuItemIds;
	}

	public void setMenuItemIds(List<Long> menuItemIds) {
		this.menuItemIds = menuItemIds;
	}

	public Integer getGuestCount() {
		return guestCount;
	}

	public void setGuestCount(Integer guestCount) {
		this.guestCount = guestCount;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Integer getHours() {
		return hours;
	}

	public void setHours(Integer hours) {
		this.hours = hours;
	}

	public Date getEventDate() {
		return eventDate;
	}

	public void setEventDate(Date eventDate) {
		this.eventDate = eventDate;
	}

	public Long getPriceVersionId() {
		return priceVersionId;
	}

	public void setPriceVersionId(Long priceVersionId) {
		this.priceVersionId = priceVersionId;
	}

	public String getCustomerTier() {
		return customerTier;
	}

	public void setCustomerTier(String customerTier) {
		this.customerTier = customerTier;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
}