package com.zbs.de.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.zbs.de.model.DecorCategoryMaster;
import com.zbs.de.model.EventDecorCategorySelection;
import com.zbs.de.model.EventDecorPropertySelection;
import com.zbs.de.model.EventDecorReferenceDocument;
import com.zbs.de.model.dto.DtoEventDecorReferenceDocument;
import com.zbs.de.model.dto.DtoEventDecorCategorySelection;
import com.zbs.de.model.dto.DtoEventDecorPropertySelection;

public class MapperEventDecorCategorySelection {
	public static EventDecorCategorySelection toEntity(DtoEventDecorCategorySelection dto) {
		EventDecorCategorySelection entity = new EventDecorCategorySelection();
		entity.setSerEventDecorCategorySelectionId(dto.getSerEventDecorCategorySelectionId());
		entity.setTxtEventDecorCategorySelectionCode(dto.getTxtEventDecorCategorySelectionCode());
		entity.setTxtRemarks(dto.getTxtRemarks());

		if (dto.getSerDecorCategoryId() != null) {
			DecorCategoryMaster category = new DecorCategoryMaster();
			category.setSerDecorCategoryId(dto.getSerDecorCategoryId());
			entity.setDecorCategory(category);
		}

		if (dto.getSelectedProperties() != null) {
			List<EventDecorPropertySelection> selected = dto.getSelectedProperties().stream()
					.map(MapperEventDecorPropertySelection::toEntity).collect(Collectors.toList());

			selected.forEach(p -> p.setEventDecorCategorySelection(entity));
			entity.setSelectedProperties(selected);
		}
		if (dto.getUserUploadedDocuments() != null) {
			List<EventDecorReferenceDocument> images = dto.getUserUploadedDocuments().stream().map(imageDto -> {
				EventDecorReferenceDocument img = MapperEventDecorReferenceDocument.toEntity(imageDto);
				img.setEventDecorCategorySelection(entity); // set parent
				return img;
			}).collect(Collectors.toList());
			entity.setUserUploadedDocuments(images);
		}
		return entity;
	}

	public static DtoEventDecorCategorySelection toDto(EventDecorCategorySelection entity) {
		DtoEventDecorCategorySelection dto = new DtoEventDecorCategorySelection();
		dto.setSerEventDecorCategorySelectionId(entity.getSerEventDecorCategorySelectionId());
		dto.setTxtEventDecorCategorySelectionCode(entity.getTxtEventDecorCategorySelectionCode());
		dto.setTxtRemarks(entity.getTxtRemarks());

		if (entity.getDecorCategory() != null) {
			dto.setSerDecorCategoryId(entity.getDecorCategory().getSerDecorCategoryId());
			dto.setTxtDecorCategoryCode(entity.getDecorCategory().getTxtDecorCategoryCode());
			dto.setTxtDecorCategoryName(entity.getDecorCategory().getTxtDecorCategoryName());
		}

		if (entity.getSelectedProperties() != null) {
			List<DtoEventDecorPropertySelection> selected = entity.getSelectedProperties().stream()
					.map(MapperEventDecorPropertySelection::toDto).collect(Collectors.toList());

			dto.setSelectedProperties(selected);
		}

		if (entity.getUserUploadedDocuments() != null) {
			List<DtoEventDecorReferenceDocument> images = entity.getUserUploadedDocuments().stream().map(image -> {
				DtoEventDecorReferenceDocument img = MapperEventDecorReferenceDocument.toDto(image);
				img.setSerEventDecorCategorySelectionId(entity.getSerEventDecorCategorySelectionId());// set parent
				return img;
			}).collect(Collectors.toList());
			dto.setUserUploadedDocuments(images);
		}
		return dto;
	}

}
