package com.zbs.de.service;

import java.util.List;

import com.zbs.de.model.dto.DtoCustomerMaster;
import com.zbs.de.util.ResponseMessage;

public interface ServiceCustomerMaster {
	List<DtoCustomerMaster> getAllData();
	ResponseMessage saveAndUpdate(DtoCustomerMaster dtoCustomerMaster);
	ResponseMessage getById(Integer id);
	
}
