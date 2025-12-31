package com.zbs.de.model.dto.price;

import java.util.Map;

public class DtoPriceEntryGrid {
	private Long serPriceEntryId;
	private Long numTargetId;
	private String txtTargetName;
	private String txtTargetCode;
	private String applyScope;
	private Double numPrice;
	private String txtCurrency;
	private String unit;
	private Integer numMinQuantity;
	private Integer numMaxQuantity;
	private String calculationMethod;
	private Map<String, Object> metadata;

	public Long getSerPriceEntryId() {
		return serPriceEntryId;
	}

	public void setSerPriceEntryId(Long serPriceEntryId) {
		this.serPriceEntryId = serPriceEntryId;
	}

	public Long getNumTargetId() {
		return numTargetId;
	}

	public void setNumTargetId(Long numTargetId) {
		this.numTargetId = numTargetId;
	}

	public String getTxtTargetName() {
		return txtTargetName;
	}

	public void setTxtTargetName(String txtTargetName) {
		this.txtTargetName = txtTargetName;
	}

	public String getTxtTargetCode() {
		return txtTargetCode;
	}

	public void setTxtTargetCode(String txtTargetCode) {
		this.txtTargetCode = txtTargetCode;
	}

	public String getApplyScope() {
		return applyScope;
	}

	public void setApplyScope(String applyScope) {
		this.applyScope = applyScope;
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

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

}
