package com.zbs.de.model;

import java.util.Map;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "itinerary_item")
@NamedQuery(name = "ItineraryItem.findAll", query = "SELECT a FROM ItineraryItem a")
public class ItineraryItem extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_itinerary_item_id")
	private Long serItineraryItemId;

	@Column(name = "txt_code")
	private String txtCode;

	@Column(name = "txt_name")
	private String txtName;

	@ManyToOne
	@JoinColumn(name = "ser_itinerary_item_type_id")
	private ItineraryItemType itineraryItemType;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "metadata", columnDefinition = "jsonb")
	private Map<String, Object> metadata;

	public ItineraryItem() {
		super();
	}

	public Long getSerItineraryItemId() {
		return serItineraryItemId;
	}

	public void setSerItineraryItemId(Long serItineraryItemId) {
		this.serItineraryItemId = serItineraryItemId;
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

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	public ItineraryItemType getItineraryItemType() {
		return itineraryItemType;
	}

	public void setItineraryItemType(ItineraryItemType itineraryItemType) {
		this.itineraryItemType = itineraryItemType;
	}
	

}
