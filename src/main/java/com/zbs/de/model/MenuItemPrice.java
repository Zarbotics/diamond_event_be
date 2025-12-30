package com.zbs.de.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "menu_item_price")
@NamedQuery(name = "MenuItemPrice.findAll", query = "SELECT a FROM MenuItemPrice a")
public class MenuItemPrice extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_price_id")
	private Long serPriceId;

	// *** Link to MenuItem ***
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_menu_item_id", nullable = false)
	private MenuItem menuItem;

	// *** Link to PriceVersion ***
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_price_version_id", nullable = false)
	private PriceVersion priceVersion;

	// *** Basic Price ***
	@Column(name = "num_base_price", precision = 10, scale = 2)
	private BigDecimal numBasePrice;

	// *** Currency ***
	@Column(name = "txt_currency", length = 3)
	private String txtCurrency = "GBP";

	// *** Pricing Model ***
	@Column(name = "txt_price_model", length = 50)
	private String txtPriceModel; // "FIXED_PER_ITEM", "PER_GUEST", "PER_HOUR", "FLAT"

	// *** Unit Type ***
	@Column(name = "txt_unit_type", length = 20)
	private String txtUnitType; // "PER_ITEM", "PER_GUEST", "PER_HOUR", "FLAT"

	// *** Quantity Settings ***
	@Column(name = "num_min_quantity")
	private Integer numMinQuantity = 1;

	@Column(name = "num_max_quantity")
	private Integer numMaxQuantity;

	// *** Guest Range ***
	@Column(name = "num_min_guests")
	private Integer numMinGuests;

	@Column(name = "num_max_guests")
	private Integer numMaxGuests;

	// *** Time Settings (for hourly pricing) ***
	@Column(name = "num_min_hours")
	private Integer numMinHours;

	@Column(name = "num_max_hours")
	private Integer numMaxHours;

	// *** JSON Configuration for Flexibility ***
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "price_config", columnDefinition = "jsonb")
	private Map<String, Object> priceConfig;

	// *** Active Status ***
	@Column(name = "bln_is_active")
	private Boolean blnIsActive = true;

	@Column(name = "bln_is_default")
	private Boolean blnIsDefault = false;

	// *** Validity Period ***
	@Column(name = "dte_valid_from")
	private Date dteValidFrom;

	@Column(name = "dte_valid_to")
	private Date dteValidTo;

	// Constructors
	public MenuItemPrice() {
	}

	public MenuItemPrice(MenuItem menuItem, PriceVersion priceVersion, BigDecimal numBasePrice) {
		this.menuItem = menuItem;
		this.priceVersion = priceVersion;
		this.numBasePrice = numBasePrice;
	}

	// Helper Methods
	public BigDecimal calculatePrice(Integer quantity, Integer guestCount, Integer hours) {
		if (numBasePrice == null)
			return BigDecimal.ZERO;

		BigDecimal calculatedPrice = numBasePrice;

		// Apply unit type calculation
		if ("PER_GUEST".equals(txtUnitType) && guestCount != null && guestCount > 0) {
			calculatedPrice = numBasePrice.multiply(BigDecimal.valueOf(guestCount));
		} else if ("PER_ITEM".equals(txtUnitType) && quantity != null && quantity > 0) {
			calculatedPrice = numBasePrice.multiply(BigDecimal.valueOf(quantity));
		} else if ("PER_HOUR".equals(txtUnitType) && hours != null && hours > 0) {
			calculatedPrice = numBasePrice.multiply(BigDecimal.valueOf(hours));
		}
		// FLAT price - no multiplication

		return calculatedPrice;
	}

	public boolean isValidForContext(Integer guestCount, Integer quantity, Date date) {
		// Check guest range
		if (guestCount != null) {
			if (numMinGuests != null && guestCount < numMinGuests)
				return false;
			if (numMaxGuests != null && guestCount > numMaxGuests)
				return false;
		}

		// Check quantity range
		if (quantity != null) {
			if (numMinQuantity != null && quantity < numMinQuantity)
				return false;
			if (numMaxQuantity != null && quantity > numMaxQuantity)
				return false;
		}

		// Check date validity
		if (date != null) {
			if (dteValidFrom != null && date.before(dteValidFrom))
				return false;
			if (dteValidTo != null && date.after(dteValidTo))
				return false;
		}

		return true;
	}

	// Getters and Setters
	public Long getSerPriceId() {
		return serPriceId;
	}

	public void setSerPriceId(Long serPriceId) {
		this.serPriceId = serPriceId;
	}

	public MenuItem getMenuItem() {
		return menuItem;
	}

	public void setMenuItem(MenuItem menuItem) {
		this.menuItem = menuItem;
	}

	public PriceVersion getPriceVersion() {
		return priceVersion;
	}

	public void setPriceVersion(PriceVersion priceVersion) {
		this.priceVersion = priceVersion;
	}

	public BigDecimal getNumBasePrice() {
		return numBasePrice;
	}

	public void setNumBasePrice(BigDecimal numBasePrice) {
		this.numBasePrice = numBasePrice;
	}

	public String getTxtCurrency() {
		return txtCurrency;
	}

	public void setTxtCurrency(String txtCurrency) {
		this.txtCurrency = txtCurrency;
	}

	public String getTxtPriceModel() {
		return txtPriceModel;
	}

	public void setTxtPriceModel(String txtPriceModel) {
		this.txtPriceModel = txtPriceModel;
	}

	public String getTxtUnitType() {
		return txtUnitType;
	}

	public void setTxtUnitType(String txtUnitType) {
		this.txtUnitType = txtUnitType;
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

	public Integer getNumMinGuests() {
		return numMinGuests;
	}

	public void setNumMinGuests(Integer numMinGuests) {
		this.numMinGuests = numMinGuests;
	}

	public Integer getNumMaxGuests() {
		return numMaxGuests;
	}

	public void setNumMaxGuests(Integer numMaxGuests) {
		this.numMaxGuests = numMaxGuests;
	}

	public Integer getNumMinHours() {
		return numMinHours;
	}

	public void setNumMinHours(Integer numMinHours) {
		this.numMinHours = numMinHours;
	}

	public Integer getNumMaxHours() {
		return numMaxHours;
	}

	public void setNumMaxHours(Integer numMaxHours) {
		this.numMaxHours = numMaxHours;
	}

	public Map<String, Object> getPriceConfig() {
		return priceConfig;
	}

	public void setPriceConfig(Map<String, Object> priceConfig) {
		this.priceConfig = priceConfig;
	}

	public Boolean getBlnIsActive() {
		return blnIsActive;
	}

	public void setBlnIsActive(Boolean blnIsActive) {
		this.blnIsActive = blnIsActive;
	}

	public Boolean getBlnIsDefault() {
		return blnIsDefault;
	}

	public void setBlnIsDefault(Boolean blnIsDefault) {
		this.blnIsDefault = blnIsDefault;
	}

	public Date getDteValidFrom() {
		return dteValidFrom;
	}

	public void setDteValidFrom(Date dteValidFrom) {
		this.dteValidFrom = dteValidFrom;
	}

	public Date getDteValidTo() {
		return dteValidTo;
	}

	public void setDteValidTo(Date dteValidTo) {
		this.dteValidTo = dteValidTo;
	}
}