package com.zbs.de.model.dto.price;

import java.util.List;

public class DtoPriceMatrixTree {
	private Long versionId;
	private String versionName;
	private String versionCode;
	private List<DtoPriceMatrixMenuItem> menuItems;

	public Long getVersionId() {
		return versionId;
	}

	public void setVersionId(Long versionId) {
		this.versionId = versionId;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public String getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(String versionCode) {
		this.versionCode = versionCode;
	}

	public List<DtoPriceMatrixMenuItem> getMenuItems() {
		return menuItems;
	}

	public void setMenuItems(List<DtoPriceMatrixMenuItem> menuItems) {
		this.menuItems = menuItems;
	}

}