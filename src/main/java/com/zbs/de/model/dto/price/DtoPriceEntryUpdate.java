package com.zbs.de.model.dto.price;

import java.util.Map;

public class DtoPriceEntryUpdate {
	private Long serPriceEntryId;
	private Double numPrice;
	private String txtCurrency;
	private Integer numMinQuantity;
	private Integer numMaxQuantity;
	private Map<String, Object> metadata;

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
