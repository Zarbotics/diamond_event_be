package com.zbs.de.service;

import java.util.List;

import com.zbs.de.model.DecorCategoryReferenceDocument;
import com.zbs.de.model.dto.DtoDecorCategoryReferenceDocument;
import com.zbs.de.model.dto.DtoResult;

public interface ServiceDecorCategoryReferenceDocument {
	DtoResult saveOrUpdate(DtoDecorCategoryReferenceDocument dto);

	DtoResult getAll();

	DtoResult getByCategoryId(Integer categoryId);

	DtoResult getById(Integer id);

	DtoResult deleteById(Integer id);
	
	DtoResult saveAll(List<DecorCategoryReferenceDocument> decorCategoryReferenceDocuments);
	
	DtoResult deleteByCategoryId(Integer id);
}
