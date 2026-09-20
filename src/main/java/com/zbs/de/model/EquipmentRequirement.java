package com.zbs.de.model;

import java.io.Serializable;
import java.math.BigDecimal;

import com.zbs.de.util.enums.EnmEquipmentBasis;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * One sentence about what something needs.
 *
 * <p>
 * "A dessert buffet needs one dessert stand per station." "Every event needs
 * one tablecloth per table." That is the whole model: a source, an item, a
 * quantity and what the quantity counts against.
 *
 * <h3>Why a rule has one source and not three</h3>
 *
 * A requirement is attached to a dish, or to nothing — and "nothing" means
 * every event. Those are the only two that earn their place: per-dish is how
 * a grazing bar asks for boards, and per-event is how a tablecloth gets said
 * once rather than against all 238 dishes.
 *
 * <p>
 * A database constraint enforces exactly one of them, because a row with no
 * dish and the flag false is a rule that can never fire, and nobody would
 * notice it had been saved.
 *
 * <h3>Why the quantity is decimal</h3>
 *
 * Because "one chafing dish per 40 guests" is a real rule and it is 0.025 per
 * guest. An integer quantity would force that to be written as a station rule
 * with a station size, which is a different thing and would read wrongly on
 * the screen.
 */
@Entity
@Table(name = "equipment_requirement")
public class EquipmentRequirement extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_equipment_requirement_id")
	private Long serEquipmentRequirementId;

	/** The dish this is about, or null when it applies to every event. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_menu_item_id")
	private MenuItem menuItem;

	@Column(name = "bln_applies_to_every_event", nullable = false)
	private Boolean blnAppliesToEveryEvent = Boolean.FALSE;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ser_equipment_item_id", nullable = false)
	private EquipmentItem equipmentItem;

	@Column(name = "num_quantity", nullable = false)
	private BigDecimal numQuantity = BigDecimal.ONE;

	@Enumerated(EnumType.STRING)
	@Column(name = "enm_basis", nullable = false, length = 20)
	private EnmEquipmentBasis enmBasis = EnmEquipmentBasis.PER_GUEST;

	/**
	 * How many guests one station serves. Only read for {@code PER_STATION}.
	 *
	 * <p>
	 * Null means one station however many come — which is right for a cake
	 * table and wrong for a grazing bar, hence the column.
	 */
	@Column(name = "num_guests_per_station")
	private Integer numGuestsPerStation;

	/**
	 * Breakage and spares, added before rounding up.
	 *
	 * <p>
	 * 300 plates at 5% is 315. Null is none. It exists because sending exactly
	 * 300 plates to a 300-cover wedding is how a table ends up short.
	 */
	@Column(name = "num_spare_percent")
	private BigDecimal numSparePercent;

	@Column(name = "txt_notes", columnDefinition = "text")
	private String txtNotes;

	public Long getSerEquipmentRequirementId() {
		return serEquipmentRequirementId;
	}

	public void setSerEquipmentRequirementId(Long serEquipmentRequirementId) {
		this.serEquipmentRequirementId = serEquipmentRequirementId;
	}

	public MenuItem getMenuItem() {
		return menuItem;
	}

	public void setMenuItem(MenuItem menuItem) {
		this.menuItem = menuItem;
	}

	public Boolean getBlnAppliesToEveryEvent() {
		return blnAppliesToEveryEvent;
	}

	public void setBlnAppliesToEveryEvent(Boolean blnAppliesToEveryEvent) {
		this.blnAppliesToEveryEvent = blnAppliesToEveryEvent;
	}

	public EquipmentItem getEquipmentItem() {
		return equipmentItem;
	}

	public void setEquipmentItem(EquipmentItem equipmentItem) {
		this.equipmentItem = equipmentItem;
	}

	public BigDecimal getNumQuantity() {
		return numQuantity;
	}

	public void setNumQuantity(BigDecimal numQuantity) {
		this.numQuantity = numQuantity;
	}

	public EnmEquipmentBasis getEnmBasis() {
		return enmBasis;
	}

	public void setEnmBasis(EnmEquipmentBasis enmBasis) {
		this.enmBasis = enmBasis;
	}

	public Integer getNumGuestsPerStation() {
		return numGuestsPerStation;
	}

	public void setNumGuestsPerStation(Integer numGuestsPerStation) {
		this.numGuestsPerStation = numGuestsPerStation;
	}

	public BigDecimal getNumSparePercent() {
		return numSparePercent;
	}

	public void setNumSparePercent(BigDecimal numSparePercent) {
		this.numSparePercent = numSparePercent;
	}

	public String getTxtNotes() {
		return txtNotes;
	}

	public void setTxtNotes(String txtNotes) {
		this.txtNotes = txtNotes;
	}
}
