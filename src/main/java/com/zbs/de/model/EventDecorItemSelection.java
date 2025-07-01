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
import lombok.Getter;
import lombok.Setter;

@Entity
@DynamicInsert
@Data
@Getter
@Setter
@Table(name = "event_decor_item_selection")
@NamedQuery(name = "EventDecorItemSelection.findAll", query = "SELECT a FROM EventDecorItemSelection a")
public class EventDecorItemSelection extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_event_decor_item_id")
	private Integer serEventDecorItemId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_event_master_id", nullable = false)
	private EventMaster eventMaster;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_decor_item_id", nullable = false)
	private DecorItemMaster decorItemMaster;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_decor_color_id")
	private DecorColorMaster decorColorMaster;

	@Column(name = "num_count")
	private Integer numCount;

	public Integer getSerEventDecorItemId() {
		return serEventDecorItemId;
	}

	public void setSerEventDecorItemId(Integer serEventDecorItemId) {
		this.serEventDecorItemId = serEventDecorItemId;
	}

	public EventMaster getEventMaster() {
		return eventMaster;
	}

	public void setEventMaster(EventMaster eventMaster) {
		this.eventMaster = eventMaster;
	}

	public DecorItemMaster getDecorItemMaster() {
		return decorItemMaster;
	}

	public void setDecorItemMaster(DecorItemMaster decorItemMaster) {
		this.decorItemMaster = decorItemMaster;
	}

	public DecorColorMaster getDecorColorMaster() {
		return decorColorMaster;
	}

	public void setDecorColorMaster(DecorColorMaster decorColorMaster) {
		this.decorColorMaster = decorColorMaster;
	}

	public Integer getNumCount() {
		return numCount;
	}

	public void setNumCount(Integer numCount) {
		this.numCount = numCount;
	}

}
