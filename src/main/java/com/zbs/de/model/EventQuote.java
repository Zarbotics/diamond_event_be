package com.zbs.de.model;

import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(name = "event_quote")
@NamedQuery(name = "EventQuote.findAll", query = "SELECT a FROM EventQuote a")
public class EventQuote extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_event_quote_id")
	private Long serEventEuoteId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_event_master_id", nullable = false)
	private EventMaster eventMaster;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "price_version_id", nullable = false)
	private PriceVersion priceVersion;

	@Column(name = "txt_currency")
	private String txtCurrency;

	@Column(name = "num_computed_total")
	private Double numComputedTotal;

	@Column(name = "num_guest_count_at_quote")
	private Integer numGuestCountAtQuote;

	@Column(name = "txt_snapshot_version")
	private String txtSnapshotVersion;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private Map<String, Object> metadata;

	public EventQuote() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Long getSerEventEuoteId() {
		return serEventEuoteId;
	}

	public void setSerEventEuoteId(Long serEventEuoteId) {
		this.serEventEuoteId = serEventEuoteId;
	}

	public EventMaster getEventMaster() {
		return eventMaster;
	}

	public void setEventMaster(EventMaster eventMaster) {
		this.eventMaster = eventMaster;
	}

	public PriceVersion getPriceVersion() {
		return priceVersion;
	}

	public void setPriceVersion(PriceVersion priceVersion) {
		this.priceVersion = priceVersion;
	}

	public String getTxtCurrency() {
		return txtCurrency;
	}

	public void setTxtCurrency(String txtCurrency) {
		this.txtCurrency = txtCurrency;
	}

	public Double getNumComputedTotal() {
		return numComputedTotal;
	}

	public void setNumComputedTotal(Double numComputedTotal) {
		this.numComputedTotal = numComputedTotal;
	}

	public Integer getNumGuestCountAtQuote() {
		return numGuestCountAtQuote;
	}

	public void setNumGuestCountAtQuote(Integer numGuestCountAtQuote) {
		this.numGuestCountAtQuote = numGuestCountAtQuote;
	}

	public String getTxtSnapshotVersion() {
		return txtSnapshotVersion;
	}

	public void setTxtSnapshotVersion(String txtSnapshotVersion) {
		this.txtSnapshotVersion = txtSnapshotVersion;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

}
