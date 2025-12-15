package com.zbs.de.model.dto;

import java.util.List;

public class DtoItineraryComputeRequest {
	private Long eventMasterId; // optional if compute by event
    private Integer guestCount; // optional override
    private Integer stationsCount; // optional, used for PER_STATION multiplier
    private List<DtoItinerarySelection> selections; // optional: if provided will be used instead of DB selections
}
