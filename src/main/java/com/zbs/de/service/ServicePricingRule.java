package com.zbs.de.service;

import java.util.List;

import com.zbs.de.model.dto.DtoPricingRule;

public interface ServicePricingRule {
	DtoPricingRule create(DtoPricingRule dto);

	DtoPricingRule update(Long id, DtoPricingRule dto);

	void delete(Long id);

	List<DtoPricingRule> listByVersion(Long priceVersionId);
}
