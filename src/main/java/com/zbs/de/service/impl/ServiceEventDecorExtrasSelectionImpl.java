package com.zbs.de.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zbs.de.model.EventDecorExtrasSelection;
import com.zbs.de.model.EventMenuFoodSelection;
import com.zbs.de.repository.RepositoryDecorExtrasOption;
import com.zbs.de.repository.RepositoryEventDecorExtrasSelection;
import com.zbs.de.service.ServiceEventDecorExtrasSelection;

@Service("serviceEventDecorExtrasSelectionImpl")
public class ServiceEventDecorExtrasSelectionImpl implements ServiceEventDecorExtrasSelection {

    private final RepositoryDecorExtrasOption repositoryDecorExtrasOption;

	@Autowired
	RepositoryEventDecorExtrasSelection repositoryEventDecorExtrasSelection;

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceEventMasterImpl.class);

    ServiceEventDecorExtrasSelectionImpl(RepositoryDecorExtrasOption repositoryDecorExtrasOption) {
        this.repositoryDecorExtrasOption = repositoryDecorExtrasOption;
    }

	@Override
	public String deleteByEventMasterId(Integer id) {
		try {
			List<EventDecorExtrasSelection>  eventDecorExtrasSelectionLst= repositoryEventDecorExtrasSelection.findByEventMaster_SerEventMasterId(id);
			
			for(EventDecorExtrasSelection selection : eventDecorExtrasSelectionLst) {
				selection.setBlnIsDeleted(true);
				selection.setBlnIsApproved(false);
				repositoryEventDecorExtrasSelection.save(selection);
			}
			return "Success";
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			return e.getMessage();
		}

	}

	@Override
	public EventDecorExtrasSelection save(EventDecorExtrasSelection eventDecorExtrasSelection) {
		try {
			return repositoryEventDecorExtrasSelection.save(eventDecorExtrasSelection);

		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			return null;
		}

	}
	

	@Override
	public List<EventDecorExtrasSelection> getByEventMasterId(Integer serEventMasterId) {
		try {
			List<EventDecorExtrasSelection> eventDecorExtrasSelection = repositoryEventDecorExtrasSelection
					.findByEventId(serEventMasterId);
			return eventDecorExtrasSelection;
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			return null;
		}
	}

}
