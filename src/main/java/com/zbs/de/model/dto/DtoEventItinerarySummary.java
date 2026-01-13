package com.zbs.de.model.dto;

public class DtoEventItinerarySummary {
	private Long serEventItinerarySummaryId;

	private Integer serEventMasterId;
	private String txtEventMasterCode;
	private String txtEventMasterName;

	private Long serItineraryItemId;
	private String txtItineraryItemCode;
	private String txtItineraryItemName;
	private Integer serItineraryItemTypeId;
	private String txtItineraryItemTypeName;

	private Integer numTotalQuantity;

	public DtoEventItinerarySummary() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DtoEventItinerarySummary(Long serEventItinerarySummaryId, Integer serEventMasterId,
			String txtEventMasterCode, String txtEventMasterName, Long serItineraryItemId, String txtItineraryItemCode,
			String txtItineraryItemName, Integer serItineraryItemTypeId, String txtItineraryItemTypeName,
			Integer numTotalQuantity) {
		super();
		this.serEventItinerarySummaryId = serEventItinerarySummaryId;
		this.serEventMasterId = serEventMasterId;
		this.txtEventMasterCode = txtEventMasterCode;
		this.txtEventMasterName = txtEventMasterName;
		this.serItineraryItemId = serItineraryItemId;
		this.txtItineraryItemCode = txtItineraryItemCode;
		this.txtItineraryItemName = txtItineraryItemName;
		this.serItineraryItemTypeId = serItineraryItemTypeId;
		this.txtItineraryItemTypeName = txtItineraryItemTypeName;
		this.numTotalQuantity = numTotalQuantity;
	}

	public Long getSerEventItinerarySummaryId() {
		return serEventItinerarySummaryId;
	}

	public void setSerEventItinerarySummaryId(Long serEventItinerarySummaryId) {
		this.serEventItinerarySummaryId = serEventItinerarySummaryId;
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

	public Integer getNumTotalQuantity() {
		return numTotalQuantity;
	}

	public void setNumTotalQuantity(Integer numTotalQuantity) {
		this.numTotalQuantity = numTotalQuantity;
	}

}
