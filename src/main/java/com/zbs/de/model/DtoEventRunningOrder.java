package com.zbs.de.model;

import java.util.Date;

public class DtoEventRunningOrder {

	private Integer serEventRunningOrderId;

	private String txtEventRunningOrderCode;

	private Date dteGuestArrival;

	private Date dteBaratArrival;

	private Date dteNikah;

	private Date dteBrideEntrance;

	private Date dte_Meal;

	private Date dteEndOfNight;

	private Boolean blnIsActive;

	public Integer getSerEventRunningOrderId() {
		return serEventRunningOrderId;
	}

	public void setSerEventRunningOrderId(Integer serEventRunningOrderId) {
		this.serEventRunningOrderId = serEventRunningOrderId;
	}

	public String getTxtEventRunningOrderCode() {
		return txtEventRunningOrderCode;
	}

	public void setTxtEventRunningOrderCode(String txtEventRunningOrderCode) {
		this.txtEventRunningOrderCode = txtEventRunningOrderCode;
	}

	public Date getDteGuestArrival() {
		return dteGuestArrival;
	}

	public void setDteGuestArrival(Date dteGuestArrival) {
		this.dteGuestArrival = dteGuestArrival;
	}

	public Date getDteBaratArrival() {
		return dteBaratArrival;
	}

	public void setDteBaratArrival(Date dteBaratArrival) {
		this.dteBaratArrival = dteBaratArrival;
	}

	public Date getDteNikah() {
		return dteNikah;
	}

	public void setDteNikah(Date dteNikah) {
		this.dteNikah = dteNikah;
	}

	public Date getDteBrideEntrance() {
		return dteBrideEntrance;
	}

	public void setDteBrideEntrance(Date dteBrideEntrance) {
		this.dteBrideEntrance = dteBrideEntrance;
	}

	public Date getDte_Meal() {
		return dte_Meal;
	}

	public void setDte_Meal(Date dte_Meal) {
		this.dte_Meal = dte_Meal;
	}

	public Date getDteEndOfNight() {
		return dteEndOfNight;
	}

	public void setDteEndOfNight(Date dteEndOfNight) {
		this.dteEndOfNight = dteEndOfNight;
	}

	public Boolean getBlnIsActive() {
		return blnIsActive;
	}

	public void setBlnIsActive(Boolean blnIsActive) {
		this.blnIsActive = blnIsActive;
	}

}