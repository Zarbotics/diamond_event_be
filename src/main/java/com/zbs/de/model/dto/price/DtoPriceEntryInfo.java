package com.zbs.de.model.dto.price;

public class DtoPriceEntryInfo {
	private Long serPriceEntryId;
	private Double numPrice;
	private String txtCurrency;
	private String applyScope;
	private String unit;
	private Integer numMinQuantity;
	private Integer numMaxQuantity;
	private String calculationMethod;

	public Long getSerPriceEntryId() {
		return serPriceEntryId;
	}

	public void setSerPriceEntryId(Long serPriceEntryId) {
		this.serPriceEntryId = serPriceEntryId;
	}

	public Double getNumPrice() {
		return numPrice;
	}

	public void setNumPrice(Double numPrice) {
		this.numPrice = numPrice;
	}

	public String getTxtCurrency() {
		return txtCurrency;
	}

	public void setTxtCurrency(String txtCurrency) {
		this.txtCurrency = txtCurrency;
	}

	public String getApplyScope() {
		return applyScope;
	}

	public void setApplyScope(String applyScope) {
		this.applyScope = applyScope;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public Integer getNumMinQuantity() {
		return numMinQuantity;
	}

	public void setNumMinQuantity(Integer numMinQuantity) {
		this.numMinQuantity = numMinQuantity;
	}

	public Integer getNumMaxQuantity() {
		return numMaxQuantity;
	}

	public void setNumMaxQuantity(Integer numMaxQuantity) {
		this.numMaxQuantity = numMaxQuantity;
	}

	public String getCalculationMethod() {
		return calculationMethod;
	}

	public void setCalculationMethod(String calculationMethod) {
		this.calculationMethod = calculationMethod;
	}

}
