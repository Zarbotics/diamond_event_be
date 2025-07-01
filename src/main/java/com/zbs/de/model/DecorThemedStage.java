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
@Table(name = "decor_themed_stage")
@NamedQuery(name = "DecorThemedStage.findAll", query = "SELECT a FROM DecorThemedStage a")
public class DecorThemedStage extends BaseEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_themed_stage_id")
	private Integer serThemedStageId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_event_master_id")
	private EventMaster eventMaster;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_decor_color_id")
	private DecorColorMaster flowerColor;

	@Column(name = "txt_platform_size")
	private String txtPlatformSize;

//	@OneToMany(mappedBy = "themedStage", cascade = CascadeType.ALL)
//	private List<DecorReferenceDocument> referenceImages;

	public Integer getSerThemedStageId() {
		return serThemedStageId;
	}

	public void setSerThemedStageId(Integer serThemedStageId) {
		this.serThemedStageId = serThemedStageId;
	}

	public EventMaster getEventMaster() {
		return eventMaster;
	}

	public void setEventMaster(EventMaster eventMaster) {
		this.eventMaster = eventMaster;
	}

	public DecorColorMaster getFlowerColor() {
		return flowerColor;
	}

	public void setFlowerColor(DecorColorMaster flowerColor) {
		this.flowerColor = flowerColor;
	}

	public String getTxtPlatformSize() {
		return txtPlatformSize;
	}

	public void setTxtPlatformSize(String txtPlatformSize) {
		this.txtPlatformSize = txtPlatformSize;
	}

//	public List<DecorReferenceDocument> getReferenceImages() {
//		return referenceImages;
//	}
//
//	public void setReferenceImages(List<DecorReferenceDocument> referenceImages) {
//		this.referenceImages = referenceImages;
//	}

}