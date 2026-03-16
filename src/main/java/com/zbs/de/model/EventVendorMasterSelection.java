package com.zbs.de.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(name = "event_vendor_master_selection")
@NamedQuery(name = "EventVendorMasterSelection.findAll", query = "SELECT a FROM EventVendorMasterSelection a")
public class EventVendorMasterSelection extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_event_vendor_master_selection_id")
	private Integer serEventVendorMasterSelectionId;

	@ManyToOne
	@JoinColumn(name = "ser_vendor_id")
	private VendorMaster vendorMaster;

	@ManyToOne
	@JoinColumn(name = "ser_event_master_id")
	private EventMaster eventMaster;

	public Integer getSerEventVendorMasterSelectionId() {
		return serEventVendorMasterSelectionId;
	}

	public void setSerEventVendorMasterSelectionId(Integer serEventVendorMasterSelectionId) {
		this.serEventVendorMasterSelectionId = serEventVendorMasterSelectionId;
	}

	public VendorMaster getVendorMaster() {
		return vendorMaster;
	}

	public void setVendorMaster(VendorMaster vendorMaster) {
		this.vendorMaster = vendorMaster;
	}

	public EventMaster getEventMaster() {
		return eventMaster;
	}

	public void setEventMaster(EventMaster eventMaster) {
		this.eventMaster = eventMaster;
	}

}
