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
		dto.setTxtBaratArrival(entity.getTxtBaratArrival());
		dto.setTxtNikah(entity.getTxtNikah());
		dto.setTxtBrideEntrance(entity.getTxtBrideEntrance());
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
		entity.setTxtBaratArrival(dto.getTxtBaratArrival());
		entity.setTxtNikah(dto.getTxtNikah());
		entity.setTxtBrideEntrance(dto.getTxtBrideEntrance());
		entity.setTxtMeal(dto.getTxtMeal());
		entity.setTxtEndOfNight(dto.getTxtEndOfNight());
		entity.setBlnIsActive(dto.getBlnIsActive());

		return entity;
	}
}
