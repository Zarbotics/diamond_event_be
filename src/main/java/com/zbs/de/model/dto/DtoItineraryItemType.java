package com.zbs.de.model.dto;

public class DtoItineraryItemType {
	private Integer serItineraryItemTypeId;
	private String txtCode;
	private String txtName;
	private Boolean blnIsActive;

	public DtoItineraryItemType() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DtoItineraryItemType(Integer serItineraryItemTypeId, String txtCode, String txtName, Boolean blnIsActive) {
		super();
		this.serItineraryItemTypeId = serItineraryItemTypeId;
		this.txtCode = txtCode;
		this.txtName = txtName;
		this.blnIsActive = blnIsActive;
	}

	public Integer getSerItineraryItemTypeId() {
		return serItineraryItemTypeId;
	}

	public void setSerItineraryItemTypeId(Integer serItineraryItemTypeId) {
		this.serItineraryItemTypeId = serItineraryItemTypeId;
	}

	public String getTxtCode() {
		return txtCode;
	}

	public void setTxtCode(String txtCode) {
		this.txtCode = txtCode;
	}

	public String getTxtName() {
		return txtName;
	}

	public void setTxtName(String txtName) {
		this.txtName = txtName;
	}

	public Boolean getBlnIsActive() {
		return blnIsActive;
	}

	public void setBlnIsActive(Boolean blnIsActive) {
		this.blnIsActive = blnIsActive;
	}

}
