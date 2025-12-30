package com.zbs.de.model.dto;

import java.math.BigDecimal;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DtoMenuItemPrice {

	private Long serPriceId;
	private Long serMenuItemId;
	private Long serPriceVersionId;

	// Menu Item Info
	private String menuItemCode;
	private String menuItemName;
	private String menuItemRole;
	private String menuItemType;

	// Price Version Info
	private String priceVersionCode;
	private String priceVersionName;

	// Price Details
	private BigDecimal numBasePrice;
	private String txtCurrency = "GBP";
	private String txtPriceModel; // FIXED_PER_ITEM, PER_GUEST, PER_HOUR, FLAT
	private String txtUnitType; // PER_ITEM, PER_GUEST, PER_HOUR, FLAT

	private Integer numMinQuantity = 1;
	private Integer numMaxQuantity;

	private Integer numMinGuests;
	private Integer numMaxGuests;

	// Time Settings
	private Integer numMinHours;
	private Integer numMaxHours;

	// Validity
	private String dteValidFrom;

	private String dteValidTo;

	// Status
	private Boolean blnIsActive = true;
	private Boolean blnIsDefault = false;

	// Configuration
	private Map<String, Object> priceConfig;

	// Calculated Fields (for display)
	private BigDecimal calculatedPrice;
	private String priceDisplay;

	public Long getSerPriceId() {
		return serPriceId;
	}

	public void setSerPriceId(Long serPriceId) {
		this.serPriceId = serPriceId;
	}

	public Long getSerMenuItemId() {
		return serMenuItemId;
	}

	public void setSerMenuItemId(Long serMenuItemId) {
		this.serMenuItemId = serMenuItemId;
	}

	public Long getSerPriceVersionId() {
		return serPriceVersionId;
	}

	public void setSerPriceVersionId(Long serPriceVersionId) {
		this.serPriceVersionId = serPriceVersionId;
	}

	public String getMenuItemCode() {
		return menuItemCode;
	}

	public void setMenuItemCode(String menuItemCode) {
		this.menuItemCode = menuItemCode;
	}

	public String getMenuItemName() {
		return menuItemName;
	}

	public void setMenuItemName(String menuItemName) {
		this.menuItemName = menuItemName;
	}

	public String getMenuItemRole() {
		return menuItemRole;
	}

	public void setMenuItemRole(String menuItemRole) {
		this.menuItemRole = menuItemRole;
	}

	public String getMenuItemType() {
		return menuItemType;
	}

	public void setMenuItemType(String menuItemType) {
		this.menuItemType = menuItemType;
	}

	public String getPriceVersionCode() {
		return priceVersionCode;
	}

	public void setPriceVersionCode(String priceVersionCode) {
		this.priceVersionCode = priceVersionCode;
	}

	public String getPriceVersionName() {
		return priceVersionName;
	}

	public void setPriceVersionName(String priceVersionName) {
		this.priceVersionName = priceVersionName;
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

	public Integer getNumMinHours() {
		return numMinHours;
	}

	public void setNumMinHours(Integer numMinHours) {
		this.numMinHours = numMinHours;
	}

	public Integer getNumMaxHours() {
		return numMaxHours;
	}

	public void setNumMaxHours(Integer numMaxHours) {
		this.numMaxHours = numMaxHours;
	}

	public String getDteValidFrom() {
		return dteValidFrom;
	}

	public void setDteValidFrom(String dteValidFrom) {
		this.dteValidFrom = dteValidFrom;
	}

	public String getDteValidTo() {
		return dteValidTo;
	}

	public void setDteValidTo(String dteValidTo) {
		this.dteValidTo = dteValidTo;
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

	public Map<String, Object> getPriceConfig() {
		return priceConfig;
	}

	public void setPriceConfig(Map<String, Object> priceConfig) {
		this.priceConfig = priceConfig;
	}

	public BigDecimal getCalculatedPrice() {
		return calculatedPrice;
	}

	public void setCalculatedPrice(BigDecimal calculatedPrice) {
		this.calculatedPrice = calculatedPrice;
	}

	public String getPriceDisplay() {
		return priceDisplay;
	}

	public void setPriceDisplay(String priceDisplay) {
		this.priceDisplay = priceDisplay;
	}

}