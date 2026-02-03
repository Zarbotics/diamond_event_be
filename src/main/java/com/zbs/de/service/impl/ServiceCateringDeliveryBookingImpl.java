package com.zbs.de.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.zbs.de.mapper.MapperCateringDeliveryBooking;
import com.zbs.de.model.CateringDeliveryBooking;
import com.zbs.de.model.CateringDeliveryItemDetail;
import com.zbs.de.model.EventBudget;
import com.zbs.de.model.EventMenuCategorySelection;
import com.zbs.de.model.EventMenuFoodSelection;
import com.zbs.de.model.EventMenuSubCategorySelection;
import com.zbs.de.model.MenuItem;
import com.zbs.de.model.dto.DtoCateringDeliveryBooking;
import com.zbs.de.model.dto.DtoCateringDeliveryBookingSearch;
import com.zbs.de.model.dto.DtoCateringDeliveryItemDetail;
import com.zbs.de.model.dto.DtoMenuComponentRequest;
import com.zbs.de.model.dto.DtoMenuFoodMaster;
import com.zbs.de.model.dto.DtoMenuItem;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.model.dto.menu.DtoCustomerMenuCategory;
import com.zbs.de.model.dto.menu.DtoCustomerMenuSubCategory;
import com.zbs.de.repository.RepositoryCateringDeliveryBooking;
import com.zbs.de.repository.RepositoryCustomerMaster;
import com.zbs.de.repository.RepositoryEventType;
import com.zbs.de.service.ServiceCateringDeliveryBooking;
import com.zbs.de.service.ServiceEventBudget;
import com.zbs.de.service.ServiceMenuItem;
import com.zbs.de.spec.SpecificationsCateringDeliveryBooking;
import com.zbs.de.util.UtilDateAndTime;
import com.zbs.de.util.UtilRandomKey;

import jakarta.transaction.Transactional;

@Service("serviceCateringDeliveryBookingImpl")
public class ServiceCateringDeliveryBookingImpl implements ServiceCateringDeliveryBooking {

	@Autowired
	private RepositoryCateringDeliveryBooking repositoryCateringDeliveryBooking;

	@Autowired
	private RepositoryCustomerMaster repositoryCustomerMaster;

	@Autowired
	private RepositoryEventType repositoryEventType;

	@Autowired
	private ServiceEventBudget serviceEventBudget;

