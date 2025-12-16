package com.zbs.de.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.MenuItemRole;

@Repository
public interface RepositoryMenuItemRole extends JpaRepository<MenuItemRole, Long> {

	Optional<MenuItemRole> findBySerMenuItemRoleIdAndBlnIsDeletedFalse(Integer id);

	List<MenuItemRole> findByBlnIsCompositionRoleTrueAndBlnIsDeletedFalseOrderBySerMenuItemRoleIdDesc();

	List<MenuItemRole> findByBlnIsCompositionRoleFalseAndBlnIsDeletedFalseOrderBySerMenuItemRoleIdDesc();

	List<MenuItemRole> findByBlnIsCompositionRoleTrueAndBlnIsActiveTrueAndBlnIsDeletedFalseOrderBySerMenuItemRoleIdDesc();

	List<MenuItemRole> findByBlnIsCompositionRoleFalseAndBlnIsActiveTrueAndBlnIsDeletedFalseOrderBySerMenuItemRoleIdDesc();

	List<MenuItemRole> findByBlnIsDeletedFalseOrderBySerMenuItemRoleIdDesc();

	List<MenuItemRole> findByBlnIsActiveTrueAndBlnIsDeletedFalseOrderBySerMenuItemRoleIdDesc();

	@Query("""
			    SELECT r
			    FROM MenuItemRole r
			    WHERE r.parentMenuItemRole.serMenuItemRoleId = :parentRoleId
			      AND r.blnIsDeleted = false
			      ORDER BY r.serMenuItemRoleId DESC
			""")
	List<MenuItemRole> findByParentRoleId(@Param("parentRoleId") Integer parentRoleId);

	@Query("""
			    SELECT r
			    FROM MenuItemRole r
			    WHERE r.parentMenuItemRole.serMenuItemRoleId = :parentRoleId
			      AND r.blnIsActive = true
			      AND r.blnIsDeleted = false
			      ORDER BY r.serMenuItemRoleId DESC
			""")
	List<MenuItemRole> findActiveByParentRoleId(@Param("parentRoleId") Integer parentRoleId);
}
