package com.zbs.de.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.io.Serializable;

import org.hibernate.annotations.DynamicInsert;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@DynamicInsert
@Data
@Table(name = "vendor_master")
@NamedQuery(name = "VendorMaster.findAll", query = "SELECT s FROM VendorMaster s")
public class VendorMaster extends BaseEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_vendor_id")
	private Integer serVendorId;

	@Column(name = "txt_vendor_code")
	private String txtVendorCode;

	@Column(name = "txt_vendor_name")
	private String txtVendorName;

	@Column(name = "enm_vendor_type")
	private String enmVendorType;

	@Column(name = "txt_address")
	private String txtAddress;

	@Column(name = "txt_phone_number")
	private String txtPhoneNumber;

	public Integer getSerVendorId() {
		return serVendorId;
	}

	public void setSerVendorId(Integer serVendorId) {
		this.serVendorId = serVendorId;
	}

	public String getTxtVendorCode() {
		return txtVendorCode;
	}

	public void setTxtVendorCode(String txtVendorCode) {
		this.txtVendorCode = txtVendorCode;
	}

	public String getTxtVendorName() {
		return txtVendorName;
	}

	public void setTxtVendorName(String txtVendorName) {
		this.txtVendorName = txtVendorName;
	}

	public String getEnmVendorType() {
		return enmVendorType;
	}

	public void setEnmVendorType(String enmVendorType) {
		this.enmVendorType = enmVendorType;
	}

	public String getTxtAddress() {
		return txtAddress;
	}

	public void setTxtAddress(String txtAddress) {
		this.txtAddress = txtAddress;
	}

	public String getTxtPhoneNumber() {
		return txtPhoneNumber;
	}

	public void setTxtPhoneNumber(String txtPhoneNumber) {
		this.txtPhoneNumber = txtPhoneNumber;
	}

}
