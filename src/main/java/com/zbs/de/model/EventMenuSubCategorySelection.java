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
		this.numTotalPrice = numTotalPrice;
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
		this.numFinalPrice = numFinalPrice;
	}
	

}
