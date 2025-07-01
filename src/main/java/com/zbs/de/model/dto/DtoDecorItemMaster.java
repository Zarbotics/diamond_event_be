package com.zbs.de.model.dto;

import java.util.List;

public class DtoDecorItemMaster {

	private Integer serDecorItemId;
	private String txtDecorCode;
	private String txtDecorName;
	private Integer serDecorCategoryId;
	private String txtDecorCategoryCode;
	private String txtDecorCategoryName;
	private Boolean blnIsColorRequired;
	private Boolean blnIsImageRequired;
	private Boolean blnIsCountRequired;
	private Boolean blnIsActive;

	private List<DtoDecorReferenceDocument> dtoDecorReferenceDocumentLst;

	public Integer getSerDecorItemId() {
		return serDecorItemId;
	}

	public void setSerDecorItemId(Integer serDecorItemId) {
		this.serDecorItemId = serDecorItemId;
	}

	public String getTxtDecorCode() {
		return txtDecorCode;
	}

	public void setTxtDecorCode(String txtDecorCode) {
		this.txtDecorCode = txtDecorCode;
	}

	public String getTxtDecorName() {
		return txtDecorName;
	}

	public void setTxtDecorName(String txtDecorName) {
		this.txtDecorName = txtDecorName;
	}

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

	public Boolean getBlnIsColorRequired() {
		return blnIsColorRequired;
	}

	public void setBlnIsColorRequired(Boolean blnIsColorRequired) {
		this.blnIsColorRequired = blnIsColorRequired;
	}

	public Boolean getBlnIsImageRequired() {
		return blnIsImageRequired;
	}

	public void setBlnIsImageRequired(Boolean blnIsImageRequired) {
		this.blnIsImageRequired = blnIsImageRequired;
	}

	public Boolean getBlnIsCountRequired() {
		return blnIsCountRequired;
	}

	public void setBlnIsCountRequired(Boolean blnIsCountRequired) {
		this.blnIsCountRequired = blnIsCountRequired;
	}

	public Boolean getBlnIsActive() {
		return blnIsActive;
	}

	public void setBlnIsActive(Boolean blnIsActive) {
		this.blnIsActive = blnIsActive;
	}

	public List<DtoDecorReferenceDocument> getDtoDecorReferenceDocumentLst() {
		return dtoDecorReferenceDocumentLst;
	}

	public void setDtoDecorReferenceDocumentLst(List<DtoDecorReferenceDocument> dtoDecorReferenceDocumentLst) {
		this.dtoDecorReferenceDocumentLst = dtoDecorReferenceDocumentLst;
	}

}
