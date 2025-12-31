package com.zbs.de.model.dto.price;

import lombok.Data;
import java.util.List;

public class DtoBulkAssignResult {
	private Boolean success;
	private String message;
	private Integer createdCount;
	private Integer updatedCount;
	private List<FailedItem> failedItems;

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Integer getCreatedCount() {
		return createdCount;
	}

	public void setCreatedCount(Integer createdCount) {
		this.createdCount = createdCount;
	}

	public Integer getUpdatedCount() {
		return updatedCount;
	}

	public void setUpdatedCount(Integer updatedCount) {
		this.updatedCount = updatedCount;
	}

	public List<FailedItem> getFailedItems() {
		return failedItems;
	}

	public void setFailedItems(List<FailedItem> failedItems) {
		this.failedItems = failedItems;
	}

	public static class FailedItem {
		private Long itemId;
		private String itemCode;
		private String error;

		public Long getItemId() {
			return itemId;
		}

		public void setItemId(Long itemId) {
			this.itemId = itemId;
		}

		public String getItemCode() {
			return itemCode;
		}

		public void setItemCode(String itemCode) {
			this.itemCode = itemCode;
		}

		public String getError() {
			return error;
		}

		public void setError(String error) {
			this.error = error;
		}

	}
}