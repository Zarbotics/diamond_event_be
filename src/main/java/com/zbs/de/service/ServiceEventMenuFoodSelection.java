package com.zbs.de.service;

import java.util.List;

import com.zbs.de.model.EventMenuFoodSelection;

public interface ServiceEventMenuFoodSelection {
	List<EventMenuFoodSelection> getByEventMasterId(Integer serEventMasterId);

}
