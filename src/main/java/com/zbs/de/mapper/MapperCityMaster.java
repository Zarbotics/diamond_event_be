package com.zbs.de.mapper;

import com.zbs.de.model.CityMaster;
import com.zbs.de.model.dto.DtoCityMaster;

public class MapperCityMaster {

	public static DtoCityMaster toDto(CityMaster entity) {
		DtoCityMaster dto = new DtoCityMaster();
		dto.setSerCityId(entity.getSerCityId());
		dto.setTxtCityCode(entity.getTxtCityCode());
		dto.setTxtCityName(entity.getTxtCityName());
		dto.setBlnIsActive(entity.getBlnIsActive());
		if (entity.getStateMaster() != null) {
			dto.setSerStateId(entity.getStateMaster().getSerStateId());
		}
		return dto;
	}

	public static CityMaster toEntity(DtoCityMaster dto) {
		CityMaster entity = new CityMaster();
		entity.setSerCityId(dto.getSerCityId());
		entity.setTxtCityCode(dto.getTxtCityCode());
		entity.setTxtCityName(dto.getTxtCityName());
		entity.setBlnIsActive(dto.getBlnIsActive());
		return entity;
	}
}
