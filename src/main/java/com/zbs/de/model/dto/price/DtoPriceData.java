package com.zbs.de.model.dto.price;

public class DtoPriceData {
	private Double numPrice;
	private String txtCurrency = "GBP";
	private String unit = "PER_GUEST";
	private Integer numMinQuantity;
	private Integer numMaxQuantity;
	private String calculationMethod = "DIRECT";

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
