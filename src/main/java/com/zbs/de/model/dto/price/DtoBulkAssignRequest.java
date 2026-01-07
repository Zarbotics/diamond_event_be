package com.zbs.de.model.dto.price;

import java.util.List;
import java.util.Map;

public class DtoBulkAssignRequest {
	private String assignmentType; // SELECTED_ITEMS, BY_ROLE, BY_TYPE, BY_BUNDLE
	private List<Long> targetIds; // menu item IDs for SELECTED_ITEMS
	private String scope; // ITEM, ROLE, TYPE, BUNDLE, STATION
	private String scopeValue; // for BY_ROLE, BY_TYPE
	private DtoPriceData priceData;
	private Map<String, Object> metadata;
	private Boolean replaceExisting = true;

	public String getAssignmentType() {
		return assignmentType;
	}

	public void setAssignmentType(String assignmentType) {
		this.assignmentType = assignmentType;
	}

	public List<Long> getTargetIds() {
		return targetIds;
	}

	public void setTargetIds(List<Long> targetIds) {
		this.targetIds = targetIds;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getScopeValue() {
		return scopeValue;
	}

	public void setScopeValue(String scopeValue) {
		this.scopeValue = scopeValue;
	}

	public DtoPriceData getPriceData() {
		return priceData;
	}

	public void setPriceData(DtoPriceData priceData) {
		this.priceData = priceData;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	public Boolean getReplaceExisting() {
		return replaceExisting;
	}

	public void setReplaceExisting(Boolean replaceExisting) {
		this.replaceExisting = replaceExisting;
	}

}
