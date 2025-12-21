package com.zbs.de.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.zbs.de.model.dto.DtoMenuItemPrice;
import com.zbs.de.model.MenuItemPrice;
import com.zbs.de.util.PriceConstants;
import com.zbs.de.util.UtilDateAndTime;

@Component
public class MapperMenuItemPrice {

	public DtoMenuItemPrice toDto(MenuItemPrice entity) {
		if (entity == null) {
			return null;
		}

		DtoMenuItemPrice dto = new DtoMenuItemPrice();
		dto.setSerPriceId(entity.getSerPriceId());

		// Menu Item info
		if (entity.getMenuItem() != null) {
			dto.setSerMenuItemId(entity.getMenuItem().getSerMenuItemId());
			dto.setMenuItemCode(entity.getMenuItem().getTxtCode());
			dto.setMenuItemName(entity.getMenuItem().getTxtName());
			dto.setMenuItemRole(entity.getMenuItem().getTxtRole());
			dto.setMenuItemType(entity.getMenuItem().getTxtType());
		}

		// Price Version info
		if (entity.getPriceVersion() != null) {
			dto.setSerPriceVersionId(entity.getPriceVersion().getSerPriceVersionId());
			dto.setPriceVersionCode(entity.getPriceVersion().getTxtVersionCode());
			dto.setPriceVersionName(entity.getPriceVersion().getTxtName());
		}

		// Price details
		dto.setNumBasePrice(entity.getNumBasePrice());
		dto.setTxtCurrency(entity.getTxtCurrency() != null ? entity.getTxtCurrency() : PriceConstants.DEFAULT_CURRENCY);
		dto.setTxtPriceModel(entity.getTxtPriceModel());
		dto.setTxtUnitType(entity.getTxtUnitType());

		// Quantity settings
		dto.setNumMinQuantity(
				entity.getNumMinQuantity() != null ? entity.getNumMinQuantity() : PriceConstants.DEFAULT_MIN_QUANTITY);
		dto.setNumMaxQuantity(entity.getNumMaxQuantity());

		// Guest settings
		dto.setNumMinGuests(entity.getNumMinGuests());
		dto.setNumMaxGuests(entity.getNumMaxGuests());

		// Time settings
		dto.setNumMinHours(entity.getNumMinHours());
		dto.setNumMaxHours(entity.getNumMaxHours());

		// Validity
		dto.setDteValidFrom(UtilDateAndTime.mmddyyyyDateToString(entity.getDteValidFrom()));
		dto.setDteValidTo(UtilDateAndTime.mmddyyyyDateToString(entity.getDteValidTo()));

		// Status
		dto.setBlnIsActive(entity.getBlnIsActive() != null ? entity.getBlnIsActive() : true);
		dto.setBlnIsDefault(entity.getBlnIsDefault() != null ? entity.getBlnIsDefault() : false);

		// Configuration
		dto.setPriceConfig(entity.getPriceConfig());

		// Calculate display price
		dto.setPriceDisplay(generatePriceDisplay(entity));

		return dto;
	}

	public MenuItemPrice toEntity(DtoMenuItemPrice dto) {
		if (dto == null) {
			return null;
		}

		MenuItemPrice entity = new MenuItemPrice();
		entity.setSerPriceId(dto.getSerPriceId());
		entity.setNumBasePrice(dto.getNumBasePrice());
		entity.setTxtCurrency(dto.getTxtCurrency() != null ? dto.getTxtCurrency() : PriceConstants.DEFAULT_CURRENCY);
		entity.setTxtPriceModel(dto.getTxtPriceModel());
		entity.setTxtUnitType(dto.getTxtUnitType());
		entity.setNumMinQuantity(
				dto.getNumMinQuantity() != null ? dto.getNumMinQuantity() : PriceConstants.DEFAULT_MIN_QUANTITY);
		entity.setNumMaxQuantity(dto.getNumMaxQuantity());
		entity.setNumMinGuests(dto.getNumMinGuests());
		entity.setNumMaxGuests(dto.getNumMaxGuests());
		entity.setNumMinHours(dto.getNumMinHours());
		entity.setNumMaxHours(dto.getNumMaxHours());
		entity.setDteValidFrom(UtilDateAndTime.ddMMyyyyDashedStringToDate(dto.getDteValidFrom()));
		entity.setDteValidTo(UtilDateAndTime.ddMMyyyyDashedStringToDate(dto.getDteValidTo()));
		entity.setBlnIsActive(dto.getBlnIsActive() != null ? dto.getBlnIsActive() : true);
		entity.setBlnIsDefault(dto.getBlnIsDefault() != null ? dto.getBlnIsDefault() : false);
		entity.setPriceConfig(dto.getPriceConfig());

		return entity;
	}

