package com.zbs.de.service;

import com.zbs.de.model.dto.DtoDecorReferenceDocument;
import com.zbs.de.model.dto.DtoResult;

public interface ServiceDecorReferenceDocument {
	DtoResult saveOrUpdate(DtoDecorReferenceDocument dto);

	DtoResult getAll();

	DtoResult getById(Integer id);

	DtoResult deleteById(Integer id);
}
