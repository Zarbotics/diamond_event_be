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
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

import org.hibernate.annotations.DynamicInsert;

@Entity
@DynamicInsert
@Data
@Getter
@Setter
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

//	@ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "ser_menu_food_id", nullable = false)
//	private MenuFoodMaster menuFoodMaster;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_menu_item_id")
	private MenuItem menuItem;

	@Column(name = "txt_food_type") // e.g., "Dessert", "Drink", etc.
	private String txtFoodType;

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

}
