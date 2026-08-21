package com.zbs.de.model.dto;

import java.util.Date;

import com.zbs.de.util.UtilDateAndTime;

/**
 * One of a customer's events, as the "choose an event" screen needs it.
 *
 * <p>
 * That screen is the second step of the journey, and it was being sent every
 * event the customer had ever had, in full — measured at 1.1 MB across 278
 * events on a development database, each carrying its food, decor, extras and
 * running-order collections. On a phone, on mobile data, to draw a list of
 * names and dates.
 *
 * <p>
 * Seven fields. Five of them are printed on the row; the other two decide what
 * happens when it is chosen — whether the booking can still be edited, and which
 * step of the journey to resume at. The full event is fetched only for the one
 * the customer actually picks.
 */
public class DtoEventSummary {

	private Integer serEventMasterId;
	private String txtEventMasterCode;
	private String txtEventMasterName;

	/** Formatted {@code dd-MM-yyyy}, as every other event DTO reports dates. */
	private String dteEventDate;

	private String txtEventTypeName;
	private String txtNumberOfGuests;
	private Integer numNumberOfGuests;

	/**
	 * Whether the customer may still change this booking.
	 *
	 * <p>
	 * Read on the row rather than after opening it, so a booking that is closed
	 * says so before somebody starts editing it.
	 */
	private Boolean isEditAllowed;

	/**
	 * Which step of the journey this event stopped at.
	 *
	 * <p>
	 * The screen resumes from here. It has to travel with the list rather than be
	 * looked up afterwards, because the customer sees the step they are returning
	 * to before they commit to returning to it.
	 */
	private Integer numFormState;

	public DtoEventSummary() {
		super();
	}

	public DtoEventSummary(Integer serEventMasterId, String txtEventMasterCode, String txtEventMasterName,
			Date dteEventDate, String txtEventTypeName, String txtNumberOfGuests, Integer numNumberOfGuests,
			Boolean isEditAllowed, Integer numFormState) {
		super();
		this.serEventMasterId = serEventMasterId;
		this.txtEventMasterCode = txtEventMasterCode;
		this.txtEventMasterName = txtEventMasterName;
		this.dteEventDate = UtilDateAndTime.mmddyyyyDateToString(dteEventDate);
		this.txtEventTypeName = txtEventTypeName;
		this.txtNumberOfGuests = txtNumberOfGuests;
		this.numNumberOfGuests = numNumberOfGuests;
		this.isEditAllowed = isEditAllowed;
		this.numFormState = numFormState;
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

	public String getTxtNumberOfGuests() {
		return txtNumberOfGuests;
	}

	public void setTxtNumberOfGuests(String txtNumberOfGuests) {
		this.txtNumberOfGuests = txtNumberOfGuests;
	}

	public Integer getNumNumberOfGuests() {
		return numNumberOfGuests;
	}

	public void setNumNumberOfGuests(Integer numNumberOfGuests) {
		this.numNumberOfGuests = numNumberOfGuests;
	}

	public Boolean getIsEditAllowed() {
		return isEditAllowed;
	}

	public void setIsEditAllowed(Boolean isEditAllowed) {
		this.isEditAllowed = isEditAllowed;
	}

	public Integer getNumFormState() {
		return numFormState;
	}

	public void setNumFormState(Integer numFormState) {
		this.numFormState = numFormState;
	}
}
