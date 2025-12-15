package com.zbs.de.model;

import jakarta.persistence.*;

@Entity
@Table(name = "event_itinerary_result")
@NamedQuery(name = "EventItineraryResult.findAll", query = "SELECT a FROM EventItineraryResult a")
public class EventItineraryResult extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_event_Itinerary_result_id")
	private Long serEventItineraryResultId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_event_master_id", nullable = false)
	private EventMaster eventMaster;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "itinerary_item_id", nullable = false)
	private ItineraryItem itineraryItem;

	@Column(name = "num_computed_qty")
	private Double numComputedQty;

	public EventItineraryResult() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Long getSerEventItineraryResultId() {
		return serEventItineraryResultId;
	}

	public void setSerEventItineraryResultId(Long serEventItineraryResultId) {
		this.serEventItineraryResultId = serEventItineraryResultId;
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

	public Double getNumComputedQty() {
		return numComputedQty;
	}

	public void setNumComputedQty(Double numComputedQty) {
		this.numComputedQty = numComputedQty;
	}

}
