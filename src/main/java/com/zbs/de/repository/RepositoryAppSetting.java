package com.zbs.de.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.AppSetting;

@Repository("repositoryAppSetting")
public interface RepositoryAppSetting extends JpaRepository<AppSetting, Integer> {

	/**
	 * One setting by key.
	 *
	 * <p>
	 * Inactive rows are returned deliberately. Deactivating a setting must not
	 * silently change the behaviour it controls — if something reads
	 * {@code booking.horizon.months} and the row is switched off, the honest
	 * outcome is that the stored value still applies, not that the horizon
	 * quietly becomes unlimited.
	 */
	@Query("SELECT s FROM AppSetting s WHERE s.blnIsDeleted = false AND s.txtKey = :key")
	Optional<AppSetting> findByKey(@Param("key") String key);

	/** Everything the office maintains, grouped as the screen draws it. */
	@Query("SELECT s FROM AppSetting s WHERE s.blnIsDeleted = false "
			+ "ORDER BY s.txtGroup ASC NULLS LAST, s.numDisplayOrder ASC NULLS LAST, s.txtLabel ASC")
	List<AppSetting> findAllForOffice();

	/**
	 * What a customer-facing caller may read.
	 *
	 * <p>
	 * A separate query rather than a filter applied afterwards, so that adding
	 * a private setting cannot start leaking it because somebody reused the
	 * office's list in a public endpoint.
	 */
	@Query("SELECT s FROM AppSetting s WHERE s.blnIsDeleted = false AND s.blnIsPublic = true "
			+ "ORDER BY s.txtKey ASC")
	List<AppSetting> findPublic();
}
