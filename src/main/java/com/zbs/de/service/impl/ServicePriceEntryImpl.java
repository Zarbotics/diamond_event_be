package com.zbs.de.service.impl;

import com.opencsv.CSVWriter;
import com.zbs.de.model.*;
import com.zbs.de.model.dto.*;
import com.zbs.de.model.dto.price.*;
import com.zbs.de.repository.*;
import com.zbs.de.service.*;
import com.zbs.de.util.enums.EnmApplyScope;
import com.zbs.de.util.enums.EnmItineraryUnitType;
import com.zbs.de.util.enums.EnmPriceCalculationMethod;
import com.zbs.de.util.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

@Service("servicePriceEntryImpl")
public class ServicePriceEntryImpl implements ServicePriceEntry {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServicePriceEntryImpl.class);

	@Autowired
	private RepositoryPriceEntry repository;

	@Autowired
	private RepositoryPriceEntry priceEntryRepo;

	@Autowired
	private RepositoryPriceVersion priceVersionRepo;

	@Autowired
	private RepositoryMenuItem menuItemRepo;

	@Autowired
	private RepositoryMenuComponent menuComponentRepo;

	@Autowired
	private RepositoryMenuItemRole menuItemRoleRepo;

	@Autowired
	private ServicePricingEngine servicePricingEngine;

	// 1. Get Menu Tree with Prices
	@Override
	@Transactional(readOnly = true)
	public DtoPriceMatrixTree getMenuTreeWithPrices(Long versionId, String roleFilter, String typeFilter) {
		LOGGER.info("Getting menu tree with prices for version: {}", versionId);

		// Get price version
		PriceVersion priceVersion = priceVersionRepo.findById(versionId)
				.orElseThrow(() -> new NotFoundException("Price version not found"));

		// Get all menu items
		List<MenuItem> allItems = menuItemRepo.getAllActiveMenuItems();

		// Filter if needed
		if (roleFilter != null && !roleFilter.isEmpty()) {
			allItems = allItems.stream().filter(item -> roleFilter.equals(item.getTxtRole()))
					.collect(Collectors.toList());
		}

		if (typeFilter != null && !typeFilter.isEmpty()) {
			allItems = allItems.stream().filter(item -> typeFilter.equals(item.getTxtType()))
					.collect(Collectors.toList());
		}

		// Get all price entries for this version
		List<PriceEntry> priceEntries = priceEntryRepo.findByPriceVersionSerPriceVersionId(versionId);
		Map<Long, PriceEntry> itemPriceMap = priceEntries.stream()
				.filter(pe -> pe.getApplyScope() == EnmApplyScope.ITEM && pe.getNumTargetId() != null)
				.collect(Collectors.toMap(PriceEntry::getNumTargetId, pe -> pe));

		// Build tree
		List<DtoPriceMatrixMenuItem> rootItems = buildMenuTreeWithPrices(null, allItems, itemPriceMap);

		DtoPriceMatrixTree result = new DtoPriceMatrixTree();
		result.setVersionId(versionId);
		result.setVersionName(priceVersion.getTxtName());
		result.setVersionCode(priceVersion.getTxtVersionCode());
		result.setMenuItems(rootItems);

		return result;
	}

	private List<DtoPriceMatrixMenuItem> buildMenuTreeWithPrices(Long parentId, List<MenuItem> allItems,
			Map<Long, PriceEntry> priceMap) {
		return allItems.stream().filter(item -> {
			Long itemParentId = item.getParent() != null ? item.getParent().getSerMenuItemId() : null;
			return Objects.equals(itemParentId, parentId);
		}).map(item -> {
			DtoPriceMatrixMenuItem dto = new DtoPriceMatrixMenuItem();
			dto.setSerMenuItemId(item.getSerMenuItemId());
			dto.setTxtCode(item.getTxtCode());
			dto.setTxtName(item.getTxtName());
			dto.setTxtRole(item.getTxtRole());
			dto.setTxtType(item.getTxtType());
			dto.setTxtPath(item.getTxtPath());
			dto.setParentId(item.getParent() != null ? item.getParent().getSerMenuItemId() : null);
			dto.setBlnIsSelectable(item.getBlnIsSelectable());
			dto.setBlnIsComposite(item.getBlnIsComposite());

			// Set price info
			PriceEntry priceEntry = priceMap.get(item.getSerMenuItemId());
			if (priceEntry != null) {
				DtoPriceEntryInfo priceInfo = new DtoPriceEntryInfo();
				priceInfo.setSerPriceEntryId(priceEntry.getSerPriceEntryId());
				priceInfo.setNumPrice(priceEntry.getNumPrice());
				priceInfo.setTxtCurrency(priceEntry.getTxtCurrency());
				priceInfo.setApplyScope(priceEntry.getApplyScope() != null ? priceEntry.getApplyScope().name() : null);
				priceInfo.setUnit(priceEntry.getUnit() != null ? priceEntry.getUnit().name() : null);
				priceInfo.setNumMinQuantity(priceEntry.getNumMinQuantity());
				priceInfo.setNumMaxQuantity(priceEntry.getNumMaxQuantity());
				priceInfo.setCalculationMethod(
						priceEntry.getCalculationMethod() != null ? priceEntry.getCalculationMethod().name() : null);
				dto.setPriceInfo(priceInfo);
				dto.setHasPrice(true);
			} else {
				dto.setHasPrice(false);
			}

			// Recursively build children
			dto.setChildren(buildMenuTreeWithPrices(item.getSerMenuItemId(), allItems, priceMap));

			return dto;
		}).collect(Collectors.toList());
	}

	// 2. Get Price Entries by Version (Grouped by Scope)
	@Override
	@Transactional(readOnly = true)
	public DtoPriceMatrixEntries getPriceEntriesByVersion(Long versionId, String scope) {
		LOGGER.info("Getting price entries for version: {}, scope: {}", versionId, scope);

		List<PriceEntry> allEntries;
		if (scope != null && !scope.isEmpty()) {
			EnmApplyScope applyScope = EnmApplyScope.of(scope.toUpperCase());
			allEntries = priceEntryRepo.findByPriceVersionSerPriceVersionIdAndApplyScope(versionId, applyScope);
		} else {
			allEntries = priceEntryRepo.findByPriceVersionSerPriceVersionId(versionId);
		}

		DtoPriceMatrixEntries result = new DtoPriceMatrixEntries();

		// Group by scope
		result.setItemScopeEntries(mapToGridEntries(
				allEntries.stream().filter(e -> e.getApplyScope() == EnmApplyScope.ITEM).collect(Collectors.toList())));
		result.setRoleScopeEntries(mapToGridEntries(
				allEntries.stream().filter(e -> e.getApplyScope() == EnmApplyScope.ROLE).collect(Collectors.toList())));
		result.setTypeScopeEntries(mapToGridEntries(
				allEntries.stream().filter(e -> e.getApplyScope() == EnmApplyScope.TYPE).collect(Collectors.toList())));
		result.setBundleScopeEntries(mapToGridEntries(allEntries.stream()
				.filter(e -> e.getApplyScope() == EnmApplyScope.BUNDLE).collect(Collectors.toList())));
		result.setStationScopeEntries(mapToGridEntries(allEntries.stream()
				.filter(e -> e.getApplyScope() == EnmApplyScope.STATION).collect(Collectors.toList())));

		result.setTotalCount(allEntries.size());

		return result;
	}

	private List<DtoPriceEntryGrid> mapToGridEntries(List<PriceEntry> entries) {
		return entries.stream().map(entry -> {
			DtoPriceEntryGrid grid = new DtoPriceEntryGrid();
			grid.setSerPriceEntryId(entry.getSerPriceEntryId());
			grid.setNumTargetId(entry.getNumTargetId());
			grid.setApplyScope(entry.getApplyScope() != null ? entry.getApplyScope().name() : null);
			grid.setNumPrice(entry.getNumPrice());
			grid.setTxtCurrency(entry.getTxtCurrency());
			grid.setUnit(entry.getUnit() != null ? entry.getUnit().name() : null);
			grid.setNumMinQuantity(entry.getNumMinQuantity());
			grid.setNumMaxQuantity(entry.getNumMaxQuantity());
			grid.setCalculationMethod(
					entry.getCalculationMethod() != null ? entry.getCalculationMethod().name() : null);
			grid.setMetadata(entry.getMetadata());

			// Get target name based on scope
			switch (entry.getApplyScope()) {
			case ITEM:
				MenuItem menuItem = menuItemRepo.findById(entry.getNumTargetId()).orElse(null);
				if (menuItem != null) {
					grid.setTxtTargetName(menuItem.getTxtName());
					grid.setTxtTargetCode(menuItem.getTxtCode());
				}
				break;
			case ROLE:
				MenuItemRole role = menuItemRoleRepo.findById(entry.getNumTargetId()).orElse(null);
				if (role != null) {
					grid.setTxtTargetName("All " + role.getTxtRoleName() + " items");
					grid.setTxtTargetCode(role.getTxtRoleCode());
				}
				break;
			case BUNDLE:
				MenuItem bundle = menuItemRepo.findById(entry.getNumTargetId()).orElse(null);
				if (bundle != null) {
					grid.setTxtTargetName("Bundle: " + bundle.getTxtName());
					grid.setTxtTargetCode(bundle.getTxtCode());
				}
				break;
			default:
				grid.setTxtTargetName("Scope: " + entry.getApplyScope());
				grid.setTxtTargetCode(String.valueOf(entry.getNumTargetId()));
			}

			return grid;
		}).collect(Collectors.toList());
	}

	// 3. Get Available Filters
	@Override
	@Transactional(readOnly = true)
	public DtoPriceMatrixFilters getAvailableFilters() {
		LOGGER.info("Getting available filters for price matrix");

		DtoPriceMatrixFilters filters = new DtoPriceMatrixFilters();

		// Get roles
		List<MenuItemRole> roles = menuItemRoleRepo
				.findByBlnIsActiveTrueAndBlnIsDeletedFalseOrderBySerMenuItemRoleIdDesc();
		filters.setRoles(roles.stream().map(role -> {
			DtoMenuItemRole dto = new DtoMenuItemRole();
			dto.setSerMenuItemRoleId(role.getSerMenuItemRoleId());
			dto.setTxtRoleCode(role.getTxtRoleCode());
			dto.setTxtRoleName(role.getTxtRoleName());
			return dto;
		}).collect(Collectors.toList()));

		// Get unique types from menu items
		List<String> types = menuItemRepo.findDistinctTxtTypes();
		filters.setTypes(types);

		// Get stations (menu items with role=STATION)
		List<MenuItem> stations = menuItemRepo.findByTxtRoleAndBlnIsDeletedFalse("STATION");
		filters.setStations(stations.stream().map(station -> {
			DtoStationInfo dto = new DtoStationInfo();
			dto.setId(station.getSerMenuItemId());
			dto.setName(station.getTxtName());
			dto.setCode(station.getTxtCode());
			return dto;
		}).collect(Collectors.toList()));

		// Get bundles (composite items)
		List<MenuItem> bundles = menuItemRepo.findAllActiveCompositeItems();
		filters.setBundles(bundles.stream().map(bundle -> {
			DtoBundleInfo dto = new DtoBundleInfo();
			dto.setSerMenuItemId(bundle.getSerMenuItemId());
			dto.setTxtCode(bundle.getTxtCode());
			dto.setTxtName(bundle.getTxtName());
			return dto;
		}).collect(Collectors.toList()));

		return filters;
	}

	// 4. Bulk Assign Prices (Main Functionality)
	@Override
	@Transactional
	public DtoBulkAssignResult bulkAssignPrices(Long versionId, DtoBulkAssignRequest request) {
		LOGGER.info("Bulk assigning prices for version: {}, type: {}", versionId, request.getAssignmentType());

		DtoBulkAssignResult result = new DtoBulkAssignResult();
		List<DtoBulkAssignResult.FailedItem> failedItems = new ArrayList<>();
		int createdCount = 0;
		int updatedCount = 0;

		// Get price version
		PriceVersion priceVersion = priceVersionRepo.findById(versionId)
				.orElseThrow(() -> new NotFoundException("Price version not found"));

		// Get target menu item IDs based on assignment type
		List<Long> targetMenuItemIds = getTargetMenuItemIds(request);

		if (targetMenuItemIds.isEmpty()) {
			result.setSuccess(false);
			result.setMessage("No target items found for the specified criteria");
			return result;
		}

		// Process each target item
		for (Long menuItemId : targetMenuItemIds) {
			try {
				MenuItem menuItem = menuItemRepo.findById(menuItemId)
						.orElseThrow(() -> new RuntimeException("Menu item not found: " + menuItemId));

				// Determine scope and target ID
				EnmApplyScope applyScope = EnmApplyScope.of(request.getScope());
				Long targetId;

				switch (applyScope) {
				case ITEM:
					targetId = menuItemId;
					break;
				case ROLE:
					targetId = menuItem.getMenuItemRole() != null
							? menuItem.getMenuItemRole().getSerMenuItemRoleId().longValue()
							: null;
					break;
				case TYPE:
					targetId = null; // TYPE uses metadata, not targetId
					break;
				case BUNDLE:
					// Find parent bundle if this is a component
					List<MenuComponent> components = menuComponentRepo.findByChildMenuItem_SerMenuItemId(menuItemId);
					targetId = components.isEmpty() ? null : components.get(0).getParentMenuItem().getSerMenuItemId();
					break;
				case STATION:
					targetId = findStationId(menuItem);
					break;
				default:
					targetId = menuItemId;
				}

				if (targetId == null && applyScope != EnmApplyScope.TYPE) {
					failedItems.add(createFailedItem(menuItem, "Cannot determine target ID for scope: " + applyScope));
					continue;
				}

				// Check if price entry already exists
				boolean exists = repository.existsByVersionTargetAndScope(versionId, targetId, applyScope.name());

				if (exists && Boolean.TRUE.equals(request.getReplaceExisting())) {
					// Update existing
					List<PriceEntry> existingEntries = repository.findByVersionTargetAndScope(versionId, targetId,
							applyScope.name());
					for (PriceEntry entry : existingEntries) {
						updatePriceEntry(entry, request.getPriceData(), applyScope, targetId);
						updatedCount++;
					}
				} else if (!exists) {
					// Create new
					createPriceEntry(priceVersion, applyScope, targetId, request.getPriceData(), request.getMetadata());
					createdCount++;
				}
				// else: exists but replaceExisting=false, skip

			} catch (Exception e) {
				LOGGER.error("Failed to assign price for menu item: {}", menuItemId, e);
				MenuItem item = menuItemRepo.findById(menuItemId).orElse(null);
				failedItems.add(createFailedItem(item, e.getMessage()));
			}
		}

		result.setSuccess(failedItems.isEmpty());
		result.setMessage(
				String.format("Created: %d, Updated: %d, Failed: %d", createdCount, updatedCount, failedItems.size()));
		result.setCreatedCount(createdCount);
		result.setUpdatedCount(updatedCount);
		result.setFailedItems(failedItems);

		return result;
	}

	private List<Long> getTargetMenuItemIds(DtoBulkAssignRequest request) {
		switch (request.getAssignmentType()) {
		case "SELECTED_ITEMS":
			return request.getTargetIds() != null ? request.getTargetIds() : new ArrayList<>();

		case "BY_ROLE":
			if (request.getScopeValue() == null) {
				throw new IllegalArgumentException("scopeValue is required for BY_ROLE assignment");
			}
			return menuItemRepo.findByTxtRoleAndBlnIsDeletedFalse(request.getScopeValue()).stream()
					.map(MenuItem::getSerMenuItemId).collect(Collectors.toList());

		case "BY_TYPE":
			if (request.getScopeValue() == null) {
				throw new IllegalArgumentException("scopeValue is required for BY_TYPE assignment");
			}
			return menuItemRepo.findByTxtTypeAndBlnIsDeletedFalse(request.getScopeValue()).stream()
					.map(MenuItem::getSerMenuItemId).collect(Collectors.toList());

		case "BY_BUNDLE":
			if (request.getScopeValue() == null || request.getTargetIds() == null || request.getTargetIds().isEmpty()) {
				throw new IllegalArgumentException("bundleId is required for BY_BUNDLE assignment");
			}
			Long bundleId = request.getTargetIds().get(0);
			return menuComponentRepo.findByParentMenuItem_SerMenuItemId(bundleId).stream()
					.map(c -> c.getChildMenuItem().getSerMenuItemId()).filter(Objects::nonNull)
					.collect(Collectors.toList());

		default:
			throw new IllegalArgumentException("Unknown assignment type: " + request.getAssignmentType());
		}
	}

	private Long findStationId(MenuItem menuItem) {
		// Traverse up the hierarchy to find STATION role
		MenuItem current = menuItem;
		while (current != null) {
			if ("STATION".equals(current.getTxtRole())) {
				return current.getSerMenuItemId();
			}
			current = current.getParent();
		}
		return null;
	}

	private DtoBulkAssignResult.FailedItem createFailedItem(MenuItem item, String error) {
		DtoBulkAssignResult.FailedItem failed = new DtoBulkAssignResult.FailedItem();
		failed.setItemId(item != null ? item.getSerMenuItemId() : null);
		failed.setItemCode(item != null ? item.getTxtCode() : "Unknown");
		failed.setError(error);
		return failed;
	}

	private void createPriceEntry(PriceVersion priceVersion, EnmApplyScope scope, Long targetId, DtoPriceData priceData,
			Map<String, Object> metadata) {
		PriceEntry entry = new PriceEntry();
		entry.setPriceVersion(priceVersion);
		entry.setApplyScope(scope);
		entry.setNumTargetId(targetId);
		entry.setNumPrice(priceData.getNumPrice());
		entry.setTxtCurrency(priceData.getTxtCurrency());
		entry.setUnit(EnmItineraryUnitType.of(priceData.getUnit()));
		entry.setNumMinQuantity(priceData.getNumMinQuantity());
		entry.setNumMaxQuantity(priceData.getNumMaxQuantity());
		entry.setCalculationMethod(EnmPriceCalculationMethod.of(priceData.getCalculationMethod()));
		entry.setMetadata(metadata != null ? metadata : new HashMap<>());
		entry.setBlnIsDeleted(false);
		entry.setBlnIsActive(true);

		repository.save(entry);
	}

	private void updatePriceEntry(PriceEntry entry, DtoPriceData priceData, EnmApplyScope scope, Long targetId) {
		entry.setApplyScope(scope);
		entry.setNumTargetId(targetId);
		entry.setNumPrice(priceData.getNumPrice());
		entry.setTxtCurrency(priceData.getTxtCurrency());
		entry.setUnit(EnmItineraryUnitType.of(priceData.getUnit()));
		entry.setNumMinQuantity(priceData.getNumMinQuantity());
		entry.setNumMaxQuantity(priceData.getNumMaxQuantity());
		entry.setCalculationMethod(EnmPriceCalculationMethod.of(priceData.getCalculationMethod()));
		entry.setUpdatedDate(new Date());

		repository.save(entry);
	}

	// 5. Bulk Update Price Entries
	@Override
	@Transactional
	public DtoResult bulkUpdatePriceEntries(Long versionId, List<DtoPriceEntryUpdate> updates) {
		LOGGER.info("Bulk updating {} price entries for version: {}", updates.size(), versionId);

		int updatedCount = 0;
		List<String> errors = new ArrayList<>();

		for (DtoPriceEntryUpdate update : updates) {
			try {
				PriceEntry entry = priceEntryRepo.findById(update.getSerPriceEntryId()).orElseThrow(
						() -> new NotFoundException("Price entry not found: " + update.getSerPriceEntryId()));

				// Verify it belongs to the correct version
				if (!entry.getPriceVersion().getSerPriceVersionId().equals(versionId)) {
					errors.add(
							"Price entry " + update.getSerPriceEntryId() + " does not belong to version " + versionId);
					continue;
				}

				// Update fields
				if (update.getNumPrice() != null)
					entry.setNumPrice(update.getNumPrice());
				if (update.getTxtCurrency() != null)
					entry.setTxtCurrency(update.getTxtCurrency());
				if (update.getNumMinQuantity() != null)
					entry.setNumMinQuantity(update.getNumMinQuantity());
				if (update.getNumMaxQuantity() != null)
					entry.setNumMaxQuantity(update.getNumMaxQuantity());
				if (update.getMetadata() != null)
					entry.setMetadata(update.getMetadata());

				entry.setUpdatedDate(new Date());
				priceEntryRepo.save(entry);
				updatedCount++;

			} catch (Exception e) {
				LOGGER.error("Failed to update price entry: {}", update.getSerPriceEntryId(), e);
				errors.add("Entry " + update.getSerPriceEntryId() + ": " + e.getMessage());
			}
		}

		DtoResult result = new DtoResult();
		if (errors.isEmpty()) {
			result.setTxtMessage("Successfully updated " + updatedCount + " price entries");
			result.setResult(updatedCount);
		} else {
			result.setTxtMessage("Updated " + updatedCount + " entries, failed: " + errors.size());
			result.setResult(errors);
		}

		return result;
	}

	// 6. Bulk Delete Price Entries
	@Override
	@Transactional
	public DtoResult bulkDeletePriceEntries(Long versionId, List<Long> priceEntryIds) {
		LOGGER.info("Bulk deleting {} price entries for version: {}", priceEntryIds.size(), versionId);

		int deletedCount = 0;
		List<String> errors = new ArrayList<>();

		for (Long entryId : priceEntryIds) {
			try {
				PriceEntry entry = priceEntryRepo.findById(entryId)
						.orElseThrow(() -> new NotFoundException("Price entry not found: " + entryId));

				// Verify it belongs to the correct version
				if (!entry.getPriceVersion().getSerPriceVersionId().equals(versionId)) {
					errors.add("Price entry " + entryId + " does not belong to version " + versionId);
					continue;
				}

				// Soft delete
				entry.setBlnIsDeleted(true);
				entry.setUpdatedDate(new Date());
				priceEntryRepo.save(entry);
				deletedCount++;

			} catch (Exception e) {
				LOGGER.error("Failed to delete price entry: {}", entryId, e);
				errors.add("Entry " + entryId + ": " + e.getMessage());
			}
		}

		DtoResult result = new DtoResult();
		if (errors.isEmpty()) {
			result.setTxtMessage("Successfully deleted " + deletedCount + " price entries");
			result.setResult(deletedCount);
		} else {
			result.setTxtMessage("Deleted " + deletedCount + " entries, failed: " + errors.size());
			result.setResult(errors);
		}

		return result;
	}

	// 7. Export Price Matrix to CSV
	@Override
	@Transactional(readOnly = true)
	public DtoResult exportPriceMatrixToCsv(Long versionId, String scope, Boolean includeMenuInfo) {
		LOGGER.info("Exporting price matrix to CSV for version: {}", versionId);

		try {
			// Get price entries
			List<PriceEntry> entries;
			if (scope != null && !scope.isEmpty()) {
				EnmApplyScope applyScope = EnmApplyScope.of(scope.toUpperCase());
				entries = priceEntryRepo.findByPriceVersionSerPriceVersionIdAndApplyScope(versionId, applyScope);
			} else {
				entries = priceEntryRepo.findByPriceVersionSerPriceVersionId(versionId);
			}

			// Create CSV
			StringWriter stringWriter = new StringWriter();
			CSVWriter csvWriter = new CSVWriter(stringWriter);

			// Write header
			String[] header = { "menu_item_id", "item_code", "item_name", "item_role", "item_type", "price", "currency",
					"min_qty", "max_qty", "unit", "scope", "calculation_method", "notes" };
			csvWriter.writeNext(header);

			// Write data
			for (PriceEntry entry : entries) {
				String[] row = new String[header.length];

				// Get menu item info if available
				String itemCode = "";
				String itemName = "";
				String itemRole = "";
				String itemType = "";

				if (entry.getApplyScope() == EnmApplyScope.ITEM && entry.getNumTargetId() != null) {
					MenuItem item = menuItemRepo.findById(entry.getNumTargetId()).orElse(null);
					if (item != null) {
						itemCode = item.getTxtCode();
						itemName = item.getTxtName();
						itemRole = item.getTxtRole();
						itemType = item.getTxtType();
					}
				}

				row[0] = entry.getNumTargetId() != null ? String.valueOf(entry.getNumTargetId()) : "";
				row[1] = itemCode;
				row[2] = itemName;
				row[3] = itemRole;
				row[4] = itemType;
				row[5] = entry.getNumPrice() != null ? String.valueOf(entry.getNumPrice()) : "";
				row[6] = entry.getTxtCurrency();
				row[7] = entry.getNumMinQuantity() != null ? String.valueOf(entry.getNumMinQuantity()) : "";
				row[8] = entry.getNumMaxQuantity() != null ? String.valueOf(entry.getNumMaxQuantity()) : "";
				row[9] = entry.getUnit() != null ? entry.getUnit().name() : "";
				row[10] = entry.getApplyScope() != null ? entry.getApplyScope().name() : "";
				row[11] = entry.getCalculationMethod() != null ? entry.getCalculationMethod().name() : "";
				row[12] = entry.getMetadata() != null && entry.getMetadata().containsKey("notes")
						? String.valueOf(entry.getMetadata().get("notes"))
						: "";

				csvWriter.writeNext(row);
			}

			csvWriter.close();

			DtoResult result = new DtoResult();
			result.setTxtMessage("Export successful");
			result.setResult(stringWriter.toString());

			return result;

		} catch (Exception e) {
			LOGGER.error("Failed to export price matrix", e);
			throw new RuntimeException("Export failed: " + e.getMessage());
		}
	}

	// 8. Quick Price Preview (delegates to existing service)
	@Override
	@Transactional(readOnly = true)
	public DtoPricePreviewRequest quickPricePreview(DtoPricePreviewRequest request) {
		return servicePricingEngine.preview(request);
	}

	// 10. Get Quantity Breaks for Item
	@Override
	@Transactional(readOnly = true)
	public List<DtoPriceEntryGrid> getQuantityBreaksForItem(Long versionId, Long itemId) {
		LOGGER.info("Getting quantity breaks for item: {} in version: {}", itemId, versionId);

		List<PriceEntry> entries = repository.findByVersionTargetAndScope(versionId, itemId, "ITEM");

		// Sort by min quantity
		entries.sort((e1, e2) -> {
			Integer min1 = e1.getNumMinQuantity() != null ? e1.getNumMinQuantity() : 0;
			Integer min2 = e2.getNumMinQuantity() != null ? e2.getNumMinQuantity() : 0;
			return min1.compareTo(min2);
		});

		return mapToGridEntries(entries);
	}

//	@Override
//	public DtoResult importPriceMatrixFromCsv(Long versionId, MultipartFile file, String importMode) {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	@Override
//	public DtoResult validatePriceAssignments(Long versionId, DtoBulkAssignRequest request) {
//		// TODO Auto-generated method stub
//		return null;
//	}

}