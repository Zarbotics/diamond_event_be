package com.zbs.de.mapper;

import com.zbs.de.model.DecorCategoryPropertyMaster;
import com.zbs.de.model.DecorCategoryPropertyValue;
import com.zbs.de.model.EventDecorPropertySelection;
import com.zbs.de.model.dto.DtoEventDecorPropertySelection;

public class MapperEventDecorPropertySelection {
	public static EventDecorPropertySelection toEntity(DtoEventDecorPropertySelection dto) {
		EventDecorPropertySelection entity = new EventDecorPropertySelection();
		entity.setSerEventDecorPropertyId(dto.getSerEventDecorPropertyId());

		if (dto.getSerPropertyId() != null) {
			DecorCategoryPropertyMaster property = new DecorCategoryPropertyMaster();
			property.setSerPropertyId(dto.getSerPropertyId());
			entity.setProperty(property);
		}
		if (dto.getSerPropertyValueId() != null) {
			DecorCategoryPropertyValue value = new DecorCategoryPropertyValue();
			value.setSerPropertyValueId(dto.getSerPropertyValueId());
			entity.setSelectedValue(value);
		}
		return entity;
	}

	public static DtoEventDecorPropertySelection toDto(EventDecorPropertySelection entity) {
		DtoEventDecorPropertySelection dto = new DtoEventDecorPropertySelection();
		dto.setSerEventDecorPropertyId(entity.getSerEventDecorPropertyId());

		if (entity.getProperty() != null) {
			dto.setSerPropertyId(entity.getProperty().getSerPropertyId());
			dto.setTxtPropertyCode(entity.getProperty().getTxtPropertyCode());
			dto.setTxtPropertyName(entity.getProperty().getTxtPropertyName());
		}
		if (entity.getSelectedValue() != null) {
			dto.setSerPropertyValueId(entity.getSelectedValue().getSerPropertyValueId());
			dto.setTxtPropertyValue(entity.getSelectedValue().getTxtPropertyValue());
		}
		return dto;
	}
}
