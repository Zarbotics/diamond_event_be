package com.zbs.de.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.annotations.DynamicInsert;

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

	@ManyToOne
	@JoinColumn(name = "ser_cust_id")
	private CustomerMaster customerMaster;
	
	@ManyToOne
	@JoinColumn(name = "ser_event_running_order_id")
	private EventRunningOrder eventRunningOrder;
	
	@ManyToOne
	@JoinColumn(name = "ser_event_type_id")
	private EventType eventType;
	

//	@OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
//	private List<EventType> eventTypeLst = new ArrayList<>();
//
//	@OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
//	private List<VendorMaster> vendorMasterLst = new ArrayList<>();
//
//	@OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
//	private List<MenuFoodMaster> menu = new ArrayList<>();
//
////    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
////    private List<EventService> services = new ArrayList<>();
//
//	@OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
//	private List<DecorItemMaster> decorItemMasterLst = new ArrayList<>();
//
//	@OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
//	private List<PaymentTransaction> paymentTransactionLst = new ArrayList<>();

}
