package com.zbs.de.model.dto;

public class DtoCateringDeliveryBookingSearch {

	private Integer serDeliveryBookingId;
	private Integer serCustId;
	private Integer serEventTypeId;

	private String dteDeliveryDate;
	private String dteDeliveryDateFrom;
	private String dteDeliveryDateTo;

	private String txtDeliveryBookingCode;
	private String txtDeliveryLocation;
	private String txtBookingStatus;
	private String txtBudgetStatus;

	private Boolean blnBookingStatus;

	private String q;

	private Integer page;
	private Integer size;
	private String sortBy;
	private String sortDir;

	public Integer getSerDeliveryBookingId() {
		return serDeliveryBookingId;
	}

	public void setSerDeliveryBookingId(Integer serDeliveryBookingId) {
		this.serDeliveryBookingId = serDeliveryBookingId;
	}

	public Integer getSerCustId() {
		return serCustId;
	}

	public void setSerCustId(Integer serCustId) {
		this.serCustId = serCustId;
	}

	public Integer getSerEventTypeId() {
		return serEventTypeId;
	}

	public void setSerEventTypeId(Integer serEventTypeId) {
		this.serEventTypeId = serEventTypeId;
	}

	public String getDteDeliveryDate() {
		return dteDeliveryDate;
	}

	public void setDteDeliveryDate(String dteDeliveryDate) {
		this.dteDeliveryDate = dteDeliveryDate;
	}

	public String getDteDeliveryDateFrom() {
		return dteDeliveryDateFrom;
	}

	public void setDteDeliveryDateFrom(String dteDeliveryDateFrom) {
		this.dteDeliveryDateFrom = dteDeliveryDateFrom;
	}

	public String getDteDeliveryDateTo() {
		return dteDeliveryDateTo;
	}

	public void setDteDeliveryDateTo(String dteDeliveryDateTo) {
		this.dteDeliveryDateTo = dteDeliveryDateTo;
	}

	public String getTxtDeliveryBookingCode() {
		return txtDeliveryBookingCode;
	}

	public void setTxtDeliveryBookingCode(String txtDeliveryBookingCode) {
		this.txtDeliveryBookingCode = txtDeliveryBookingCode;
	}

	public String getTxtDeliveryLocation() {
		return txtDeliveryLocation;
	}

	public void setTxtDeliveryLocation(String txtDeliveryLocation) {
		this.txtDeliveryLocation = txtDeliveryLocation;
	}

	public String getTxtBookingStatus() {
		return txtBookingStatus;
	}

	public void setTxtBookingStatus(String txtBookingStatus) {
		this.txtBookingStatus = txtBookingStatus;
	}

	public String getTxtBudgetStatus() {
		return txtBudgetStatus;
	}

	public void setTxtBudgetStatus(String txtBudgetStatus) {
		this.txtBudgetStatus = txtBudgetStatus;
	}

	public Boolean getBlnBookingStatus() {
		return blnBookingStatus;
	}

	public void setBlnBookingStatus(Boolean blnBookingStatus) {
		this.blnBookingStatus = blnBookingStatus;
	}

	public String getQ() {
		return q;
	}

	public void setQ(String q) {
		this.q = q;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public String getSortBy() {
		return sortBy;
	}

	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}

	public String getSortDir() {
		return sortDir;
	}

	public void setSortDir(String sortDir) {
		this.sortDir = sortDir;
	}

}
