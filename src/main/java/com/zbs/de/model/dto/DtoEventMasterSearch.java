package com.zbs.de.model.dto;

public class DtoEventMasterSearch {
	private Integer serEventMasterId;
	private String txtEventMasterCode;
	private String txtEventMasterName;

	// date exact OR range
	private String dteEventDate; // yyyy-MM-dd (exact)
	private String dteEventDateFrom; // yyyy-MM-dd
	private String dteEventDateTo; // yyyy-MM-dd

	// numeric ranges
	private Integer numNumberOfGuests;
	private Integer minNumberOfGuests;
	private Integer maxNumberOfGuests;
	private Integer numNumberOfTables;
	private Integer numInfoFilledStatus;

	private String txtBrideName;
	private String txtBrideFirstName;
	private String txtBrideLastName;
	private String txtGroomName;
	private String txtGroomFirstName;
	private String txtGroomLastName;
	private String txtBirthDayCelebrant;
	private String txtAgeCategory;
	private String txtChiefGuest;
	private String txtEventStatus;
	private Boolean blnIsActive;
	private Boolean blnIsCouple;

	// joined entities
	private Integer serCustId;
	private String txtCustCode;
	private String txtCustName;
	private Integer serEventTypeId;
	private String txtEventTypeCode;
	private String txtEventTypeName;
	private String txtOtherEventType;
	private Integer serVenueMasterId;
	private String txtVenueCode;
	private String txtVenueName;
	private Integer serVendorId;
	private String txtVendorCode;
	private String txtVendorName;

	// remarks filters
	private String txtEventRemarks;
	private String txtDecoreRemarks;
	private String txtCateringRemarks;
	private String txtExternalSupplierRemarks;
	private String txtEventExtrasRemarks;
	private String txtVenueRemarks;

	// metafilters
	private String dteCreatedDateFrom;
	private String dteCreatedDateTo;
	private String dteUpdateDateFrom;
	private String dteUpdateDateTo;

	private String txtBudgetStatus;

	// helpful extras for UI
	private String q; // global text search across multiple columns
	private Integer page = 0;
	private Integer size = 20;
	private String sortBy = "dteEventDate";
	private String sortDir = "DESC";

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

	public String getDteEventDateFrom() {
		return dteEventDateFrom;
	}

	public void setDteEventDateFrom(String dteEventDateFrom) {
		this.dteEventDateFrom = dteEventDateFrom;
	}

	public String getDteEventDateTo() {
		return dteEventDateTo;
	}

	public void setDteEventDateTo(String dteEventDateTo) {
		this.dteEventDateTo = dteEventDateTo;
	}

	public Integer getNumNumberOfGuests() {
		return numNumberOfGuests;
	}

	public void setNumNumberOfGuests(Integer numNumberOfGuests) {
		this.numNumberOfGuests = numNumberOfGuests;
	}

	public Integer getMinNumberOfGuests() {
		return minNumberOfGuests;
	}

	public void setMinNumberOfGuests(Integer minNumberOfGuests) {
		this.minNumberOfGuests = minNumberOfGuests;
	}

	public Integer getMaxNumberOfGuests() {
		return maxNumberOfGuests;
	}

