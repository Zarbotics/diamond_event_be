package com.zbs.de.model.dto;

import java.util.List;

public class DtoEventMaster {
	private Integer serEventMasterId;
	private String txtEventMasterCode;
	private String txtEventMasterName;
	private String dteEventDate;
	private Integer numNumberOfGuests;
	private String txtNumberOfGuests;
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

	private String txtEventRemarks;
	private String txtDecoreRemarks;
	private String txtCateringRemarks;
	private String txtExternalSupplierRemarks;
	private String txtEventExtrasRemarks;
	private String txtVenueRemarks;

	private String dteCreatedDate;
	private String dteUpdateDate;

	private DtoEventRunningOrder dtoEventRunningOrder;
	private DtoEventVenue dtoEventVenue;

	private List<DtoEventDecorCategorySelection> dtoEventDecorSelections;
	private List<DtoEventMenuFoodSelection> foodSelections;

	private List<DtoEventDecorExtrasSelection> extrasSelections;

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

	public List<DtoEventMenuFoodSelection> getFoodSelections() {
		return foodSelections;
	}

	public void setFoodSelections(List<DtoEventMenuFoodSelection> foodSelections) {
		this.foodSelections = foodSelections;
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

	public String getTxtNumberOfGuests() {
		return txtNumberOfGuests;
	}

	public void setTxtNumberOfGuests(String txtNumberOfGuests) {
		this.txtNumberOfGuests = txtNumberOfGuests;
	}

	public String getTxtOtherEventType() {
		return txtOtherEventType;
	}

	public void setTxtOtherEventType(String txtOtherEventType) {
		this.txtOtherEventType = txtOtherEventType;
	}

	public DtoEventVenue getDtoEventVenue() {
		return dtoEventVenue;
	}

	public void setDtoEventVenue(DtoEventVenue dtoEventVenue) {
		this.dtoEventVenue = dtoEventVenue;
	}

	public List<DtoEventDecorCategorySelection> getDtoEventDecorSelections() {
		return dtoEventDecorSelections;
	}

	public void setDtoEventDecorSelections(List<DtoEventDecorCategorySelection> dtoEventDecorSelections) {
		this.dtoEventDecorSelections = dtoEventDecorSelections;
	}

	public Boolean getBlnIsActive() {
		return blnIsActive;
	}

	public void setBlnIsActive(Boolean blnIsActive) {
		this.blnIsActive = blnIsActive;
	}

	public String getTxtEventStatus() {
		return txtEventStatus;
	}

	public void setTxtEventStatus(String txtEventStatus) {
		this.txtEventStatus = txtEventStatus;
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

	public Boolean getBlnIsCouple() {
		return blnIsCouple;
	}

	public void setBlnIsCouple(Boolean blnIsCouple) {
		this.blnIsCouple = blnIsCouple;
	}

	public List<DtoEventDecorExtrasSelection> getExtrasSelections() {
		return extrasSelections;
	}

	public void setExtrasSelections(List<DtoEventDecorExtrasSelection> extrasSelections) {
		this.extrasSelections = extrasSelections;
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

	public String getDteCreatedDate() {
		return dteCreatedDate;
	}

	public void setDteCreatedDate(String dteCreatedDate) {
		this.dteCreatedDate = dteCreatedDate;
	}

	public String getDteUpdateDate() {
		return dteUpdateDate;
	}

	public void setDteUpdateDate(String dteUpdateDate) {
		this.dteUpdateDate = dteUpdateDate;
	}

}
