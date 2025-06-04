package com.zbs.de.mapper;

import com.zbs.de.model.VendorMaster;
import com.zbs.de.model.dto.DtoVendorMaster;

public class MapperVendorMaster {
	public static DtoVendorMaster toDto(VendorMaster entity) {
		DtoVendorMaster dto = new DtoVendorMaster();
		dto.setSerVendorId(entity.getSerVendorId());
		dto.setTxtVendorCode(entity.getTxtVendorCode());
		dto.setTxtVendorName(entity.getTxtVendorName());
		dto.setEnmVendorType(entity.getEnmVendorType());
		dto.setTxtAddress(entity.getTxtAddress());
		dto.setTxtPhoneNumber(entity.getTxtPhoneNumber());
		return dto;
	}

	public static VendorMaster toEntity(DtoVendorMaster dto) {
		VendorMaster entity = new VendorMaster();
		entity.setSerVendorId(dto.getSerVendorId());
		entity.setTxtVendorCode(dto.getTxtVendorCode());
		entity.setTxtVendorName(dto.getTxtVendorName());
		entity.setEnmVendorType(dto.getEnmVendorType());
		entity.setTxtAddress(dto.getTxtAddress());
		entity.setTxtPhoneNumber(dto.getTxtPhoneNumber());
		return entity;
	}
}