	public void updateEntityFromDto(DtoMenuItemPrice dto, MenuItemPrice entity) {
		if (dto == null || entity == null) {
			return;
		}

		if (dto.getNumBasePrice() != null) {
			entity.setNumBasePrice(dto.getNumBasePrice());
		}

		if (dto.getTxtCurrency() != null) {
			entity.setTxtCurrency(dto.getTxtCurrency());
		}

		if (dto.getTxtPriceModel() != null) {
			entity.setTxtPriceModel(dto.getTxtPriceModel());
		}

		if (dto.getTxtUnitType() != null) {
			entity.setTxtUnitType(dto.getTxtUnitType());
		}

		if (dto.getNumMinQuantity() != null) {
			entity.setNumMinQuantity(dto.getNumMinQuantity());
		}

		if (dto.getNumMaxQuantity() != null) {
			entity.setNumMaxQuantity(dto.getNumMaxQuantity());
		}

		if (dto.getNumMinGuests() != null) {
			entity.setNumMinGuests(dto.getNumMinGuests());
		}

		if (dto.getNumMaxGuests() != null) {
			entity.setNumMaxGuests(dto.getNumMaxGuests());
		}

		if (dto.getNumMinHours() != null) {
			entity.setNumMinHours(dto.getNumMinHours());
		}

		if (dto.getNumMaxHours() != null) {
			entity.setNumMaxHours(dto.getNumMaxHours());
		}

		if (dto.getDteValidFrom() != null) {
			entity.setDteValidFrom(UtilDateAndTime.ddMMyyyyDashedStringToDate(dto.getDteValidFrom()));
		}

		if (dto.getDteValidTo() != null) {
			entity.setDteValidTo(UtilDateAndTime.ddMMyyyyDashedStringToDate(dto.getDteValidTo()));
		}

		if (dto.getBlnIsActive() != null) {
			entity.setBlnIsActive(dto.getBlnIsActive());
		}

		if (dto.getBlnIsDefault() != null) {
			entity.setBlnIsDefault(dto.getBlnIsDefault());
		}

		if (dto.getPriceConfig() != null) {
			entity.setPriceConfig(dto.getPriceConfig());
		}
	}

	public List<DtoMenuItemPrice> toDtoList(List<MenuItemPrice> entities) {
		if (entities == null) {
			return new ArrayList<>();
		}

		return entities.stream().map(this::toDto).collect(Collectors.toList());
	}

	public List<MenuItemPrice> toEntityList(List<DtoMenuItemPrice> dtos) {
		if (dtos == null) {
			return new ArrayList<>();
		}

		return dtos.stream().map(this::toEntity).collect(Collectors.toList());
	}

	private String generatePriceDisplay(MenuItemPrice entity) {
		if (entity.getNumBasePrice() == null) {
			return "Price not set";
		}

		StringBuilder display = new StringBuilder();
		display.append(entity.getTxtCurrency()).append(" ").append(entity.getNumBasePrice());

		if (PriceConstants.UNIT_PER_GUEST.equals(entity.getTxtUnitType())) {
			display.append(" per guest");
		} else if (PriceConstants.UNIT_PER_ITEM.equals(entity.getTxtUnitType())) {
			display.append(" per item");
		} else if (PriceConstants.UNIT_PER_HOUR.equals(entity.getTxtUnitType())) {
			display.append(" per hour");
		} else if (PriceConstants.UNIT_FLAT.equals(entity.getTxtUnitType())) {
			display.append(" flat fee");
		}

		if (entity.getNumMinGuests() != null || entity.getNumMaxGuests() != null) {
			display.append(" (");
			if (entity.getNumMinGuests() != null) {
				display.append("min ").append(entity.getNumMinGuests()).append(" guests");
				if (entity.getNumMaxGuests() != null) {
					display.append(", ");
				}
			}
			if (entity.getNumMaxGuests() != null) {
				display.append("max ").append(entity.getNumMaxGuests()).append(" guests");
			}
			display.append(")");
		}

		return display.toString();
	}
}