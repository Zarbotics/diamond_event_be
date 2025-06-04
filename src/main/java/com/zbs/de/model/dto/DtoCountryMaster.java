package com.zbs.de.model.dto;

import lombok.Data;

@Data
public class DtoCountryMaster {
	private Integer serCountryId;
	private String txtCountryCode;
	private String txtCountryName;
	private Boolean isActive;
	private String shortName;
	private Integer defaultCountry;
	
	private Boolean blnIsAvtive;

	public Integer getSerCountryId() {
		return serCountryId;
	}

	public void setSerCountryId(Integer serCountryId) {
		this.serCountryId = serCountryId;
	}

	public String getTxtCountryCode() {
		return txtCountryCode;
	}

	public void setTxtCountryCode(String txtCountryCode) {
		this.txtCountryCode = txtCountryCode;
	}

	public String getTxtCountryName() {
		return txtCountryName;
	}

	public void setTxtCountryName(String txtCountryName) {
		this.txtCountryName = txtCountryName;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public Integer getDefaultCountry() {
		return defaultCountry;
	}

	public void setDefaultCountry(Integer defaultCountry) {
		this.defaultCountry = defaultCountry;
	}

	public Boolean getBlnIsAvtive() {
		return blnIsAvtive;
	}

	public void setBlnIsAvtive(Boolean blnIsAvtive) {
		this.blnIsAvtive = blnIsAvtive;
	}
	

}