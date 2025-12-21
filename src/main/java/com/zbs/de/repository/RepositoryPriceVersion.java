package com.zbs.de.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.PriceVersion;

@Repository
public interface RepositoryPriceVersion extends JpaRepository<PriceVersion, Long> {

	// Find non-deleted versions
	List<PriceVersion> findByBlnIsDeletedFalseOrderByNumPriorityDescSerPriceVersionIdDesc();

	// Find active versions
	List<PriceVersion> findByBlnIsActiveTrueAndBlnIsDeletedFalseOrderByNumPriorityDescSerPriceVersionIdDesc();

	// Find default version
	Optional<PriceVersion> findByBlnIsDefaultTrueAndBlnIsActiveTrueAndBlnIsDeletedFalse();

	// Find by version code
	Optional<PriceVersion> findByTxtVersionCodeAndBlnIsDeletedFalse(String txtVersionCode);

	// Find versions active for a date
	@Query("SELECT pv FROM PriceVersion pv WHERE " + "pv.blnIsActive = true AND pv.blnIsDeleted = false AND "
			+ "(pv.dteEffectiveFrom IS NULL OR pv.dteEffectiveFrom <= :date) AND "
			+ "(pv.dteEffectiveTo IS NULL OR pv.dteEffectiveTo >= :date) " + "ORDER BY pv.numPriority DESC")
	List<PriceVersion> findActiveForDate(@Param("date") Date date);

	// Get max version code number
	@Query("SELECT MAX(CAST(SUBSTRING(pv.txtVersionCode, 5) AS long)) FROM PriceVersion pv WHERE pv.txtVersionCode LIKE 'MPV-%'")
	Optional<Long> findMaxVersionCodeNumber();
}