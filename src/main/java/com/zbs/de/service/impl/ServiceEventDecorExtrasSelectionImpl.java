package com.zbs.de.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zbs.de.model.EventDecorExtrasSelection;
import com.zbs.de.repository.RepositoryEventDecorExtrasSelection;
import com.zbs.de.service.ServiceEventDecorExtrasSelection;

@Service("serviceEventDecorExtrasSelectionImpl")
public class ServiceEventDecorExtrasSelectionImpl implements ServiceEventDecorExtrasSelection {

	@Autowired
	RepositoryEventDecorExtrasSelection repositoryEventDecorExtrasSelection;

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceEventMasterImpl.class);

	@Override
	public String deleteByEventMasterId(Integer id) {
		try {
			repositoryEventDecorExtrasSelection.deleteByEventMasterId(id);
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

}
