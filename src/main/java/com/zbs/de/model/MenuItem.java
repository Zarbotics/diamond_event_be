package com.zbs.de.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import org.hibernate.annotations.Type;
import io.hypersistence.utils.hibernate.type.basic.PostgreSQLLTreeType;

@Entity
@Table(name = "menu_item")
@NamedQuery(name = "MenuItem.findAll", query = "SELECT a FROM MenuItem a")
public class MenuItem extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_menu_item_id")
	private Long serMenuItemId;

	@Column(name = "txt_code", unique = true)
	private String txtCode;

	@Column(name = "txt_name", nullable = false)
	private String txtName;

	@Column(name = "txt_short_name")
	private String txtShortName;

	@Column(name = "txt_description", columnDefinition = "text")
	private String txtDescription;

	@Column(name = "num_display_order")
	private Integer numDisplayOrder;

	@Column(name = "bln_is_selectable")
	private Boolean blnIsSelectable = true;

	/**
	 * Roles: CATEGORY | ITEM | STATION | GROUP | SELECTION | BUNDLE
	 */
	@Column(name = "txt_role", nullable = false)
	private String txtRole;

	@Column(name = "txt_type", nullable = false)
	private String txtType;

	@ManyToOne
	@JoinColumn(name = "ser_menu_item_role_id")
	private MenuItemRole menuItemRole;

	@ManyToOne
	@JoinColumn(name = "parent_menu_item_id")
	private MenuItem parent;

	@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = false)
	private List<MenuItem> children = new ArrayList<>();

	/**
	 * Store hierarchical path using Postgres ltree type (DDL must set column type
	 * ltree). Example: "Grazing.GrazingBar.Selections.Fruits"
	 */
	@Type(PostgreSQLLTreeType.class)
	@Column(name = "txt_path", columnDefinition = "ltree", nullable = false)
	private String txtPath;

	@Column(name = "num_default_servings_per_guest")
	private Double numDefaultServingsPerGuest;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "metadata", columnDefinition = "jsonb")
	private Map<String, Object> metadata;

	// Constructors, getters, setters

	public MenuItem() {
		super();
	}

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

	public String getTxtRole() {
		return txtRole;
	}

	public void setTxtRole(String txtRole) {
		this.txtRole = txtRole;
	}

	public MenuItem getParent() {
		return parent;
	}

	public void setParent(MenuItem parent) {
		this.parent = parent;
	}

	public List<MenuItem> getChildren() {
		return children;
	}

	public void setChildren(List<MenuItem> children) {
		this.children = children;
	}

	public String getTxtPath() {
		return txtPath;
	}

	public void setTxtPath(String txtPath) {
		this.txtPath = txtPath;
	}

	public Double getNumDefaultServingsPerGuest() {
		return numDefaultServingsPerGuest;
	}

	public void setNumDefaultServingsPerGuest(Double numDefaultServingsPerGuest) {
		this.numDefaultServingsPerGuest = numDefaultServingsPerGuest;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	public String getTxtType() {
		return txtType;
	}

	public void setTxtType(String txtType) {
		this.txtType = txtType;
	}

	public MenuItemRole getMenuItemRole() {
		return menuItemRole;
	}

	public void setMenuItemRole(MenuItemRole menuItemRole) {
		this.menuItemRole = menuItemRole;
	}

}
