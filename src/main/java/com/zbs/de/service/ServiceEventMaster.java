package com.zbs.de.service;

import com.zbs.de.model.dto.DtoEventMaster;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;

public interface ServiceEventMaster {

	DtoResult saveAndUpdate(DtoEventMaster dtoEventMaster);

	DtoResult getByEventTypeIdAndCustId(DtoSearch dtoSearch);

	String generateNextEventMasterCode();

	DtoResult getAllEvents();
}
