package com.zbs.de.util.enums;

public enum EnmMenuItemType {
	CATEGORY, SUBCATEGORY, STATION, SELECTION,GROUP, BUNDLE, ITEM;

	public static EnmMenuItemType of(String s) {
		if (s == null)
			return null;
		try {
			return EnmMenuItemType.valueOf(s.toUpperCase());
		} catch (Exception ex) {
			return null;
		}
	}

}
