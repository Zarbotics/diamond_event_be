package com.zbs.de.mapper;

import com.zbs.de.model.DtoEventRunningOrder;
import com.zbs.de.model.EventRunningOrder;

public class MapperEventRunningOrder {
	public static DtoEventRunningOrder toDto(EventRunningOrder entity) {
		if (entity == null) {
			return null;
		}

		DtoEventRunningOrder dto = new DtoEventRunningOrder();
		dto.setSerEventRunningOrderId(entity.getSerEventRunningOrderId());
		dto.setTxtEventRunningOrderCode(entity.getTxtEventRunningOrderCode());
		dto.setDteGuestArrival(entity.getDteGuestArrival());
		dto.setDteBaratArrival(entity.getDteBaratArrival());
		dto.setDteNikah(entity.getDteNikah());
		dto.setDteBrideEntrance(entity.getDteBrideEntrance());
		dto.setDte_Meal(entity.getDte_Meal());
		dto.setDteEndOfNight(entity.getDteEndOfNight());
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
		entity.setDteGuestArrival(dto.getDteGuestArrival());
		entity.setDteBaratArrival(dto.getDteBaratArrival());
		entity.setDteNikah(dto.getDteNikah());
		entity.setDteBrideEntrance(dto.getDteBrideEntrance());
		entity.setDte_Meal(dto.getDte_Meal());
		entity.setDteEndOfNight(dto.getDteEndOfNight());
		entity.setBlnIsActive(dto.getBlnIsActive());

		return entity;
	}
}
