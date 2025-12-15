package com.zbs.de.model.dto;

public class DtoPreviewSelection {
	private Long menuItemId;
	private Double qty;
	private Double unitPriceOverride;

	public DtoPreviewSelection() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DtoPreviewSelection(Long menuItemId, Double qty, Double unitPriceOverride) {
		super();
		this.menuItemId = menuItemId;
		this.qty = qty;
		this.unitPriceOverride = unitPriceOverride;
	}

	public Long getMenuItemId() {
		return menuItemId;
	}

	public void setMenuItemId(Long menuItemId) {
		this.menuItemId = menuItemId;
	}

	public Double getQty() {
		return qty;
	}

	public void setQty(Double qty) {
		this.qty = qty;
	}

	public Double getUnitPriceOverride() {
		return unitPriceOverride;
	}

	public void setUnitPriceOverride(Double unitPriceOverride) {
		this.unitPriceOverride = unitPriceOverride;
	}

}
