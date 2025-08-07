package com.zbs.de.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.RefreshToken;
import com.zbs.de.model.UserMaster;

@Repository("repositoryRefreshToken")
public interface RepositoryRefreshToken extends JpaRepository<RefreshToken, Long> {
	Optional<RefreshToken> findByToken(String token);

	int deleteByUser(UserMaster user);
}