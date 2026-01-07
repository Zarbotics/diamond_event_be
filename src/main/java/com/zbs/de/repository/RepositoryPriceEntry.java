
package com.zbs.de.repository;

import com.zbs.de.model.MenuItem;
import com.zbs.de.model.PriceEntry;
import com.zbs.de.util.enums.EnmApplyScope;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository("repositoryPriceEntry")
public interface RepositoryPriceEntry extends JpaRepository<PriceEntry, Long> {

	// Custom query methods for Price Matrix screen
	@Query("SELECT pe FROM PriceEntry pe WHERE pe.priceVersion.serPriceVersionId = :versionId "
			+ "AND pe.blnIsDeleted = false " + "ORDER BY pe.applyScope, pe.numTargetId")
	List<PriceEntry> findByVersionId(@Param("versionId") Long versionId);

	@Query("SELECT pe FROM PriceEntry pe WHERE pe.priceVersion.serPriceVersionId = :versionId "
			+ "AND pe.applyScope = :scope AND pe.blnIsDeleted = false")
	List<PriceEntry> findByVersionAndScope(@Param("versionId") Long versionId, @Param("scope") String scope);

	@Query("SELECT pe FROM PriceEntry pe WHERE pe.priceVersion.serPriceVersionId = :versionId "
			+ "AND pe.numTargetId = :targetId AND pe.applyScope = :scope AND pe.blnIsDeleted = false")
	List<PriceEntry> findByVersionTargetAndScope(@Param("versionId") Long versionId, @Param("targetId") Long targetId,
			@Param("scope") String scope);

	@Query("SELECT pe FROM PriceEntry pe WHERE pe.priceVersion.serPriceVersionId = :versionId "
			+ "AND pe.numTargetId IN :targetIds AND pe.blnIsDeleted = false")
	List<PriceEntry> findByVersionAndTargetIds(@Param("versionId") Long versionId,
			@Param("targetIds") List<Long> targetIds);

	@Query("SELECT mi FROM MenuItem mi WHERE mi.txtRole = 'STATION' "
			+ "AND mi.blnIsDeleted = false ORDER BY mi.txtName")
	List<MenuItem> findAllStations();

	@Query("SELECT mi FROM MenuItem mi WHERE mi.blnIsComposite = true "
			+ "AND mi.blnIsDeleted = false ORDER BY mi.txtName")
	List<MenuItem> findAllBundles();

	// Check if price entry exists for specific scope and target
	@Query("SELECT COUNT(pe) > 0 FROM PriceEntry pe WHERE pe.priceVersion.serPriceVersionId = :versionId "
			+ "AND pe.numTargetId = :targetId AND pe.applyScope = :scope AND pe.blnIsDeleted = false")
	boolean existsByVersionTargetAndScope(@Param("versionId") Long versionId, @Param("targetId") Long targetId,
			@Param("scope") String scope);

	List<PriceEntry> findByPriceVersionSerPriceVersionId(Long priceVersionId);

	List<PriceEntry> findByPriceVersionSerPriceVersionIdAndApplyScope(Long priceVersionId, EnmApplyScope applyScope);

	List<PriceEntry> findByPriceVersionSerPriceVersionIdAndApplyScopeAndNumTargetId(Long priceVersionId,
			EnmApplyScope applyScope, Long targetId);
}