	public void setMaxNumberOfGuests(Integer maxNumberOfGuests) {
		this.maxNumberOfGuests = maxNumberOfGuests;
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

	public String getTxtBrideName() {
		return txtBrideName;
	}

	public void setTxtBrideName(String txtBrideName) {
		this.txtBrideName = txtBrideName;
	}

	public String getTxtBrideFirstName() {
		return txtBrideFirstName;
	}

	public void setTxtBrideFirstName(String txtBrideFirstName) {
		this.txtBrideFirstName = txtBrideFirstName;
	}

	public String getTxtBrideLastName() {
		return txtBrideLastName;
	}

	public void setTxtBrideLastName(String txtBrideLastName) {
		this.txtBrideLastName = txtBrideLastName;
	}

	public String getTxtGroomName() {
		return txtGroomName;
	}

	public void setTxtGroomName(String txtGroomName) {
		this.txtGroomName = txtGroomName;
	}

	public String getTxtGroomFirstName() {
		return txtGroomFirstName;
	}

	public void setTxtGroomFirstName(String txtGroomFirstName) {
		this.txtGroomFirstName = txtGroomFirstName;
	}

	public String getTxtGroomLastName() {
		return txtGroomLastName;
	}

	public void setTxtGroomLastName(String txtGroomLastName) {
		this.txtGroomLastName = txtGroomLastName;
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

	public String getTxtEventStatus() {
		return txtEventStatus;
	}

	public void setTxtEventStatus(String txtEventStatus) {
		this.txtEventStatus = txtEventStatus;
	}

	public Boolean getBlnIsActive() {
		return blnIsActive;
	}

	public void setBlnIsActive(Boolean blnIsActive) {
		this.blnIsActive = blnIsActive;
	}

	public Boolean getBlnIsCouple() {
		return blnIsCouple;
	}

	public void setBlnIsCouple(Boolean blnIsCouple) {
		this.blnIsCouple = blnIsCouple;
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

	public String getTxtOtherEventType() {
		return txtOtherEventType;
	}

	public void setTxtOtherEventType(String txtOtherEventType) {
		this.txtOtherEventType = txtOtherEventType;
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

	public String getTxtEventRemarks() {
		return txtEventRemarks;
	}

	public void setTxtEventRemarks(String txtEventRemarks) {
		this.txtEventRemarks = txtEventRemarks;
	}

	public String getTxtDecoreRemarks() {
		return txtDecoreRemarks;
	}

	public void setTxtDecoreRemarks(String txtDecoreRemarks) {
		this.txtDecoreRemarks = txtDecoreRemarks;
	}

	public String getTxtCateringRemarks() {
		return txtCateringRemarks;
	}

	public void setTxtCateringRemarks(String txtCateringRemarks) {
		this.txtCateringRemarks = txtCateringRemarks;
	}

	public String getTxtExternalSupplierRemarks() {
		return txtExternalSupplierRemarks;
	}

	public void setTxtExternalSupplierRemarks(String txtExternalSupplierRemarks) {
		this.txtExternalSupplierRemarks = txtExternalSupplierRemarks;
	}

	public String getTxtEventExtrasRemarks() {
		return txtEventExtrasRemarks;
	}

	public void setTxtEventExtrasRemarks(String txtEventExtrasRemarks) {
		this.txtEventExtrasRemarks = txtEventExtrasRemarks;
	}

	public String getTxtVenueRemarks() {
		return txtVenueRemarks;
	}

	public void setTxtVenueRemarks(String txtVenueRemarks) {
		this.txtVenueRemarks = txtVenueRemarks;
	}

	public String getDteCreatedDateFrom() {
		return dteCreatedDateFrom;
	}

	public void setDteCreatedDateFrom(String dteCreatedDateFrom) {
		this.dteCreatedDateFrom = dteCreatedDateFrom;
	}

	public String getDteCreatedDateTo() {
		return dteCreatedDateTo;
	}

	public void setDteCreatedDateTo(String dteCreatedDateTo) {
		this.dteCreatedDateTo = dteCreatedDateTo;
	}

	public String getDteUpdateDateFrom() {
		return dteUpdateDateFrom;
	}

	public void setDteUpdateDateFrom(String dteUpdateDateFrom) {
		this.dteUpdateDateFrom = dteUpdateDateFrom;
	}

	public String getDteUpdateDateTo() {
		return dteUpdateDateTo;
	}

	public void setDteUpdateDateTo(String dteUpdateDateTo) {
		this.dteUpdateDateTo = dteUpdateDateTo;
	}

	public String getQ() {
		return q;
	}

	public void setQ(String q) {
		this.q = q;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public String getSortBy() {
		return sortBy;
	}

	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}

	public String getSortDir() {
		return sortDir;
	}

	public void setSortDir(String sortDir) {
		this.sortDir = sortDir;
	}

	public String getTxtBudgetStatus() {
		return txtBudgetStatus;
	}

	public void setTxtBudgetStatus(String txtBudgetStatus) {
		this.txtBudgetStatus = txtBudgetStatus;
	}

}
