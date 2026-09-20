package com.zbs.de.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/** A kind of supplier a customer may declare. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DtoExternalSupplierCategory {

	private Long serSupplierCategoryId;
	private String txtName;
	private String txtDescription;
	private Integer numDisplayOrder;

	/**
	 * Whether the journey offers it.
	 *
	 * <p>
	 * Carried because the office's screen needs to show retired categories in
	 * order to bring one back. The customer-facing list never contains an
	 * inactive one, so the journey can ignore this.
	 */
	private Boolean blnIsActive;

	/**
	 * How many declared suppliers point at it.
	 *
	 * <p>
	 * Only filled in for the office, and only so that deleting a category can
	 * say what it would affect. "Delete" on a list of words is easy to press;
	 * "used by 14 suppliers" is the thing that stops somebody.
	 */
	private Long numSupplierCount;

	public Long getSerSupplierCategoryId() {
		return serSupplierCategoryId;
	}

	public void setSerSupplierCategoryId(Long serSupplierCategoryId) {
		this.serSupplierCategoryId = serSupplierCategoryId;
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

	public Integer getNumDisplayOrder() {
		return numDisplayOrder;
	}

	public void setNumDisplayOrder(Integer numDisplayOrder) {
		this.numDisplayOrder = numDisplayOrder;
	}

	public Boolean getBlnIsActive() {
		return blnIsActive;
	}

	public void setBlnIsActive(Boolean blnIsActive) {
		this.blnIsActive = blnIsActive;
	}

	public Long getNumSupplierCount() {
		return numSupplierCount;
	}

	public void setNumSupplierCount(Long numSupplierCount) {
		this.numSupplierCount = numSupplierCount;
	}
}
