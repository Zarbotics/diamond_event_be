package com.zbs.de.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zbs.de.model.EventDecorCategorySelection;
import com.zbs.de.repository.RepositoryEventDecorCategorySelection;
import com.zbs.de.service.ServiceEventDecorCategorySelection;

@Service("serviceEventDecorCategorySelection")
public class ServiceEventDecorCategorySelectionImpl implements ServiceEventDecorCategorySelection {

	@Autowired
	RepositoryEventDecorCategorySelection repositoryEventDecorCategorySelection;

	@Override
	public void deleteByEventMasterId(Integer serEventMasterId) {
		List<EventDecorCategorySelection> selections = repositoryEventDecorCategorySelection
				.findByEventMaster_SerEventMasterId(serEventMasterId);

		for (EventDecorCategorySelection selection : selections) {
			selection.setBlnIsDeleted(true);
			repositoryEventDecorCategorySelection.save(selection);
		}
	}
}
