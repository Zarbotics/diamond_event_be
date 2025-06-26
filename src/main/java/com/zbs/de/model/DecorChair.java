package com.zbs.de.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Id;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

import org.hibernate.annotations.DynamicInsert;

@Entity
@DynamicInsert
@Data
@Table(name = "decor_chair")
@NamedQuery(name = "DecorChair.findAll", query = "SELECT a FROM DecorChair a")
public class DecorChair extends BaseEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_chair_id")
	private Integer serChairId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_event_master_id")
	private EventMaster eventMaster;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_chair_cover_color_id")
	private DecorColorMaster chairCoverColor;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_sash_color_id")
	private DecorColorMaster sashColor;

	@OneToMany(mappedBy = "chair", cascade = CascadeType.ALL)
	private List<DecorReferenceDocument> referenceImages;

	public Integer getSerChairId() {
		return serChairId;
	}

	public void setSerChairId(Integer serChairId) {
		this.serChairId = serChairId;
	}

	public EventMaster getEventMaster() {
		return eventMaster;
	}

	public void setEventMaster(EventMaster eventMaster) {
		this.eventMaster = eventMaster;
	}

	public DecorColorMaster getChairCoverColor() {
		return chairCoverColor;
	}

	public void setChairCoverColor(DecorColorMaster chairCoverColor) {
		this.chairCoverColor = chairCoverColor;
	}

	public DecorColorMaster getSashColor() {
		return sashColor;
	}

	public void setSashColor(DecorColorMaster sashColor) {
		this.sashColor = sashColor;
	}

	public List<DecorReferenceDocument> getReferenceImages() {
		return referenceImages;
	}

	public void setReferenceImages(List<DecorReferenceDocument> referenceImages) {
		this.referenceImages = referenceImages;
	}
	
	
}