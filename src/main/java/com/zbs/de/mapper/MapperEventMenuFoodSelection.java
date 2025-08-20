package com.zbs.de.mapper;

import com.zbs.de.model.EventMenuFoodSelection;
import com.zbs.de.model.MenuFoodMaster;
import com.zbs.de.model.dto.DtoEventMenuFoodSelection;
import com.zbs.de.util.UtilRandomKey;

public class MapperEventMenuFoodSelection {

	public static DtoEventMenuFoodSelection toDto(EventMenuFoodSelection entity) {
		if (UtilRandomKey.isNull(entity)) {
			return null;
		}
		DtoEventMenuFoodSelection dto = new DtoEventMenuFoodSelection();
		dto.setSerEventMenuFoodId(entity.getSerEventMenuFoodId());
		dto.setBlnIsActive(entity.getBlnIsActive());
		dto.setTxtFoodType(entity.getTxtFoodType());
		if (UtilRandomKey.isNotNull(entity.getEventMaster())) {
			dto.setSerEventMasterId(entity.getEventMaster().getSerEventMasterId());
			dto.setTxtEventMasterCode(entity.getEventMaster().getTxtEventMasterCode());
			dto.setTxtEventMasterName(entity.getEventMaster().getTxtEventMasterName());
		}
		if (UtilRandomKey.isNotNull(entity.getMenuFoodMaster())) {
			dto.setSerMenuFoodId(entity.getMenuFoodMaster().getSerMenuFoodId());
			dto.setTxtMenuFoodCode(entity.getMenuFoodMaster().getTxtMenuFoodCode());
			dto.setTxtMenuFoodName(entity.getMenuFoodMaster().getTxtMenuFoodName());
			dto.setTxtFoodType(getFoodName(entity.getMenuFoodMaster()));
		}

		return dto;
	}
	
	private static String getFoodName(MenuFoodMaster food) {
		if(food.getBlnIsAppetiser() != null && food.getBlnIsAppetiser()) {
			return "Appetizers";
		}else if(food.getBlnIsDessert() != null && food.getBlnIsDessert()) {
			return "Desserts";
		}else if(food.getBlnIsStarter() != null && food.getBlnIsStarter()) {
			return "Starters & Main course";
		}else if(food.getBlnIsSaladAndCondiment() != null && food.getBlnIsSaladAndCondiment()) {
			return "Salad & Condiments";
		}else if(food.getBlnIsDrink() != null && food.getBlnIsDrink()) {
			return "Reception Drinks";
		}else if(food.getBlnIsAppetiser() != null && food.getBlnIsAppetiser()) {
			
		}else if(food.getBlnIsMainCourse() != null && food.getBlnIsMainCourse()) {
			return "Mains";
		}
		return null;
	}

}
