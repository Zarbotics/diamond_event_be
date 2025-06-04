package com.zbs.de.mapper;

import com.zbs.de.model.EventType;
import com.zbs.de.model.dto.DtoEventType;

public class MapperEventType {

	public static DtoEventType toDto(EventType entity) {
		DtoEventType dto = new DtoEventType();
		dto.setSerEventTypeId(entity.getSerEventTypeId());
		dto.setTxtEventTypeCode(entity.getTxtEventTypeCode());
		dto.setTxtEventTypeName(entity.getTxtEventTypeName());
		dto.setBlnIsMainEvent(entity.getBlnIsMainEvent());
		dto.setParentEventTypeId(
				entity.getParentEventType() != null ? entity.getParentEventType().getSerEventTypeId() : null);
		return dto;
	}

	public static EventType toEntity(DtoEventType dto) {
		EventType entity = new EventType();
		entity.setSerEventTypeId(dto.getSerEventTypeId());
		entity.setTxtEventTypeCode(dto.getTxtEventTypeCode());
		entity.setTxtEventTypeName(dto.getTxtEventTypeName());
		entity.setBlnIsMainEvent(dto.getBlnIsMainEvent());
		return entity;
	}
}
