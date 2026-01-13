package com.zbs.de.model.dto;

import java.math.BigDecimal;
import java.util.List;

public class DtoDecorCategoryPropertyMaster {
	private Integer serPropertyId;
	private String txtPropertyName;
	private String txtInputType;
	private String txtRemarks;
	private Boolean blnIsRequired;
	private Boolean blnIsActive;
	private Integer serDecorCategoryId;
	private String txtDecorCategoryCode;
	private String txtDecorCategoryName;
	private BigDecimal numPrice;
	private List<DtoDecorCategoryPropertyValue> propertyValues;

	public Integer getSerPropertyId() {
		return serPropertyId;
	}

	public void setSerPropertyId(Integer serPropertyId) {
		this.serPropertyId = serPropertyId;
	}

	public String getTxtPropertyName() {
		return txtPropertyName;
	}

	public void setTxtPropertyName(String txtPropertyName) {
		this.txtPropertyName = txtPropertyName;
	}

	public String getTxtInputType() {
		return txtInputType;
	}

	public void setTxtInputType(String txtInputType) {
		this.txtInputType = txtInputType;
	}

	public Integer getSerDecorCategoryId() {
		return serDecorCategoryId;
	}

	public void setSerDecorCategoryId(Integer serDecorCategoryId) {
		this.serDecorCategoryId = serDecorCategoryId;
	}

	public List<DtoDecorCategoryPropertyValue> getPropertyValues() {
		return propertyValues;
	}

	public void setPropertyValues(List<DtoDecorCategoryPropertyValue> propertyValues) {
		this.propertyValues = propertyValues;
	}

	public String getTxtRemarks() {
		return txtRemarks;
	}

	public void setTxtRemarks(String txtRemarks) {
		this.txtRemarks = txtRemarks;
	}

	public Boolean getBlnIsRequired() {
		return blnIsRequired;
	}

	public void setBlnIsRequired(Boolean blnIsRequired) {
		this.blnIsRequired = blnIsRequired;
	}

	public Boolean getBlnIsActive() {
		return blnIsActive;
	}

	public void setBlnIsActive(Boolean blnIsActive) {
		this.blnIsActive = blnIsActive;
	}

	public String getTxtDecorCategoryCode() {
		return txtDecorCategoryCode;
	}

	public void setTxtDecorCategoryCode(String txtDecorCategoryCode) {
		this.txtDecorCategoryCode = txtDecorCategoryCode;
	}

	public String getTxtDecorCategoryName() {
		return txtDecorCategoryName;
	}

	public void setTxtDecorCategoryName(String txtDecorCategoryName) {
		this.txtDecorCategoryName = txtDecorCategoryName;
	}

	public BigDecimal getNumPrice() {
		return numPrice;
	}

	public void setNumPrice(BigDecimal numPrice) {
		this.numPrice = numPrice;
	}

}
