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
 * A dish, offered somewhere, on terms.
 *
 * <h2>Why this exists</h2>
 *
 * {@link MenuItem} gives a dish exactly one parent, so <em>where a dish
 * appears</em> is a property of the dish. Offering a chocolate brownie in the
 * Dessert Buffet, on the Dessert Stand, as a Trio and Served To The Table
 * therefore takes four chocolate brownies — and the live catalogue has become
 * exactly that: 368 selectable rows holding 238 distinct dishes, with
 * twenty-one desserts existing five times each.
 *
 * <p>
 * The cost is not tidiness. Renaming a dish is five edits, repricing it is
 * five, the copies have already drifted apart, and no report can total
 * "brownies for Saturday" because five ids are five different dishes.
 *
 * <h2>What belongs here and what belongs on the dish</h2>
 *
 * The dish is what it <em>is</em>: its name, its description, whether it is
 * vegetarian. The offering is what it <em>costs and where</em>: the price, the
 * per-guest rule, its position in the list. A brownie can legitimately be
 * priced differently in a buffet than plated, and that is the whole reason
 * those two fields sit here rather than on {@code MenuItem}.
 *
 * <h2>What this is not, yet</h2>
 *
 * Stage M2 of §17.3. Every selectable item was given one offering of itself
 * under its current parent, so the catalogue reads exactly as it did. Nothing
 * reads this table and no duplicates have been merged — merging is a decision
 * about two particular dishes, taken by a person in the admin screen, not a
 * guess made in a migration by matching names.
 */
@Entity
@Table(name = "menu_offering")
public class MenuOffering extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_offering_id")
	private Long serOfferingId;

	/** The dish being offered. */
	@ManyToOne
	@JoinColumn(name = "ser_menu_item_id", nullable = false)
	private MenuItem menuItem;

	/**
	 * Where it is offered: a subcategory, or a composite's section.
	 *
	 * <p>
	 * Also a {@link MenuItem}, because that is what a section is today. When
	 * sections become things of their own this is the column that moves, and
	 * nothing else has to.
	 */
	@ManyToOne
	@JoinColumn(name = "ser_section_id", nullable = false)
	private MenuItem section;

	@Column(name = "num_price")
	private BigDecimal numPrice;

	/**
	 * {@code PER_GUEST} or {@code FLAT}, and nothing else.
	 *
	 * <p>
	 * Nullable at this stage because 238 of the live items have never said, and
	 * {@code getMenuWithPrices} has been quietly assuming {@code PER_GUEST} for
	 * all of them — which turns a £2.00 sweet cart into £600 at a three hundred
	 * guest wedding with nothing on any screen saying so. M3 is where saying
	 * becomes compulsory; a {@code NOT NULL} here would have rejected the
	 * backfill of precisely the rows that need correcting.
	 *
	 * <p>
	 * Held as a string rather than the enum so that a value nobody has mapped
	 * yet arrives as data to be corrected rather than as a failure to start.
	 */
	@Column(name = "txt_price_rule")
	private String txtPriceRule;

	/** Where it sits in its section's list. */
	@Column(name = "num_position")
	private Integer numPosition;

	public Long getSerOfferingId() {
		return serOfferingId;
	}

	public void setSerOfferingId(Long serOfferingId) {
		this.serOfferingId = serOfferingId;
	}

	public MenuItem getMenuItem() {
		return menuItem;
	}

	public void setMenuItem(MenuItem menuItem) {
		this.menuItem = menuItem;
	}

	public MenuItem getSection() {
		return section;
	}

	public void setSection(MenuItem section) {
		this.section = section;
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

	public Integer getNumPosition() {
		return numPosition;
	}

	public void setNumPosition(Integer numPosition) {
		this.numPosition = numPosition;
	}
}
