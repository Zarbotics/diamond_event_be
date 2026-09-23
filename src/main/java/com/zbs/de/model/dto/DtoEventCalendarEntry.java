package com.zbs.de.model.dto;

import java.util.Date;

import com.zbs.de.util.UtilDateAndTime;

/**
 * One event, as a calendar needs it.
 *
 * <p>
 * The admin calendar draws a box per event with a reference and a name on it.
 * That is a handful of fields, and it was being served the whole event — sixty
 * fields with the food, decor, extras and running-order collections nested
 * inside — for every event the business has ever taken. 624 KB measured on a
 * development database, to draw a month.
 *
 * <p>
 * A calendar cannot be paginated: a month view either has every event in the
 * month or it is wrong. So the answer here is not fewer rows but narrower ones.
 *
 * <h2>Why the status and the venue are here</h2>
 *
 * The calendar answers one question — which days are taken, and by what. An
 * enquiry and a confirmed booking are not the same answer: a Saturday held by
 * three enquiries is available in a way a Saturday with a confirmed wedding on
 * it is not, and the calendar could not tell them apart.
 *
 * <p>
 * The venue is here because the portal already claimed to show it. The event
 * dialog has a "Venue:" line that has never had anything to put on it, so it
 * has always read "Venue: N/A" — for every event, on every day.
 */
public class DtoEventCalendarEntry {

	private Integer serEventMasterId;
	private String txtEventMasterCode;
	private String txtEventMasterName;

	/** Formatted {@code dd-MM-yyyy}, as every other event DTO reports dates. */
	private String dteEventDate;

	private String txtEventTypeName;

	/** Where it is. Null until a hall has been chosen, which is most enquiries. */
	private String txtVenueName;

	/**
	 * Enquiry, Quoted or Confirmed, from the budget.
	 *
	 * <p>
	 * Not from {@code event_master.txt_event_status}, which looks like the
	 * obvious source and is written by nothing — it is null on every row.
	 */
	private String txtStatus;

	public DtoEventCalendarEntry() {
		super();
	}

	public DtoEventCalendarEntry(Integer serEventMasterId, String txtEventMasterCode, String txtEventMasterName,
			Date dteEventDate, String txtEventTypeName, String txtVenueName, String txtStatus) {
		super();
		this.serEventMasterId = serEventMasterId;
		this.txtEventMasterCode = txtEventMasterCode;
		this.txtEventMasterName = txtEventMasterName;
		this.dteEventDate = UtilDateAndTime.mmddyyyyDateToString(dteEventDate);
		this.txtEventTypeName = txtEventTypeName;
		this.txtVenueName = txtVenueName;
		this.txtStatus = txtStatus;
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

	public String getDteEventDate() {
		return dteEventDate;
	}

	public void setDteEventDate(String dteEventDate) {
		this.dteEventDate = dteEventDate;
	}

	public String getTxtEventTypeName() {
		return txtEventTypeName;
	}

	public void setTxtEventTypeName(String txtEventTypeName) {
		this.txtEventTypeName = txtEventTypeName;
	}

	public String getTxtVenueName() {
		return txtVenueName;
	}

	public void setTxtVenueName(String txtVenueName) {
		this.txtVenueName = txtVenueName;
	}

	public String getTxtStatus() {
		return txtStatus;
	}

	public void setTxtStatus(String txtStatus) {
		this.txtStatus = txtStatus;
	}
}
