package com.zbs.de.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "itinerary_assignment")
@NamedQuery(name = "ItineraryAssignment.findAll", query = "SELECT a FROM ItineraryAssignment a")
public class ItineraryAssignment extends BaseEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_itinerary_assignment_id")
	private Long serItineraryAssignmentId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_menu_item_id", nullable = false)
	private MenuItem menuItem;

	@Column(name = "txt_assignment_code", unique = true)
	private String txtAssignmentCode;

	@Column(name = "txt_assignment_name")
	private String txtAssignmentName;

	@Column(name = "txt_description", columnDefinition = "TEXT")
	private String txtDescription;

	@Column(name = "num_total_itineraries")
	private Integer numTotalItineraries = 0;

	@Column(name = "dte_valid_from")
	private Date dteValidFrom;

	@Column(name = "dte_valid_to")
	private Date dteValidTo;

	@Column(name = "num_priority")
	private Integer numPriority = 1;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "assignment_metadata", columnDefinition = "jsonb")
	private Map<String, Object> metadata;

	// Detail records
	@OneToMany(mappedBy = "itineraryAssignment", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("displayOrder ASC")
	private List<ItineraryAssignmentDetail> itineraryAssignmentDetails = new ArrayList<>();

	public Long getSerItineraryAssignmentId() {
		return serItineraryAssignmentId;
	}

	public void setSerItineraryAssignmentId(Long serItineraryAssignmentId) {
		this.serItineraryAssignmentId = serItineraryAssignmentId;
	}

	public MenuItem getMenuItem() {
		return menuItem;
	}

	public void setMenuItem(MenuItem menuItem) {
		this.menuItem = menuItem;
	}

	public String getTxtAssignmentName() {
		return txtAssignmentName;
	}

	public void setTxtAssignmentName(String txtAssignmentName) {
		this.txtAssignmentName = txtAssignmentName;
	}

	public String getTxtDescription() {
		return txtDescription;
	}

	public void setTxtDescription(String txtDescription) {
		this.txtDescription = txtDescription;
	}

	public Integer getNumTotalItineraries() {
		return numTotalItineraries;
	}

	public void setNumTotalItineraries(Integer numTotalItineraries) {
		this.numTotalItineraries = numTotalItineraries;
	}

	public Date getDteValidFrom() {
		return dteValidFrom;
	}

	public void setDteValidFrom(Date dteValidFrom) {
		this.dteValidFrom = dteValidFrom;
	}

	public Date getDteValidTo() {
		return dteValidTo;
	}

	public void setDteValidTo(Date dteValidTo) {
		this.dteValidTo = dteValidTo;
	}

	public Integer getNumPriority() {
		return numPriority;
	}

	public void setNumPriority(Integer numPriority) {
		this.numPriority = numPriority;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	public List<ItineraryAssignmentDetail> getItineraryAssignmentDetails() {
		return itineraryAssignmentDetails;
	}

	public void setItineraryAssignmentDetails(List<ItineraryAssignmentDetail> itineraryAssignmentDetails) {
		this.itineraryAssignmentDetails = itineraryAssignmentDetails;
	}

	public String getTxtAssignmentCode() {
		return txtAssignmentCode;
	}

	public void setTxtAssignmentCode(String txtAssignmentCode) {
		this.txtAssignmentCode = txtAssignmentCode;
	}

}
