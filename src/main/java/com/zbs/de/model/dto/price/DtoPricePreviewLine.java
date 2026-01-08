package com.zbs.de.model.dto.price;

public class DtoPricePreviewLine {

	private Long menuItemId;
	private Double qty;
	private Double unitPrice;
	private Double lineTotal;
	private Long appliedPriceEntryId;

	public DtoPricePreviewLine() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DtoPricePreviewLine(Long menuItemId, Double qty, Double unitPrice, Double lineTotal,
			Long appliedPriceEntryId) {
		super();
		this.menuItemId = menuItemId;
		this.qty = qty;
		this.unitPrice = unitPrice;
		this.lineTotal = lineTotal;
		this.appliedPriceEntryId = appliedPriceEntryId;
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

	public Double getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(Double unitPrice) {
		this.unitPrice = unitPrice;
	}

	public Double getLineTotal() {
		return lineTotal;
	}

	public void setLineTotal(Double lineTotal) {
		this.lineTotal = lineTotal;
	}

	public Long getAppliedPriceEntryId() {
		return appliedPriceEntryId;
	}

	public void setAppliedPriceEntryId(Long appliedPriceEntryId) {
		this.appliedPriceEntryId = appliedPriceEntryId;
	}

}
