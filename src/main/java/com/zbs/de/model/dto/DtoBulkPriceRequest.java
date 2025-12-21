package com.zbs.de.model.dto;

import java.math.BigDecimal;
import java.util.List;

public class DtoBulkPriceRequest {

	private List<Long> menuItemIds;
	private Long serPriceVersionId;
	private BigDecimal numBasePrice;
	private String txtCurrency = "GBP";
	private String txtPriceModel;
	private String txtUnitType;
	private Integer numMinQuantity = 1;
	private Integer numMaxQuantity;
	private Integer numMinGuests;
	private Integer numMaxGuests;
	private Boolean blnIsActive = true;
	private Boolean blnIsDefault = false;
	private String conflictResolution = "SKIP"; // SKIP, OVERWRITE, UPDATE

	// Getters and Setters
	public List<Long> getMenuItemIds() {
		return menuItemIds;
	}

	public void setMenuItemIds(List<Long> menuItemIds) {
		this.menuItemIds = menuItemIds;
	}

	public Long getSerPriceVersionId() {
		return serPriceVersionId;
	}

	public void setSerPriceVersionId(Long serPriceVersionId) {
		this.serPriceVersionId = serPriceVersionId;
	}

	public BigDecimal getNumBasePrice() {
		return numBasePrice;
	}

	public void setNumBasePrice(BigDecimal numBasePrice) {
		this.numBasePrice = numBasePrice;
	}

	public String getTxtCurrency() {
		return txtCurrency;
	}

	public void setTxtCurrency(String txtCurrency) {
		this.txtCurrency = txtCurrency;
	}

	public String getTxtPriceModel() {
		return txtPriceModel;
	}

	public void setTxtPriceModel(String txtPriceModel) {
		this.txtPriceModel = txtPriceModel;
	}

	public String getTxtUnitType() {
		return txtUnitType;
	}

	public void setTxtUnitType(String txtUnitType) {
		this.txtUnitType = txtUnitType;
	}

	public Integer getNumMinQuantity() {
		return numMinQuantity;
	}

	public void setNumMinQuantity(Integer numMinQuantity) {
		this.numMinQuantity = numMinQuantity;
	}

	public Integer getNumMaxQuantity() {
		return numMaxQuantity;
	}

	public void setNumMaxQuantity(Integer numMaxQuantity) {
		this.numMaxQuantity = numMaxQuantity;
	}

	public Integer getNumMinGuests() {
		return numMinGuests;
	}

	public void setNumMinGuests(Integer numMinGuests) {
		this.numMinGuests = numMinGuests;
	}

	public Integer getNumMaxGuests() {
		return numMaxGuests;
	}

	public void setNumMaxGuests(Integer numMaxGuests) {
		this.numMaxGuests = numMaxGuests;
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

	public String getConflictResolution() {
		return conflictResolution;
	}

	public void setConflictResolution(String conflictResolution) {
		this.conflictResolution = conflictResolution;
	}
}