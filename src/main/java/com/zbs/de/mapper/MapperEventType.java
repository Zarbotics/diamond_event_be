package com.zbs.de.mapper;

import java.util.stream.Collectors;

import com.zbs.de.model.EventType;
import com.zbs.de.model.dto.DtoEventType;
import com.zbs.de.model.dto.DtoEventTypeDocument;
import com.zbs.de.util.UtilRandomKey;

public class MapperEventType {

	public static DtoEventType toDto(EventType entity) {
		DtoEventType dto = new DtoEventType();
		dto.setSerEventTypeId(entity.getSerEventTypeId());
		dto.setTxtEventTypeCode(entity.getTxtEventTypeCode());
		dto.setTxtEventTypeName(entity.getTxtEventTypeName());
		dto.setBlnIsMainEvent(entity.getBlnIsMainEvent());
		dto.setParentEventTypeId(
				entity.getParentEventType() != null ? entity.getParentEventType().getSerEventTypeId() : null);

		if (UtilRandomKey.isNotNull(entity.getEventTypeDocuments())) {
			dto.setDocuments(entity.getEventTypeDocuments().stream().map(doc -> {
				DtoEventTypeDocument d = new DtoEventTypeDocument();
				d.setDocumentId(doc.getDocumentId());
				d.setDocumentName(doc.getDocumentName());
				d.setDocumentType(doc.getDocumentType());
				d.setOriginalName(doc.getOriginalName());
				d.setSize(doc.getSize());
				d.setTxtDocumentUrl(doc.getFilePath());
//				d.setDocumentFile(doc.getDocumentFile());
				return d;
			}).collect(Collectors.toList()));
		}

		return dto;
	}

	public static EventType toEntity(DtoEventType dto) {
		EventType entity = new EventType();
		entity.setSerEventTypeId(dto.getSerEventTypeId());
		entity.setTxtEventTypeCode(dto.getTxtEventTypeCode());
		entity.setTxtEventTypeName(dto.getTxtEventTypeName());
		entity.setBlnIsMainEvent(dto.getBlnIsMainEvent());
		entity.setBlnIsActive(dto.getBlnIsActive());
		return entity;
	}
}
