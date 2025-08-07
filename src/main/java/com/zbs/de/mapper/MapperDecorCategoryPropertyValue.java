package com.zbs.de.mapper;

import com.zbs.de.model.DecorCategoryPropertyMaster;
import com.zbs.de.model.DecorCategoryPropertyValue;
import com.zbs.de.model.dto.DtoDecorCategoryPropertyValue;
import com.zbs.de.model.dto.DtoDecorCategoryPropertyValueDocument;

public class MapperDecorCategoryPropertyValue {
	public static DecorCategoryPropertyValue toEntity(DtoDecorCategoryPropertyValue dto) {
		DecorCategoryPropertyValue entity = new DecorCategoryPropertyValue();
		entity.setSerPropertyValueId(dto.getSerPropertyValueId());
		entity.setTxtPropertyValue(dto.getTxtPropertyValue());
		entity.setBlnIsActive(dto.getBlnIsActive());
		entity.setBlnIsDeleted(false);

		if (dto.getSerPropertyId() != null) {
			DecorCategoryPropertyMaster property = new DecorCategoryPropertyMaster();
			property.setSerPropertyId(dto.getSerPropertyId());
			entity.setDecorCategoryProperty(property);
		}

		return entity;
	}

	public static DtoDecorCategoryPropertyValue toDto(DecorCategoryPropertyValue entity) {
		DtoDecorCategoryPropertyValue dto = new DtoDecorCategoryPropertyValue();
		dto.setSerPropertyValueId(entity.getSerPropertyValueId());
		dto.setTxtPropertyValue(entity.getTxtPropertyValue());
		dto.setBlnIsActive(entity.getBlnIsActive());
		dto.setBlnIsDocument(entity.getBlnIsDocument());

		if (entity.getDecorCategoryProperty() != null) {
			dto.setSerPropertyId(entity.getDecorCategoryProperty().getSerPropertyId());
			dto.setTxtPropertyName(entity.getDecorCategoryProperty().getTxtPropertyName());
		}
		if (entity.getDecorCategoryPropertyValueDocument() != null) {
			DtoDecorCategoryPropertyValueDocument dtoDoc = new DtoDecorCategoryPropertyValueDocument();
			dtoDoc.setDocumentId(entity.getDecorCategoryPropertyValueDocument().getDocumentId());
			dtoDoc.setDocumentName(entity.getDecorCategoryPropertyValueDocument().getDocumentName());
			dtoDoc.setOriginalName(entity.getDecorCategoryPropertyValueDocument().getOriginalName());
			dtoDoc.setTxtDocumentUrl(entity.getDecorCategoryPropertyValueDocument().getFilePath());
			dtoDoc.setDocumentType(entity.getDecorCategoryPropertyValueDocument().getDocumentType());
			dtoDoc.setSize(entity.getDecorCategoryPropertyValueDocument().getSize());
			dtoDoc.setSerPropertyValueId(entity.getSerPropertyValueId());
			dtoDoc.setTxtPropertyValue(entity.getTxtPropertyValue());
			dto.setDocument(dtoDoc);
		}

		return dto;
	}
}
