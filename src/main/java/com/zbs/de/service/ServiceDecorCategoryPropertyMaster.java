package com.zbs.de.service;

import java.util.List;

import com.zbs.de.model.DecorCategoryPropertyMaster;
import com.zbs.de.model.dto.DtoDecorCategoryMaster;
import com.zbs.de.model.dto.DtoDecorCategoryPropertyMaster;
import com.zbs.de.model.dto.DtoResult;

public interface ServiceDecorCategoryPropertyMaster {
	DtoResult saveOrUpdate(DtoDecorCategoryPropertyMaster dto);

	DtoResult getAll();

	DtoResult getById(Integer id);
	
	DecorCategoryPropertyMaster getByPk(Integer id);

	DtoResult deleteById(Integer id);
	
	DtoResult saveWithListProperties(DtoDecorCategoryMaster dto);
	
	DtoResult deleteByCategoryId(Integer id);
	
	List<DecorCategoryPropertyMaster> getAllPropertiesMaster();
	
	String generateNextDecorCategoryPropertyMasterCode();
	
}
