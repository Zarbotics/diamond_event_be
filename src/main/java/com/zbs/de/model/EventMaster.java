package com.zbs.de.model;

import java.io.Serializable;
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
public class EventMaster extends BaseEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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

	@Column(name = "txt_number_of_guests")
	private String txtNumberOfGuests;

	@Column(name = "num_number_of_tables")
	private Integer numNumberOfTables;

	@Column(name = "txt_bride_name")
	private String txtBrideName;

	@Column(name = "txt_groom_name")
	private String txtGroomName;

	@Column(name = "txt_birthday_celebrant")
	private String txtBirthDayCelebrant;

	@Column(name = "txt_age_category")
	private String txtAgeCategory;

	@Column(name = "txt_chief_guest")
	private String txtChiefGuest;

	@Column(name = "txt_event_status")
	private String txtEventStatus;

	/**
	 * This variable is to show how many sections has been filled by the client
	 * related Customer Journey
	 */
	@Column(name = "num_inof_filled_status")
	private Integer numInfoFilledStatus;

	@ManyToOne
	@JoinColumn(name = "ser_cust_id")
	private CustomerMaster customerMaster;

	@ManyToOne
	@JoinColumn(name = "ser_event_running_order_id")
	private EventRunningOrder eventRunningOrder;

	@ManyToOne
	@JoinColumn(name = "ser_event_type_id")
	private EventType eventType;

	@Column(name = "txt_other_event_type")
	private String txtOtherEventType;

	@ManyToOne
	@JoinColumn(name = "ser_venue_master_detail_id")
	private VenueMasterDetail venueMasterDetail;

	@ManyToOne
	@JoinColumn(name = "ser_vendor_id")
	private VendorMaster vendorMaster;

	@OneToMany(mappedBy = "eventMaster", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<EventMenuFoodSelection> foodSelections = new ArrayList<>();

	@OneToMany(mappedBy = "eventMaster", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<EventDecorCategorySelection> decorSelections = new ArrayList<>();

	public Integer getSerEventMasterId() {
		return serEventMasterId;
	}

	public void setSerEventMasterId(Integer serEventMasterId) {
		this.serEventMasterId = serEventMasterId;
	}

	public String getTxtEventMasterCode() {
		return txtEventMasterCode;
	}

	public void setTxtEventMasterCode(String txtEventMasterCode) {
		this.txtEventMasterCode = txtEventMasterCode;
	}

	public String getTxtEventMasterName() {
		return txtEventMasterName;
	}

	public void setTxtEventMasterName(String txtEventMasterName) {
		this.txtEventMasterName = txtEventMasterName;
	}

	public Date getDteEventDate() {
		return dteEventDate;
	}

	public void setDteEventDate(Date dteEventDate) {
		this.dteEventDate = dteEventDate;
	}

	public Integer getNumNumberOfGuests() {
		return numNumberOfGuests;
	}

	public void setNumNumberOfGuests(Integer numNumberOfGuests) {
		this.numNumberOfGuests = numNumberOfGuests;
	}

	public Integer getNumNumberOfTables() {
		return numNumberOfTables;
	}

	public void setNumNumberOfTables(Integer numNumberOfTables) {
		this.numNumberOfTables = numNumberOfTables;
	}

	public String getTxtBrideName() {
		return txtBrideName;
	}

	public void setTxtBrideName(String txtBrideName) {
		this.txtBrideName = txtBrideName;
	}

	public String getTxtGroomName() {
		return txtGroomName;
	}

	public void setTxtGroomName(String txtGroomName) {
		this.txtGroomName = txtGroomName;
	}

	public String getTxtBirthDayCelebrant() {
		return txtBirthDayCelebrant;
	}

	public void setTxtBirthDayCelebrant(String txtBirthDayCelebrant) {
		this.txtBirthDayCelebrant = txtBirthDayCelebrant;
	}

	public String getTxtAgeCategory() {
		return txtAgeCategory;
	}

	public void setTxtAgeCategory(String txtAgeCategory) {
		this.txtAgeCategory = txtAgeCategory;
	}

	public String getTxtChiefGuest() {
		return txtChiefGuest;
	}

	public void setTxtChiefGuest(String txtChiefGuest) {
		this.txtChiefGuest = txtChiefGuest;
	}

	public Integer getNumInfoFilledStatus() {
		return numInfoFilledStatus;
	}

	public void setNumInfoFilledStatus(Integer numInfoFilledStatus) {
		this.numInfoFilledStatus = numInfoFilledStatus;
	}

	public CustomerMaster getCustomerMaster() {
		return customerMaster;
	}

	public void setCustomerMaster(CustomerMaster customerMaster) {
		this.customerMaster = customerMaster;
	}

	public EventRunningOrder getEventRunningOrder() {
		return eventRunningOrder;
	}

	public void setEventRunningOrder(EventRunningOrder eventRunningOrder) {
		this.eventRunningOrder = eventRunningOrder;
	}

	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	public List<EventMenuFoodSelection> getFoodSelections() {
		return foodSelections;
	}

	public void setFoodSelections(List<EventMenuFoodSelection> foodSelections) {
		this.foodSelections = foodSelections;
	}

	public VendorMaster getVendorMaster() {
		return vendorMaster;
	}

	public void setVendorMaster(VendorMaster vendorMaster) {
		this.vendorMaster = vendorMaster;
	}

	public String getTxtNumberOfGuests() {
		return txtNumberOfGuests;
	}

	public void setTxtNNumberOfGuests(String txtNumberOfGuests) {
		this.txtNumberOfGuests = txtNumberOfGuests;
	}

	public String getTxtOtherEventType() {
		return txtOtherEventType;
	}

	public void setTxtOtherEventType(String txtOtherEventType) {
		this.txtOtherEventType = txtOtherEventType;
	}

	public List<EventDecorCategorySelection> getDecorSelections() {
		return decorSelections;
	}

	public void setDecorSelections(List<EventDecorCategorySelection> decorSelections) {
		this.decorSelections = decorSelections;
	}

	public void setTxtNumberOfGuests(String txtNumberOfGuests) {
		this.txtNumberOfGuests = txtNumberOfGuests;
	}

	public VenueMasterDetail getVenueMasterDetail() {
		return venueMasterDetail;
	}

	public void setVenueMasterDetail(VenueMasterDetail venueMasterDetail) {
		this.venueMasterDetail = venueMasterDetail;
	}

	public String getTxtEventStatus() {
		return txtEventStatus;
	}

	public void setTxtEventStatus(String txtEventStatus) {
		this.txtEventStatus = txtEventStatus;
	}

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
