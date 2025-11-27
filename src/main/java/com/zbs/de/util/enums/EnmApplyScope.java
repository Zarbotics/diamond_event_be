package com.zbs.de.util.enums;

public enum EnmApplyScope {
	ITEM, ROLE, BUNDLE, COMBINATION, STATION, TYPE;

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
