package com.zbs.de.model.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * One rule: what a dish — or every event — needs, and how it is counted.
 *
 * <p>
 * The names travel with the ids so the office's list reads as sentences
 * without holding two more lookups to make sense of itself.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DtoEquipmentRequirement {

	private Long serEquipmentRequirementId;

	private Long serMenuItemId;
	private String txtMenuItemName;
	private Boolean blnAppliesToEveryEvent;

	private Integer serEquipmentItemId;
	private String txtEquipmentItemName;

	private BigDecimal numQuantity;

	/** PER_GUEST, PER_TABLE, PER_STATION or PER_EVENT. */
	private String txtBasis;

	private Integer numGuestsPerStation;
	private BigDecimal numSparePercent;
	private String txtNotes;
	private Boolean blnIsActive;

	public Long getSerEquipmentRequirementId() { return serEquipmentRequirementId; }
	public void setSerEquipmentRequirementId(Long v) { this.serEquipmentRequirementId = v; }

	public Long getSerMenuItemId() { return serMenuItemId; }
	public void setSerMenuItemId(Long v) { this.serMenuItemId = v; }

	public String getTxtMenuItemName() { return txtMenuItemName; }
	public void setTxtMenuItemName(String v) { this.txtMenuItemName = v; }

	public Boolean getBlnAppliesToEveryEvent() { return blnAppliesToEveryEvent; }
	public void setBlnAppliesToEveryEvent(Boolean v) { this.blnAppliesToEveryEvent = v; }

	public Integer getSerEquipmentItemId() { return serEquipmentItemId; }
	public void setSerEquipmentItemId(Integer v) { this.serEquipmentItemId = v; }

	public String getTxtEquipmentItemName() { return txtEquipmentItemName; }
	public void setTxtEquipmentItemName(String v) { this.txtEquipmentItemName = v; }

	public BigDecimal getNumQuantity() { return numQuantity; }
	public void setNumQuantity(BigDecimal v) { this.numQuantity = v; }

	public String getTxtBasis() { return txtBasis; }
	public void setTxtBasis(String v) { this.txtBasis = v; }

	public Integer getNumGuestsPerStation() { return numGuestsPerStation; }
	public void setNumGuestsPerStation(Integer v) { this.numGuestsPerStation = v; }

	public BigDecimal getNumSparePercent() { return numSparePercent; }
	public void setNumSparePercent(BigDecimal v) { this.numSparePercent = v; }

	public String getTxtNotes() { return txtNotes; }
	public void setTxtNotes(String v) { this.txtNotes = v; }

	public Boolean getBlnIsActive() { return blnIsActive; }
	public void setBlnIsActive(Boolean v) { this.blnIsActive = v; }
}
