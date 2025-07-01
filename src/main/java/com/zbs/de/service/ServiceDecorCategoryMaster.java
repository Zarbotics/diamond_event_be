package com.zbs.de.service;

import com.zbs.de.model.dto.DtoDecorCategoryMaster;
import com.zbs.de.model.dto.DtoResult;

public interface ServiceDecorCategoryMaster {
	DtoResult saveOrUpdate(DtoDecorCategoryMaster dto);

	DtoResult getAll();

	DtoResult getById(Integer id);

	DtoResult deleteById(Integer id);
}