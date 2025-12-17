package com.zbs.de.service;

import com.zbs.de.model.ItineraryItemType;
import com.zbs.de.model.dto.DtoItineraryItemType;
import com.zbs.de.model.dto.DtoResult;

public interface ServiceItineraryItemType {
	DtoResult create(DtoItineraryItemType dto);

	DtoResult update(DtoItineraryItemType dto);

	DtoResult getAll();

	DtoResult getAllActive();

	DtoResult getById(Integer id);

	ItineraryItemType getItineraryItemTypeById(Integer Id);
	
	DtoResult generateNextCode();

}
