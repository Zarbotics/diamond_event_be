package com.zbs.de.model;

import jakarta.persistence.*;

@Entity
@Table(name = "itinerary_item_type")
@NamedQuery(name = "ItineraryItemType.findAll", query = "SELECT a FROM ItineraryItemType a")
public class ItineraryItemType extends BaseEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_itinerary_item_type_id")
	private Integer serItineraryItemTypeId;

	@Column(name = "txt_code")
	private String txtCode;

	@Column(name = "txt_name")
	private String txtName;

	public Integer getSerItineraryItemTypeId() {
		return serItineraryItemTypeId;
	}

	public void setSerItineraryItemTypeId(Integer serItineraryItemTypeId) {
		this.serItineraryItemTypeId = serItineraryItemTypeId;
	}

	public String getTxtCode() {
		return txtCode;
	}

	public void setTxtCode(String txtCode) {
		this.txtCode = txtCode;
	}

	public String getTxtName() {
		return txtName;
	}

	public void setTxtName(String txtName) {
		this.txtName = txtName;
	}

}
