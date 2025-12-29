package com.zbs.de.model.dto;

import java.util.List;

public class DtoMenuComponentRequest {
	private Long parentMenuItemId;
	private String txtparentMenuItemName;
	private String txtparentMenuItemCode;
	private String txtparentMenuItemDesc;
	private List<String> txtcomponenetNameLst;
	private List<DtoMenuComponent> components;

	// Getters and Setters
	public Long getParentMenuItemId() {
		return parentMenuItemId;
	}

	public void setParentMenuItemId(Long parentMenuItemId) {
		this.parentMenuItemId = parentMenuItemId;
	}

	public List<DtoMenuComponent> getComponents() {
		return components;
	}

	public void setComponents(List<DtoMenuComponent> components) {
		this.components = components;
	}

	public String getTxtparentMenuItemName() {
		return txtparentMenuItemName;
	}

	public void setTxtparentMenuItemName(String txtparentMenuItemName) {
		this.txtparentMenuItemName = txtparentMenuItemName;
	}

	public String getTxtparentMenuItemCode() {
		return txtparentMenuItemCode;
	}

	public void setTxtparentMenuItemCode(String txtparentMenuItemCode) {
		this.txtparentMenuItemCode = txtparentMenuItemCode;
	}

	public List<String> getTxtcomponenetNameLst() {
		return txtcomponenetNameLst;
	}

	public void setTxtcomponenetNameLst(List<String> txtcomponenetNameLst) {
		this.txtcomponenetNameLst = txtcomponenetNameLst;
	}

	public String getTxtparentMenuItemDesc() {
		return txtparentMenuItemDesc;
	}

	public void setTxtparentMenuItemDesc(String txtparentMenuItemDesc) {
		this.txtparentMenuItemDesc = txtparentMenuItemDesc;
	}

}