package com.zbs.de.mapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.zbs.de.model.CateringDeliveryBooking;
import com.zbs.de.model.CateringDeliveryItemDetail;
import com.zbs.de.model.EventMenuCategorySelection;
import com.zbs.de.model.EventMenuFoodSelection;
import com.zbs.de.model.EventMenuSubCategorySelection;
import com.zbs.de.model.MenuFoodMaster;
import com.zbs.de.model.MenuItem;
import com.zbs.de.model.dto.DtoCateringDeliveryBooking;
import com.zbs.de.model.dto.DtoCateringDeliveryItemDetail;
import com.zbs.de.model.dto.DtoEventQuoteAndStatus;
import com.zbs.de.model.dto.DtoMenuFoodMaster;
import com.zbs.de.model.dto.DtoMenuItem;
import com.zbs.de.model.dto.menu.DtoCustomerMenuCategory;
import com.zbs.de.model.dto.menu.DtoCustomerMenuSubCategory;
import com.zbs.de.util.UtilDateAndTime;
import com.zbs.de.util.UtilRandomKey;

public class MapperCateringDeliveryBooking {
	public static DtoCateringDeliveryBooking toDto(CateringDeliveryBooking entity) {
		if (UtilRandomKey.isNull(entity)) {
			return null;
		}
		DtoCateringDeliveryBooking dto = new DtoCateringDeliveryBooking();
		dto.setSerDeliveryBookingId(entity.getSerDeliveryBookingId());
		dto.setTxtBookingStatus(entity.getTxtBookingStatus());
		dto.setTxtDeliveryBookingCode(entity.getTxtDeliveryBookingCode());
		dto.setBlnBookingStatus(entity.getBlnBookingStatus());
		dto.setBlnRequestMeeting(entity.getBlnRequestMeeting());
		if (UtilRandomKey.isNotNull(entity.getDteDeliveryDate())) {
			dto.setDteDeliveryDate(UtilDateAndTime.mmddyyyyDateToString(entity.getDteDeliveryDate()));
		}
		dto.setTxtDeliveryLocation(entity.getTxtDeliveryLocation());
		dto.setTxtDeliveryTime(entity.getTxtDeliveryTime());
		dto.setTxtRemarks(entity.getTxtRemarks());
		dto.setTxtSpecialInstructions(entity.getTxtSpecialInstructions());
		dto.setIsEditAllowed(entity.getIsEditAllowed());

		if (entity.getCustomerMaster() != null) {
			dto.setSerCustId(entity.getCustomerMaster().getSerCustId());
			dto.setTxtCustCode(entity.getCustomerMaster().getTxtCustCode());
			dto.setTxtCustName(entity.getCustomerMaster().getTxtCustName());
		}
		if (entity.getEventType() != null) {
			dto.setSerEventTypeId(entity.getEventType().getSerEventTypeId());
			dto.setTxtEventTypeCode(entity.getEventType().getTxtEventTypeCode());
			dto.setTxtEventTypeName(entity.getEventType().getTxtEventTypeName());
		}
//		if(entity.getCateringDeliveryItemDetails() != null && !entity.getCateringDeliveryItemDetails().isEmpty()) {
//			List<DtoCateringDeliveryItemDetail> details = new ArrayList<>();
//			for(CateringDeliveryItemDetail detail : entity.getCateringDeliveryItemDetails()) {
//				DtoCateringDeliveryItemDetail detailDto = toDtoCateringDeliveryItemDetail(detail);
//				details.add(detailDto);
//			}
//			
//			dto.setCateringDeliveryItemDetails(details);
//		}

		// **********************************************************************************************
		// ************************ Food Selections Category and SubCategory Pricing ********************
		// **********************************************************************************************
		List<DtoCustomerMenuCategory> catDtos = new ArrayList<>();

		for (EventMenuCategorySelection cat : entity.getMenuCategorySelections()) {

			DtoCustomerMenuCategory catDto = new DtoCustomerMenuCategory();
			catDto.setCategoryId(cat.getCategory().getSerMenuItemId().longValue());
			catDto.setCategoryName(cat.getCategory().getTxtName());
			catDto.setNumPrice(cat.getNumTotalPrice());
			catDto.setNumFinalPrice(cat.getNumFinalPrice());

			List<DtoCustomerMenuSubCategory> subDtos = new ArrayList<>();

			for (EventMenuSubCategorySelection sub : cat.getSubCategories()) {

				DtoCustomerMenuSubCategory subDto = new DtoCustomerMenuSubCategory();
				subDto.setSubCategoryId(sub.getSubCategory().getSerMenuItemId().longValue());
				subDto.setSubCategoryName(sub.getSubCategory().getTxtName());
				subDto.setNumPrice(sub.getNumTotalPrice());
				subDto.setNumFinalPrice(sub.getNumFinalPrice());

				List<DtoMenuItem> itemDtos = new ArrayList<>();

				for (EventMenuFoodSelection item : sub.getItems()) {

					MenuItem mi = item.getMenuItem();

					DtoMenuItem itemDto = new DtoMenuItem();
					itemDto.setSerMenuItemId(mi.getSerMenuItemId().longValue());
					itemDto.setTxtCode(mi.getTxtCode());
					itemDto.setTxtName(mi.getTxtName());
					itemDto.setTxtShortName(mi.getTxtShortName());
					itemDto.setTxtDescription(mi.getTxtDescription());
					itemDto.setNumPrice(item.getNumPrice());
					itemDto.setNumCalculatedPrice(item.getNumCalculatedPrice());
					itemDto.setNumFinalPrice(item.getNumFinalPrice());

					itemDtos.add(itemDto);
				}

				subDto.setItems(itemDtos);
				subDtos.add(subDto);
			}

			catDto.setSubCategories(subDtos);
			catDtos.add(catDto);
		}
		dto.setMenuCategoriesSelection(catDtos);
		
		if (entity.getEventBudget() != null) {

			DtoEventQuoteAndStatus quote = new DtoEventQuoteAndStatus();
			quote.setNumQuotedPrice(entity.getEventBudget().getNumQuotedPrice());
			quote.setNumPaidAmount(entity.getEventBudget().getNumPaidAmount());
			quote.setTxtStatus(entity.getEventBudget().getTxtStatus());
			if(entity.getEventBudget().getNumQuotedPrice() != null && entity.getEventBudget().getNumPaidAmount() != null) {
				quote.setNumPendingAmount(entity.getEventBudget().getNumQuotedPrice().subtract(entity.getEventBudget().getNumPaidAmount()));
			}else {
				quote.setNumPendingAmount(BigDecimal.ZERO);
			}
			dto.setDtoEventQuoteAndStatus(quote);
		}


		return dto;
	}
	
	
	public static DtoCateringDeliveryBooking toDtoCP(CateringDeliveryBooking entity) {
		if (UtilRandomKey.isNull(entity)) {
			return null;
		}
		DtoCateringDeliveryBooking dto = new DtoCateringDeliveryBooking();
		dto.setSerDeliveryBookingId(entity.getSerDeliveryBookingId());
		dto.setTxtBookingStatus(entity.getTxtBookingStatus());
		dto.setTxtDeliveryBookingCode(entity.getTxtDeliveryBookingCode());
		dto.setBlnBookingStatus(entity.getBlnBookingStatus());
		dto.setBlnRequestMeeting(entity.getBlnRequestMeeting());
		if (UtilRandomKey.isNotNull(entity.getDteDeliveryDate())) {
			dto.setDteDeliveryDate(UtilDateAndTime.mmddyyyyDateToString(entity.getDteDeliveryDate()));
		}
		dto.setTxtDeliveryLocation(entity.getTxtDeliveryLocation());
		dto.setTxtDeliveryTime(entity.getTxtDeliveryTime());
		dto.setTxtRemarks(entity.getTxtRemarks());
		dto.setTxtSpecialInstructions(entity.getTxtSpecialInstructions());
		dto.setIsEditAllowed(entity.getIsEditAllowed());

		if (entity.getCustomerMaster() != null) {
			dto.setSerCustId(entity.getCustomerMaster().getSerCustId());
			dto.setTxtCustCode(entity.getCustomerMaster().getTxtCustCode());
			dto.setTxtCustName(entity.getCustomerMaster().getTxtCustName());
		}
		if (entity.getEventType() != null) {
			dto.setSerEventTypeId(entity.getEventType().getSerEventTypeId());
			dto.setTxtEventTypeCode(entity.getEventType().getTxtEventTypeCode());
			dto.setTxtEventTypeName(entity.getEventType().getTxtEventTypeName());
		}
//		if(entity.getCateringDeliveryItemDetails() != null && !entity.getCateringDeliveryItemDetails().isEmpty()) {
////			List<DtoCateringDeliveryItemDetail> details = new ArrayList<>();
////			for(CateringDeliveryItemDetail detail : entity.getCateringDeliveryItemDetails()) {
////				DtoCateringDeliveryItemDetail detailDto = toDtoCateringDeliveryItemDetail(detail);
////				details.add(detailDto);
////			}
////			
////			dto.setCateringDeliveryItemDetails(details);
//			
//
////			Map<String, List<DtoMenuFoodMaster>> foodSelectionsMap = new HashMap<>();
//			List<DtoMenuFoodMaster> foodSelections = new ArrayList<>();
//
//			for (CateringDeliveryItemDetail detail : entity.getCateringDeliveryItemDetails()) {
//				if (detail.getMenuItem() != null) {
//					DtoMenuFoodMaster dtoMenuFoodMaster = new DtoMenuFoodMaster();
////					MenuFoodMaster foodMaster = detail.getMenueFoodMaster();
//					MenuItem menuItem = detail.getMenuItem();
//
////					dtoMenuFoodMaster.setSerMenuFoodId(foodMaster.getSerMenuFoodId());
////					dtoMenuFoodMaster.setTxtMenuFoodCode(foodMaster.getTxtMenuFoodCode());
////					dtoMenuFoodMaster.setTxtMenuFoodName(foodMaster.getTxtMenuFoodName());
////					dtoMenuFoodMaster.setBlnIsMainCourse(foodMaster.getBlnIsMainCourse());
////					dtoMenuFoodMaster.setBlnIsAppetiser(foodMaster.getBlnIsAppetiser());
////					dtoMenuFoodMaster.setBlnIsStarter(foodMaster.getBlnIsStarter());
////					dtoMenuFoodMaster.setBlnIsSaladAndCondiment(foodMaster.getBlnIsSaladAndCondiment());
////					dtoMenuFoodMaster.setBlnIsDessert(foodMaster.getBlnIsDessert());
////					dtoMenuFoodMaster.setBlnIsDrink(foodMaster.getBlnIsDrink());
////					dtoMenuFoodMaster.setBlnIsActive(foodMaster.getBlnIsActive());
//					
//					dtoMenuFoodMaster.setBlnIsActive(menuItem.getBlnIsActive());
//					dtoMenuFoodMaster.setSerMenuItemId(menuItem.getSerMenuItemId());
//					dtoMenuFoodMaster.setTxtName(menuItem.getTxtName());
//					dtoMenuFoodMaster.setTxtCode(menuItem.getTxtCode());
//					dtoMenuFoodMaster.setTxtDescription(menuItem.getTxtDescription());
//					dtoMenuFoodMaster.setNumPrice(detail.getNumPrice());
//
////					String foodType = getFoodType(foodMaster);
//
////					if (!foodSelectionsMap.containsKey(foodType)) {
////						foodSelectionsMap.put(foodType, new ArrayList<>());
////					}
////					foodSelectionsMap.get(foodType).add(dtoMenuFoodMaster);
//					foodSelections.add(dtoMenuFoodMaster);
//					
//				}
//			}
//
//			dto.setFoodSelections(foodSelections);
//		}
		
		// **********************************************************************************************
		// ************************ Food Selections Category and SubCategory Pricing ********************
		// **********************************************************************************************
		List<DtoCustomerMenuCategory> catDtos = new ArrayList<>();

		for (EventMenuCategorySelection cat : entity.getMenuCategorySelections()) {

			DtoCustomerMenuCategory catDto = new DtoCustomerMenuCategory();
			catDto.setCategoryId(cat.getCategory().getSerMenuItemId().longValue());
			catDto.setCategoryName(cat.getCategory().getTxtName());
			catDto.setNumPrice(cat.getNumTotalPrice());
			catDto.setNumFinalPrice(cat.getNumFinalPrice());

			List<DtoCustomerMenuSubCategory> subDtos = new ArrayList<>();

			for (EventMenuSubCategorySelection sub : cat.getSubCategories()) {

				DtoCustomerMenuSubCategory subDto = new DtoCustomerMenuSubCategory();
				subDto.setSubCategoryId(sub.getSubCategory().getSerMenuItemId().longValue());
				subDto.setSubCategoryName(sub.getSubCategory().getTxtName());
				subDto.setNumPrice(sub.getNumTotalPrice());
				subDto.setNumFinalPrice(sub.getNumFinalPrice());

				List<DtoMenuItem> itemDtos = new ArrayList<>();

				for (EventMenuFoodSelection item : sub.getItems()) {

					MenuItem mi = item.getMenuItem();

					DtoMenuItem itemDto = new DtoMenuItem();
					itemDto.setSerMenuItemId(mi.getSerMenuItemId().longValue());
					itemDto.setTxtCode(mi.getTxtCode());
					itemDto.setTxtName(mi.getTxtName());
					itemDto.setTxtShortName(mi.getTxtShortName());
					itemDto.setTxtDescription(mi.getTxtDescription());
					itemDto.setNumPrice(item.getNumPrice());
					itemDto.setNumCalculatedPrice(item.getNumCalculatedPrice());
					itemDto.setNumFinalPrice(item.getNumFinalPrice());

					itemDtos.add(itemDto);
				}

				subDto.setItems(itemDtos);
				subDtos.add(subDto);
			}

			catDto.setSubCategories(subDtos);
			catDtos.add(catDto);
		}
		dto.setMenuCategoriesSelection(catDtos);

		if (entity.getEventBudget() != null) {

			DtoEventQuoteAndStatus quote = new DtoEventQuoteAndStatus();
			quote.setNumQuotedPrice(entity.getEventBudget().getNumQuotedPrice());
			quote.setNumPaidAmount(entity.getEventBudget().getNumPaidAmount());
			quote.setTxtStatus(entity.getEventBudget().getTxtStatus());
			if (entity.getEventBudget().getNumQuotedPrice() != null
					&& entity.getEventBudget().getNumPaidAmount() != null) {
				quote.setNumPendingAmount(entity.getEventBudget().getNumQuotedPrice()
						.subtract(entity.getEventBudget().getNumPaidAmount()));
			} else {
				quote.setNumPendingAmount(BigDecimal.ZERO);
			}
			dto.setDtoEventQuoteAndStatus(quote);
		}

		return dto;
	}
	
