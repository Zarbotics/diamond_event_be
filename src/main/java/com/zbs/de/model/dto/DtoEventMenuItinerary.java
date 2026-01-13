package com.zbs.de.model.dto;

public class DtoEventMenuItinerary {
	private Long serEventMenuItineraryId;

	private Integer serEventMasterId;
	private String txtEventMasterCode;
	private String txtEventMasterName;

	private Long serMenuItemId;
	private String txtMenuItemCode;
	private String txtMenuItemName;
	private String txtMenuItemShortName;
	private String txtMenuItemDescription;

	private Long serItineraryItemId;
	private String txtItineraryItemCode;
	private String txtItineraryItemName;
	private Integer serItineraryItemTypeId;
	private String txtItineraryItemTypeName;

	private Integer numCalculatedQuantity;

	public DtoEventMenuItinerary() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DtoEventMenuItinerary(Long serEventMenuItineraryId, Integer serEventMasterId, String txtEventMasterCode,
			String txtEventMasterName, Long serMenuItemId, String txtMenuItemCode, String txtMenuItemName,
			String txtMenuItemShortName, String txtMenuItemDescription, Long serItineraryItemId,
			String txtItineraryItemCode, String txtItineraryItemName, Integer serItineraryItemTypeId,
			String txtItineraryItemTypeName, Integer numCalculatedQuantity) {
		super();
		this.serEventMenuItineraryId = serEventMenuItineraryId;
		this.serEventMasterId = serEventMasterId;
		this.txtEventMasterCode = txtEventMasterCode;
		this.txtEventMasterName = txtEventMasterName;
		this.serMenuItemId = serMenuItemId;
		this.txtMenuItemCode = txtMenuItemCode;
		this.txtMenuItemName = txtMenuItemName;
		this.txtMenuItemShortName = txtMenuItemShortName;
		this.txtMenuItemDescription = txtMenuItemDescription;
		this.serItineraryItemId = serItineraryItemId;
		this.txtItineraryItemCode = txtItineraryItemCode;
		this.txtItineraryItemName = txtItineraryItemName;
		this.serItineraryItemTypeId = serItineraryItemTypeId;
		this.txtItineraryItemTypeName = txtItineraryItemTypeName;
		this.numCalculatedQuantity = numCalculatedQuantity;
	}

	public Long getSerEventMenuItineraryId() {
		return serEventMenuItineraryId;
	}

	public void setSerEventMenuItineraryId(Long serEventMenuItineraryId) {
		this.serEventMenuItineraryId = serEventMenuItineraryId;
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

	public Long getSerMenuItemId() {
		return serMenuItemId;
	}

	public void setSerMenuItemId(Long serMenuItemId) {
		this.serMenuItemId = serMenuItemId;
	}

	public String getTxtMenuItemCode() {
		return txtMenuItemCode;
	}

	public void setTxtMenuItemCode(String txtMenuItemCode) {
		this.txtMenuItemCode = txtMenuItemCode;
	}

	public String getTxtMenuItemName() {
		return txtMenuItemName;
	}

	public void setTxtMenuItemName(String txtMenuItemName) {
		this.txtMenuItemName = txtMenuItemName;
	}

	public String getTxtMenuItemShortName() {
		return txtMenuItemShortName;
	}

	public void setTxtMenuItemShortName(String txtMenuItemShortName) {
		this.txtMenuItemShortName = txtMenuItemShortName;
	}

	public String getTxtMenuItemDescription() {
		return txtMenuItemDescription;
	}

	public void setTxtMenuItemDescription(String txtMenuItemDescription) {
		this.txtMenuItemDescription = txtMenuItemDescription;
	}

	public Long getSerItineraryItemId() {
		return serItineraryItemId;
	}

	public void setSerItineraryItemId(Long serItineraryItemId) {
		this.serItineraryItemId = serItineraryItemId;
	}

	public String getTxtItineraryItemCode() {
		return txtItineraryItemCode;
	}

	public void setTxtItineraryItemCode(String txtItineraryItemCode) {
		this.txtItineraryItemCode = txtItineraryItemCode;
	}

	public String getTxtItineraryItemName() {
		return txtItineraryItemName;
	}

	public void setTxtItineraryItemName(String txtItineraryItemName) {
		this.txtItineraryItemName = txtItineraryItemName;
	}

	public Integer getSerItineraryItemTypeId() {
		return serItineraryItemTypeId;
	}

	public void setSerItineraryItemTypeId(Integer serItineraryItemTypeId) {
		this.serItineraryItemTypeId = serItineraryItemTypeId;
	}

	public String getTxtItineraryItemTypeName() {
		return txtItineraryItemTypeName;
	}

	public void setTxtItineraryItemTypeName(String txtItineraryItemTypeName) {
		this.txtItineraryItemTypeName = txtItineraryItemTypeName;
	}

	public Integer getNumCalculatedQuantity() {
		return numCalculatedQuantity;
	}

	public void setNumCalculatedQuantity(Integer numCalculatedQuantity) {
		this.numCalculatedQuantity = numCalculatedQuantity;
	}

}
