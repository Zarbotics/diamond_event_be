package com.zbs.de.model;

import java.io.Serializable;

import org.hibernate.annotations.DynamicInsert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@DynamicInsert
@Data
@Table(name = "event_decor_extras_selection")
@NamedQuery(name = "EventDecorExtrasSelection.findAll", query = "SELECT a FROM EventDecorExtrasSelection a")
public class EventDecorExtrasSelection extends BaseEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_extras_selection_id")
	private Integer serExtrasSelectionId;

	@Column(name = "txt_dynamic_property1")
	private String txtDynamicProperty1;

	@Column(name = "txt_dynamic_property2")
	private String txtDynamicProperty2;

	@ManyToOne
	@JoinColumn(name = "ser_event_master_id")
	private EventMaster eventMaster;

	@ManyToOne
	@JoinColumn(name = "ser_extras_id")
	private DecorExtrasMaster decorExtrasMaster;

	@ManyToOne
	@JoinColumn(name = "ser_extra_option_id")
	private DecorExtrasOption decorExtrasOption;

	public Integer getSerExtrasSelectionId() {
		return serExtrasSelectionId;
	}

	public void setSerExtrasSelectionId(Integer serExtrasSelectionId) {
		this.serExtrasSelectionId = serExtrasSelectionId;
	}

	public EventMaster getEventMaster() {
		return eventMaster;
	}

	public void setEventMaster(EventMaster eventMaster) {
		this.eventMaster = eventMaster;
	}

	public DecorExtrasMaster getDecorExtrasMaster() {
		return decorExtrasMaster;
	}

	public void setDecorExtrasMaster(DecorExtrasMaster decorExtrasMaster) {
		this.decorExtrasMaster = decorExtrasMaster;
	}

	public DecorExtrasOption getDecorExtrasOption() {
		return decorExtrasOption;
	}

	public void setDecorExtrasOption(DecorExtrasOption decorExtrasOption) {
		this.decorExtrasOption = decorExtrasOption;
	}

	public String getTxtDynamicProperty1() {
		return txtDynamicProperty1;
	}

	public void setTxtDynamicProperty1(String txtDynamicProperty1) {
		this.txtDynamicProperty1 = txtDynamicProperty1;
	}

	public String getTxtDynamicProperty2() {
		return txtDynamicProperty2;
	}

	public void setTxtDynamicProperty2(String txtDynamicProperty2) {
		this.txtDynamicProperty2 = txtDynamicProperty2;
	}

}
