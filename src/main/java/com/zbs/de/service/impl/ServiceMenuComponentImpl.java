package com.zbs.de.service.impl;

import com.zbs.de.mapper.MapperMenuComponent;
import com.zbs.de.model.MenuComponent;
import com.zbs.de.model.MenuItem;
import com.zbs.de.model.dto.DtoMenuComponent;
import com.zbs.de.repository.RepositoryMenuComponent;
import com.zbs.de.repository.RepositoryMenuItem;
import com.zbs.de.service.ServiceMenuComponent;
import com.zbs.de.service.ServiceMenuValidation;
import com.zbs.de.util.exception.NotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceMenuComponentImpl implements ServiceMenuComponent {

	@Autowired
	private RepositoryMenuComponent repo;
	
	@Autowired
	private RepositoryMenuItem menuItemRepo;
	
	@Autowired
	private ServiceMenuValidation validator;

	@Override
	@Transactional
	public DtoMenuComponent create(DtoMenuComponent dto) {
		validator.validateComponentDto(dto);
		MenuComponent entity = MapperMenuComponent.toEntity(dto);
		MenuItem parent = menuItemRepo.findById(dto.getParentMenuItemId())
				.orElseThrow(() -> new NotFoundException("Parent menu item not found"));
		entity.setParentMenuItem(parent);
		if (dto.getChildMenuItemId() != null) {
			MenuItem child = menuItemRepo.findById(dto.getChildMenuItemId())
					.orElseThrow(() -> new NotFoundException("Child menu item not found"));
			entity.setChildMenuItem(child);
		}
		repo.save(entity);
		return MapperMenuComponent.toDto(entity);
	}

	@Override
	@Transactional
	public DtoMenuComponent update(Long id, DtoMenuComponent dto) {
		MenuComponent exist = repo.findById(id).orElseThrow(() -> new NotFoundException("Component not found"));
		validator.validateComponentDto(dto);
		exist.setTxtComponentKind(dto.getTxtComponentKind());
		exist.setNumSelectionMin(dto.getNumSelectionMin());
		exist.setNumSelectionMax(dto.getNumSelectionMax());
		exist.setNumSequenceOrder(dto.getNumSequenceOrder());
		exist.setMetadata(dto.getMetadata());
		if (dto.getChildMenuItemId() != null) {
			exist.setChildMenuItem(menuItemRepo.findById(dto.getChildMenuItemId())
					.orElseThrow(() -> new NotFoundException("Child menu item not found")));
		} else {
			exist.setChildMenuItem(null);
		}
		repo.save(exist);
		return MapperMenuComponent.toDto(exist);
	}

	@Override
	public void delete(Long id) {
		MenuComponent exist = repo.findById(id).orElseThrow(() -> new NotFoundException("Component not found"));
		exist.setBlnIsDeleted(true);
		repo.save(exist);
	}

	@Override
	public List<DtoMenuComponent> findByParent(Long parentMenuItemId) {
		return repo.findByParentMenuItem_SerMenuItemId(parentMenuItemId).stream().map(MapperMenuComponent::toDto).collect(Collectors.toList());
	}
}
