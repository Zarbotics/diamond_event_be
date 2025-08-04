package com.zbs.de.model.dto;

public class DtoCateringDeliveryItemDetail {
	private Integer serCateringDeliveryDetailId;
	private String txtCateringDeliveryDetailCode;
	private Integer numQuantity;
	private String txtNotes;

	private Integer serMenuFoodId;
	private String txtMenuFoodCode;
	private String txtMenuFoodName;

	public Integer getSerCateringDeliveryDetailId() {
		return serCateringDeliveryDetailId;
	}

	public void setSerCateringDeliveryDetailId(Integer serCateringDeliveryDetailId) {
		this.serCateringDeliveryDetailId = serCateringDeliveryDetailId;
	}

	public String getTxtCateringDeliveryDetailCode() {
		return txtCateringDeliveryDetailCode;
	}

	public void setTxtCateringDeliveryDetailCode(String txtCateringDeliveryDetailCode) {
		this.txtCateringDeliveryDetailCode = txtCateringDeliveryDetailCode;
	}

	public Integer getNumQuantity() {
		return numQuantity;
	}

	public void setNumQuantity(Integer numQuantity) {
		this.numQuantity = numQuantity;
	}

	public String getTxtNotes() {
		return txtNotes;
	}

	public void setTxtNotes(String txtNotes) {
		this.txtNotes = txtNotes;
	}

	public Integer getSerMenuFoodId() {
		return serMenuFoodId;
	}

	public void setSerMenuFoodId(Integer serMenuFoodId) {
		this.serMenuFoodId = serMenuFoodId;
	}

	public String getTxtMenuFoodCode() {
		return txtMenuFoodCode;
	}

	public void setTxtMenuFoodCode(String txtMenuFoodCode) {
		this.txtMenuFoodCode = txtMenuFoodCode;
	}

	public String getTxtMenuFoodName() {
		return txtMenuFoodName;
	}

	public void setTxtMenuFoodName(String txtMenuFoodName) {
		this.txtMenuFoodName = txtMenuFoodName;
	}

}