	public static DtoCateringDeliveryItemDetail toDtoCateringDeliveryItemDetail(CateringDeliveryItemDetail entity) {
		if(entity != null) {
			DtoCateringDeliveryItemDetail dto =new DtoCateringDeliveryItemDetail();
			dto.setNumQuantity(entity.getNumQuantity());
			dto.setSerCateringDeliveryDetailId(entity.getSerCateringDeliveryDetailId());
			dto.setTxtCateringDeliveryDetailCode(entity.getTxtCateringDeliveryDetailCode());
			dto.setTxtNotes(entity.getTxtCateringDeliveryDetailCode());
			dto.setNumPrice(entity.getNumPrice());
//			if(entity.getMenueFoodMaster() != null) {
//				dto.setSerMenuFoodId(entity.getMenueFoodMaster().getSerMenuFoodId());
//				dto.setTxtMenuFoodCode(entity.getMenueFoodMaster().getTxtMenuFoodCode());
//				dto.setTxtMenuFoodName(entity.getMenueFoodMaster().getTxtMenuFoodName());
//			}
			if(entity.getMenuItem() != null) {
				dto.setSerMenuItemId(entity.getMenuItem().getSerMenuItemId());
				dto.setTxtCode(entity.getMenuItem().getTxtCode());
				dto.setTxtName(entity.getMenuItem().getTxtName());
				dto.setTxtDescription(entity.getMenuItem().getTxtDescription());
			}
			return dto;
		}
		return null;
		
	}

