package com.zbs.de.model.dto.menu;

import java.math.BigDecimal;
import java.util.Map;

public class DtoMenuItemSearch {
	private Long serMenuItemId;
	private String txtCode;
	private String txtName;
	private String txtShortName;
	private String txtDescription;

	private String txtRole;
	private Integer serMenuItemRoleId;

	private String txtType;
	private Long parentId;
	private Integer numDisplayOrder;
	private Boolean blnIsSelectable;
	private Map<String, Object> metadata;
	private Double numDefaultServingsPerGuest;
	private String txtPath;
	private Boolean blnIsCateringItem;

	private Boolean blnIsActive;
	private Boolean blnIsCompostie;
	private BigDecimal numPrice; // it's unit price
	private BigDecimal numCalculatedPrice;
	private BigDecimal numFinalPrice;
	private String txtPriceMultiplierType;

	private String q;

	private Integer page;
	private Integer size;
	private String sortBy;
	private String sortDir;

	public Long getSerMenuItemId() {
		return serMenuItemId;
	}

	public void setSerMenuItemId(Long serMenuItemId) {
		this.serMenuItemId = serMenuItemId;
	}

	public String getTxtCode() {
		return txtCode;
	}

	public void setTxtCode(String txtCode) {
		this.txtCode = txtCode;
	}

	public String getTxtName() {
		return txtName;
	}

	public void setTxtName(String txtName) {
		this.txtName = txtName;
	}

	public String getTxtShortName() {
		return txtShortName;
	}

	public void setTxtShortName(String txtShortName) {
		this.txtShortName = txtShortName;
	}

	public String getTxtDescription() {
		return txtDescription;
	}

	public void setTxtDescription(String txtDescription) {
		this.txtDescription = txtDescription;
	}

	public String getTxtRole() {
		return txtRole;
	}

	public void setTxtRole(String txtRole) {
		this.txtRole = txtRole;
	}

	public Integer getSerMenuItemRoleId() {
		return serMenuItemRoleId;
	}

	public void setSerMenuItemRoleId(Integer serMenuItemRoleId) {
		this.serMenuItemRoleId = serMenuItemRoleId;
	}

	public String getTxtType() {
		return txtType;
	}

	public void setTxtType(String txtType) {
		this.txtType = txtType;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public Integer getNumDisplayOrder() {
		return numDisplayOrder;
	}

	public void setNumDisplayOrder(Integer numDisplayOrder) {
		this.numDisplayOrder = numDisplayOrder;
	}

	public Boolean getBlnIsSelectable() {
		return blnIsSelectable;
	}

	public void setBlnIsSelectable(Boolean blnIsSelectable) {
		this.blnIsSelectable = blnIsSelectable;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	public Double getNumDefaultServingsPerGuest() {
		return numDefaultServingsPerGuest;
	}

	public void setNumDefaultServingsPerGuest(Double numDefaultServingsPerGuest) {
		this.numDefaultServingsPerGuest = numDefaultServingsPerGuest;
	}

	public String getTxtPath() {
		return txtPath;
	}

	public void setTxtPath(String txtPath) {
		this.txtPath = txtPath;
	}

	public Boolean getBlnIsCateringItem() {
		return blnIsCateringItem;
	}

	public void setBlnIsCateringItem(Boolean blnIsCateringItem) {
		this.blnIsCateringItem = blnIsCateringItem;
	}

	public Boolean getBlnIsActive() {
		return blnIsActive;
	}

	public void setBlnIsActive(Boolean blnIsActive) {
		this.blnIsActive = blnIsActive;
	}

	public Boolean getBlnIsCompostie() {
		return blnIsCompostie;
	}

	public void setBlnIsCompostie(Boolean blnIsCompostie) {
		this.blnIsCompostie = blnIsCompostie;
	}

	public BigDecimal getNumPrice() {
		return numPrice;
	}

	public void setNumPrice(BigDecimal numPrice) {
		this.numPrice = numPrice;
	}

	public BigDecimal getNumCalculatedPrice() {
		return numCalculatedPrice;
	}

	public void setNumCalculatedPrice(BigDecimal numCalculatedPrice) {
		this.numCalculatedPrice = numCalculatedPrice;
	}

	public BigDecimal getNumFinalPrice() {
		return numFinalPrice;
	}

	public void setNumFinalPrice(BigDecimal numFinalPrice) {
		this.numFinalPrice = numFinalPrice;
	}

	public String getTxtPriceMultiplierType() {
		return txtPriceMultiplierType;
	}

	public void setTxtPriceMultiplierType(String txtPriceMultiplierType) {
		this.txtPriceMultiplierType = txtPriceMultiplierType;
	}

	public String getQ() {
		return q;
	}

	public void setQ(String q) {
		this.q = q;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public String getSortBy() {
		return sortBy;
	}

	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}

	public String getSortDir() {
		return sortDir;
	}

	public void setSortDir(String sortDir) {
		this.sortDir = sortDir;
	}

}
