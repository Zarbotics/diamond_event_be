package com.zbs.de.model.dto;

import java.util.Map;

public class DtoItineraryItem {
	private Long serItineraryItemId;
	private String txtCode;
	private String txtName;
	private String txtItemType;
	private Boolean blnIsActive;
	private Map<String, Object> metadata;

	public DtoItineraryItem() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DtoItineraryItem(Long serItineraryItemId, String txtCode, String txtName, String txtItemType,
			Boolean blnIsActive, Map<String, Object> metadata) {
		super();
		this.serItineraryItemId = serItineraryItemId;
		this.txtCode = txtCode;
		this.txtName = txtName;
		this.txtItemType = txtItemType;
		this.metadata = metadata;
		this.blnIsActive = blnIsActive;
	}

	public Long getSerItineraryItemId() {
		return serItineraryItemId;
	}

	public void setSerItineraryItemId(Long serItineraryItemId) {
		this.serItineraryItemId = serItineraryItemId;
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

	public String getTxtItemType() {
		return txtItemType;
	}

	public void setTxtItemType(String txtItemType) {
		this.txtItemType = txtItemType;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	public Boolean getBlnIsActive() {
		return blnIsActive;
	}

	public void setBlnIsActive(Boolean blnIsActive) {
		this.blnIsActive = blnIsActive;
	}

}
