package com.zbs.de.model.dto.price;

import java.util.List;

public class DtoPriceMatrixEntries {
	private List<DtoPriceEntryGrid> itemScopeEntries;
	private List<DtoPriceEntryGrid> roleScopeEntries;
	private List<DtoPriceEntryGrid> typeScopeEntries;
	private List<DtoPriceEntryGrid> bundleScopeEntries;
	private List<DtoPriceEntryGrid> stationScopeEntries;
	private Integer totalCount;

	public List<DtoPriceEntryGrid> getItemScopeEntries() {
		return itemScopeEntries;
	}

	public void setItemScopeEntries(List<DtoPriceEntryGrid> itemScopeEntries) {
		this.itemScopeEntries = itemScopeEntries;
	}

	public List<DtoPriceEntryGrid> getRoleScopeEntries() {
		return roleScopeEntries;
	}

	public void setRoleScopeEntries(List<DtoPriceEntryGrid> roleScopeEntries) {
		this.roleScopeEntries = roleScopeEntries;
	}

	public List<DtoPriceEntryGrid> getTypeScopeEntries() {
		return typeScopeEntries;
	}

	public void setTypeScopeEntries(List<DtoPriceEntryGrid> typeScopeEntries) {
		this.typeScopeEntries = typeScopeEntries;
	}

	public List<DtoPriceEntryGrid> getBundleScopeEntries() {
		return bundleScopeEntries;
	}

	public void setBundleScopeEntries(List<DtoPriceEntryGrid> bundleScopeEntries) {
		this.bundleScopeEntries = bundleScopeEntries;
	}

	public List<DtoPriceEntryGrid> getStationScopeEntries() {
		return stationScopeEntries;
	}

	public void setStationScopeEntries(List<DtoPriceEntryGrid> stationScopeEntries) {
		this.stationScopeEntries = stationScopeEntries;
	}

	public Integer getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}

}
