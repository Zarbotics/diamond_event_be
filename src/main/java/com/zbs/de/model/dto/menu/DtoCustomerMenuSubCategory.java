package com.zbs.de.model.dto.menu;

import java.util.List;

import com.zbs.de.model.dto.DtoMenuComponentRequest;
import com.zbs.de.model.dto.DtoMenuItem;

public class DtoCustomerMenuSubCategory {
	Long subCategoryId;
	String subCategoryName;
	List<DtoMenuItem> items;
	List<DtoMenuComponentRequest> compositeItems;

	public Long getSubCategoryId() {
		return subCategoryId;
	}

	public void setSubCategoryId(Long subCategoryId) {
		this.subCategoryId = subCategoryId;
	}

	public String getSubCategoryName() {
		return subCategoryName;
	}

	public void setSubCategoryName(String subCategoryName) {
		this.subCategoryName = subCategoryName;
	}

	public List<DtoMenuItem> getItems() {
		return items;
	}

	public void setItems(List<DtoMenuItem> items) {
		this.items = items;
	}

	public List<DtoMenuComponentRequest> getCompositeItems() {
		return compositeItems;
	}

	public void setCompositeItems(List<DtoMenuComponentRequest> compositeItems) {
		this.compositeItems = compositeItems;
	}

}
