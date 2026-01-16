package com.zbs.de.repository;

import com.zbs.de.model.MenuItem;
import com.zbs.de.model.MenuItemRole;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RepositoryMenuItem extends JpaRepository<MenuItem, Long> {
	@Query("SELECT e FROM MenuItem e WHERE  e.serMenuItemId = :id AND e.blnIsDeleted = false")
	Optional<MenuItem> getByMenuItemId(@Param("id") Long id);

	List<MenuItem> findByTxtRole(String role);

	List<MenuItem> findByTxtType(String txtType);

	@Query(value = "select * from menu_item where txt_path <@ ?1", nativeQuery = true)
	List<MenuItem> findDescendantsByTxtPath(String ltreePath);

	@Query(value = "select * from menu_item where parent_menu_item_id =:parentId ORDER BY LOWER(txt_name) asc", nativeQuery = true)
	List<MenuItem> findByParentId(@Param("parentId")Long parentId);

	MenuItem findByTxtCode(String txtCode);

	@Query("""
			    SELECT m.txtCode
			    FROM MenuItem m
			    WHERE m.txtCode LIKE CONCAT(:prefix, '_%')
			    ORDER BY m.txtCode DESC
			    LIMIT 1
			""")
	String findMaxCodeByPrefix(@Param("prefix") String prefix);

	List<MenuItem> findByTxtRoleAndBlnIsDeletedFalse(String txtRole);

	@Query("SELECT e FROM MenuItem e WHERE  e.menuItemRole.serMenuItemRoleId = :id AND e.blnIsDeleted = false")
	List<MenuItem> getAllItemsByRoleId(@Param("id") Integer id);
	

	@Query("SELECT e FROM MenuItem e WHERE  e.menuItemRole.serMenuItemRoleId = :id AND LOWER(e.txtName) <> 'other' AND e.blnIsDeleted = false AND e.blnIsActive = true ORDER BY e.numDisplayOrder")
	List<MenuItem> getAllActiveItemsByRoleId(@Param("id") Integer id);

	@Query("SELECT mi FROM MenuItem mi WHERE mi.blnIsComposite = true AND mi.blnIsDeleted = false ORDER BY mi.txtName")
	List<MenuItem> findAllCompositeItems();

	@Query("SELECT mi FROM MenuItem mi WHERE mi.blnIsComposite = true AND mi.blnIsDeleted = false AND mi.blnIsActive = true ORDER BY mi.txtName")
	List<MenuItem> findAllActiveCompositeItems();

	@Query("SELECT mi FROM MenuItem mi WHERE mi.blnIsDeleted = false AND mi.blnIsActive = true ORDER BY mi.serMenuItemId desc")
	List<MenuItem> getAllActiveMenuItems();

	List<MenuItem> findByMenuItemRoleInAndBlnIsDeletedFalse(Collection<MenuItemRole> roles);

	@Query("SELECT DISTINCT m.txtType FROM MenuItem m WHERE m.txtType IS NOT NULL")
	List<String> findDistinctTxtTypes();

	List<MenuItem> findByTxtTypeAndBlnIsDeletedFalse(String txtType);

	@Query("""
			SELECT m
			FROM MenuItem m
			WHERE m.blnIsDeleted = false
			  AND m.blnIsActive = true
			  AND (
			       LOWER(m.txtName) LIKE LOWER(CONCAT('%', :query, '%'))
			    OR LOWER(m.txtCode) LIKE LOWER(CONCAT('%', :query, '%'))
			  )
			ORDER BY m.numDisplayOrder ASC
			""")
	List<MenuItem> searchByQuery(@Param("query") String query);

	@Query("SELECT mi FROM MenuItem mi WHERE mi.blnIsDeleted = false ORDER BY mi.serMenuItemId desc")
	List<MenuItem> getAllMenuItems();

	@Query("""
			    SELECT mi
			    FROM MenuItem mi
			    WHERE LOWER(mi.parent.txtName) = LOWER(:code)
			      AND mi.blnIsComposite = false
			      AND mi.blnIsDeleted = false
			      AND mi.blnIsActive = true
			      AND mi.menuItemRole.serMenuItemRoleId = 3
			    ORDER BY mi.txtName
			""")
	List<MenuItem> getAllNonCompositeActiveItemsByParentItemCode(@Param("code") String code);

}
