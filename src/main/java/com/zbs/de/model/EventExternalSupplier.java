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
	 * What they do — "Photographer", "Mehndi artist", "Cake".
	 *
	 * <p>
	 * Free text rather than a lookup on purpose. The whole point of this record
	 * is the supplier the venue has not thought of, and a dropdown of known
	 * kinds sends exactly those back into the notes box this exists to empty.
	 */
	@Column(name = "txt_supplier_type")
	private String txtSupplierType;

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

	public String getTxtSupplierType() {
		return txtSupplierType;
	}

	public void setTxtSupplierType(String txtSupplierType) {
		this.txtSupplierType = txtSupplierType;
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

	/** Whether this row says anything at all. */
	public boolean isEmpty() {
		return isBlank(txtSupplierType) && isBlank(txtSupplierName) && isBlank(txtContactName)
				&& isBlank(txtContactPhone) && isBlank(txtContactEmail) && isBlank(txtNotes);
	}

	private static boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}
}
