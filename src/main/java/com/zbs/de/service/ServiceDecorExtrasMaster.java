package com.zbs.de.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.zbs.de.model.DecorExtrasMaster;
import com.zbs.de.model.dto.DtoDecorExtrasMaster;
import com.zbs.de.model.dto.DtoResult;

public interface ServiceDecorExtrasMaster {

	DtoResult saveWithListOptions(DtoDecorExtrasMaster dto, List<MultipartFile> files);

	DecorExtrasMaster getByPk(Integer id);

	DtoResult getAll();

	DtoResult getById(Integer id);
	
	DecorExtrasMaster getByIdAndNotDeleted(Integer id);

	DtoResult deleteById(Integer id);
	
	DtoResult deleteExtrasOptionById(Integer id);
	
	List<DecorExtrasMaster> getAllDecorExtrasMaster();
	
	 DtoResult saveAndUpdate(DtoDecorExtrasMaster dto);
	 
	 DtoResult getAllActive();

}
