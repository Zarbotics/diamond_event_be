package com.zbs.de.model.dto;

public class DtoEventMaster {
	private Integer serEventMasterId;
	private String txtEventMasterCode;
	private String txtEventMasterName;
	private String dteEventDate;
	private Integer numNumberOfGuests;
	private Integer numNumberOfTables;
	private Integer numInfoFilledStatus;
	private String txtBrideName;
	private String txtGroomName;
	private String txtBirthDayCelebrant;
	private String txtAgeCategory;
	private String txtChiefGuest;

	private Integer serCustId;
	private String txtCustCode;
	private String txtCustName;

	private DtoEventRunningOrder dtoEventRunningOrder;

	private Integer serEventTypeId;
	private String txtEventTypeCode;
	private String txtEventTypeName;

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

	public Integer getNumNumberOfGuests() {
		return numNumberOfGuests;
	}

	public void setNumNumberOfGuests(Integer numNumberOfGuests) {
		this.numNumberOfGuests = numNumberOfGuests;
	}

	public Integer getNumNumberOfTables() {
		return numNumberOfTables;
	}

	public void setNumNumberOfTables(Integer numNumberOfTables) {
		this.numNumberOfTables = numNumberOfTables;
	}

	public Integer getNumInfoFilledStatus() {
		return numInfoFilledStatus;
	}

	public void setNumInfoFilledStatus(Integer numInfoFilledStatus) {
		this.numInfoFilledStatus = numInfoFilledStatus;
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

	public String getTxtBrideName() {
		return txtBrideName;
	}

	public void setTxtBrideName(String txtBrideName) {
		this.txtBrideName = txtBrideName;
	}

	public String getTxtGroomName() {
		return txtGroomName;
	}

	public void setTxtGroomName(String txtGroomName) {
		this.txtGroomName = txtGroomName;
	}

	public String getTxtBirthDayCelebrant() {
		return txtBirthDayCelebrant;
	}

	public void setTxtBirthDayCelebrant(String txtBirthDayCelebrant) {
		this.txtBirthDayCelebrant = txtBirthDayCelebrant;
	}

	public String getTxtAgeCategory() {
		return txtAgeCategory;
	}

	public void setTxtAgeCategory(String txtAgeCategory) {
		this.txtAgeCategory = txtAgeCategory;
	}

	public String getTxtChiefGuest() {
		return txtChiefGuest;
	}

	public void setTxtChiefGuest(String txtChiefGuest) {
		this.txtChiefGuest = txtChiefGuest;
	}

	public DtoEventRunningOrder getDtoEventRunningOrder() {
		return dtoEventRunningOrder;
	}

	public void setDtoEventRunningOrder(DtoEventRunningOrder dtoEventRunningOrder) {
		this.dtoEventRunningOrder = dtoEventRunningOrder;
	}

}
