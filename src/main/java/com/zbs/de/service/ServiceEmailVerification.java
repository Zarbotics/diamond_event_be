package com.zbs.de.service;

import com.zbs.de.model.EmailVerificationToken;
import com.zbs.de.model.UserMaster;

public interface ServiceEmailVerification {

	EmailVerificationToken createToken(UserMaster user);
	boolean verifyToken(String token);
}