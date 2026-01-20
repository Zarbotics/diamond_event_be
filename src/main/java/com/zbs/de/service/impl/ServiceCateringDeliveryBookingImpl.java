package com.zbs.de.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zbs.de.mapper.MapperCateringDeliveryBooking;
import com.zbs.de.model.CateringDeliveryBooking;
import com.zbs.de.model.CateringDeliveryItemDetail;
import com.zbs.de.model.EventBudget;
import com.zbs.de.model.MenuFoodMaster;
import com.zbs.de.model.MenuItem;
import com.zbs.de.model.dto.DtoCateringDeliveryBooking;
import com.zbs.de.model.dto.DtoCateringDeliveryItemDetail;
import com.zbs.de.model.dto.DtoMenuFoodMaster;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.repository.RepositoryCateringDeliveryBooking;
import com.zbs.de.repository.RepositoryCustomerMaster;
import com.zbs.de.repository.RepositoryEventType;
import com.zbs.de.repository.RepositoryMenuFoodMaster;
import com.zbs.de.service.ServiceCateringDeliveryBooking;
import com.zbs.de.service.ServiceEventBudget;
import com.zbs.de.service.ServiceMenuFoodMaster;
import com.zbs.de.service.ServiceMenuItem;
import com.zbs.de.util.UtilRandomKey;

import jakarta.persistence.EntityNotFoundException;

@Service("serviceCateringDeliveryBookingImpl")
public class ServiceCateringDeliveryBookingImpl implements ServiceCateringDeliveryBooking {

	@Autowired
	private RepositoryCateringDeliveryBooking repositoryCateringDeliveryBooking;

	@Autowired
	private RepositoryCustomerMaster repositoryCustomerMaster;

	@Autowired
	private RepositoryEventType repositoryEventType;

	@Autowired
	private RepositoryMenuFoodMaster repositoryMenuFoodMaster;

	@Autowired
	private ServiceMenuFoodMaster serviceMenuFoodMaster;

	@Autowired
	private ServiceEventBudget serviceEventBudget;

	@Autowired
	private ServiceMenuItem serviceMenuItem;

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

			// Map item details
			List<CateringDeliveryItemDetail> detailList = new ArrayList<>();
			if (dto.getCateringDeliveryItemDetails() != null) {
				for (DtoCateringDeliveryItemDetail detailDto : dto.getCateringDeliveryItemDetails()) {
//					if (detailDto.getSerMenuFoodId() == null) {
//						throw new IllegalArgumentException("Menu Food ID is required for each delivery item.");
//					}

					if (detailDto.getSerMenuItemId() == null) {
						throw new IllegalArgumentException("Menu Food ID is required for each delivery item.");
					}

//					MenuFoodMaster food = repositoryMenuFoodMaster.findById(detailDto.getSerMenuFoodId())
//							.orElseThrow(() -> new EntityNotFoundException(
//									"Menu Food not found with ID: " + detailDto.getSerMenuFoodId()));

					MenuItem menuItem = serviceMenuItem.getMenuItemById(detailDto.getSerMenuItemId());
					if (menuItem == null) {
						result.setTxtMessage("Menu Food not found with ID: " + detailDto.getSerMenuItemId());
						return result;
					}

					CateringDeliveryItemDetail detail = new CateringDeliveryItemDetail();
					detail.setCateringDeliveryBooking(entity);
//					detail.setMenueFoodMaster(food);
					detail.setMenuItem(menuItem);
					detail.setNumPrice(detailDto.getNumPrice());
					detailList.add(detail);
				}
			}

