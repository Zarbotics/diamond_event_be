package com.zbs.de.service;

import java.util.List;

import com.zbs.de.model.dto.DtoPriceEntry;

public interface ServicePriceEntry {
	DtoPriceEntry create(DtoPriceEntry dto);

	DtoPriceEntry update(Long id, DtoPriceEntry dto);

	void delete(Long id);

	List<DtoPriceEntry> listByVersion(Long priceVersionId);
}
