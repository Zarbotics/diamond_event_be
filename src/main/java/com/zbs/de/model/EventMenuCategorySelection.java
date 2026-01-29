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
		this.numTotalPrice = numTotalPrice;
	}

	public BigDecimal getNumFinalPrice() {
		return numFinalPrice;
	}

	public void setNumFinalPrice(BigDecimal numFinalPrice) {
		this.numFinalPrice = numFinalPrice;
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

}
