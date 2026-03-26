package com.zbs.de.mapper;

import java.util.ArrayList;
import java.util.List;

import com.zbs.de.model.DecorCategoryPropertyMaster;
import com.zbs.de.model.DecorCategoryPropertyValue;
import com.zbs.de.model.EventDecorPropertySelection;
import com.zbs.de.model.EventDecorPropertyValueSelection;
import com.zbs.de.model.dto.DtoEventDecorPropertySelection;

public class MapperEventDecorPropertySelection {
	public static EventDecorPropertySelection toEntity(DtoEventDecorPropertySelection dto) {
		EventDecorPropertySelection entity = new EventDecorPropertySelection();
		entity.setSerEventDecorPropertyId(dto.getSerEventDecorPropertyId());
		entity.setNumPrice(dto.getNumPrice());

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
		
		// For multi select property values
		if (dto.getSerPropertyValueIds() != null) {
			List<EventDecorPropertyValueSelection> values = new ArrayList<>();

			for (Integer valId : dto.getSerPropertyValueIds()) {
				EventDecorPropertyValueSelection val = new EventDecorPropertyValueSelection();

				DecorCategoryPropertyValue value = new DecorCategoryPropertyValue();
				value.setSerPropertyValueId(valId);

				val.setPropertyValue(value);
				val.setEventDecorPropertySelection(entity);

				values.add(val);
			}

			entity.setSelectedValues(values);
		}
		
		return entity;
	}

	public static DtoEventDecorPropertySelection toDto(EventDecorPropertySelection entity) {
		DtoEventDecorPropertySelection dto = new DtoEventDecorPropertySelection();
		dto.setSerEventDecorPropertyId(entity.getSerEventDecorPropertyId());
		dto.setNumPrice(entity.getNumPrice());

		if (entity.getProperty() != null) {
			dto.setSerPropertyId(entity.getProperty().getSerPropertyId());
			dto.setTxtPropertyCode(entity.getProperty().getTxtPropertyCode());
			dto.setTxtPropertyName(entity.getProperty().getTxtPropertyName());
		}
		if (entity.getSelectedValue() != null) {
			dto.setSerPropertyValueId(entity.getSelectedValue().getSerPropertyValueId());
			dto.setTxtPropertyValue(entity.getSelectedValue().getTxtPropertyValue());
		}
		
		// For multi select property values
		if (entity.getSelectedValues() != null) {
		    dto.setSerPropertyValueIds(
		        entity.getSelectedValues().stream()
		            .map(v -> v.getPropertyValue().getSerPropertyValueId())
		            .toList()
		    );  
		}
		return dto;
	}
}
