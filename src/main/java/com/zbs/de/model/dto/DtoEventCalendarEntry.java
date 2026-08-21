package com.zbs.de.model.dto;

import java.util.Date;

import com.zbs.de.util.UtilDateAndTime;

/**
 * One event, as a calendar needs it.
 *
 * <p>
 * The admin calendar draws a box per event with a reference and a name on it.
 * That is four fields, and it was being served the whole event — sixty fields
 * with the food, decor, extras and running-order collections nested inside —
 * for every event the business has ever taken. 624 KB measured on a development
 * database, to draw a month.
 *
 * <p>
 * A calendar cannot be paginated: a month view either has every event in the
 * month or it is wrong. So the answer here is not fewer rows but narrower ones.
 */
public class DtoEventCalendarEntry {

	private Integer serEventMasterId;
	private String txtEventMasterCode;
	private String txtEventMasterName;

	/** Formatted {@code dd-MM-yyyy}, as every other event DTO reports dates. */
	private String dteEventDate;

	private String txtEventTypeName;

	public DtoEventCalendarEntry() {
		super();
	}

	public DtoEventCalendarEntry(Integer serEventMasterId, String txtEventMasterCode, String txtEventMasterName,
			Date dteEventDate, String txtEventTypeName) {
		super();
		this.serEventMasterId = serEventMasterId;
		this.txtEventMasterCode = txtEventMasterCode;
		this.txtEventMasterName = txtEventMasterName;
		this.dteEventDate = UtilDateAndTime.mmddyyyyDateToString(dteEventDate);
		this.txtEventTypeName = txtEventTypeName;
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
}
