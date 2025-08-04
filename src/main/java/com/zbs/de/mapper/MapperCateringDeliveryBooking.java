package com.zbs.de.mapper;

import com.zbs.de.model.CateringDeliveryBooking;
import com.zbs.de.model.CateringDeliveryItemDetail;
import com.zbs.de.model.dto.DtoCateringDeliveryBooking;
import com.zbs.de.model.dto.DtoCateringDeliveryItemDetail;
import com.zbs.de.util.UtilDateAndTime;
import com.zbs.de.util.UtilRandomKey;

public class MapperCateringDeliveryBooking {
	public static DtoCateringDeliveryBooking toDto(CateringDeliveryBooking entity) {
		if (UtilRandomKey.isNull(entity)) {
			return null;
		}
		DtoCateringDeliveryBooking dto = new DtoCateringDeliveryBooking();
		dto.setSerDeliveryBookingId(entity.getSerDeliveryBookingId());
		dto.setTxtBookingStatus(entity.getTxtBookingStatus());
		dto.setTxtDeliveryBookingCode(entity.getTxtDeliveryBookingCode());
		dto.setBlnBookingStatus(entity.getBlnBookingStatus());
		dto.setBlnRequestMeeting(entity.getBlnRequestMeeting());
		if (UtilRandomKey.isNotNull(entity.getDteDeliveryDate())) {
			dto.setDteDeliveryDate(UtilDateAndTime.dateToStringddmmyyyy(entity.getDteDeliveryDate()));
		}
		dto.setTxtDeliveryLocation(entity.getTxtDeliveryLocation());
		dto.setTxtDeliveryTime(entity.getTxtDeliveryTime());
		dto.setTxtRemarks(entity.getTxtRemarks());
		dto.setTxtSpecialInstructions(entity.getTxtSpecialInstructions());

		if (entity.getCustomerMaster() != null) {
			dto.setSerCustId(entity.getCustomerMaster().getSerCustId());
			dto.setTxtCustCode(entity.getCustomerMaster().getTxtCustCode());
			dto.setTxtCustName(entity.getCustomerMaster().getTxtCustName());
		}
		if (entity.getEventType() != null) {
			dto.setSerEventTypeId(entity.getEventType().getSerEventTypeId());
			dto.setTxtEventTypeCode(entity.getEventType().getTxtEventTypeCode());
			dto.setTxtEventTypeName(entity.getEventType().getTxtEventTypeName());
		}

		return dto;
	}

	public static CateringDeliveryBooking toEntity(DtoCateringDeliveryBooking dto) {
		if (UtilRandomKey.isNull(dto)) {
			return null;
		}
		CateringDeliveryBooking entity = new CateringDeliveryBooking();
		entity.setSerDeliveryBookingId(dto.getSerDeliveryBookingId());
		entity.setTxtBookingStatus(dto.getTxtBookingStatus());
		entity.setTxtDeliveryBookingCode(dto.getTxtDeliveryBookingCode());
		entity.setBlnBookingStatus(dto.getBlnBookingStatus());
		entity.setBlnRequestMeeting(dto.getBlnRequestMeeting());
		if (UtilRandomKey.isNotNull(dto.getDteDeliveryDate())) {
			entity.setDteDeliveryDate(UtilDateAndTime.ddmmyyyyStringToDate(dto.getDteDeliveryDate()));
		}
		entity.setTxtDeliveryLocation(dto.getTxtDeliveryLocation());
		entity.setTxtDeliveryTime(dto.getTxtDeliveryTime());
		entity.setTxtRemarks(dto.getTxtRemarks());
		entity.setTxtSpecialInstructions(dto.getTxtSpecialInstructions());

		return entity;
	}

}
