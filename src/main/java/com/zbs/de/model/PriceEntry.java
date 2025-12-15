package com.zbs.de.model;

import java.util.Map;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.zbs.de.util.enums.EnmApplyScope;
import com.zbs.de.util.enums.EnmItineraryUnitType;
import com.zbs.de.util.enums.EnmPriceCalculationMethod;

@Entity
@Table(name = "price_entry")
@NamedQuery(name = "PriceEntry.findAll", query = "SELECT a FROM PriceEntry a")
public class PriceEntry extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "price_entry_id")
	private Long serPriceEntryId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "price_version_id", nullable = false)
	private PriceVersion priceVersion;

	@Column(name = "num_target_id")
	private Long numTargetId; // menu_item_id or other

	@Column(name = "num_price")
	private Double numPrice;

	@Column(name = "txt_currency")
	private String txtCurrency;

	@Column(name = "num_min_quantity")
	private Integer numMinQuantity;

	@Column(name = "num_max_quantity")
	private Integer numMaxQuantity;

	@Enumerated(EnumType.STRING)
	@Column(name = "apply_scope")
	private EnmApplyScope applyScope; // ITEM|ROLE|BUNDLE|COMBINATION|STATION|TYPE

	@Enumerated(EnumType.STRING)
	@Column(name = "unit")
	private EnmItineraryUnitType unit; // PER_GUEST|PER_ITEM|FLAT|PER_PORTION

	@Enumerated(EnumType.STRING)
	@Column(name = "calculation_method")
	private EnmPriceCalculationMethod calculationMethod;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "metadata", columnDefinition = "jsonb")
	private Map<String, Object> metadata;

	public PriceEntry() {
		super();
	}

	public Long getSerPriceEntryId() {
		return serPriceEntryId;
	}

	public void setSerPriceEntryId(Long serPriceEntryId) {
		this.serPriceEntryId = serPriceEntryId;
	}

	public PriceVersion getPriceVersion() {
		return priceVersion;
	}

	public void setPriceVersion(PriceVersion priceVersion) {
		this.priceVersion = priceVersion;
	}

	public Long getNumTargetId() {
		return numTargetId;
	}

	public void setNumTargetId(Long numTargetId) {
		this.numTargetId = numTargetId;
	}

	public Double getNumPrice() {
		return numPrice;
	}

	public void setNumPrice(Double numPrice) {
		this.numPrice = numPrice;
	}

	public String getTxtCurrency() {
		return txtCurrency;
	}

	public void setTxtCurrency(String txtCurrency) {
		this.txtCurrency = txtCurrency;
	}

	public Integer getNumMinQuantity() {
		return numMinQuantity;
	}

	public void setNumMinQuantity(Integer numMinQuantity) {
		this.numMinQuantity = numMinQuantity;
	}

	public Integer getNumMaxQuantity() {
		return numMaxQuantity;
	}

	public void setNumMaxQuantity(Integer numMaxQuantity) {
		this.numMaxQuantity = numMaxQuantity;
	}

	public EnmApplyScope getApplyScope() {
		return applyScope;
	}

	public void setApplyScope(EnmApplyScope applyScope) {
		this.applyScope = applyScope;
	}

	public EnmItineraryUnitType getUnit() {
		return unit;
	}

	public void setUnit(EnmItineraryUnitType unit) {
		this.unit = unit;
	}

	public EnmPriceCalculationMethod getCalculationMethod() {
		return calculationMethod;
	}

	public void setCalculationMethod(EnmPriceCalculationMethod calculationMethod) {
		this.calculationMethod = calculationMethod;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

}
