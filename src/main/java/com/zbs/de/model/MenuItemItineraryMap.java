package com.zbs.de.model;

import java.util.Map;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "menu_item_itinerary_map")
@NamedQuery(name = "MenuItemItineraryMap.findAll", query = "SELECT a FROM MenuItemItineraryMap a")
public class MenuItemItineraryMap extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "map_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "menu_item_id", nullable = false)
	private MenuItem menuItem;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "itinerary_item_id", nullable = false)
	private ItineraryItem itineraryItem;

	@Column(name = "txt_multiplier_type")
	private String txtMultiplierType; // PER_GUEST|PER_DISH|PER_STATION|FIXED

	@Column(name = "txt_multiplier_value")
	private Double txtMultiplierValue;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "dependency_expression", columnDefinition = "jsonb")
	private Map<String, Object> dependencyExpression;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "metadata", columnDefinition = "jsonb")
	private Map<String, Object> metadata;

	public MenuItemItineraryMap() {
		super();
	}

	// getters/setters...
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public MenuItem getMenuItem() {
		return menuItem;
	}

	public void setMenuItem(MenuItem menuItem) {
		this.menuItem = menuItem;
	}

	public ItineraryItem getItineraryItem() {
		return itineraryItem;
	}

	public void setItineraryItem(ItineraryItem itineraryItem) {
		this.itineraryItem = itineraryItem;
	}

	public String getTxtMultiplierType() {
		return txtMultiplierType;
	}

	public void setTxtMultiplierType(String txtMultiplierType) {
		this.txtMultiplierType = txtMultiplierType;
	}

	public Double getTxtMultiplierValue() {
		return txtMultiplierValue;
	}

	public void setTxtMultiplierValue(Double txtMultiplierValue) {
		this.txtMultiplierValue = txtMultiplierValue;
	}

	public Map<String, Object> getDependencyExpression() {
		return dependencyExpression;
	}

	public void setDependencyExpression(Map<String, Object> dependencyExpression) {
		this.dependencyExpression = dependencyExpression;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}
}
