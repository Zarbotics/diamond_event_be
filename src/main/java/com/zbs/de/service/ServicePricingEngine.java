package com.zbs.de.service;

import com.zbs.de.model.dto.DtoPricePreviewRequest;

public interface ServicePricingEngine {
	DtoPricePreviewRequest preview(DtoPricePreviewRequest request);
}
