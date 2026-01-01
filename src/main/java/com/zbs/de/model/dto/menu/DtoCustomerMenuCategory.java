package com.zbs.de.model.dto.menu;

import java.util.List;

public class DtoCustomerMenuCategory {
	Long categoryId;
	String categoryName;
	List<DtoCustomerMenuSubCategory> subCategories;

	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public List<DtoCustomerMenuSubCategory> getSubCategories() {
		return subCategories;
	}

	public void setSubCategories(List<DtoCustomerMenuSubCategory> subCategories) {
		this.subCategories = subCategories;
	}

}
