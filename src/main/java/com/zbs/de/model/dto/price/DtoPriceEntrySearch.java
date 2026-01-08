package com.zbs.de.model.dto.price;

public class DtoPriceEntrySearch {
	private Long serPriceVersionId;
	private String txtRole;
	private String txtType;
	private String txtScope;
	private Boolean blnIncludeMenuInfo;

	public Long getSerPriceVersionId() {
		return serPriceVersionId;
	}

	public void setSerPriceVersionId(Long serPriceVersionId) {
		this.serPriceVersionId = serPriceVersionId;
	}

	public String getTxtRole() {
		return txtRole;
	}

	public void setTxtRole(String txtRole) {
		this.txtRole = txtRole;
	}

	public String getTxtType() {
		return txtType;
	}

	public void setTxtType(String txtType) {
		this.txtType = txtType;
	}

	public String getTxtScope() {
		return txtScope;
	}

	public void setTxtScope(String txtScope) {
		this.txtScope = txtScope;
	}

	public Boolean getBlnIncludeMenuInfo() {
		return blnIncludeMenuInfo;
	}

	public void setBlnIncludeMenuInfo(Boolean blnIncludeMenuInfo) {
		this.blnIncludeMenuInfo = blnIncludeMenuInfo;
	}

}
