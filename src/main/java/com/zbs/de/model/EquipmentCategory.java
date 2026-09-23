package com.zbs.de.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** A heading the equipment list groups under — Crockery, Linen, Serveware. */
@Entity
@Table(name = "equipment_category")
public class EquipmentCategory extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_equipment_category_id")
	private Integer serEquipmentCategoryId;

	@Column(name = "txt_name", nullable = false, length = 120)
	private String txtName;

	@Column(name = "txt_description", columnDefinition = "text")
	private String txtDescription;

	@Column(name = "num_display_order")
	private Integer numDisplayOrder;

	public Integer getSerEquipmentCategoryId() {
		return serEquipmentCategoryId;
	}

	public void setSerEquipmentCategoryId(Integer serEquipmentCategoryId) {
		this.serEquipmentCategoryId = serEquipmentCategoryId;
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

	public Integer getNumDisplayOrder() {
		return numDisplayOrder;
	}

	public void setNumDisplayOrder(Integer numDisplayOrder) {
		this.numDisplayOrder = numDisplayOrder;
	}
}
