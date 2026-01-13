package com.zbs.de.model.dto;

import java.math.BigDecimal;
import java.util.List;

public class DtoEventDecorCategorySelection {
	private Integer serEventDecorCategorySelectionId;
	private String txtEventDecorCategorySelectionCode;

	private Integer serDecorCategoryId;
	private String txtDecorCategoryCode;
	private String txtDecorCategoryName;

	private String txtRemarks;
	private BigDecimal numPrice;

	private List<DtoEventDecorPropertySelection> selectedProperties;
	private List<DtoEventDecorReferenceDocument> userUploadedDocuments;

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

	public String getTxtRemarks() {
		return txtRemarks;
	}

	public void setTxtRemarks(String txtRemarks) {
		this.txtRemarks = txtRemarks;
	}

	public List<DtoEventDecorPropertySelection> getSelectedProperties() {
		return selectedProperties;
	}

	public void setSelectedProperties(List<DtoEventDecorPropertySelection> selectedProperties) {
		this.selectedProperties = selectedProperties;
	}

	public Integer getSerEventDecorCategorySelectionId() {
		return serEventDecorCategorySelectionId;
	}

	public void setSerEventDecorCategorySelectionId(Integer serEventDecorCategorySelectionId) {
		this.serEventDecorCategorySelectionId = serEventDecorCategorySelectionId;
	}

	public String getTxtEventDecorCategorySelectionCode() {
		return txtEventDecorCategorySelectionCode;
	}

	public void setTxtEventDecorCategorySelectionCode(String txtEventDecorCategorySelectionCode) {
		this.txtEventDecorCategorySelectionCode = txtEventDecorCategorySelectionCode;
	}

	public List<DtoEventDecorReferenceDocument> getUserUploadedDocuments() {
		return userUploadedDocuments;
	}

	public void setUserUploadedDocuments(List<DtoEventDecorReferenceDocument> userUploadedDocuments) {
		this.userUploadedDocuments = userUploadedDocuments;
	}

	public BigDecimal getNumPrice() {
		return numPrice;
	}

	public void setNumPrice(BigDecimal numPrice) {
		this.numPrice = numPrice;
	}

}
