package com.zbs.de.model.dto;

public class DtoMenuItemIngredient {
	private Long ingredientId;
	private Double numQuantity;
	private String txtUOM;

	public DtoMenuItemIngredient() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DtoMenuItemIngredient(Long ingredientId, Double numQuantity, String txtUOM) {
		super();
		this.ingredientId = ingredientId;
		this.numQuantity = numQuantity;
		this.txtUOM = txtUOM;
	}

	public Long getIngredientId() {
		return ingredientId;
	}

	public void setIngredientId(Long ingredientId) {
		this.ingredientId = ingredientId;
	}

	public Double getNumQuantity() {
		return numQuantity;
	}

	public void setNumQuantity(Double numQuantity) {
		this.numQuantity = numQuantity;
	}

	public String getTxtUOM() {
		return txtUOM;
	}

	public void setTxtUOM(String txtUOM) {
		this.txtUOM = txtUOM;
	}

}
