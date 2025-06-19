package com.zbs.de.model.dto;

import java.util.List;

public class DtoEventType {
	private Integer serEventTypeId;
	private String txtEventTypeCode;
	private String txtEventTypeName;
	private Boolean blnIsMainEvent;
	private Integer parentEventTypeId;
	private Boolean blnIsActive;

	private List<DtoEventType> subEvents;

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

	public List<DtoEventType> getSubEvents() {
		return subEvents;
	}

	public void setSubEvents(List<DtoEventType> subEvents) {
		this.subEvents = subEvents;
	}

	public Boolean getBlnIsMainEvent() {
		return blnIsMainEvent;
	}

	public void setBlnIsMainEvent(Boolean blnIsMainEvent) {
		this.blnIsMainEvent = blnIsMainEvent;
	}

}