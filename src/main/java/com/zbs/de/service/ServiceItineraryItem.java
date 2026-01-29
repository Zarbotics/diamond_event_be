package com.zbs.de.service;

import org.springframework.web.multipart.MultipartFile;

import com.zbs.de.model.ItineraryItem;
import com.zbs.de.model.dto.DtoItineraryCsvImportResult;
import com.zbs.de.model.dto.DtoItineraryItem;
import com.zbs.de.model.dto.DtoResult;

public interface ServiceItineraryItem {

	DtoResult create(DtoItineraryItem dto);

	DtoResult update(DtoItineraryItem dto);

	DtoResult getById(Long id);

	ItineraryItem getItineraryItemById(Long id);

	DtoResult getAll();

	DtoResult getAllActive();

	DtoResult generateNextCode();

	DtoResult getAllActiveItineraryItemsByType(Integer typeId);

	DtoResult getAllItineraryItemsByType(Integer typeId);
	
	DtoItineraryCsvImportResult importCsv(MultipartFile file);
}
