package com.zbs.de.mapper;

import com.zbs.de.model.PriceVersion;
import com.zbs.de.model.PricingRule;
import com.zbs.de.model.dto.DtoPricingRule;

import java.util.HashMap;

public final class MapperPricingRule {

	private MapperPricingRule() {
	}

	public static DtoPricingRule toDto(PricingRule e) {
		if (e == null)
			return null;
		DtoPricingRule d = new DtoPricingRule();
		d.setSerPricingRuleId(e.getSerPricingRuleId());
		d.setPriceVersionId(e.getPriceVersion() != null ? e.getPriceVersion().getSerPriceVersionId() : null);
		if (e.getExpression() != null)
			d.setExpression(new HashMap<>(e.getExpression()));
		d.setNumPriority(e.getNumPriority());
		d.setBlnStackable(e.getBlnStackable());
		d.setTxtLabel(e.getTxtLabel());
		return d;
	}

	public static PricingRule toEntity(DtoPricingRule d) {
		if (d == null)
			return null;
		PricingRule e = new PricingRule();

		e.setSerPricingRuleId(d.getSerPricingRuleId());
		if (d.getPriceVersionId() != null) {
			PriceVersion pv = new PriceVersion();
			pv.setSerPriceVersionId(d.getPriceVersionId());
			e.setPriceVersion(pv);
		}
		if (d.getExpression() != null)
			e.setExpression(new HashMap<>(d.getExpression()));
		e.setNumPriority(d.getNumPriority());
		e.setBlnStackable(d.getBlnStackable());
		e.setTxtLabel(d.getTxtLabel());

		return e;
	}
}
