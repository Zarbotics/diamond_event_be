package com.zbs.de.model;

import jakarta.persistence.Column;
import jakarta.persistence.Id;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.annotations.DynamicInsert;

import com.zbs.de.util.UtilDateAndTime;

@Entity
@DynamicInsert
@Data
@Getter
@Setter
@Table(name = "email_verification_token")
@NamedQuery(name = "EmailVerificationToken.findAll", query = "SELECT a FROM EmailVerificationToken a")
public class EmailVerificationToken extends BaseEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String token;

	@Column(nullable = false)
	private Date expiryDate;

	@OneToOne
	@JoinColumn(name = "ser_user_id", referencedColumnName = "ser_user_id")
	private UserMaster userMaster;

	@Column(name = "bln_is_expired")
	private Boolean blnIsExpired;

	public boolean isExpired() {
		return expiryDate.before(UtilDateAndTime.getCurrentDate());
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}

	public UserMaster getUserMaster() {
		return userMaster;
	}

	public void setUserMaster(UserMaster userMaster) {
		this.userMaster = userMaster;
	}

	public Boolean getBlnIsExpired() {
		return blnIsExpired;
	}

	public void setBlnIsExpired(Boolean blnIsExpired) {
		this.blnIsExpired = blnIsExpired;
	}

}
