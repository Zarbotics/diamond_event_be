package com.zbs.de.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.UserMaster;

import java.util.List;
import java.util.Optional;

@Repository("repositoryUserMaster")
public interface RepositoryUserMaster extends JpaRepository<UserMaster, Long> {
	Optional<UserMaster> findByTxtGoogleId(String googleId);

	Optional<UserMaster> findByTxtEmail(String email);

	@Query("SELECT u FROM UserMaster u WHERE LOWER(u.txtRole) = 'role_admin' AND u.blnIsDeleted = false and u.blnIsActive = true and u.blnReceiveEmail = true")
	List<UserMaster> getAdminUsersForEmail();
}