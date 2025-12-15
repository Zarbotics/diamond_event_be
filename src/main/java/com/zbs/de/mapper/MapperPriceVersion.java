package com.zbs.de.mapper;

import com.zbs.de.model.PriceVersion;
import com.zbs.de.model.dto.DtoPriceVersion;
import com.zbs.de.util.UtilDateAndTime;
import com.zbs.de.util.enums.EnmPriceVersionStatus;

public class MapperPriceVersion {

	public static DtoPriceVersion toDto(PriceVersion e) {
		if (e == null)
			return null;
		DtoPriceVersion d = new DtoPriceVersion();
		d.setSerPriceVersionId(e.getSerPriceVersionId());
		d.setTxtName(e.getTxtName());
		if (e.getDteEffectiveFrom() != null) {
			d.setDteEffectiveFrom(UtilDateAndTime.mmddyyyyDateToString(e.getDteEffectiveFrom()));
		}
		if (e.getDteEffectiveTo() != null) {
			d.setDteEffectiveTo(UtilDateAndTime.mmddyyyyDateToString(e.getDteEffectiveTo()));
		}
		d.setTxtStatus(e.getPriceVersionStatus() != null ? e.getPriceVersionStatus().toString() : null);
		return d;
	}

	public static PriceVersion toEntity(DtoPriceVersion d) {
		if (d == null)
			return null;

		PriceVersion e = new PriceVersion();
		e.setSerPriceVersionId(d.getSerPriceVersionId());
		e.setTxtName(d.getTxtName());
		if (d.getDteEffectiveFrom() != null) {
			e.setDteEffectiveFrom(UtilDateAndTime.ddMMyyyyDashedStringToDate(d.getDteEffectiveFrom()));
		}
		if (d.getDteEffectiveTo() != null) {
			e.setDteEffectiveTo(UtilDateAndTime.ddMMyyyyDashedStringToDate(d.getDteEffectiveTo()));
		}
		EnmPriceVersionStatus s = EnmPriceVersionStatus.of(d.getTxtStatus());
		e.setPriceVersionStatus(s == null ? null : s);

		return e;
	}
}
