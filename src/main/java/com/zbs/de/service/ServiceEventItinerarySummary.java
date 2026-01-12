package com.zbs.de.service;


import com.zbs.de.model.dto.DtoResult;

public interface ServiceEventItinerarySummary {
	void calculateEventItinerary(Integer serEventMasterId);

	DtoResult getMenuItinerary(Integer eventId);

	DtoResult getEventItinerarySummary(Integer eventId);
}
