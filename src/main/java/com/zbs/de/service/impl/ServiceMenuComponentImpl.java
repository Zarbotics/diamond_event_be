package com.zbs.de.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zbs.de.mapper.MapperMenuComponent;
import com.zbs.de.mapper.MapperMenuItem;
import com.zbs.de.model.MenuComponent;
import com.zbs.de.model.MenuItem;
import com.zbs.de.model.MenuItemRole;
import com.zbs.de.model.dto.DtoMenuComponent;
import com.zbs.de.model.dto.DtoMenuComponentRequest;
import com.zbs.de.model.dto.DtoMenuItem;
import com.zbs.de.model.dto.DtoMenuItemRole;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryMenuComponent;
import com.zbs.de.repository.RepositoryMenuItem;
import com.zbs.de.service.ServiceMenuComponent;
import com.zbs.de.service.ServiceMenuItemRole;

@Service
public class ServiceMenuComponentImpl implements ServiceMenuComponent {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceMenuComponentImpl.class);

	@Autowired
	private RepositoryMenuComponent repository;

	@Autowired
	private RepositoryMenuItem repositoryMenuItem;

	@Autowired
	private ServiceMenuItemRole serviceMenuItemRole;

	@Override
	@Transactional
	public DtoResult saveOrUpdateComponents(DtoMenuComponentRequest request) {
		LOGGER.info("Saving/Updating components for parent menu item: {}", request.getParentMenuItemId());

		DtoResult result = new DtoResult();

		try {
			// Validate parent menu item exists
			MenuItem parentMenuItem = null;
			Optional<MenuItem> optParentMenuItem = repositoryMenuItem.getByMenuItemId(request.getParentMenuItemId());
			if (optParentMenuItem == null || optParentMenuItem.isEmpty()) {
				return new DtoResult("Parent menu item not found", null, null, null);
			} else {
				parentMenuItem = optParentMenuItem.get();
			}

			// Track all saved components
			List<DtoMenuComponent> allSavedComponents = new ArrayList<>();

			// Process each group
			for (DtoMenuComponent group : request.getComponents()) {
				// Validate component kind role exists
				MenuItemRole componentRole = serviceMenuItemRole
						.getMenuItemRoleById(group.getSerComponenetKindRoleId());

				// Get existing components for this parent and role
				List<MenuComponent> existingComponents = repository.findByParentAndRole(request.getParentMenuItemId(),
						group.getSerComponenetKindRoleId());

//				Map<Long, MenuComponent> existingChildMap = existingComponents.stream()
//						.filter(c -> c.getChildMenuItem() != null)
//						.collect(Collectors.toMap(c -> c.getChildMenuItem().getSerMenuItemId(), c -> c));

				// Process each item in the group
				int itemOrder = 1;
				for (DtoMenuItem itemDto : group.getComponentItems()) {
					MenuComponent component;

					// Check if this child item already exists in this group
//					if (itemDto.getSerMenuItemId() != null
//							&& existingChildMap.containsKey(itemDto.getSerMenuItemId())) {
//						// Update existing component
//						component = existingChildMap.get(itemDto.getSerMenuItemId());
//						component.setUpdatedDate(new Date());
//						LOGGER.debug("Updating existing component for item: {}", itemDto.getSerMenuItemId());
//					} else {
						// Create new component
						component = new MenuComponent();
						component.setBlnIsDeleted(false);
						component.setBlnIsActive(true);
						component.setBlnIsApproved(true);
						component.setCreatedDate(new Date());
						component.setParentMenuItem(parentMenuItem);

						// Set child menu item if provided
						if (itemDto.getSerMenuItemId() != null) {
							MenuItem childItem = null;
							Optional<MenuItem> optChildItem = repositoryMenuItem
									.getByMenuItemId(itemDto.getSerMenuItemId());
							if (optChildItem == null || optChildItem.isEmpty()) {
								return new DtoResult("Child menu item not found: " + itemDto.getSerMenuItemId(), null,
										null, null);
							} else {
								childItem = optChildItem.get();
							}
							component.setChildMenuItem(childItem);
						}

						LOGGER.debug("Creating new component for item in group: {}", group.getTxtDisplayName());
//					}

					// Set component properties
					component.setComponenetKindRole(componentRole);
					component.setTxtDisplayName(group.getTxtDisplayName());
					component.setNumSelectionMin(group.getNumSelectionMin());
					component.setNumSelectionMax(group.getNumSelectionMax());
					component
							.setNumSequenceOrder(group.getNumSequenceOrder() != null ? group.getNumSequenceOrder() : 0);

					// Set item-specific sequence order
					component.setNumSequenceOrder(itemOrder++);

					// Save component
					MenuComponent savedComponent = repository.save(component);
					allSavedComponents.add(MapperMenuComponent.toDto(savedComponent));

//					// Remove from existing map to track processed items
//					if (itemDto.getSerMenuItemId() != null) {
//						existingChildMap.remove(itemDto.getSerMenuItemId());
//					}
				}

				// Delete components for items that were removed from this group
//				for (MenuComponent componentToDelete : existingChildMap.values()) {
//					LOGGER.info("Soft deleting component for removed item: {}",
//							componentToDelete.getChildMenuItem().getSerMenuItemId());
//					componentToDelete.setBlnIsDeleted(true);
//					componentToDelete.setUpdatedDate(new Date());
//					repository.save(componentToDelete);
//				}
			}

			// Handle groups that were completely removed
			List<MenuItemRole> remainingRoles = request.getComponents().stream()
					.map(g -> serviceMenuItemRole.getMenuItemRoleById(g.getSerComponenetKindRoleId()))
					.collect(Collectors.toList());

			List<MenuComponent> allExistingComponents = repository
					.findByParentMenuItemId(request.getParentMenuItemId());
			for (MenuComponent existingComponent : allExistingComponents) {
				if (existingComponent.getComponenetKindRole() != null
						&& !remainingRoles.contains(existingComponent.getComponenetKindRole())
						&& !existingComponent.getBlnIsDeleted()) {
					LOGGER.info("Soft deleting component for removed group: {}",
							existingComponent.getComponenetKindRole().getTxtRoleName());
					existingComponent.setBlnIsDeleted(true);
					existingComponent.setUpdatedDate(new Date());
					repository.save(existingComponent);
				}
			}

			result.setTxtMessage("Components saved successfully");
			result.setResult(allSavedComponents);

		} catch (Exception e) {
			LOGGER.error("Error saving components", e);
			throw new RuntimeException("Failed to save components: " + e.getMessage());
		}

		return result;
	}

	@Override
	public List<DtoMenuComponent> getComponentsByParentId(Long parentMenuItemId) {
		LOGGER.info("Fetching components grouped by kind for parent: {}", parentMenuItemId);

		try {
			// Get distinct component kinds for this parent
			List<MenuItemRole> componentKinds = repository.findDistinctComponentKindsByParent(parentMenuItemId);

			List<DtoMenuComponent> groups = new ArrayList<>();

			for (MenuItemRole role : componentKinds) {
				// Get all components for this parent and role
				List<MenuComponent> components = repository.findByParentAndRole(parentMenuItemId,
						role.getSerMenuItemRoleId());

				if (!components.isEmpty()) {
					DtoMenuComponent group = new DtoMenuComponent();
					group.setSerComponenetKindRoleId(role.getSerMenuItemRoleId());

					// Use display name from first component (should be same for all in group)
					if (components.get(0).getTxtDisplayName() != null) {
						group.setTxtDisplayName(components.get(0).getTxtDisplayName());
					} else {
						group.setTxtDisplayName(role.getTxtRoleName());
					}

					// Use selection limits from first component
					group.setNumSelectionMin(components.get(0).getNumSelectionMin());
					group.setNumSelectionMax(components.get(0).getNumSelectionMax());
					group.setNumSequenceOrder(components.get(0).getNumSequenceOrder());

					// Map child items
					List<DtoMenuItem> items = components.stream().filter(c -> c.getChildMenuItem() != null)
							.map(MenuComponent::getChildMenuItem).map(MapperMenuItem::toDto)
							.collect(Collectors.toList());

					group.setComponentItems(items);
					groups.add(group);
				}
			}

			// Sort groups by sequence order
			groups.sort((g1, g2) -> {
				Integer order1 = g1.getNumSequenceOrder() != null ? g1.getNumSequenceOrder() : 0;
				Integer order2 = g2.getNumSequenceOrder() != null ? g2.getNumSequenceOrder() : 0;
				return order1.compareTo(order2);
			});

			return groups;

		} catch (Exception e) {
			LOGGER.error("Error fetching components", e);
			throw new RuntimeException("Failed to fetch components: " + e.getMessage());
		}
	}

	@Override
	public DtoResult deleteComponent(Long componentId) {
		// Same as before
		LOGGER.info("Deleting component: {}", componentId);

		DtoResult result = new DtoResult();

		try {
			MenuComponent component = repository.findById(componentId)
					.orElseThrow(() -> new RuntimeException("Component not found"));

			component.setBlnIsDeleted(true);
			component.setUpdatedDate(new Date());
			repository.save(component);

			result.setTxtMessage("Component deleted successfully");
			result.setResult(true);

		} catch (Exception e) {
			LOGGER.error("Error deleting component", e);
			throw new RuntimeException("Failed to delete component: " + e.getMessage());
		}

		return result;
	}

	@Override
	public DtoResult deleteComponentsByGroup(Long parentMenuItemId, Integer roleId) {
		LOGGER.info("Deleting all components for parent: {} and role: {}", parentMenuItemId, roleId);

		DtoResult result = new DtoResult();

		try {
			List<MenuComponent> components = repository.findByParentAndRole(parentMenuItemId, roleId);

			for (MenuComponent component : components) {
				component.setBlnIsDeleted(true);
				component.setUpdatedDate(new Date());
				repository.save(component);
			}

			result.setTxtMessage("Group components deleted successfully");
			result.setResult(components.size());

		} catch (Exception e) {
			LOGGER.error("Error deleting group components", e);
			throw new RuntimeException("Failed to delete group components: " + e.getMessage());
		}

		return result;
	}

	@Override
	public List<DtoMenuItemRole> getAvailableComponentRoles() {
		LOGGER.info("Fetching available component roles");

		try {
			return serviceMenuItemRole.getAllActiveCompositionRoles();

		} catch (Exception e) {
			LOGGER.error("Error fetching component roles", e);
			throw new RuntimeException("Failed to fetch component roles: " + e.getMessage());
		}
	}

	@Override
	public List<DtoMenuItemRole> getUsedComponentRoles(Long parentMenuItemId) {
		LOGGER.info("Fetching used component roles for parent: {}", parentMenuItemId);

		try {
			List<MenuItemRole> roles = repository.findDistinctComponentKindsByParent(parentMenuItemId);
			return roles.stream().map(this::toRoleDto).collect(Collectors.toList());

		} catch (Exception e) {
			LOGGER.error("Error fetching used component roles", e);
			throw new RuntimeException("Failed to fetch used component roles: " + e.getMessage());
		}
	}

	@Override
	public Map<String, Object> validateComponentGroup(DtoMenuComponent group) {
		Map<String, Object> validationResult = new HashMap<>();
		List<String> errors = new ArrayList<>();

		// Validate role
		if (group.getSerComponenetKindRoleId() == null) {
			errors.add("Component role is required");
		}

		// Validate display name
		if (group.getTxtDisplayName() == null || group.getTxtDisplayName().trim().isEmpty()) {
			errors.add("Display name is required");
		}

		// Validate selection range
		if (group.getNumSelectionMin() != null && group.getNumSelectionMax() != null) {
			if (group.getNumSelectionMin() > group.getNumSelectionMax()) {
				errors.add("Minimum selection cannot be greater than maximum selection");
			}
			if (group.getNumSelectionMin() < 0) {
				errors.add("Minimum selection cannot be negative");
			}
		}

		// Validate items
		if (group.getComponentItems() == null || group.getComponentItems().isEmpty()) {
			errors.add("At least one item is required in the group");
		} else {
			// Check for duplicate items
			List<Long> itemIds = group.getComponentItems().stream().filter(item -> item.getSerMenuItemId() != null)
					.map(DtoMenuItem::getSerMenuItemId).collect(Collectors.toList());

			if (itemIds.size() != new HashSet<>(itemIds).size()) {
				errors.add("Duplicate items found in the group");
			}
		}

		validationResult.put("isValid", errors.isEmpty());
		validationResult.put("errors", errors);

		return validationResult;
	}

	private DtoMenuItemRole toRoleDto(MenuItemRole entity) {
		DtoMenuItemRole dto = new DtoMenuItemRole();
		dto.setSerMenuItemRoleId(entity.getSerMenuItemRoleId());
		dto.setTxtRoleCode(entity.getTxtRoleCode());
		dto.setTxtRoleName(entity.getTxtRoleName());
		dto.setBlnIsCompositionRole(entity.getBlnIsCompositionRole());
		dto.setBlnIsActive(entity.getBlnIsActive());

		if (entity.getParentMenuItemRole() != null) {
			dto.setParentMenuRoleId(entity.getParentMenuItemRole().getSerMenuItemRoleId());
		}

		return dto;
	}
	
	
	@Override
	public List<DtoMenuComponentRequest> getAllCompositesWithComponents() {
	    LOGGER.info("Fetching all composite menu items with their components");
	    
	    try {
	        // Get all composite menu items
	        List<MenuItem> compositeItems = repositoryMenuItem.findAllCompositeItems();
	        
	        List<DtoMenuComponentRequest> result = new ArrayList<>();
	        
	        for (MenuItem compositeItem : compositeItems) {
	            DtoMenuComponentRequest request = getCompositeWithComponents(compositeItem.getSerMenuItemId());
	            if (request != null) {
	                result.add(request);
	            }
	        }
	        
	        return result;
	        
	    } catch (Exception e) {
	        LOGGER.error("Error fetching all composites with components", e);
	        throw new RuntimeException("Failed to fetch all composites with components: " + e.getMessage());
	    }
	}

	@Override
	public DtoMenuComponentRequest getCompositeWithComponents(Long parentMenuItemId) {
	    LOGGER.info("Fetching composite with components for ID: {}", parentMenuItemId);
	    
	    try {
	        // Get the menu item
	        MenuItem menuItem = repositoryMenuItem.getByMenuItemId(parentMenuItemId)
	                .orElseThrow(() -> new RuntimeException("Menu item not found: " + parentMenuItemId));
	        
	        // Check if it's a composite
	        if (!Boolean.TRUE.equals(menuItem.getBlnIsComposite())) {
	            LOGGER.warn("Menu item {} is not a composite", parentMenuItemId);
	            return null;
	        }
	        
	        // Create the request DTO
	        DtoMenuComponentRequest request = new DtoMenuComponentRequest();
	        request.setParentMenuItemId(parentMenuItemId);
	        
	        // Get components for this parent
	        List<DtoMenuComponent> components = getComponentsByParentId(parentMenuItemId);
	        request.setComponents(components);
	        
	        return request;
	        
	    } catch (Exception e) {
	        LOGGER.error("Error fetching composite with components for ID: {}", parentMenuItemId, e);
	        throw new RuntimeException("Failed to fetch composite with components: " + e.getMessage());
	    }
	}

	@Override
	public List<DtoMenuComponentRequest> getAllActiveCompositesWithComponents() {
	    LOGGER.info("Fetching all active composite menu items with their components");
	    
	    try {
	        // Get all active composite menu items
	        List<MenuItem> compositeItems = repositoryMenuItem.findAllActiveCompositeItems();
	        
	        List<DtoMenuComponentRequest> result = new ArrayList<>();
	        
	        for (MenuItem compositeItem : compositeItems) {
	            DtoMenuComponentRequest request = getCompositeWithComponents(compositeItem.getSerMenuItemId());
	            if (request != null) {
	                result.add(request);
	            }
	        }
	        
	        return result;
	        
	    } catch (Exception e) {
	        LOGGER.error("Error fetching all active composites with components", e);
	        throw new RuntimeException("Failed to fetch all active composites with components: " + e.getMessage());
	    }
	}
	
}