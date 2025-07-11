package com.zbs.de.mapper;

import com.zbs.de.model.EventMaster;
import com.zbs.de.model.dto.DtoEventMaster;
import com.zbs.de.model.dto.DtoEventRunningOrder;
import com.zbs.de.util.UtilDateAndTime;
import com.zbs.de.util.UtilRandomKey;

public class MapperEventMaster {

	// Entity → DTO
	public static DtoEventMaster toDto(EventMaster entity) {
		if (entity == null)
			return null;

		DtoEventMaster dto = new DtoEventMaster();
		dto.setSerEventMasterId(entity.getSerEventMasterId());
		dto.setTxtEventMasterCode(entity.getTxtEventMasterCode());
		dto.setTxtEventMasterName(entity.getTxtEventMasterName());
		dto.setDteEventDate(UtilDateAndTime.dateToStringddmmyyyy(entity.getDteEventDate()));
		dto.setNumNumberOfGuests(entity.getNumNumberOfGuests());
		dto.setNumNumberOfTables(entity.getNumNumberOfTables());
		dto.setNumInfoFilledStatus(entity.getNumInfoFilledStatus());
		dto.setTxtBrideName(entity.getTxtBrideName());
		dto.setTxtGroomName(entity.getTxtGroomName());
		dto.setTxtBirthDayCelebrant(entity.getTxtBirthDayCelebrant());
		dto.setTxtAgeCategory(entity.getTxtAgeCategory());
		dto.setTxtChiefGuest(entity.getTxtChiefGuest());

		if (entity.getCustomerMaster() != null) {
			dto.setSerCustId(entity.getCustomerMaster().getSerCustId());
			dto.setTxtCustCode(entity.getCustomerMaster().getTxtCustCode());
			dto.setTxtCustName(entity.getCustomerMaster().getTxtCustName());
		}

		if (entity.getEventRunningOrder() != null) {
			DtoEventRunningOrder dtoEventRunningOrder = new DtoEventRunningOrder();
			dtoEventRunningOrder.setSerEventRunningOrderId(entity.getEventRunningOrder().getSerEventRunningOrderId());
			dtoEventRunningOrder.setTxtEventRunningOrderCode(entity.getEventRunningOrder().getTxtEventRunningOrderCode());
			dtoEventRunningOrder.setTxtBaratArrival(entity.getEventRunningOrder().getTxtBaratArrival());
			dtoEventRunningOrder.setTxtBrideEntrance(entity.getEventRunningOrder().getTxtBrideEntrance());
			dtoEventRunningOrder.setTxtEndOfNight(entity.getEventRunningOrder().getTxtEndOfNight());
			dtoEventRunningOrder.setTxtGuestArrival(entity.getEventRunningOrder().getTxtGuestArrival());
			dtoEventRunningOrder.setTxtMeal(entity.getEventRunningOrder().getTxtMeal());
			dtoEventRunningOrder.setTxtNikah(entity.getEventRunningOrder().getTxtNikah());
			dto.setDtoEventRunningOrder(dtoEventRunningOrder);
		}

		if (entity.getEventType() != null) {
			dto.setSerEventTypeId(entity.getEventType().getSerEventTypeId());
			dto.setTxtEventTypeCode(entity.getEventType().getTxtEventTypeCode());
			dto.setTxtEventTypeName(entity.getEventType().getTxtEventTypeName());
		}
		
		if(UtilRandomKey.isNotNull(entity.getVenueMaster())) {
			dto.setSerVenueMasterId(entity.getVenueMaster().getSerVenueMasterId());
			dto.setTxtVenueCode(entity.getVenueMaster().getTxtVenueCode());
			dto.setTxtVenueName(entity.getVenueMaster().getTxtVenueName());
		}
		
		if(UtilRandomKey.isNotNull(entity.getVendorMaster())) {
			dto.setSerVendorId(entity.getVendorMaster().getSerVendorId());
			dto.setTxtVendorCode(entity.getVendorMaster().getTxtVendorCode());
			dto.setTxtVendorName(entity.getVendorMaster().getTxtVendorName());
		}

		return dto;
	}

	// DTO → Entity
	public static EventMaster toEntity(DtoEventMaster dto) {
		if (dto == null)
			return null;

		EventMaster entity = new EventMaster();
		entity.setSerEventMasterId(dto.getSerEventMasterId());
		entity.setTxtEventMasterCode(dto.getTxtEventMasterCode());
		entity.setTxtEventMasterName(dto.getTxtEventMasterName());
		entity.setDteEventDate(UtilDateAndTime.ddmmyyyyStringToDate(dto.getDteEventDate()));
		entity.setNumNumberOfGuests(dto.getNumNumberOfGuests());
		entity.setNumNumberOfTables(dto.getNumNumberOfTables());
		entity.setNumInfoFilledStatus(dto.getNumInfoFilledStatus());
		entity.setTxtBrideName(dto.getTxtBrideName());
		entity.setTxtGroomName(dto.getTxtGroomName());
		entity.setTxtBirthDayCelebrant(dto.getTxtBirthDayCelebrant());
		entity.setTxtAgeCategory(dto.getTxtAgeCategory());
		entity.setTxtChiefGuest(dto.getTxtChiefGuest());

		return entity;
	}

}
