package com.zbs.de.model;

import java.util.Map;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "pricing_rule")
@NamedQuery(name = "PricingRule.findAll", query = "SELECT a FROM PricingRule a")
public class PricingRule extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_pricing_rule_id")
	private Long serPricingRuleId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "price_version_id", nullable = false)
	private PriceVersion priceVersion;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "expression", columnDefinition = "jsonb")
	private Map<String, Object> expression; // AST-like rule

	@Column(name = "num_priority")
	private Integer numPriority;

	@Column(name = "bln_stackable")
	private Boolean blnStackable;

	@Column(name = "txt_label")
	private String txtLabel;

	public Long getSerPricingRuleId() {
		return serPricingRuleId;
	}

	public void setSerPricingRuleId(Long serPricingRuleId) {
		this.serPricingRuleId = serPricingRuleId;
	}

	public PriceVersion getPriceVersion() {
		return priceVersion;
	}

	public void setPriceVersion(PriceVersion priceVersion) {
		this.priceVersion = priceVersion;
	}

	public Map<String, Object> getExpression() {
		return expression;
	}

	public void setExpression(Map<String, Object> expression) {
		this.expression = expression;
	}

	public Integer getNumPriority() {
		return numPriority;
	}

	public void setNumPriority(Integer numPriority) {
		this.numPriority = numPriority;
	}

	public Boolean getBlnStackable() {
		return blnStackable;
	}

	public void setBlnStackable(Boolean blnStackable) {
		this.blnStackable = blnStackable;
	}

	public String getTxtLabel() {
		return txtLabel;
	}

	public void setTxtLabel(String txtLabel) {
		this.txtLabel = txtLabel;
	}

}
