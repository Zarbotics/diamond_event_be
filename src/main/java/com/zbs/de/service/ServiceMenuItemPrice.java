package com.zbs.de.service;

import java.util.Date;
import java.util.List;

import com.zbs.de.model.dto.DtoBulkPriceRequest;
import com.zbs.de.model.dto.DtoMenuItemPrice;
import com.zbs.de.model.dto.DtoPriceCalculationRequest;
import com.zbs.de.model.dto.DtoPriceCalculationResult;
import com.zbs.de.model.dto.DtoResult;

public interface ServiceMenuItemPrice {

	// CRUD Operations
	DtoResult create(DtoMenuItemPrice dto);

	DtoResult update(DtoMenuItemPrice dto);

	DtoResult delete(Long id);

	DtoResult getById(Long id);

	DtoResult getAll();

	DtoResult getAllActive();

	// Query Operations
	DtoResult getByMenuItem(Long menuItemId);

	DtoResult getByPriceVersion(Long priceVersionId);

	DtoResult getByMenuItemAndVersion(Long menuItemId, Long priceVersionId);

	DtoResult getDefaultByMenuItem(Long menuItemId);

	DtoResult getApplicablePrice(Long menuItemId, Integer guestCount, Date date, Long priceVersionId);

	// Bulk Operations
	DtoResult bulkSetPrices(DtoBulkPriceRequest request);

	DtoResult copyPrices(Long sourceVersionId, Long targetVersionId);

	DtoResult updatePricesStatus(List<Long> priceIds, Boolean isActive);

	// Price Calculation
	DtoPriceCalculationResult calculatePrice(DtoPriceCalculationRequest request);

	DtoResult calculatePriceForMenuItem(Long menuItemId, Integer guestCount, Integer quantity, Date date);

	// Validation
	DtoResult validatePrice(DtoMenuItemPrice dto);
}