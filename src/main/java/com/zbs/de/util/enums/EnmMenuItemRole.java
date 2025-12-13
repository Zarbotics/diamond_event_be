package com.zbs.de.util.enums;

public enum EnmMenuItemRole {
	CATEGORY, SUBCATEGORY, STATION, GROUP, BUNDLE, ITEM;

	public static EnmMenuItemRole of(String s) {
		if (s == null)
			return null;
		try {
			return EnmMenuItemRole.valueOf(s.toUpperCase());
		} catch (Exception ex) {
			return null;
		}
	}

}
