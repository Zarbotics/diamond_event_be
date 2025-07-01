package com.zbs.de.model.dto;

public class DtoDecorColorMaster {

	private Integer serDecorColorId;
	private String txtColorCode;
	private String txtColorName;
	private String txtColorHex;
	private Boolean blnIsActive;

	public Integer getSerDecorColorId() {
		return serDecorColorId;
	}

	public void setSerDecorColorId(Integer serDecorColorId) {
		this.serDecorColorId = serDecorColorId;
	}

	public String getTxtColorName() {
		return txtColorName;
	}

	public void setTxtColorName(String txtColorName) {
		this.txtColorName = txtColorName;
	}

	public String getTxtColorHex() {
		return txtColorHex;
	}

	public void setTxtColorHex(String txtColorHex) {
		this.txtColorHex = txtColorHex;
	}

	public Boolean getBlnIsActive() {
		return blnIsActive;
	}

	public void setBlnIsActive(Boolean blnIsActive) {
		this.blnIsActive = blnIsActive;
	}

	public String getTxtColorCode() {
		return txtColorCode;
	}

	public void setTxtColorCode(String txtColorCode) {
		this.txtColorCode = txtColorCode;
	}

}
