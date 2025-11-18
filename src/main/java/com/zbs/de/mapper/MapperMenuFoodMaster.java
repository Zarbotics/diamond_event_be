package com.zbs.de.mapper;

import com.zbs.de.model.MenuFoodMaster;
import com.zbs.de.model.dto.DtoMenuFoodMaster;

public class MapperMenuFoodMaster {
	public static DtoMenuFoodMaster toDto(MenuFoodMaster entity) {
		DtoMenuFoodMaster dto = new DtoMenuFoodMaster();
		dto.setSerMenuFoodId(entity.getSerMenuFoodId());
		dto.setTxtMenuFoodCode(entity.getTxtMenuFoodCode());
		dto.setTxtMenuFoodName(entity.getTxtMenuFoodName());
		dto.setBlnIsMainCourse(entity.getBlnIsMainCourse());
		dto.setBlnIsAppetiser(entity.getBlnIsAppetiser());
		dto.setBlnIsStarter(entity.getBlnIsStarter());
		dto.setBlnIsSaladAndCondiment(entity.getBlnIsSaladAndCondiment());
		dto.setBlnIsDessert(entity.getBlnIsDessert());
		dto.setBlnIsDrink(entity.getBlnIsDrink());
		dto.setBlnIsActive(entity.getBlnIsActive());
		return dto;
	}

	public static MenuFoodMaster toEntity(DtoMenuFoodMaster dto) {
		MenuFoodMaster entity = new MenuFoodMaster();
		entity.setSerMenuFoodId(dto.getSerMenuFoodId());
		entity.setTxtMenuFoodCode(dto.getTxtMenuFoodCode());
		entity.setTxtMenuFoodName(dto.getTxtMenuFoodName());
		entity.setBlnIsMainCourse(dto.getBlnIsMainCourse());
		entity.setBlnIsAppetiser(dto.getBlnIsAppetiser());
		entity.setBlnIsStarter(dto.getBlnIsStarter());
		entity.setBlnIsSaladAndCondiment(dto.getBlnIsSaladAndCondiment());
		entity.setBlnIsDessert(dto.getBlnIsDessert());
		entity.setBlnIsDrink(dto.getBlnIsDrink());
		entity.setBlnIsActive(dto.getBlnIsActive());
		return entity;
	}
}