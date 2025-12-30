package com.zbs.de.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.MenuItemPrice;

@Repository
public interface RepositoryMenuItemPrice extends JpaRepository<MenuItemPrice, Long> {

	// Find by menu item ID
	List<MenuItemPrice> findByMenuItem_SerMenuItemIdOrderByBlnIsDefaultDescBlnIsActiveDescSerPriceIdDesc(
			Long menuItemId);

	// Find active prices by menu item
	List<MenuItemPrice> findByMenuItem_SerMenuItemIdAndBlnIsActiveTrueAndBlnIsDeletedFalseOrderByBlnIsDefaultDescSerPriceIdDesc(
			Long menuItemId);

	// Find by price version ID
	List<MenuItemPrice> findByPriceVersion_SerPriceVersionIdAndBlnIsDeletedFalseOrderByMenuItem_TxtCodeAsc(
			Long priceVersionId);

	// Find by menu item and price version
	Optional<MenuItemPrice> findByMenuItem_SerMenuItemIdAndPriceVersion_SerPriceVersionIdAndBlnIsDeletedFalse(
			Long menuItemId, Long priceVersionId);

	// Find default price for menu item
	Optional<MenuItemPrice> findByMenuItem_SerMenuItemIdAndBlnIsDefaultTrueAndBlnIsActiveTrueAndBlnIsDeletedFalse(
			Long menuItemId);

	// Find applicable price for context
	@Query("SELECT p FROM MenuItemPrice p WHERE " + "p.menuItem.serMenuItemId = :menuItemId AND "
			+ "p.blnIsActive = true AND p.blnIsDeleted = false AND "
			+ "(p.numMinGuests IS NULL OR p.numMinGuests <= :guestCount) AND "
			+ "(p.numMaxGuests IS NULL OR p.numMaxGuests >= :guestCount) AND "
			+ "(p.dteValidFrom IS NULL OR p.dteValidFrom <= :date) AND "
			+ "(p.dteValidTo IS NULL OR p.dteValidTo >= :date) AND "
			+ "(p.priceVersion.serPriceVersionId = :versionId OR "
			+ " (p.priceVersion.blnIsActive = true AND p.priceVersion.blnIsDeleted = false AND "
			+ "  (p.priceVersion.dteEffectiveFrom IS NULL OR p.priceVersion.dteEffectiveFrom <= :date) AND "
			+ "  (p.priceVersion.dteEffectiveTo IS NULL OR p.priceVersion.dteEffectiveTo >= :date))) "
			+ "ORDER BY p.blnIsDefault DESC, p.priceVersion.numPriority DESC, p.serPriceId DESC")
	List<MenuItemPrice> findApplicablePrice(@Param("menuItemId") Long menuItemId,
			@Param("guestCount") Integer guestCount, @Param("date") Date date, @Param("versionId") Long versionId);

	// Bulk operations
	@Query("SELECT p FROM MenuItemPrice p WHERE " + "p.menuItem.serMenuItemId IN :menuItemIds AND "
			+ "p.priceVersion.serPriceVersionId = :versionId AND " + "p.blnIsDeleted = false")
	List<MenuItemPrice> findByMenuItemIdsAndVersion(@Param("menuItemIds") List<Long> menuItemIds,
			@Param("versionId") Long versionId);

	// Count prices by version
	@Query("SELECT COUNT(p) FROM MenuItemPrice p WHERE " + "p.priceVersion.serPriceVersionId = :versionId AND "
			+ "p.blnIsDeleted = false")
	Integer countByPriceVersionId(@Param("versionId") Long versionId);

	// Count active prices by version
	@Query("SELECT COUNT(p) FROM MenuItemPrice p WHERE " + "p.priceVersion.serPriceVersionId = :versionId AND "
			+ "p.blnIsActive = true AND p.blnIsDeleted = false")
	Integer countActiveByPriceVersionId(@Param("versionId") Long versionId);

	// Find by price range
	List<MenuItemPrice> findByNumBasePriceBetweenAndBlnIsActiveTrueAndBlnIsDeletedFalse(BigDecimal minPrice,
			BigDecimal maxPrice);

	// Check if price exists
	@Query("SELECT COUNT(p) > 0 FROM MenuItemPrice p WHERE " + "p.menuItem.serMenuItemId = :menuItemId AND "
			+ "p.priceVersion.serPriceVersionId = :versionId AND " + "p.blnIsDeleted = false")
	boolean existsByMenuItemAndVersion(@Param("menuItemId") Long menuItemId, @Param("versionId") Long versionId);
}