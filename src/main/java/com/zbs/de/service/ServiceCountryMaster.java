package com.zbs.de.service;

import com.zbs.de.model.dto.DtoCountryMaster;
import com.zbs.de.util.ResponseMessage;

public interface ServiceCountryMaster {

	ResponseMessage getAllData();

	ResponseMessage saveAndUpdate(DtoCountryMaster dto);

	ResponseMessage getById(Integer id);
}
