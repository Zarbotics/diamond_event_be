package com.zbs.de.model.dto;

import java.util.Map;

public class DtoIngredient {
	private Long serIngredientId;
	private String txtName;
	private Map<String, Object> metadata;

	public DtoIngredient() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DtoIngredient(Long serIngredientId, String txtName, Map<String, Object> metadata) {
		super();
		this.serIngredientId = serIngredientId;
		this.txtName = txtName;
		this.metadata = metadata;
	}

	public Long getSerIngredientId() {
		return serIngredientId;
	}

	public void setSerIngredientId(Long serIngredientId) {
		this.serIngredientId = serIngredientId;
	}

	public String getTxtName() {
		return txtName;
	}

	public void setTxtName(String txtName) {
		this.txtName = txtName;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

}
