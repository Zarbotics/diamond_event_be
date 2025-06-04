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
import lombok.Getter;
import lombok.Setter;


@Entity
@DynamicInsert
@Data
@Getter
@Setter
@Table(name = "event_type")
@NamedQuery(name = "EventType.findAll", query = "SELECT a FROM EventType a")
public class EventType extends BaseEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_event_type_id")
	private Integer serEventTypeId;

	@Column(name = "txt_event_type_code")
	private String txtEventTypeCode;

	@Column(name = "txt_event_type_name")
	private String txtEventTypeName;

	@Column(name = "bln_is_main_event")
	private String blnIsMainEvent;

	@ManyToOne
	@JoinColumn(name = "parent_event_type")
	private EventType parentEventType;

	public Integer getSerEventTypeId() {
		return serEventTypeId;
	}

	public void setSerEventTypeId(Integer serEventTypeId) {
		this.serEventTypeId = serEventTypeId;
	}

	public String getTxtEventTypeCode() {
		return txtEventTypeCode;
	}

	public void setTxtEventTypeCode(String txtEventTypeCode) {
		this.txtEventTypeCode = txtEventTypeCode;
	}

	public String getTxtEventTypeName() {
		return txtEventTypeName;
	}

	public void setTxtEventTypeName(String txtEventTypeName) {
		this.txtEventTypeName = txtEventTypeName;
	}

	public String getBlnIsMainEvent() {
		return blnIsMainEvent;
	}

	public void setBlnIsMainEvent(String blnIsMainEvent) {
		this.blnIsMainEvent = blnIsMainEvent;
	}

	public EventType getParentEventType() {
		return parentEventType;
	}

	public void setParentEventType(EventType parentEventType) {
		this.parentEventType = parentEventType;
	}

}
