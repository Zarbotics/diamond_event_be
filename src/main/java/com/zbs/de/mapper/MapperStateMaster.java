package com.zbs.de.mapper;

import com.zbs.de.model.StateMaster;
import com.zbs.de.model.dto.DtoStateMaster;

public class MapperStateMaster {
	public static DtoStateMaster toDto(StateMaster entity) {
		DtoStateMaster dto = new DtoStateMaster();
		dto.setSerStateId(entity.getSerStateId());
		dto.setTxtStateName(entity.getTxtStateName());
		dto.setTxtStateCode(entity.getTxtStateCode());
		dto.setBlnIsAvtive(entity.getBlnIsActive());
		if (entity.getCountryMaster() != null)
			dto.setSerCountryId(entity.getCountryMaster().getSerCountryId());
		return dto;
	}

	public static StateMaster toEntity(DtoStateMaster dto) {
		StateMaster entity = new StateMaster();
		entity.setSerStateId(dto.getSerStateId());
		entity.setTxtStateName(dto.getTxtStateName());
		entity.setTxtStateCode(dto.getTxtStateCode());
		entity.setBlnIsActive(dto.getBlnIsAvtive());
		return entity;
	}
}
