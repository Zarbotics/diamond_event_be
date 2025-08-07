package com.zbs.de.model.dto;

public class DtoDecorExtrasOption {

	private Integer serExtraOptionId;
	private String txtOptionCode;
	private String txtOptionName;
	private Boolean blnIsDocument;
	private Boolean blnIsActive;
	private DtoDecorExtrasOptionDocument document;

	public Integer getSerExtraOptionId() {
		return serExtraOptionId;
	}

	public void setSerExtraOptionId(Integer serExtraOptionId) {
		this.serExtraOptionId = serExtraOptionId;
	}

	public String getTxtOptionCode() {
		return txtOptionCode;
	}

	public void setTxtOptionCode(String txtOptionCode) {
		this.txtOptionCode = txtOptionCode;
	}

	public String getTxtOptionName() {
		return txtOptionName;
	}

	public void setTxtOptionName(String txtOptionName) {
		this.txtOptionName = txtOptionName;
	}

	public Boolean getBlnIsDocument() {
		return blnIsDocument;
	}

	public void setBlnIsDocument(Boolean blnIsDocument) {
		this.blnIsDocument = blnIsDocument;
	}

	public Boolean getBlnIsActive() {
		return blnIsActive;
	}

	public void setBlnIsActive(Boolean blnIsActive) {
		this.blnIsActive = blnIsActive;
	}

	public DtoDecorExtrasOptionDocument getDocument() {
		return document;
	}

	public void setDocument(DtoDecorExtrasOptionDocument document) {
		this.document = document;
	}

}
