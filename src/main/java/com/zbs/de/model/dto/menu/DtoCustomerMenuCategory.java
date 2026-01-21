package com.zbs.de.model.dto.menu;

import java.math.BigDecimal;
import java.util.List;

public class DtoCustomerMenuCategory {
	private Long categoryId;
	private String categoryName;
	private BigDecimal numPrice;
	private List<DtoCustomerMenuSubCategory> subCategories;

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

	public BigDecimal getNumPrice() {
		return numPrice;
	}

	public void setNumPrice(BigDecimal numPrice) {
		this.numPrice = numPrice;
	}

}
