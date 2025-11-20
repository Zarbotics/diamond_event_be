package com.zbs.de.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.zbs.de.model.EventType;
import com.zbs.de.model.dto.DtoEventType;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.util.ResponseMessage;

public interface ServiceEventType {

	List<DtoEventType> getAllData();

	ResponseMessage saveAndUpdate(DtoEventType dtoEventType);

	ResponseMessage getById(Integer id);

	EventType getByPK(Integer id);

	List<DtoEventType> getAllEventTypesWithSubEvents();

	DtoResult saveEventTypeWithDocuments(DtoEventType dto, List<MultipartFile> files) throws IOException;

	DtoResult deleteById(Integer id);

	String generateNextEventTypeCode();

	List<DtoEventType> getAllActiveEventTypesWithSubEvents();
	
	List<DtoEventType> getAllActiveSubEventsOnlyCP();
}