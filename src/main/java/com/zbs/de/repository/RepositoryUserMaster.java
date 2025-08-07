package com.zbs.de.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.UserMaster;

import java.util.Optional;

@Repository("repositoryUserMaster")
public interface RepositoryUserMaster extends JpaRepository<UserMaster, Long> {
	Optional<UserMaster> findByTxtGoogleId(String googleId);

	Optional<UserMaster> findByTxtEmail(String email);
}