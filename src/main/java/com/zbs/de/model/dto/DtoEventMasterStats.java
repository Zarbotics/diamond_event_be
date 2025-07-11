package com.zbs.de.model.dto;

public class DtoEventMasterStats {
	private String txtEventTypeName;
	private long numTotalEvents;

	public DtoEventMasterStats() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DtoEventMasterStats(String txtEventTypeName, long numTotalEvents) {
		super();
		this.txtEventTypeName = txtEventTypeName;
		this.numTotalEvents = numTotalEvents;
	}

	public String getTxtEventTypeName() {
		return txtEventTypeName;
	}

	public void setTxtEventTypeName(String txtEventTypeName) {
		this.txtEventTypeName = txtEventTypeName;
	}

	public long getNumTotalEvents() {
		return numTotalEvents;
	}

	public void setNumTotalEvents(long numTotalEvents) {
		this.numTotalEvents = numTotalEvents;
	}

}
