package com.zbs.de.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtoPercentageChange {
	private long lastMonthCount;
	private long currentMonthCount;
	private BigDecimal percentChange; // null means "undefined" (division by zero)

	public DtoPercentageChange(long lastMonthCount, long currentMonthCount, BigDecimal percentChange) {
		super();
		this.lastMonthCount = lastMonthCount;
		this.currentMonthCount = currentMonthCount;
		this.percentChange = percentChange;
	}

	public DtoPercentageChange() {
		super();
		// TODO Auto-generated constructor stub
	}

	public long getLastMonthCount() {
		return lastMonthCount;
	}

	public void setLastMonthCount(long lastMonthCount) {
		this.lastMonthCount = lastMonthCount;
	}

	public long getCurrentMonthCount() {
		return currentMonthCount;
	}

	public void setCurrentMonthCount(long currentMonthCount) {
		this.currentMonthCount = currentMonthCount;
	}

	public BigDecimal getPercentChange() {
		return percentChange;
	}

	public void setPercentChange(BigDecimal percentChange) {
		this.percentChange = percentChange;
	}

}
