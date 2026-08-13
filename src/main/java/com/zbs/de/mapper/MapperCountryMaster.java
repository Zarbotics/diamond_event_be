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
		// The DTO field is a Boolean and a request body need not carry it, so
		// passing it straight into the primitive setter unboxed a null and threw
		// NullPointerException on any payload that simply omitted the flag.
		entity.setActive(Boolean.TRUE.equals(dto.getBlnIsAvtive()));
		// Keep the two active flags on this table in step. They are the same fact
		// stored twice — see CountryMaster.isActive — and letting them diverge is
		// how the venue list ended up filtering on one while the journey displayed
		// the other.
		entity.setBlnIsActive(Boolean.TRUE.equals(dto.getBlnIsAvtive()));
		entity.setShortName(dto.getShortName());
		entity.setDefaultCountry(dto.getDefaultCountry());
		return entity;
	}
}
