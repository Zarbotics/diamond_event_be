package com.zbs.de.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zbs.de.mapper.MapperMenuItem;
import com.zbs.de.model.MenuItem;
import com.zbs.de.model.dto.DtoMenuComponentRequest;
import com.zbs.de.model.dto.menu.DtoCustomerMenuCategory;
import com.zbs.de.model.dto.menu.DtoCustomerMenuSubCategory;
import com.zbs.de.repository.RepositoryMenuItem;
import com.zbs.de.service.ServiceMenuComponent;
import com.zbs.de.service.ServiceMenuSelection;

@Service("serviceMenuSelectionImpl")
public class ServiceMenuSelectionImpl implements ServiceMenuSelection {

	@Autowired
	RepositoryMenuItem repositoryMenuItem;

	@Autowired
	ServiceMenuComponent serviceMenuComponent;

	@Override
	@Transactional(readOnly = true)
	public List<DtoCustomerMenuCategory> getCustomerMenu() {

		// 1️⃣ Fetch all active categories
		List<MenuItem> categories = repositoryMenuItem.getAllActiveItemsByRoleId(1);

		List<DtoCustomerMenuCategory> result = new ArrayList<>();

		for (MenuItem category : categories) {

			DtoCustomerMenuCategory catDto = new DtoCustomerMenuCategory();
			catDto.setCategoryId(category.getSerMenuItemId());
			catDto.setCategoryName(category.getTxtName());

			// 2️⃣ Fetch subcategories
			List<MenuItem> subCategories = repositoryMenuItem.findByParentId(category.getSerMenuItemId());

			List<DtoCustomerMenuSubCategory> subDtos = new ArrayList<>();

			for (MenuItem sub : subCategories) {

				DtoCustomerMenuSubCategory subDto = new DtoCustomerMenuSubCategory();
				subDto.setSubCategoryId(sub.getSerMenuItemId());
				subDto.setSubCategoryName(sub.getTxtName());

				// 3️⃣ Normal (non-composite) items
				List<MenuItem> items = repositoryMenuItem.findByParentId(sub.getSerMenuItemId()).stream()
						.filter(i -> Boolean.TRUE.equals(i.getBlnIsSelectable()))
						.filter(i -> !Boolean.TRUE.equals(i.getBlnIsComposite())).toList();

				subDto.setItems(items.stream().map(MapperMenuItem::toDto).toList());

				// 4️⃣ Composite items
				List<MenuItem> composites = repositoryMenuItem.findByParentId(sub.getSerMenuItemId()).stream()
						.filter(i -> Boolean.TRUE.equals(i.getBlnIsComposite())).toList();

				List<DtoMenuComponentRequest> compositeDtos = new ArrayList<>();
				for (MenuItem composite : composites) {
					DtoMenuComponentRequest comp = serviceMenuComponent
							.getCompositeWithComponents(composite.getSerMenuItemId());
					if (comp != null) {
						compositeDtos.add(comp);
					}
				}

				subDto.setCompositeItems(compositeDtos);
				subDtos.add(subDto);
			}

			catDto.setSubCategories(subDtos);
			result.add(catDto);
		}

		return result;
	}
	
	
	@Override
	@Transactional(readOnly = true)
	public List<DtoCustomerMenuCategory> getCustomerCateringMenu() {

		// 1️⃣ Fetch all active categories
		List<MenuItem> categories = repositoryMenuItem.getAllActiveCateringItemsByRoleId(1);

		List<DtoCustomerMenuCategory> result = new ArrayList<>();

		for (MenuItem category : categories) {

			DtoCustomerMenuCategory catDto = new DtoCustomerMenuCategory();
			catDto.setCategoryId(category.getSerMenuItemId());
			catDto.setCategoryName(category.getTxtName());

			// 2️⃣ Fetch subcategories
			List<MenuItem> subCategories = repositoryMenuItem.findCateringItemsByParentId(category.getSerMenuItemId());

			List<DtoCustomerMenuSubCategory> subDtos = new ArrayList<>();

			for (MenuItem sub : subCategories) {

				DtoCustomerMenuSubCategory subDto = new DtoCustomerMenuSubCategory();
				subDto.setSubCategoryId(sub.getSerMenuItemId());
				subDto.setSubCategoryName(sub.getTxtName());

				// 3️⃣ Normal (non-composite) items
				List<MenuItem> items = repositoryMenuItem.findCateringItemsByParentId(sub.getSerMenuItemId()).stream()
						.filter(i -> Boolean.TRUE.equals(i.getBlnIsSelectable()))
						.filter(i -> !Boolean.TRUE.equals(i.getBlnIsComposite())).toList();

				subDto.setItems(items.stream().map(MapperMenuItem::toDto).toList());

				// 4️⃣ Composite items
				List<MenuItem> composites = repositoryMenuItem.findCateringItemsByParentId(sub.getSerMenuItemId()).stream()
						.filter(i -> Boolean.TRUE.equals(i.getBlnIsComposite())).toList();

				List<DtoMenuComponentRequest> compositeDtos = new ArrayList<>();
				for (MenuItem composite : composites) {
					DtoMenuComponentRequest comp = serviceMenuComponent
							.getCompositeWithComponents(composite.getSerMenuItemId());
					if (comp != null) {
						compositeDtos.add(comp);
					}
				}

				subDto.setCompositeItems(compositeDtos);
				subDtos.add(subDto);
			}

			catDto.setSubCategories(subDtos);
			result.add(catDto);
		}

		return result;
	}

}