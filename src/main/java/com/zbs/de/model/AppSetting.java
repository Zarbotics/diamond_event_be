package com.zbs.de.model;

import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * One thing the business configures.
 *
 * <h3>Why these are rows and not constants</h3>
 *
 * The people who need to change them are not the people who can deploy. "How
 * far ahead do we take bookings" and "are we selling catering this season" are
 * commercial decisions that move on a Tuesday afternoon, and every day one of
 * them lives in Java is a day the business has to ask an engineer for
 * something it should be able to do itself.
 *
 * <h3>Why each row describes itself</h3>
 *
 * A settings table of key and value is one nobody trusts: there is no way to
 * render an editor for it, nothing stops somebody typing "yes" into a number,
 * and after a year nobody can say which of forty rows still matters.
 *
 * <p>
 * So a setting carries its own type, its bounds, the heading it belongs under
 * and a sentence saying what changing it does. The admin screen is generated
 * from that rather than hard-coded against a list of keys, which is what lets
 * a new setting appear without a new screen.
 */
@Entity
@Table(name = "app_setting")
public class AppSetting extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_setting_id")
	private Integer serSettingId;

	/** Dotted and lowercase: {@code catering.booking.enabled}. */
	@Column(name = "txt_key", nullable = false, length = 120)
	private String txtKey;

	/**
	 * Held as text whatever it is.
	 *
	 * <p>
	 * PostgreSQL has no union type and four nullable value columns would be
	 * wrong three ways at once. {@link #txtValueType} is what tells a reader
	 * how to parse this, and {@code ServiceAppSettings} is the only thing that
	 * should be doing the parsing.
	 */
	@Column(name = "txt_value", columnDefinition = "text")
	private String txtValue;

	/** {@code BOOLEAN}, {@code INTEGER}, {@code DECIMAL} or {@code STRING}. */
	@Column(name = "txt_value_type", nullable = false, length = 20)
	private String txtValueType;

	/** What the office sees. Nobody should have to read a key to find a switch. */
	@Column(name = "txt_label", nullable = false, length = 200)
	private String txtLabel;

	@Column(name = "txt_description", columnDefinition = "text")
	private String txtDescription;

	@Column(name = "txt_group", length = 80)
	private String txtGroup;

	@Column(name = "num_display_order")
	private Integer numDisplayOrder;

	/** Null means unbounded. Only meaningful for the numeric types. */
	@Column(name = "num_min")
	private BigDecimal numMin;

	@Column(name = "num_max")
	private BigDecimal numMax;

	/*
	 * For a CHOICE setting: the permitted values, comma separated, in the
	 * order they should be offered.
	 *
	 * A free text box would let somebody type "Totl" into the VAT mode and
	 * turn VAT off across the business without a word of complaint.
	 */
	@Column(name = "txt_allowed_values")
	private String txtAllowedValues;

	/**
	 * Whether a customer-facing caller may read it.
	 *
	 * <p>
	 * The journey needs to know whether catering is on sale. It has no
	 * business knowing anything else that ends up in this table, and defaulting
	 * this to false means a setting added later is private until somebody
	 * decides otherwise.
	 */
	@Column(name = "bln_is_public", nullable = false)
	private Boolean blnIsPublic = Boolean.FALSE;

	public Integer getSerSettingId() {
		return serSettingId;
	}

	public void setSerSettingId(Integer serSettingId) {
		this.serSettingId = serSettingId;
	}

	public String getTxtKey() {
		return txtKey;
	}

	public void setTxtKey(String txtKey) {
		this.txtKey = txtKey;
	}

	public String getTxtValue() {
		return txtValue;
	}

	public void setTxtValue(String txtValue) {
		this.txtValue = txtValue;
	}

	public String getTxtAllowedValues() {
		return txtAllowedValues;
	}

	public void setTxtAllowedValues(String txtAllowedValues) {
		this.txtAllowedValues = txtAllowedValues;
	}

	public String getTxtValueType() {
		return txtValueType;
	}

	public void setTxtValueType(String txtValueType) {
		this.txtValueType = txtValueType;
	}

	public String getTxtLabel() {
		return txtLabel;
	}

	public void setTxtLabel(String txtLabel) {
		this.txtLabel = txtLabel;
	}

	public String getTxtDescription() {
		return txtDescription;
	}

	public void setTxtDescription(String txtDescription) {
		this.txtDescription = txtDescription;
	}

	public String getTxtGroup() {
		return txtGroup;
	}

	public void setTxtGroup(String txtGroup) {
		this.txtGroup = txtGroup;
	}

	public Integer getNumDisplayOrder() {
		return numDisplayOrder;
	}

	public void setNumDisplayOrder(Integer numDisplayOrder) {
		this.numDisplayOrder = numDisplayOrder;
	}

	public BigDecimal getNumMin() {
		return numMin;
	}

	public void setNumMin(BigDecimal numMin) {
		this.numMin = numMin;
	}

	public BigDecimal getNumMax() {
		return numMax;
	}

	public void setNumMax(BigDecimal numMax) {
		this.numMax = numMax;
	}

	public Boolean getBlnIsPublic() {
		return blnIsPublic;
	}

	public void setBlnIsPublic(Boolean blnIsPublic) {
		this.blnIsPublic = blnIsPublic;
	}
}
