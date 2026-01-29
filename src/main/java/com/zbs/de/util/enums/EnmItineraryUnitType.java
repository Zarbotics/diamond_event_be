package com.zbs.de.util.enums;

public enum EnmItineraryUnitType {
	PER_GUEST,PER_TABLE, PER_ITEM, PER_PORTION, FLAT;

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
