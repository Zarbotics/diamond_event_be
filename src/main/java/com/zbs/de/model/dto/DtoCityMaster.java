package com.zbs.de.model.dto;

import lombok.Data;

@Data
public class DtoCityMaster {

	private Integer serCityId;
	private String txtCityCode;
	private String txtCityName;
	private Integer serStateId;
	private Boolean blnIsActive;

	public Integer getSerCityId() {
		return serCityId;
	}

	public void setSerCityId(Integer serCityId) {
		this.serCityId = serCityId;
	}

	public String getTxtCityCode() {
		return txtCityCode;
	}

	public void setTxtCityCode(String txtCityCode) {
		this.txtCityCode = txtCityCode;
	}

	public String getTxtCityName() {
		return txtCityName;
	}

	public void setTxtCityName(String txtCityName) {
		this.txtCityName = txtCityName;
	}

	public Integer getSerStateId() {
		return serStateId;
	}

	public void setSerStateId(Integer serStateId) {
		this.serStateId = serStateId;
	}

	public Boolean getBlnIsActive() {
		return blnIsActive;
	}

	public void setBlnIsActive(Boolean blnIsActive) {
		this.blnIsActive = blnIsActive;
	}

}