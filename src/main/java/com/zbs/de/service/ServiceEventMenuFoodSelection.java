package com.zbs.de.service;

import java.util.List;

import com.zbs.de.model.EventMenuFoodSelection;
import com.zbs.de.model.dto.DtoResult;

public interface ServiceEventMenuFoodSelection {
	List<EventMenuFoodSelection> getByEventMasterId(Integer serEventMasterId);
	DtoResult deleteByEventMasterId(Integer serEventMasterId);
	String saveAll(List<EventMenuFoodSelection> eventMenuFoodSelectionLst);

}
