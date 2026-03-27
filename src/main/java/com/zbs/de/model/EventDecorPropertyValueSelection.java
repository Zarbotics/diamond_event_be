package com.zbs.de.model;

import java.io.Serializable;

import org.hibernate.annotations.DynamicInsert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@DynamicInsert
@Data
@Table(name = "event_decor_property_value_selection")
@NamedQuery(name = "EventDecorPropertyValueSelection.findAll", query = "SELECT a FROM EventDecorPropertyValueSelection a")
public class EventDecorPropertyValueSelection extends BaseEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_event_decor_property_value_id")
	private Integer serEventDecorPropertyValueId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_event_decor_property_id")
	private EventDecorPropertySelection eventDecorPropertySelection;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_property_value_id")
	private DecorCategoryPropertyValue propertyValue;

	public Integer getSerEventDecorPropertyValueId() {
		return serEventDecorPropertyValueId;
	}

	public void setSerEventDecorPropertyValueId(Integer serEventDecorPropertyValueId) {
		this.serEventDecorPropertyValueId = serEventDecorPropertyValueId;
	}

	public EventDecorPropertySelection getEventDecorPropertySelection() {
		return eventDecorPropertySelection;
	}

	public void setEventDecorPropertySelection(EventDecorPropertySelection eventDecorPropertySelection) {
		this.eventDecorPropertySelection = eventDecorPropertySelection;
	}

	public DecorCategoryPropertyValue getPropertyValue() {
		return propertyValue;
	}

	public void setPropertyValue(DecorCategoryPropertyValue propertyValue) {
		this.propertyValue = propertyValue;
	}
	
	@Override
	public boolean equals(Object o) {
	    if (this == o) return true;
	    if (!(o instanceof EventDecorPropertyValueSelection)) return false;
	    EventDecorPropertyValueSelection that = (EventDecorPropertyValueSelection) o;
	    return serEventDecorPropertyValueId != null && serEventDecorPropertyValueId.equals(that.serEventDecorPropertyValueId);
	}

	@Override
	public int hashCode() {
	    return getClass().hashCode();
	}

}
