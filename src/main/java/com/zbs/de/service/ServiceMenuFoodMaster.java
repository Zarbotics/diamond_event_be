package com.zbs.de.service;

import java.util.List;
import java.util.Map;

import com.zbs.de.model.MenuFoodMaster;
import com.zbs.de.model.dto.DtoMenuFoodMaster;
import com.zbs.de.util.ResponseMessage;

public interface ServiceMenuFoodMaster {

	List<DtoMenuFoodMaster> getAllData();

	ResponseMessage saveAndUpdate(DtoMenuFoodMaster dto);

	ResponseMessage getById(Integer id);

	MenuFoodMaster getByPK(Integer id);

	ResponseMessage getByType(String tye);

	Map<String, List<DtoMenuFoodMaster>> getAllFoodGroupedByType();
}
