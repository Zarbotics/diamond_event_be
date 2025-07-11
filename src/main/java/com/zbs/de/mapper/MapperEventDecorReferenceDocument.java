package com.zbs.de.mapper;

import com.zbs.de.model.EventDecorReferenceDocument;
import com.zbs.de.model.dto.DtoEventDecorReferenceDocument;
import com.zbs.de.util.UtilRandomKey;

public class MapperEventDecorReferenceDocument {

	public static EventDecorReferenceDocument toEntity(DtoEventDecorReferenceDocument dto) {
		if (dto == null)
			return null;

		EventDecorReferenceDocument entity = new EventDecorReferenceDocument();
		entity.setDocumentId(dto.getDocumentId());
		entity.setDocumentName(dto.getDocumentName());
		entity.setDocumentType(dto.getDocumentType());
		entity.setOriginalName(dto.getOriginalName());
		entity.setSize(dto.getSize());
		entity.setFilePath(dto.getTxtDocumentUrl());
		entity.setBlnIsDeleted(false); // Default as fals

		return entity;
	}

	public static DtoEventDecorReferenceDocument toDto(EventDecorReferenceDocument entity) {
		if (entity == null)
			return null;

		DtoEventDecorReferenceDocument dto = new DtoEventDecorReferenceDocument();
		dto.setDocumentId(entity.getDocumentId());
		dto.setDocumentName(entity.getDocumentName());
		dto.setDocumentType(entity.getDocumentType());
		dto.setOriginalName(entity.getOriginalName());
		dto.setSize(entity.getSize());
		dto.setTxtDocumentUrl(entity.getFilePath());

		if (UtilRandomKey.isNotNull(entity.getEventDecorCategorySelection())) {
			dto.setSerEventDecorCategorySelectionId(
					entity.getEventDecorCategorySelection().getSerEventDecorCategorySelectionId());
			dto.setTxtEventDecorCategorySelectionCode(
					entity.getEventDecorCategorySelection().getTxtEventDecorCategorySelectionCode());
		}

		return dto;
	}
}