			entity.setCateringDeliveryItemDetails(detailList);

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
			if (dto.getIsEditAllowed() != null) {
				entity.setIsEditAllowed(dto.getIsEditAllowed());
			}

//			// Map item details
//			List<CateringDeliveryItemDetail> detailList = new ArrayList<>();
//			if (dto.getCateringDeliveryItemDetails() != null) {
//				for (DtoCateringDeliveryItemDetail detailDto : dto.getCateringDeliveryItemDetails()) {
//					if (detailDto.getSerMenuFoodId() == null) {
//						throw new IllegalArgumentException("Menu Food ID is required for each delivery item.");
//					}
//
//					MenuFoodMaster food = repositoryMenuFoodMaster.findById(detailDto.getSerMenuFoodId())
//							.orElseThrow(() -> new EntityNotFoundException(
//									"Menu Food not found with ID: " + detailDto.getSerMenuFoodId()));
//
//					CateringDeliveryItemDetail detail = new CateringDeliveryItemDetail();
//					detail.setCateringDeliveryBooking(entity);
//					detail.setMenueFoodMaster(food);
//					detailList.add(detail);
//				}
//			}

//			entity.setCateringDeliveryItemDetails(detailList);

//			// Set Food Menu Selection
//			// ***********************
//			if (dto.getFoodSelections() != null && !dto.getFoodSelections().isEmpty()) {
//				Map<String, List<DtoMenuFoodMaster>> foodSelectionsMap = dto.getFoodSelections();
//				List<CateringDeliveryItemDetail> detailList = new ArrayList<>();
//				for (Map.Entry<String, List<DtoMenuFoodMaster>> entry : foodSelectionsMap.entrySet()) {
//					String foodType = entry.getKey(); // e.g., "MainCourse", "Starter"
//					List<DtoMenuFoodMaster> foodList = entry.getValue();
//
//					for (DtoMenuFoodMaster dtoMenuFoodMaster : foodList) {
//						if (UtilRandomKey.isNotNull(dtoMenuFoodMaster.getSerMenuFoodId())) {
//							// Find the actual MenuFoodMaster from your list
//							MenuFoodMaster menuFoodMaster = dtoMenuFoodMasterLst.stream()
//									.filter(food -> food.getSerMenuFoodId() != null && food.getSerMenuFoodId()
//											.intValue() == dtoMenuFoodMaster.getSerMenuFoodId().intValue())
//									.findFirst().orElse(null);
//
//							if (UtilRandomKey.isNotNull(menuFoodMaster)) {
//								CateringDeliveryItemDetail detail = new CateringDeliveryItemDetail();
//								detail.setCateringDeliveryBooking(entity);
//								detail.setMenueFoodMaster(menuFoodMaster);
//								detailList.add(detail);
//							} else {
//								result.setTxtMessage("Food Selection Item Does Not Have Food Menu With Id: "
//										+ dtoMenuFoodMaster.getSerMenuFoodId() + " In DB.");
//								return result;
//							}
//						} else {
//							result.setTxtMessage("Food Selection Item Does Not Have The Id OF Food Menu");
//							return result;
//						}
//					}
//				}
//				entity.setCateringDeliveryItemDetails(detailList);
//
//			}

			// Set Food Menu Selection
			// ***********************
			if (dto.getFoodSelections() != null && !dto.getFoodSelections().isEmpty()) {

				List<DtoMenuFoodMaster> foodSelections = dto.getFoodSelections();

				List<CateringDeliveryItemDetail> detailList = new ArrayList<>();

				for (DtoMenuFoodMaster dtoMenuFoodMaster : foodSelections) {
					if (UtilRandomKey.isNotNull(dtoMenuFoodMaster.getSerMenuItemId())) {
						// Find the actual MenuFoodMaster from your list
						MenuItem menuItem = menuItems.stream()
								.filter(item -> item.getSerMenuItemId() != null && item.getSerMenuItemId()
										.intValue() == dtoMenuFoodMaster.getSerMenuItemId().intValue())
								.findFirst().orElse(null);

						if (UtilRandomKey.isNotNull(menuItem)) {
							CateringDeliveryItemDetail detail = new CateringDeliveryItemDetail();
							detail.setCateringDeliveryBooking(entity);
//							detail.setMenueFoodMaster(menuFoodMaster);
							detail.setMenuItem(menuItem);
							detail.setNumPrice(dtoMenuFoodMaster.getNumPrice());
							detailList.add(detail);
						} else {
							result.setTxtMessage("Food Selection Item Does Not Have Food Menu With Id: "
									+ dtoMenuFoodMaster.getSerMenuFoodId() + " In DB.");
							return result;
						}
					} else {
						result.setTxtMessage("Food Selection Item Does Not Have The Id OF Food Menu");
						return result;
					}
				}

				entity.setCateringDeliveryItemDetails(detailList);

			}

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

}
