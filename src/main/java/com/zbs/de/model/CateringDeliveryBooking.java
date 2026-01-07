package com.zbs.de.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.hibernate.annotations.DynamicInsert;

@Entity
@DynamicInsert
@Data
@Table(name = "catering_delivery_booking")
@NamedQuery(name = "CateringDeliveryBooking.findAll", query = "SELECT a FROM CateringDeliveryBooking a")
public class CateringDeliveryBooking extends BaseEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_delivery_booking_id")
	private Integer serDeliveryBookingId;

	@Column(name = "txt_delivery_booking_code")
	private String txtDeliveryBookingCode;

	@Column(name = "dte_delivery_date")
	private Date dteDeliveryDate;

	@Column(name = "txt_delivery_time")
	private String txtDeliveryTime;

	@Column(name = "txt_delivery_location", length = 1000)
	private String txtDeliveryLocation;

	@Column(name = "txt_special_instructions", length = 1000)
	private String txtSpecialInstructions;

	@Column(name = "bln_request_meeting")
	private Boolean blnRequestMeeting = false;

	@Column(name = "txt_booking_status") // e.g., Pending, Quoted, Confirmed
	private String txtBookingStatus;

	@Column(name = "bln_booking_status")
	private Boolean blnBookingStatus;

	@Column(name = "txt_remarks")
	private String txtRemarks;

	@Column(name = "is_edit_allowed")
	private Boolean isEditAllowed = true;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_cust_id")
	private CustomerMaster customerMaster;

	@ManyToOne
	@JoinColumn(name = "ser_event_type_id")
	private EventType eventType;

	@OneToMany(mappedBy = "cateringDeliveryBooking", cascade = CascadeType.ALL)
	private List<CateringDeliveryItemDetail> cateringDeliveryItemDetails;

	public Integer getSerDeliveryBookingId() {
		return serDeliveryBookingId;
	}

	public void setSerDeliveryBookingId(Integer serDeliveryBookingId) {
		this.serDeliveryBookingId = serDeliveryBookingId;
	}

	public Date getDteDeliveryDate() {
		return dteDeliveryDate;
	}

	public void setDteDeliveryDate(Date dteDeliveryDate) {
		this.dteDeliveryDate = dteDeliveryDate;
	}

	public String getTxtDeliveryTime() {
		return txtDeliveryTime;
	}

	public void setTxtDeliveryTime(String txtDeliveryTime) {
		this.txtDeliveryTime = txtDeliveryTime;
	}

	public String getTxtDeliveryLocation() {
		return txtDeliveryLocation;
	}

	public void setTxtDeliveryLocation(String txtDeliveryLocation) {
		this.txtDeliveryLocation = txtDeliveryLocation;
	}

	public String getTxtSpecialInstructions() {
		return txtSpecialInstructions;
	}

	public void setTxtSpecialInstructions(String txtSpecialInstructions) {
		this.txtSpecialInstructions = txtSpecialInstructions;
	}

	public Boolean getBlnRequestMeeting() {
		return blnRequestMeeting;
	}

	public void setBlnRequestMeeting(Boolean blnRequestMeeting) {
		this.blnRequestMeeting = blnRequestMeeting;
	}

	public String getTxtBookingStatus() {
		return txtBookingStatus;
	}

	public void setTxtBookingStatus(String txtBookingStatus) {
		this.txtBookingStatus = txtBookingStatus;
	}

	public Boolean getBlnBookingStatus() {
		return blnBookingStatus;
	}

	public void setBlnBookingStatus(Boolean blnBookingStatus) {
		this.blnBookingStatus = blnBookingStatus;
	}

	public String getTxtRemarks() {
		return txtRemarks;
	}

	public void setTxtRemarks(String txtRemarks) {
		this.txtRemarks = txtRemarks;
	}

	public CustomerMaster getCustomerMaster() {
		return customerMaster;
	}

	public void setCustomerMaster(CustomerMaster customerMaster) {
		this.customerMaster = customerMaster;
	}

	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	public List<CateringDeliveryItemDetail> getCateringDeliveryItemDetails() {
		return cateringDeliveryItemDetails;
	}

	public void setCateringDeliveryItemDetails(List<CateringDeliveryItemDetail> cateringDeliveryItemDetails) {
		this.cateringDeliveryItemDetails = cateringDeliveryItemDetails;
	}

	public String getTxtDeliveryBookingCode() {
		return txtDeliveryBookingCode;
	}

	public void setTxtDeliveryBookingCode(String txtDeliveryBookingCode) {
		this.txtDeliveryBookingCode = txtDeliveryBookingCode;
	}

	public Boolean getIsEditAllowed() {
		return isEditAllowed;
	}

	public void setIsEditAllowed(Boolean isEditAllowed) {
		this.isEditAllowed = isEditAllowed;
	}

}