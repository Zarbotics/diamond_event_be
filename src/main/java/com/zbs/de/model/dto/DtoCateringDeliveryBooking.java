package com.zbs.de.model.dto;

import java.util.List;

import com.zbs.de.model.dto.menu.DtoCustomerMenuCategory;

public class DtoCateringDeliveryBooking {

	private Integer serDeliveryBookingId;
	private String txtDeliveryBookingCode;
	private String dteDeliveryDate;
	private String txtDeliveryTime;
	private String txtDeliveryLocation;
	private String txtSpecialInstructions;
	private Boolean blnRequestMeeting;
	private String txtBookingStatus;
	private Boolean blnBookingStatus;
	private String txtRemarks;

	private Integer serCustId;
	private String txtCustCode;
	private String txtCustName;

	private Integer serEventTypeId;
	private String txtEventTypeCode;
	private String txtEventTypeName;

	private Boolean isEditAllowed;

	private DtoEventQuoteAndStatus dtoEventQuoteAndStatus;

	private List<DtoCateringDeliveryItemDetail> cateringDeliveryItemDetails;

//	private Map<String, List<DtoMenuFoodMaster>> foodSelections;

	private List<DtoMenuFoodMaster> foodSelections;

	private List<DtoCustomerMenuCategory> menuCategoriesSelection;

	public Integer getSerDeliveryBookingId() {
		return serDeliveryBookingId;
	}

	public void setSerDeliveryBookingId(Integer serDeliveryBookingId) {
		this.serDeliveryBookingId = serDeliveryBookingId;
	}

	public String getTxtDeliveryBookingCode() {
		return txtDeliveryBookingCode;
	}

	public void setTxtDeliveryBookingCode(String txtDeliveryBookingCode) {
		this.txtDeliveryBookingCode = txtDeliveryBookingCode;
	}

	public String getDteDeliveryDate() {
		return dteDeliveryDate;
	}

	public void setDteDeliveryDate(String dteDeliveryDate) {
		this.dteDeliveryDate = dteDeliveryDate;
	}

	public String getTxtDeliveryTime() {
		return txtDeliveryTime;
	}

	public void setTxtDeliveryTime(String txtDeliveryTime) {
		this.txtDeliveryTime = txtDeliveryTime;
	}

	public String getTxtDeliveryLocation() {
		return txtDeliveryLocation;
	}

	public void setTxtDeliveryLocation(String txtDeliveryLocation) {
		this.txtDeliveryLocation = txtDeliveryLocation;
	}

	public String getTxtSpecialInstructions() {
		return txtSpecialInstructions;
	}

	public void setTxtSpecialInstructions(String txtSpecialInstructions) {
		this.txtSpecialInstructions = txtSpecialInstructions;
	}

	public Boolean getBlnRequestMeeting() {
		return blnRequestMeeting;
	}

	public void setBlnRequestMeeting(Boolean blnRequestMeeting) {
		this.blnRequestMeeting = blnRequestMeeting;
	}

	public String getTxtBookingStatus() {
		return txtBookingStatus;
	}

	public void setTxtBookingStatus(String txtBookingStatus) {
		this.txtBookingStatus = txtBookingStatus;
	}

	public Boolean getBlnBookingStatus() {
		return blnBookingStatus;
	}

	public void setBlnBookingStatus(Boolean blnBookingStatus) {
		this.blnBookingStatus = blnBookingStatus;
	}

	public String getTxtRemarks() {
		return txtRemarks;
	}

	public void setTxtRemarks(String txtRemarks) {
		this.txtRemarks = txtRemarks;
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

	public List<DtoCateringDeliveryItemDetail> getCateringDeliveryItemDetails() {
		return cateringDeliveryItemDetails;
	}

	public void setCateringDeliveryItemDetails(List<DtoCateringDeliveryItemDetail> cateringDeliveryItemDetails) {
		this.cateringDeliveryItemDetails = cateringDeliveryItemDetails;
	}

	public List<DtoMenuFoodMaster> getFoodSelections() {
		return foodSelections;
	}

	public void setFoodSelections(List<DtoMenuFoodMaster> foodSelections) {
		this.foodSelections = foodSelections;
	}

	public Boolean getIsEditAllowed() {
		return isEditAllowed;
	}

	public void setIsEditAllowed(Boolean isEditAllowed) {
		this.isEditAllowed = isEditAllowed;
	}

	public DtoEventQuoteAndStatus getDtoEventQuoteAndStatus() {
		return dtoEventQuoteAndStatus;
	}

	public void setDtoEventQuoteAndStatus(DtoEventQuoteAndStatus dtoEventQuoteAndStatus) {
		this.dtoEventQuoteAndStatus = dtoEventQuoteAndStatus;
	}

	public List<DtoCustomerMenuCategory> getMenuCategoriesSelection() {
		return menuCategoriesSelection;
	}

	public void setMenuCategoriesSelection(List<DtoCustomerMenuCategory> menuCategoriesSelection) {
		this.menuCategoriesSelection = menuCategoriesSelection;
	}

}
