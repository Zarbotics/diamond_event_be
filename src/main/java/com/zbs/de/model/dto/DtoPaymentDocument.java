package com.zbs.de.model.dto;

public class DtoPaymentDocument {
	private Integer serEventPaymentDocumentId;
	private Integer serEventPaymentId;
	private String txtOriginalFileName;
	private String txtFileName;
	private String txtFilePath;
	private String txtContentType;
	private Long numFileSize;

	// getters / setters
	public Integer getSerEventPaymentDocumentId() {
		return serEventPaymentDocumentId;
	}

	public void setSerEventPaymentDocumentId(Integer serEventPaymentDocumentId) {
		this.serEventPaymentDocumentId = serEventPaymentDocumentId;
	}

	public Integer getSerEventPaymentId() {
		return serEventPaymentId;
	}

	public void setSerEventPaymentId(Integer serEventPaymentId) {
		this.serEventPaymentId = serEventPaymentId;
	}

	public String getTxtFileName() {
		return txtFileName;
	}

	public void setTxtFileName(String txtFileName) {
		this.txtFileName = txtFileName;
	}

	public String getTxtFilePath() {
		return txtFilePath;
	}

	public void setTxtFilePath(String txtFilePath) {
		this.txtFilePath = txtFilePath;
	}

	public String getTxtContentType() {
		return txtContentType;
	}

	public void setTxtContentType(String txtContentType) {
		this.txtContentType = txtContentType;
	}

	public Long getNumFileSize() {
		return numFileSize;
	}

	public void setNumFileSize(Long numFileSize) {
		this.numFileSize = numFileSize;
	}

	public String getTxtOriginalFileName() {
		return txtOriginalFileName;
	}

	public void setTxtOriginalFileName(String txtOriginalFileName) {
		this.txtOriginalFileName = txtOriginalFileName;
	}

}
