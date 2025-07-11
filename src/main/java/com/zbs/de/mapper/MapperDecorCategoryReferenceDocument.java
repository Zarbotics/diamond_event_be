package com.zbs.de.mapper;

import com.zbs.de.model.DecorCategoryMaster;
import com.zbs.de.model.DecorCategoryReferenceDocument;
import com.zbs.de.model.dto.DtoDecorCategoryReferenceDocument;

public class MapperDecorCategoryReferenceDocument {
	public static DecorCategoryReferenceDocument toEntity(DtoDecorCategoryReferenceDocument dto) {
		if (dto == null)
			return null;

		DecorCategoryReferenceDocument entity = new DecorCategoryReferenceDocument();
		entity.setDocumentId(dto.getDocumentId());
		entity.setDocumentName(dto.getDocumentName());
		entity.setDocumentType(dto.getDocumentType());
		entity.setOriginalName(dto.getOriginalName());
		entity.setSize(dto.getSize());
		entity.setFilePath(dto.getTxtDocumentUrl());
		entity.setBlnIsDeleted(false); // Default as false

		if (dto.getSerDecorCategoryId() != null) {
			DecorCategoryMaster category = new DecorCategoryMaster();
			category.setSerDecorCategoryId(dto.getSerDecorCategoryId());
			entity.setDecorCategoryMaster(category);
		}

		return entity;
	}

	public static DtoDecorCategoryReferenceDocument toDto(DecorCategoryReferenceDocument entity) {
		if (entity == null)
			return null;

		DtoDecorCategoryReferenceDocument dto = new DtoDecorCategoryReferenceDocument();
		dto.setDocumentId(entity.getDocumentId());
		dto.setDocumentName(entity.getDocumentName());
		dto.setDocumentType(entity.getDocumentType());
		dto.setOriginalName(entity.getOriginalName());
		dto.setSize(entity.getSize());
		dto.setTxtDocumentUrl(entity.getFilePath());

		if (entity.getDecorCategoryMaster() != null) {
			dto.setSerDecorCategoryId(entity.getDecorCategoryMaster().getSerDecorCategoryId());
			dto.setTxtDecorCategoryCode(entity.getDecorCategoryMaster().getTxtDecorCategoryCode());
			dto.setTxtDecorCategoryName(entity.getDecorCategoryMaster().getTxtDecorCategoryName());
		}

		return dto;
	}
}
