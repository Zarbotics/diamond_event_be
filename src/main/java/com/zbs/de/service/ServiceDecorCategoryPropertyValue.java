package com.zbs.de.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.zbs.de.model.dto.DtoDecorCategoryPropertyMaster;
import com.zbs.de.model.dto.DtoDecorCategoryPropertyValue;
import com.zbs.de.model.dto.DtoResult;

public interface ServiceDecorCategoryPropertyValue {
	
	DtoResult saveOrUpdate(DtoDecorCategoryPropertyValue dto);

	DtoResult saveWithListValues(DtoDecorCategoryPropertyMaster dto);

	DtoResult getAll();

	DtoResult getByPropertyId(Integer propertyId);

	DtoResult getById(Integer id);

	DtoResult deleteById(Integer id);
	
	DtoResult deleteByPropertyId(Integer serPropertyId);
	
	 DtoResult saveListValuesWithDocuments(DtoDecorCategoryPropertyMaster dto, List<MultipartFile> files);
}
