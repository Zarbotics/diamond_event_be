package com.zbs.de.model.dto;

public class DtoEventVendorMasterSelection {

	private Integer serEventVendorMasterSelectionId;

	private Integer serVendorId;
	private String txtVendorCode;
	private String txtVendorName;

	private Integer serEventMasterId;
	private String txtEventMasterCode;
	private String txtEventMasterName;

	public Integer getSerEventVendorMasterSelectionId() {
		return serEventVendorMasterSelectionId;
	}

	public void setSerEventVendorMasterSelectionId(Integer serEventVendorMasterSelectionId) {
		this.serEventVendorMasterSelectionId = serEventVendorMasterSelectionId;
	}

	public Integer getSerVendorId() {
		return serVendorId;
	}

	public void setSerVendorId(Integer serVendorId) {
		this.serVendorId = serVendorId;
	}

	public String getTxtVendorCode() {
		return txtVendorCode;
	}

	public void setTxtVendorCode(String txtVendorCode) {
		this.txtVendorCode = txtVendorCode;
	}

	public String getTxtVendorName() {
		return txtVendorName;
	}

	public void setTxtVendorName(String txtVendorName) {
		this.txtVendorName = txtVendorName;
	}

	public Integer getSerEventMasterId() {
		return serEventMasterId;
	}

	public void setSerEventMasterId(Integer serEventMasterId) {
		this.serEventMasterId = serEventMasterId;
	}

	public String getTxtEventMasterCode() {
		return txtEventMasterCode;
	}

	public void setTxtEventMasterCode(String txtEventMasterCode) {
		this.txtEventMasterCode = txtEventMasterCode;
	}

	public String getTxtEventMasterName() {
		return txtEventMasterName;
	}

	public void setTxtEventMasterName(String txtEventMasterName) {
		this.txtEventMasterName = txtEventMasterName;
	}

}
