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
@Table(name = "decor_table_centerpiece")
@NamedQuery(name = "DecorTableCenterpiece.findAll", query = "SELECT a FROM DecorTableCenterpiece a")
public class DecorTableCenterpiece extends BaseEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_centerpiece_id")
	private Integer serCenterpieceId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_event_master_id")
	private EventMaster eventMaster;

	@OneToMany(mappedBy = "tableCenterpiece", cascade = CascadeType.ALL)
	private List<DecorReferenceDocument> referenceImages;

	public Integer getSerCenterpieceId() {
		return serCenterpieceId;
	}

	public void setSerCenterpieceId(Integer serCenterpieceId) {
		this.serCenterpieceId = serCenterpieceId;
	}

	public EventMaster getEventMaster() {
		return eventMaster;
	}

	public void setEventMaster(EventMaster eventMaster) {
		this.eventMaster = eventMaster;
	}

	public List<DecorReferenceDocument> getReferenceImages() {
		return referenceImages;
	}

	public void setReferenceImages(List<DecorReferenceDocument> referenceImages) {
		this.referenceImages = referenceImages;
	}
	
}
