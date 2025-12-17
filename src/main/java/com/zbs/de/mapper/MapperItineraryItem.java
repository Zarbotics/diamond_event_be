package com.zbs.de.mapper;

import java.util.HashMap;

import com.zbs.de.model.ItineraryItem;
import com.zbs.de.model.dto.DtoItineraryItem;

public class MapperItineraryItem {

	public DtoItineraryItem toDto(ItineraryItem entity) {
		if (entity == null)
			return null;

		DtoItineraryItem dto = new DtoItineraryItem();
		dto.setSerItineraryItemId(entity.getSerItineraryItemId());
		dto.setTxtCode(entity.getTxtCode());
		dto.setTxtName(entity.getTxtName());
		if (entity.getItineraryItemType() != null) {
			dto.setSerItineraryItemTypeId(entity.getItineraryItemType().getSerItineraryItemTypeId());
			dto.setTxtItineraryItemCode(entity.getItineraryItemType().getTxtCode());
			dto.setTxtItineraryItemName(entity.getItineraryItemType().getTxtName());
		}
		dto.setMetadata(entity.getMetadata() != null ? new HashMap<>(entity.getMetadata()) : null);

		return dto;
	}

	public ItineraryItem toEntity(DtoItineraryItem dto) {
		if (dto == null)
			return null;

		ItineraryItem entity = new ItineraryItem();
		entity.setSerItineraryItemId(dto.getSerItineraryItemId());
		entity.setTxtCode(dto.getTxtCode());
		entity.setTxtName(dto.getTxtName());
		entity.setMetadata(dto.getMetadata() != null ? new HashMap<>(dto.getMetadata()) : null);

		return entity;
	}
}
