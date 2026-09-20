package com.zbs.de.model.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/** One kind of equipment, and how many of it the event needs. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DtoEquipmentLine {

	private Integer serEquipmentItemId;
	private String txtItemName;

	/** "each", "pair", "set of 6" — so "12 linen" says twelve of what. */
	private String txtUnit;

	private String txtCategoryName;
	private Integer numCategoryOrder;

	/** The total, rounded up once after every source has been added. */
	private Integer numQuantity;

	/** Everything that asked for this item, and why. */
	private List<DtoEquipmentSource> sources = new ArrayList<>();

	public Integer getSerEquipmentItemId() {
		return serEquipmentItemId;
	}

	public void setSerEquipmentItemId(Integer serEquipmentItemId) {
		this.serEquipmentItemId = serEquipmentItemId;
	}

	public String getTxtItemName() {
		return txtItemName;
	}

	public void setTxtItemName(String txtItemName) {
		this.txtItemName = txtItemName;
	}

	public String getTxtUnit() {
		return txtUnit;
	}

	public void setTxtUnit(String txtUnit) {
		this.txtUnit = txtUnit;
	}

	public String getTxtCategoryName() {
		return txtCategoryName;
	}

	public void setTxtCategoryName(String txtCategoryName) {
		this.txtCategoryName = txtCategoryName;
	}

	public Integer getNumCategoryOrder() {
		return numCategoryOrder;
	}

	public void setNumCategoryOrder(Integer numCategoryOrder) {
		this.numCategoryOrder = numCategoryOrder;
	}

	public Integer getNumQuantity() {
		return numQuantity;
	}

	public void setNumQuantity(Integer numQuantity) {
		this.numQuantity = numQuantity;
	}

	public List<DtoEquipmentSource> getSources() {
		return sources;
	}

	public void setSources(List<DtoEquipmentSource> sources) {
		this.sources = sources;
	}
}
