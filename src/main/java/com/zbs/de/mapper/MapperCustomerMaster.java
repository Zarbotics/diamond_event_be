package com.zbs.de.mapper;

import com.zbs.de.model.CustomerMaster;
import com.zbs.de.model.dto.DtoCustomerMaster;

public class MapperCustomerMaster {

	public static DtoCustomerMaster toDto(CustomerMaster entity) {
		if (entity == null)
			return null;

		DtoCustomerMaster dto = new DtoCustomerMaster();
		dto.setSerCustId(entity.getSerCustId());
		dto.setTxtAddress1(entity.getTxtAddress1());
		dto.setTxtAddress2(entity.getTxtAddress2());
		dto.setAddress3(entity.getAddress3());
		dto.setTxtCustName(entity.getTxtCustName());
		dto.setTxt_phone_number_1(entity.getTxt_phone_number_1());
		dto.setTxt_phone_number_2(entity.getTxt_phone_number_2());
		dto.setTxtEmail(entity.getTxtEmail());
		dto.setComments(entity.getComments());
		dto.setNum_longitude(entity.getNum_longitude());
		dto.setNumLatitude(entity.getNumLatitude());
		dto.setTxtGMapUrl(entity.getTxtGMapUrl());
		dto.setTxtStreetName(entity.getTxtStreetName());
		dto.setTxtBuildingName(entity.getTxtBuildingName());
		dto.setTxtBuildingNumber(entity.getTxtBuildingNumber());
		dto.setTxtPostalCode(entity.getTxtPostalCode());
		dto.setTxtDistrict(entity.getTxtDistrict());
		dto.setTxtCountryCode(entity.getTxtCountryCode());

		if (entity.getCountryMaster() != null) {
			dto.setSerCountryId(entity.getCountryMaster().getSerCountryId());
			dto.setTxtCountryName(entity.getCountryMaster().getTxtCountryName());
		}

		if (entity.getCityMaster() != null) {
			dto.setSerCityId(entity.getCityMaster().getSerCityId());
			dto.setTxtCityCode(entity.getCityMaster().getTxtCityCode());
			dto.setTxtCityName(entity.getCityMaster().getTxtCityName());
		}

		if (entity.getStateMaster() != null) {
			dto.setSerStateId(entity.getStateMaster().getSerStateId());
			dto.setTxtStateCode(entity.getStateMaster().getTxtStateCode());
			dto.setTxtStateName(entity.getStateMaster().getTxtStateName());
		}

		// From BaseEntity
		dto.setBlnIsActive(entity.getBlnIsActive());

		return dto;
	}

	public static CustomerMaster toEntity(DtoCustomerMaster dto) {
		if (dto == null)
			return null;

		CustomerMaster entity = new CustomerMaster();
		entity.setSerCustId(dto.getSerCustId());
		entity.setTxtAddress1(dto.getTxtAddress1());
		entity.setTxtAddress2(dto.getTxtAddress2());
		entity.setAddress3(dto.getAddress3());
		entity.setTxtCustName(dto.getTxtCustName());
		entity.setTxt_phone_number_1(dto.getTxt_phone_number_1());
		entity.setTxt_phone_number_2(dto.getTxt_phone_number_2());
		entity.setTxtEmail(dto.getTxtEmail());
		entity.setComments(dto.getComments());
		entity.setNum_longitude(dto.getNum_longitude());
		entity.setNumLatitude(dto.getNumLatitude());
		entity.setTxtGMapUrl(dto.getTxtGMapUrl());
		entity.setTxtStreetName(dto.getTxtStreetName());
		entity.setTxtBuildingName(dto.getTxtBuildingName());
		entity.setTxtBuildingNumber(dto.getTxtBuildingNumber());
		entity.setTxtPostalCode(dto.getTxtPostalCode());
		entity.setTxtDistrict(dto.getTxtDistrict());
		entity.setTxtCountryCode(dto.getTxtCountryCode());

		// From BaseEntity
		entity.setBlnIsActive(dto.getBlnIsActive());

		return entity;
	}

}
