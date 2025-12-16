package com.zbs.de.model.dto;

public class DtoMenuItemRole {
	private Integer serMenuItemRoleId;
	private String txtRoleCode;
	private String txtRoleName;
	private Boolean blnIsCompositionRole;
	private Boolean blnIsActive;
	private Integer parentMenuRoleId;

	public DtoMenuItemRole() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DtoMenuItemRole(Integer serMenuItemRoleId, String txtRoleCode, String txtRoleName,
			Boolean blnIsCompositionRole, Integer parentMenuRoleId,Boolean blnIsActive) {
		super();
		this.serMenuItemRoleId = serMenuItemRoleId;
		this.txtRoleCode = txtRoleCode;
		this.txtRoleName = txtRoleName;
		this.blnIsCompositionRole = blnIsCompositionRole;
		this.parentMenuRoleId = parentMenuRoleId;
		this.blnIsActive = blnIsActive;
	}

	public Integer getSerMenuItemRoleId() {
		return serMenuItemRoleId;
	}

	public void setSerMenuItemRoleId(Integer serMenuItemRoleId) {
		this.serMenuItemRoleId = serMenuItemRoleId;
	}

	public String getTxtRoleCode() {
		return txtRoleCode;
	}

	public void setTxtRoleCode(String txtRoleCode) {
		this.txtRoleCode = txtRoleCode;
	}

	public String getTxtRoleName() {
		return txtRoleName;
	}

	public void setTxtRoleName(String txtRoleName) {
		this.txtRoleName = txtRoleName;
	}

	public Boolean getBlnIsCompositionRole() {
		return blnIsCompositionRole;
	}

	public void setBlnIsCompositionRole(Boolean blnIsCompositionRole) {
		this.blnIsCompositionRole = blnIsCompositionRole;
	}

	public Integer getParentMenuRoleId() {
		return parentMenuRoleId;
	}

	public void setParentMenuRoleId(Integer parentMenuRoleId) {
		this.parentMenuRoleId = parentMenuRoleId;
	}

	public Boolean getBlnIsActive() {
		return blnIsActive;
	}

	public void setBlnIsActive(Boolean blnIsActive) {
		this.blnIsActive = blnIsActive;
	}
	

}
