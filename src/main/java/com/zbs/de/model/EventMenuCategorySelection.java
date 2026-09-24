package com.zbs.de.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "event_menu_category_selection")
@NamedQuery(name = "EventMenuCategorySelection.findAll", query = "SELECT a FROM EventMenuCategorySelection a")
public class EventMenuCategorySelection extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer serEventMenuCategoryId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_event_master_id")
	private EventMaster eventMaster;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_delivery_booking_id")
	private CateringDeliveryBooking cateringDeliveryBooking;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_menu_category_id", nullable = false)
	private MenuItem category;

	@Column(name = "num_total_price", nullable = false)
	private BigDecimal numTotalPrice;

	@Column(name = "num_final_price", nullable = false)
	private BigDecimal numFinalPrice;

	@OneToMany(mappedBy = "eventCategory", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<EventMenuSubCategorySelection> subCategories = new ArrayList<>();

	public Integer getSerEventMenuCategoryId() {
		return serEventMenuCategoryId;
	}

	public void setSerEventMenuCategoryId(Integer serEventMenuCategoryId) {
		this.serEventMenuCategoryId = serEventMenuCategoryId;
	}

	public EventMaster getEventMaster() {
		return eventMaster;
	}

	public void setEventMaster(EventMaster eventMaster) {
		this.eventMaster = eventMaster;
	}

	public MenuItem getCategory() {
		return category;
	}

	public void setCategory(MenuItem category) {
		this.category = category;
	}

	public BigDecimal getNumTotalPrice() {
		return numTotalPrice;
	}

	public void setNumTotalPrice(BigDecimal numTotalPrice) {
		/* The column is NOT NULL; null here would roll back the whole save. */
		this.numTotalPrice = numTotalPrice == null ? BigDecimal.ZERO : numTotalPrice;
	}

	public BigDecimal getNumFinalPrice() {
		return numFinalPrice;
	}

	public void setNumFinalPrice(BigDecimal numFinalPrice) {
		/* The column is NOT NULL; null here would roll back the whole save. */
		this.numFinalPrice = numFinalPrice == null ? BigDecimal.ZERO : numFinalPrice;
	}

	public List<EventMenuSubCategorySelection> getSubCategories() {
		return subCategories;
	}

	public void setSubCategories(List<EventMenuSubCategorySelection> subCategories) {
		this.subCategories = subCategories;
	}

	public CateringDeliveryBooking getCateringDeliveryBooking() {
		return cateringDeliveryBooking;
	}

	public void setCateringDeliveryBooking(CateringDeliveryBooking cateringDeliveryBooking) {
		this.cateringDeliveryBooking = cateringDeliveryBooking;
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
