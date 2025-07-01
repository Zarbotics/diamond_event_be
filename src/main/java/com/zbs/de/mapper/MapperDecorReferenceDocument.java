package com.zbs.de.mapper;

import com.zbs.de.model.DecorReferenceDocument;
import com.zbs.de.model.dto.DtoDecorReferenceDocument;

public class MapperDecorReferenceDocument {
	public static DecorReferenceDocument toEntity(DtoDecorReferenceDocument dto) {
		DecorReferenceDocument entity = new DecorReferenceDocument();
		entity.setDocumentId(dto.getSerDecorReferenceDocumentId());
		entity.setDocumentName(dto.getTxtDocumentName());
		entity.setDocumentType(dto.getTxtDocumentType());
		entity.setOriginalName(dto.getTxtOriginalName());
		entity.setSize(dto.getTxtSize());
		entity.setFilePath(dto.getTxtFilePath());
		entity.setBlnIsActive(dto.getBlnIsActive());
		return entity;
	}

	public static DtoDecorReferenceDocument toDto(DecorReferenceDocument entity) {
		DtoDecorReferenceDocument dto = new DtoDecorReferenceDocument();
		dto.setSerDecorReferenceDocumentId(entity.getDocumentId());
		dto.setTxtDocumentName(entity.getDocumentName());
		dto.setTxtDocumentType(entity.getDocumentType());
		dto.setTxtOriginalName(entity.getOriginalName());
		dto.setTxtSize(entity.getSize());
		dto.setTxtFilePath(entity.getFilePath());
		dto.setBlnIsActive(entity.getBlnIsActive());

		if (entity.getDecorItemMaster() != null) {
			dto.setSerDecorItemId(entity.getDecorItemMaster().getSerDecorItemId());
		}
		return dto;
	}
}
