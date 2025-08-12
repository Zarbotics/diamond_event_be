package com.zbs.de.repository;

import com.zbs.de.model.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("repositoryEmailVerificationToken")
public interface RepositoryEmailVerificationToken extends JpaRepository<EmailVerificationToken, Long> {
	Optional<EmailVerificationToken> findByToken(String token);
}