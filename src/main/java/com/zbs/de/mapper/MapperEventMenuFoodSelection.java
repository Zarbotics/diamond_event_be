package com.zbs.de.mapper;

import com.zbs.de.model.EventMenuFoodSelection;
import com.zbs.de.model.dto.DtoEventMenuFoodSelection;
import com.zbs.de.util.UtilRandomKey;

public class MapperEventMenuFoodSelection {

	public static DtoEventMenuFoodSelection toDto(EventMenuFoodSelection entity) {
		if (UtilRandomKey.isNull(entity)) {
			return null;
		}
		DtoEventMenuFoodSelection dto = new DtoEventMenuFoodSelection();
		dto.setSerEventMenuFoodId(entity.getSerEventMenuFoodId());
		if (UtilRandomKey.isNotNull(entity.getEventMaster())) {
			dto.setSerEventMasterId(entity.getEventMaster().getSerEventMasterId());
			dto.setTxtEventMasterCode(entity.getEventMaster().getTxtEventMasterCode());
			dto.setTxtEventMasterName(entity.getEventMaster().getTxtEventMasterName());
		}
		if (UtilRandomKey.isNotNull(entity.getMenuFoodMaster())) {
			dto.setSerMenuFoodId(entity.getMenuFoodMaster().getSerMenuFoodId());
			dto.setTxtMenuFoodCode(entity.getMenuFoodMaster().getTxtMenuFoodCode());
			dto.setTxtMenuFoodName(entity.getMenuFoodMaster().getTxtMenuFoodName());
		}

		return dto;
	}

}
