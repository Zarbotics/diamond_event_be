package com.zbs.de.model;

import java.io.Serializable;
import java.math.BigDecimal;

import java.util.Map;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.zbs.de.util.enums.EnmItineraryUnitType;

@Entity
@Table(name = "itinerary_assignment_detail")
@NamedQuery(name = "ItineraryAssignmentDetail.findAll", query = "SELECT a FROM ItineraryAssignmentDetail a")
public class ItineraryAssignmentDetail extends BaseEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_itinerary_assignment_detail_id")
	private Long serItineraryAssignmentDetailId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_itinerary_assignment_id", nullable = false)
	private ItineraryAssignment itineraryAssignment;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_itinerary_item_id", nullable = false)
	private ItineraryItem itineraryItem;

	@Column(name = "num_display_order")
	private Integer numDisplayOrder;

	@Enumerated(EnumType.STRING)
	@Column(name = "enm_multiplier_type", length = 20)
	private EnmItineraryUnitType enmMultiplierType = EnmItineraryUnitType.PER_GUEST;

	@Column(name = "num_multiplier_value", precision = 10, scale = 3)
	private BigDecimal numMultiplierValue = BigDecimal.ONE;

	@Column(name = "bnl_is_required")
	private Boolean blnIsRequired = false;

	@Column(name = "num_override_price")
	private BigDecimal numOverridePrice;

	@Column(name = "txt_notes")
	private String txtNotes;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "dependency_expression", columnDefinition = "jsonb")
	private Map<String, Object> dependencyExpression;

	public Long getSerItineraryAssignmentDetailId() {
		return serItineraryAssignmentDetailId;
	}

	public void setSerItineraryAssignmentDetailId(Long serItineraryAssignmentDetailId) {
		this.serItineraryAssignmentDetailId = serItineraryAssignmentDetailId;
	}

	public ItineraryAssignment getItineraryAssignment() {
		return itineraryAssignment;
	}

	public void setItineraryAssignment(ItineraryAssignment itineraryAssignment) {
		this.itineraryAssignment = itineraryAssignment;
	}

	public ItineraryItem getItineraryItem() {
		return itineraryItem;
	}

	public void setItineraryItem(ItineraryItem itineraryItem) {
		this.itineraryItem = itineraryItem;
	}

	public Integer getNumDisplayOrder() {
		return numDisplayOrder;
	}

	public void setNumDisplayOrder(Integer numDisplayOrder) {
		this.numDisplayOrder = numDisplayOrder;
	}

	public EnmItineraryUnitType getEnmMultiplierType() {
		return enmMultiplierType;
	}

	public void setEnmMultiplierType(EnmItineraryUnitType enmMultiplierType) {
		this.enmMultiplierType = enmMultiplierType;
	}

	public BigDecimal getNumMultiplierValue() {
		return numMultiplierValue;
	}

	public void setNumMultiplierValue(BigDecimal numMultiplierValue) {
		this.numMultiplierValue = numMultiplierValue;
	}

	public Boolean getBlnIsRequired() {
		return blnIsRequired;
	}

	public void setBlnIsRequired(Boolean blnIsRequired) {
		this.blnIsRequired = blnIsRequired;
	}

	public BigDecimal getNumOverridePrice() {
		return numOverridePrice;
	}

	public void setNumOverridePrice(BigDecimal numOverridePrice) {
		this.numOverridePrice = numOverridePrice;
	}

	public String getTxtNotes() {
		return txtNotes;
	}

	public void setTxtNotes(String txtNotes) {
		this.txtNotes = txtNotes;
	}

	public Map<String, Object> getDependencyExpression() {
		return dependencyExpression;
	}

	public void setDependencyExpression(Map<String, Object> dependencyExpression) {
		this.dependencyExpression = dependencyExpression;
	}

}