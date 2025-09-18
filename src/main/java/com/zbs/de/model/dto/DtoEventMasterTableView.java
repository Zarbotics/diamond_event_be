package com.zbs.de.model.dto;

import java.util.Date;

import com.zbs.de.util.UtilDateAndTime;

public class DtoEventMasterTableView {
	private Integer serEventMasterId;
	private String txtEventMasterCode;
	private String txtEventMasterName;
	private String dteEventDate;

	// Customer
	private Integer serCustId;
	private String txtCustCode;
	private String txtCustName;

	// Event Type
	private Integer serEventTypeId;
	private String txtEventTypeCode;
	private String txtEventTypeName;

	// Venue Master
	private Integer serVenueMasterId;
	private String txtVenueCode;
	private String txtVenueName;

	private Integer serVendorId;
	private String txtVendorCode;
	private String txtVendorName;

	public DtoEventMasterTableView() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DtoEventMasterTableView(Integer serEventMasterId, String txtEventMasterCode, String txtEventMasterName,
			Date dteEventDate, Integer serCustId, String txtCustCode, String txtCustName, Integer serEventTypeId,
			String txtEventTypeCode, String txtEventTypeName, Integer serVenueMasterId, String txtVenueCode,
			String txtVenueName) {
		super();
		this.serEventMasterId = serEventMasterId;
		this.txtEventMasterCode = txtEventMasterCode;
		this.txtEventMasterName = txtEventMasterName;
		this.dteEventDate = UtilDateAndTime.mmddyyyyDateToString(dteEventDate);
		this.serCustId = serCustId;
		this.txtCustCode = txtCustCode;
		this.txtCustName = txtCustName;
		this.serEventTypeId = serEventTypeId;
		this.txtEventTypeCode = txtEventTypeCode;
		this.txtEventTypeName = txtEventTypeName;
		this.serVenueMasterId = serVenueMasterId;
		this.txtVenueCode = txtVenueCode;
		this.txtVenueName = txtVenueName;
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

	public String getDteEventDate() {
		return dteEventDate;
	}

	public void setDteEventDate(String dteEventDate) {
		this.dteEventDate = dteEventDate;
	}

	public Integer getSerCustId() {
		return serCustId;
	}

	public void setSerCustId(Integer serCustId) {
		this.serCustId = serCustId;
	}

	public String getTxtCustCode() {
		return txtCustCode;
	}

	public void setTxtCustCode(String txtCustCode) {
		this.txtCustCode = txtCustCode;
	}

	public String getTxtCustName() {
		return txtCustName;
	}

	public void setTxtCustName(String txtCustName) {
		this.txtCustName = txtCustName;
	}

	public Integer getSerEventTypeId() {
		return serEventTypeId;
	}

	public void setSerEventTypeId(Integer serEventTypeId) {
		this.serEventTypeId = serEventTypeId;
	}

	public String getTxtEventTypeCode() {
		return txtEventTypeCode;
	}

	public void setTxtEventTypeCode(String txtEventTypeCode) {
		this.txtEventTypeCode = txtEventTypeCode;
	}

	public String getTxtEventTypeName() {
		return txtEventTypeName;
	}

	public void setTxtEventTypeName(String txtEventTypeName) {
		this.txtEventTypeName = txtEventTypeName;
	}

	public Integer getSerVenueMasterId() {
		return serVenueMasterId;
	}

	public void setSerVenueMasterId(Integer serVenueMasterId) {
		this.serVenueMasterId = serVenueMasterId;
	}

	public String getTxtVenueCode() {
		return txtVenueCode;
	}

	public void setTxtVenueCode(String txtVenueCode) {
		this.txtVenueCode = txtVenueCode;
	}

	public String getTxtVenueName() {
		return txtVenueName;
	}

	public void setTxtVenueName(String txtVenueName) {
		this.txtVenueName = txtVenueName;
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

}
