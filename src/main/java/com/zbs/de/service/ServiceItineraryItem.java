package com.zbs.de.service;

import com.zbs.de.model.dto.DtoItineraryItem;

import java.util.List;

public interface ServiceItineraryItem {
	DtoItineraryItem create(DtoItineraryItem dto);

	DtoItineraryItem update(Long id, DtoItineraryItem dto);

	DtoItineraryItem getById(Long id);

	List<DtoItineraryItem> getAll();

	void delete(Long id);
}
