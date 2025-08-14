package com.zbs.de.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.io.Serializable;
import java.util.List;

import org.hibernate.annotations.DynamicInsert;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@DynamicInsert
@Data
@Table(name = "venue_master")
@NamedQuery(name = "VenueMaster.findAll", query = "SELECT s FROM VenueMaster s")
public class VenueMaster extends BaseEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_venue_master_id")
	private Integer serVenueMasterId;

	@Column(name = "txt_venue_code")
	private String txtVenueCode;

	@Column(name = "txt_venue_name")
	private String txtVenueName;

	@Column(name = "txt_address")
	private String txtAddress;

	@Column(name = "txt_phone_number")
	private String txtPhoneNumber;

	@Column(name = "txt_email_address")
	private String txtEmailAddress;

	@Column(name = "txt_web_link")
	private String txtWebLink;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_city_master_id", nullable = false)
	private CityMaster cityMaster;

	@OneToMany(mappedBy = "venueMaster", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<VenueMasterDetail> venueMasterDetails;

	public Integer getSerVenueMasterId() {
		return serVenueMasterId;
	}

	public void setSerVenueMasterId(Integer serVenueMasterId) {
		this.serVenueMasterId = serVenueMasterId;
	}

	public String getTxtVenueCode() {
		return txtVenueCode;
	}

	public void setTxtVenueCode(String txtVenueCode) {
		this.txtVenueCode = txtVenueCode;
	}

	public String getTxtVenueName() {
		return txtVenueName;
	}

	public void setTxtVenueName(String txtVenueName) {
		this.txtVenueName = txtVenueName;
	}

	public String getTxtAddress() {
		return txtAddress;
	}

	public void setTxtAddress(String txtAddress) {
		this.txtAddress = txtAddress;
	}

	public CityMaster getCityMaster() {
		return cityMaster;
	}

	public void setCityMaster(CityMaster cityMaster) {
		this.cityMaster = cityMaster;
	}

	public List<VenueMasterDetail> getVenueMasterDetails() {
		return venueMasterDetails;
	}

	public void setVenueMasterDetails(List<VenueMasterDetail> venueMasterDetails) {
		this.venueMasterDetails = venueMasterDetails;
	}

	public String getTxtPhoneNumber() {
		return txtPhoneNumber;
	}

	public void setTxtPhoneNumber(String txtPhoneNumber) {
		this.txtPhoneNumber = txtPhoneNumber;
	}

	public String getTxtEmailAddress() {
		return txtEmailAddress;
	}

	public void setTxtEmailAddress(String txtEmailAddress) {
		this.txtEmailAddress = txtEmailAddress;
	}

	public String getTxtWebLink() {
		return txtWebLink;
	}

	public void setTxtWebLink(String txtWebLink) {
		this.txtWebLink = txtWebLink;
	}

}
