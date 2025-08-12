package com.zbs.de.model.dto;

public class DtoNotificationMaster {
	private Long id;
	private Integer serUserId;
	private String txtUserName;
	private String txtTitle;
	private String txtMessage;

	private String txtTargetUrl;
	private String txtType;
	private Boolean blnIsRead = false;

	private String createdAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getSerUserId() {
		return serUserId;
	}

	public void setSerUserId(Integer serUserId) {
		this.serUserId = serUserId;
	}

	public String getTxtUserName() {
		return txtUserName;
	}

	public void setTxtUserName(String txtUserName) {
		this.txtUserName = txtUserName;
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

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

}
