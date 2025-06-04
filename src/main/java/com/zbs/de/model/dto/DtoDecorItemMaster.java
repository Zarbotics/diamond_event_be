package com.zbs.de.model.dto;

import java.math.BigDecimal;

public class DtoDecorItemMaster {

	private Integer serDecorItemId;
	private String txtDecorCode;
	private String txtDecorName;
	private String txtDescription;
	private BigDecimal numPrice;
	private Boolean blnIsActive;

	public Integer getSerDecorItemId() {
		return serDecorItemId;
	}

	public void setSerDecorItemId(Integer serDecorItemId) {
		this.serDecorItemId = serDecorItemId;
	}

	public String getTxtDecorCode() {
		return txtDecorCode;
	}

	public void setTxtDecorCode(String txtDecorCode) {
		this.txtDecorCode = txtDecorCode;
	}

	public String getTxtDecorName() {
		return txtDecorName;
	}

	public void setTxtDecorName(String txtDecorName) {
		this.txtDecorName = txtDecorName;
	}

	public String getTxtDescription() {
		return txtDescription;
	}

	public void setTxtDescription(String txtDescription) {
		this.txtDescription = txtDescription;
	}

	public BigDecimal getNumPrice() {
		return numPrice;
	}

	public void setNumPrice(BigDecimal numPrice) {
		this.numPrice = numPrice;
	}

	public Boolean getBlnIsActive() {
		return blnIsActive;
	}

	public void setBlnIsActive(Boolean blnIsActive) {
		this.blnIsActive = blnIsActive;
	}

}
