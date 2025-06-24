package com.zbs.de.service;

import com.zbs.de.model.dto.DtoEventMaster;
import com.zbs.de.model.dto.DtoResult;

public interface ServiceEventMaster {

	DtoResult saveAndUpdate(DtoEventMaster dtoEventMaster);

	String generateNextEventMasterCode();
}
