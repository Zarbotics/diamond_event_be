package com.zbs.de.mapper;

import com.zbs.de.model.PriceEntry;
import com.zbs.de.model.PriceVersion;
import com.zbs.de.model.dto.DtoPriceEntry;
import com.zbs.de.util.enums.EnmApplyScope;
import com.zbs.de.util.enums.EnmItineraryUnitType;
import com.zbs.de.util.enums.EnmPriceCalculationMethod;

import java.util.HashMap;

public class MapperPriceEntry {

	public static DtoPriceEntry toDto(PriceEntry e) {
		if (e == null)
			return null;
		DtoPriceEntry d = new DtoPriceEntry();
		d.setSerPriceEntryId(e.getSerPriceEntryId());
		d.setPriceVersionId(e.getPriceVersion() == null ? null : e.getPriceVersion().getSerPriceVersionId());
		d.setTxtApplyScope(e.getApplyScope() != null ? e.getApplyScope().name() : null);
		d.setNumTargetId(e.getNumTargetId());
		d.setNumPrice(e.getNumPrice());
		d.setTxtCurrency(e.getTxtCurrency());
		d.setTxtUnit(e.getUnit() != null ? e.getUnit().name() : null);
		d.setTxtCalculationMethod(e.getCalculationMethod() != null ? e.getCalculationMethod().name() : null);
		d.setNumMaxQuantity(e.getNumMaxQuantity());
		d.setNumMinQuantity(e.getNumMinQuantity());
		if (e.getMetadata() != null)
			d.setMetadata(new HashMap<>(e.getMetadata()));
		return d;
	}

	public static PriceEntry toEntity(DtoPriceEntry d) {
		if (d == null)
			return null;

		PriceEntry e = new PriceEntry();
		e.setSerPriceEntryId(d.getSerPriceEntryId());
		if (d.getPriceVersionId() != null) {
			PriceVersion pv = new PriceVersion();
			pv.setSerPriceVersionId(d.getPriceVersionId());
			e.setPriceVersion(pv);
		}
		e.setApplyScope(EnmApplyScope.of(d.getTxtApplyScope()));
		e.setNumTargetId(d.getNumTargetId());
		e.setNumPrice(d.getNumPrice());
		e.setTxtCurrency(d.getTxtCurrency());
		e.setUnit(EnmItineraryUnitType.of(d.getTxtUnit()));
		e.setCalculationMethod(EnmPriceCalculationMethod.of(d.getTxtCalculationMethod()));
		e.setNumMaxQuantity(d.getNumMaxQuantity());
		e.setNumMinQuantity(d.getNumMinQuantity());
		if (d.getMetadata() != null)
			e.setMetadata(new HashMap<>(d.getMetadata()));

		return e;
	}

}
