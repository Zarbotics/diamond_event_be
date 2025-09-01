package com.zbs.de.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zbs.de.controller.ControllerEventType;
import com.zbs.de.model.UserMaster;
import com.zbs.de.repository.RepositoryUserMaster;
import com.zbs.de.service.ServiceUserMaster;

@Service("serviceUserMaster")
public class ServiceUserMasterImpl implements ServiceUserMaster {

	@Autowired
	private RepositoryUserMaster repositoryUserMaster;

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerEventType.class);

	@Override
	public List<UserMaster> getAllActiveAdminUsersForEmail() {
		try {
			List<UserMaster> userMasters = repositoryUserMaster.getAdminUsersForEmail();
			if (userMasters != null) {
				return userMasters;
			}
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
		}
		return null;
	}

}
