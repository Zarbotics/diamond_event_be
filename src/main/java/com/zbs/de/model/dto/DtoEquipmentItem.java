package com.zbs.de.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/** A piece of equipment, as the office's catalogue draws it. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DtoEquipmentItem {

	private Integer serEquipmentItemId;
	private Integer serEquipmentCategoryId;
	private String txtCategoryName;
	private String txtCode;
	private String txtName;
	private String txtDescription;
	private String txtUnit;
	private Integer numDisplayOrder;
	private Boolean blnIsActive;

	/**
	 * How many rules mention it.
	 *
	 * <p>
	 * Only so that removing one is a decision rather than a reflex: a rule
	 * vanishing means an event quietly stops asking for something it needs.
	 */
	private Long numRuleCount;

	public Integer getSerEquipmentItemId() { return serEquipmentItemId; }
	public void setSerEquipmentItemId(Integer v) { this.serEquipmentItemId = v; }

	public Integer getSerEquipmentCategoryId() { return serEquipmentCategoryId; }
	public void setSerEquipmentCategoryId(Integer v) { this.serEquipmentCategoryId = v; }

	public String getTxtCategoryName() { return txtCategoryName; }
	public void setTxtCategoryName(String v) { this.txtCategoryName = v; }

	public String getTxtCode() { return txtCode; }
	public void setTxtCode(String v) { this.txtCode = v; }

	public String getTxtName() { return txtName; }
	public void setTxtName(String v) { this.txtName = v; }

	public String getTxtDescription() { return txtDescription; }
	public void setTxtDescription(String v) { this.txtDescription = v; }

	public String getTxtUnit() { return txtUnit; }
	public void setTxtUnit(String v) { this.txtUnit = v; }

	public Integer getNumDisplayOrder() { return numDisplayOrder; }
	public void setNumDisplayOrder(Integer v) { this.numDisplayOrder = v; }

	public Boolean getBlnIsActive() { return blnIsActive; }
	public void setBlnIsActive(Boolean v) { this.blnIsActive = v; }

	public Long getNumRuleCount() { return numRuleCount; }
	public void setNumRuleCount(Long v) { this.numRuleCount = v; }
}
