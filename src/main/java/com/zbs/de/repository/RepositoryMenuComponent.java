package com.zbs.de.repository;

import com.zbs.de.model.MenuComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RepositoryMenuComponent extends JpaRepository<MenuComponent, Long> {
	List<MenuComponent> findByParentMenuItem_SerMenuItemId(Long parentMenuItemId);

	List<MenuComponent> findByChildMenuItem_SerMenuItemId(Long childMenuItemId);

	// Find by parent menu item ID ordered by sequence
	List<MenuComponent> findByParentMenuItem_SerMenuItemIdOrderByNumSequenceOrderAsc(Long parentMenuItemId);

	// Find by parent and component kind
	List<MenuComponent> findByParentMenuItem_SerMenuItemIdAndTxtComponentKind(Long parentMenuItemId,
			String txtComponentKind);

	// Find specific component
	@Query("SELECT mc FROM MenuComponent mc WHERE " + "mc.parentMenuItem.serMenuItemId = :parentId AND "
			+ "mc.childMenuItem.serMenuItemId = :childId AND " + "mc.blnIsDeleted = false")
	Optional<MenuComponent> findByParentAndChild(@Param("parentId") Long parentId, @Param("childId") Long childId);

	// Find by parent ID and display name
	List<MenuComponent> findByParentMenuItem_SerMenuItemIdAndTxtDisplayName(Long parentMenuItemId,
			String txtDisplayName);

	// Count components by parent
	@Query("SELECT COUNT(mc) FROM MenuComponent mc WHERE " + "mc.parentMenuItem.serMenuItemId = :parentId AND "
			+ "mc.blnIsDeleted = false")
	Integer countByParentMenuItemId(@Param("parentId") Long parentId);

	// Find next sequence order for a parent
	@Query("SELECT COALESCE(MAX(mc.numSequenceOrder), 0) FROM MenuComponent mc WHERE "
			+ "mc.parentMenuItem.serMenuItemId = :parentId")
	Integer findMaxSequenceOrderByParentId(@Param("parentId") Long parentId);

	// Find non-deleted components
	List<MenuComponent> findByBlnIsDeletedFalse();

	// Find active components by parent
	List<MenuComponent> findByParentMenuItem_SerMenuItemIdAndBlnIsActiveTrueAndBlnIsDeletedFalseOrderByNumSequenceOrderAsc(
			Long parentMenuItemId);
}
