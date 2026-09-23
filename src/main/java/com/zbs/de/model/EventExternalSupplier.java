package com.zbs.de.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A supplier the customer is bringing themselves.
 *
 * <p>
 * Their own photographer, a particular mehndi artist, the cake maker the
 * family has used for twenty years. The venue has to know who these people
 * are — access, a delivery window, parking, somebody to ring on the day — and
 * the terms the customer accepts on the last step say the booking may be
 * cancelled over an undeclared third party. The declaration matters on both
 * sides, which is why it is a record rather than a sentence.
 *
 * <p>
 * It used to be a sentence: one free-text box per booking, into which a
 * customer might write "none", or three paragraphs containing two names and a
 * phone number. Nobody can plan a day from that.
 */
@Entity
@Table(name = "event_external_supplier")
public class EventExternalSupplier extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_event_external_supplier_id")
	private Long serEventExternalSupplierId;

	@ManyToOne
	@JoinColumn(name = "ser_event_master_id")
	private EventMaster eventMaster;

	/**
	 * What they do, from the list the office keeps.
	 *
	 * <p>
	 * This was free text, on the reasoning that the supplier worth hearing
	 * about is the one the venue has not thought of. The reasoning was about
	 * coverage, and a list the office can add to answers it better: free text
	 * gave "DJ", "dj", "Disc Jockey" and "Music" as four different things, so
	 * the one thing the office wants to do with a category — find every
	 * photographer arriving on Saturday — could not be done at all.
	 *
	 * <p>
	 * Nullable, because a category can be retired and because the migration in
	 * V19 could only match what was there. A supplier with no category is still
	 * a supplier the venue must let in.
	 */
	@ManyToOne
	@JoinColumn(name = "ser_supplier_category_id")
	private ExternalSupplierCategory supplierCategory;

	@Column(name = "txt_supplier_name")
	private String txtSupplierName;

	@Column(name = "txt_contact_name")
	private String txtContactName;

	@Column(name = "txt_contact_phone")
	private String txtContactPhone;

	@Column(name = "txt_contact_email")
	private String txtContactEmail;

	/** Arrival window, access needs — the things that genuinely are a sentence. */
	@Column(name = "txt_notes", columnDefinition = "TEXT")
	private String txtNotes;

	@Column(name = "num_display_order")
	private Integer numDisplayOrder;

	public ExternalSupplierCategory getSupplierCategory() {
		return supplierCategory;
	}

	public void setSupplierCategory(ExternalSupplierCategory supplierCategory) {
		this.supplierCategory = supplierCategory;
	}

	public Long getSerEventExternalSupplierId() {
		return serEventExternalSupplierId;
	}

	public void setSerEventExternalSupplierId(Long serEventExternalSupplierId) {
		this.serEventExternalSupplierId = serEventExternalSupplierId;
	}

	public EventMaster getEventMaster() {
		return eventMaster;
	}

	public void setEventMaster(EventMaster eventMaster) {
		this.eventMaster = eventMaster;
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

	/**
	 * Whether this row says anything at all.
	 *
	 * <p>
	 * A category on its own counts. The journey's form offers a blank row to
	 * type into, and somebody who picked "DJ" and then went to find the phone
	 * number has told us something worth keeping — where somebody who opened
	 * the step and left has not.
	 */
	public boolean isEmpty() {
		return supplierCategory == null && isBlank(txtSupplierName) && isBlank(txtContactName)
				&& isBlank(txtContactPhone) && isBlank(txtContactEmail) && isBlank(txtNotes);
	}

	private static boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}
}
