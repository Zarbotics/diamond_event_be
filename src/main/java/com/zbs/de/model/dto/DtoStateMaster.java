package com.zbs.de.model.dto;

import lombok.Data;

@Data
public class DtoStateMaster {
	private Integer serStateId;
	private String txtStateName;
	private String txtStateCode;
	private Integer serCountryId;
	private Boolean blnIsAvtive;

	public Integer getSerStateId() {
		return serStateId;
	}

	public void setSerStateId(Integer serStateId) {
		this.serStateId = serStateId;
	}

	public String getTxtStateName() {
		return txtStateName;
	}

	public void setTxtStateName(String txtStateName) {
		this.txtStateName = txtStateName;
	}

	public String getTxtStateCode() {
		return txtStateCode;
	}

	public void setTxtStateCode(String txtStateCode) {
		this.txtStateCode = txtStateCode;
	}

	public Integer getSerCountryId() {
		return serCountryId;
	}

	public void setSerCountryId(Integer serCountryId) {
		this.serCountryId = serCountryId;
	}

	public Boolean getBlnIsAvtive() {
		return blnIsAvtive;
	}

	public void setBlnIsAvtive(Boolean blnIsAvtive) {
		this.blnIsAvtive = blnIsAvtive;
	}

}