package com.zbs.de.service;

import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoStateMaster;
import com.zbs.de.util.ResponseMessage;

public interface ServiceStateMaster {
	
	ResponseMessage getAllData();
	ResponseMessage saveAndUpdate(DtoStateMaster dto);
	ResponseMessage getById(Integer id);
	DtoResult deleteById(Integer id);
}