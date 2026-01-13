package com.zbs.de.model.dto;

import java.math.BigDecimal;

public class DtoEventDecorPropertySelection {
	private Integer serEventDecorPropertyId;

	private Integer serPropertyId;
	private String txtPropertyCode;
	private String txtPropertyName;
	private BigDecimal numPrice;

	private Integer serPropertyValueId;
	private String txtPropertyValue;

	public Integer getSerEventDecorPropertyId() {
		return serEventDecorPropertyId;
	}

	public void setSerEventDecorPropertyId(Integer serEventDecorPropertyId) {
		this.serEventDecorPropertyId = serEventDecorPropertyId;
	}

	public Integer getSerPropertyId() {
		return serPropertyId;
	}

	public void setSerPropertyId(Integer serPropertyId) {
		this.serPropertyId = serPropertyId;
	}

	public String getTxtPropertyCode() {
		return txtPropertyCode;
	}

	public void setTxtPropertyCode(String txtPropertyCode) {
		this.txtPropertyCode = txtPropertyCode;
	}

	public String getTxtPropertyName() {
		return txtPropertyName;
	}

	public void setTxtPropertyName(String txtPropertyName) {
		this.txtPropertyName = txtPropertyName;
	}

	public Integer getSerPropertyValueId() {
		return serPropertyValueId;
	}

	public void setSerPropertyValueId(Integer serPropertyValueId) {
		this.serPropertyValueId = serPropertyValueId;
	}

	public String getTxtPropertyValue() {
		return txtPropertyValue;
	}

	public void setTxtPropertyValue(String txtPropertyValue) {
		this.txtPropertyValue = txtPropertyValue;
	}

	public BigDecimal getNumPrice() {
		return numPrice;
	}

	public void setNumPrice(BigDecimal numPrice) {
		this.numPrice = numPrice;
	}

}
