package com.zbs.de.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.zbs.de.model.dto.DtoPayment;
import com.zbs.de.model.dto.DtoResult;

public interface ServiceCateringPayment {
	DtoResult savePayment(DtoPayment dtoPayment);

	DtoResult savePaymentWithFiles(DtoPayment dtoPayment, List<MultipartFile> files);

	DtoResult deletePayment(Integer serEventPaymentId);

	List<DtoPayment> getPaymentsBySerDeliveryBookingId(Integer serDeliveryBookingId);
}
