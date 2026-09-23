package com.zbs.de.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.EquipmentItem;

@Repository("repositoryEquipmentItem")
public interface RepositoryEquipmentItem extends JpaRepository<EquipmentItem, Integer> {

	@Query("SELECT i FROM EquipmentItem i WHERE i.blnIsDeleted = false "
			+ "ORDER BY i.equipmentCategory.numDisplayOrder ASC NULLS LAST, "
			+ "i.numDisplayOrder ASC NULLS LAST, i.txtName ASC")
	List<EquipmentItem> findAllForOffice();

	/**
	 * An item with this name, ignoring case.
	 *
	 * <p>
	 * Checked in Java so a duplicate reaches the office as a sentence rather
	 * than as a constraint violation naming an index.
	 */
	@Query("SELECT i FROM EquipmentItem i WHERE i.blnIsDeleted = false "
			+ "AND LOWER(i.txtName) = LOWER(:name) "
			+ "AND (:excludeId IS NULL OR i.serEquipmentItemId <> :excludeId)")
	Optional<EquipmentItem> findByName(@Param("name") String name, @Param("excludeId") Integer excludeId);

	/** How many rules point at each item, so deleting one is a decision. */
	@Query("SELECT r.equipmentItem.serEquipmentItemId, COUNT(r) FROM EquipmentRequirement r "
			+ "WHERE r.blnIsDeleted = false GROUP BY r.equipmentItem.serEquipmentItemId")
	List<Object[]> countRulesByItem();
}
