package com.zbs.de.service;

import com.zbs.de.model.dto.DtoEventDecorItemSelection;
import com.zbs.de.model.dto.DtoResult;

public interface ServiceEventDecorItemSelection {
	DtoResult saveOrUpdate(DtoEventDecorItemSelection dto);

	DtoResult getAll();

	DtoResult getById(Integer id);

	DtoResult deleteById(Integer id);
	
	DtoResult deleteByEventMasterId(Integer serEventMasterId);
}
