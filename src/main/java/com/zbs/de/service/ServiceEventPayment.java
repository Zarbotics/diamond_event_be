package com.zbs.de.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.zbs.de.model.dto.DtoPayment;
import com.zbs.de.model.dto.DtoResult;

public interface ServiceEventPayment {
	DtoResult savePayment(DtoPayment dtoPayment);

	DtoResult savePaymentWithFiles(DtoPayment dtoPayment, List<MultipartFile> files);

	List<DtoPayment> getPaymentsByBudgetId(Integer serEventBudgetId);

	DtoResult deletePayment(Integer serEventPaymentId);

	java.math.BigDecimal getTotalPaidByBudget(Integer serEventBudgetId);

	List<DtoPayment> getPaymentsByEventMasterId(Integer serEventMasterId);

	List<String> getAllPaymentStatuses();

	List<String> getAllPaymentMethods();
}