	public static CateringDeliveryBooking toEntity(DtoCateringDeliveryBooking dto) {
		if (UtilRandomKey.isNull(dto)) {
			return null;
		}
		CateringDeliveryBooking entity = new CateringDeliveryBooking();
		entity.setSerDeliveryBookingId(dto.getSerDeliveryBookingId());
		entity.setTxtBookingStatus(dto.getTxtBookingStatus());
		entity.setTxtDeliveryBookingCode(dto.getTxtDeliveryBookingCode());
		entity.setBlnBookingStatus(dto.getBlnBookingStatus());
		entity.setBlnRequestMeeting(dto.getBlnRequestMeeting());
		if (UtilRandomKey.isNotNull(dto.getDteDeliveryDate())) {
			entity.setDteDeliveryDate(UtilDateAndTime.ddMMyyyyDashedStringToDate(dto.getDteDeliveryDate()));
		}
		entity.setTxtDeliveryLocation(dto.getTxtDeliveryLocation());
		entity.setTxtDeliveryTime(dto.getTxtDeliveryTime());
		entity.setTxtRemarks(dto.getTxtRemarks());
		entity.setTxtSpecialInstructions(dto.getTxtSpecialInstructions());
		entity.setIsEditAllowed(dto.getIsEditAllowed());

		return entity;
	}
	
