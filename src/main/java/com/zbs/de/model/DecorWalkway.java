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

import java.io.Serializable;

import org.hibernate.annotations.DynamicInsert;

@Entity
@DynamicInsert
@Data
@Table(name = "decor_walkway")
@NamedQuery(name = "DecorWalkway.findAll", query = "SELECT a FROM DecorWalkway a")
public class DecorWalkway extends BaseEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_walkway_id")
	private Integer serWalkwayId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_event_master_id")
	private EventMaster eventMaster;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_carpet_color_id")
	private DecorColorMaster carpetColor;

	@Column(name = "num_flowers")
	private Integer numFlowers;

	@Column(name = "num_vases")
	private Integer numVases;

	@Column(name = "num_candles")
	private Integer numCandles;

	public Integer getSerWalkwayId() {
		return serWalkwayId;
	}

	public void setSerWalkwayId(Integer serWalkwayId) {
		this.serWalkwayId = serWalkwayId;
	}

	public EventMaster getEventMaster() {
		return eventMaster;
	}

	public void setEventMaster(EventMaster eventMaster) {
		this.eventMaster = eventMaster;
	}

	public DecorColorMaster getCarpetColor() {
		return carpetColor;
	}

	public void setCarpetColor(DecorColorMaster carpetColor) {
		this.carpetColor = carpetColor;
	}

	public Integer getNumFlowers() {
		return numFlowers;
	}

	public void setNumFlowers(Integer numFlowers) {
		this.numFlowers = numFlowers;
	}

	public Integer getNumVases() {
		return numVases;
	}

	public void setNumVases(Integer numVases) {
		this.numVases = numVases;
	}

	public Integer getNumCandles() {
		return numCandles;
	}

	public void setNumCandles(Integer numCandles) {
		this.numCandles = numCandles;
	}

}