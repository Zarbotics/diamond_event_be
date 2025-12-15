package com.zbs.de.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.annotations.DynamicInsert;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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

	@Column(name = "txt_bride_first_name")
	private String txtBrideFirstName;

	@Column(name = "txt_bride_last_name")
	private String txtBrideLastName;

	@Column(name = "txt_groom_name")
	private String txtGroomName;

	@Column(name = "txt_groom_first_name")
	private String txtGroomFirstName;

	@Column(name = "txt_groom_last_name")
	private String txtGroomLastName;

	@Column(name = "txt_contact_person_first_name")
	private String txtContactPersonFirstName;

	@Column(name = "txt_contact_person_last_name")
	private String txtContactPersonLastName;

	@Column(name = "txt_contact_person_phone_no")
	private String txtContactPersonPhoneNo;

	@Column(name = "txt_birthday_celebrant")
	private String txtBirthDayCelebrant;

	@Column(name = "txt_age_category")
	private String txtAgeCategory;

	@Column(name = "txt_chief_guest")
	private String txtChiefGuest;

	@Column(name = "txt_event_status")
	private String txtEventStatus;

	@Column(name = "bln_is_couple")
	private Boolean blnIsCouple;

	@Column(name = "txt_event_remarks")
	private String txtEventRemarks;

	@Column(name = "txt_decore_remarks")
	private String txtDecoreRemarks;

	@Column(name = "txt_catering_remarks")
	private String txtCateringRemarks;

	@Column(name = "txt_external_supplier_remarks")
	private String txtExternalSupplierRemarks;

	@Column(name = "txt_event_extras_remarks")
	private String txtEventExtrasRemarks;

	@Column(name = "txt_venue_remarks")
	private String txtVenueRemarks;

	@Column(name = "is_edit_allowed")
	private Boolean isEditAllowed = true;

	/**
	 * This variable is to show how many sections has been filled by the client
	 * related Customer Journey
	 */
	@Column(name = "num_inof_filled_status")
	private Integer numInfoFilledStatus;

	@Column(name = "num_form_state")
	private Integer numFormState;

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

	// This is when you need to specify which hall you selected
	@ManyToOne
	@JoinColumn(name = "ser_venue_master_detail_id")
	private VenueMasterDetail venueMasterDetail;

	// This is when you only need venue
	@ManyToOne
	@JoinColumn(name = "serVenueMasterId")
	private VenueMaster venueMaster;

	@ManyToOne
	@JoinColumn(name = "ser_vendor_id")
	private VendorMaster vendorMaster;

	@OneToOne(mappedBy = "eventMaster", fetch = FetchType.LAZY)
	private EventBudget eventBudget;

	@OneToMany(mappedBy = "eventMaster", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<EventMenuFoodSelection> foodSelections = new ArrayList<>();

	@OneToMany(mappedBy = "eventMaster", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<EventDecorCategorySelection> decorSelections = new ArrayList<>();

	@OneToMany(mappedBy = "eventMaster", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<EventDecorExtrasSelection> extrasSelections;

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

	public String getTxtBrideFirstName() {
		return txtBrideFirstName;
	}

	public void setTxtBrideFirstName(String txtBrideFirstName) {
		this.txtBrideFirstName = txtBrideFirstName;
	}

	public String getTxtBrideLastName() {
		return txtBrideLastName;
	}

	public void setTxtBrideLastName(String txtBrideLastName) {
		this.txtBrideLastName = txtBrideLastName;
	}

	public String getTxtGroomFirstName() {
		return txtGroomFirstName;
	}

	public void setTxtGroomFirstName(String txtGroomFirstName) {
		this.txtGroomFirstName = txtGroomFirstName;
	}

	public String getTxtGroomLastName() {
		return txtGroomLastName;
	}

	public void setTxtGroomLastName(String txtGroomLastName) {
		this.txtGroomLastName = txtGroomLastName;
	}

	public Boolean getBlnIsCouple() {
		return blnIsCouple;
	}

	public void setBlnIsCouple(Boolean blnIsCouple) {
		this.blnIsCouple = blnIsCouple;
	}

	public VenueMaster getVenueMaster() {
		return venueMaster;
	}

	public void setVenueMaster(VenueMaster venueMaster) {
		this.venueMaster = venueMaster;
	}

	public List<EventDecorExtrasSelection> getExtrasSelections() {
		return extrasSelections;
	}

	public void setExtrasSelections(List<EventDecorExtrasSelection> extrasSelections) {
		this.extrasSelections = extrasSelections;
	}

	public String getTxtEventRemarks() {
		return txtEventRemarks;
	}

	public void setTxtEventRemarks(String txtEventRemarks) {
		this.txtEventRemarks = txtEventRemarks;
	}

	public String getTxtDecoreRemarks() {
		return txtDecoreRemarks;
	}

	public void setTxtDecoreRemarks(String txtDecoreRemarks) {
		this.txtDecoreRemarks = txtDecoreRemarks;
	}

	public String getTxtCateringRemarks() {
		return txtCateringRemarks;
	}

	public void setTxtCateringRemarks(String txtCateringRemarks) {
		this.txtCateringRemarks = txtCateringRemarks;
	}

	public String getTxtExternalSupplierRemarks() {
		return txtExternalSupplierRemarks;
	}

	public void setTxtExternalSupplierRemarks(String txtExternalSupplierRemarks) {
		this.txtExternalSupplierRemarks = txtExternalSupplierRemarks;
	}

	public String getTxtEventExtrasRemarks() {
		return txtEventExtrasRemarks;
	}

	public void setTxtEventExtrasRemarks(String txtEventExtrasRemarks) {
		this.txtEventExtrasRemarks = txtEventExtrasRemarks;
	}

	public String getTxtVenueRemarks() {
		return txtVenueRemarks;
	}

	public void setTxtVenueRemarks(String txtVenueRemarks) {
		this.txtVenueRemarks = txtVenueRemarks;
	}

	public EventBudget getEventBudget() {
		return eventBudget;
	}

	public void setEventBudget(EventBudget eventBudget) {
		this.eventBudget = eventBudget;
	}

	public Integer getNumFormState() {
		return numFormState;
	}

	public void setNumFormState(Integer numFormState) {
		this.numFormState = numFormState;
	}

	public Boolean getIsEditAllowed() {
		return isEditAllowed;
	}

	public void setIsEditAllowed(Boolean isEditAllowed) {
		this.isEditAllowed = isEditAllowed;
	}

	public String getTxtContactPersonFirstName() {
		return txtContactPersonFirstName;
	}

	public void setTxtContactPersonFirstName(String txtContactPersonFirstName) {
		this.txtContactPersonFirstName = txtContactPersonFirstName;
	}

	public String getTxtContactPersonLastName() {
		return txtContactPersonLastName;
	}

	public void setTxtContactPersonLastName(String txtContactPersonLastName) {
		this.txtContactPersonLastName = txtContactPersonLastName;
	}

	public String getTxtContactPersonPhoneNo() {
		return txtContactPersonPhoneNo;
	}

	public void setTxtContactPersonPhoneNo(String txtContactPersonPhoneNo) {
		this.txtContactPersonPhoneNo = txtContactPersonPhoneNo;
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
