package com.zbs.de.model;

import jakarta.persistence.Column;
import jakarta.persistence.Id;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "event_menu_food_selection")
@NamedQuery(name = "EventMenuFoodSelection.findAll", query = "SELECT a FROM EventMenuFoodSelection a")
public class EventMenuFoodSelection extends BaseEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_event_menu_food_id")
	private Integer serEventMenuFoodId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_event_master_id", nullable = false)
	private EventMaster eventMaster;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_menu_item_id")
	private MenuItem menuItem;

	@Column(name = "txt_food_type") // e.g., "Dessert", "Drink", etc.
	private String txtFoodType;

	@Column(name = "num_price")
	private BigDecimal numPrice;

	@Column(name = "num_calculated_price")
	private BigDecimal numCalculatedPrice;

	@Column(name = "num_final_price")
	private BigDecimal numFinalPrice;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_event_menu_sub_category_id")
	private EventMenuSubCategorySelection eventSubCategory;

	public Integer getSerEventMenuFoodId() {
		return serEventMenuFoodId;
	}

	public void setSerEventMenuFoodId(Integer serEventMenuFoodId) {
		this.serEventMenuFoodId = serEventMenuFoodId;
	}

	public EventMaster getEventMaster() {
		return eventMaster;
	}

	public void setEventMaster(EventMaster eventMaster) {
		this.eventMaster = eventMaster;
	}

	public String getTxtFoodType() {
		return txtFoodType;
	}

	public void setTxtFoodType(String txtFoodType) {
		this.txtFoodType = txtFoodType;
	}

	public MenuItem getMenuItem() {
		return menuItem;
	}

	public void setMenuItem(MenuItem menuItem) {
		this.menuItem = menuItem;
	}

	public BigDecimal getNumPrice() {
		return numPrice;
	}

	public void setNumPrice(BigDecimal numPrice) {
		this.numPrice = numPrice;
	}

	public EventMenuSubCategorySelection getEventSubCategory() {
		return eventSubCategory;
	}

	public void setEventSubCategory(EventMenuSubCategorySelection eventSubCategory) {
		this.eventSubCategory = eventSubCategory;
	}

	public BigDecimal getNumCalculatedPrice() {
		return numCalculatedPrice;
	}

	public void setNumCalculatedPrice(BigDecimal numCalculatedPrice) {
		this.numCalculatedPrice = numCalculatedPrice;
	}

	public BigDecimal getNumFinalPrice() {
		return numFinalPrice;
	}

	public void setNumFinalPrice(BigDecimal numFinalPrice) {
		this.numFinalPrice = numFinalPrice;
	}

}
