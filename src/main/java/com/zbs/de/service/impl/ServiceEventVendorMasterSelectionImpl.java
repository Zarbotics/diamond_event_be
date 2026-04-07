package com.zbs.de.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zbs.de.model.EventVendorMasterSelection;
import com.zbs.de.repository.RepositoryEventVendorMasterSelection;
import com.zbs.de.service.ServiceEventVendorMasterSelection;

@Service("serviceEventVendorMasterSelectionImpl")
public class ServiceEventVendorMasterSelectionImpl implements ServiceEventVendorMasterSelection {

	@Autowired
	private RepositoryEventVendorMasterSelection repositoryEventVendorMasterSelection;

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceEventVendorMasterSelectionImpl.class);

	@Override
	public List<EventVendorMasterSelection> getByEventMasterId(Integer serEventMasterId) {
		try {
			List<EventVendorMasterSelection> eventVendorMasterSelections = repositoryEventVendorMasterSelection
					.findByEventId(serEventMasterId);
			return eventVendorMasterSelections;
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			return null;
		}
	}

}