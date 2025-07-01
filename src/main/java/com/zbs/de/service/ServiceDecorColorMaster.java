package com.zbs.de.service;

import com.zbs.de.model.DecorColorMaster;
import com.zbs.de.model.dto.DtoDecorColorMaster;
import com.zbs.de.model.dto.DtoResult;

public interface ServiceDecorColorMaster {

	DtoResult saveOrUpdate(DtoDecorColorMaster dto);

	DtoResult getAll();

	DtoResult getById(Integer id);
	
	DecorColorMaster getByPK(Integer id);

	DtoResult deleteById(Integer id);

}
