package com.zbs.de.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.zbs.de.model.EventDecorCategorySelection;
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
		dto.setTxtNumberOfGuests(entity.getTxtNumberOfGuests());
		dto.setTxtOtherEventType(entity.getTxtOtherEventType());
		dto.setBlnIsActive(entity.getBlnIsActive());
		dto.setTxtEventStatus(entity.getTxtEventStatus());


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
		entity.setTxtNNumberOfGuests(dto.getTxtNumberOfGuests());
		entity.setTxtOtherEventType(dto.getTxtOtherEventType());
		entity.setTxtEventStatus(dto.getTxtEventStatus());
		if (dto.getDtoEventDecorSelections() != null) {
		    List<EventDecorCategorySelection> decorSelections = dto.getDtoEventDecorSelections().stream()
		        .map(MapperEventDecorCategorySelection::toEntity)
		        .collect(Collectors.toList());

		    decorSelections.forEach(d -> d.setEventMaster(entity));
		    entity.setDecorSelections(decorSelections);
		}

		return entity;
	}

}
