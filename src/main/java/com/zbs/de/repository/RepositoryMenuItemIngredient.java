package com.zbs.de.repository;

import com.zbs.de.model.MenuItemIngredient;
import com.zbs.de.model.MenuItemIngredient.MenuItemIngredientKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepositoryMenuItemIngredient extends JpaRepository<MenuItemIngredient, MenuItemIngredientKey> {
	List<MenuItemIngredient> findById_MenuItemId(Long menuItemId);
}