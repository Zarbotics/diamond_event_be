package com.zbs.de.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.MenuComponent;
import com.zbs.de.model.MenuItemRole;

@Repository
public interface RepositoryMenuComponent extends JpaRepository<MenuComponent, Long> {

	// Existing methods...

	@Query("SELECT DISTINCT mc.componenetKindRole FROM MenuComponent mc WHERE mc.parentMenuItem.serMenuItemId = :parentId AND mc.blnIsDeleted = false")
	List<MenuItemRole> findDistinctComponentKindsByParent(@Param("parentId") Long parentMenuItemId);

	@Query("SELECT mc FROM MenuComponent mc WHERE mc.parentMenuItem.serMenuItemId = :parentId AND mc.blnIsDeleted = false")
	List<MenuComponent> findByParentMenuItemId(@Param("parentId") Long parentMenuItemId);

	@Query("SELECT mc FROM MenuComponent mc WHERE mc.parentMenuItem.serMenuItemId = :parentId AND mc.blnIsDeleted = false ORDER BY  mc.childMenuItem.txtName")
	List<MenuComponent> findByParentAndRole(@Param("parentId") Long parentMenuItemId, @Param("roleId") Integer roleId);

	@Query("SELECT mc FROM MenuComponent mc WHERE mc.parentMenuItem.serMenuItemId = :parentId AND mc.childMenuItem.serMenuItemId = :childId AND mc.blnIsDeleted = false")
	List<MenuComponent> findByParentAndChild(@Param("parentId") Long parentMenuItemId,
			@Param("childId") Long childMenuItemId);

	@Query("SELECT COUNT(mc) FROM MenuComponent mc WHERE mc.parentMenuItem.serMenuItemId = :parentId AND mc.componenetKindRole.serMenuItemRoleId = :roleId AND mc.blnIsDeleted = false")
	Long countByParentAndRole(@Param("parentId") Long parentMenuItemId, @Param("roleId") Integer roleId);

	@Query("DELETE FROM MenuComponent mc WHERE mc.parentMenuItem.serMenuItemId = :parentId AND mc.componenetKindRole.serMenuItemRoleId = :roleId")
	void deleteByParentAndRole(@Param("parentId") Long parentMenuItemId, @Param("roleId") Integer roleId);
	
	List<MenuComponent> findByChildMenuItem_SerMenuItemId(Long childMenuItemId);
	
	List<MenuComponent> findByParentMenuItem_SerMenuItemId(Long parentMenuItemId);
}
