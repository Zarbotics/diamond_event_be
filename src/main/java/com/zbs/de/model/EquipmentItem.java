package com.zbs.de.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * One thing the venue puts out — a dinner plate, a chafing dish, a grazing
 * board, a 120-inch round cloth.
 *
 * <h3>What it deliberately does not know</h3>
 *
 * How many there are. This is a catalogue of things that can be required, not
 * an inventory: the calculation says a Saturday needs 315 dinner plates and
 * stops there. Whether the venue owns 400 or hires them in is a different
 * question, and a half-built answer to it here would be a screen that looks
 * like it knows the cupboard when it does not.
 */
@Entity
@Table(name = "equipment_item")
public class EquipmentItem extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_equipment_item_id")
	private Integer serEquipmentItemId;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ser_equipment_category_id")
	private EquipmentCategory equipmentCategory;

	@Column(name = "txt_code", length = 60)
	private String txtCode;

	@Column(name = "txt_name", nullable = false, length = 160)
	private String txtName;

	@Column(name = "txt_description", columnDefinition = "text")
	private String txtDescription;

	/**
	 * What one of them is: "each", "pair", "set of 6".
	 *
	 * <p>
	 * Printed beside the number, because a picking list that says "12 linen"
	 * says twelve of what.
	 */
	@Column(name = "txt_unit", length = 40)
	private String txtUnit = "each";

	@Column(name = "num_display_order")
	private Integer numDisplayOrder;

	public Integer getSerEquipmentItemId() {
		return serEquipmentItemId;
	}

	public void setSerEquipmentItemId(Integer serEquipmentItemId) {
		this.serEquipmentItemId = serEquipmentItemId;
	}

	public EquipmentCategory getEquipmentCategory() {
		return equipmentCategory;
	}

	public void setEquipmentCategory(EquipmentCategory equipmentCategory) {
		this.equipmentCategory = equipmentCategory;
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

	public String getTxtDescription() {
		return txtDescription;
	}

	public void setTxtDescription(String txtDescription) {
		this.txtDescription = txtDescription;
	}

	public String getTxtUnit() {
		return txtUnit;
	}

	public void setTxtUnit(String txtUnit) {
		this.txtUnit = txtUnit;
	}

	public Integer getNumDisplayOrder() {
		return numDisplayOrder;
	}

	public void setNumDisplayOrder(Integer numDisplayOrder) {
		this.numDisplayOrder = numDisplayOrder;
	}
}
