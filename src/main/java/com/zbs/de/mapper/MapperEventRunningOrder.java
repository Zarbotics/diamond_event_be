package com.zbs.de.mapper;

import com.zbs.de.model.EventRunningOrder;
import com.zbs.de.model.dto.DtoEventRunningOrder;

public class MapperEventRunningOrder {
	public static DtoEventRunningOrder toDto(EventRunningOrder entity) {
		if (entity == null) {
			return null;
		}

		DtoEventRunningOrder dto = new DtoEventRunningOrder();
		dto.setSerEventRunningOrderId(entity.getSerEventRunningOrderId());
		dto.setTxtEventRunningOrderCode(entity.getTxtEventRunningOrderCode());
		dto.setTxtGuestArrival(entity.getTxtGuestArrival());
		dto.setTxtBrideGuestArrival(entity.getTxtBrideGuestArrival());
		dto.setTxtGroomGuestArrival(entity.getTxtGroomGuestArrival());
		dto.setTxtBaratArrival(entity.getTxtBaratArrival());
		dto.setTxtNikah(entity.getTxtNikah());
		dto.setTxtBrideEntrance(entity.getTxtBrideEntrance());
		dto.setTxtGroomEntrance(entity.getTxtGroomEntrance());
		dto.setTxtCouplesEntrance(entity.getTxtCouplesEntrance());
		dto.setTxtDua(entity.getTxtDua());
		dto.setTxtDance(entity.getTxtDance());
		dto.setTxtCakeCutting(entity.getTxtCakeCutting());
		dto.setTxtRingExchange(entity.getTxtRingExchange());
		dto.setTxtRams(entity.getTxtRams());
		dto.setTxtSpeeches(entity.getTxtSpeeches());
		dto.setTxtMeal(entity.getTxtMeal());
		dto.setTxtEndOfNight(entity.getTxtEndOfNight());
		dto.setBlnIsActive(entity.getBlnIsActive());

		return dto;
	}

	public static EventRunningOrder toEntity(DtoEventRunningOrder dto) {
		if (dto == null) {
			return null;
		}

		EventRunningOrder entity = new EventRunningOrder();
		entity.setSerEventRunningOrderId(dto.getSerEventRunningOrderId());
		entity.setTxtEventRunningOrderCode(dto.getTxtEventRunningOrderCode());
		entity.setTxtGuestArrival(dto.getTxtGuestArrival());
		entity.setTxtBrideGuestArrival(dto.getTxtBrideGuestArrival());
		entity.setTxtGroomGuestArrival(dto.getTxtGroomGuestArrival());
		entity.setTxtBaratArrival(dto.getTxtBaratArrival());
		entity.setTxtNikah(dto.getTxtNikah());
		entity.setTxtBrideEntrance(dto.getTxtBrideEntrance());
		entity.setTxtGroomEntrance(dto.getTxtGroomEntrance());
		entity.setTxtCouplesEntrance(dto.getTxtCouplesEntrance());
		entity.setTxtDua(dto.getTxtDua());
		entity.setTxtDance(dto.getTxtDance());
		entity.setTxtCakeCutting(dto.getTxtCakeCutting());
		entity.setTxtRingExchange(dto.getTxtRingExchange());
		entity.setTxtRams(dto.getTxtRams());
		entity.setTxtSpeeches(dto.getTxtSpeeches());
		entity.setTxtMeal(dto.getTxtMeal());
		entity.setTxtEndOfNight(dto.getTxtEndOfNight());
		entity.setBlnIsActive(dto.getBlnIsActive());

		return entity;
	}
}
