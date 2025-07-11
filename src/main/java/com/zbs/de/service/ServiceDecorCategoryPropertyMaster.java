package com.zbs.de.service;

import com.zbs.de.model.dto.DtoDecorCategoryPropertyMaster;
import com.zbs.de.model.dto.DtoResult;

public interface ServiceDecorCategoryPropertyMaster {
	DtoResult saveOrUpdate(DtoDecorCategoryPropertyMaster dto);

	DtoResult getAll();

	DtoResult getById(Integer id);

	DtoResult deleteById(Integer id);
}
