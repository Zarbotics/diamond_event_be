package com.zbs.de.model;

import java.util.Map;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "menu_component")
@NamedQuery(name = "MenuComponent.findAll", query = "SELECT a FROM MenuComponent a")
public class MenuComponent extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_component_id")
	private Long serComponentId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_menu_item_id", nullable = false)
	private MenuItem parentMenuItem;

	/**
	 * The child may be null if this component allows free-form entries or external
	 * choices.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "child_menu_item_id")
	private MenuItem childMenuItem;

	@Column(name = "txt_component_kind")
	private String txtComponentKind; // INCLUDED|SELECTION|OPTIONAL|GROUP

	@Column(name = "txt_display_name")
	private String txtDisplayName;

	@Column(name = "num_selection_min")
	private Integer numSelectionMin;

	@Column(name = "num_selection_max")
	private Integer numSelectionMax;

	@Column(name = "num_sequence_order")
	private Integer numSequenceOrder;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "metadata", columnDefinition = "jsonb")
	private Map<String, Object> metadata;

	public MenuComponent() {
		super();
	}

	public Long getSerComponentId() {
		return serComponentId;
	}

	public void setSerComponentId(Long serComponentId) {
		this.serComponentId = serComponentId;
	}

	public MenuItem getParentMenuItem() {
		return parentMenuItem;
	}

	public void setParentMenuItem(MenuItem parentMenuItem) {
		this.parentMenuItem = parentMenuItem;
	}

	public MenuItem getChildMenuItem() {
		return childMenuItem;
	}

	public void setChildMenuItem(MenuItem childMenuItem) {
		this.childMenuItem = childMenuItem;
	}

	public String getTxtComponentKind() {
		return txtComponentKind;
	}

	public void setTxtComponentKind(String txtComponentKind) {
		this.txtComponentKind = txtComponentKind;
	}

	public Integer getNumSelectionMin() {
		return numSelectionMin;
	}

	public void setNumSelectionMin(Integer numSelectionMin) {
		this.numSelectionMin = numSelectionMin;
	}

	public Integer getNumSelectionMax() {
		return numSelectionMax;
	}

	public void setNumSelectionMax(Integer numSelectionMax) {
		this.numSelectionMax = numSelectionMax;
	}

	public Integer getNumSequenceOrder() {
		return numSequenceOrder;
	}

	public void setNumSequenceOrder(Integer numSequenceOrder) {
		this.numSequenceOrder = numSequenceOrder;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	public String getTxtDisplayName() {
		return txtDisplayName;
	}

	public void setTxtDisplayName(String txtDisplayName) {
		this.txtDisplayName = txtDisplayName;
	}

}
