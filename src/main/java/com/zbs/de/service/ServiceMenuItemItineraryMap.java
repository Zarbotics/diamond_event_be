package com.zbs.de.service;

import java.util.List;

import com.zbs.de.model.dto.DtoMenuItemItineraryMap;

public interface ServiceMenuItemItineraryMap {
	DtoMenuItemItineraryMap create(DtoMenuItemItineraryMap dto);

	void delete(Long id);

	List<DtoMenuItemItineraryMap> findByMenuItem(Long menuItemId);
}