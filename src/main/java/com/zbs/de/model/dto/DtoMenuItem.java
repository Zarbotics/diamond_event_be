package com.zbs.de.model.dto;

import java.util.Map;

public class DtoMenuItem {
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

	public DtoMenuItem() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DtoMenuItem(Long idserMenuItemId, String txtCode, String txtName, String txtShortName, String txtDescription,
			String txtRole, String txtType, Long parentId, Integer numDisplayOrder, Boolean blnIsSelectable,
			Map<String, Object> metadata, Double numDefaultServingsPerGuest, String txtPath) {
		super();
		this.serMenuItemId = idserMenuItemId;
		this.txtCode = txtCode;
		this.txtName = txtName;
		this.txtShortName = txtShortName;
		this.txtDescription = txtDescription;
		this.txtRole = txtRole;
		this.txtType = txtType;
		this.parentId = parentId;
		this.numDisplayOrder = numDisplayOrder;
		this.blnIsSelectable = blnIsSelectable;
		this.metadata = metadata;
		this.numDefaultServingsPerGuest = numDefaultServingsPerGuest;
		this.txtPath = txtPath;
	}

	public Long getSerMenuItemId() {
		return serMenuItemId;
	}

	public void setSerMenuItemId(Long idserMenuItemId) {
		this.serMenuItemId = idserMenuItemId;
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

	public String getTxtType() {
		return txtType;
	}

	public void setTxtType(String txtType) {
		this.txtType = txtType;
	}

	public Integer getSerMenuItemRoleId() {
		return serMenuItemRoleId;
	}

	public void setSerMenuItemRoleId(Integer serMenuItemRoleId) {
		this.serMenuItemRoleId = serMenuItemRoleId;
	}

	
}
