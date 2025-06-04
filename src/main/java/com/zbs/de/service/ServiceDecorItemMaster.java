package com.zbs.de.service;

import java.util.List;

import com.zbs.de.model.dto.DtoDecorItemMaster;
import com.zbs.de.util.ResponseMessage;

public interface ServiceDecorItemMaster {
	List<DtoDecorItemMaster> getAllData();

	ResponseMessage saveAndUpdate(DtoDecorItemMaster dto);

	ResponseMessage getById(Integer id);
}