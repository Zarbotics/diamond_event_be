package com.zbs.de.model;

import jakarta.persistence.Column;
import jakarta.persistence.Id;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

import org.hibernate.annotations.DynamicInsert;

@Entity
@DynamicInsert
@Data
@Getter
@Setter
@Table(name = "customer_master")
@NamedQuery(name = "CustomerMaster.findAll", query = "SELECT a FROM CustomerMaster a")
public class CustomerMaster extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_cust_id")
	private Integer serCustId;

	@Column(name = "txt_address_1", columnDefinition = "VARCHAR(255)")
	private String txtAddress1;

	@Column(name = "txt_address_2", columnDefinition = "VARCHAR(255)")
	private String txtAddress2;

	@Column(name = "ADDRESS3", columnDefinition = "VARCHAR(255)")
	private String address3;

	@Column(name = "txt_cust_name")
	private String txtCustName;

	@Column(name = "txt_phone_number_1")
	private String txt_phone_number_1;

	@Column(name = "txt_phone_number_3")
	private String txt_phone_number_2;

	@Column(name = "txt_email")
	private String txtEmail;

	@Column(name = "COMMENTS", columnDefinition = "TEXT DEFAULT NULL")
	private String comments;

	@Column(name = "num_longitude")
	private Double num_longitude;

	@Column(name = "num_latitude")
	private Double numLatitude;

	@Column(name = "txt_gmap_url")
	private String txtGMapUrl;

	@Column(name = "txt_country_code")
	private String txtCountryCode;

	@Column(name = "txt_street_name")
	private String txtStreetName;

	@Column(name = "txt_building_name")
	private String txtBuildingName;

	@Column(name = "txt_building_number")
	private String txtBuildingNumber;

	@Column(name = "txt_postal_code")
	private String txtPostalCode;

	@Column(name = "txt_district")
	private String txtDistrict;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_country_id")
	private CountryMaster countryMaster;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_city_id")
	private CityMaster cityMaster;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_state_id")
	private StateMaster stateMaster;

	public Integer getSerCustId() {
		return serCustId;
	}

	public void setSerCustId(Integer serCustId) {
		this.serCustId = serCustId;
	}

	public String getTxtAddress1() {
		return txtAddress1;
	}

	public void setTxtAddress1(String txtAddress1) {
		this.txtAddress1 = txtAddress1;
	}

	public String getTxtAddress2() {
		return txtAddress2;
		
	}

	public void setTxtAddress2(String txtAddress2) {
		this.txtAddress2 = txtAddress2;
	}

	public String getAddress3() {
		return address3;
	}

	public void setAddress3(String address3) {
		this.address3 = address3;
	}

	public String getTxtCustName() {
		return txtCustName;
	}

	public void setTxtCustName(String txtCustName) {
		this.txtCustName = txtCustName;
	}

	public String getTxt_phone_number_1() {
		return txt_phone_number_1;
	}

	public void setTxt_phone_number_1(String txt_phone_number_1) {
		this.txt_phone_number_1 = txt_phone_number_1;
	}

	public String getTxt_phone_number_2() {
		return txt_phone_number_2;
	}

	public void setTxt_phone_number_2(String txt_phone_number_2) {
		this.txt_phone_number_2 = txt_phone_number_2;
	}

	public String getTxtEmail() {
		return txtEmail;
	}

	public void setTxtEmail(String txtEmail) {
		this.txtEmail = txtEmail;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public Double getNum_longitude() {
		return num_longitude;
	}

	public void setNum_longitude(Double num_longitude) {
		this.num_longitude = num_longitude;
	}

	public Double getNumLatitude() {
		return numLatitude;
	}

	public void setNumLatitude(Double numLatitude) {
		this.numLatitude = numLatitude;
	}

	public String getTxtGMapUrl() {
		return txtGMapUrl;
	}

	public void setTxtGMapUrl(String txtGMapUrl) {
		this.txtGMapUrl = txtGMapUrl;
	}

	public String getTxtCountryCode() {
		return txtCountryCode;
	}

	public void setTxtCountryCode(String txtCountryCode) {
		this.txtCountryCode = txtCountryCode;
	}

	public String getTxtStreetName() {
		return txtStreetName;
	}

	public void setTxtStreetName(String txtStreetName) {
		this.txtStreetName = txtStreetName;
	}

	public String getTxtBuildingName() {
		return txtBuildingName;
	}

	public void setTxtBuildingName(String txtBuildingName) {
		this.txtBuildingName = txtBuildingName;
	}

	public String getTxtBuildingNumber() {
		return txtBuildingNumber;
	}

	public void setTxtBuildingNumber(String txtBuildingNumber) {
		this.txtBuildingNumber = txtBuildingNumber;
	}

	public String getTxtPostalCode() {
		return txtPostalCode;
	}

	public void setTxtPostalCode(String txtPostalCode) {
		this.txtPostalCode = txtPostalCode;
	}

	public String getTxtDistrict() {
		return txtDistrict;
	}

	public void setTxtDistrict(String txtDistrict) {
		this.txtDistrict = txtDistrict;
	}

	public CountryMaster getCountryMaster() {
		return countryMaster;
	}

	public void setCountryMaster(CountryMaster countryMaster) {
		this.countryMaster = countryMaster;
	}

	public CityMaster getCityMaster() {
		return cityMaster;
	}

	public void setCityMaster(CityMaster cityMaster) {
		this.cityMaster = cityMaster;
	}

	public StateMaster getStateMaster() {
		return stateMaster;
	}

	public void setStateMaster(StateMaster stateMaster) {
		this.stateMaster = stateMaster;
	}

}
