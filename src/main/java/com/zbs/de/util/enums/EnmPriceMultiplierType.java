package com.zbs.de.util.enums;

public enum EnmPriceMultiplierType {
	PER_GUEST, FLAT;

	public static EnmItineraryUnitType of(String s) {
		if (s == null)
			return null;
		try {
			return EnmItineraryUnitType.valueOf(s.toUpperCase());
		} catch (Exception ex) {
			return null;
		}
	}
}
