package com.zbs.de.util.enums;

public enum EnmPriceVersionStatus {
	DRAFT, PUBLISHED, RETIRED;

	public static EnmPriceVersionStatus of(String s) {
		if (s == null)
			return null;
		try {
			return EnmPriceVersionStatus.valueOf(s.toUpperCase());
		} catch (Exception ex) {
			return null;
		}
	}
}
