package com.zbs.de.service;

import com.zbs.de.model.dto.DtoDecorItemMaster;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.util.ResponseMessage;

public interface ServiceDecorItemMaster {
	DtoResult saveOrUpdate(DtoDecorItemMaster dto);

	DtoResult getAll();

	DtoResult getById(Integer id);

	DtoResult deleteById(Integer id);
}