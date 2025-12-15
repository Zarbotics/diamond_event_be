package com.zbs.de.util.enums;

public enum EnmPriceCalculationMethod {
	DIRECT, // price * qty
	MIN_OF, // min(selected)
	MAX_OF, // max(selected)
	SUM, // sum of specific items
	FORMULA; // requires rule engine

	public static EnmPriceCalculationMethod of(String s) {
		if (s == null)
			return null;
		try {
			return EnmPriceCalculationMethod.valueOf(s.toUpperCase());
		} catch (Exception ex) {
			return null;
		}
	}
}
