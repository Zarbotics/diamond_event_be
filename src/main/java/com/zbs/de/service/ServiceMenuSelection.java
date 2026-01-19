package com.zbs.de.service;

import java.util.List;

import com.zbs.de.model.dto.menu.DtoCustomerMenuCategory;

public interface ServiceMenuSelection {
	List<DtoCustomerMenuCategory> getCustomerMenu();
	List<DtoCustomerMenuCategory> getCustomerCateringMenu();
}