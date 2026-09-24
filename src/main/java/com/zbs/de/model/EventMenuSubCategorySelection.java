package com.zbs.de.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "event_menu_subcategory_selection")
@NamedQuery(name = "EventMenuSubCategorySelection.findAll", query = "SELECT a FROM EventMenuSubCategorySelection a")
public class EventMenuSubCategorySelection extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer serEventMenuSubCategoryId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_event_menu_category_id", nullable = false)
	private EventMenuCategorySelection eventCategory;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_menu_sub_category_id", nullable = false)
	private MenuItem subCategory;

	@Column(name = "num_total_price", nullable = false)
	private BigDecimal numTotalPrice;
	
	@Column(name = "num_final_price", nullable = false)
	private BigDecimal numFinalPrice;

	@OneToMany(mappedBy = "eventSubCategory", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<EventMenuFoodSelection> items = new ArrayList<>();

	public Integer getSerEventMenuSubCategoryId() {
		return serEventMenuSubCategoryId;
	}

	public void setSerEventMenuSubCategoryId(Integer serEventMenuSubCategoryId) {
		this.serEventMenuSubCategoryId = serEventMenuSubCategoryId;
	}

	public EventMenuCategorySelection getEventCategory() {
		return eventCategory;
	}

	public void setEventCategory(EventMenuCategorySelection eventCategory) {
		this.eventCategory = eventCategory;
	}

	public MenuItem getSubCategory() {
		return subCategory;
	}

	public void setSubCategory(MenuItem subCategory) {
		this.subCategory = subCategory;
	}

	public BigDecimal getNumTotalPrice() {
		return numTotalPrice;
	}

	public void setNumTotalPrice(BigDecimal numTotalPrice) {
		/* The column is NOT NULL; null here would roll back the whole save. */
		this.numTotalPrice = numTotalPrice == null ? BigDecimal.ZERO : numTotalPrice;
	}

	public List<EventMenuFoodSelection> getItems() {
		return items;
	}

	public void setItems(List<EventMenuFoodSelection> items) {
		this.items = items;
	}

	public BigDecimal getNumFinalPrice() {
		return numFinalPrice;
	}

	public void setNumFinalPrice(BigDecimal numFinalPrice) {
		/* The column is NOT NULL; null here would roll back the whole save. */
		this.numFinalPrice = numFinalPrice == null ? BigDecimal.ZERO : numFinalPrice;
	}
	

	/**
	 * Both prices at once, because there is a rule between them.
	 *
	 * <h3>Why a missing price is not a refusal</h3>
	 *
	 * Both columns are {@code NOT NULL}, and both used to be written straight
	 * from the payload. A client that left either out — the customer journey
	 * posting a menu it has not priced, a test fixture, an integration sending
	 * only the dishes — did not get a booking priced at zero. It got a
	 * constraint violation, which rolled back the <em>entire</em> save: the
	 * customer, the date, the venue, the running order, all of it, lost over
	 * one absent decimal.
	 *
	 * <p>
	 * That is the wrong trade. The selection is the part the business cannot
	 * replace; the price is recomputed by the pricing engine from the dish rows
	 * on every save, and those rows are nullable precisely because a price can
	 * legitimately be unknown. So a missing price is recorded, not rejected.
	 *
	 * <h3>Why the charge falls back to the listed price</h3>
	 *
	 * Because that is what an empty price box means everywhere else. The office
	 * types a figure here only to charge something other than the usual; leaving
	 * it empty is "charge the usual", not "charge nothing". Defaulting the
	 * charge to zero instead would quietly give the category away.
	 *
	 * @param listed  what the catalogue makes it, or null.
	 * @param charged what the office is charging instead, or null for the listed
	 *                price.
	 */
	public void priceAs(BigDecimal listed, BigDecimal charged) {
		this.numTotalPrice = listed == null ? BigDecimal.ZERO : listed;
		this.numFinalPrice = charged == null ? this.numTotalPrice : charged;
	}
}
