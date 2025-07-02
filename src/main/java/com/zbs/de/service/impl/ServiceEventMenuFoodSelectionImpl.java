package com.zbs.de.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zbs.de.model.EventMenuFoodSelection;
import com.zbs.de.repository.RepositoryEventMenuFoodSelection;
import com.zbs.de.service.ServiceEventMenuFoodSelection;

@Service("serviceEventMenuFoodSelectionImpl")
public class ServiceEventMenuFoodSelectionImpl implements ServiceEventMenuFoodSelection {

	@Autowired
	private RepositoryEventMenuFoodSelection repositoryEventMenuFoodSelection;

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceEventMasterImpl.class);

	@Override
	public List<EventMenuFoodSelection> getByEventMasterId(Integer serEventMasterId) {
		try {
			List<EventMenuFoodSelection> eventMenuFoodSelections = repositoryEventMenuFoodSelection
					.findByEventId(serEventMasterId);
			return eventMenuFoodSelections;
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			return null;
		}
	}

}
