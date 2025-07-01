package com.zbs.de.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.zbs.de.model.DecorCategoryMaster;
import com.zbs.de.model.DecorItemMaster;
import com.zbs.de.model.DecorReferenceDocument;
import com.zbs.de.model.dto.DtoDecorItemMaster;
import com.zbs.de.model.dto.DtoDecorReferenceDocument;
import com.zbs.de.model.dto.DtoVenueMasterDetailDocument;
import com.zbs.de.util.UtilRandomKey;

public class MapperDecorItemMaster {
	public static DecorItemMaster toEntity(DtoDecorItemMaster dto) {
		DecorItemMaster entity = new DecorItemMaster();
		entity.setSerDecorItemId(dto.getSerDecorItemId());
		entity.setTxtDecorCode(dto.getTxtDecorCode());
		entity.setTxtDecorName(dto.getTxtDecorName());
		entity.setBlnIsColorRequired(dto.getBlnIsColorRequired());
		entity.setBlnIsImageRequired(dto.getBlnIsImageRequired());
		entity.setBlnIsCountRequired(dto.getBlnIsCountRequired());
		entity.setBlnIsActive(dto.getBlnIsActive());

		if (dto.getSerDecorCategoryId() != null) {
			DecorCategoryMaster category = new DecorCategoryMaster();
			category.setSerDecorCategoryId(dto.getSerDecorCategoryId());
			entity.setDecorCategoryMaster(category);
		}
		return entity;
	}

	public static DtoDecorItemMaster toDto(DecorItemMaster entity) {
		DtoDecorItemMaster dto = new DtoDecorItemMaster();
		dto.setSerDecorItemId(entity.getSerDecorItemId());
		dto.setTxtDecorCode(entity.getTxtDecorCode());
		dto.setTxtDecorName(entity.getTxtDecorName());
		dto.setBlnIsColorRequired(entity.getBlnIsColorRequired());
		dto.setBlnIsImageRequired(entity.getBlnIsImageRequired());
		dto.setBlnIsCountRequired(entity.getBlnIsCountRequired());
		dto.setBlnIsActive(entity.getBlnIsActive());

		if (entity.getDecorCategoryMaster() != null) {
			dto.setSerDecorCategoryId(entity.getDecorCategoryMaster().getSerDecorCategoryId());
			dto.setTxtDecorCategoryCode(entity.getDecorCategoryMaster().getTxtDecorCategoryCode());
			dto.setTxtDecorCategoryName(entity.getDecorCategoryMaster().getTxtDecorCategoryName());
		}

		if (UtilRandomKey.isNotNull(entity.getDecorReferenceDocuments())) {
			List<DtoDecorReferenceDocument> dtoDecorReferenceDocuments = new ArrayList<>();
			for (DecorReferenceDocument doc : entity.getDecorReferenceDocuments()) {
				DtoDecorReferenceDocument d = new DtoDecorReferenceDocument();
				d.setSerDecorReferenceDocumentId(doc.getDocumentId());
				d.setTxtDocumentName(doc.getDocumentName());
				d.setTxtDocumentType(doc.getDocumentType());
				d.setTxtOriginalName(doc.getOriginalName());
				d.setTxtSize(doc.getSize());
				d.setTxtFilePath(doc.getFilePath());
				dtoDecorReferenceDocuments.add(d);
			}
			dto.setDtoDecorReferenceDocumentLst(dtoDecorReferenceDocuments);
		}
		return dto;
	}
}