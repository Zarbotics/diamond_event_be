package com.zbs.de.model.dto;

public class DtoItinerarySelection {
	public Long serMenuItemId;
	public Double qty;

	public DtoItinerarySelection() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DtoItinerarySelection(Long serMenuItemId, Double qty) {
		super();
		this.serMenuItemId = serMenuItemId;
		this.qty = qty;
	}

	public Long getSerMenuItemId() {
		return serMenuItemId;
	}

	public void setSerMenuItemId(Long serMenuItemId) {
		this.serMenuItemId = serMenuItemId;
	}

	public Double getQty() {
		return qty;
	}

	public void setQty(Double qty) {
		this.qty = qty;
	}

}
