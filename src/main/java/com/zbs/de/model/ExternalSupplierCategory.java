package com.zbs.de.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A kind of supplier a customer may declare — photographer, DJ, mehndi artist.
 *
 * <h3>Why this replaced free text</h3>
 *
 * {@code event_external_supplier.txt_supplier_type} was free text, on the
 * reasoning that the supplier worth hearing about is the one the venue has not
 * thought of, and a fixed dropdown would send those back into the notes box.
 *
 * <p>
 * That reasoning was about coverage, and a list the office can add to answers
 * it better than asking every customer to invent a word. Free text gives you
 * "DJ", "dj", "Disc Jockey", "Music" and "dj + sound" as five different
 * things — so nothing can be counted, filtered, or driven from the kind of
 * supplier it is, which is exactly what the office wants to do with it: every
 * photographer arriving on Saturday, different access instructions for the cake
 * maker than for the mehndi artist.
 *
 * <p>
 * The escape hatch stayed: every supplier still carries a free-text note, there
 * is an "Other" category, and adding a category takes seconds.
 *
 * <h3>Active, not deleted</h3>
 *
 * Retiring a category must not rewrite history. A booking made last year
 * against a trade the venue no longer takes still happened, and its supplier
 * still needs a name on the run sheet — so {@code blnIsActive} stops it being
 * offered to new customers while the old rows keep pointing at it.
 */
@Entity
@Table(name = "external_supplier_category")
public class ExternalSupplierCategory extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_supplier_category_id")
	private Long serSupplierCategoryId;

	/** Unique case-insensitively, enforced by a partial index in V19. */
	@Column(name = "txt_name", nullable = false, length = 120)
	private String txtName;

	/**
	 * Shown under the name in the journey.
	 *
	 * <p>
	 * To settle "does my cousin doing the flowers count as a florist?" without
	 * a telephone call. A category list with no explanation is a quiz.
	 */
	@Column(name = "txt_description")
	private String txtDescription;

	/** The order they appear in. Null sorts last, then by name. */
	@Column(name = "num_display_order")
	private Integer numDisplayOrder;

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
}
