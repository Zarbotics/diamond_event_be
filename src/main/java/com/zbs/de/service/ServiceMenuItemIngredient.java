package com.zbs.de.service;

import java.util.List;

import com.zbs.de.model.dto.DtoMenuItemIngredient;

public interface ServiceMenuItemIngredient {
	void addIngredientToMenuItem(Long menuItemId, DtoMenuItemIngredient dto);

	void removeIngredientFromMenuItem(Long menuItemId, Long ingredientId);

	List<DtoMenuItemIngredient> listIngredients(Long menuItemId);
}