	private static String getFoodType(MenuFoodMaster dtoMenuFoodMaster) {
		if (UtilRandomKey.isNotNull(dtoMenuFoodMaster.getBlnIsDrink()) && dtoMenuFoodMaster.getBlnIsDrink()) {
			return "Drink";
		} else if (UtilRandomKey.isNotNull(dtoMenuFoodMaster.getBlnIsDessert())
				&& dtoMenuFoodMaster.getBlnIsDessert()) {
			return "Dessert";
		} else if (UtilRandomKey.isNotNull(dtoMenuFoodMaster.getBlnIsAppetiser())
				&& dtoMenuFoodMaster.getBlnIsAppetiser()) {
			return "Appetiser";
		} else if (UtilRandomKey.isNotNull(dtoMenuFoodMaster.getBlnIsMainCourse())
				&& dtoMenuFoodMaster.getBlnIsMainCourse()) {
			return "MainCourse";
		} else if (UtilRandomKey.isNotNull(dtoMenuFoodMaster.getBlnIsSaladAndCondiment())
				&& dtoMenuFoodMaster.getBlnIsSaladAndCondiment()) {
			return "SaladAndCondiment";
		} else if (UtilRandomKey.isNotNull(dtoMenuFoodMaster.getBlnIsStarter())
				&& dtoMenuFoodMaster.getBlnIsStarter()) {
			return "Starter";
		} else {
			return null;
		}

	}
}
