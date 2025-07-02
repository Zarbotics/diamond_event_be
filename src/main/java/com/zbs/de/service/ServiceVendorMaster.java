package com.zbs.de.service;

import java.util.List;

import com.zbs.de.model.VendorMaster;
import com.zbs.de.model.dto.DtoVendorMaster;
import com.zbs.de.util.ResponseMessage;

public interface ServiceVendorMaster {
	List<DtoVendorMaster> getAllData();

	ResponseMessage saveAndUpdate(DtoVendorMaster dto);

	ResponseMessage getById(Integer id);
	
	VendorMaster getByPK(Integer id);
}
