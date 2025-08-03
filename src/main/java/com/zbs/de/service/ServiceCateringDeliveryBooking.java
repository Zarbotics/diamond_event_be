package com.zbs.de.service;

import com.zbs.de.model.dto.DtoCateringDeliveryBooking;
import com.zbs.de.model.dto.DtoResult;

public interface ServiceCateringDeliveryBooking {
	DtoResult saveOrUpdate(DtoCateringDeliveryBooking dto);

	DtoResult softDelete(Integer id);

	DtoResult getByPK(Integer id);

	DtoResult getAll();

	DtoResult search(String keyword);

	String generateAutoCode();
}
