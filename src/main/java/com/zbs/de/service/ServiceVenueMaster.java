package com.zbs.de.service;

import com.zbs.de.model.dto.DtoVenueMaster;
import com.zbs.de.util.ResponseMessage;

public interface ServiceVenueMaster {
	ResponseMessage saveOrUpdate(DtoVenueMaster dto);

	ResponseMessage getAllVenues();

	ResponseMessage getAllVenuesGroupedByCity();

	ResponseMessage getVenuesByCityId(Integer cityId);
}
