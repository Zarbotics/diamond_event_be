package com.zbs.de.model.dto;

import java.util.List;

public class DtoEventType {
	private Integer serEventTypeId;
	private String txtEventTypeCode;
	private String txtEventTypeName;
	private Boolean blnIsMainEvent;
	private Integer parentEventTypeId;
	private Boolean blnIsActive;

	private List<DtoEventTypeDocument> documents;
	private List<String> txtDocuments;

	private List<DtoEventType> subEvents;

	/**
	 * The moments a running order has for this kind of event, in the order the
	 * day runs — e.g. ["txtGuestArrival", "txtNikah", ..., "txtEndOfNight"].
	 *
	 * <p>
	 * The control panel used to hold this as a map keyed by event type id, which
	 * is a surrogate key the business assigns. It drifted. It is answered here,
	 * by the row the business edits.
	 */
	private List<String> txtRunningOrderMoments;

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

	public List<DtoEventTypeDocument> getDocuments() {
		return documents;
	}

	public void setDocuments(List<DtoEventTypeDocument> documents) {
		this.documents = documents;
	}

	public List<String> getTxtDocuments() {
		return txtDocuments;
	}

	public void setTxtDocuments(List<String> txtDocuments) {
		this.txtDocuments = txtDocuments;
	}

	public List<String> getTxtRunningOrderMoments() {
		return txtRunningOrderMoments;
	}

	public void setTxtRunningOrderMoments(List<String> txtRunningOrderMoments) {
		this.txtRunningOrderMoments = txtRunningOrderMoments;
	}
}
