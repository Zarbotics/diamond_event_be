package com.zbs.de.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.zbs.de.model.DecorExtrasOption;
import com.zbs.de.model.dto.DtoDecorExtrasOption;
import com.zbs.de.model.dto.DtoResult;

public interface ServiceDecorExtrasOption {

	DecorExtrasOption getByIdAndNotDeleted(Integer id);
	DtoResult saveExtrasOptionWithDoc(DtoDecorExtrasOption dto, List<MultipartFile> files);
	DtoResult getAll();
}
