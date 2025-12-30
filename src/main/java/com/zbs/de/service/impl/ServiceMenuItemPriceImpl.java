package com.zbs.de.service.impl;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zbs.de.model.dto.*;
import com.zbs.de.mapper.MapperMenuItemPrice;
import com.zbs.de.model.MenuItem;
import com.zbs.de.model.MenuItemPrice;
import com.zbs.de.model.PriceVersion;
import com.zbs.de.repository.RepositoryMenuItem;
import com.zbs.de.repository.RepositoryMenuItemPrice;
import com.zbs.de.repository.RepositoryPriceVersion;
import com.zbs.de.service.ServiceMenuItemPrice;
import com.zbs.de.service.ServicePriceVersion;
import com.zbs.de.util.PriceConstants;
import com.zbs.de.util.UtilDateAndTime;

@Service
@Transactional
public class ServiceMenuItemPriceImpl implements ServiceMenuItemPrice {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceMenuItemPriceImpl.class);

	@Autowired
	private RepositoryMenuItemPrice repository;

	@Autowired
	private RepositoryMenuItem menuItemRepository;

	@Autowired
	private RepositoryPriceVersion priceVersionRepository;

	@Autowired
	private MapperMenuItemPrice mapper;

	@Autowired
	private ServicePriceVersion priceVersionService;

	@Override
	public DtoResult create(DtoMenuItemPrice dto) {
		try {
			// Validate required fields
			if (dto.getSerMenuItemId() == null) {
				return new DtoResult("Menu item ID is required.", null, null, null);
			}

			if (dto.getSerPriceVersionId() == null) {
				return new DtoResult("Price version ID is required.", null, null, null);
			}

			if (dto.getNumBasePrice() == null) {
				return new DtoResult("Base price is required.", null, null, null);
			}

			// Get menu item
			MenuItem menuItem = menuItemRepository.findById(dto.getSerMenuItemId())
					.orElseThrow(() -> new RuntimeException("Menu item not found with ID: " + dto.getSerMenuItemId()));

			// Get price version
			PriceVersion priceVersion = priceVersionRepository.findById(dto.getSerPriceVersionId()).orElseThrow(
					() -> new RuntimeException("Price version not found with ID: " + dto.getSerPriceVersionId()));

			// Check if price already exists for this combination
			boolean exists = repository.existsByMenuItemAndVersion(menuItem.getSerMenuItemId(),
					priceVersion.getSerPriceVersionId());
			if (exists) {
				return new DtoResult("Price already exists for this menu item and version.", null, null, null);
			}

			// Create entity
			MenuItemPrice entity = mapper.toEntity(dto);
			entity.setMenuItem(menuItem);
			entity.setPriceVersion(priceVersion);
			entity.setBlnIsDeleted(false);

			// Set defaults if not provided
			if (entity.getTxtCurrency() == null) {
				entity.setTxtCurrency(PriceConstants.DEFAULT_CURRENCY);
			}

			if (entity.getNumMinQuantity() == null) {
				entity.setNumMinQuantity(PriceConstants.DEFAULT_MIN_QUANTITY);
			}

			if (entity.getBlnIsActive() == null) {
				entity.setBlnIsActive(true);
			}

			if (entity.getBlnIsDefault() == null) {
				entity.setBlnIsDefault(false);
			}

			// If setting as default, unset current default for this menu item
			if (Boolean.TRUE.equals(entity.getBlnIsDefault())) {
				repository.findByMenuItem_SerMenuItemIdAndBlnIsDefaultTrueAndBlnIsActiveTrueAndBlnIsDeletedFalse(
						menuItem.getSerMenuItemId()).ifPresent(currentDefault -> {
							currentDefault.setBlnIsDefault(false);
							repository.save(currentDefault);
						});
			}

			repository.save(entity);

			return new DtoResult("Price created successfully.", null, mapper.toDto(entity), null);

		} catch (Exception e) {
			LOGGER.error("Error creating menu item price", e);
			return new DtoResult("Failed to create price: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult update(DtoMenuItemPrice dto) {
		try {
			MenuItemPrice entity = repository.findById(dto.getSerPriceId()).orElseThrow(
					() -> new RuntimeException("Menu item price not found with ID: " + dto.getSerPriceId()));

			if (Boolean.TRUE.equals(entity.getBlnIsDeleted())) {
				return new DtoResult("Price is deleted and cannot be updated.", null, null, null);
			}

			// Update fields
			mapper.updateEntityFromDto(dto, entity);

			// If setting as default, unset current default for this menu item
			if (Boolean.TRUE.equals(dto.getBlnIsDefault()) && !Boolean.TRUE.equals(entity.getBlnIsDefault())) {
				repository.findByMenuItem_SerMenuItemIdAndBlnIsDefaultTrueAndBlnIsActiveTrueAndBlnIsDeletedFalse(
						entity.getMenuItem().getSerMenuItemId()).ifPresent(currentDefault -> {
							if (!currentDefault.getSerPriceId().equals(entity.getSerPriceId())) {
								currentDefault.setBlnIsDefault(false);
								repository.save(currentDefault);
							}
						});
			}

			repository.save(entity);

			return new DtoResult("Price updated successfully.", null, mapper.toDto(entity), null);

		} catch (Exception e) {
			LOGGER.error("Error updating menu item price", e);
			return new DtoResult("Failed to update price: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult delete(Long id) {
		try {
			MenuItemPrice entity = repository.findById(id)
					.orElseThrow(() -> new RuntimeException("Menu item price not found with ID: " + id));

			if (Boolean.TRUE.equals(entity.getBlnIsDeleted())) {
				return new DtoResult("Price is already deleted.", null, null, null);
			}

			// Check if this is the default price
			if (Boolean.TRUE.equals(entity.getBlnIsDefault())) {
				return new DtoResult("Cannot delete default price. Set another price as default first.", null, null,
						null);
			}

			// Soft delete
			entity.setBlnIsDeleted(true);
			entity.setBlnIsActive(false);
			repository.save(entity);

			return new DtoResult("Price deleted successfully.", null, null, null);

		} catch (Exception e) {
			LOGGER.error("Error deleting menu item price", e);
			return new DtoResult("Failed to delete price: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult getById(Long id) {
		try {
			MenuItemPrice entity = repository.findById(id)
					.orElseThrow(() -> new RuntimeException("Menu item price not found with ID: " + id));

			if (Boolean.TRUE.equals(entity.getBlnIsDeleted())) {
				return new DtoResult("Price is deleted.", null, null, null);
			}

			return new DtoResult("Fetched successfully.", null, mapper.toDto(entity), null);

		} catch (Exception e) {
			LOGGER.error("Error fetching menu item price by ID", e);
			return new DtoResult("Failed to fetch price: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult getAll() {
		try {
			// Note: For performance, you might want to add pagination
			List<MenuItemPrice> entities = repository.findAll().stream()
					.filter(p -> !Boolean.TRUE.equals(p.getBlnIsDeleted())).collect(Collectors.toList());

			List<DtoMenuItemPrice> dtos = mapper.toDtoList(entities);

			return new DtoResult("Fetched successfully.", null, dtos, null);

		} catch (Exception e) {
			LOGGER.error("Error fetching all menu item prices", e);
			return new DtoResult("Failed to fetch prices: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult getAllActive() {
		try {
			// Note: For performance, you might want to add filtering by version or date
			List<MenuItemPrice> entities = repository.findAll().stream()
					.filter(p -> Boolean.TRUE.equals(p.getBlnIsActive()) && !Boolean.TRUE.equals(p.getBlnIsDeleted()))
					.collect(Collectors.toList());

			List<DtoMenuItemPrice> dtos = mapper.toDtoList(entities);

			return new DtoResult("Fetched successfully.", null, dtos, null);

		} catch (Exception e) {
			LOGGER.error("Error fetching all active menu item prices", e);
			return new DtoResult("Failed to fetch active prices: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult getByMenuItem(Long menuItemId) {
		try {
			List<MenuItemPrice> entities = repository
					.findByMenuItem_SerMenuItemIdOrderByBlnIsDefaultDescBlnIsActiveDescSerPriceIdDesc(menuItemId);

			List<DtoMenuItemPrice> dtos = entities.stream().filter(p -> !Boolean.TRUE.equals(p.getBlnIsDeleted()))
					.map(mapper::toDto).collect(Collectors.toList());

			return new DtoResult("Fetched successfully.", null, dtos, null);

		} catch (Exception e) {
			LOGGER.error("Error fetching prices by menu item", e);
			return new DtoResult("Failed to fetch prices: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult getByPriceVersion(Long priceVersionId) {
		try {
			List<MenuItemPrice> entities = repository
					.findByPriceVersion_SerPriceVersionIdAndBlnIsDeletedFalseOrderByMenuItem_TxtCodeAsc(priceVersionId);

			List<DtoMenuItemPrice> dtos = mapper.toDtoList(entities);

			return new DtoResult("Fetched successfully.", null, dtos, null);

		} catch (Exception e) {
			LOGGER.error("Error fetching prices by price version", e);
			return new DtoResult("Failed to fetch prices: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult getByMenuItemAndVersion(Long menuItemId, Long priceVersionId) {
		try {
			Optional<MenuItemPrice> entity = repository
					.findByMenuItem_SerMenuItemIdAndPriceVersion_SerPriceVersionIdAndBlnIsDeletedFalse(menuItemId,
							priceVersionId);

			if (entity.isPresent()) {
				return new DtoResult("Fetched successfully.", null, mapper.toDto(entity.get()), null);
			} else {
				return new DtoResult("No price found for this menu item and version.", null, null, null);
			}

		} catch (Exception e) {
			LOGGER.error("Error fetching price by menu item and version", e);
			return new DtoResult("Failed to fetch price: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult getDefaultByMenuItem(Long menuItemId) {
		try {
			Optional<MenuItemPrice> entity = repository
					.findByMenuItem_SerMenuItemIdAndBlnIsDefaultTrueAndBlnIsActiveTrueAndBlnIsDeletedFalse(menuItemId);

			if (entity.isPresent()) {
				return new DtoResult("Fetched successfully.", null, mapper.toDto(entity.get()), null);
			} else {
				// Fallback to any active price
				List<MenuItemPrice> activePrices = repository
						.findByMenuItem_SerMenuItemIdAndBlnIsActiveTrueAndBlnIsDeletedFalseOrderByBlnIsDefaultDescSerPriceIdDesc(
								menuItemId);
				if (!activePrices.isEmpty()) {
					return new DtoResult("Fetched successfully (no default found, using first active).", null,
							mapper.toDto(activePrices.get(0)), null);
				} else {
					return new DtoResult("No price found for this menu item.", null, null, null);
				}
			}

		} catch (Exception e) {
			LOGGER.error("Error fetching default price by menu item", e);
			return new DtoResult("Failed to fetch price: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult getApplicablePrice(Long menuItemId, Integer guestCount, Date date, Long priceVersionId) {
		try {
			List<MenuItemPrice> applicablePrices = repository.findApplicablePrice(menuItemId, guestCount,
					date != null ? date : new Date(), priceVersionId);

			if (applicablePrices.isEmpty()) {
				return new DtoResult("No applicable price found for the given context.", null, null, null);
			}

			// Return the first applicable price (highest priority)
			MenuItemPrice price = applicablePrices.get(0);
			DtoMenuItemPrice dto = mapper.toDto(price);

			// Calculate price for the context
			BigDecimal calculated = price.calculatePrice(1, guestCount, null); // Default quantity 1
			dto.setCalculatedPrice(calculated);

			return new DtoResult("Fetched successfully.", null, dto, null);

		} catch (Exception e) {
			LOGGER.error("Error fetching applicable price", e);
			return new DtoResult("Failed to fetch applicable price: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult bulkSetPrices(DtoBulkPriceRequest request) {
		try {
			if (request.getMenuItemIds() == null || request.getMenuItemIds().isEmpty()) {
				return new DtoResult("Menu item IDs list is empty.", null, null, null);
			}

			if (request.getSerPriceVersionId() == null) {
				return new DtoResult("Price version ID is required.", null, null, null);
			}

			if (request.getNumBasePrice() == null) {
				return new DtoResult("Base price is required.", null, null, null);
			}

			// Get price version
			PriceVersion priceVersion = priceVersionRepository.findById(request.getSerPriceVersionId()).orElseThrow(
					() -> new RuntimeException("Price version not found with ID: " + request.getSerPriceVersionId()));

			int successCount = 0;
			int skipCount = 0;
			int errorCount = 0;
			List<String> errors = new ArrayList<>();

			for (Long menuItemId : request.getMenuItemIds()) {
				try {
					// Get menu item
					MenuItem menuItem = menuItemRepository.findById(menuItemId)
							.orElseThrow(() -> new RuntimeException("Menu item not found with ID: " + menuItemId));

					// Check if price already exists
					boolean exists = repository.existsByMenuItemAndVersion(menuItemId,
							priceVersion.getSerPriceVersionId());

					if (exists) {
						if ("SKIP".equals(request.getConflictResolution())) {
							skipCount++;
							continue;
						}

						if ("OVERWRITE".equals(request.getConflictResolution())) {
							// Delete existing and create new
							Optional<MenuItemPrice> existing = repository
									.findByMenuItem_SerMenuItemIdAndPriceVersion_SerPriceVersionIdAndBlnIsDeletedFalse(
											menuItemId, priceVersion.getSerPriceVersionId());

							if (existing.isPresent()) {
								MenuItemPrice existingPrice = existing.get();
								existingPrice.setBlnIsDeleted(true);
								existingPrice.setBlnIsActive(false);
								repository.save(existingPrice);
							}
						}
						// For "UPDATE", we'll update the existing record below
					}

					MenuItemPrice price;

					if (exists && "UPDATE".equals(request.getConflictResolution())) {
						// Update existing price
						price = repository
								.findByMenuItem_SerMenuItemIdAndPriceVersion_SerPriceVersionIdAndBlnIsDeletedFalse(
										menuItemId, priceVersion.getSerPriceVersionId())
								.orElseThrow(() -> new RuntimeException("Existing price not found for update"));

						price.setNumBasePrice(request.getNumBasePrice());
						price.setTxtCurrency(request.getTxtCurrency());
						price.setTxtPriceModel(request.getTxtPriceModel());
						price.setTxtUnitType(request.getTxtUnitType());
						price.setNumMinQuantity(request.getNumMinQuantity());
						price.setNumMaxQuantity(request.getNumMaxQuantity());
						price.setNumMinGuests(request.getNumMinGuests());
						price.setNumMaxGuests(request.getNumMaxGuests());
						price.setBlnIsActive(request.getBlnIsActive());
						price.setBlnIsDefault(request.getBlnIsDefault());

					} else {
						// Create new price
						price = new MenuItemPrice();
						price.setMenuItem(menuItem);
						price.setPriceVersion(priceVersion);
						price.setNumBasePrice(request.getNumBasePrice());
						price.setTxtCurrency(request.getTxtCurrency() != null ? request.getTxtCurrency()
								: PriceConstants.DEFAULT_CURRENCY);
						price.setTxtPriceModel(request.getTxtPriceModel());
						price.setTxtUnitType(request.getTxtUnitType());
						price.setNumMinQuantity(request.getNumMinQuantity() != null ? request.getNumMinQuantity()
								: PriceConstants.DEFAULT_MIN_QUANTITY);
						price.setNumMaxQuantity(request.getNumMaxQuantity());
						price.setNumMinGuests(request.getNumMinGuests());
						price.setNumMaxGuests(request.getNumMaxGuests());
						price.setBlnIsActive(request.getBlnIsActive() != null ? request.getBlnIsActive() : true);
						price.setBlnIsDefault(request.getBlnIsDefault() != null ? request.getBlnIsDefault() : false);
						price.setBlnIsDeleted(false);
					}

					repository.save(price);
					successCount++;

				} catch (Exception e) {
					errorCount++;
					errors.add("Menu Item ID " + menuItemId + ": " + e.getMessage());
				}
			}

			String message = String.format("Bulk price update completed. Success: %d, Skipped: %d, Failed: %d",
					successCount, skipCount, errorCount);

			Map<String, Object> result = new HashMap<>();
			result.put("successCount", successCount);
			result.put("skipCount", skipCount);
			result.put("errorCount", errorCount);
			result.put("errors", errors);

			return new DtoResult(message, errors, result, null);

		} catch (Exception e) {
			LOGGER.error("Error in bulk price update", e);
			return new DtoResult("Failed to perform bulk price update: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult copyPrices(Long sourceVersionId, Long targetVersionId) {
		try {
			if (sourceVersionId.equals(targetVersionId)) {
				return new DtoResult("Source and target versions cannot be the same.", null, null, null);
			}

			// Get source prices
			List<MenuItemPrice> sourcePrices = repository
					.findByPriceVersion_SerPriceVersionIdAndBlnIsDeletedFalseOrderByMenuItem_TxtCodeAsc(
							sourceVersionId);

			if (sourcePrices.isEmpty()) {
				return new DtoResult("No prices found in source version.", null, null, null);
			}

			// Get target version
			PriceVersion targetVersion = priceVersionRepository.findById(targetVersionId).orElseThrow(
					() -> new RuntimeException("Target price version not found with ID: " + targetVersionId));

			int copiedCount = 0;
			int skippedCount = 0;

			for (MenuItemPrice sourcePrice : sourcePrices) {
				// Check if price already exists in target
				boolean exists = repository.existsByMenuItemAndVersion(sourcePrice.getMenuItem().getSerMenuItemId(),
						targetVersion.getSerPriceVersionId());

				if (exists) {
					skippedCount++;
					continue;
				}

				// Create copy
				MenuItemPrice copy = new MenuItemPrice();
				copy.setMenuItem(sourcePrice.getMenuItem());
				copy.setPriceVersion(targetVersion);
				copy.setNumBasePrice(sourcePrice.getNumBasePrice());
				copy.setTxtCurrency(sourcePrice.getTxtCurrency());
				copy.setTxtPriceModel(sourcePrice.getTxtPriceModel());
				copy.setTxtUnitType(sourcePrice.getTxtUnitType());
				copy.setNumMinQuantity(sourcePrice.getNumMinQuantity());
				copy.setNumMaxQuantity(sourcePrice.getNumMaxQuantity());
				copy.setNumMinGuests(sourcePrice.getNumMinGuests());
				copy.setNumMaxGuests(sourcePrice.getNumMaxGuests());
				copy.setNumMinHours(sourcePrice.getNumMinHours());
				copy.setNumMaxHours(sourcePrice.getNumMaxHours());
				copy.setDteValidFrom(sourcePrice.getDteValidFrom());
				copy.setDteValidTo(sourcePrice.getDteValidTo());
				copy.setPriceConfig(sourcePrice.getPriceConfig());
				copy.setBlnIsActive(true); // Always active when copying
				copy.setBlnIsDefault(false); // Never default when copying
				copy.setBlnIsDeleted(false);

				repository.save(copy);
				copiedCount++;
			}

			String message = String.format("Copied %d prices, skipped %d (already exist).", copiedCount, skippedCount);

			Map<String, Object> result = new HashMap<>();
			result.put("copiedCount", copiedCount);
			result.put("skippedCount", skippedCount);
			result.put("totalSource", sourcePrices.size());

			return new DtoResult(message, null, result, null);

		} catch (Exception e) {
			LOGGER.error("Error copying prices", e);
			return new DtoResult("Failed to copy prices: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult updatePricesStatus(List<Long> priceIds, Boolean isActive) {
		try {
			if (priceIds == null || priceIds.isEmpty()) {
				return new DtoResult("Price IDs list is empty.", null, null, null);
			}

			int updatedCount = 0;

			for (Long priceId : priceIds) {
				try {
					MenuItemPrice price = repository.findById(priceId)
							.orElseThrow(() -> new RuntimeException("Price not found with ID: " + priceId));

					if (!Boolean.TRUE.equals(price.getBlnIsDeleted())) {
						// Check if trying to deactivate default price
						if (Boolean.FALSE.equals(isActive) && Boolean.TRUE.equals(price.getBlnIsDefault())) {
							throw new RuntimeException("Cannot deactivate default price");
						}

						price.setBlnIsActive(isActive);
						repository.save(price);
						updatedCount++;
					}

				} catch (Exception e) {
					LOGGER.warn("Error updating price ID {}: {}", priceId, e.getMessage());
				}
			}

			String message = String.format("Updated %d of %d prices.", updatedCount, priceIds.size());

			return new DtoResult(message, null, updatedCount, null);

		} catch (Exception e) {
			LOGGER.error("Error updating prices status", e);
			return new DtoResult("Failed to update prices status: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoPriceCalculationResult calculatePrice(DtoPriceCalculationRequest request) {
		DtoPriceCalculationResult result = new DtoPriceCalculationResult();
		result.setCurrency("GBP");
		result.setSubtotal(BigDecimal.ZERO);
		result.setTotal(BigDecimal.ZERO);
		result.setItems(new ArrayList<>());
		result.setWarnings(new ArrayList<>());
		result.setErrors(new ArrayList<>());

		try {
			if (request.getMenuItemIds() == null || request.getMenuItemIds().isEmpty()) {
				result.getErrors().add("No menu items specified for calculation");
				return result;
			}

			// Determine which price version to use
			Long priceVersionId = request.getPriceVersionId();
			if (priceVersionId == null) {
				// Use active version for date
				PriceVersion activeVersion = priceVersionService.getActivePriceVersionForDate(request.getEventDate());
				priceVersionId = activeVersion.getSerPriceVersionId();
			}

			BigDecimal subtotal = BigDecimal.ZERO;
			List<DtoCalculatedItem> calculatedItems = new ArrayList<>();

			for (Long menuItemId : request.getMenuItemIds()) {
				try {
					// Get applicable price
					List<MenuItemPrice> applicablePrices = repository.findApplicablePrice(menuItemId,
							request.getGuestCount(), request.getEventDate(), priceVersionId);

					if (applicablePrices.isEmpty()) {
						result.getWarnings().add("No applicable price found for menu item ID: " + menuItemId);
						continue;
					}

					MenuItemPrice price = applicablePrices.get(0);
					MenuItem menuItem = price.getMenuItem();

					// Calculate price
					BigDecimal unitPrice = price.getNumBasePrice();
					BigDecimal itemTotal = price.calculatePrice(request.getQuantity(), request.getGuestCount(),
							request.getHours());

					// Create calculated item
					DtoCalculatedItem calculatedItem = new DtoCalculatedItem();
					calculatedItem.setMenuItemId(menuItemId);
					calculatedItem.setMenuItemCode(menuItem.getTxtCode());
					calculatedItem.setMenuItemName(menuItem.getTxtName());
					calculatedItem.setUnitPrice(unitPrice);
					calculatedItem.setTotalPrice(itemTotal);
					calculatedItem.setUnitType(price.getTxtUnitType());
					calculatedItem.setQuantity(request.getQuantity() != null ? request.getQuantity() : 1);

					calculatedItems.add(calculatedItem);
					subtotal = subtotal.add(itemTotal);

				} catch (Exception e) {
					result.getWarnings()
							.add("Error calculating price for menu item ID " + menuItemId + ": " + e.getMessage());
				}
			}

			// Set results
			result.setItems(calculatedItems);
			result.setSubtotal(subtotal);
			result.setTotal(subtotal); // No taxes or discounts for now

			if (calculatedItems.isEmpty()) {
				result.getErrors().add("No prices could be calculated for the specified items");
			}

		} catch (Exception e) {
			LOGGER.error("Error calculating price", e);
			result.getErrors().add("Calculation error: " + e.getMessage());
		}

		return result;
	}

	@Override
	public DtoResult calculatePriceForMenuItem(Long menuItemId, Integer guestCount, Integer quantity, Date date) {
		try {
			// Get applicable price
			List<MenuItemPrice> applicablePrices = repository.findApplicablePrice(menuItemId, guestCount,
					date != null ? date : new Date(), null); // Let the system determine version

			if (applicablePrices.isEmpty()) {
				return new DtoResult("No applicable price found.", null, null, null);
			}

			MenuItemPrice price = applicablePrices.get(0);

			// Calculate
			BigDecimal calculated = price.calculatePrice(quantity, guestCount, null);

			Map<String, Object> result = new HashMap<>();
			result.put("menuItemId", menuItemId);
			result.put("basePrice", price.getNumBasePrice());
			result.put("unitType", price.getTxtUnitType());
			result.put("calculatedPrice", calculated);
			result.put("currency", price.getTxtCurrency());
			result.put("guestCount", guestCount);
			result.put("quantity", quantity);

			return new DtoResult("Price calculated successfully.", null, result, null);

		} catch (Exception e) {
			LOGGER.error("Error calculating price for menu item", e);
			return new DtoResult("Failed to calculate price: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult validatePrice(DtoMenuItemPrice dto) {
		try {
			List<String> errors = new ArrayList<>();
			List<String> warnings = new ArrayList<>();

			// Basic validation
			if (dto.getSerMenuItemId() == null) {
				errors.add("Menu item ID is required");
			}

			if (dto.getSerPriceVersionId() == null) {
				errors.add("Price version ID is required");
			}

			if (dto.getNumBasePrice() == null) {
				errors.add("Base price is required");
			} else if (dto.getNumBasePrice().compareTo(BigDecimal.ZERO) < 0) {
				errors.add("Base price cannot be negative");
			}

			if (dto.getTxtUnitType() != null) {
				if (!isValidUnitType(dto.getTxtUnitType())) {
					errors.add("Invalid unit type: " + dto.getTxtUnitType());
				}
			}

			// Quantity validation
			if (dto.getNumMinQuantity() != null && dto.getNumMaxQuantity() != null) {
				if (dto.getNumMinQuantity() > dto.getNumMaxQuantity()) {
					errors.add("Minimum quantity cannot be greater than maximum quantity");
				}
			}

			// Guest count validation
			if (dto.getNumMinGuests() != null && dto.getNumMaxGuests() != null) {
				if (dto.getNumMinGuests() > dto.getNumMaxGuests()) {
					errors.add("Minimum guests cannot be greater than maximum guests");
				}
			}

			// Date validation
			if (dto.getDteValidFrom() != null && dto.getDteValidTo() != null) {
				if (UtilDateAndTime.ddMMyyyyDashedStringToDate(dto.getDteValidFrom())
						.after(UtilDateAndTime.ddMMyyyyDashedStringToDate(dto.getDteValidTo()))) {
					errors.add("Valid from date cannot be after valid to date");
				}
			}

			Map<String, Object> result = new HashMap<>();
			result.put("isValid", errors.isEmpty());
			result.put("errors", errors);
			result.put("warnings", warnings);

			String message = errors.isEmpty() ? "Price validation passed" : "Price validation failed";

			return new DtoResult(message, errors, result, null);

		} catch (Exception e) {
			LOGGER.error("Error validating price", e);
			return new DtoResult("Validation error: " + e.getMessage(), null, null, null);
		}
	}

	private boolean isValidUnitType(String unitType) {
		return PriceConstants.UNIT_PER_ITEM.equals(unitType) || PriceConstants.UNIT_PER_GUEST.equals(unitType)
				|| PriceConstants.UNIT_PER_HOUR.equals(unitType) || PriceConstants.UNIT_FLAT.equals(unitType);
	}
}