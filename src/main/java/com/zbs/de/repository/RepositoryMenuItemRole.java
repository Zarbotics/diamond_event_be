package com.zbs.de.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.MenuItemRole;

@Repository
public interface RepositoryMenuItemRole extends JpaRepository<MenuItemRole, Long> {

	Optional<MenuItemRole> findBySerMenuItemRoleIdAndBlnIsDeletedFalse(Integer id);

	List<MenuItemRole> findByBlnIsCompositionRoleTrueAndBlnIsDeletedFalse();

	List<MenuItemRole> findByBlnIsCompositionRoleFalseAndBlnIsDeletedFalse();

	List<MenuItemRole> findByBlnIsCompositionRoleTrueAndBlnIsActiveTrueAndBlnIsDeletedFalse();

	List<MenuItemRole> findByBlnIsCompositionRoleFalseAndBlnIsActiveTrueAndBlnIsDeletedFalse();

	List<MenuItemRole> findByBlnIsDeletedFalse();

	List<MenuItemRole> findByBlnIsActiveTrueAndBlnIsDeletedFalse();
}
