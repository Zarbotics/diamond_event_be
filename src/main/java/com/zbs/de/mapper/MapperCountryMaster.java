package com.zbs.de.mapper;

import com.zbs.de.model.CountryMaster;
import com.zbs.de.model.dto.DtoCountryMaster;

public class MapperCountryMaster {
	public static DtoCountryMaster toDto(CountryMaster entity) {
		DtoCountryMaster dto = new DtoCountryMaster();
		dto.setSerCountryId(entity.getSerCountryId());
		dto.setTxtCountryCode(entity.getTxtCountryCode());
		dto.setTxtCountryName(entity.getTxtCountryName());
		dto.setBlnIsAvtive(entity.getBlnIsActive());
		dto.setShortName(entity.getShortName());
		dto.setDefaultCountry(entity.getDefaultCountry());
		return dto;
	}

	public static CountryMaster toEntity(DtoCountryMaster dto) {
		CountryMaster entity = new CountryMaster();
		entity.setSerCountryId(dto.getSerCountryId());
		entity.setTxtCountryCode(dto.getTxtCountryCode());
		entity.setTxtCountryName(dto.getTxtCountryName());
		entity.setActive(dto.getBlnIsAvtive());
		entity.setShortName(dto.getShortName());
		entity.setDefaultCountry(dto.getDefaultCountry());
		return entity;
	}
}
