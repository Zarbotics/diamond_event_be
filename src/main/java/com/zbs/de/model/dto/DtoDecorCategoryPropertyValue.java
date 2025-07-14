package com.zbs.de.model.dto;

public class DtoDecorCategoryPropertyValue {
	private Integer serPropertyValueId;
	private String txtPropertyValue;
	private Integer serPropertyId;
	private String txtPropertyName;
	private Boolean blnIsActive;

	public Integer getSerPropertyValueId() {
		return serPropertyValueId;
	}

	public void setSerPropertyValueId(Integer serPropertyValueId) {
		this.serPropertyValueId = serPropertyValueId;
	}

	public String getTxtPropertyValue() {
		return txtPropertyValue;
	}

	public void setTxtPropertyValue(String txtPropertyValue) {
		this.txtPropertyValue = txtPropertyValue;
	}

	public Integer getSerPropertyId() {
		return serPropertyId;
	}

	public void setSerPropertyId(Integer serPropertyId) {
		this.serPropertyId = serPropertyId;
	}

	public String getTxtPropertyName() {
		return txtPropertyName;
	}

	public void setTxtPropertyName(String txtPropertyName) {
		this.txtPropertyName = txtPropertyName;
	}

	public Boolean getBlnIsActive() {
		return blnIsActive;
	}

	public void setBlnIsActive(Boolean blnIsActive) {
		this.blnIsActive = blnIsActive;
	}

}
