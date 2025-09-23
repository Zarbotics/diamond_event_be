package com.zbs.de.model.dto;

import java.math.BigDecimal;
import java.util.List;

public class DtoPayment {
	private Integer serEventPaymentId;
	private Integer serEventBudgetId;
	private Integer serEventMasterId;
	private BigDecimal numAmount;
	private String txtPaymentMode;
	private String txtTransactionRef;
	private String dtePaymentDate; // ISO string or dd-MM-yyyy per your util
	private String txtPaymentStatus;
	private String txtRemarks;
	private List<DtoPaymentDocument> documents;

	// getters / setters
	public Integer getSerEventPaymentId() {
		return serEventPaymentId;
	}

	public void setSerEventPaymentId(Integer serEventPaymentId) {
		this.serEventPaymentId = serEventPaymentId;
	}

	public Integer getSerEventBudgetId() {
		return serEventBudgetId;
	}

	public void setSerEventBudgetId(Integer serEventBudgetId) {
		this.serEventBudgetId = serEventBudgetId;
	}

	public Integer getSerEventMasterId() {
		return serEventMasterId;
	}

	public void setSerEventMasterId(Integer serEventMasterId) {
		this.serEventMasterId = serEventMasterId;
	}

	public BigDecimal getNumAmount() {
		return numAmount;
	}

	public void setNumAmount(BigDecimal numAmount) {
		this.numAmount = numAmount;
	}

	public String getTxtPaymentMode() {
		return txtPaymentMode;
	}

	public void setTxtPaymentMode(String txtPaymentMode) {
		this.txtPaymentMode = txtPaymentMode;
	}

	public String getTxtTransactionRef() {
		return txtTransactionRef;
	}

	public void setTxtTransactionRef(String txtTransactionRef) {
		this.txtTransactionRef = txtTransactionRef;
	}

	public String getDtePaymentDate() {
		return dtePaymentDate;
	}

	public void setDtePaymentDate(String dtePaymentDate) {
		this.dtePaymentDate = dtePaymentDate;
	}

	public String getTxtPaymentStatus() {
		return txtPaymentStatus;
	}

	public void setTxtPaymentStatus(String txtPaymentStatus) {
		this.txtPaymentStatus = txtPaymentStatus;
	}

	public String getTxtRemarks() {
		return txtRemarks;
	}

	public void setTxtRemarks(String txtRemarks) {
		this.txtRemarks = txtRemarks;
	}

	public List<DtoPaymentDocument> getDocuments() {
		return documents;
	}

	public void setDocuments(List<DtoPaymentDocument> documents) {
		this.documents = documents;
	}
}
