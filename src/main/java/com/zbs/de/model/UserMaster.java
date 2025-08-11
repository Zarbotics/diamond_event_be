package com.zbs.de.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Getter
@Setter
@Table(name = "user_master")
public class UserMaster extends BaseEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_user_id")
	private Long serUserId;

	@Column(name = "google_id")
	private String txtGoogleId;

	@Column(name = "txt_email", unique = true, nullable = false)
	private String txtEmail;

	@Column(name = "txt_name")
	private String txtName;

	@Column(name = "txt_picture_url")
	private String txtPictureUrl;

	@Column(name = "bln_email_verified")
	private Boolean blnEmailVerified;

	@Column(name = "txt_first_name")
	private String txtFirstName;

	@Column(name = "txt_last_name")
	private String txtLastName;

	@Column(name = "txt_password")
	private String txtPassword;

	@Column(name = "txt_role")
	private String txtRole;

	public Long getSerUserId() {
		return serUserId;
	}

	public void setSerUserId(Long serUserId) {
		this.serUserId = serUserId;
	}

	public String getTxtGoogleId() {
		return txtGoogleId;
	}

	public void setTxtGoogleId(String txtGoogleId) {
		this.txtGoogleId = txtGoogleId;
	}

	public String getTxtEmail() {
		return txtEmail;
	}

	public void setTxtEmail(String txtEmail) {
		this.txtEmail = txtEmail;
	}

	public String getTxtName() {
		return txtName;
	}

	public void setTxtName(String txtName) {
		this.txtName = txtName;
	}

	public String getTxtPictureUrl() {
		return txtPictureUrl;
	}

	public void setTxtPictureUrl(String txtPictureUrl) {
		this.txtPictureUrl = txtPictureUrl;
	}

	public Boolean getBlnEmailVerified() {
		return blnEmailVerified;
	}

	public void setBlnEmailVerified(Boolean blnEmailVerified) {
		this.blnEmailVerified = blnEmailVerified;
	}

	public String getTxtRole() {
		return txtRole;
	}

	public void setTxtRole(String txtRole) {
		this.txtRole = txtRole;
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

	public String getTxtPassword() {
		return txtPassword;
	}

	public void setTxtPassword(String txtPassword) {
		this.txtPassword = txtPassword;
	}

	
}
