package com.zbs.de.model.dto;

public class DtoMenuFoodMaster {

	private Integer serMenuFoodId;
	private String txtMenuFoodCode;
	private String txtMenuFoodName;

	private Long serMenuItemId;
	private String txtCode;
	private String txtName;
	private String txtDescription;
	private Boolean blnIsSelectable = true;
	private Long serParentMenuItemId;

	private Boolean blnIsMainCourse;
	private Boolean blnIsAppetiser;
	private Boolean blnIsStarter;
	private Boolean blnIsSaladAndCondiment;
	private Boolean blnIsDessert;
	private Boolean blnIsDrink;
	private Boolean blnIsActive;

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

	public Boolean getBlnIsMainCourse() {
		return blnIsMainCourse;
	}

	public void setBlnIsMainCourse(Boolean blnIsMainCourse) {
		this.blnIsMainCourse = blnIsMainCourse;
	}

	public Boolean getBlnIsAppetiser() {
		return blnIsAppetiser;
	}

	public void setBlnIsAppetiser(Boolean blnIsAppetiser) {
		this.blnIsAppetiser = blnIsAppetiser;
	}

	public Boolean getBlnIsStarter() {
		return blnIsStarter;
	}

	public void setBlnIsStarter(Boolean blnIsStarter) {
		this.blnIsStarter = blnIsStarter;
	}

	public Boolean getBlnIsSaladAndCondiment() {
		return blnIsSaladAndCondiment;
	}

	public void setBlnIsSaladAndCondiment(Boolean blnIsSaladAndCondiment) {
		this.blnIsSaladAndCondiment = blnIsSaladAndCondiment;
	}

	public Boolean getBlnIsDessert() {
		return blnIsDessert;
	}

	public void setBlnIsDessert(Boolean blnIsDessert) {
		this.blnIsDessert = blnIsDessert;
	}

	public Boolean getBlnIsDrink() {
		return blnIsDrink;
	}

	public void setBlnIsDrink(Boolean blnIsDrink) {
		this.blnIsDrink = blnIsDrink;
	}

	public Boolean getBlnIsActive() {
		return blnIsActive;
	}

	public void setBlnIsActive(Boolean blnIsActive) {
		this.blnIsActive = blnIsActive;
	}

	public Long getSerMenuItemId() {
		return serMenuItemId;
	}

	public void setSerMenuItemId(Long serMenuItemId) {
		this.serMenuItemId = serMenuItemId;
	}

	public String getTxtCode() {
		return txtCode;
	}

	public void setTxtCode(String txtCode) {
		this.txtCode = txtCode;
	}

	public String getTxtName() {
		return txtName;
	}

	public void setTxtName(String txtName) {
		this.txtName = txtName;
	}

	public String getTxtDescription() {
		return txtDescription;
	}

	public void setTxtDescription(String txtDescription) {
		this.txtDescription = txtDescription;
	}

	public Boolean getBlnIsSelectable() {
		return blnIsSelectable;
	}

	public void setBlnIsSelectable(Boolean blnIsSelectable) {
		this.blnIsSelectable = blnIsSelectable;
	}

	public Long getSerParentMenuItemId() {
		return serParentMenuItemId;
	}

	public void setSerParentMenuItemId(Long serParentMenuItemId) {
		this.serParentMenuItemId = serParentMenuItemId;
	}

}
