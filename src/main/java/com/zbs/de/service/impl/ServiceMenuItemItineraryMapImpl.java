package com.zbs.de.service.impl;

import com.zbs.de.model.MenuItemItineraryMap;
import com.zbs.de.model.dto.DtoMenuItemItineraryMap;
import com.zbs.de.repository.RepositoryItineraryItem;
import com.zbs.de.repository.RepositoryMenuItem;
import com.zbs.de.repository.RepositoryMenuItemItineraryMap;
import com.zbs.de.service.ServiceMenuItemItineraryMap;
import com.zbs.de.util.exception.NotFoundException;
import com.zbs.de.model.MenuItem;
import com.zbs.de.mapper.MapperMenuItemItineraryMap;
import com.zbs.de.model.ItineraryItem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service("serviceMenuItemItineraryMapImpl")
public class ServiceMenuItemItineraryMapImpl implements ServiceMenuItemItineraryMap {

	@Autowired
	private RepositoryMenuItemItineraryMap repo;

	@Autowired
	private RepositoryMenuItem menuRepo;

	@Autowired
	private RepositoryItineraryItem itRepo;

	@Override
	public DtoMenuItemItineraryMap create(DtoMenuItemItineraryMap dto) {
		MenuItem menu = menuRepo.findById(dto.getMenuItemId())
				.orElseThrow(() -> new NotFoundException("MenuItem not found"));
		ItineraryItem it = itRepo.findById(dto.getItineraryItemId())
				.orElseThrow(() -> new NotFoundException("ItineraryItem not found"));
		MenuItemItineraryMap entity = new MenuItemItineraryMap();
		entity.setMenuItem(menu);
		entity.setItineraryItem(it);
		entity.setTxtMultiplierType(dto.getTxtMultiplierType());
		entity.setTxtMultiplierValue(dto.getTxtMultiplierValue());
		entity.setDependencyExpression(dto.getDependencyExpression());
		entity.setMetadata(dto.getMetadata());
		repo.save(entity);
		return MapperMenuItemItineraryMap.toDto(entity);
	}

	@Override
	public void delete(Long id) {
		MenuItemItineraryMap exist = repo.findById(id).orElseThrow(() -> new NotFoundException("Map not found"));
		exist.setBlnIsDeleted(true);
		repo.save(exist);
	}

	@Override
	public List<DtoMenuItemItineraryMap> findByMenuItem(Long menuItemId) {
		return repo.findByMenuItem_SerMenuItemId(menuItemId).stream().map(MapperMenuItemItineraryMap::toDto)
				.collect(Collectors.toList());
	}
}
