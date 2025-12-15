package com.zbs.de.service.impl;

import com.zbs.de.model.MenuItemIngredient;
import com.zbs.de.model.MenuItemIngredient.MenuItemIngredientKey;
import com.zbs.de.model.dto.DtoMenuItemIngredient;
import com.zbs.de.repository.RepositoryIngredient;
import com.zbs.de.repository.RepositoryMenuItem;
import com.zbs.de.repository.RepositoryMenuItemIngredient;
import com.zbs.de.service.ServiceMenuItemIngredient;
import com.zbs.de.util.exception.NotFoundException;
import com.zbs.de.model.MenuItem;
import com.zbs.de.model.Ingredient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service("serviceMenuItemIngredientImpl")
public class ServiceMenuItemIngredientImpl implements ServiceMenuItemIngredient {

	private final RepositoryMenuItemIngredient repo;
	private final RepositoryMenuItem menuRepo;
	private final RepositoryIngredient ingredientRepo;

	public ServiceMenuItemIngredientImpl(RepositoryMenuItemIngredient repo, RepositoryMenuItem menuRepo,
			RepositoryIngredient ingredientRepo) {
		this.repo = repo;
		this.menuRepo = menuRepo;
		this.ingredientRepo = ingredientRepo;
	}

	@Override
	@Transactional
	public void addIngredientToMenuItem(Long menuItemId, DtoMenuItemIngredient dto) {
		MenuItem menu = menuRepo.findById(menuItemId).orElseThrow(() -> new NotFoundException("MenuItem not found"));
		Ingredient ing = ingredientRepo.findById(dto.getIngredientId())
				.orElseThrow(() -> new NotFoundException("Ingredient not found"));
		MenuItemIngredientKey key = new MenuItemIngredientKey(menuItemId, dto.getIngredientId());
		MenuItemIngredient mii = new MenuItemIngredient();
		mii.setId(key);
		mii.setNumQuantity(dto.getNumQuantity());
		mii.setTxtUOM(dto.getTxtUOM());
		repo.save(mii);
	}

	@Override
	@Transactional
	public void removeIngredientFromMenuItem(Long menuItemId, Long ingredientId) {
		MenuItemIngredientKey key = new MenuItemIngredientKey(menuItemId, ingredientId);
		repo.deleteById(key);
	}

	@Override
	public List<DtoMenuItemIngredient> listIngredients(Long menuItemId) {
		return repo.findById_MenuItemId(menuItemId).stream().map(e -> {
			DtoMenuItemIngredient d = new DtoMenuItemIngredient();
			d.setIngredientId(e.getId().getIngredientId());
			d.setNumQuantity(e.getNumQuantity());
			d.setTxtUOM(e.getTxtUOM());
			return d;
		}).collect(Collectors.toList());
	}
}
