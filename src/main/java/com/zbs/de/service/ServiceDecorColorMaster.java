package com.zbs.de.service;

import com.zbs.de.model.dto.DtoDecorColorMaster;
import com.zbs.de.model.dto.DtoResult;

public interface ServiceDecorColorMaster {

	DtoResult saveOrUpdate(DtoDecorColorMaster dto);

	DtoResult getAll();

	DtoResult getById(Integer id);

	DtoResult deleteById(Integer id);

}
