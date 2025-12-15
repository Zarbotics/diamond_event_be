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
@Table(name = "event_quote_line")
@NamedQuery(name = "EventQuoteLine.findAll", query = "SELECT a FROM EventQuoteLine a")
public class EventQuoteLine extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_event_quote_line_id")
	private Long serEventQuoteLineId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_event_quote_id", nullable = false)
	private EventQuote eventQuote;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "menu_item_id")
	private MenuItem menuItem;

	@Column(name = "num_quanity")
	private Double numQuantity;

	@Column(name = "num_unit_price")
	private Double numUnitPrice;

	@Column(name = "num_line_total")
	private Double numLineTotal;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private Map<String, Object> metadata;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "menu_item_snapshot", columnDefinition = "jsonb")
	private Map<String, Object> menuItemSnapshot;

	public EventQuoteLine() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Long getSerEventQuoteLineId() {
		return serEventQuoteLineId;
	}

	public void setSerEventQuoteLineId(Long serEventQuoteLineId) {
		this.serEventQuoteLineId = serEventQuoteLineId;
	}

	public EventQuote getEventQuote() {
		return eventQuote;
	}

	public void setEventQuote(EventQuote eventQuote) {
		this.eventQuote = eventQuote;
	}

	public MenuItem getMenuItem() {
		return menuItem;
	}

	public void setMenuItem(MenuItem menuItem) {
		this.menuItem = menuItem;
	}

	public Double getNumQuantity() {
		return numQuantity;
	}

	public void setNumQuantity(Double numQuantity) {
		this.numQuantity = numQuantity;
	}

	public Double getNumUnitPrice() {
		return numUnitPrice;
	}

	public void setNumUnitPrice(Double numUnitPrice) {
		this.numUnitPrice = numUnitPrice;
	}

	public Double getNumLineTotal() {
		return numLineTotal;
	}

	public void setNumLineTotal(Double numLineTotal) {
		this.numLineTotal = numLineTotal;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	public Map<String, Object> getMenuItemSnapshot() {
		return menuItemSnapshot;
	}

	public void setMenuItemSnapshot(Map<String, Object> menuItemSnapshot) {
		this.menuItemSnapshot = menuItemSnapshot;
	}

}
