package com.zbs.de.util.enums;

public enum EnmMenuItemType {
	STATION, BUNDLE, GROUP,SECTION,CATEGORY, SUBCATEGORY, ITEM, OPTION, SELECTION;

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
