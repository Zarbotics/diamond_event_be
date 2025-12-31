package com.zbs.de.model.dto.price;

import java.util.List;

public class DtoPriceMatrixMenuItem {
	private Long serMenuItemId;
	private String txtCode;
	private String txtName;
	private String txtRole;
	private String txtType;
	private String txtPath;
	private Long parentId;
	private List<DtoPriceMatrixMenuItem> children;
	private DtoPriceEntryInfo priceInfo;
	private Boolean hasPrice;
	private Boolean blnIsSelectable;
	private Boolean blnIsComposite;

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

	public String getTxtRole() {
		return txtRole;
	}

	public void setTxtRole(String txtRole) {
		this.txtRole = txtRole;
	}

	public String getTxtType() {
		return txtType;
	}

	public void setTxtType(String txtType) {
		this.txtType = txtType;
	}

	public String getTxtPath() {
		return txtPath;
	}

	public void setTxtPath(String txtPath) {
		this.txtPath = txtPath;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public List<DtoPriceMatrixMenuItem> getChildren() {
		return children;
	}

	public void setChildren(List<DtoPriceMatrixMenuItem> children) {
		this.children = children;
	}

	public DtoPriceEntryInfo getPriceInfo() {
		return priceInfo;
	}

	public void setPriceInfo(DtoPriceEntryInfo priceInfo) {
		this.priceInfo = priceInfo;
	}

	public Boolean getHasPrice() {
		return hasPrice;
	}

	public void setHasPrice(Boolean hasPrice) {
		this.hasPrice = hasPrice;
	}

	public Boolean getBlnIsSelectable() {
		return blnIsSelectable;
	}

	public void setBlnIsSelectable(Boolean blnIsSelectable) {
		this.blnIsSelectable = blnIsSelectable;
	}

	public Boolean getBlnIsComposite() {
		return blnIsComposite;
	}

	public void setBlnIsComposite(Boolean blnIsComposite) {
		this.blnIsComposite = blnIsComposite;
	}

}
