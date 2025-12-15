package com.zbs.de.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(name = "menu_item_role")
@NamedQuery(name = "MenuItemRole.findAll", query = "SELECT a FROM MenuItemRole a")
public class MenuItemRole extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_menu_item_role_id")
	private Integer serMenuItemRoleId;

	@Column(name = "txt_code", unique = true)
	private String txtRoleCode;

	@Column(name = "txt_name", nullable = false)
	private String txtRoleName;

	@Column(name = "bln_is_component_role")
	private Boolean blnIsCompositionRole;

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
}
