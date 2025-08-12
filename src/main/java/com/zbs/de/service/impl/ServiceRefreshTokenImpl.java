package com.zbs.de.service.impl;

import com.zbs.de.model.RefreshToken;
import com.zbs.de.model.UserMaster;
import com.zbs.de.repository.RepositoryRefreshToken;
import com.zbs.de.repository.RepositoryUserMaster;
import com.zbs.de.service.ServiceRefreshToken;
import com.zbs.de.util.UtilDateAndTime;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service("serviceRefreshTokenImpl")
public class ServiceRefreshTokenImpl implements ServiceRefreshToken {
	@Value("${app.jwt.refresh-expiration}")
	private long refreshTokenDurationMs;

	private final RepositoryRefreshToken refreshTokenRepository;
	private final RepositoryUserMaster userRepository;

	public ServiceRefreshTokenImpl(RepositoryRefreshToken refreshTokenRepository, RepositoryUserMaster userRepository) {
		this.refreshTokenRepository = refreshTokenRepository;
		this.userRepository = userRepository;
	}

	@Override
	public Optional<RefreshToken> findByToken(String token) {
		return refreshTokenRepository.findByToken(token);
	}
//
//	@Override
//	public RefreshToken createRefreshToken(UserMaster user) {
//		RefreshToken refreshToken = new RefreshToken();
//		refreshToken.setUser(user);
//		refreshToken.setExpiryDate(java.util.Date.from(Instant.now().plusMillis(refreshTokenDurationMs)));
//		refreshToken.setToken(UUID.randomUUID().toString());
//		return refreshTokenRepository.save(refreshToken);
//	}

	@Override
	public RefreshToken createRefreshToken(UserMaster user) {
		Optional<RefreshToken> existingToken = refreshTokenRepository.findByUser_SerUserId(user.getSerUserId());
	    Date expiryDate = new Date(System.currentTimeMillis() + refreshTokenDurationMs);
		if (existingToken.isPresent()) {
			RefreshToken token = existingToken.get();
			token.setToken(UUID.randomUUID().toString());
			token.setExpiryDate(expiryDate);
			return refreshTokenRepository.save(token);
		}

		RefreshToken newToken = new RefreshToken();
		newToken.setUser(user);
		newToken.setToken(UUID.randomUUID().toString());
		newToken.setExpiryDate(expiryDate);
		return refreshTokenRepository.save(newToken);
	}

	@Override
	public RefreshToken verifyExpiration(RefreshToken token) {
		if (token.getExpiryDate().before(new java.util.Date())) {
			refreshTokenRepository.delete(token);
			throw new RuntimeException("Refresh token was expired. Please login again.");
		}
		return token;
	}

	@Override
	@Transactional
	public int deleteByUserId(Long userId) {
		UserMaster user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
		return refreshTokenRepository.deleteByUser(user);
	}
}
