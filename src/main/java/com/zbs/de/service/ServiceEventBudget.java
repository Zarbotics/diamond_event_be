package com.zbs.de.service;

import java.util.List;

import com.zbs.de.model.dto.DtoEventAnalytics;
import com.zbs.de.model.dto.DtoEventBudget;
import com.zbs.de.model.dto.DtoResult;

public interface ServiceEventBudget {
	DtoResult saveOrUpdate(DtoEventBudget dtoEventBudget);
	
	DtoResult getAllData();

	DtoEventBudget getByEventId(Integer serEventMasterId);

	List<DtoEventAnalytics> getMonthlySales();

	List<DtoEventAnalytics> getMonthlyProfitByEventType();

	DtoEventAnalytics getOverallSummary();
}
