package com.zbs.de.model.dto;

public class DtoCustomerMasterDropDown {
	private Integer serCustId;
	private String txtCustCode;
	private String txtFirstName;
	private String txtLastName;
	private String txtCustName;
	private String txt_phone_number_1;
	private String txtEmail;

	public DtoCustomerMasterDropDown() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DtoCustomerMasterDropDown(Integer serCustId, String txtCustCode, String txtFirstName, String txtLastName,
			String txtCustName, String txt_phone_number_1, String txtEmail) {
		super();
		this.serCustId = serCustId;
		this.txtCustCode = txtCustCode;
		this.txtFirstName = txtFirstName;
		this.txtLastName = txtLastName;
		this.txtCustName = txtCustName;
		this.txt_phone_number_1 = txt_phone_number_1;
		this.txtEmail = txtEmail;
	}

	public Integer getSerCustId() {
		return serCustId;
	}

	public void setSerCustId(Integer serCustId) {
		this.serCustId = serCustId;
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

	public String getTxtEmail() {
		return txtEmail;
	}

	public void setTxtEmail(String txtEmail) {
		this.txtEmail = txtEmail;
	}

}
