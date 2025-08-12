package com.zbs.de.model;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.annotations.DynamicInsert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@DynamicInsert
@Data
@Getter
@Setter
@Table(name = "notification_master")
@NamedQuery(name = "NotificationMaster.findAll", query = "SELECT a FROM NotificationMaster a")
public class NotificationMaster extends BaseEntity implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// the user who will receive the notification
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_user_id", referencedColumnName = "ser_user_id", nullable = false)
	private UserMaster userMaster;

	@Column(name = "txt_title", nullable = false)
	private String txtTitle;

	@Column(name = "txt_message", length = 2000)
	private String txtMessage;

	// optional link the frontend can navigate to (e.g. /events/123)
	@Column(name = "txt_target_url")
	private String txtTargetUrl;

	// notification type (INFO, WARNING, PAYMENT_REMINDER, EVENT_REGISTERED, etc.)
	@Column(name = "notif_type")
	private String txtType;

	@Column(name = "bln_is_read")
	private Boolean blnIsRead = false;

	@Column(name = "created_at", nullable = false)
	private Date createdAt = new Date();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public UserMaster getUserMaster() {
		return userMaster;
	}

	public void setUserMaster(UserMaster userMaster) {
		this.userMaster = userMaster;
	}

	public String getTxtTitle() {
		return txtTitle;
	}

	public void setTxtTitle(String txtTitle) {
		this.txtTitle = txtTitle;
	}

	public String getTxtMessage() {
		return txtMessage;
	}

	public void setTxtMessage(String txtMessage) {
		this.txtMessage = txtMessage;
	}

	public String getTxtTargetUrl() {
		return txtTargetUrl;
	}

	public void setTxtTargetUrl(String txtTargetUrl) {
		this.txtTargetUrl = txtTargetUrl;
	}

	public String getTxtType() {
		return txtType;
	}

	public void setTxtType(String txtType) {
		this.txtType = txtType;
	}

	public Boolean getBlnIsRead() {
		return blnIsRead;
	}

	public void setBlnIsRead(Boolean blnIsRead) {
		this.blnIsRead = blnIsRead;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

}
