package com.zbs.de.model.dto;

public class DtoDecorExtrasOptionDocument {
	private Integer documentId;
	private String documentName;
	private String documentType;
	private String originalName;
	private String size;
	private byte[] documentFile;
	private String txtDocumentUrl;
	private Integer serExtraOptionId;
	private String txtOptionCode;
	private String txtOptionName;

	public Integer getDocumentId() {
		return documentId;
	}

	public void setDocumentId(Integer documentId) {
		this.documentId = documentId;
	}

	public String getDocumentName() {
		return documentName;
	}

	public void setDocumentName(String documentName) {
		this.documentName = documentName;
	}

	public String getDocumentType() {
		return documentType;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	public String getOriginalName() {
		return originalName;
	}

	public void setOriginalName(String originalName) {
		this.originalName = originalName;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public byte[] getDocumentFile() {
		return documentFile;
	}

	public void setDocumentFile(byte[] documentFile) {
		this.documentFile = documentFile;
	}

	public String getTxtDocumentUrl() {
		return txtDocumentUrl;
	}

	public void setTxtDocumentUrl(String txtDocumentUrl) {
		this.txtDocumentUrl = txtDocumentUrl;
	}

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

}
