package com.zbs.de.model;

import java.io.Serializable;
import java.util.List;

import org.hibernate.annotations.DynamicInsert;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
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
	private Boolean blnIsMainEvent;

	@ManyToOne
	@JoinColumn(name = "parent_event_type")
	private EventType parentEventType;

	@OneToMany(mappedBy = "eventType", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<EventTypeDocument> eventTypeDocuments;

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

	public EventType getParentEventType() {
		return parentEventType;
	}

	public void setParentEventType(EventType parentEventType) {
		this.parentEventType = parentEventType;
	}

	public Boolean getBlnIsMainEvent() {
		return blnIsMainEvent;
	}

	public void setBlnIsMainEvent(Boolean blnIsMainEvent) {
		this.blnIsMainEvent = blnIsMainEvent;
	}

	public List<EventTypeDocument> getEventTypeDocuments() {
		return eventTypeDocuments;
	}

	public void setEventTypeDocuments(List<EventTypeDocument> eventTypeDocuments) {
		this.eventTypeDocuments = eventTypeDocuments;
	}

}
