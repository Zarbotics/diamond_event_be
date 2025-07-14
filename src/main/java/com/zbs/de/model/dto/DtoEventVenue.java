package com.zbs.de.model.dto;

import java.util.List;

public class DtoEventVenue {
	private Integer serVenueMasterId;
	private String txtVenueCode;
	private String txtVenueName;

	private Integer serVenueMasterDetailId;
	private String txtHallCode;
	private String txtHallName;
	private List<String> txtDocumentsUrl;

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

	public Integer getSerVenueMasterDetailId() {
		return serVenueMasterDetailId;
	}

	public void setSerVenueMasterDetailId(Integer serVenueMasterDetailId) {
		this.serVenueMasterDetailId = serVenueMasterDetailId;
	}

	public String getTxtHallCode() {
		return txtHallCode;
	}

	public void setTxtHallCode(String txtHallCode) {
		this.txtHallCode = txtHallCode;
	}

	public String getTxtHallName() {
		return txtHallName;
	}

	public void setTxtHallName(String txtHallName) {
		this.txtHallName = txtHallName;
	}

	public List<String> getTxtDocumentsUrl() {
		return txtDocumentsUrl;
	}

	public void setTxtDocumentsUrl(List<String> txtDocumentsUrl) {
		this.txtDocumentsUrl = txtDocumentsUrl;
	}

}
