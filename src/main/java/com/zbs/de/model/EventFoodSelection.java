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
@Table(name = "event_food_selection")
@NamedQuery(name = "EventFoodSelection.findAll", query = "SELECT a FROM EventFoodSelection a")
public class EventFoodSelection extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_event_food_selection_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_event_master_id", nullable = false)
	private EventMaster eventMaster;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "menu_item_id", nullable = false)
	private MenuItem menuItem;

	@Column(name = "qty")
	private Double qty;

	@Column(name = "unit_price_override")
	private Double unitPriceOverride;

	@Column(name = "txt_notes", columnDefinition = "text")
	private String txtNotes;
	
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "snapshot_metadata", columnDefinition = "jsonb")
	private Map<String, Object> snapshotMetadata;
}
