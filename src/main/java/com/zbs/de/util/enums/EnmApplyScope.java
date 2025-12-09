package com.zbs.de.util.enums;

public enum EnmApplyScope {
	ITEM, ROLE, BUNDLE, COMBINATION, STATION, TYPE;
	
	//ApplyScope is part of PricingEngine, not MenuEngine.
	//ApplyScope tells the PriceEntry HOW pricing applies, not what the item is

	public static EnmApplyScope of(String s) {
		if (s == null)
			return null;
		try {
			return EnmApplyScope.valueOf(s.toUpperCase());
		} catch (Exception ex) {
			return null;
		}
	}
}
