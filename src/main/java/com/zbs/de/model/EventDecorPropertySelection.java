package com.zbs.de.model;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

import org.hibernate.annotations.DynamicInsert;

@Entity
@DynamicInsert
@Data
@Table(name = "event_decor_property_selection")
@NamedQuery(name = "EventDecorPropertySelection.findAll", query = "SELECT a FROM EventDecorPropertySelection a")
public class EventDecorPropertySelection extends BaseEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_event_decor_property_id")
	private Integer serEventDecorPropertyId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_event_decor_category_selection_id", nullable = false)
	private EventDecorCategorySelection eventDecorCategorySelection;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_property_id")
	private DecorCategoryPropertyMaster property;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_property_value_id")
	private DecorCategoryPropertyValue selectedValue;

	@Column(name = "num_price")
	private BigDecimal numPrice = BigDecimal.ZERO;

	public Integer getSerEventDecorPropertyId() {
		return serEventDecorPropertyId;
	}

	public void setSerEventDecorPropertyId(Integer serEventDecorPropertyId) {
		this.serEventDecorPropertyId = serEventDecorPropertyId;
	}

	public EventDecorCategorySelection getEventDecorCategorySelection() {
		return eventDecorCategorySelection;
	}

	public void setEventDecorCategorySelection(EventDecorCategorySelection eventDecorCategorySelection) {
		this.eventDecorCategorySelection = eventDecorCategorySelection;
	}

	public DecorCategoryPropertyMaster getProperty() {
		return property;
	}

	public void setProperty(DecorCategoryPropertyMaster property) {
		this.property = property;
	}

	public DecorCategoryPropertyValue getSelectedValue() {
		return selectedValue;
	}

	public void setSelectedValue(DecorCategoryPropertyValue selectedValue) {
		this.selectedValue = selectedValue;
	}

	public BigDecimal getNumPrice() {
		return numPrice;
	}

	public void setNumPrice(BigDecimal numPrice) {
		this.numPrice = numPrice;
	}

}