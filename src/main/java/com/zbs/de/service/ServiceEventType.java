package com.zbs.de.service;

import java.util.List;

import com.zbs.de.model.EventType;
import com.zbs.de.model.dto.DtoEventType;
import com.zbs.de.util.ResponseMessage;

public interface ServiceEventType {
	
	List<DtoEventType> getAllData();

	ResponseMessage saveAndUpdate(DtoEventType dtoEventType);

	ResponseMessage getById(Integer id);
	
	EventType getByPK(Integer id);
	
	List<DtoEventType> getAllEventTypesWithSubEvents();
}