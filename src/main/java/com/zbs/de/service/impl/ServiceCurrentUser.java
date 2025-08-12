package com.zbs.de.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.zbs.de.controller.ControllerCustomerMaster;
import com.zbs.de.model.UserMaster;

public class ServiceCurrentUser {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerCustomerMaster.class);

	public static UserMaster getCurrentUser() {

		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			if (authentication != null && authentication.isAuthenticated()
					&& authentication.getPrincipal() instanceof UserMaster) {
				return (UserMaster) authentication.getPrincipal();
			}
			throw new Exception("No authenticated user found");
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			return null;
		}

	}

	public static Integer getCurrentUserId() {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			if (authentication != null && authentication.isAuthenticated()
					&& authentication.getPrincipal() instanceof UserMaster) {
				UserMaster user = (UserMaster) authentication.getPrincipal();
				if (user != null) {
					return user.getSerUserId().intValue();
				} else {
					return null;
				}
			}
			throw new Exception("No authenticated user found");
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			return null;
		}

	}

	public static String getCurrentUserEmail() {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			if (authentication != null && authentication.isAuthenticated()
					&& authentication.getPrincipal() instanceof UserMaster) {
				UserMaster user = (UserMaster) authentication.getPrincipal();
				if (user != null) {
					return user.getTxtEmail();
				} else {
					return null;
				}
			}
			throw new Exception("No authenticated user found");
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			return null;
		}

	}


}
