package com.zbs.de.service;

import com.zbs.de.model.dto.DtoDecorCategoryPropertyValue;
import com.zbs.de.model.dto.DtoResult;

public interface ServiceDecorCategoryPropertyValue {
	DtoResult saveOrUpdate(DtoDecorCategoryPropertyValue dto);

	DtoResult getAll();

	DtoResult getByPropertyId(Integer propertyId);

	DtoResult getById(Integer id);

	DtoResult deleteById(Integer id);
}
