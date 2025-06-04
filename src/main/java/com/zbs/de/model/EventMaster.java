package com.zbs.de.model;

import java.util.Date;

import org.hibernate.annotations.DynamicInsert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "event_master")
@NamedQuery(name = "EventMaster.findAll", query = "SELECT a FROM EventMaster a")
public class EventMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_event_master_id")
	private Integer serEventMasterId;

	@Column(name = "txt_event_master_code")
	private String txtEventMasterCode;

	@Column(name = "txt_event_master_name")
	private String txtEventMasterName;
	
	@Column(name = "dte_event_date")
	private Date dteEventDate;
	
	@Column(name = "num_number_of_guests")
	private Integer numNumberOfGuests;
	
	@Column(name = "num_number_of_tables")
	private Integer numNumberOfTables;
	

}
