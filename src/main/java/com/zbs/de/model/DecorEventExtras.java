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
@Table(name = "decor_event_extras")
@NamedQuery(name = "DecorEventExtras.findAll", query = "SELECT a FROM DecorEventExtras a")
public class DecorEventExtras extends BaseEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_event_extra_id")
	private Integer serEventExtraId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_event_master_id")
	private EventMaster eventMaster;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_extra_option_id")
	private DecorExtrasOption decorExtrasOption;

	@Column(name = "bln_is_selected")
	private Boolean blnIsSelected;

	public Integer getSerEventExtraId() {
		return serEventExtraId;
	}

	public void setSerEventExtraId(Integer serEventExtraId) {
		this.serEventExtraId = serEventExtraId;
	}

	public EventMaster getEventMaster() {
		return eventMaster;
	}

	public void setEventMaster(EventMaster eventMaster) {
		this.eventMaster = eventMaster;
	}

	public DecorExtrasOption getDecorExtrasOption() {
		return decorExtrasOption;
	}

	public void setDecorExtrasOption(DecorExtrasOption decorExtrasOption) {
		this.decorExtrasOption = decorExtrasOption;
	}

	public Boolean getBlnIsSelected() {
		return blnIsSelected;
	}

	public void setBlnIsSelected(Boolean blnIsSelected) {
		this.blnIsSelected = blnIsSelected;
	}

}