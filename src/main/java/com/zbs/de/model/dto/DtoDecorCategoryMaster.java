package com.zbs.de.model.dto;

import java.math.BigDecimal;
import java.util.List;

public class DtoDecorCategoryMaster {

	private Integer serDecorCategoryId;
	private String txtDecorCategoryCode;
	private String txtDecorCategoryName;
	private Boolean blnIsActive;
	private BigDecimal numPrice;
	private List<DtoDecorCategoryPropertyMaster> categoryProperties;
	private List<DtoDecorCategoryReferenceDocument> referenceDocuments;

	public Integer getSerDecorCategoryId() {
		return serDecorCategoryId;
	}

	public void setSerDecorCategoryId(Integer serDecorCategoryId) {
		this.serDecorCategoryId = serDecorCategoryId;
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

	public Boolean getBlnIsActive() {
		return blnIsActive;
	}

	public void setBlnIsActive(Boolean blnIsActive) {
		this.blnIsActive = blnIsActive;
	}

	public List<DtoDecorCategoryPropertyMaster> getCategoryProperties() {
		return categoryProperties;
	}

	public void setCategoryProperties(List<DtoDecorCategoryPropertyMaster> categoryProperties) {
		this.categoryProperties = categoryProperties;
	}

	public List<DtoDecorCategoryReferenceDocument> getReferenceDocuments() {
		return referenceDocuments;
	}

	public void setReferenceDocuments(List<DtoDecorCategoryReferenceDocument> referenceDocuments) {
		this.referenceDocuments = referenceDocuments;
	}

	public BigDecimal getNumPrice() {
		return numPrice;
	}

	public void setNumPrice(BigDecimal numPrice) {
		this.numPrice = numPrice;
	}

}
