package com.zbs.de.model.dto;

import java.util.List;

public class DtoVenueMaster {
	private Integer serVenueMasterId;
	private String txtVenueCode;
	private String txtVenueName;
	private String txtAddress;
	private Integer serCityId;
	private String txtCityName;
	private String txtCityCode;
	private Boolean blnIsActive;
	private List<DtoVenueMasterDetail> venueMasterDetails;

	public Integer getSerVenueMasterId() {
		return serVenueMasterId;
	}

	public void setSerVenueMasterId(Integer serVenueMasterId) {
		this.serVenueMasterId = serVenueMasterId;
	}

	public String getTxtVenueCode() {
		return txtVenueCode;
	}

	public void setTxtVenueCode(String txtVenueCode) {
		this.txtVenueCode = txtVenueCode;
	}

	public String getTxtVenueName() {
		return txtVenueName;
	}

	public void setTxtVenueName(String txtVenueName) {
		this.txtVenueName = txtVenueName;
	}

	public String getTxtAddress() {
		return txtAddress;
	}

	public void setTxtAddress(String txtAddress) {
		this.txtAddress = txtAddress;
	}

	public Integer getSerCityId() {
		return serCityId;
	}

	public void setSerCityId(Integer serCityId) {
		this.serCityId = serCityId;
	}

	public String getTxtCityName() {
		return txtCityName;
	}

	public void setTxtCityName(String txtCityName) {
		this.txtCityName = txtCityName;
	}

	public String getTxtCityCode() {
		return txtCityCode;
	}

	public void setTxtCityCode(String txtCityCode) {
		this.txtCityCode = txtCityCode;
	}

	public Boolean getBlnIsActive() {
		return blnIsActive;
	}

	public void setBlnIsActive(Boolean blnIsActive) {
		this.blnIsActive = blnIsActive;
	}

	public List<DtoVenueMasterDetail> getVenueMasterDetails() {
		return venueMasterDetails;
	}

	public void setVenueMasterDetails(List<DtoVenueMasterDetail> venueMasterDetails) {
		this.venueMasterDetails = venueMasterDetails;
	}

}
