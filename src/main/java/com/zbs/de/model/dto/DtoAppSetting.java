package com.zbs.de.model.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

/** One configurable setting, as the office's screen draws it. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DtoAppSetting {

	private Integer serSettingId;
	private String txtKey;
	private String txtValue;
	private String txtValueType;
	private String txtLabel;
	private String txtDescription;
	private String txtGroup;
	private Integer numDisplayOrder;
	private BigDecimal numMin;
	private BigDecimal numMax;

	public Integer getSerSettingId() {
		return serSettingId;
	}

	public void setSerSettingId(Integer serSettingId) {
		this.serSettingId = serSettingId;
	}

	public String getTxtKey() {
		return txtKey;
	}

	public void setTxtKey(String txtKey) {
		this.txtKey = txtKey;
	}

	public String getTxtValue() {
		return txtValue;
	}

	public void setTxtValue(String txtValue) {
		this.txtValue = txtValue;
	}

	public String getTxtValueType() {
		return txtValueType;
	}

	public void setTxtValueType(String txtValueType) {
		this.txtValueType = txtValueType;
	}

	public String getTxtLabel() {
		return txtLabel;
	}

	public void setTxtLabel(String txtLabel) {
		this.txtLabel = txtLabel;
	}

	public String getTxtDescription() {
		return txtDescription;
	}

	public void setTxtDescription(String txtDescription) {
		this.txtDescription = txtDescription;
	}

	public String getTxtGroup() {
		return txtGroup;
	}

	public void setTxtGroup(String txtGroup) {
		this.txtGroup = txtGroup;
	}

	public Integer getNumDisplayOrder() {
		return numDisplayOrder;
	}

	public void setNumDisplayOrder(Integer numDisplayOrder) {
		this.numDisplayOrder = numDisplayOrder;
	}

	public BigDecimal getNumMin() {
		return numMin;
	}

	public void setNumMin(BigDecimal numMin) {
		this.numMin = numMin;
	}

	public BigDecimal getNumMax() {
		return numMax;
	}

	public void setNumMax(BigDecimal numMax) {
		this.numMax = numMax;
	}
}
