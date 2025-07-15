package com.zbs.de.mapper;

import com.zbs.de.model.EventBudget;
import com.zbs.de.model.EventMaster;
import com.zbs.de.model.dto.DtoEventBudget;
import com.zbs.de.util.UtilDateAndTime;
import com.zbs.de.util.UtilRandomKey;

public class MapperEventBudget {
	public static EventBudget toEntity(DtoEventBudget dto, EventMaster eventMaster) {
		EventBudget entity = new EventBudget();
		entity.setSerEventBudgetId(dto.getSerEventBudgetId());
		entity.setEventMaster(eventMaster);
		entity.setNumTotalBudget(dto.getNumTotalBudget());
		entity.setNumTotalExpense(dto.getNumTotalExpense());
		entity.setTxtPaymentType(dto.getTxtPaymentType());
		entity.setTxtPaymentStatus(dto.getTxtPaymentStatus());
		if (UtilRandomKey.isNotNull(dto.getDteDealDate())) {
			entity.setDteDealDate(UtilDateAndTime.ddmmyyyyStringToDate(dto.getDteDealDate()));
		}
		entity.setTxtDealClosedBy(dto.getTxtDealClosedBy());
		entity.setTxtRemarks(dto.getTxtRemarks());

		if (dto.getNumTotalBudget() != null && dto.getNumTotalExpense() != null) {
			entity.setNumTotalProfit(dto.getNumTotalBudget().subtract(dto.getNumTotalExpense()));
		}

		return entity;
	}

	public static DtoEventBudget toDto(EventBudget entity) {
		DtoEventBudget dto = new DtoEventBudget();
		dto.setSerEventBudgetId(entity.getSerEventBudgetId());
		dto.setSerEventMasterId(entity.getEventMaster().getSerEventMasterId());
		dto.setNumTotalBudget(entity.getNumTotalBudget());
		dto.setNumTotalExpense(entity.getNumTotalExpense());
		dto.setNumTotalProfit(entity.getNumTotalProfit());
		dto.setTxtPaymentType(entity.getTxtPaymentType());
		dto.setTxtPaymentStatus(entity.getTxtPaymentStatus());
		if (UtilRandomKey.isNotNull(entity.getDteDealDate())) {
			dto.setDteDealDate(UtilDateAndTime.dateToStringddmmyyyy(entity.getDteDealDate()));
		}
		dto.setTxtDealClosedBy(entity.getTxtDealClosedBy());
		dto.setTxtRemarks(entity.getTxtRemarks());
		return dto;
	}
}
