package com.zbs.de.service;

import com.zbs.de.model.CityMaster;
import com.zbs.de.model.dto.DtoCityMaster;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.util.ResponseMessage;

public interface ServiceCityMaster {

	ResponseMessage getAllData();

	ResponseMessage saveAndUpdate(DtoCityMaster dtoCityMaster);

	ResponseMessage getById(Integer serCityId);

	CityMaster getByPK(Integer serCityId);

	DtoResult deleteById(Integer id);
}
