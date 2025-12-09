package com.zbs.de.util.enums;

public enum EnmMenuItemRole {
	SINGLE, PARENT, SELECTION_GROUP, OPTIONAL, BUNDLE;

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
