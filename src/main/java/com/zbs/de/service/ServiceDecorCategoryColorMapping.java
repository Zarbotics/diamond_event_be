package com.zbs.de.service;

import com.zbs.de.model.dto.DtoDecorCategoryColorMapping;
import com.zbs.de.model.dto.DtoResult;

public interface ServiceDecorCategoryColorMapping {
	DtoResult saveOrUpdate(DtoDecorCategoryColorMapping dto);

	DtoResult getAll();

	DtoResult getById(Integer id);

	DtoResult deleteById(Integer id);

	DtoResult getByCategoryId(Integer id);
}
