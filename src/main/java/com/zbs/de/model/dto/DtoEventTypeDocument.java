package com.zbs.de.model.dto;

public class DtoEventTypeDocument {
	private Integer documentId;
	private String documentName;
	private String documentType;
	private String originalName;
	private String size;
	private byte[] documentFile;
	private String txtDocumentUrl;

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

}
