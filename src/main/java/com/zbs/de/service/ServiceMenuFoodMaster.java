package com.zbs.de.service;

import java.util.List;

import com.zbs.de.model.dto.DtoMenuFoodMaster;
import com.zbs.de.util.ResponseMessage;

public interface ServiceMenuFoodMaster {
	
	List<DtoMenuFoodMaster> getAllData();

	ResponseMessage saveAndUpdate(DtoMenuFoodMaster dto);

	ResponseMessage getById(Integer id);
}
