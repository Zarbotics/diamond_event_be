package com.zbs.de.service;

import java.util.List;

import com.zbs.de.model.UserMaster;

public interface ServiceUserMaster {
	List<UserMaster> getAllActiveAdminUsersForEmail();

}
