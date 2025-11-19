package com.zbs.de.service;

import com.zbs.de.model.dto.DtoCateringDeliveryBooking;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;

public interface ServiceCateringDeliveryBooking {
	DtoResult saveOrUpdate(DtoCateringDeliveryBooking dto);

	DtoResult softDelete(Integer id);

	DtoResult getByPK(Integer id);

	DtoResult getAll();

	DtoResult search(String keyword);

	String generateAutoCode();
	
	DtoResult getByCustId(DtoSearch dtoSearch);
	
	DtoResult saveOrUpdateCateringAdminPortal(DtoCateringDeliveryBooking dto);
}
