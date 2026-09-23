package com.zbs.de.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/** One supplier the customer is bringing themselves. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DtoEventExternalSupplier {

	private Long serEventExternalSupplierId;

	/**
	 * Which kind of supplier, from the list the office keeps.
	 *
	 * <p>
	 * The name travels beside the id because every reader of this — the review
	 * screen, the enquiry document, the office's event form — wants to print
	 * "Photographer", and none of them should have to fetch the category list
	 * to turn a number into a word.
	 */
	private Long serSupplierCategoryId;
	private String txtSupplierCategoryName;

	private String txtSupplierName;
	private String txtContactName;
	private String txtContactPhone;
	private String txtContactEmail;
	private String txtNotes;
	private Integer numDisplayOrder;

	public Long getSerEventExternalSupplierId() {
		return serEventExternalSupplierId;
	}

	public void setSerEventExternalSupplierId(Long serEventExternalSupplierId) {
		this.serEventExternalSupplierId = serEventExternalSupplierId;
	}

	public Long getSerSupplierCategoryId() {
		return serSupplierCategoryId;
	}

	public void setSerSupplierCategoryId(Long serSupplierCategoryId) {
		this.serSupplierCategoryId = serSupplierCategoryId;
	}

	public String getTxtSupplierCategoryName() {
		return txtSupplierCategoryName;
	}

	public void setTxtSupplierCategoryName(String txtSupplierCategoryName) {
		this.txtSupplierCategoryName = txtSupplierCategoryName;
	}

	public String getTxtSupplierName() {
		return txtSupplierName;
	}

	public void setTxtSupplierName(String txtSupplierName) {
		this.txtSupplierName = txtSupplierName;
	}

	public String getTxtContactName() {
		return txtContactName;
	}

	public void setTxtContactName(String txtContactName) {
		this.txtContactName = txtContactName;
	}

	public String getTxtContactPhone() {
		return txtContactPhone;
	}

	public void setTxtContactPhone(String txtContactPhone) {
		this.txtContactPhone = txtContactPhone;
	}

	public String getTxtContactEmail() {
		return txtContactEmail;
	}

	public void setTxtContactEmail(String txtContactEmail) {
		this.txtContactEmail = txtContactEmail;
	}

	public String getTxtNotes() {
		return txtNotes;
	}

	public void setTxtNotes(String txtNotes) {
		this.txtNotes = txtNotes;
	}

	public Integer getNumDisplayOrder() {
		return numDisplayOrder;
	}

	public void setNumDisplayOrder(Integer numDisplayOrder) {
		this.numDisplayOrder = numDisplayOrder;
	}
}
