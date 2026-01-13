package com.zbs.de.model;

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
@Table(name = "event_itinerary_summary")
@NamedQuery(name = "EventItinerarySummary.findAll", query = "SELECT a FROM EventItinerarySummary a")
public class EventItinerarySummary extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_event_itinerary_summary_id")
	private Long serEventItinerarySummaryId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_event_id", nullable = false)
	private EventMaster eventMaster;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_itinerary_item_id", nullable = false)
	private ItineraryItem itineraryItem;

	@Column(name = "num_total_quantity")
	private Integer numTotalQuantity;

	@Column(name = "num_sequence_order")
	private Integer numSequenceOrder;

	public Long getSerEventItinerarySummaryId() {
		return serEventItinerarySummaryId;
	}

	public void setSerEventItinerarySummaryId(Long serEventItinerarySummaryId) {
		this.serEventItinerarySummaryId = serEventItinerarySummaryId;
	}

	public EventMaster getEventMaster() {
		return eventMaster;
	}

	public void setEventMaster(EventMaster eventMaster) {
		this.eventMaster = eventMaster;
	}

	public ItineraryItem getItineraryItem() {
		return itineraryItem;
	}

	public void setItineraryItem(ItineraryItem itineraryItem) {
		this.itineraryItem = itineraryItem;
	}

	public Integer getNumTotalQuantity() {
		return numTotalQuantity;
	}

	public void setNumTotalQuantity(Integer numTotalQuantity) {
		this.numTotalQuantity = numTotalQuantity;
	}

	public Integer getNumSequenceOrder() {
		return numSequenceOrder;
	}

	public void setNumSequenceOrder(Integer numSequenceOrder) {
		this.numSequenceOrder = numSequenceOrder;
	}

}
