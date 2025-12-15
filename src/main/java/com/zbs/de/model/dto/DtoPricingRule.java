package com.zbs.de.model.dto;

import java.util.Map;

public class DtoPricingRule {
	private Long serPricingRuleId;
	private Long priceVersionId;
	private Map<String, Object> expression;
	private Integer numPriority;
	private Boolean blnStackable;
	private String txtLabel;

	public DtoPricingRule() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DtoPricingRule(Long serPricingRuleId, Long priceVersionId, Map<String, Object> expression,
			Integer numPriority, Boolean blnStackable, String txtLabel) {
		super();
		this.serPricingRuleId = serPricingRuleId;
		this.priceVersionId = priceVersionId;
		this.expression = expression;
		this.numPriority = numPriority;
		this.blnStackable = blnStackable;
		this.txtLabel = txtLabel;
	}

	public Long getSerPricingRuleId() {
		return serPricingRuleId;
	}

	public void setSerPricingRuleId(Long serPricingRuleId) {
		this.serPricingRuleId = serPricingRuleId;
	}

	public Long getPriceVersionId() {
		return priceVersionId;
	}

	public void setPriceVersionId(Long priceVersionId) {
		this.priceVersionId = priceVersionId;
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