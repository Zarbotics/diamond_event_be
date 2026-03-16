package com.zbs.de.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DtoCustomerMaster extends DtoBaseEntity {

	private Integer serCustId;
	private String txtCustCode;
	private String txtFirstName;
	private String txtLastName;
	private String txtAddress1;
	private String txtAddress2;
	private String address3;
	private String txtCustName;
	private String txt_phone_number_1;
	private String txt_phone_number_2;
	private String txtEmail;
	private String comments;
	private Double num_longitude;
	private Double numLatitude;
	private String txtGMapUrl;
	private String txtStreetName;
	private String txtBuildingName;
	private String txtBuildingNumber;
	private String txtPostalCode;
	private String txtDistrict;
	private Integer serCountryId;
	private String txtCountryCode;
	private String txtCountryName;
	private Integer serCityId;
	private String txtCityCode;
	private String txtCityName;
	private Integer serStateId;
	private String txtStateCode;
	private String txtStateName;

	private Boolean blnIsActive;

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

	public Integer getSerCountryId() {
		return serCountryId;
	}

	public void setSerCountryId(Integer serCountryId) {
		this.serCountryId = serCountryId;
	}

	public String getTxtCountryCode() {
		return txtCountryCode;
	}

	public void setTxtCountryCode(String txtCountryCode) {
		this.txtCountryCode = txtCountryCode;
	}

	public String getTxtCountryName() {
		return txtCountryName;
	}

	public void setTxtCountryName(String txtCountryName) {
		this.txtCountryName = txtCountryName;
	}

	public Integer getSerCityId() {
		return serCityId;
	}

	public void setSerCityId(Integer serCityId) {
		this.serCityId = serCityId;
	}

	public String getTxtCityCode() {
		return txtCityCode;
	}

	public void setTxtCityCode(String txtCityCode) {
		this.txtCityCode = txtCityCode;
	}

	public String getTxtCityName() {
		return txtCityName;
	}

	public void setTxtCityName(String txtCityName) {
		this.txtCityName = txtCityName;
	}

	public Integer getSerStateId() {
		return serStateId;
	}

	public void setSerStateId(Integer serStateId) {
		this.serStateId = serStateId;
	}

	public String getTxtStateCode() {
		return txtStateCode;
	}

	public void setTxtStateCode(String txtStateCode) {
		this.txtStateCode = txtStateCode;
	}

	public String getTxtStateName() {
		return txtStateName;
	}

	public void setTxtStateName(String txtStateName) {
		this.txtStateName = txtStateName;
	}

	public Boolean getBlnIsActive() {
		return blnIsActive;
	}

	public void setBlnIsActive(Boolean blnIsActive) {
		this.blnIsActive = blnIsActive;
	}

	public String getTxtCustCode() {
		return txtCustCode;
	}

	public void setTxtCustCode(String txtCustCode) {
		this.txtCustCode = txtCustCode;
	}

	public String getTxtFirstName() {
		return txtFirstName;
	}

	public void setTxtFirstName(String txtFirstName) {
		this.txtFirstName = txtFirstName;
	}

	public String getTxtLastName() {
		return txtLastName;
	}

	public void setTxtLastName(String txtLastName) {
		this.txtLastName = txtLastName;
	}

}