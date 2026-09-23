package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.EquipmentRequirement;

@Repository("repositoryEquipmentRequirement")
public interface RepositoryEquipmentRequirement extends JpaRepository<EquipmentRequirement, Long> {

	/**
	 * The rules that fire for every booking — a tablecloth per table, a
	 * corkscrew behind the bar.
	 *
	 * <p>
	 * These exist so a rule true of everything is written once rather than
	 * against all 238 dishes, where the 239th would miss it.
	 */
	@Query("SELECT r FROM EquipmentRequirement r WHERE r.blnIsDeleted = false "
			+ "AND r.blnIsActive = true AND r.blnAppliesToEveryEvent = true")
	List<EquipmentRequirement> findAppliedToEveryEvent();

	/**
	 * The rules against any of these dishes.
	 *
	 * <p>
	 * The dish is fetched with the rule, not left as a proxy. Every line of
	 * the calculated list says which dish asked for it, and the calculation
	 * runs outside a transaction — so a lazy {@code menuItem} throws
	 * LazyInitializationException at the moment it tries to explain itself.
	 * Fetching here also avoids a query per rule.
	 */
	@Query("SELECT r FROM EquipmentRequirement r JOIN FETCH r.menuItem m "
			+ "WHERE r.blnIsDeleted = false AND r.blnIsActive = true "
			+ "AND m.serMenuItemId IN :menuItemIds")
	List<EquipmentRequirement> findForMenuItems(@Param("menuItemIds") List<Long> menuItemIds);

	/** Everything the office maintains for one dish. */
	@Query("SELECT r FROM EquipmentRequirement r JOIN FETCH r.menuItem m "
			+ "WHERE r.blnIsDeleted = false AND m.serMenuItemId = :menuItemId "
			+ "ORDER BY r.equipmentItem.txtName ASC")
	List<EquipmentRequirement> findByMenuItem(@Param("menuItemId") Long menuItemId);

	/**
	 * Everything the office maintains, for the rules screen.
	 *
	 * <p>
	 * LEFT join, not an inner one: a rule that applies to every event has no
	 * dish, and an inner join would silently drop exactly the rules that are
	 * hardest to notice missing.
	 */
	@Query("SELECT r FROM EquipmentRequirement r LEFT JOIN FETCH r.menuItem "
			+ "WHERE r.blnIsDeleted = false "
			+ "ORDER BY r.blnAppliesToEveryEvent DESC, r.equipmentItem.txtName ASC")
	List<EquipmentRequirement> findAllForOffice();
}
