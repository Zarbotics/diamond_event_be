package com.zbs.de.service;

import java.util.Optional;

import com.zbs.de.model.RefreshToken;
import com.zbs.de.model.UserMaster;

public interface ServiceRefreshToken {
	Optional<RefreshToken> findByToken(String token);

	RefreshToken createRefreshToken(UserMaster user);

	RefreshToken verifyExpiration(RefreshToken token);

	int deleteByUserId(Long userId);

}
