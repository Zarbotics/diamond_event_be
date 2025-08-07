package com.zbs.de.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.zbs.de.model.DecorExtrasOption;
import com.zbs.de.repository.RepositoryDecorExtrasOption;
import com.zbs.de.service.ServiceDecorExtrasOption;

public class ServiceDecorExtrasOptionImpl implements ServiceDecorExtrasOption {

	@Autowired
	private RepositoryDecorExtrasOption repositoryDecorExtrasOption;

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceEventMasterImpl.class);

	@Override
	public DecorExtrasOption getByIdAndNotDeleted(Integer id) {
		try {
			return repositoryDecorExtrasOption.findByIdAndNotDeleted(id).orElse(null);
		} catch (Exception e) {
			LOGGER.debug("Failed to get DecorExtrasOption by ID {}: {}", id, e.getMessage());
			return null;
		}
	}

}
