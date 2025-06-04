package com.zbs.de.model.dto;

public class DtoEventType {
	private Integer serEventTypeId;
	private String txtEventTypeCode;
	private String txtEventTypeName;
	private String blnIsMainEvent;
	private Integer parentEventTypeId;
	private Boolean blnIsActive;

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

	public String getBlnIsMainEvent() {
		return blnIsMainEvent;
	}

	public void setBlnIsMainEvent(String blnIsMainEvent) {
		this.blnIsMainEvent = blnIsMainEvent;
	}

	public Integer getParentEventTypeId() {
		return parentEventTypeId;
	}

	public void setParentEventTypeId(Integer parentEventTypeId) {
		this.parentEventTypeId = parentEventTypeId;
	}

	public Boolean getBlnIsActive() {
		return blnIsActive;
	}

	public void setBlnIsActive(Boolean blnIsActive) {
		this.blnIsActive = blnIsActive;
	}

}