package com.zbs.de.model.dto;

import java.util.Map;

public class DtoPriceEntry {
	private Long serPriceEntryId;
	private Long priceVersionId;
	private String txtApplyScope; // ITEM|ROLE|...
	private Long numTargetId;
	private Double numPrice;
	private String txtCurrency;
	private String txtUnit;
	private String txtCalculationMethod;
	private Integer numMinQuantity;
	private Integer numMaxQuantity;
	private Map<String, Object> metadata;

	public DtoPriceEntry() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DtoPriceEntry(Long serPriceEntryId, Long priceVersionId, String txtApplyScope, Long numTargetId,
			Double numPrice, String txtCurrency, String txtUnit, String txtCalculationMethod, Integer numMinQuantity,
			Integer numMaxQuantity, Map<String, Object> metadata) {
		super();
		this.serPriceEntryId = serPriceEntryId;
		this.priceVersionId = priceVersionId;
		this.txtApplyScope = txtApplyScope;
		this.numTargetId = numTargetId;
		this.numPrice = numPrice;
		this.txtCurrency = txtCurrency;
		this.txtUnit = txtUnit;
		this.txtCalculationMethod = txtCalculationMethod;
		this.numMinQuantity = numMinQuantity;
		this.numMaxQuantity = numMaxQuantity;
		this.metadata = metadata;
	}

	public Long getSerPriceEntryId() {
		return serPriceEntryId;
	}

	public void setSerPriceEntryId(Long serPriceEntryId) {
		this.serPriceEntryId = serPriceEntryId;
	}

	public Long getPriceVersionId() {
		return priceVersionId;
	}

	public void setPriceVersionId(Long priceVersionId) {
		this.priceVersionId = priceVersionId;
	}

	public String getTxtApplyScope() {
		return txtApplyScope;
	}

	public void setTxtApplyScope(String txtApplyScope) {
		this.txtApplyScope = txtApplyScope;
	}

	public Long getNumTargetId() {
		return numTargetId;
	}

	public void setNumTargetId(Long numTargetId) {
		this.numTargetId = numTargetId;
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

	public String getTxtUnit() {
		return txtUnit;
	}

	public void setTxtUnit(String txtUnit) {
		this.txtUnit = txtUnit;
	}

	public String getTxtCalculationMethod() {
		return txtCalculationMethod;
	}

	public void setTxtCalculationMethod(String txtCalculationMethod) {
		this.txtCalculationMethod = txtCalculationMethod;
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

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

}
