package com.zbs.de.mapper;

import java.util.ArrayList;
import java.util.List;

import com.zbs.de.model.DecorCategoryMaster;
import com.zbs.de.model.DecorCategoryPropertyMaster;
import com.zbs.de.model.DecorCategoryPropertyValue;
import com.zbs.de.model.DecorCategoryReferenceDocument;
import com.zbs.de.model.dto.DtoDecorCategoryMaster;
import com.zbs.de.model.dto.DtoDecorCategoryPropertyMaster;
import com.zbs.de.model.dto.DtoDecorCategoryPropertyValue;
import com.zbs.de.model.dto.DtoDecorCategoryReferenceDocument;
import com.zbs.de.util.UtilRandomKey;

public class MapperDecorCategoryMaster {
	public static DecorCategoryMaster toEntity(DtoDecorCategoryMaster dto) {
		DecorCategoryMaster entity = new DecorCategoryMaster();
		entity.setSerDecorCategoryId(dto.getSerDecorCategoryId());
		entity.setTxtDecorCategoryCode(dto.getTxtDecorCategoryCode());
		entity.setTxtDecorCategoryName(dto.getTxtDecorCategoryName());
		entity.setBlnIsActive(dto.getBlnIsActive());
		entity.setNumPrice(dto.getNumPrice());
		return entity;
	}

	public static DtoDecorCategoryMaster toDto(DecorCategoryMaster entity) {
		DtoDecorCategoryMaster dto = new DtoDecorCategoryMaster();
		dto.setSerDecorCategoryId(entity.getSerDecorCategoryId());
		dto.setTxtDecorCategoryCode(entity.getTxtDecorCategoryCode());
		dto.setTxtDecorCategoryName(entity.getTxtDecorCategoryName());
		dto.setBlnIsActive(entity.getBlnIsActive());
		dto.setNumPrice(entity.getNumPrice());
		List<DtoDecorCategoryReferenceDocument> dtoDocLst = new ArrayList<>();
		if (UtilRandomKey.isNotNull(entity.getReferenceDocuments())) {
			for (DecorCategoryReferenceDocument doc : entity.getReferenceDocuments()) {
				DtoDecorCategoryReferenceDocument dtoDoc = new DtoDecorCategoryReferenceDocument();
				dtoDoc.setTxtDocumentUrl(doc.getFilePath());
				dtoDocLst.add(dtoDoc);
			}

			dto.setReferenceDocuments(dtoDocLst);
		}
		return dto;
	}

	public static DtoDecorCategoryMaster toMasterDto(DecorCategoryMaster entity) {

		DtoDecorCategoryMaster dto = new DtoDecorCategoryMaster();
		dto.setSerDecorCategoryId(entity.getSerDecorCategoryId());
		dto.setTxtDecorCategoryCode(entity.getTxtDecorCategoryCode());
		dto.setTxtDecorCategoryName(entity.getTxtDecorCategoryName());
		dto.setBlnIsActive(entity.getBlnIsActive());
		dto.setNumPrice(entity.getNumPrice());
		List<DtoDecorCategoryReferenceDocument> dtoDocLst = new ArrayList<>();
		if (UtilRandomKey.isNotNull(entity.getReferenceDocuments())) {
			for (DecorCategoryReferenceDocument doc : entity.getReferenceDocuments()) {
				DtoDecorCategoryReferenceDocument dtoDoc = new DtoDecorCategoryReferenceDocument();
				dtoDoc.setTxtDocumentUrl(doc.getFilePath());
				dtoDocLst.add(dtoDoc);
			}

			dto.setReferenceDocuments(dtoDocLst);
		}

		List<DtoDecorCategoryPropertyMaster> dtoDecorCategoryPropertyMasterLst = new ArrayList<>();
		if (UtilRandomKey.isNotNull(entity.getCategoryProperties())) {
			for (DecorCategoryPropertyMaster decorCategoryPropertyMaster : entity.getCategoryProperties()) {
				DtoDecorCategoryPropertyMaster dtoDecorCategoryPropertyMaster = MapperDecorCategoryPropertyMaster
						.toDto(decorCategoryPropertyMaster);
				if (UtilRandomKey.isNotNull(decorCategoryPropertyMaster.getPropertyValues())) {
					List<DtoDecorCategoryPropertyValue> decorCategoryPropertyValueLst = new ArrayList<>();
					for (DecorCategoryPropertyValue decorCategoryPropertyValue : decorCategoryPropertyMaster
							.getPropertyValues()) {
						DtoDecorCategoryPropertyValue dtoDecorCategoryPropertyValue = MapperDecorCategoryPropertyValue
								.toDto(decorCategoryPropertyValue);
						decorCategoryPropertyValueLst.add(dtoDecorCategoryPropertyValue);
					}

					dtoDecorCategoryPropertyMaster.setPropertyValues(decorCategoryPropertyValueLst);
				}
				dtoDecorCategoryPropertyMasterLst.add(dtoDecorCategoryPropertyMaster);
			}

			dto.setCategoryProperties(dtoDecorCategoryPropertyMasterLst);

		}
		return dto;
	}

}
