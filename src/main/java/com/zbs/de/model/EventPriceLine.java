package com.zbs.de.model;

import java.io.Serializable;
import java.math.BigDecimal;

import com.zbs.de.util.enums.EnmPriceMultiplierType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * One line of the engine's working.
 *
 * <p>
 * "Chicken Karahi — £25.00 per guest × 250 guests = £6,250.00." A total is the
 * sum of these, and the reason for keeping them is that a total on its own
 * cannot be argued with. Somebody who thinks the quote should be four thousand
 * pounds needs to see which line they disagree with.
 *
 * <h3>Why the description is copied rather than joined</h3>
 *
 * A dish gets renamed. A décor category is withdrawn. Neither should change
 * what a quote sent last month says it was for, so the words are copied in at
 * the moment of pricing and the catalogue id is kept beside them for anyone who
 * wants to go and look.
 *
 * <h3>Why an override keeps both figures</h3>
 *
 * {@code numCatalogueTotal} is what the rules would have said;
 * {@code numLineTotal} is what is being charged. When a member of staff agrees
 * a price on the telephone, the difference between those two is the only record
 * that a person decided it — and a number that disagrees with the catalogue and
 * cannot say why is one the next person has to make a phone call about.
 */
@Entity
@Table(name = "event_price_line")
public class EventPriceLine extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_event_price_line_id")
	private Long serEventPriceLineId;

	/*
	 * The event id rather than a mapping to EventMaster.
	 *
	 * These rows are written and read as a block, always by event, and never
	 * navigated from. A ManyToOne here would buy nothing and would add another
	 * collection for the save to keep in step, which is the thing that has gone
	 * wrong most often in this part of the codebase.
	 */
	@Column(name = "ser_event_master_id", nullable = false)
	private Integer serEventMasterId;

	@Column(name = "txt_section", nullable = false, length = 30)
	private String txtSection;

	@Column(name = "txt_description", nullable = false, length = 255)
	private String txtDescription;

	/*
	 * A Long because a menu item id is one. Décor ids are Integer and widen
	 * without loss; going the other way would not.
	 */
	@Column(name = "num_source_id")
	private Long numSourceId;

	@Column(name = "num_unit_price", nullable = false)
	private BigDecimal numUnitPrice = BigDecimal.ZERO;

	@Enumerated(EnumType.STRING)
	@Column(name = "enm_basis", nullable = false, length = 20)
	private EnmPriceMultiplierType enmBasis = EnmPriceMultiplierType.FLAT;

	@Column(name = "num_quantity", nullable = false)
	private BigDecimal numQuantity = BigDecimal.ONE;

	@Column(name = "num_line_total", nullable = false)
	private BigDecimal numLineTotal = BigDecimal.ZERO;

	@Column(name = "bln_is_overridden", nullable = false)
	private Boolean blnIsOverridden = Boolean.FALSE;

	@Column(name = "num_catalogue_total")
	private BigDecimal numCatalogueTotal;

	/*
	 * Whether VAT was charged on this line, and how much.
	 *
	 * Per line rather than per section, because the office can make VAT apply
	 * to some parts of a bill and not others. A total that cannot show which
	 * lines carried the VAT is a total nobody can check against a return.
	 */
	@Column(name = "bln_is_vatable", nullable = false)
	private Boolean blnIsVatable = Boolean.FALSE;

	@Column(name = "num_vat", nullable = false)
	private BigDecimal numVat = BigDecimal.ZERO;

	@Column(name = "txt_reason", length = 500)
	private String txtReason;

	public Long getSerEventPriceLineId() {
		return serEventPriceLineId;
	}

	public void setSerEventPriceLineId(Long serEventPriceLineId) {
		this.serEventPriceLineId = serEventPriceLineId;
	}

	public Integer getSerEventMasterId() {
		return serEventMasterId;
	}

	public void setSerEventMasterId(Integer serEventMasterId) {
		this.serEventMasterId = serEventMasterId;
	}

	public String getTxtSection() {
		return txtSection;
	}

	public void setTxtSection(String txtSection) {
		this.txtSection = txtSection;
	}

	public String getTxtDescription() {
		return txtDescription;
	}

	public void setTxtDescription(String txtDescription) {
		this.txtDescription = txtDescription;
	}

	public Long getNumSourceId() {
		return numSourceId;
	}

	public void setNumSourceId(Long numSourceId) {
		this.numSourceId = numSourceId;
	}

	public BigDecimal getNumUnitPrice() {
		return numUnitPrice;
	}

	public void setNumUnitPrice(BigDecimal numUnitPrice) {
		this.numUnitPrice = numUnitPrice;
	}

	public EnmPriceMultiplierType getEnmBasis() {
		return enmBasis;
	}

	public void setEnmBasis(EnmPriceMultiplierType enmBasis) {
		this.enmBasis = enmBasis;
	}

	public BigDecimal getNumQuantity() {
		return numQuantity;
	}

	public void setNumQuantity(BigDecimal numQuantity) {
		this.numQuantity = numQuantity;
	}

	public BigDecimal getNumLineTotal() {
		return numLineTotal;
	}

	public void setNumLineTotal(BigDecimal numLineTotal) {
		this.numLineTotal = numLineTotal;
	}

	public Boolean getBlnIsOverridden() {
		return blnIsOverridden;
	}

	public void setBlnIsOverridden(Boolean blnIsOverridden) {
		this.blnIsOverridden = blnIsOverridden;
	}

	public BigDecimal getNumCatalogueTotal() {
		return numCatalogueTotal;
	}

	public void setNumCatalogueTotal(BigDecimal numCatalogueTotal) {
		this.numCatalogueTotal = numCatalogueTotal;
	}

	public Boolean getBlnIsVatable() {
		return blnIsVatable;
	}

	public void setBlnIsVatable(Boolean blnIsVatable) {
		this.blnIsVatable = blnIsVatable;
	}

	public BigDecimal getNumVat() {
		return numVat;
	}

	public void setNumVat(BigDecimal numVat) {
		this.numVat = numVat;
	}

	public String getTxtReason() {
		return txtReason;
	}

	public void setTxtReason(String txtReason) {
		this.txtReason = txtReason;
	}
}
