package com.zbs.de.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.SsoHandoffCode;

@Repository
public interface RepositorySsoHandoffCode extends JpaRepository<SsoHandoffCode, Long> {

	Optional<SsoHandoffCode> findByCodeHash(String codeHash);

	/**
	 * Consumes a code, returning the number of rows actually deleted.
	 *
	 * <p>
	 * The delete is the claim. Two tabs — or an attacker replaying a code at the
	 * same moment as the real browser — race on this single statement, and the
	 * database decides: exactly one caller sees a count of 1 and gets the tokens,
	 * every other caller sees 0 and is refused. Checking existence first and
	 * deleting afterwards would leave a window where both callers succeed.
	 */
	@Modifying
	@Query("DELETE FROM SsoHandoffCode c WHERE c.codeHash = :codeHash")
	int deleteByCodeHash(@Param("codeHash") String codeHash);

	/** Housekeeping for codes that were issued but never exchanged. */
	@Modifying
	@Query("DELETE FROM SsoHandoffCode c WHERE c.expiresAt < :cutoff")
	int deleteExpired(@Param("cutoff") Instant cutoff);
}