	@Autowired
	private ServiceMenuItem serviceMenuItem;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCateringDeliveryBookingImpl.class);

	@Override
	public DtoResult saveOrUpdate(DtoCateringDeliveryBooking dto) {
		DtoResult result = new DtoResult();

		try {
			// Validate required fields
			if (dto.getSerCustId() == null) {
				result.setTxtMessage("Customer is required.");
				return result;
			}
			if (dto.getSerEventTypeId() == null) {
				result.setTxtMessage("Event Type is required.");
				return result;
			}

			// Fetch Customer
			var customer = repositoryCustomerMaster.findById(dto.getSerCustId()).orElse(null);
			if (customer == null) {
				result.setTxtMessage("Customer not found with ID: " + dto.getSerCustId());
				return result;
			}

			// Fetch Event Type
			var eventType = repositoryEventType.findById(dto.getSerEventTypeId()).orElse(null);
//			if (eventType == null) {
//				result.setTxtMessage("Event Type not found with ID: " + dto.getSerEventTypeId());
//				return result;
//			}

			List<MenuItem> menuItems = serviceMenuItem.getAllMenuItems();
			if (UtilRandomKey.isNull(menuItems)) {
				result.setTxtMessage("No Food Item Is Present In DB");
				return result;
			}

			CateringDeliveryBooking entity;

			if (dto.getSerDeliveryBookingId() != null) {
				entity = repositoryCateringDeliveryBooking.findById(dto.getSerDeliveryBookingId()).orElseThrow(
						() -> new RuntimeException("Booking not found with ID: " + dto.getSerDeliveryBookingId()));

				entity.setUpdatedBy(ServiceCurrentUser.getCurrentUserId());
			} else {
				entity = new CateringDeliveryBooking();
				entity.setTxtDeliveryBookingCode(generateAutoCode());
				entity.setCreatedBy(ServiceCurrentUser.getCurrentUserId());
			}

			// Set master fields
			entity.setCustomerMaster(customer);
			if (eventType != null) {
				entity.setEventType(eventType);
			}

			// Map other fields using Mapper
			CateringDeliveryBooking mapped = MapperCateringDeliveryBooking.toEntity(dto);
			entity.setTxtBookingStatus(mapped.getTxtBookingStatus());
			entity.setBlnBookingStatus(mapped.getBlnBookingStatus());
			entity.setBlnRequestMeeting(mapped.getBlnRequestMeeting());
			entity.setDteDeliveryDate(mapped.getDteDeliveryDate());
			entity.setTxtDeliveryLocation(mapped.getTxtDeliveryLocation());
			entity.setTxtDeliveryTime(mapped.getTxtDeliveryTime());
			entity.setTxtRemarks(mapped.getTxtRemarks());
			entity.setTxtSpecialInstructions(mapped.getTxtSpecialInstructions());
			entity.setNumNumberOfGuests(mapped.getNumNumberOfGuests());
			entity.setNumNumberOfTables(mapped.getNumNumberOfGuests());
			
			// *** Validate Duplicate Date Check ***
			Boolean isalreadyBooked = false;

			if (mapped.getDteDeliveryDate() != null && entity.getDteDeliveryDate() != null
					&& mapped.getDteDeliveryDate().compareTo(entity.getDteDeliveryDate()) != 0) {
				isalreadyBooked = repositoryCateringDeliveryBooking
						.existsByDteDeliveryDateAndBlnIsDeletedFalse(mapped.getDteDeliveryDate());

			} else if (mapped.getDteDeliveryDate() != null && entity.getDteDeliveryDate() == null) {
				isalreadyBooked = repositoryCateringDeliveryBooking
						.existsByDteDeliveryDateAndBlnIsDeletedFalse(mapped.getDteDeliveryDate());

			}
			if (isalreadyBooked) {
				result.setTxtMessage("An event is already booked against this date." + mapped.getDteDeliveryDate());
				return result;
			}

//			// Map item details
//			List<CateringDeliveryItemDetail> detailList = new ArrayList<>();
//			if (dto.getCateringDeliveryItemDetails() != null) {
//				for (DtoCateringDeliveryItemDetail detailDto : dto.getCateringDeliveryItemDetails()) {
////					if (detailDto.getSerMenuFoodId() == null) {
////						throw new IllegalArgumentException("Menu Food ID is required for each delivery item.");
////					}
//
//					if (detailDto.getSerMenuItemId() == null) {
//						throw new IllegalArgumentException("Menu Food ID is required for each delivery item.");
//					}
//
////					MenuFoodMaster food = repositoryMenuFoodMaster.findById(detailDto.getSerMenuFoodId())
////							.orElseThrow(() -> new EntityNotFoundException(
////									"Menu Food not found with ID: " + detailDto.getSerMenuFoodId()));
//
//					MenuItem menuItem = serviceMenuItem.getMenuItemById(detailDto.getSerMenuItemId());
//					if (menuItem == null) {
//						result.setTxtMessage("Menu Food not found with ID: " + detailDto.getSerMenuItemId());
//						return result;
//					}
//
//					CateringDeliveryItemDetail detail = new CateringDeliveryItemDetail();
//					detail.setCateringDeliveryBooking(entity);
////					detail.setMenueFoodMaster(food);
//					detail.setMenuItem(menuItem);
//					detail.setNumPrice(detailDto.getNumPrice());
//					detailList.add(detail);
//				}
//			}
//
//			entity.setCateringDeliveryItemDetails(detailList);

			// *********************************************************************************************
			// ************************ Food Menu Categories and Sub Categories
			// ****************************
			// *********************************************************************************************
			if (dto.getMenuCategoriesSelection() != null && !dto.getMenuCategoriesSelection().isEmpty()) {

				// 🔥 FIX: PROPERLY CLEAR OLD MENU SELECTIONS WITH SESSION MANAGEMENT
				if (entity.getMenuCategorySelections() != null && !entity.getMenuCategorySelections().isEmpty()) {
					// Before clearing, we need to break the bidirectional relationships
					// This helps orphanRemoval work correctly
					List<EventMenuCategorySelection> categoriesToClear = new ArrayList<>(
							entity.getMenuCategorySelections());

					for (EventMenuCategorySelection category : categoriesToClear) {
						// Break relationship with EventMaster
						category.setEventMaster(null);

						if (category.getSubCategories() != null) {
							List<EventMenuSubCategorySelection> subCategoriesToClear = new ArrayList<>(
									category.getSubCategories());

							for (EventMenuSubCategorySelection subCategory : subCategoriesToClear) {
								// Break relationship with parent category
								subCategory.setEventCategory(null);

								if (subCategory.getItems() != null) {
									List<EventMenuFoodSelection> itemsToClear = new ArrayList<>(subCategory.getItems());

									for (EventMenuFoodSelection item : itemsToClear) {
										// Break relationships with EventMaster and SubCategory
										item.setEventMaster(null);
										item.setEventSubCategory(null);
									}

									// Clear the items collection
									subCategory.getItems().clear();
								}
							}

							// Clear the subcategories collection
							category.getSubCategories().clear();
						}
					}

					// Now clear the main collection - orphanRemoval will delete from DB
					entity.getMenuCategorySelections().clear();

					// 🔥 CRITICAL: Save immediately to persist deletions and clear session state
					// This flushes the deletions to DB and clears deleted entities from session
					entity = repositoryCateringDeliveryBooking.saveAndFlush(entity);

				} else if (entity.getMenuCategorySelections() == null) {
					entity.setMenuCategorySelections(new ArrayList<>());
				}

				// CREATE NEW MENU SELECTIONS
				for (DtoCustomerMenuCategory catDto : dto.getMenuCategoriesSelection()) {

					MenuItem category = menuItems.stream()
							.filter(item -> item.getSerMenuItemId() != null
									&& item.getSerMenuItemId().intValue() == catDto.getCategoryId().intValue())
							.findFirst().orElse(null);

					EventMenuCategorySelection catEntity = new EventMenuCategorySelection();
					catEntity.setCateringDeliveryBooking(entity);
					catEntity.setCategory(category);
					catEntity.setNumTotalPrice(catDto.getNumPrice());
					catEntity.setNumFinalPrice(catDto.getNumFinalPrice());

					// Initialize collections
					if (catEntity.getSubCategories() == null) {
						catEntity.setSubCategories(new ArrayList<>());
					}

					for (DtoCustomerMenuSubCategory subDto : catDto.getSubCategories()) {

						MenuItem subCategory = menuItems.stream()
								.filter(item -> item.getSerMenuItemId() != null
										&& item.getSerMenuItemId().intValue() == subDto.getSubCategoryId().intValue())
								.findFirst().orElse(null);

						EventMenuSubCategorySelection subEntity = new EventMenuSubCategorySelection();
						subEntity.setEventCategory(catEntity);
						subEntity.setSubCategory(subCategory);
						subEntity.setNumTotalPrice(subDto.getNumPrice());
						subEntity.setNumFinalPrice(subDto.getNumFinalPrice());

						// Initialize items collection
						if (subEntity.getItems() == null) {
							subEntity.setItems(new ArrayList<>());
						}

						// Simple Items
						for (DtoMenuItem itemDto : subDto.getItems()) {

							MenuItem menuItem = menuItems.stream().filter(item -> item.getSerMenuItemId() != null
									&& item.getSerMenuItemId().intValue() == itemDto.getSerMenuItemId().intValue())
									.findFirst().orElse(null);

							EventMenuFoodSelection itemEntity = new EventMenuFoodSelection();
//			                itemEntity.setEventMaster(entity);
							itemEntity.setEventSubCategory(subEntity);
							itemEntity.setMenuItem(menuItem);
							itemEntity.setNumPrice(itemDto.getNumPrice());
							itemEntity.setNumCalculatedPrice(itemDto.getNumCalculatedPrice());
							itemEntity.setNumFinalPrice(itemDto.getNumFinalPrice());

							subEntity.getItems().add(itemEntity);
						}

						// Composite Items If Exists
						for (DtoMenuComponentRequest itemDto : subDto.getCompositeItems()) {

							MenuItem menuItem = menuItems.stream().filter(item -> item.getSerMenuItemId() != null
									&& item.getSerMenuItemId().intValue() == itemDto.getParentMenuItemId().intValue())
									.findFirst().orElse(null);

							EventMenuFoodSelection itemEntity = new EventMenuFoodSelection();
//			                itemEntity.setEventMaster(entity);
							itemEntity.setEventSubCategory(subEntity);
							itemEntity.setMenuItem(menuItem);
							itemEntity.setNumPrice(itemDto.getNumPrice());
							itemEntity.setNumCalculatedPrice(itemDto.getNumCalculatedPrice());
							itemEntity.setNumFinalPrice(itemDto.getNumFinalPrice());

							subEntity.getItems().add(itemEntity);
						}

						catEntity.getSubCategories().add(subEntity);
					}

					entity.getMenuCategorySelections().add(catEntity);
				}

			}

			// Handle case when no menu selections in DTO but editing existing event
			else if (entity.getSerDeliveryBookingId() != null && entity.getMenuCategorySelections() != null
					&& !entity.getMenuCategorySelections().isEmpty()) {

				// User removed all menu selections - clear them properly
				List<EventMenuCategorySelection> categoriesToClear = new ArrayList<>(
						entity.getMenuCategorySelections());

				for (EventMenuCategorySelection category : categoriesToClear) {
					category.setEventMaster(null);
					if (category.getSubCategories() != null) {
						for (EventMenuSubCategorySelection subCategory : category.getSubCategories()) {
							subCategory.setEventCategory(null);
							if (subCategory.getItems() != null) {
								for (EventMenuFoodSelection item : subCategory.getItems()) {
									item.setEventMaster(null);
									item.setEventSubCategory(null);
								}
								subCategory.getItems().clear();
							}
						}
						category.getSubCategories().clear();
					}
				}

				entity.getMenuCategorySelections().clear();
				entity = repositoryCateringDeliveryBooking.saveAndFlush(entity);
			}
			// *********************************************************************************************
			// *********************************************************************************************
			// *********************************************************************************************

			// Setting Event Quoted Price
			// **************************

			EventBudget eventBudget = serviceEventBudget
					.getEventBudgetByCateringDelevieryBookingId(entity.getSerDeliveryBookingId());
			if (eventBudget != null) {
				if (eventBudget.getNumPaidAmount() != null
						&& eventBudget.getNumPaidAmount().compareTo(BigDecimal.ZERO) == 1) {
					eventBudget.setTxtStatus("Confirmed");

				} else if (dto.getDtoEventQuoteAndStatus() != null
						&& dto.getDtoEventQuoteAndStatus().getNumQuotedPrice() != null
						&& dto.getDtoEventQuoteAndStatus().getNumQuotedPrice().compareTo(BigDecimal.ZERO) == 1) {
					eventBudget.setTxtStatus("Quoted");
					eventBudget.setNumQuotedPrice(dto.getDtoEventQuoteAndStatus().getNumQuotedPrice());
					eventBudget.setNumPaidAmount(BigDecimal.ZERO);

				} else {
					eventBudget.setTxtStatus("Enquiry");
					eventBudget.setNumQuotedPrice(BigDecimal.ZERO);
					eventBudget.setNumPaidAmount(BigDecimal.ZERO);

				}

			} else {
				eventBudget = new EventBudget();
				if (dto.getDtoEventQuoteAndStatus().getNumQuotedPrice() != null
						&& dto.getDtoEventQuoteAndStatus().getNumQuotedPrice().compareTo(BigDecimal.ZERO) == 1) {
					eventBudget.setTxtStatus("Quoted");
					eventBudget.setNumQuotedPrice(dto.getDtoEventQuoteAndStatus().getNumQuotedPrice());
					eventBudget.setNumPaidAmount(BigDecimal.ZERO);

				} else {
					eventBudget.setTxtStatus("Enquiry");
					eventBudget.setNumQuotedPrice(BigDecimal.ZERO);
					eventBudget.setNumPaidAmount(BigDecimal.ZERO);

				}

			}

			repositoryCateringDeliveryBooking.save(entity);

			if (eventBudget != null) {
				eventBudget.setCateringDeliveryBooking(entity);
				serviceEventBudget.save(eventBudget);
			}

			result.setTxtMessage("Success");
			result.setResult(MapperCateringDeliveryBooking.toDto(entity));

		} catch (RuntimeException e) {
			result.setTxtMessage("Error: " + e.getMessage());
		} catch (Exception e) {
			result.setTxtMessage("Unexpected error while saving: " + e.getMessage());
		}

		return result;
	}

	@Override
	public DtoResult saveOrUpdateCateringAdminPortal(DtoCateringDeliveryBooking dto) {
		DtoResult result = new DtoResult();

		try {
			// Validate required fields
			if (dto.getSerCustId() == null) {
				result.setTxtMessage("Customer is required.");
				return result;
			}
			if (dto.getSerEventTypeId() == null) {
				result.setTxtMessage("Event Type is required.");
				return result;
			}

			// Fetch Customer
			var customer = repositoryCustomerMaster.findById(dto.getSerCustId()).orElse(null);
			if (customer == null) {
				result.setTxtMessage("Customer not found with ID: " + dto.getSerCustId());
				return result;
			}

			// Fetch Event Type
			var eventType = repositoryEventType.findById(dto.getSerEventTypeId()).orElse(null);
//			if (eventType == null) {
//				result.setTxtMessage("Event Type not found with ID: " + dto.getSerEventTypeId());
//				return result;
//			}

			CateringDeliveryBooking entity;

			if (dto.getSerDeliveryBookingId() != null) {
				entity = repositoryCateringDeliveryBooking.findById(dto.getSerDeliveryBookingId()).orElseThrow(
						() -> new RuntimeException("Booking not found with ID: " + dto.getSerDeliveryBookingId()));

				entity.setUpdatedBy(ServiceCurrentUser.getCurrentUserId());
			} else {
				entity = new CateringDeliveryBooking();
				entity.setTxtDeliveryBookingCode(generateAutoCode());
				entity.setCreatedBy(ServiceCurrentUser.getCurrentUserId());
			}

			// Set master fields
			entity.setCustomerMaster(customer);
			if (eventType != null) {
				entity.setEventType(eventType);
			}

//			List<MenuFoodMaster> dtoMenuFoodMasterLst = serviceMenuFoodMaster.getAllDataEntity();
//			if (UtilRandomKey.isNull(dtoMenuFoodMasterLst)) {
//				result.setTxtMessage("No Food Item Is Present In DB");
//				return result;
//			}

			List<MenuItem> menuItems = serviceMenuItem.getAllMenuItems();
			if (UtilRandomKey.isNull(menuItems)) {
				result.setTxtMessage("No Food Item Is Present In DB");
				return result;
			}

			// Map other fields using Mapper
			CateringDeliveryBooking mapped = MapperCateringDeliveryBooking.toEntity(dto);
			entity.setTxtBookingStatus(mapped.getTxtBookingStatus());
			entity.setBlnBookingStatus(mapped.getBlnBookingStatus());
			entity.setBlnRequestMeeting(mapped.getBlnRequestMeeting());
			entity.setDteDeliveryDate(mapped.getDteDeliveryDate());
			entity.setTxtDeliveryLocation(mapped.getTxtDeliveryLocation());
			entity.setTxtDeliveryTime(mapped.getTxtDeliveryTime());
			entity.setTxtRemarks(mapped.getTxtRemarks());
			entity.setTxtSpecialInstructions(mapped.getTxtSpecialInstructions());
			entity.setNumNumberOfGuests(mapped.getNumNumberOfGuests());
			entity.setNumNumberOfTables(mapped.getNumNumberOfGuests());
	
			// *** Validate Duplicate Date Check ***
			Boolean isalreadyBooked = false;

			if (mapped.getDteDeliveryDate() != null && entity.getDteDeliveryDate() != null
					&& mapped.getDteDeliveryDate().compareTo(entity.getDteDeliveryDate()) != 0) {
				isalreadyBooked = repositoryCateringDeliveryBooking
						.existsByDteDeliveryDateAndBlnIsDeletedFalse(mapped.getDteDeliveryDate());

			} else if (mapped.getDteDeliveryDate() != null && entity.getDteDeliveryDate() == null) {
				isalreadyBooked = repositoryCateringDeliveryBooking
						.existsByDteDeliveryDateAndBlnIsDeletedFalse(mapped.getDteDeliveryDate());

			}
			if (isalreadyBooked) {
				result.setTxtMessage("An catering event is already booked against this date." + mapped.getDteDeliveryDate());
				return result;
			}

			
			if (dto.getIsEditAllowed() != null) {
				entity.setIsEditAllowed(dto.getIsEditAllowed());
			}

			// *********************************************************************************************
			// ************************ Food Menu Categories and Sub Categories
			// ****************************
			// *********************************************************************************************
			if (dto.getMenuCategoriesSelection() != null && !dto.getMenuCategoriesSelection().isEmpty()) {

				// 🔥 FIX: PROPERLY CLEAR OLD MENU SELECTIONS WITH SESSION MANAGEMENT
				if (entity.getMenuCategorySelections() != null && !entity.getMenuCategorySelections().isEmpty()) {
					// Before clearing, we need to break the bidirectional relationships
					// This helps orphanRemoval work correctly
					List<EventMenuCategorySelection> categoriesToClear = new ArrayList<>(
							entity.getMenuCategorySelections());

					for (EventMenuCategorySelection category : categoriesToClear) {
						// Break relationship with EventMaster
						category.setEventMaster(null);

						if (category.getSubCategories() != null) {
							List<EventMenuSubCategorySelection> subCategoriesToClear = new ArrayList<>(
									category.getSubCategories());

							for (EventMenuSubCategorySelection subCategory : subCategoriesToClear) {
								// Break relationship with parent category
								subCategory.setEventCategory(null);

								if (subCategory.getItems() != null) {
									List<EventMenuFoodSelection> itemsToClear = new ArrayList<>(subCategory.getItems());

									for (EventMenuFoodSelection item : itemsToClear) {
										// Break relationships with EventMaster and SubCategory
										item.setEventMaster(null);
										item.setEventSubCategory(null);
									}

									// Clear the items collection
									subCategory.getItems().clear();
								}
							}

							// Clear the subcategories collection
							category.getSubCategories().clear();
						}
					}

					// Now clear the main collection - orphanRemoval will delete from DB
					entity.getMenuCategorySelections().clear();

					// 🔥 CRITICAL: Save immediately to persist deletions and clear session state
					// This flushes the deletions to DB and clears deleted entities from session
					entity = repositoryCateringDeliveryBooking.saveAndFlush(entity);

				} else if (entity.getMenuCategorySelections() == null) {
					entity.setMenuCategorySelections(new ArrayList<>());
				}

				// CREATE NEW MENU SELECTIONS
				for (DtoCustomerMenuCategory catDto : dto.getMenuCategoriesSelection()) {

					MenuItem category = menuItems.stream()
							.filter(item -> item.getSerMenuItemId() != null
									&& item.getSerMenuItemId().intValue() == catDto.getCategoryId().intValue())
							.findFirst().orElse(null);

					EventMenuCategorySelection catEntity = new EventMenuCategorySelection();
					catEntity.setCateringDeliveryBooking(entity);
					catEntity.setCategory(category);
					catEntity.setNumTotalPrice(catDto.getNumPrice());
					catEntity.setNumFinalPrice(catDto.getNumFinalPrice());

					// Initialize collections
					if (catEntity.getSubCategories() == null) {
						catEntity.setSubCategories(new ArrayList<>());
					}

					for (DtoCustomerMenuSubCategory subDto : catDto.getSubCategories()) {

						MenuItem subCategory = menuItems.stream()
								.filter(item -> item.getSerMenuItemId() != null
										&& item.getSerMenuItemId().intValue() == subDto.getSubCategoryId().intValue())
								.findFirst().orElse(null);

						EventMenuSubCategorySelection subEntity = new EventMenuSubCategorySelection();
						subEntity.setEventCategory(catEntity);
						subEntity.setSubCategory(subCategory);
						subEntity.setNumTotalPrice(subDto.getNumPrice());
						subEntity.setNumFinalPrice(subDto.getNumFinalPrice());

						// Initialize items collection
						if (subEntity.getItems() == null) {
							subEntity.setItems(new ArrayList<>());
						}

						// Simple Items
						for (DtoMenuItem itemDto : subDto.getItems()) {

							MenuItem menuItem = menuItems.stream().filter(item -> item.getSerMenuItemId() != null
									&& item.getSerMenuItemId().intValue() == itemDto.getSerMenuItemId().intValue())
									.findFirst().orElse(null);

							EventMenuFoodSelection itemEntity = new EventMenuFoodSelection();
//			                itemEntity.setEventMaster(entity);
							itemEntity.setEventSubCategory(subEntity);
							itemEntity.setMenuItem(menuItem);
							itemEntity.setNumPrice(itemDto.getNumPrice());
							itemEntity.setNumCalculatedPrice(itemDto.getNumCalculatedPrice());
							itemEntity.setNumFinalPrice(itemDto.getNumFinalPrice());

							subEntity.getItems().add(itemEntity);
						}

						// Composite Items If Exists
						for (DtoMenuComponentRequest itemDto : subDto.getCompositeItems()) {

							MenuItem menuItem = menuItems.stream().filter(item -> item.getSerMenuItemId() != null
									&& item.getSerMenuItemId().intValue() == itemDto.getParentMenuItemId().intValue())
									.findFirst().orElse(null);

							EventMenuFoodSelection itemEntity = new EventMenuFoodSelection();
//			                itemEntity.setEventMaster(entity);
							itemEntity.setEventSubCategory(subEntity);
							itemEntity.setMenuItem(menuItem);
							itemEntity.setNumPrice(itemDto.getNumPrice());
							itemEntity.setNumCalculatedPrice(itemDto.getNumCalculatedPrice());
							itemEntity.setNumFinalPrice(itemDto.getNumFinalPrice());

							subEntity.getItems().add(itemEntity);
						}

						catEntity.getSubCategories().add(subEntity);
					}

					entity.getMenuCategorySelections().add(catEntity);
				}

			}

			// Handle case when no menu selections in DTO but editing existing event
			else if (entity.getSerDeliveryBookingId() != null && entity.getMenuCategorySelections() != null
					&& !entity.getMenuCategorySelections().isEmpty()) {

				// User removed all menu selections - clear them properly
				List<EventMenuCategorySelection> categoriesToClear = new ArrayList<>(
						entity.getMenuCategorySelections());

				for (EventMenuCategorySelection category : categoriesToClear) {
					category.setEventMaster(null);
					if (category.getSubCategories() != null) {
						for (EventMenuSubCategorySelection subCategory : category.getSubCategories()) {
							subCategory.setEventCategory(null);
							if (subCategory.getItems() != null) {
								for (EventMenuFoodSelection item : subCategory.getItems()) {
									item.setEventMaster(null);
									item.setEventSubCategory(null);
								}
								subCategory.getItems().clear();
							}
						}
						category.getSubCategories().clear();
					}
				}

				entity.getMenuCategorySelections().clear();
				entity = repositoryCateringDeliveryBooking.saveAndFlush(entity);
			}
			// *********************************************************************************************
			// *********************************************************************************************
			// *********************************************************************************************

//			//*********************************************************************************************
//			//************************ Food Menu Categories and Sub Categories ****************************
//			//*********************************************************************************************
//			if (dto.getMenuCategoriesSelection() != null
//					&& !dto.getMenuCategoriesSelection().isEmpty()) {
//
//				// 🔥 CLEAR OLD MENU SELECTIONS (EDIT SAFE)
//				entity.getMenuCategorySelections().clear();
//
//				if (dto.getMenuCategoriesSelection() == null) {
//					result.setTxtMessage("Event saved without menu");
//					return result;
//				}
//
//				for (DtoCustomerMenuCategory catDto : dto.getMenuCategoriesSelection()) {
//
//					MenuItem category = menuItems.stream()
//							.filter(item -> item.getSerMenuItemId() != null
//									&& item.getSerMenuItemId().intValue() == catDto.getCategoryId().intValue())
//							.findFirst().orElse(null);
//
//					EventMenuCategorySelection catEntity = new EventMenuCategorySelection();
//					catEntity.setCateringDeliveryBooking(entity);
//					catEntity.setCategory(category);
//					catEntity.setNumTotalPrice(catDto.getNumPrice());
//					catEntity.setNumFinalPrice(catDto.getNumFinalPrice());
//
//					for (DtoCustomerMenuSubCategory subDto : catDto.getSubCategories()) {
//
//						MenuItem subCategory = menuItems.stream().filter(item -> item.getSerMenuItemId() != null
//								&& item.getSerMenuItemId().intValue() == subDto.getSubCategoryId().intValue())
//								.findFirst().orElse(null);
//
//						EventMenuSubCategorySelection subEntity = new EventMenuSubCategorySelection();
//						subEntity.setEventCategory(catEntity);
//						subEntity.setSubCategory(subCategory);
//						subEntity.setNumTotalPrice(subDto.getNumPrice());
//						subEntity.setNumFinalPrice(subDto.getNumFinalPrice());
//
//						//Simple Items
//						for (DtoMenuItem itemDto : subDto.getItems()) {
//
//							MenuItem menuItem = menuItems.stream().filter(item -> item.getSerMenuItemId() != null
//									&& item.getSerMenuItemId().intValue() == itemDto.getSerMenuItemId().intValue())
//									.findFirst().orElse(null);
//
//							EventMenuFoodSelection itemEntity = new EventMenuFoodSelection();
////							itemEntity.setEventMaster(entity);
//							itemEntity.setEventSubCategory(subEntity);
//							itemEntity.setMenuItem(menuItem);
//							itemEntity.setNumPrice(itemDto.getNumPrice()); // unit price
//							itemEntity.setNumCalculatedPrice(itemDto.getNumCalculatedPrice());
//							itemEntity.setNumFinalPrice(itemDto.getNumFinalPrice());
//
//							subEntity.getItems().add(itemEntity);
//						}
//
//						//Composite Items If Exists
//						for (DtoMenuComponentRequest itemDto : subDto.getCompositeItems()) {
//
//							MenuItem menuItem = menuItems.stream().filter(item -> item.getSerMenuItemId() != null
//									&& item.getSerMenuItemId().intValue() == itemDto.getParentMenuItemId().intValue())
//									.findFirst().orElse(null);
//
//							EventMenuFoodSelection itemEntity = new EventMenuFoodSelection();
//							itemEntity.setEventSubCategory(subEntity);
//							itemEntity.setMenuItem(menuItem);
//							itemEntity.setNumPrice(itemDto.getNumPrice()); // unit price
//							itemEntity.setNumCalculatedPrice(itemDto.getNumCalculatedPrice());
//							itemEntity.setNumFinalPrice(itemDto.getNumFinalPrice());
//
//							subEntity.getItems().add(itemEntity);
//						}
//
//						catEntity.getSubCategories().add(subEntity);
//					}
//
//					entity.getMenuCategorySelections().add(catEntity);
//				}
//
//			}

			// Setting Event Quoted Price
			// **************************

			EventBudget eventBudget = serviceEventBudget
					.getEventBudgetByCateringDelevieryBookingId(entity.getSerDeliveryBookingId());
			if (eventBudget != null) {
				if (eventBudget.getNumPaidAmount() != null
						&& eventBudget.getNumPaidAmount().compareTo(BigDecimal.ZERO) == 1) {
					eventBudget.setTxtStatus("Confirmed");

				} else if (dto.getDtoEventQuoteAndStatus() != null
						&& dto.getDtoEventQuoteAndStatus().getNumQuotedPrice() != null
						&& dto.getDtoEventQuoteAndStatus().getNumQuotedPrice().compareTo(BigDecimal.ZERO) == 1) {
					eventBudget.setTxtStatus("Quoted");
					eventBudget.setNumQuotedPrice(dto.getDtoEventQuoteAndStatus().getNumQuotedPrice());
					eventBudget.setNumPaidAmount(BigDecimal.ZERO);

				} else {
					eventBudget.setTxtStatus("Enquiry");
					eventBudget.setNumQuotedPrice(BigDecimal.ZERO);
					eventBudget.setNumPaidAmount(BigDecimal.ZERO);

				}

			} else {
				eventBudget = new EventBudget();
				if (dto.getDtoEventQuoteAndStatus().getNumQuotedPrice() != null
						&& dto.getDtoEventQuoteAndStatus().getNumQuotedPrice().compareTo(BigDecimal.ZERO) == 1) {
					eventBudget.setTxtStatus("Quoted");
					eventBudget.setNumQuotedPrice(dto.getDtoEventQuoteAndStatus().getNumQuotedPrice());
					eventBudget.setNumPaidAmount(BigDecimal.ZERO);

				} else {
					eventBudget.setTxtStatus("Enquiry");
					eventBudget.setNumQuotedPrice(BigDecimal.ZERO);
					eventBudget.setNumPaidAmount(BigDecimal.ZERO);

				}

			}

			repositoryCateringDeliveryBooking.save(entity);

			if (eventBudget != null) {
				eventBudget.setCateringDeliveryBooking(entity);
				serviceEventBudget.save(eventBudget);
			}

			result.setTxtMessage("Success");
			result.setResult(MapperCateringDeliveryBooking.toDto(entity));

		} catch (RuntimeException e) {
			result.setTxtMessage("Error: " + e.getMessage());
		} catch (Exception e) {
			result.setTxtMessage("Unexpected error while saving: " + e.getMessage());
		}

		return result;
	}

	@Override
	public DtoResult softDelete(Integer id) {
		DtoResult result = new DtoResult();
		try {
			CateringDeliveryBooking entity = repositoryCateringDeliveryBooking.findById(id)
					.orElseThrow(() -> new RuntimeException("Booking not found"));
			entity.setBlnIsDeleted(true);
			repositoryCateringDeliveryBooking.save(entity);
			result.setTxtMessage("Deleted successfully");
		} catch (Exception e) {
			result.setTxtMessage("Error while deleting: " + e.getMessage());
		}
		return result;
	}

	@Override
	public DtoResult getByPK(Integer id) {
		DtoResult result = new DtoResult();
		repositoryCateringDeliveryBooking.findBySerDeliveryBookingIdAndBlnIsDeletedFalse(id).ifPresentOrElse(
				booking -> result.setResult(MapperCateringDeliveryBooking.toDto(booking)),
				() -> result.setTxtMessage("Not found"));
		return result;
	}

	@Override
	public DtoResult getByPKCP(Integer id) {
		DtoResult result = new DtoResult();
		repositoryCateringDeliveryBooking.findBySerDeliveryBookingIdAndBlnIsDeletedFalse(id).ifPresentOrElse(
				booking -> result.setResult(MapperCateringDeliveryBooking.toDtoCP(booking)),
				() -> result.setTxtMessage("Not found"));
		return result;
	}

	@Override
	public DtoResult getAll() {
		DtoResult result = new DtoResult();
		List<CateringDeliveryBooking> bookings = repositoryCateringDeliveryBooking.findByBlnIsDeletedFalse();
		List<DtoCateringDeliveryBooking> list = bookings.stream().map(MapperCateringDeliveryBooking::toDto)
				.collect(Collectors.toList());
		result.setResulList(new ArrayList<>(list));
		result.setTxtMessage("Fetched successfully");
		return result;
	}

	@Override
	public DtoResult getAllCP() {
		DtoResult result = new DtoResult();
//		List<CateringDeliveryBooking> bookings = repositoryCateringDeliveryBooking.findByBlnIsDeletedFalse();
		List<CateringDeliveryBooking> bookings = repositoryCateringDeliveryBooking.findAllSortedByClosestDeliveryDate();
		List<DtoCateringDeliveryBooking> list = bookings.stream().map(MapperCateringDeliveryBooking::toDtoCP)
				.collect(Collectors.toList());
		result.setResulList(new ArrayList<>(list));
		result.setTxtMessage("Fetched successfully");
		return result;
	}

	@Override
	public DtoResult getByCustId(DtoSearch dtoSearch) {
		DtoResult result = new DtoResult();
		List<CateringDeliveryBooking> bookings = repositoryCateringDeliveryBooking.findByCustomerId(dtoSearch.getId());
		List<DtoCateringDeliveryBooking> list = bookings.stream().map(MapperCateringDeliveryBooking::toDto)
				.collect(Collectors.toList());
		result.setResulList(new ArrayList<>(list));
		result.setTxtMessage("Fetched successfully");
		return result;
	}

	@Override
	public DtoResult search(String keyword) {
		DtoResult result = new DtoResult();
		List<CateringDeliveryBooking> all = repositoryCateringDeliveryBooking.findByBlnIsDeletedFalse();
		List<DtoCateringDeliveryBooking> filtered = all.stream()
				.filter(e -> e.getTxtDeliveryLocation() != null
						&& e.getTxtDeliveryLocation().toLowerCase().contains(keyword.toLowerCase()))
				.map(MapperCateringDeliveryBooking::toDto).collect(Collectors.toList());
		result.setResulList(new ArrayList<>(filtered));
		result.setTxtMessage("Filtered");
		return result;
	}

	public String generateAutoCode() {
		String maxCode = repositoryCateringDeliveryBooking.findMaxCateringDeliveryBookingCode();

		int nextNumber = 1;

		if (maxCode != null && maxCode.startsWith("CDB-")) {
			try {
				String numberPart = maxCode.substring(4);
				nextNumber = Integer.parseInt(numberPart) + 1;
			} catch (NumberFormatException e) {
				nextNumber = 1;
			}
		}

		return String.format("CDB-%03d", nextNumber);
	}

	@Override
	public Page<DtoCateringDeliveryBooking> searchCateringDeliveryBookings(DtoCateringDeliveryBookingSearch dto) {
		int page = dto.getPage() != null && dto.getPage() >= 0 ? dto.getPage() : 0;
		int size = dto.getSize() != null && dto.getSize() > 0 ? Math.min(dto.getSize(), 200) : 20;

		String sortBy = dto.getSortBy() != null ? dto.getSortBy() : "dteDeliveryDate";

		Sort.Direction dir = "ASC".equalsIgnoreCase(dto.getSortDir()) ? Sort.Direction.ASC : Sort.Direction.DESC;

		Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));

		Specification<CateringDeliveryBooking> spec = SpecificationsCateringDeliveryBooking.fromDto(dto);

		Page<CateringDeliveryBooking> pageResult = repositoryCateringDeliveryBooking.findAll(spec, pageable);

		List<CateringDeliveryBooking> sorted = applyDeliveryDateSorting(pageResult.getContent());

		List<DtoCateringDeliveryBooking> dtos = sorted.stream().map(MapperCateringDeliveryBooking::toDtoCP).toList();

		return new PageImpl<>(dtos, pageable, pageResult.getTotalElements());
	}

	private List<CateringDeliveryBooking> applyDeliveryDateSorting(List<CateringDeliveryBooking> list) {
		Date now = new Date();

		List<CateringDeliveryBooking> future = new ArrayList<>();
		List<CateringDeliveryBooking> past = new ArrayList<>();
		List<CateringDeliveryBooking> noDate = new ArrayList<>();

		for (CateringDeliveryBooking b : list) {
			if (b.getDteDeliveryDate() == null) {
				noDate.add(b);
			} else if (!b.getDteDeliveryDate().before(now)) {
				future.add(b);
			} else {
				past.add(b);
			}
		}

		future.sort(Comparator.comparing(CateringDeliveryBooking::getDteDeliveryDate));
		past.sort(Comparator.comparing(CateringDeliveryBooking::getDteDeliveryDate).reversed());

		List<CateringDeliveryBooking> out = new ArrayList<>();
		out.addAll(future);
		out.addAll(past);
		out.addAll(noDate);
		return out;
	}
	
	@Transactional
	public DtoResult validateEventDateAvailability(Date eventDate) {
		DtoResult dtoResult = new DtoResult();
		try {
			boolean alreadyBooked = repositoryCateringDeliveryBooking
					.existsByDteDeliveryDateAndBlnIsDeletedFalse(eventDate);

			if (alreadyBooked) {
				dtoResult.setTxtMessage("A catering is already registered at this date.");
				dtoResult.setResult(alreadyBooked);
			} else {
				dtoResult.setTxtMessage("No catering is registered at this date.");
				dtoResult.setResult(alreadyBooked);
			}
			return dtoResult;
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			dtoResult.setTxtMessage(e.getMessage());
			return dtoResult;
		}

	}

	@Override
	public DtoResult getAlreadyBookedDates() {
		DtoResult dtoResult = new DtoResult();
		try {
			List<Date> dates = repositoryCateringDeliveryBooking.getAlreadyBookedDates();
			List<String> strDates = new ArrayList<>();
			for (Date date : dates) {
				String strdate = UtilDateAndTime.mmddyyyyDateToString(date);
				strDates.add(strdate);
			}
			dtoResult.setTxtMessage("Success");
			dtoResult.setResult(strDates);
			return dtoResult;
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			dtoResult.setTxtMessage(e.getMessage());
			return dtoResult;
		}
	}

}
