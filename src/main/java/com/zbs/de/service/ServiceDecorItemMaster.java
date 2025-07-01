package com.zbs.de.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.zbs.de.model.DecorItemMaster;
import com.zbs.de.model.dto.DtoDecorItemMaster;
import com.zbs.de.model.dto.DtoResult;

public interface ServiceDecorItemMaster {
	DtoResult saveOrUpdate(DtoDecorItemMaster dto);

	DtoResult getAll();

	DtoResult getById(Integer id);

	DtoResult deleteById(Integer id);
	
	DtoResult saveDecorItemWithDocuments(DtoDecorItemMaster dto, List<MultipartFile> files) throws IOException;
}