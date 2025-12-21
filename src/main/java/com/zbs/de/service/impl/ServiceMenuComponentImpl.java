package com.zbs.de.service.impl;

import com.zbs.de.mapper.MapperMenuItem;
import com.zbs.de.model.MenuComponent;
import com.zbs.de.model.MenuItem;
import com.zbs.de.model.dto.DtoMenuComponent;
import com.zbs.de.model.dto.DtoMenuItem;
import com.zbs.de.model.dto.DtoComponentGroup;
import com.zbs.de.model.dto.DtoMenuItemWithComponents;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSelectionGroupRequest;
import com.zbs.de.repository.RepositoryMenuComponent;
import com.zbs.de.repository.RepositoryMenuItem;
import com.zbs.de.service.ServiceMenuComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ServiceMenuComponentImpl implements ServiceMenuComponent {

	@Autowired
	private RepositoryMenuComponent repository;

	@Autowired
	private RepositoryMenuItem menuItemRepository;

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceMenuItemImpl.class);

	@Override
	public DtoResult createComponent(DtoMenuComponent request) {
		try {
			// Validate parent
			MenuItem parent = menuItemRepository.findById(request.getParentMenuItemId()).orElseThrow(
					() -> new RuntimeException("Parent menu item not found with ID: " + request.getParentMenuItemId()));

			// Mark parent as composite if not already
			if (parent.getBlnIsComposite() == null || !parent.getBlnIsComposite()) {
				parent.setBlnIsComposite(true);
				menuItemRepository.save(parent);
			}

			// Create component
			MenuComponent component = new MenuComponent();
			component.setParentMenuItem(parent);
			component.setTxtComponentKind(request.getTxtComponentKind());
			component.setTxtDisplayName(request.getTxtDisplayName());
			component.setNumSelectionMin(request.getNumSelectionMin());
			component.setNumSelectionMax(request.getNumSelectionMax());

			// Set sequence order if not provided
			if (request.getNumSequenceOrder() == null) {
				Integer maxOrder = repository.findMaxSequenceOrderByParentId(parent.getSerMenuItemId());
				component.setNumSequenceOrder(maxOrder + 1);
			} else {
				component.setNumSequenceOrder(request.getNumSequenceOrder());
			}

			// Set child if provided
			if (request.getChildMenuItemId() != null) {
				MenuItem child = menuItemRepository.findById(request.getChildMenuItemId())
						.orElseThrow(() -> new RuntimeException(
								"Child menu item not found with ID: " + request.getChildMenuItemId()));
				component.setChildMenuItem(child);
			}

			// Set default active status
			component.setBlnIsActive(true);
			component.setBlnIsDeleted(false);

			// Save
			repository.save(component);

			return new DtoResult("Component created successfully.", null, convertToDto(component), null);

		} catch (Exception e) {
			LOGGER.error("Error creating menu component", e);
			return new DtoResult("Failed to create component: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult createSelectionGroup(DtoSelectionGroupRequest request) {
		try {
			// Validate parent
			MenuItem parent = menuItemRepository.findById(request.getParentMenuItemId()).orElseThrow(
					() -> new RuntimeException("Parent menu item not found with ID: " + request.getParentMenuItemId()));

			// Mark parent as composite
			if (request.getMarkAsComposite() != null && request.getMarkAsComposite()) {
				parent.setBlnIsComposite(true);
				menuItemRepository.save(parent);
			}

			// Validate menu items
			if (request.getMenuItemIds() == null || request.getMenuItemIds().isEmpty()) {
				return new DtoResult("Menu item IDs list is empty.", null, null, null);
			}

			// Get current max sequence order
			Integer currentMaxOrder = repository.findMaxSequenceOrderByParentId(parent.getSerMenuItemId());
			int sequenceOrder = request.getStartSequenceOrder() != null ? request.getStartSequenceOrder()
					: (currentMaxOrder != null ? currentMaxOrder + 1 : 1);

			List<DtoMenuComponent> createdComponents = new ArrayList<>();

			// Create components for each item
			for (Long itemId : request.getMenuItemIds()) {
				MenuItem child = menuItemRepository.findById(itemId)
						.orElseThrow(() -> new RuntimeException("Menu item not found with ID: " + itemId));

				// Check if component already exists
				Optional<MenuComponent> existing = repository.findByParentAndChild(parent.getSerMenuItemId(),
						child.getSerMenuItemId());

				if (existing.isPresent()) {
					LOGGER.warn("Component already exists for parent {} and child {}", parent.getSerMenuItemId(),
							child.getSerMenuItemId());
					continue;
				}

				// Create new component
				MenuComponent component = new MenuComponent();
				component.setParentMenuItem(parent);
				component.setChildMenuItem(child);
				component.setTxtComponentKind(request.getTxtComponentKind());
				component.setTxtDisplayName(request.getGroupDisplayName());
				component.setNumSelectionMin(request.getNumSelectionMin());
				component.setNumSelectionMax(request.getNumSelectionMax());
				component.setNumSequenceOrder(sequenceOrder++);
				component.setBlnIsActive(true);
				component.setBlnIsDeleted(false);

				repository.save(component);
				createdComponents.add(convertToDto(component));
			}

			String message = String.format("Selection group created with %d items.", createdComponents.size());
			return new DtoResult(message, null, createdComponents, null);

		} catch (Exception e) {
			LOGGER.error("Error creating selection group", e);
			return new DtoResult("Failed to create selection group: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult updateComponent(DtoMenuComponent dto) {
		try {
			MenuComponent component = repository.findById(dto.getSerComponentId()).orElseThrow(
					() -> new RuntimeException("Menu component not found with ID: " + dto.getSerComponentId()));

			if (Boolean.TRUE.equals(component.getBlnIsDeleted())) {
				return new DtoResult("Component is deleted and cannot be updated.", null, null, null);
			}

			// Update fields
			if (dto.getTxtComponentKind() != null) {
				component.setTxtComponentKind(dto.getTxtComponentKind());
			}

			if (dto.getTxtDisplayName() != null) {
				component.setTxtDisplayName(dto.getTxtDisplayName());
			}

			if (dto.getNumSelectionMin() != null) {
				component.setNumSelectionMin(dto.getNumSelectionMin());
			}

			if (dto.getNumSelectionMax() != null) {
				component.setNumSelectionMax(dto.getNumSelectionMax());
			}

			if (dto.getNumSequenceOrder() != null) {
				component.setNumSequenceOrder(dto.getNumSequenceOrder());
			}

			if (dto.getBlnIsActive() != null) {
				component.setBlnIsActive(dto.getBlnIsActive());
			}

			repository.save(component);

			return new DtoResult("Component updated successfully.", null, convertToDto(component), null);

		} catch (Exception e) {
			LOGGER.error("Error updating menu component", e);
			return new DtoResult("Failed to update component: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult deleteComponent(Long componentId) {
		try {
			MenuComponent component = repository.findById(componentId)
					.orElseThrow(() -> new RuntimeException("Menu component not found with ID: " + componentId));

			if (Boolean.TRUE.equals(component.getBlnIsDeleted())) {
				return new DtoResult("Component is already deleted.", null, null, null);
			}

			component.setBlnIsDeleted(true);
			component.setBlnIsActive(false);
			repository.save(component);

			// Check if parent still has components
			Long parentId = component.getParentMenuItem().getSerMenuItemId();
			Integer remainingCount = repository.countByParentMenuItemId(parentId);

			if (remainingCount == 0) {
				MenuItem parent = component.getParentMenuItem();
				parent.setBlnIsComposite(false);
				menuItemRepository.save(parent);
			}

			return new DtoResult("Component deleted successfully.", null, null, null);

		} catch (Exception e) {
			LOGGER.error("Error deleting menu component", e);
			return new DtoResult("Failed to delete component: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult deleteComponentsByParent(Long parentMenuItemId) {
		try {
			List<MenuComponent> components = repository.findByParentMenuItem_SerMenuItemId(parentMenuItemId);

			if (components.isEmpty()) {
				return new DtoResult("No components found for this menu item.", null, null, null);
			}

			for (MenuComponent component : components) {
				component.setBlnIsDeleted(true);
				component.setBlnIsActive(false);
				repository.save(component);
			}

			// Update parent composite flag
			MenuItem parent = menuItemRepository.findById(parentMenuItemId).orElse(null);
			if (parent != null) {
				parent.setBlnIsComposite(false);
				menuItemRepository.save(parent);
			}

			return new DtoResult("Deleted " + components.size() + " components.", null, null, null);

		} catch (Exception e) {
			LOGGER.error("Error deleting components by parent", e);
			return new DtoResult("Failed to delete components: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult deleteComponentsByParentAndDisplayName(Long parentMenuItemId, String displayName) {
		try {
			List<MenuComponent> components = repository
					.findByParentMenuItem_SerMenuItemIdAndTxtDisplayName(parentMenuItemId, displayName);

			if (components.isEmpty()) {
				return new DtoResult("No components found with this display name.", null, null, null);
			}

			for (MenuComponent component : components) {
				component.setBlnIsDeleted(true);
				component.setBlnIsActive(false);
				repository.save(component);
			}

			return new DtoResult("Deleted " + components.size() + " components.", null, null, null);

		} catch (Exception e) {
			LOGGER.error("Error deleting components by display name", e);
			return new DtoResult("Failed to delete components: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult getComponentById(Long componentId) {
		try {
			MenuComponent component = repository.findById(componentId)
					.orElseThrow(() -> new RuntimeException("Menu component not found with ID: " + componentId));

			if (Boolean.TRUE.equals(component.getBlnIsDeleted())) {
				return new DtoResult("Component is deleted.", null, null, null);
			}

			return new DtoResult("Fetched successfully.", null, convertToDto(component), null);

		} catch (Exception e) {
			LOGGER.error("Error fetching menu component", e);
			return new DtoResult("Failed to fetch component: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult getComponentsByMenuItem(Long menuItemId) {
		try {
			List<MenuComponent> components = repository
					.findByParentMenuItem_SerMenuItemIdOrderByNumSequenceOrderAsc(menuItemId);

			List<DtoMenuComponent> dtos = components.stream().filter(c -> !Boolean.TRUE.equals(c.getBlnIsDeleted()))
					.map(this::convertToDto).collect(Collectors.toList());

			return new DtoResult("Fetched successfully.", null, dtos, null);

		} catch (Exception e) {
			LOGGER.error("Error fetching components by menu item", e);
			return new DtoResult("Failed to fetch components: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult getActiveComponentsByMenuItem(Long menuItemId) {
		try {
			List<MenuComponent> components = repository
					.findByParentMenuItem_SerMenuItemIdAndBlnIsActiveTrueAndBlnIsDeletedFalseOrderByNumSequenceOrderAsc(
							menuItemId);

			List<DtoMenuComponent> dtos = components.stream().map(this::convertToDto).collect(Collectors.toList());

			return new DtoResult("Fetched successfully.", null, dtos, null);

		} catch (Exception e) {
			LOGGER.error("Error fetching active components", e);
			return new DtoResult("Failed to fetch active components: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult getComponentsGrouped(Long menuItemId) {
		try {
			List<MenuComponent> components = repository
					.findByParentMenuItem_SerMenuItemIdAndBlnIsActiveTrueAndBlnIsDeletedFalseOrderByNumSequenceOrderAsc(
							menuItemId);

			// Group by display name
			Map<String, List<DtoMenuComponent>> grouped = components.stream().map(this::convertToDto)
					.collect(Collectors.groupingBy(
							dto -> dto.getTxtDisplayName() != null ? dto.getTxtDisplayName() : "Unnamed Group",
							LinkedHashMap::new, // Preserve order
							Collectors.toList()));

			return new DtoResult("Fetched successfully.", null, grouped, null);

		} catch (Exception e) {
			LOGGER.error("Error fetching grouped components", e);
			return new DtoResult("Failed to fetch grouped components: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult getMenuItemWithComponents(Long menuItemId) {
		try {
			// Get the menu item
			MenuItem menuItem = menuItemRepository.findById(menuItemId)
					.orElseThrow(() -> new RuntimeException("Menu item not found with ID: " + menuItemId));

			DtoMenuItemWithComponents result = new DtoMenuItemWithComponents();
			result.setMenuItem(MapperMenuItem.toDto(menuItem));

			// Get components grouped
			List<MenuComponent> components = repository
					.findByParentMenuItem_SerMenuItemIdAndBlnIsActiveTrueAndBlnIsDeletedFalseOrderByNumSequenceOrderAsc(
							menuItemId);

			// Group by display name and component kind
			Map<String, List<MenuComponent>> grouped = components.stream()
					.collect(Collectors.groupingBy(
							c -> c.getTxtDisplayName() != null ? c.getTxtDisplayName()
									: c.getTxtComponentKind() + "_" + c.getNumSequenceOrder(),
							LinkedHashMap::new, Collectors.toList()));

			// Convert to DTO structure
			List<DtoComponentGroup> componentGroups = new ArrayList<>();

			for (Map.Entry<String, List<MenuComponent>> entry : grouped.entrySet()) {
				List<MenuComponent> groupComponents = entry.getValue();
				if (groupComponents.isEmpty())
					continue;

				DtoComponentGroup groupDto = new DtoComponentGroup();
				MenuComponent first = groupComponents.get(0);

				groupDto.setDisplayName(first.getTxtDisplayName());
				groupDto.setComponentKind(first.getTxtComponentKind());
				groupDto.setMinSelections(first.getNumSelectionMin());
				groupDto.setMaxSelections(first.getNumSelectionMax());

				// Get child items
				List<DtoMenuItem> items = groupComponents.stream().filter(c -> c.getChildMenuItem() != null)
						.map(c -> MapperMenuItem.toDto(c.getChildMenuItem())).collect(Collectors.toList());

				groupDto.setItems(items);
				componentGroups.add(groupDto);
			}

			result.setComponentGroups(componentGroups);

			return new DtoResult("Fetched successfully.", null, result, null);

		} catch (Exception e) {
			LOGGER.error("Error fetching menu item with components", e);
			return new DtoResult("Failed to fetch menu item with components: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult updateComponentSequence(List<Long> componentIds) {
		try {
			if (componentIds == null || componentIds.isEmpty()) {
				return new DtoResult("Component IDs list is empty.", null, null, null);
			}

			List<MenuComponent> updated = new ArrayList<>();

			for (int i = 0; i < componentIds.size(); i++) {
				Long componentId = componentIds.get(i);
				MenuComponent component = repository.findById(componentId)
						.orElseThrow(() -> new RuntimeException("Component not found: " + componentId));

				component.setNumSequenceOrder(i + 1);
				repository.save(component);
				updated.add(component);
			}

			List<DtoMenuComponent> dtos = updated.stream().map(this::convertToDto).collect(Collectors.toList());

			return new DtoResult("Sequence updated successfully.", null, dtos, null);

		} catch (Exception e) {
			LOGGER.error("Error updating component sequence", e);
			return new DtoResult("Failed to update sequence: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult copyComponents(Long sourceMenuItemId, Long targetMenuItemId) {
		try {
			// Get source components
			List<MenuComponent> sourceComponents = repository
					.findByParentMenuItem_SerMenuItemIdAndBlnIsActiveTrueAndBlnIsDeletedFalseOrderByNumSequenceOrderAsc(
							sourceMenuItemId);

			if (sourceComponents.isEmpty()) {
				return new DtoResult("No components found to copy.", null, null, null);
			}

			// Get target menu item
			MenuItem target = menuItemRepository.findById(targetMenuItemId)
					.orElseThrow(() -> new RuntimeException("Target menu item not found with ID: " + targetMenuItemId));

			// Mark target as composite
			target.setBlnIsComposite(true);
			menuItemRepository.save(target);

			// Copy components
			List<DtoMenuComponent> copied = new ArrayList<>();
			int sequenceOrder = repository.findMaxSequenceOrderByParentId(targetMenuItemId) + 1;

			for (MenuComponent source : sourceComponents) {
				MenuComponent copy = new MenuComponent();
				copy.setParentMenuItem(target);
				copy.setChildMenuItem(source.getChildMenuItem());
				copy.setTxtComponentKind(source.getTxtComponentKind());
				copy.setTxtDisplayName(source.getTxtDisplayName());
				copy.setNumSelectionMin(source.getNumSelectionMin());
				copy.setNumSelectionMax(source.getNumSelectionMax());
				copy.setNumSequenceOrder(sequenceOrder++);
				copy.setBlnIsActive(true);
				copy.setBlnIsDeleted(false);

				repository.save(copy);
				copied.add(convertToDto(copy));
			}

			return new DtoResult("Copied " + copied.size() + " components.", null, copied, null);

		} catch (Exception e) {
			LOGGER.error("Error copying components", e);
			return new DtoResult("Failed to copy components: " + e.getMessage(), null, null, null);
		}
	}

	// Helper methods
	private DtoMenuComponent convertToDto(MenuComponent entity) {
		if (entity == null)
			return null;

		DtoMenuComponent dto = new DtoMenuComponent();
		dto.setSerComponentId(entity.getSerComponentId());

		// Parent info
		if (entity.getParentMenuItem() != null) {
			dto.setParentMenuItemId(entity.getParentMenuItem().getSerMenuItemId());
			dto.setParentMenuItemName(entity.getParentMenuItem().getTxtName());
			dto.setParentMenuItemCode(entity.getParentMenuItem().getTxtCode());
		}

		// Child info
		if (entity.getChildMenuItem() != null) {
			dto.setChildMenuItemId(entity.getChildMenuItem().getSerMenuItemId());
			dto.setChildMenuItemName(entity.getChildMenuItem().getTxtName());
			dto.setChildMenuItemCode(entity.getChildMenuItem().getTxtCode());
		}

		// Component fields
		dto.setTxtComponentKind(entity.getTxtComponentKind());
		dto.setTxtDisplayName(entity.getTxtDisplayName());
		dto.setNumSelectionMin(entity.getNumSelectionMin());
		dto.setNumSelectionMax(entity.getNumSelectionMax());
		dto.setNumSequenceOrder(entity.getNumSequenceOrder());
		dto.setBlnIsActive(entity.getBlnIsActive());

		return dto;
	}
}
