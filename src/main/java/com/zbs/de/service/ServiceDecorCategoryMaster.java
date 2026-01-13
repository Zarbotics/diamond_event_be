package com.zbs.de.service;

import org.springframework.web.multipart.MultipartFile;

import com.zbs.de.model.DecorCategoryMaster;
import com.zbs.de.model.dto.DtoDecorCategoryMaster;
import com.zbs.de.model.dto.DtoResult;

public interface ServiceDecorCategoryMaster {
	DtoResult saveOrUpdate(DtoDecorCategoryMaster dto);

	DtoResult getAll();

	DtoResult getById(Integer id);

	DtoResult deleteById(Integer id);

	DecorCategoryMaster getByPK(Integer id);
	
	public DtoResult getAllMasterData();

	DtoResult saveWithDocuments(DtoDecorCategoryMaster dto, MultipartFile[] documents);
	
	DtoResult getAllActive();
	
	String generateNextDecorCategoryMasterCode();
	
	DtoResult getAllDecorMasterDataWithPrice();
}