package com.zbs.de.model;

import java.io.Serializable;
import java.util.Map;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

@Entity
@DynamicInsert
@Table(name = "event_menu_itinerary")
@NamedQuery(name = "EventMenuItinerary.findAll", query = "SELECT a FROM EventMenuItinerary a")
public class EventMenuItinerary extends BaseEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_event_menu_itinerary_id")
	private Long serEventMenuItineraryId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_event_id", nullable = false)
	private EventMaster eventMaster;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_menu_item_id", nullable = false)
	private MenuItem menuItem;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_itinerary_item_id", nullable = false)
	private ItineraryItem itineraryItem;

	@Column(name = "num_calculated_quantity")
	private Integer numCalculatedQuantity;

	@Column(name = "num_squence_order")
	private Integer numSequenceOrder;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "metadata", columnDefinition = "jsonb")
	private Map<String, Object> metadata;

	public Long getSerEventMenuItineraryId() {
		return serEventMenuItineraryId;
	}

	public void setSerEventMenuItineraryId(Long serEventMenuItineraryId) {
		this.serEventMenuItineraryId = serEventMenuItineraryId;
	}

	public EventMaster getEventMaster() {
		return eventMaster;
	}

	public void setEventMaster(EventMaster eventMaster) {
		this.eventMaster = eventMaster;
	}

	public MenuItem getMenuItem() {
		return menuItem;
	}

	public void setMenuItem(MenuItem menuItem) {
		this.menuItem = menuItem;
	}

	public ItineraryItem getItineraryItem() {
		return itineraryItem;
	}

	public void setItineraryItem(ItineraryItem itineraryItem) {
		this.itineraryItem = itineraryItem;
	}

	public Integer getNumCalculatedQuantity() {
		return numCalculatedQuantity;
	}

	public void setNumCalculatedQuantity(Integer numCalculatedQuantity) {
		this.numCalculatedQuantity = numCalculatedQuantity;
	}

	public Integer getNumSequenceOrder() {
		return numSequenceOrder;
	}

	public void setNumSequenceOrder(Integer numSequenceOrder) {
		this.numSequenceOrder = numSequenceOrder;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

}
