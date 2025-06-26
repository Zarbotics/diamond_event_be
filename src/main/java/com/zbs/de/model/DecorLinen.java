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
@Table(name = "decor_linen")
@NamedQuery(name = "DecorLinen.findAll", query = "SELECT a FROM DecorLinen a")
public class DecorLinen extends BaseEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_linen_id")
	private Integer serLinenId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_event_master_id")
	private EventMaster eventMaster;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_tablecloth_color_id")
	private DecorColorMaster tableclothColor;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_napkin_color_id")
	private DecorColorMaster napkinColor;

	public Integer getSerLinenId() {
		return serLinenId;
	}

	public void setSerLinenId(Integer serLinenId) {
		this.serLinenId = serLinenId;
	}

	public EventMaster getEventMaster() {
		return eventMaster;
	}

	public void setEventMaster(EventMaster eventMaster) {
		this.eventMaster = eventMaster;
	}

	public DecorColorMaster getTableclothColor() {
		return tableclothColor;
	}

	public void setTableclothColor(DecorColorMaster tableclothColor) {
		this.tableclothColor = tableclothColor;
	}

	public DecorColorMaster getNapkinColor() {
		return napkinColor;
	}

	public void setNapkinColor(DecorColorMaster napkinColor) {
		this.napkinColor = napkinColor;
	}

}