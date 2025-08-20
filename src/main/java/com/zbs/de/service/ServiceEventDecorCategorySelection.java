package com.zbs.de.service;

import java.util.List;

import com.zbs.de.model.dto.DtoEventDecorCategorySelection;

public interface ServiceEventDecorCategorySelection {
	void deleteByEventMasterId(Integer serEventMasterId);
	List<DtoEventDecorCategorySelection> getSelectionsWithChosenValues(Integer eventMasterId);

}
