package com.zbs.de.model;

import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * What one offering costs on one price list.
 *
 * <h2>What this makes answerable</h2>
 *
 * "What did this dish cost when the Khans booked in March?" and "put next
 * year's prices in now, to take effect on the first of April." Neither was
 * possible: a {@link MenuOffering} carries one price, so changing it overwrites
 * what the old one was, and a price rise had to be typed in on the morning it
 * started.
 *
 * <h2>Why the price list is per offering</h2>
 *
 * Because that is where a price means something. A chocolate brownie is £3.50
 * plated and £4.00 on a stand — the distinction M2 exists to express — and a
 * price list keyed on the *dish* would collapse the two back together the day it
 * was adopted.
 *
 * <p>
 * That is also why the empty {@code price_entry} table was not used for this.
 * It prices a target id under one of ITEM, ROLE, BUNDLE, COMBINATION, STATION
 * or TYPE, by one of DIRECT, MIN_OF, MAX_OF, SUM or FORMULA — a general pricing
 * engine, never populated, for a business that prices dishes. {@link
 * PriceVersion} beside it is a different matter: a named, dated, prioritised
 * list with DRAFT, PUBLISHED and RETIRED, which is exactly the right shape and
 * is reused here rather than replaced.
 *
 * <h2>What this is not, yet</h2>
 *
 * Stage M5 of §17.3. Every offering's current price was copied onto a "Current
 * prices" version, and <strong>nothing reads this table</strong>:
 * {@code menu_offering.num_price} is still what the application charges from.
 * The stage is undone by dropping a table, and the reads move across in M5b —
 * at which point an offering's own price becomes a cache of whichever version
 * is effective, rather than the truth.
 */
@Entity
@Table(name = "menu_offering_price")
public class MenuOfferingPrice extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_offering_price_id")
	private Long serOfferingPriceId;

	@ManyToOne
	@JoinColumn(name = "ser_offering_id", nullable = false)
	private MenuOffering offering;

	@ManyToOne
	@JoinColumn(name = "ser_price_version_id", nullable = false)
	private PriceVersion priceVersion;

	/**
	 * What it costs on this list.
	 *
	 * <p>
	 * Nullable, and deliberately recorded even when empty. An offering that is
	 * unpriced is a fact about the price list — 334 of them in the live
	 * catalogue — and leaving those rows out would make "this dish had no price
	 * in March" indistinguishable from "this dish did not exist in March".
	 */
	@Column(name = "num_price")
	private BigDecimal numPrice;

	/**
	 * {@code PER_GUEST} or {@code FLAT}, for this list.
	 *
	 * <p>
	 * Travels with the price rather than staying on the offering, because a
	 * price change can change how it is charged: the same sweet cart may be
	 * £2.00 a head this year and a £600 flat hire next. Keeping the rule in one
	 * place would apply next year's rule to last year's figure.
	 */
	@Column(name = "txt_price_rule")
	private String txtPriceRule;

	public Long getSerOfferingPriceId() {
		return serOfferingPriceId;
	}

	public void setSerOfferingPriceId(Long serOfferingPriceId) {
		this.serOfferingPriceId = serOfferingPriceId;
	}

	public MenuOffering getOffering() {
		return offering;
	}

	public void setOffering(MenuOffering offering) {
		this.offering = offering;
	}

	public PriceVersion getPriceVersion() {
		return priceVersion;
	}

	public void setPriceVersion(PriceVersion priceVersion) {
		this.priceVersion = priceVersion;
	}

	public BigDecimal getNumPrice() {
		return numPrice;
	}

	public void setNumPrice(BigDecimal numPrice) {
		this.numPrice = numPrice;
	}

	public String getTxtPriceRule() {
		return txtPriceRule;
	}

	public void setTxtPriceRule(String txtPriceRule) {
		this.txtPriceRule = txtPriceRule;
	}
}
