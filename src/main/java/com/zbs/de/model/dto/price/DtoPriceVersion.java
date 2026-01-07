package com.zbs.de.model.dto.price;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DtoPriceVersion {

	private Long serPriceVersionId;
	private String txtVersionCode;
	private String txtName;
	private String txtDescription;

	private String dteEffectiveFrom;
	private String dteEffectiveTo;

	private Boolean blnIsActive = true;
	private Boolean blnIsDefault = false;
	private Integer numPriority = 1;
	private Map<String, Object> metadata;

	private String txtPriceVersionStatus;

	private Integer totalPrices;
	private Integer activePrices;

	public Long getSerPriceVersionId() {
		return serPriceVersionId;
	}

	public void setSerPriceVersionId(Long serPriceVersionId) {
		this.serPriceVersionId = serPriceVersionId;
	}

	public String getTxtVersionCode() {
		return txtVersionCode;
	}

	public void setTxtVersionCode(String txtVersionCode) {
		this.txtVersionCode = txtVersionCode;
	}

	public String getTxtName() {
		return txtName;
	}

	public void setTxtName(String txtName) {
		this.txtName = txtName;
	}

	public String getTxtDescription() {
		return txtDescription;
	}

	public void setTxtDescription(String txtDescription) {
		this.txtDescription = txtDescription;
	}

	public String getDteEffectiveFrom() {
		return dteEffectiveFrom;
	}

	public void setDteEffectiveFrom(String dteEffectiveFrom) {
		this.dteEffectiveFrom = dteEffectiveFrom;
	}

	public String getDteEffectiveTo() {
		return dteEffectiveTo;
	}

	public void setDteEffectiveTo(String dteEffectiveTo) {
		this.dteEffectiveTo = dteEffectiveTo;
	}

	public Boolean getBlnIsActive() {
		return blnIsActive;
	}

	public void setBlnIsActive(Boolean blnIsActive) {
		this.blnIsActive = blnIsActive;
	}

	public Boolean getBlnIsDefault() {
		return blnIsDefault;
	}

	public void setBlnIsDefault(Boolean blnIsDefault) {
		this.blnIsDefault = blnIsDefault;
	}

	public Integer getNumPriority() {
		return numPriority;
	}

	public void setNumPriority(Integer numPriority) {
		this.numPriority = numPriority;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	public Integer getTotalPrices() {
		return totalPrices;
	}

	public void setTotalPrices(Integer totalPrices) {
		this.totalPrices = totalPrices;
	}

	public Integer getActivePrices() {
		return activePrices;
	}

	public void setActivePrices(Integer activePrices) {
		this.activePrices = activePrices;
	}

	public String getTxtPriceVersionStatus() {
		return txtPriceVersionStatus;
	}

	public void setTxtPriceVersionStatus(String txtPriceVersionStatus) {
		this.txtPriceVersionStatus = txtPriceVersionStatus;
	}

}