package com.zbs.de.model.dto;

import java.math.BigDecimal;
import java.util.List;

public class DtoPriceCalculationResult {

	private BigDecimal subtotal;
	private BigDecimal total;
	private String currency = "GBP";
	private List<DtoCalculatedItem> items;
	private List<String> warnings;
	private List<String> errors;

	// Getters and Setters
	public BigDecimal getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(BigDecimal subtotal) {
		this.subtotal = subtotal;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public List<DtoCalculatedItem> getItems() {
		return items;
	}

	public void setItems(List<DtoCalculatedItem> items) {
		this.items = items;
	}

	public List<String> getWarnings() {
		return warnings;
	}

	public void setWarnings(List<String> warnings) {
		this.warnings = warnings;
	}

	public List<String> getErrors() {
		return errors;
	}

	public void setErrors(List<String> errors) {
		this.errors = errors;
	}
}