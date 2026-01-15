package com.zbs.de.service.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.springframework.web.multipart.MultipartFile;
import com.zbs.de.mapper.*;
import com.zbs.de.model.CustomerMaster;
import com.zbs.de.model.DecorCategoryPropertyMaster;
import com.zbs.de.model.DecorCategoryPropertyValue;
import com.zbs.de.model.DecorExtrasMaster;
import com.zbs.de.model.DecorExtrasOption;
import com.zbs.de.model.EventBudget;
import com.zbs.de.model.EventDecorCategorySelection;
import com.zbs.de.model.EventDecorExtrasSelection;
import com.zbs.de.model.EventDecorPropertySelection;
import com.zbs.de.model.EventDecorReferenceDocument;
import com.zbs.de.model.EventMaster;
import com.zbs.de.model.EventMenuFoodSelection;
import com.zbs.de.model.EventRunningOrder;
import com.zbs.de.model.EventType;
import com.zbs.de.model.MenuFoodMaster;
import com.zbs.de.model.MenuItem;
import com.zbs.de.model.UserMaster;
import com.zbs.de.model.VendorMaster;
import com.zbs.de.model.VenueMaster;
import com.zbs.de.model.dto.DtoEventBudget;
import com.zbs.de.model.dto.DtoEventDecorCategorySelection;
import com.zbs.de.model.dto.DtoEventDecorExtrasSelection;
import com.zbs.de.model.dto.DtoEventDecorPropertySelection;
import com.zbs.de.model.dto.DtoEventDecorReferenceDocument;
import com.zbs.de.model.dto.DtoEventMaster;
import com.zbs.de.model.dto.DtoEventMasterAdminPortal;
import com.zbs.de.model.dto.DtoEventMasterSearch;
import com.zbs.de.model.dto.DtoEventMasterStats;
import com.zbs.de.model.dto.DtoEventMasterTableView;
import com.zbs.de.model.dto.DtoEventMenuFoodSelection;
import com.zbs.de.model.dto.DtoEventQuoteAndStatus;
import com.zbs.de.model.dto.DtoEventVenue;
import com.zbs.de.model.dto.DtoMenuFoodMaster;
import com.zbs.de.model.dto.DtoMenuItem;
import com.zbs.de.model.dto.DtoNotificationMaster;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.repository.RepositoryEventMaster;
import com.zbs.de.repository.RepositoryEventRunningOrder;
import com.zbs.de.service.ServiceCustomerMaster;
import com.zbs.de.service.ServiceDecorCategoryPropertyMaster;
import com.zbs.de.service.ServiceDecorCategoryPropertyValue;
import com.zbs.de.service.ServiceDecorExtrasMaster;
import com.zbs.de.service.ServiceDecorExtrasOption;
import com.zbs.de.service.ServiceEmailSender;
import com.zbs.de.service.ServiceEventBudget;
import com.zbs.de.service.ServiceEventDecorCategorySelection;
import com.zbs.de.service.ServiceEventDecorExtrasSelection;
import com.zbs.de.service.ServiceEventMaster;
import com.zbs.de.service.ServiceEventMenuFoodSelection;
import com.zbs.de.service.ServiceEventType;
import com.zbs.de.service.ServiceMenuFoodMaster;
import com.zbs.de.service.ServiceMenuItem;
import com.zbs.de.service.ServiceNotificationMaster;
import com.zbs.de.service.ServiceVendorMaster;
import com.zbs.de.service.ServiceVenueMaster;
import com.zbs.de.spec.SepecificationsEventMaster;
import com.zbs.de.util.UtilDateAndTime;
import com.zbs.de.util.UtilFileStorage;
import com.zbs.de.util.UtilRandomKey;

import jakarta.transaction.Transactional;

@Service("serviceEventMaster")
public class ServiceEventMasterImpl implements ServiceEventMaster {

	@Autowired
	private RepositoryEventMaster repositoryEventMaster;

	@Autowired
	private RepositoryEventRunningOrder repositoryEventRunningOrder;

	@Autowired
	private ServiceCustomerMaster serviceCustomerMaster;

	@Autowired
	private ServiceEventType serviceEventType;

	@Autowired
	private ServiceVenueMaster serviceVenueMaster;

	@Autowired
	private ServiceVendorMaster serviceVendorMaster;

	@Autowired
	private ServiceEventMenuFoodSelection serviceEventMenuFoodSelection;

	@Autowired
	private ServiceMenuFoodMaster serviceMenuFoodMaster;

	@Autowired
	private ServiceEventDecorCategorySelection serviceEventDecorCategorySelection;

	@Autowired
	private ServiceEventBudget serviceEventBudget;

	@Autowired
	private ServiceEventDecorExtrasSelection serviceEventDecorExtrasSelection;

	@Autowired
	private ServiceDecorExtrasMaster serviceDecorExtrasMaster;

	@Autowired
	private ServiceDecorExtrasOption serviceDecorExtrasOption;

	@Autowired
	private ServiceDecorCategoryPropertyMaster serviceDecorCategoryPropertyMaster;

	@Autowired
	private ServiceDecorCategoryPropertyValue serviceDecorCategoryPropertyValue;

	@Autowired
	private ServiceNotificationMaster serviceNotificationMaster;

	@Autowired
	private ServiceEmailSender serviceEmailSender;

	@Autowired
	private ServiceMenuItem serviceMenuItem;

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceEventMasterImpl.class);

	public DtoResult saveAndUpdate(DtoEventMaster dtoEventMaster) {
		// Validate required IDs
		DtoResult dtoResult = new DtoResult();

		if (dtoEventMaster.getSerCustId() == null || dtoEventMaster.getSerEventTypeId() == null) {
			LOGGER.debug("Customer ID and Event Type ID are required");
			dtoResult.setTxtMessage("Customer ID and Event Type ID are required");
			return dtoResult;
		}

		// Fetch existing if exists
		Optional<EventMaster> optionalExisting = repositoryEventMaster
				.findByCustomerAndEventType(dtoEventMaster.getSerCustId(), dtoEventMaster.getSerEventTypeId());

		List<DtoMenuFoodMaster> dtoMenuFoodMasterLst = serviceMenuFoodMaster.getAllData();
		if (UtilRandomKey.isNull(dtoMenuFoodMasterLst)) {
			dtoResult.setTxtMessage("No Food Item Is Present In DB");
			return dtoResult;
		}

		EventMaster entity;

		if (optionalExisting.isPresent()) {
			// Update existing
			entity = optionalExisting.get();

			// ****Check if Edit Allowed or Not***
			if (entity.getIsEditAllowed() != null && entity.getIsEditAllowed() == false) {
				LOGGER.debug("This Event Can't be deleted as Event Is Marked For Restrict Edit.");
				dtoResult.setTxtMessage(
						"You do not have permission to edit this event. Contact Diamond Event administration for changes!");
				return dtoResult;
			}

			// Manually update values (keep ID)
			entity.setTxtEventMasterName(dtoEventMaster.getTxtEventMasterName());
			entity.setDteEventDate(UtilDateAndTime.ddmmyyyyStringToDate(dtoEventMaster.getDteEventDate()));
			entity.setNumNumberOfGuests(dtoEventMaster.getNumNumberOfGuests());
			entity.setNumNumberOfTables(dtoEventMaster.getNumNumberOfTables());
			entity.setTxtBrideName(dtoEventMaster.getTxtBrideName());
			entity.setTxtBrideFirstName(dtoEventMaster.getTxtBrideFirstName());
			entity.setTxtBrideLastName(dtoEventMaster.getTxtBrideLastName());
			entity.setTxtGroomName(dtoEventMaster.getTxtGroomName());
			entity.setTxtGroomFirstName(dtoEventMaster.getTxtGroomFirstName());
			entity.setTxtGroomLastName(dtoEventMaster.getTxtGroomLastName());
			entity.setTxtBirthDayCelebrant(dtoEventMaster.getTxtBirthDayCelebrant());
			entity.setTxtAgeCategory(dtoEventMaster.getTxtAgeCategory());
			entity.setTxtChiefGuest(dtoEventMaster.getTxtChiefGuest());
			entity.setCreatedBy(ServiceCurrentUser.getCurrentUserId());
			entity.setCreatedDate(UtilDateAndTime.getCurrentDate());
			entity.setTxtCateringRemarks(dtoEventMaster.getTxtCateringRemarks());
			entity.setTxtDecoreRemarks(dtoEventMaster.getTxtDecoreRemarks());
			entity.setTxtEventExtrasRemarks(dtoEventMaster.getTxtEventExtrasRemarks());
			entity.setTxtEventRemarks(dtoEventMaster.getTxtEventRemarks());
			entity.setTxtExternalSupplierRemarks(dtoEventMaster.getTxtExternalSupplierRemarks());
			entity.setIsEditAllowed(dtoEventMaster.getIsEditAllowed());
			entity.setTxtContactPersonFirstName(dtoEventMaster.getTxtContactPersonFirstName());
			entity.setTxtContactPersonLastName(dtoEventMaster.getTxtContactPersonLastName());
			entity.setTxtContactPersonPhoneNo(dtoEventMaster.getTxtContactPersonPhoneNo());

			// if (UtilRandomKey.isNull(entity.getNumInfoFilledStatus())) {
			// entity.setNumInfoFilledStatus(0);
			// }

			// Set customer
			// ************
			if (UtilRandomKey.isNotNull(dtoEventMaster.getSerCustId())) {
				CustomerMaster customer = serviceCustomerMaster.getByPK(dtoEventMaster.getSerCustId());
				if (UtilRandomKey.isNull(customer)) {
					dtoResult.setTxtMessage("Customer Not Foound For Id" + dtoEventMaster.getSerCustId());
					return dtoResult;
				}
				// entity.setNumInfoFilledStatus(10);
				entity.setCustomerMaster(customer);
			}

			// Set event type
			// **************
			if (UtilRandomKey.isNotNull(dtoEventMaster.getSerEventTypeId())) {
				EventType eventType = serviceEventType.getByPK(dtoEventMaster.getSerEventTypeId());
				if (UtilRandomKey.isNull(eventType)) {
					dtoResult.setTxtMessage("Event Type Not Foound For Id" + dtoEventMaster.getSerEventTypeId());
					return dtoResult;
				}
				entity.setEventType(eventType);
			}

			// Set optional event running order
			// ********************************
			if (dtoEventMaster.getDtoEventRunningOrder() != null) {
				EventRunningOrder runningOrder = new EventRunningOrder();

				if (UtilRandomKey.isNotNull(entity.getEventRunningOrder())) {

					runningOrder = entity.getEventRunningOrder();
					runningOrder.setTxtGuestArrival(dtoEventMaster.getDtoEventRunningOrder().getTxtGuestArrival());
					runningOrder.setTxtBaratArrival(dtoEventMaster.getDtoEventRunningOrder().getTxtBaratArrival());
					runningOrder.setTxtBrideEntrance(dtoEventMaster.getDtoEventRunningOrder().getTxtBrideEntrance());
					runningOrder.setTxtNikah(dtoEventMaster.getDtoEventRunningOrder().getTxtNikah());
					runningOrder.setTxtMeal(dtoEventMaster.getDtoEventRunningOrder().getTxtMeal());
					runningOrder.setTxtEndOfNight(dtoEventMaster.getDtoEventRunningOrder().getTxtEndOfNight());

					runningOrder.setTxtBrideGuestArrival(
							dtoEventMaster.getDtoEventRunningOrder().getTxtBrideGuestArrival());
					runningOrder.setTxtGroomGuestArrival(
							dtoEventMaster.getDtoEventRunningOrder().getTxtGroomGuestArrival());
					runningOrder.setTxtGroomEntrance(dtoEventMaster.getDtoEventRunningOrder().getTxtGroomEntrance());
					runningOrder
							.setTxtCouplesEntrance(dtoEventMaster.getDtoEventRunningOrder().getTxtCouplesEntrance());
					runningOrder.setTxtDua(dtoEventMaster.getDtoEventRunningOrder().getTxtDua());
					runningOrder.setTxtDance(dtoEventMaster.getDtoEventRunningOrder().getTxtDance());
					runningOrder.setTxtCakeCutting(dtoEventMaster.getDtoEventRunningOrder().getTxtCakeCutting());
					runningOrder.setTxtRingExchange(dtoEventMaster.getDtoEventRunningOrder().getTxtRingExchange());
					runningOrder.setTxtRams(dtoEventMaster.getDtoEventRunningOrder().getTxtRams());
					runningOrder.setTxtSpeeches(dtoEventMaster.getDtoEventRunningOrder().getTxtSpeeches());
					runningOrder = repositoryEventRunningOrder.save(runningOrder);
				} else {
					runningOrder = MapperEventRunningOrder.toEntity(dtoEventMaster.getDtoEventRunningOrder());
					runningOrder = repositoryEventRunningOrder.save(runningOrder);
					// entity.setNumInfoFilledStatus(entity.getNumInfoFilledStatus() + 1);
				}
				entity.setEventRunningOrder(runningOrder);

				// entity.setNumInfoFilledStatus(30);

			}

			// Setting Venue Master
			// ********************

			// This is for which you only need to specify which venu is selected
			if (UtilRandomKey.isNotNull(dtoEventMaster.getSerVenueMasterId())) {
				VenueMaster venueMaster = serviceVenueMaster.getByPK(dtoEventMaster.getSerVenueMasterId());
				if (UtilRandomKey.isNull(venueMaster)) {
					dtoResult.setTxtMessage("Venue Not Found For Id: " + dtoEventMaster.getSerVenueMasterId());
					return dtoResult;
				}

				entity.setVenueMaster(venueMaster);
				// entity.setNumInfoFilledStatus(50);
			}

			// //This is For when you need to save which hall of the venu was selected
			// if (UtilRandomKey.isNotNull(dtoEventMaster.getDtoEventVenue())) {
			// if
			// (UtilRandomKey.isNotNull(dtoEventMaster.getDtoEventVenue().getSerVenueMasterDetailId()))
			// {
			// DtoResult res = serviceVenueMaster.getVenueDetailByVenueMasterDetailId(
			// dtoEventMaster.getDtoEventVenue().getSerVenueMasterDetailId());
			// if (res.getTxtMessage().equalsIgnoreCase("Success")) {
			// VenueMasterDetail venueMasterDetail = (VenueMasterDetail) res.getResult();
			// entity.setVenueMasterDetail(venueMasterDetail);
			// } else {
			// dtoResult.setTxtMessage("Venue Hall Is Not Active");
			// return dtoResult;
			// }
			// } else {
			// dtoResult.setTxtMessage("Venue Hall Is Not Selected");
			// return dtoResult;
			// }
			// entity.setNumInfoFilledStatus(50);
			//
			// }

			// Set Decor Item Selections
			// *************************
			if (UtilRandomKey.isNotNull(dtoEventMaster.getDtoEventDecorSelections())) {

				// Deleting Existing Selections
				serviceEventDecorCategorySelection.deleteByEventMasterId(entity.getSerEventMasterId());

				List<EventDecorCategorySelection> decorSelections = new ArrayList<>();

				for (DtoEventDecorCategorySelection dto : dtoEventMaster.getDtoEventDecorSelections()) {
					EventDecorCategorySelection decorSelection = MapperEventDecorCategorySelection.toEntity(dto);
					decorSelection.setEventMaster(entity);

					// Set property selections' back reference
					if (decorSelection.getSelectedProperties() != null) {
						for (EventDecorPropertySelection prop : decorSelection.getSelectedProperties()) {
							prop.setEventDecorCategorySelection(decorSelection);
						}
					}

					// Set reference image back reference
					if (decorSelection.getUserUploadedDocuments() != null) {
						for (EventDecorReferenceDocument img : decorSelection.getUserUploadedDocuments()) {
							img.setEventDecorCategorySelection(decorSelection);
						}
					}

					decorSelections.add(decorSelection);
				}

				entity.setDecorSelections(decorSelections);
				// entity.setNumInfoFilledStatus(70);
			}

			// // Set Food Menu Selection
			// // ***********************
			// if (UtilRandomKey.isNotNull(dtoEventMaster.getFoodSelections())) {
			// serviceEventMenuFoodSelection.deleteByEventMasterId(entity.getSerEventMasterId());
			// List<EventMenuFoodSelection> eventMenuFoodSelectionLst = new ArrayList<>();
			// for (DtoEventMenuFoodSelection dto : dtoEventMaster.getFoodSelections()) {
			// EventMenuFoodSelection eventMenuFoodSelection = new EventMenuFoodSelection();
			// eventMenuFoodSelection.setEventMaster(entity);
			// eventMenuFoodSelection.setBlnIsActive(true);
			// eventMenuFoodSelection.setBlnIsApproved(true);
			// eventMenuFoodSelection.setBlnIsDeleted(false);
			// if (UtilRandomKey.isNotNull(dto.getSerMenuFoodId())) {
			// DtoMenuFoodMaster dtoMenuFoodMaster = dtoMenuFoodMasterLst.stream()
			// .filter(food -> food.getSerMenuFoodId() != null
			// && food.getSerMenuFoodId().intValue() == dto.getSerMenuFoodId().intValue())
			// .findFirst().orElse(null);
			// if (UtilRandomKey.isNotNull(dtoMenuFoodMaster)) {
			// eventMenuFoodSelection.setTxtFoodType(getFoodType(dtoMenuFoodMaster));
			// MenuFoodMaster menuFoodMaster = new MenuFoodMaster();
			// menuFoodMaster.setSerMenuFoodId(dtoMenuFoodMaster.getSerMenuFoodId());
			// eventMenuFoodSelection.setMenuFoodMaster(menuFoodMaster);
			// } else {
			// dtoResult.setTxtMessage("Food Selection Item Does Not Have Food Menu With Id:
			// "
			// + dto.getSerMenuFoodId() + " In DB.");
			// return dtoResult;
			// }
			// } else {
			// dtoResult.setTxtMessage("Food Selection Item Does Not Have The Id OF Food
			// Menu");
			// return dtoResult;
			// }
			//
			// eventMenuFoodSelectionLst.add(eventMenuFoodSelection);
			// }
			//
			// String result =
			// serviceEventMenuFoodSelection.saveAll(eventMenuFoodSelectionLst);
			// if (!result.equalsIgnoreCase("Success")) {
			// dtoResult.setTxtMessage(result);
			// return dtoResult;
			// }
			//
			//// entity.setNumInfoFilledStatus(90);
			//
			// }

			// Set Vendor
			// **********
			if (UtilRandomKey.isNotNull(dtoEventMaster.getSerVendorId())) {
				VendorMaster vendorMaster = serviceVendorMaster.getByPK(dtoEventMaster.getSerVendorId());
				if (UtilRandomKey.isNotNull(vendorMaster)) {

					entity.setVendorMaster(vendorMaster);
				} else {
					dtoResult.setTxtMessage(
							"External Supplier Not Found Against Id: " + dtoEventMaster.getSerVendorId());
					return dtoResult;
				}
				// entity.setNumInfoFilledStatus(100);
			}

		} else {
			// Create new
			entity = MapperEventMaster.toEntity(dtoEventMaster);
			entity.setCreatedBy(ServiceCurrentUser.getCurrentUserId());
			// if (UtilRandomKey.isNull(entity.getNumInfoFilledStatus())) {
			// entity.setNumInfoFilledStatus(0);
			// }

			// Set customer
			// ************
			if (UtilRandomKey.isNotNull(dtoEventMaster.getSerCustId())) {
				CustomerMaster customer = serviceCustomerMaster.getByPK(dtoEventMaster.getSerCustId());
				if (UtilRandomKey.isNull(customer)) {
					dtoResult.setTxtMessage("Customer Not Foound For Id" + dtoEventMaster.getSerCustId());
					return dtoResult;
				}
				entity.setCustomerMaster(customer);
				// entity.setNumInfoFilledStatus(10);

			}

			// Set event type
			// **************
			if (UtilRandomKey.isNotNull(dtoEventMaster.getSerEventTypeId())) {
				EventType eventType = serviceEventType.getByPK(dtoEventMaster.getSerEventTypeId());
				if (UtilRandomKey.isNull(eventType)) {
					dtoResult.setTxtMessage("Event Type Not Foound For Id" + dtoEventMaster.getSerEventTypeId());
					return dtoResult;
				}
				entity.setEventType(eventType);
			}

			// Set optional event running order
			// ********************************
			if (dtoEventMaster.getDtoEventRunningOrder() != null) {
				EventRunningOrder runningOrder = new EventRunningOrder();
				runningOrder = MapperEventRunningOrder.toEntity(dtoEventMaster.getDtoEventRunningOrder());
				runningOrder = repositoryEventRunningOrder.save(runningOrder);
				entity.setEventRunningOrder(runningOrder);
				// entity.setNumInfoFilledStatus(30);
			}

			// Set Venue Master
			// ****************

			// This is for which you only need to specify which venu is selected
			if (UtilRandomKey.isNotNull(dtoEventMaster.getSerVenueMasterId())) {
				VenueMaster venueMaster = serviceVenueMaster.getByPK(dtoEventMaster.getSerVenueMasterId());
				if (UtilRandomKey.isNull(venueMaster)) {
					dtoResult.setTxtMessage("Venue Not Found For Id: " + dtoEventMaster.getSerVenueMasterId());
					return dtoResult;
				}
				entity.setVenueMaster(venueMaster);
				// entity.setNumInfoFilledStatus(50);
			}

			// //This is For when you need to save which hall of the venu was selected
			// if (UtilRandomKey.isNotNull(dtoEventMaster.getDtoEventVenue())) {
			// if
			// (UtilRandomKey.isNotNull(dtoEventMaster.getDtoEventVenue().getSerVenueMasterDetailId()))
			// {
			// DtoResult res = serviceVenueMaster.getVenueDetailByVenueMasterDetailId(
			// dtoEventMaster.getDtoEventVenue().getSerVenueMasterDetailId());
			// if (res.getTxtMessage().equalsIgnoreCase("Success")) {
			// VenueMasterDetail venueMasterDetail = (VenueMasterDetail) res.getResult();
			// entity.setVenueMasterDetail(venueMasterDetail);
			// } else {
			// dtoResult.setTxtMessage("Venue Hall Is Not Active");
			// return dtoResult;
			// }
			// } else {
			// dtoResult.setTxtMessage("Venue Hall Is Not Selected");
			// return dtoResult;
			// }
			// entity.setNumInfoFilledStatus(50);
			//
			// }

			// Set Decore Item Selections
			// **************************
			if (UtilRandomKey.isNotNull(dtoEventMaster.getDtoEventDecorSelections())) {
				List<EventDecorCategorySelection> decorSelections = new ArrayList<>();

				for (DtoEventDecorCategorySelection dto : dtoEventMaster.getDtoEventDecorSelections()) {
					EventDecorCategorySelection decorSelection = MapperEventDecorCategorySelection.toEntity(dto);
					decorSelection.setEventMaster(entity);

					if (decorSelection.getSelectedProperties() != null) {
						decorSelection.getSelectedProperties()
								.forEach(p -> p.setEventDecorCategorySelection(decorSelection));
					}

					if (decorSelection.getUserUploadedDocuments() != null) {
						decorSelection.getUserUploadedDocuments()
								.forEach(img -> img.setEventDecorCategorySelection(decorSelection));
					}

					decorSelections.add(decorSelection);
				}

				entity.setDecorSelections(decorSelections);
				// entity.setNumInfoFilledStatus(70);
			}

			// // Set Food Menu Selection
			// // ***********************
			// if (UtilRandomKey.isNotNull(dtoEventMaster.getFoodSelections())) {
			// List<EventMenuFoodSelection> eventMenuFoodSelectionLst = new ArrayList<>();
			// for (DtoEventMenuFoodSelection dto : dtoEventMaster.getFoodSelections()) {
			// EventMenuFoodSelection eventMenuFoodSelection = new EventMenuFoodSelection();
			// eventMenuFoodSelection.setEventMaster(entity);
			// eventMenuFoodSelection.setBlnIsActive(true);
			// eventMenuFoodSelection.setBlnIsApproved(true);
			// eventMenuFoodSelection.setBlnIsDeleted(false);
			// if (UtilRandomKey.isNotNull(dto.getSerMenuFoodId())) {
			// DtoMenuFoodMaster dtoMenuFoodMaster = dtoMenuFoodMasterLst.stream()
			// .filter(food -> food.getSerMenuFoodId() != null
			// && food.getSerMenuFoodId().intValue() == dto.getSerMenuFoodId().intValue())
			// .findFirst().orElse(null);
			// if (UtilRandomKey.isNotNull(dtoMenuFoodMaster)) {
			// eventMenuFoodSelection.setTxtFoodType(getFoodType(dtoMenuFoodMaster));
			// MenuFoodMaster menuFoodMaster = new MenuFoodMaster();
			// menuFoodMaster.setSerMenuFoodId(dtoMenuFoodMaster.getSerMenuFoodId());
			// eventMenuFoodSelection.setMenuFoodMaster(menuFoodMaster);
			// } else {
			// dtoResult.setTxtMessage("Food Selection Item Does Not Have Food Menu With Id:
			// "
			// + dto.getSerMenuFoodId() + " In DB.");
			// return dtoResult;
			// }
			// } else {
			// dtoResult.setTxtMessage("Food Selection Item Does Not Have The Id OF Food
			// Menu");
			// return dtoResult;
			// }
			//
			// eventMenuFoodSelectionLst.add(eventMenuFoodSelection);
			// }
			//
			// String result =
			// serviceEventMenuFoodSelection.saveAll(eventMenuFoodSelectionLst);
			// if (!result.equalsIgnoreCase("Success")) {
			// dtoResult.setTxtMessage(result);
			// return dtoResult;
			// }
			//// entity.setNumInfoFilledStatus(90);
			// }

			// Set Vendor
			// **********
			if (UtilRandomKey.isNotNull(dtoEventMaster.getSerVendorId())) {
				VendorMaster vendorMaster = serviceVendorMaster.getByPK(dtoEventMaster.getSerVendorId());
				if (UtilRandomKey.isNotNull(vendorMaster)) {
					entity.setVendorMaster(vendorMaster);
					// entity.setNumInfoFilledStatus(entity.getNumInfoFilledStatus() + 1);
				} else {
					dtoResult.setTxtMessage(
							"External Supplier Not Found Against Id: " + dtoEventMaster.getSerVendorId());
					return dtoResult;
				}
			}
			// entity.setNumInfoFilledStatus(100);

			// Generate event master code
			String code = generateNextEventMasterCode();
			entity.setTxtEventMasterCode(code);
		}

		entity.setNumInfoFilledStatus(getEventCompletionPercentage(entity));
		entity = repositoryEventMaster.save(entity);

		dtoResult.setTxtMessage("Success");
		dtoResult.setResult(entity);
		return dtoResult;
	}

	// public String generateNextEventMasterCode() {
	// String maxCode = repositoryEventMaster.findMaxEventCode();
	//
	// int nextNumber = 1;
	//
	// if (maxCode != null && maxCode.startsWith("EVT-")) {
	// try {
	// String numberPart = maxCode.substring(4);
	// nextNumber = Integer.parseInt(numberPart) + 1;
	// } catch (NumberFormatException e) {
	// nextNumber = 1;
	// }
	// }
	//
	// return String.format("EVT-%03d", nextNumber);
	// }

	public String generateNextEventMasterCode() {
		// Get current year (last 2 digits)
		int year = LocalDate.now().getYear() % 100;

		// Query the latest event code for this year (e.g. DE-25-1050)
		String maxCode = repositoryEventMaster.findMaxEventCodeForYear(year);

		int nextSerial = 1001; // default starting serial for the year

		if (maxCode != null && maxCode.startsWith("DE-" + year + "-")) {
			try {
				// Extract serial part
				String serialPart = maxCode.substring(maxCode.lastIndexOf("-") + 1);
				int lastSerial = Integer.parseInt(serialPart);
				nextSerial = lastSerial + 1;
			} catch (NumberFormatException e) {
				// fallback to default
				nextSerial = 1001;
			}
		}

		return String.format("DE-%02d-%04d", year, nextSerial);
	}

	@Override
	public DtoResult getByEventTypeIdAndCustId(DtoSearch dtoSearch) {
		DtoResult dtoResult = new DtoResult();

		try {
			if (dtoSearch == null || dtoSearch.getId() == null || dtoSearch.getId1() == null) {
				dtoResult.setTxtMessage("Event Type ID and Customer ID are required");
				return dtoResult;
			}

			Optional<EventMaster> optionalEvent = repositoryEventMaster.findByCustomerAndEventType(dtoSearch.getId1(),
					dtoSearch.getId());

			if (optionalEvent.isPresent()) {
				DtoEventMaster dto = MapperEventMaster.toDto(optionalEvent.get());

				// Fetching Event Venue Detail
				// ***********************************
				if (UtilRandomKey.isNotNull(optionalEvent.get().getVenueMasterDetail())) {
					DtoResult res = serviceVenueMaster.getVenueByVenueMasterDetailId(
							optionalEvent.get().getVenueMasterDetail().getSerVenueMasterDetailId());
					if (UtilRandomKey.isNotNull(res) && res.getTxtMessage().equalsIgnoreCase("Success")) {
						VenueMaster venueMaster = (VenueMaster) res.getResult();
						DtoEventVenue dtoEventVenue = new DtoEventVenue();
						dtoEventVenue.setSerVenueMasterId(venueMaster.getSerVenueMasterId());
						dtoEventVenue.setTxtVenueCode(venueMaster.getTxtVenueCode());
						dtoEventVenue.setTxtVenueName(venueMaster.getTxtVenueName());
						dtoEventVenue.setSerVenueMasterDetailId(
								optionalEvent.get().getVenueMasterDetail().getSerVenueMasterDetailId());
						dtoEventVenue.setTxtHallCode(optionalEvent.get().getVenueMasterDetail().getTxtHallCode());
						dtoEventVenue.setTxtHallName(optionalEvent.get().getVenueMasterDetail().getTxtHallName());
					}
				}

				// Fetching Decor
				// ***********************************

				// Fetching Event Menu Food Selection
				// ***********************************

				List<EventMenuFoodSelection> eventMenuFoodSelections = serviceEventMenuFoodSelection
						.getByEventMasterId(dto.getSerEventMasterId());
//				List<DtoEventMenuFoodSelection> dtoEventMenuFoodSelections = new ArrayList<>();
//				if (UtilRandomKey.isNotNull(eventMenuFoodSelections)) {
//					for (EventMenuFoodSelection entity : eventMenuFoodSelections) {
//						DtoEventMenuFoodSelection dtoEventMenuFoodSelection = MapperEventMenuFoodSelection
//								.toDto(entity);
//						dtoEventMenuFoodSelections.add(dtoEventMenuFoodSelection);
//					}
//				}
//				dto.setFoodSelections(dtoEventMenuFoodSelections);
				
				List<DtoMenuFoodMaster> foodSelections = new ArrayList<>();
				if (UtilRandomKey.isNotNull(eventMenuFoodSelections)) {
					for (EventMenuFoodSelection entity : eventMenuFoodSelections) {
						if (entity.getMenuItem() != null) {
							DtoMenuFoodMaster dtoMenuFoodMaster = new DtoMenuFoodMaster();
							MenuItem menuItem = entity.getMenuItem();
							dtoMenuFoodMaster.setBlnIsActive(menuItem.getBlnIsActive());
							dtoMenuFoodMaster.setSerMenuItemId(menuItem.getSerMenuItemId());
							dtoMenuFoodMaster.setTxtName(menuItem.getTxtName());
							dtoMenuFoodMaster.setTxtCode(menuItem.getTxtCode());
							dtoMenuFoodMaster.setTxtDescription(menuItem.getTxtDescription());
							dtoMenuFoodMaster.setNumPrice(entity.getNumPrice());
							foodSelections.add(dtoMenuFoodMaster);
						}
					}
				}
				dto.setFoodSelections(foodSelections);

				dtoResult.setResult(dto);
				dtoResult.setTxtMessage("Success");
			} else {
				dtoResult.setTxtMessage("No record found");
			}
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			dtoResult.setTxtMessage("Error occurred: " + e.getMessage());
		}

		return dtoResult;
	}

	@Override
	public DtoResult getByCustId(DtoSearch dtoSearch) {
		DtoResult dtoResult = new DtoResult();

		try {
			if (dtoSearch == null || dtoSearch.getId() == null) {
				dtoResult.setTxtMessage("Customer ID Is Required");
				return dtoResult;
			}

			List<EventMaster> eventMasterLst = repositoryEventMaster
					.findActiveEventMasterByCustomerId(dtoSearch.getId());
			List<DtoEventMaster> dtoEventMasterLst = new ArrayList<>();
			if (eventMasterLst != null && !eventMasterLst.isEmpty()) {

				for (EventMaster eventMaster : eventMasterLst) {
					DtoEventMaster dto = MapperEventMaster.toDto(eventMaster);

					// Fetching Event Venue Detail
					// ***********************************
					if (UtilRandomKey.isNotNull(eventMaster.getVenueMasterDetail())) {
						DtoResult res = serviceVenueMaster.getVenueByVenueMasterDetailId(
								eventMaster.getVenueMasterDetail().getSerVenueMasterDetailId());
						if (UtilRandomKey.isNotNull(res) && res.getTxtMessage().equalsIgnoreCase("Success")) {
							VenueMaster venueMaster = (VenueMaster) res.getResult();
							DtoEventVenue dtoEventVenue = new DtoEventVenue();
							dtoEventVenue.setSerVenueMasterId(venueMaster.getSerVenueMasterId());
							dtoEventVenue.setTxtVenueCode(venueMaster.getTxtVenueCode());
							dtoEventVenue.setTxtVenueName(venueMaster.getTxtVenueName());
							dtoEventVenue.setSerVenueMasterDetailId(
									eventMaster.getVenueMasterDetail().getSerVenueMasterDetailId());
							dtoEventVenue.setTxtHallCode(eventMaster.getVenueMasterDetail().getTxtHallCode());
							dtoEventVenue.setTxtHallName(eventMaster.getVenueMasterDetail().getTxtHallName());
						}
					}

					// Fetching Decor
					// ***********************************
					List<DtoEventDecorCategorySelection> eventDecorCategorySelections = serviceEventDecorCategorySelection
							.getSelectionsWithChosenValues(dto.getSerEventMasterId());
					dto.setDtoEventDecorSelections(eventDecorCategorySelections);

					// Fetching Event Menu Food Selection
					// ***********************************

					List<EventMenuFoodSelection> eventMenuFoodSelections = serviceEventMenuFoodSelection
							.getByEventMasterId(dto.getSerEventMasterId());
//					List<DtoEventMenuFoodSelection> dtoEventMenuFoodSelections = new ArrayList<>();
//					if (UtilRandomKey.isNotNull(eventMenuFoodSelections)) {
//						for (EventMenuFoodSelection entity : eventMenuFoodSelections) {
//							DtoEventMenuFoodSelection dtoEventMenuFoodSelection = MapperEventMenuFoodSelection
//									.toDto(entity);
//							dtoEventMenuFoodSelections.add(dtoEventMenuFoodSelection);
//						}
//					}
//					dto.setFoodSelections(dtoEventMenuFoodSelections);
					
					List<DtoMenuFoodMaster> foodSelections = new ArrayList<>();
					if (UtilRandomKey.isNotNull(eventMenuFoodSelections)) {
						for (EventMenuFoodSelection entity : eventMenuFoodSelections) {
							if (entity.getMenuItem() != null) {
								DtoMenuFoodMaster dtoMenuFoodMaster = new DtoMenuFoodMaster();
								MenuItem menuItem = entity.getMenuItem();
								dtoMenuFoodMaster.setBlnIsActive(menuItem.getBlnIsActive());
								dtoMenuFoodMaster.setSerMenuItemId(menuItem.getSerMenuItemId());
								dtoMenuFoodMaster.setTxtName(menuItem.getTxtName());
								dtoMenuFoodMaster.setTxtCode(menuItem.getTxtCode());
								dtoMenuFoodMaster.setTxtDescription(menuItem.getTxtDescription());
								dtoMenuFoodMaster.setNumPrice(entity.getNumPrice());
								foodSelections.add(dtoMenuFoodMaster);
							}
						}
					}
					dto.setFoodSelections(foodSelections);

					// Fetching Event Extras Selection
					// ***********************************

					List<EventDecorExtrasSelection> eventDecorExtrasSelection = serviceEventDecorExtrasSelection
							.getByEventMasterId(dto.getSerEventMasterId());
					List<DtoEventDecorExtrasSelection> dtoEventDecorExtrasSelections = new ArrayList<>();
					if (UtilRandomKey.isNotNull(eventDecorExtrasSelection)) {
						for (EventDecorExtrasSelection entity : eventDecorExtrasSelection) {
							DtoEventDecorExtrasSelection dtoEventDecorExtrasSelection = new DtoEventDecorExtrasSelection();
							dtoEventDecorExtrasSelection.setSerExtrasSelectionId(entity.getSerExtrasSelectionId());
							dtoEventDecorExtrasSelection.setTxtDynamicProperty1(entity.getTxtDynamicProperty1());
							dtoEventDecorExtrasSelection.setTxtDynamicProperty2(entity.getTxtDynamicProperty2());
							if (entity.getDecorExtrasMaster() != null) {
								dtoEventDecorExtrasSelection
										.setSerExtrasId(entity.getDecorExtrasMaster().getSerExtrasId());
								dtoEventDecorExtrasSelection
										.setTxtExtrasCode(entity.getDecorExtrasMaster().getTxtExtrasCode());
								dtoEventDecorExtrasSelection
										.setTxtExtrasName(entity.getDecorExtrasMaster().getTxtExtrasName());
							}

							if (entity.getDecorExtrasOption() != null) {
								dtoEventDecorExtrasSelection
										.setSerExtraOptionId(entity.getDecorExtrasOption().getSerExtraOptionId());
								dtoEventDecorExtrasSelection
										.setTxtOptionCode(entity.getDecorExtrasOption().getTxtOptionCode());
								dtoEventDecorExtrasSelection
										.setTxtOptionName(entity.getDecorExtrasOption().getTxtOptionName());
							}

							dtoEventDecorExtrasSelections.add(dtoEventDecorExtrasSelection);
						}
					}
					dto.setExtrasSelections(dtoEventDecorExtrasSelections);

					dtoEventMasterLst.add(dto);
				}

				dtoResult.setResulList(new ArrayList<>(dtoEventMasterLst));
				dtoResult.setTxtMessage("Success");
			} else {
				dtoResult.setTxtMessage("No record found");
			}
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			dtoResult.setTxtMessage("Error occurred: " + e.getMessage());
		}

		return dtoResult;
	}

	@Override
	public DtoResult getAllEvents() {
		DtoResult dtoResult = new DtoResult();

		try {

			List<EventMaster> events = repositoryEventMaster.findByBlnIsDeletedFalse();
			if (UtilRandomKey.isNotNull(events)) {

				List<DtoEventMaster> dtoEventMasterLst = new ArrayList<>();
				for (EventMaster event : events) {

					DtoEventMaster dto = MapperEventMaster.toDto(event);

					// Fetching Event Venue Detail
					// ***********************************
					if (UtilRandomKey.isNotNull(event.getVenueMasterDetail())) {
						DtoResult res = serviceVenueMaster.getVenueByVenueMasterDetailId(
								event.getVenueMasterDetail().getSerVenueMasterDetailId());
						if (UtilRandomKey.isNotNull(res) && res.getTxtMessage().equalsIgnoreCase("Success")) {
							VenueMaster venueMaster = (VenueMaster) res.getResult();
							DtoEventVenue dtoEventVenue = new DtoEventVenue();
							dtoEventVenue.setSerVenueMasterId(venueMaster.getSerVenueMasterId());
							dtoEventVenue.setTxtVenueCode(venueMaster.getTxtVenueCode());
							dtoEventVenue.setTxtVenueName(venueMaster.getTxtVenueName());
							dtoEventVenue.setSerVenueMasterDetailId(
									event.getVenueMasterDetail().getSerVenueMasterDetailId());
							dtoEventVenue.setTxtHallCode(event.getVenueMasterDetail().getTxtHallCode());
							dtoEventVenue.setTxtHallName(event.getVenueMasterDetail().getTxtHallName());
						}
					}

					// Fetching Decor
					// ***********************************
					List<DtoEventDecorCategorySelection> eventDecorCategorySelections = serviceEventDecorCategorySelection
							.getSelectionsWithChosenValues(dto.getSerEventMasterId());
					dto.setDtoEventDecorSelections(eventDecorCategorySelections);

					// Fetching Event Menu Food Selection
					// ***********************************

					List<EventMenuFoodSelection> eventMenuFoodSelections = serviceEventMenuFoodSelection
							.getByEventMasterId(dto.getSerEventMasterId());
//					List<DtoEventMenuFoodSelection> dtoEventMenuFoodSelections = new ArrayList<>();
//					if (UtilRandomKey.isNotNull(eventMenuFoodSelections)) {
//						for (EventMenuFoodSelection entity : eventMenuFoodSelections) {
//							DtoEventMenuFoodSelection dtoEventMenuFoodSelection = MapperEventMenuFoodSelection
//									.toDto(entity);
//							dtoEventMenuFoodSelections.add(dtoEventMenuFoodSelection);
//						}
//					}
					
					List<DtoMenuFoodMaster> foodSelections = new ArrayList<>();
					if (UtilRandomKey.isNotNull(eventMenuFoodSelections)) {
						for (EventMenuFoodSelection entity : eventMenuFoodSelections) {
							if (entity.getMenuItem() != null) {
								DtoMenuFoodMaster dtoMenuFoodMaster = new DtoMenuFoodMaster();
								MenuItem menuItem = entity.getMenuItem();
								dtoMenuFoodMaster.setBlnIsActive(menuItem.getBlnIsActive());
								dtoMenuFoodMaster.setSerMenuItemId(menuItem.getSerMenuItemId());
								dtoMenuFoodMaster.setTxtName(menuItem.getTxtName());
								dtoMenuFoodMaster.setTxtCode(menuItem.getTxtCode());
								dtoMenuFoodMaster.setTxtDescription(menuItem.getTxtDescription());
								dtoMenuFoodMaster.setNumPrice(entity.getNumPrice());
								foodSelections.add(dtoMenuFoodMaster);
							}
						}
					}
					dto.setFoodSelections(foodSelections);

					// Fetching Event Extras Selection
					// ***********************************

					List<EventDecorExtrasSelection> eventDecorExtrasSelection = serviceEventDecorExtrasSelection
							.getByEventMasterId(dto.getSerEventMasterId());
					List<DtoEventDecorExtrasSelection> dtoEventDecorExtrasSelections = new ArrayList<>();
					if (UtilRandomKey.isNotNull(eventDecorExtrasSelection)) {
						for (EventDecorExtrasSelection entity : eventDecorExtrasSelection) {
							DtoEventDecorExtrasSelection dtoEventDecorExtrasSelection = new DtoEventDecorExtrasSelection();
							dtoEventDecorExtrasSelection.setSerExtrasSelectionId(entity.getSerExtrasSelectionId());
							dtoEventDecorExtrasSelection.setTxtDynamicProperty1(entity.getTxtDynamicProperty1());
							dtoEventDecorExtrasSelection.setTxtDynamicProperty2(entity.getTxtDynamicProperty2());
							if (entity.getDecorExtrasMaster() != null) {
								dtoEventDecorExtrasSelection
										.setSerExtrasId(entity.getDecorExtrasMaster().getSerExtrasId());
								dtoEventDecorExtrasSelection
										.setTxtExtrasCode(entity.getDecorExtrasMaster().getTxtExtrasCode());
								dtoEventDecorExtrasSelection
										.setTxtExtrasName(entity.getDecorExtrasMaster().getTxtExtrasName());
							}

							if (entity.getDecorExtrasOption() != null) {
								dtoEventDecorExtrasSelection
										.setSerExtraOptionId(entity.getDecorExtrasOption().getSerExtraOptionId());
								dtoEventDecorExtrasSelection
										.setTxtOptionCode(entity.getDecorExtrasOption().getTxtOptionCode());
								dtoEventDecorExtrasSelection
										.setTxtOptionName(entity.getDecorExtrasOption().getTxtOptionName());
							}

							dtoEventDecorExtrasSelections.add(dtoEventDecorExtrasSelection);
						}
					}
					dto.setExtrasSelections(dtoEventDecorExtrasSelections);

					dtoEventMasterLst.add(dto);

				}

				dtoResult.setTxtMessage("Success");
				dtoResult.setResulList(new ArrayList<>(dtoEventMasterLst));
				return dtoResult;

			} else {
				dtoResult.setTxtMessage("No Data Found In System");
				return dtoResult;
			}

		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			dtoResult.setTxtMessage("Error occurred: " + e.getMessage());
			return dtoResult;
		}

	}

	@Override
	public DtoResult getAllEventsTableView() {
		DtoResult dtoResult = new DtoResult();
		try {
			List<DtoEventMasterTableView> events = repositoryEventMaster.getAllEventMastersTableView();
			if (UtilRandomKey.isNotNull(events)) {
				dtoResult.setTxtMessage("Success");
				dtoResult.setResulList(new ArrayList<>(events));
				return dtoResult;
			} else {
				dtoResult.setTxtMessage("No Data Found In System");
				return dtoResult;
			}
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			dtoResult.setTxtMessage("Error occurred: " + e.getMessage());
			return dtoResult;
		}

	}

	private String getFoodType(DtoMenuFoodMaster dtoMenuFoodMaster) {
		if (UtilRandomKey.isNotNull(dtoMenuFoodMaster.getBlnIsDrink()) && dtoMenuFoodMaster.getBlnIsDrink()) {
			return "Drink";
		} else if (UtilRandomKey.isNotNull(dtoMenuFoodMaster.getBlnIsDessert())
				&& dtoMenuFoodMaster.getBlnIsDessert()) {
			return "Desert";
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

	private String getFoodType(MenuFoodMaster dtoMenuFoodMaster) {
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

	@Override
	public List<DtoEventMasterStats> getEventTypeStats() {
		return repositoryEventMaster.countEventsGroupedByEventType();
	}

	@Override
	@Transactional
	public DtoResult saveAndUpdateWithDocs(DtoEventMaster dtoEventMaster, List<MultipartFile> files)
			throws IOException {
		// Validate required IDs
		DtoResult dtoResult = new DtoResult();
		try {
			if (dtoEventMaster.getSerCustId() == null || dtoEventMaster.getSerEventTypeId() == null) {
				LOGGER.debug("Customer ID and Event Type ID are required");
				dtoResult.setTxtMessage("Customer ID and Event Type ID are required");
				return dtoResult;
			}

			Boolean blnIsNewEvent = false;
			// Fetch existing if exists
			Optional<EventMaster> optionalExisting = null;
			if (dtoEventMaster.getSerEventMasterId() != null) {
				optionalExisting = repositoryEventMaster
						.findByIdAndBlnIsDeletedFalse(dtoEventMaster.getSerEventMasterId());
			}

//			List<MenuFoodMaster> dtoMenuFoodMasterLst = serviceMenuFoodMaster.getAllDataEntity();
//			if (UtilRandomKey.isNull(dtoMenuFoodMasterLst)) {
//				dtoResult.setTxtMessage("No Food Item Is Present In DB");
//				return dtoResult;
//			}
			
			List<MenuItem> menuItems = serviceMenuItem.getAllMenuItems();
			if (UtilRandomKey.isNull(menuItems)) {
				dtoResult.setTxtMessage("No Food Item Is Present In DB");
				return dtoResult;
			}


			EventBudget eventBudget = null;
			List<DecorCategoryPropertyMaster> decorCategoryPropertyMasterLst = serviceDecorCategoryPropertyMaster
					.getAllPropertiesMaster();
			List<DecorCategoryPropertyValue> decorCategoryPropertyValueLst = serviceDecorCategoryPropertyValue
					.getAllPropertyValueMaster();
			List<DecorExtrasMaster> decorExtrasMasterLst = serviceDecorExtrasMaster.getAllDecorExtrasMaster();
			List<DecorExtrasOption> decorExtrasOptions = null;
			if (decorExtrasMasterLst != null && !decorExtrasMasterLst.isEmpty()) {
				decorExtrasOptions = new ArrayList<>();
				for (DecorExtrasMaster extrasMaster : decorExtrasMasterLst) {
					if (extrasMaster.getDecorExtrasOptions() != null) {
						decorExtrasOptions.addAll(extrasMaster.getDecorExtrasOptions());
					}
				}
			}

			EventMaster entity;
			Map<String, MultipartFile> fileMap = null;
			if (UtilRandomKey.isNotNull(files)) {
				fileMap = files.stream().collect(Collectors.toMap(MultipartFile::getOriginalFilename, f -> f));
			}

			if (optionalExisting != null && optionalExisting.isPresent()) {
				// Update existing
				entity = optionalExisting.get();

				// ****Check if Edit Allowed or Not***
				if (entity.getIsEditAllowed() != null && entity.getIsEditAllowed() == false) {
					LOGGER.debug("This Event Can't be deleted as Event Is Marked For Restrict Edit.");
					dtoResult.setTxtMessage(
							"You do not have permission to edit this event. Contact Diamond Event administration for changes!");
					return dtoResult;
				}

				// Manually update values (keep ID)
				entity.setTxtEventMasterName(dtoEventMaster.getTxtEventMasterName());
				entity.setDteEventDate(UtilDateAndTime.ddMMyyyyDashedStringToDate(dtoEventMaster.getDteEventDate()));
				entity.setNumNumberOfGuests(dtoEventMaster.getNumNumberOfGuests());
				entity.setNumNumberOfTables(dtoEventMaster.getNumNumberOfTables());
				entity.setTxtBrideName(dtoEventMaster.getTxtBrideName());
				entity.setTxtBrideFirstName(dtoEventMaster.getTxtBrideFirstName());
				entity.setTxtBrideLastName(dtoEventMaster.getTxtBrideLastName());
				entity.setTxtGroomName(dtoEventMaster.getTxtGroomName());
				entity.setTxtGroomFirstName(dtoEventMaster.getTxtGroomFirstName());
				entity.setTxtGroomLastName(dtoEventMaster.getTxtGroomLastName());
				entity.setTxtBirthDayCelebrant(dtoEventMaster.getTxtBirthDayCelebrant());
				entity.setTxtAgeCategory(dtoEventMaster.getTxtAgeCategory());
				entity.setTxtChiefGuest(dtoEventMaster.getTxtChiefGuest());
				entity.setUpdatedBy(ServiceCurrentUser.getCurrentUserId());
				entity.setTxtCateringRemarks(dtoEventMaster.getTxtCateringRemarks());
				entity.setTxtDecoreRemarks(dtoEventMaster.getTxtDecoreRemarks());
				entity.setTxtEventExtrasRemarks(dtoEventMaster.getTxtEventExtrasRemarks());
				entity.setTxtEventRemarks(dtoEventMaster.getTxtEventRemarks());
				entity.setTxtExternalSupplierRemarks(dtoEventMaster.getTxtExternalSupplierRemarks());
				entity.setTxtVenueRemarks(dtoEventMaster.getTxtVenueRemarks());
				entity.setNumFormState(dtoEventMaster.getNumFormState());
				entity.setIsEditAllowed(dtoEventMaster.getIsEditAllowed());
				entity.setTxtContactPersonFirstName(dtoEventMaster.getTxtContactPersonFirstName());
				entity.setTxtContactPersonLastName(dtoEventMaster.getTxtContactPersonLastName());
				entity.setTxtContactPersonPhoneNo(dtoEventMaster.getTxtContactPersonPhoneNo());
				// if (UtilRandomKey.isNull(entity.getNumInfoFilledStatus())) {
				// entity.setNumInfoFilledStatus(0);
				// }

				// Set customer
				// ************
				if (UtilRandomKey.isNotNull(dtoEventMaster.getSerCustId())) {
					CustomerMaster customer = serviceCustomerMaster.getByPK(dtoEventMaster.getSerCustId());
					if (UtilRandomKey.isNull(customer)) {
						dtoResult.setTxtMessage("Customer Not Foound For Id" + dtoEventMaster.getSerCustId());
						return dtoResult;
					}
					// entity.setNumInfoFilledStatus(10);
					entity.setCustomerMaster(customer);
				}

				// Set event type
				// **************
				if (UtilRandomKey.isNotNull(dtoEventMaster.getSerEventTypeId())) {
					EventType eventType = serviceEventType.getByPK(dtoEventMaster.getSerEventTypeId());
					if (UtilRandomKey.isNull(eventType)) {
						dtoResult.setTxtMessage("Event Type Not Foound For Id" + dtoEventMaster.getSerEventTypeId());
						return dtoResult;
					}
					entity.setEventType(eventType);
				}

				// Set optional event running order
				// ********************************
				if (dtoEventMaster.getDtoEventRunningOrder() != null) {
					EventRunningOrder runningOrder = new EventRunningOrder();

					if (UtilRandomKey.isNotNull(entity.getEventRunningOrder())) {

						runningOrder = entity.getEventRunningOrder();
						runningOrder.setTxtGuestArrival(dtoEventMaster.getDtoEventRunningOrder().getTxtGuestArrival());
						runningOrder.setTxtBaratArrival(dtoEventMaster.getDtoEventRunningOrder().getTxtBaratArrival());
						runningOrder
								.setTxtBrideEntrance(dtoEventMaster.getDtoEventRunningOrder().getTxtBrideEntrance());
						runningOrder.setTxtNikah(dtoEventMaster.getDtoEventRunningOrder().getTxtNikah());
						runningOrder.setTxtMeal(dtoEventMaster.getDtoEventRunningOrder().getTxtMeal());
						runningOrder.setTxtEndOfNight(dtoEventMaster.getDtoEventRunningOrder().getTxtEndOfNight());
						runningOrder.setTxtBrideGuestArrival(
								dtoEventMaster.getDtoEventRunningOrder().getTxtBrideGuestArrival());
						runningOrder.setTxtGroomGuestArrival(
								dtoEventMaster.getDtoEventRunningOrder().getTxtGroomGuestArrival());
						runningOrder
								.setTxtGroomEntrance(dtoEventMaster.getDtoEventRunningOrder().getTxtGroomEntrance());
						runningOrder.setTxtCouplesEntrance(
								dtoEventMaster.getDtoEventRunningOrder().getTxtCouplesEntrance());
						runningOrder.setTxtDua(dtoEventMaster.getDtoEventRunningOrder().getTxtDua());
						runningOrder.setTxtDance(dtoEventMaster.getDtoEventRunningOrder().getTxtDance());
						runningOrder.setTxtCakeCutting(dtoEventMaster.getDtoEventRunningOrder().getTxtCakeCutting());
						runningOrder.setTxtRingExchange(dtoEventMaster.getDtoEventRunningOrder().getTxtRingExchange());
						runningOrder.setTxtRams(dtoEventMaster.getDtoEventRunningOrder().getTxtRams());
						runningOrder.setTxtSpeeches(dtoEventMaster.getDtoEventRunningOrder().getTxtSpeeches());
						runningOrder = repositoryEventRunningOrder.save(runningOrder);
					} else {
						runningOrder = MapperEventRunningOrder.toEntity(dtoEventMaster.getDtoEventRunningOrder());
						runningOrder = repositoryEventRunningOrder.save(runningOrder);
						// entity.setNumInfoFilledStatus(entity.getNumInfoFilledStatus() + 1);
					}
					entity.setEventRunningOrder(runningOrder);

					// entity.setNumInfoFilledStatus(30);

				}

				// Setting Venue Master
				// ********************

				// This is for which you only need to specify which venu is selected
				if (UtilRandomKey.isNotNull(dtoEventMaster.getSerVenueMasterId())) {
					VenueMaster venueMaster = serviceVenueMaster.getByPK(dtoEventMaster.getSerVenueMasterId());
					if (UtilRandomKey.isNull(venueMaster)) {
						dtoResult.setTxtMessage("Venue Not Found For Id: " + dtoEventMaster.getSerVenueMasterId());
						return dtoResult;
					}
					entity.setVenueMaster(venueMaster);
					// entity.setNumInfoFilledStatus(50);
				}

				// //This is For when you need to save which hall of the venu was selected
				// if (UtilRandomKey.isNotNull(dtoEventMaster.getDtoEventVenue())) {
				// if
				// (UtilRandomKey.isNotNull(dtoEventMaster.getDtoEventVenue().getSerVenueMasterDetailId()))
				// {
				// DtoResult res = serviceVenueMaster.getVenueDetailByVenueMasterDetailId(
				// dtoEventMaster.getDtoEventVenue().getSerVenueMasterDetailId());
				// if (res.getTxtMessage().equalsIgnoreCase("Success")) {
				// VenueMasterDetail venueMasterDetail = (VenueMasterDetail) res.getResult();
				// entity.setVenueMasterDetail(venueMasterDetail);
				// } else {
				// dtoResult.setTxtMessage("Venue Hall Is Not Active");
				// return dtoResult;
				// }
				// } else {
				// dtoResult.setTxtMessage("Venue Hall Is Not Selected");
				// return dtoResult;
				// }
				// entity.setNumInfoFilledStatus(50);
				//
				// }

				// Set Decor Item Selections
				// *************************
				if (UtilRandomKey.isNotNull(dtoEventMaster.getDtoEventDecorSelections())) {

					// Deleting Existing Selections
					// if(entity.getDecorSelections() != null &&
					// !entity.getDecorSelections().isEmpty()) {
					// serviceEventDecorCategorySelection.deleteByEventMasterId(entity.getSerEventMasterId());
					// }
					if (entity.getDecorSelections() != null) {
						entity.getDecorSelections().clear();
					}

					List<EventDecorCategorySelection> decorSelections = new ArrayList<>();

					for (DtoEventDecorCategorySelection dto : dtoEventMaster.getDtoEventDecorSelections()) {
						EventDecorCategorySelection decorSelection = MapperEventDecorCategorySelection.toEntity(dto);
						decorSelection.setSerEventDecorCategorySelectionId(null);
						decorSelection.setEventMaster(entity);
						// decorSelection = repositoryEventDecorCategorySelection.save(decorSelection);

						// Set property selections' back reference
						// if (decorSelection.getSelectedProperties() != null) {
						// for (EventDecorPropertySelection prop :
						// decorSelection.getSelectedProperties()) {
						// prop.setEventDecorCategorySelection(decorSelection);
						//
						// }
						// }
						if (dto.getSelectedProperties() != null && !dto.getSelectedProperties().isEmpty()) {
							if (decorSelection.getSelectedProperties() != null) {
								decorSelection.getSelectedProperties().clear();
							}

							List<EventDecorPropertySelection> newSelectedProperties = new ArrayList<>();
							for (DtoEventDecorPropertySelection property : dto.getSelectedProperties()) {
								EventDecorPropertySelection eventDecorPropertySelection = new EventDecorPropertySelection();
								eventDecorPropertySelection.setBlnIsActive(true);
								eventDecorPropertySelection.setBlnIsDeleted(false);
								eventDecorPropertySelection.setCreatedDate(UtilDateAndTime.getCurrentDate());
								eventDecorPropertySelection.setEventDecorCategorySelection(decorSelection);

								DecorCategoryPropertyMaster matchedMaster = decorCategoryPropertyMasterLst.stream()
										.filter(pm -> pm.getSerPropertyId().intValue() == property.getSerPropertyId()
												.intValue())
										.findFirst().orElse(null);

								DecorCategoryPropertyValue matchedValue = decorCategoryPropertyValueLst.stream()
										.filter(pv -> pv.getSerPropertyValueId().intValue() == property
												.getSerPropertyValueId().intValue())
										.findFirst().orElse(null);

								eventDecorPropertySelection.setProperty(matchedMaster);
								eventDecorPropertySelection.setSelectedValue(matchedValue);
								newSelectedProperties.add(eventDecorPropertySelection);
							}

							decorSelection.getSelectedProperties().addAll(newSelectedProperties);
						}

						// Set reference image back reference

						if (decorSelection.getUserUploadedDocuments() != null && UtilRandomKey.isNotNull(files)) {
							decorSelection.getUserUploadedDocuments().clear();
							List<EventDecorReferenceDocument> documents = new ArrayList<>();
							for (DtoEventDecorReferenceDocument dtoImg : dto.getUserUploadedDocuments()) {
								MultipartFile file = fileMap.get(dtoImg.getOriginalName());
								if (file != null) {
									String uploadPath = UtilFileStorage.saveFile(file, "UserReferenceDecor");
									EventDecorReferenceDocument doc = new EventDecorReferenceDocument();
									doc.setDocumentName(file.getName());
									doc.setOriginalName(file.getOriginalFilename());
									doc.setDocumentType(file.getContentType());
									doc.setSize(String.valueOf(file.getSize()));
									doc.setFilePath(uploadPath);
									doc.setEventDecorCategorySelection(decorSelection);
									documents.add(doc);
								}
							}
							// decorSelection.setUserUploadedDocuments(documents);
							decorSelection.getUserUploadedDocuments().addAll(documents);
						}

						decorSelections.add(decorSelection);
					}

					// entity.setDecorSelections(decorSelections);
					entity.getDecorSelections().addAll(decorSelections);
					// entity.setNumInfoFilledStatus(70);
				}

				// Set Food Menu Selection
				// ***********************
				if (UtilRandomKey.isNotNull(dtoEventMaster.getFoodSelections())
						&& !dtoEventMaster.getFoodSelections().isEmpty()) {
//					serviceEventMenuFoodSelection.deleteByEventMasterId(entity.getSerEventMasterId());
					if (entity.getFoodSelections() != null) {
						entity.getFoodSelections().clear();
					}
					List<DtoMenuFoodMaster> foodSelections = dtoEventMaster.getFoodSelections();
					List<EventMenuFoodSelection> eventMenuFoodSelectionLst = new ArrayList<>();
//					for (DtoEventMenuFoodSelection dto : dtoEventMaster.getFoodSelections()) {
					for (DtoMenuFoodMaster dto : foodSelections) {
						EventMenuFoodSelection eventMenuFoodSelection = new EventMenuFoodSelection();
						eventMenuFoodSelection.setEventMaster(entity);
						eventMenuFoodSelection.setBlnIsActive(true);
						eventMenuFoodSelection.setBlnIsApproved(true);
						eventMenuFoodSelection.setBlnIsDeleted(false);
						eventMenuFoodSelection.setEventMaster(entity);
						
						if (UtilRandomKey.isNotNull(dto.getSerMenuItemId())) {
							
//							MenuFoodMaster menuFoodMaster = dtoMenuFoodMasterLst.stream()
//									.filter(food -> food.getSerMenuFoodId() != null
//											&& food.getSerMenuFoodId().intValue() == dto.getSerMenuFoodId().intValue())
//									.findFirst().orElse(null);
							MenuItem menuItem = menuItems.stream()
									.filter(item -> item.getSerMenuItemId() != null
											&& item.getSerMenuItemId().intValue() == dto.getSerMenuItemId().intValue())
									.findFirst().orElse(null);
							
							if (UtilRandomKey.isNotNull(menuItem)) {
//								eventMenuFoodSelection.setMenuFoodMaster(menuFoodMaster);
								eventMenuFoodSelection.setMenuItem(menuItem);
//								eventMenuFoodSelection.setTxtFoodType(getFoodName(menuFoodMaster));
							} else {
								dtoResult.setTxtMessage("Food Selection Item Does Not Have Food Menu With Id: "
										+ dto.getSerMenuFoodId() + " In DB.");
								return dtoResult;
							}
						} else {
							dtoResult.setTxtMessage("Food Selection Item Does Not Have The Id OF Food Menu");
							return dtoResult;
						}

						eventMenuFoodSelectionLst.add(eventMenuFoodSelection);

					}

//					String result = serviceEventMenuFoodSelection.saveAll(eventMenuFoodSelectionLst);
//					if (!result.equalsIgnoreCase("Success")) {
//						dtoResult.setTxtMessage(result);
//						return dtoResult;
//					}
					entity.getFoodSelections().addAll(eventMenuFoodSelectionLst);
//					entity.setNumInfoFilledStatus(90);

				}

				// Set Vendor
				// **********
				if (UtilRandomKey.isNotNull(dtoEventMaster.getSerVendorId())) {
					VendorMaster vendorMaster = serviceVendorMaster.getByPK(dtoEventMaster.getSerVendorId());
					if (UtilRandomKey.isNotNull(vendorMaster)) {

						entity.setVendorMaster(vendorMaster);
					} else {
						dtoResult.setTxtMessage(
								"External Supplier Not Found Against Id: " + dtoEventMaster.getSerVendorId());
						return dtoResult;
					}
					// entity.setNumInfoFilledStatus(100);
				}

				// Setting Event Quoted Price
				// **************************

				eventBudget = serviceEventBudget.getEventBudgetByEventId(entity.getSerEventMasterId());
				if (eventBudget != null) {
					if (eventBudget.getNumPaidAmount() != null
							&& eventBudget.getNumPaidAmount().compareTo(BigDecimal.ZERO) == 1) {
						eventBudget.setTxtStatus("Confirmed");

					} else if (dtoEventMaster.getDtoEventQuoteAndStatus() != null
							&& dtoEventMaster.getDtoEventQuoteAndStatus().getNumQuotedPrice() != null && dtoEventMaster
									.getDtoEventQuoteAndStatus().getNumQuotedPrice().compareTo(BigDecimal.ZERO) == 1) {
						eventBudget.setTxtStatus("Quoted");
						eventBudget.setNumQuotedPrice(dtoEventMaster.getDtoEventQuoteAndStatus().getNumQuotedPrice());
						eventBudget.setNumPaidAmount(BigDecimal.ZERO);

					} else {
						eventBudget.setTxtStatus("Enquiry");
						eventBudget.setNumQuotedPrice(BigDecimal.ZERO);
						eventBudget.setNumPaidAmount(BigDecimal.ZERO);

					}

				} else {
					eventBudget = new EventBudget();
					if (dtoEventMaster.getDtoEventQuoteAndStatus().getNumQuotedPrice() != null && dtoEventMaster
							.getDtoEventQuoteAndStatus().getNumQuotedPrice().compareTo(BigDecimal.ZERO) == 1) {
						eventBudget.setTxtStatus("Quoted");
						eventBudget.setNumQuotedPrice(dtoEventMaster.getDtoEventQuoteAndStatus().getNumQuotedPrice());
						eventBudget.setNumPaidAmount(BigDecimal.ZERO);

					} else {
						eventBudget.setTxtStatus("Enquiry");
						eventBudget.setNumQuotedPrice(BigDecimal.ZERO);
						eventBudget.setNumPaidAmount(BigDecimal.ZERO);

					}

				}

			} else {

				// ******************************************
				// *************** Create New ***************
				// ******************************************

				blnIsNewEvent = true;
				entity = MapperEventMaster.toEntity(dtoEventMaster);
				entity.setEventRunningOrder(null);
				entity.setEventType(null);
				entity.setDecorSelections(null);
				entity.setFoodSelections(null);
				entity.setNumFormState(dtoEventMaster.getNumFormState());
				entity = repositoryEventMaster.save(entity);
				entity.setCreatedBy(ServiceCurrentUser.getCurrentUserId());

				// if (UtilRandomKey.isNull(entity.getNumInfoFilledStatus())) {
				// entity.setNumInfoFilledStatus(0);
				// }

				// Set customer
				// ************
				if (UtilRandomKey.isNotNull(dtoEventMaster.getSerCustId())) {
					CustomerMaster customer = serviceCustomerMaster.getByPK(dtoEventMaster.getSerCustId());
					if (UtilRandomKey.isNull(customer)) {
						dtoResult.setTxtMessage("Customer Not Foound For Id" + dtoEventMaster.getSerCustId());
						return dtoResult;
					}
					entity.setCustomerMaster(customer);
					// entity.setNumInfoFilledStatus(10);

				}

				// Set event type
				// **************
				if (UtilRandomKey.isNotNull(dtoEventMaster.getSerEventTypeId())) {
					EventType eventType = serviceEventType.getByPK(dtoEventMaster.getSerEventTypeId());
					if (UtilRandomKey.isNull(eventType)) {
						dtoResult.setTxtMessage("Event Type Not Foound For Id" + dtoEventMaster.getSerEventTypeId());
						return dtoResult;
					}
					entity.setEventType(eventType);
				}

				// Set optional event running order
				// ********************************
				if (dtoEventMaster.getDtoEventRunningOrder() != null) {
					EventRunningOrder runningOrder = new EventRunningOrder();
					runningOrder = MapperEventRunningOrder.toEntity(dtoEventMaster.getDtoEventRunningOrder());
					runningOrder = repositoryEventRunningOrder.save(runningOrder);
					entity.setEventRunningOrder(runningOrder);
					// entity.setNumInfoFilledStatus(30);
				}

				// Set Venue Master
				// ****************

				// This is for which you only need to specify which venu is selected
				if (UtilRandomKey.isNotNull(dtoEventMaster.getSerVenueMasterId())) {
					VenueMaster venueMaster = serviceVenueMaster.getByPK(dtoEventMaster.getSerVenueMasterId());
					if (UtilRandomKey.isNull(venueMaster)) {
						dtoResult.setTxtMessage("Venue Not Found For Id: " + dtoEventMaster.getSerVenueMasterId());
						return dtoResult;
					}
					entity.setVenueMaster(venueMaster);
					// entity.setNumInfoFilledStatus(50);
				}

				// //This is For when you need to save which hall of the venu was selected
				// if (UtilRandomKey.isNotNull(dtoEventMaster.getDtoEventVenue())) {
				// if
				// (UtilRandomKey.isNotNull(dtoEventMaster.getDtoEventVenue().getSerVenueMasterDetailId()))
				// {
				// DtoResult res = serviceVenueMaster.getVenueDetailByVenueMasterDetailId(
				// dtoEventMaster.getDtoEventVenue().getSerVenueMasterDetailId());
				// if (res.getTxtMessage().equalsIgnoreCase("Success")) {
				// VenueMasterDetail venueMasterDetail = (VenueMasterDetail) res.getResult();
				// entity.setVenueMasterDetail(venueMasterDetail);
				// } else {
				// dtoResult.setTxtMessage("Venue Hall Is Not Active");
				// return dtoResult;
				// }
				// } else {
				// dtoResult.setTxtMessage("Venue Hall Is Not Selected");
				// return dtoResult;
				// }
				// entity.setNumInfoFilledStatus(50);
				//
				// }

				// Set Decore Item Selections
				// **************************
				if (UtilRandomKey.isNotNull(dtoEventMaster.getDtoEventDecorSelections())) {
					List<EventDecorCategorySelection> decorSelections = new ArrayList<>();

					for (DtoEventDecorCategorySelection dto : dtoEventMaster.getDtoEventDecorSelections()) {
						EventDecorCategorySelection decorSelection = MapperEventDecorCategorySelection.toEntity(dto);
						decorSelection.setSerEventDecorCategorySelectionId(null);
						decorSelection.setEventMaster(entity);

						// if (decorSelection.getSelectedProperties() != null) {
						// decorSelection.getSelectedProperties()
						// .forEach(p -> p.setEventDecorCategorySelection(decorSelection));
						// }

						if (dto.getSelectedProperties() != null && !dto.getSelectedProperties().isEmpty()) {
							if (decorSelection.getSelectedProperties() != null) {
								decorSelection.getSelectedProperties().clear();
							}

							List<EventDecorPropertySelection> newSelectedProperties = new ArrayList<>();
							for (DtoEventDecorPropertySelection property : dto.getSelectedProperties()) {
								EventDecorPropertySelection eventDecorPropertySelection = new EventDecorPropertySelection();
								eventDecorPropertySelection.setBlnIsActive(true);
								eventDecorPropertySelection.setBlnIsDeleted(false);
								eventDecorPropertySelection.setCreatedDate(UtilDateAndTime.getCurrentDate());
								eventDecorPropertySelection.setEventDecorCategorySelection(decorSelection);

								DecorCategoryPropertyMaster matchedMaster = decorCategoryPropertyMasterLst.stream()
										.filter(pm -> pm.getSerPropertyId().intValue() == property
												.getSerEventDecorPropertyId().intValue())
										.findFirst().orElse(null);

								DecorCategoryPropertyValue matchedValue = decorCategoryPropertyValueLst.stream()
										.filter(pv -> pv.getSerPropertyValueId().intValue() == property
												.getSerPropertyValueId().intValue())
										.findFirst().orElse(null);

								eventDecorPropertySelection.setProperty(matchedMaster);
								eventDecorPropertySelection.setSelectedValue(matchedValue);
								newSelectedProperties.add(eventDecorPropertySelection);
							}

							decorSelection.getSelectedProperties().addAll(newSelectedProperties);
						}

						// Set reference image back reference

						if (decorSelection.getUserUploadedDocuments() != null && UtilRandomKey.isNotNull(files)) {
							decorSelection.getUserUploadedDocuments().clear();
							List<EventDecorReferenceDocument> documents = new ArrayList<>();
							for (DtoEventDecorReferenceDocument dtoImg : dto.getUserUploadedDocuments()) {
								MultipartFile file = fileMap.get(dtoImg.getOriginalName());
								if (file != null) {
									String uploadPath = UtilFileStorage.saveFile(file, "UserReferenceDecor");
									EventDecorReferenceDocument doc = new EventDecorReferenceDocument();
									doc.setDocumentName(file.getName());
									doc.setOriginalName(file.getOriginalFilename());
									doc.setDocumentType(file.getContentType());
									doc.setSize(String.valueOf(file.getSize()));
									doc.setFilePath(uploadPath);
									doc.setEventDecorCategorySelection(decorSelection);
									documents.add(doc);
								}
							}
							// decorSelection.setUserUploadedDocuments(documents);
							decorSelection.getUserUploadedDocuments().addAll(documents);
						}

						decorSelections.add(decorSelection);
					}

					entity.setDecorSelections(decorSelections);
					// entity.setNumInfoFilledStatus(70);
				}

				// Set Food Menu Selection
				// ***********************
				if (UtilRandomKey.isNotNull(dtoEventMaster.getFoodSelections())) {
					
					
					List<EventMenuFoodSelection> eventMenuFoodSelectionLst = new ArrayList<>();
					List<DtoMenuFoodMaster> foodSelections = dtoEventMaster.getFoodSelections();

					
//					for (DtoEventMenuFoodSelection dto : dtoEventMaster.getFoodSelections()) {
					for (DtoMenuFoodMaster dto : foodSelections) {
						EventMenuFoodSelection eventMenuFoodSelection = new EventMenuFoodSelection();
						eventMenuFoodSelection.setEventMaster(entity);
						eventMenuFoodSelection.setBlnIsActive(true);
						eventMenuFoodSelection.setBlnIsApproved(true);
						eventMenuFoodSelection.setBlnIsDeleted(false);
						if (UtilRandomKey.isNotNull(dto.getSerMenuItemId())) {
//							MenuFoodMaster menuFoodMaster = dtoMenuFoodMasterLst.stream()
//									.filter(food -> food.getSerMenuFoodId() != null
//											&& food.getSerMenuFoodId().intValue() == dto.getSerMenuFoodId().intValue())
//									.findFirst().orElse(null);
							
							MenuItem menuItem = menuItems.stream()
									.filter(item -> item.getSerMenuItemId() != null
											&& item.getSerMenuItemId().intValue() == dto.getSerMenuItemId().intValue())
									.findFirst().orElse(null);
							
							if (UtilRandomKey.isNotNull(menuItem)) {
								eventMenuFoodSelection.setMenuItem(menuItem);
//								eventMenuFoodSelection.setTxtFoodType(getFoodName(menuFoodMaster));
							} else {
								dtoResult.setTxtMessage("Food Selection Item Does Not Have Food Menu With Id: "
										+ dto.getSerMenuFoodId() + " In DB.");
								return dtoResult;
							}
						} else {
							dtoResult.setTxtMessage("Food Selection Item Does Not Have The Id OF Food Menu");
							return dtoResult;
						}

						eventMenuFoodSelectionLst.add(eventMenuFoodSelection);
					}

					String result = serviceEventMenuFoodSelection.saveAll(eventMenuFoodSelectionLst);
					if (!result.equalsIgnoreCase("Success")) {
						dtoResult.setTxtMessage(result);
						return dtoResult;
					}
//					entity.setNumInfoFilledStatus(90);
				}

				// Set Vendor
				// **********
				if (UtilRandomKey.isNotNull(dtoEventMaster.getSerVendorId())) {
					VendorMaster vendorMaster = serviceVendorMaster.getByPK(dtoEventMaster.getSerVendorId());
					if (UtilRandomKey.isNotNull(vendorMaster)) {
						entity.setVendorMaster(vendorMaster);
						// entity.setNumInfoFilledStatus(entity.getNumInfoFilledStatus() + 1);
					} else {
						dtoResult.setTxtMessage(
								"External Supplier Not Found Against Id: " + dtoEventMaster.getSerVendorId());
						return dtoResult;
					}
				}
				// entity.setNumInfoFilledStatus(100);

				// Generate event master code
				String code = generateNextEventMasterCode();
				entity.setTxtEventMasterCode(code);

				// Setting up Event QuotePrice
				// ***************************

				eventBudget = new EventBudget();
				if (dtoEventMaster.getDtoEventQuoteAndStatus() != null
						&& dtoEventMaster.getDtoEventQuoteAndStatus().getNumQuotedPrice() != null && dtoEventMaster
								.getDtoEventQuoteAndStatus().getNumQuotedPrice().compareTo(BigDecimal.ZERO) == 1) {
					eventBudget.setTxtStatus("Quoted");
					eventBudget.setNumQuotedPrice(dtoEventMaster.getDtoEventQuoteAndStatus().getNumQuotedPrice());
					eventBudget.setNumPaidAmount(BigDecimal.ZERO);

				} else {
					eventBudget.setTxtStatus("Enquiry");
					eventBudget.setNumQuotedPrice(BigDecimal.ZERO);
					eventBudget.setNumPaidAmount(BigDecimal.ZERO);

				}
			}

			// ****** Setting Event Decor Extras ******
			if (entity.getExtrasSelections() != null) {
				entity.getExtrasSelections().clear();
			}

			if (UtilRandomKey.isNotNull(dtoEventMaster.getExtrasSelections())
					&& !dtoEventMaster.getExtrasSelections().isEmpty()) {
				// serviceEventDecorExtrasSelection.deleteByEventMasterId(entity.getSerEventMasterId());

				List<EventDecorExtrasSelection> newSelections = new ArrayList<>();
				for (DtoEventDecorExtrasSelection dto : dtoEventMaster.getExtrasSelections()) {
					EventDecorExtrasSelection selection = new EventDecorExtrasSelection();
					selection.setTxtDynamicProperty1(dto.getTxtDynamicProperty1());
					selection.setTxtDynamicProperty2(dto.getTxtDynamicProperty2());
					selection.setEventMaster(entity);
					if (dto.getSerExtrasId() != null) {
						selection.setDecorExtrasMaster(
								serviceDecorExtrasMaster.getByIdAndNotDeleted(dto.getSerExtrasId()));
					}
					if (dto.getSerExtraOptionId() != null) {
						selection.setDecorExtrasOption(
								serviceDecorExtrasOption.getByIdAndNotDeleted(dto.getSerExtraOptionId()));
					}

					// selection = serviceEventDecorExtrasSelection.save(selection);
					newSelections.add(selection);
				}
				// entity.setExtrasSelections(newSelections);
				// entity.setNumInfoFilledStatus(entity.getNumInfoFilledStatus() + 1);
				entity.getExtrasSelections().addAll(newSelections);
			}

			entity.setNumInfoFilledStatus(getEventCompletionPercentage(entity));
			entity = repositoryEventMaster.save(entity);
			if (eventBudget != null) {
				eventBudget.setEventMaster(entity);
				serviceEventBudget.save(eventBudget);
			}

			// ***** Sending Notification Of New Customer Registration *****
			if (blnIsNewEvent) {
				this.sendNewEventRegistrationNotification(
						entity.getEventType() != null ? entity.getEventType().getTxtEventTypeName() : "",
						entity.getTxtEventMasterCode() != null ? entity.getTxtEventMasterCode() : "",
						dtoEventMaster.getDteEventDate() != null ? dtoEventMaster.getDteEventDate() : "",
						entity.getSerEventMasterId());
			}

			if (this.isEventRegistrationCompleted(entity)) {
				UserMaster userMaster = ServiceCurrentUser.getCurrentUser();
				if (userMaster != null) {
					serviceEmailSender.sendEventRegistrationEmail(userMaster.getTxtEmail(), userMaster.getTxtName(),
							entity.getTxtEventMasterCode(), entity.getEventType().getTxtEventTypeName(),
							entity.getDteEventDate());

					// ***** Send Email To All Admin Users ********
					serviceEmailSender.sendEventRegistrationEmailToAdminUsers(userMaster.getTxtName(),
							entity.getTxtEventMasterCode(), entity.getEventType().getTxtEventTypeName(),
							entity.getDteEventDate());

					dtoResult.setTxtMessage(entity.getEventType().getTxtEventTypeName()
							+ " Event Has Been Registered. A Confirmation Email Has Been Sent To Your Registered Email.");
				} else {
					dtoResult.setTxtMessage("Success");

				}
			} else {
				dtoResult.setTxtMessage("Success");
			}

			DtoEventMaster dtoEvent = this.getEventById(entity.getSerEventMasterId());
			dtoResult.setResult(dtoEvent);
			return dtoResult;
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			dtoResult.setTxtMessage("Failure");
			return dtoResult;
		}
	}

	@Override
	public DtoResult deleteById(Integer id) {
		DtoResult result = new DtoResult();
		Optional<EventMaster> optional = repositoryEventMaster.findById(id);
		if (optional.isPresent()) {
			EventMaster e = optional.get();
			DtoEventBudget eventBudget = serviceEventBudget.getByEventId(e.getSerEventMasterId());
			if (eventBudget != null) {
				result.setTxtMessage("Can Not Delete Event Master As It Exists In Event Busget");
				return result;
			}
			e.setBlnIsDeleted(true);
			e.setUpdatedBy(ServiceCurrentUser.getCurrentUserId());
			repositoryEventMaster.save(e);
			result.setTxtMessage("Deleted (soft) successfully");
		} else {
			result.setTxtMessage("No record found to delete");
		}
		return result;
	}

	private Boolean isEventRegistrationCompleted(EventMaster eventMaster) {
		if (eventMaster != null && eventMaster.getTxtEventMasterCode() != null && eventMaster.getDteEventDate() != null
				&& eventMaster.getCustomerMaster() != null && eventMaster.getEventRunningOrder() != null
				&& eventMaster.getEventType() != null
				&& (eventMaster.getVenueMaster() != null || eventMaster.getVenueMasterDetail() != null)
				&& (eventMaster.getFoodSelections() != null && !eventMaster.getFoodSelections().isEmpty())
				&& (eventMaster.getDecorSelections() != null && !eventMaster.getDecorSelections().isEmpty())) {
			return true;
		} else {
			return false;
		}
	}

	private Integer getEventCompletionPercentage(EventMaster eventMaster) {
		int percentage = 0;
		if (eventMaster != null) {
			if (eventMaster.getEventType() != null) {
				percentage += 10;
			}
			if (eventMaster.getDteEventDate() != null) {
				percentage += 10;
			}
			if (eventMaster.getCustomerMaster() != null) {
				percentage += 10;
			}
			if (eventMaster.getEventRunningOrder() != null) {
				percentage += 10;
			}
			if (eventMaster.getDecorSelections() != null && !eventMaster.getDecorSelections().isEmpty()) {
				percentage += 25;
			}
			if (eventMaster.getFoodSelections() != null && !eventMaster.getFoodSelections().isEmpty()) {
				percentage += 25;
			}
			if (eventMaster.getExtrasSelections() != null && !eventMaster.getExtrasSelections().isEmpty()) {
				percentage += 10;
			}

		}
		return percentage;
	}

	private DtoEventMaster getEventById(Integer serEventId) {
		DtoResult dtoResult = new DtoResult();

		try {

			Optional<EventMaster> optEvent = repositoryEventMaster.findById(serEventId);
			if (UtilRandomKey.isNotNull(optEvent)) {
				EventMaster event = optEvent.get();
				DtoEventMaster dto = MapperEventMaster.toDto(event);

				// Fetching Event Venue Detail
				// ***********************************
				if (UtilRandomKey.isNotNull(event.getVenueMasterDetail())) {
					DtoResult res = serviceVenueMaster
							.getVenueByVenueMasterDetailId(event.getVenueMasterDetail().getSerVenueMasterDetailId());
					if (UtilRandomKey.isNotNull(res) && res.getTxtMessage().equalsIgnoreCase("Success")) {
						VenueMaster venueMaster = (VenueMaster) res.getResult();
						DtoEventVenue dtoEventVenue = new DtoEventVenue();
						dtoEventVenue.setSerVenueMasterId(venueMaster.getSerVenueMasterId());
						dtoEventVenue.setTxtVenueCode(venueMaster.getTxtVenueCode());
						dtoEventVenue.setTxtVenueName(venueMaster.getTxtVenueName());
						dtoEventVenue
								.setSerVenueMasterDetailId(event.getVenueMasterDetail().getSerVenueMasterDetailId());
						dtoEventVenue.setTxtHallCode(event.getVenueMasterDetail().getTxtHallCode());
						dtoEventVenue.setTxtHallName(event.getVenueMasterDetail().getTxtHallName());
					}
				}

				// Fetching Decor
				// ***********************************
				List<DtoEventDecorCategorySelection> eventDecorCategorySelections = serviceEventDecorCategorySelection
						.getSelectionsWithChosenValues(dto.getSerEventMasterId());
				dto.setDtoEventDecorSelections(eventDecorCategorySelections);

				// Fetching Event Menu Food Selection
				// ***********************************

				List<EventMenuFoodSelection> eventMenuFoodSelections = serviceEventMenuFoodSelection
						.getByEventMasterId(dto.getSerEventMasterId());
//				List<DtoEventMenuFoodSelection> dtoEventMenuFoodSelections = new ArrayList<>();
//				if (UtilRandomKey.isNotNull(eventMenuFoodSelections)) {
//					for (EventMenuFoodSelection entity : eventMenuFoodSelections) {
//						DtoEventMenuFoodSelection dtoEventMenuFoodSelection = MapperEventMenuFoodSelection
//								.toDto(entity);
//						dtoEventMenuFoodSelections.add(dtoEventMenuFoodSelection);
//					}
//				}
//				dto.setFoodSelections(dtoEventMenuFoodSelections);
				List<DtoMenuFoodMaster> foodSelections = new ArrayList<>();
				if (UtilRandomKey.isNotNull(eventMenuFoodSelections)) {
					for (EventMenuFoodSelection entity : eventMenuFoodSelections) {
						if (entity.getMenuItem() != null) {
							DtoMenuFoodMaster dtoMenuFoodMaster = new DtoMenuFoodMaster();
							MenuItem menuItem = entity.getMenuItem();
							dtoMenuFoodMaster.setBlnIsActive(menuItem.getBlnIsActive());
							dtoMenuFoodMaster.setSerMenuItemId(menuItem.getSerMenuItemId());
							dtoMenuFoodMaster.setTxtName(menuItem.getTxtName());
							dtoMenuFoodMaster.setTxtCode(menuItem.getTxtCode());
							dtoMenuFoodMaster.setTxtDescription(menuItem.getTxtDescription());
							dtoMenuFoodMaster.setNumPrice(entity.getNumPrice());
							foodSelections.add(dtoMenuFoodMaster);
						}
					}
				}
				dto.setFoodSelections(foodSelections);

				// Fetching Event Extras Selection
				// ***********************************

				List<EventDecorExtrasSelection> eventDecorExtrasSelection = serviceEventDecorExtrasSelection
						.getByEventMasterId(dto.getSerEventMasterId());
				List<DtoEventDecorExtrasSelection> dtoEventDecorExtrasSelections = new ArrayList<>();
				if (UtilRandomKey.isNotNull(eventDecorExtrasSelection)) {
					for (EventDecorExtrasSelection entity : eventDecorExtrasSelection) {
						DtoEventDecorExtrasSelection dtoEventDecorExtrasSelection = new DtoEventDecorExtrasSelection();
						dtoEventDecorExtrasSelection.setSerExtrasSelectionId(entity.getSerExtrasSelectionId());
						dtoEventDecorExtrasSelection.setTxtDynamicProperty1(entity.getTxtDynamicProperty1());
						dtoEventDecorExtrasSelection.setTxtDynamicProperty2(entity.getTxtDynamicProperty2());
						if (entity.getDecorExtrasMaster() != null) {
							dtoEventDecorExtrasSelection.setSerExtrasId(entity.getDecorExtrasMaster().getSerExtrasId());
							dtoEventDecorExtrasSelection
									.setTxtExtrasCode(entity.getDecorExtrasMaster().getTxtExtrasCode());
							dtoEventDecorExtrasSelection
									.setTxtExtrasName(entity.getDecorExtrasMaster().getTxtExtrasName());
						}

						if (entity.getDecorExtrasOption() != null) {
							dtoEventDecorExtrasSelection
									.setSerExtraOptionId(entity.getDecorExtrasOption().getSerExtraOptionId());
							dtoEventDecorExtrasSelection
									.setTxtOptionCode(entity.getDecorExtrasOption().getTxtOptionCode());
							dtoEventDecorExtrasSelection
									.setTxtOptionName(entity.getDecorExtrasOption().getTxtOptionName());
						}

						dtoEventDecorExtrasSelections.add(dtoEventDecorExtrasSelection);
					}
				}
				dto.setExtrasSelections(dtoEventDecorExtrasSelections);

				return dto;

			} else {
				dtoResult.setTxtMessage("No Data Found In System");
				return null;
			}

		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			dtoResult.setTxtMessage("Error occurred: " + e.getMessage());
			return null;
		}

	}

	private String getFoodName(MenuFoodMaster food) {
		if (food.getBlnIsAppetiser() != null && food.getBlnIsAppetiser()) {
			return "Appetizers";
		} else if (food.getBlnIsDessert() != null && food.getBlnIsDessert()) {
			return "Desserts";
		} else if (food.getBlnIsStarter() != null && food.getBlnIsStarter()) {
			return "Starters & Main course";
		} else if (food.getBlnIsSaladAndCondiment() != null && food.getBlnIsSaladAndCondiment()) {
			return "Salad & Condiments";
		} else if (food.getBlnIsDrink() != null && food.getBlnIsDrink()) {
			return "Reception Drinks";
		} else if (food.getBlnIsAppetiser() != null && food.getBlnIsAppetiser()) {

		} else if (food.getBlnIsMainCourse() != null && food.getBlnIsMainCourse()) {
			return "Mains";
		}
		return null;
	}

	private void sendNewEventRegistrationNotification(String eventTypeName, String txtEventCode, String Date,
			Integer Id) {
		// Inside ServiceEventMasterImpl, after saving event
		DtoNotificationMaster notif = serviceNotificationMaster.createNotification(
				ServiceCurrentUser.getCurrentUserId().longValue(), "Event Registered",
				"A New '" + eventTypeName + "' Event With Code '" + txtEventCode + "'  Is Registered for " + Date,
				"/eventMaster/getByEventId" + Id, "EVENT_REGISTERED");

		serviceNotificationMaster.sendNotification(ServiceCurrentUser.getCurrentUserId().longValue(), notif);
	}

	@Override
	@Transactional
	public DtoResult saveAndUpdateWithDocsAdminPortal(DtoEventMasterAdminPortal dtoEventMasterAdminPortal,
			List<MultipartFile> files) throws IOException {
		// Validate required IDs
		DtoResult dtoResult = new DtoResult();
		try {
			if (dtoEventMasterAdminPortal.getSerCustId() == null
					|| dtoEventMasterAdminPortal.getSerEventTypeId() == null) {
				LOGGER.debug("Customer ID and Event Type ID are required");
				dtoResult.setTxtMessage("Customer ID and Event Type ID are required");
				return dtoResult;
			}

			Boolean blnIsNewEvent = false;
			// Fetch existing if exists
			Optional<EventMaster> optionalExisting = null;
			if (dtoEventMasterAdminPortal.getSerEventMasterId() != null) {
				optionalExisting = repositoryEventMaster
						.findByIdAndBlnIsDeletedFalse(dtoEventMasterAdminPortal.getSerEventMasterId());
			}

			// List<MenuFoodMaster> dtoMenuFoodMasterLst =
			// serviceMenuFoodMaster.getAllDataEntity();
			// if (UtilRandomKey.isNull(dtoMenuFoodMasterLst)) {
			// dtoResult.setTxtMessage("No Food Item Is Present In DB");
			// return dtoResult;
			// }

			List<MenuItem> menuItems = serviceMenuItem.getAllMenuItems();
			if (UtilRandomKey.isNull(menuItems)) {
				dtoResult.setTxtMessage("No Food Item Is Present In DB");
				return dtoResult;
			}

			EventBudget eventBudget = null;
			List<DecorCategoryPropertyMaster> decorCategoryPropertyMasterLst = serviceDecorCategoryPropertyMaster
					.getAllPropertiesMaster();
			List<DecorCategoryPropertyValue> decorCategoryPropertyValueLst = serviceDecorCategoryPropertyValue
					.getAllPropertyValueMaster();
			List<DecorExtrasMaster> decorExtrasMasterLst = serviceDecorExtrasMaster.getAllDecorExtrasMaster();
			List<DecorExtrasOption> decorExtrasOptions = null;
			if (decorExtrasMasterLst != null && !decorExtrasMasterLst.isEmpty()) {
				decorExtrasOptions = new ArrayList<>();
				for (DecorExtrasMaster extrasMaster : decorExtrasMasterLst) {
					if (extrasMaster.getDecorExtrasOptions() != null) {
						decorExtrasOptions.addAll(extrasMaster.getDecorExtrasOptions());
					}
				}
			}

			EventMaster entity;
			Map<String, MultipartFile> fileMap = null;
			if (UtilRandomKey.isNotNull(files)) {
				fileMap = files.stream().collect(Collectors.toMap(MultipartFile::getOriginalFilename, f -> f));
			}

			if (optionalExisting != null && optionalExisting.isPresent()) {
				// Update existing
				entity = optionalExisting.get();

				// Manually update values (keep ID)
				entity.setTxtEventMasterName(dtoEventMasterAdminPortal.getTxtEventMasterName());
				entity.setDteEventDate(
						UtilDateAndTime.ddMMyyyyDashedStringToDate(dtoEventMasterAdminPortal.getDteEventDate()));
				entity.setNumNumberOfGuests(dtoEventMasterAdminPortal.getNumNumberOfGuests());
				entity.setNumNumberOfTables(dtoEventMasterAdminPortal.getNumNumberOfTables());
				entity.setTxtBrideName(dtoEventMasterAdminPortal.getTxtBrideName());
				entity.setTxtBrideFirstName(dtoEventMasterAdminPortal.getTxtBrideFirstName());
				entity.setTxtBrideLastName(dtoEventMasterAdminPortal.getTxtBrideLastName());
				entity.setTxtGroomName(dtoEventMasterAdminPortal.getTxtGroomName());
				entity.setTxtGroomFirstName(dtoEventMasterAdminPortal.getTxtGroomFirstName());
				entity.setTxtGroomLastName(dtoEventMasterAdminPortal.getTxtGroomLastName());
				entity.setTxtBirthDayCelebrant(dtoEventMasterAdminPortal.getTxtBirthDayCelebrant());
				entity.setTxtAgeCategory(dtoEventMasterAdminPortal.getTxtAgeCategory());
				entity.setTxtChiefGuest(dtoEventMasterAdminPortal.getTxtChiefGuest());
				entity.setUpdatedBy(ServiceCurrentUser.getCurrentUserId());
				entity.setTxtCateringRemarks(dtoEventMasterAdminPortal.getTxtCateringRemarks());
				entity.setTxtDecoreRemarks(dtoEventMasterAdminPortal.getTxtDecoreRemarks());
				entity.setTxtEventExtrasRemarks(dtoEventMasterAdminPortal.getTxtEventExtrasRemarks());
				entity.setTxtEventRemarks(dtoEventMasterAdminPortal.getTxtEventRemarks());
				entity.setTxtExternalSupplierRemarks(dtoEventMasterAdminPortal.getTxtExternalSupplierRemarks());
				entity.setTxtVenueRemarks(dtoEventMasterAdminPortal.getTxtVenueRemarks());
				entity.setIsEditAllowed(dtoEventMasterAdminPortal.getIsEditAllowed());
				entity.setTxtContactPersonFirstName(dtoEventMasterAdminPortal.getTxtContactPersonFirstName());
				entity.setTxtContactPersonLastName(dtoEventMasterAdminPortal.getTxtContactPersonLastName());
				entity.setTxtContactPersonPhoneNo(dtoEventMasterAdminPortal.getTxtContactPersonPhoneNo());
				entity.setNumItineraryPrice(dtoEventMasterAdminPortal.getNumItineraryPrice());
				entity.setNumServingDishesPrice(dtoEventMasterAdminPortal.getNumServingDishesPrice());
				// if (UtilRandomKey.isNull(entity.getNumInfoFilledStatus())) {
				// entity.setNumInfoFilledStatus(0);
				// }

				// Set customer
				// ************
				if (UtilRandomKey.isNotNull(dtoEventMasterAdminPortal.getSerCustId())) {
					CustomerMaster customer = serviceCustomerMaster.getByPK(dtoEventMasterAdminPortal.getSerCustId());
					if (UtilRandomKey.isNull(customer)) {
						dtoResult
								.setTxtMessage("Customer Not Foound For Id" + dtoEventMasterAdminPortal.getSerCustId());
						return dtoResult;
					}
					// entity.setNumInfoFilledStatus(10);
					entity.setCustomerMaster(customer);
				}

				// Set event type
				// **************
				if (UtilRandomKey.isNotNull(dtoEventMasterAdminPortal.getSerEventTypeId())) {
					EventType eventType = serviceEventType.getByPK(dtoEventMasterAdminPortal.getSerEventTypeId());
					if (UtilRandomKey.isNull(eventType)) {
						dtoResult.setTxtMessage(
								"Event Type Not Foound For Id" + dtoEventMasterAdminPortal.getSerEventTypeId());
						return dtoResult;
					}
					entity.setEventType(eventType);
				}

				// Set optional event running order
				// ********************************
				if (dtoEventMasterAdminPortal.getDtoEventRunningOrder() != null) {
					EventRunningOrder runningOrder = new EventRunningOrder();

					if (UtilRandomKey.isNotNull(entity.getEventRunningOrder())) {

						runningOrder = entity.getEventRunningOrder();
						runningOrder.setTxtGuestArrival(
								dtoEventMasterAdminPortal.getDtoEventRunningOrder().getTxtGuestArrival());
						runningOrder.setTxtBaratArrival(
								dtoEventMasterAdminPortal.getDtoEventRunningOrder().getTxtBaratArrival());
						runningOrder.setTxtBrideEntrance(
								dtoEventMasterAdminPortal.getDtoEventRunningOrder().getTxtBrideEntrance());
						runningOrder.setTxtNikah(dtoEventMasterAdminPortal.getDtoEventRunningOrder().getTxtNikah());
						runningOrder.setTxtMeal(dtoEventMasterAdminPortal.getDtoEventRunningOrder().getTxtMeal());
						runningOrder.setTxtEndOfNight(
								dtoEventMasterAdminPortal.getDtoEventRunningOrder().getTxtEndOfNight());
						runningOrder.setTxtBrideGuestArrival(
								dtoEventMasterAdminPortal.getDtoEventRunningOrder().getTxtBrideGuestArrival());
						runningOrder.setTxtGroomGuestArrival(
								dtoEventMasterAdminPortal.getDtoEventRunningOrder().getTxtGroomGuestArrival());
						runningOrder.setTxtGroomEntrance(
								dtoEventMasterAdminPortal.getDtoEventRunningOrder().getTxtGroomEntrance());
						runningOrder.setTxtCouplesEntrance(
								dtoEventMasterAdminPortal.getDtoEventRunningOrder().getTxtCouplesEntrance());
						runningOrder.setTxtDua(dtoEventMasterAdminPortal.getDtoEventRunningOrder().getTxtDua());
						runningOrder.setTxtDance(dtoEventMasterAdminPortal.getDtoEventRunningOrder().getTxtDance());
						runningOrder.setTxtCakeCutting(
								dtoEventMasterAdminPortal.getDtoEventRunningOrder().getTxtCakeCutting());
						runningOrder.setTxtRingExchange(
								dtoEventMasterAdminPortal.getDtoEventRunningOrder().getTxtRingExchange());
						runningOrder.setTxtRams(dtoEventMasterAdminPortal.getDtoEventRunningOrder().getTxtRams());
						runningOrder
								.setTxtSpeeches(dtoEventMasterAdminPortal.getDtoEventRunningOrder().getTxtSpeeches());
						runningOrder = repositoryEventRunningOrder.save(runningOrder);
					} else {
						runningOrder = MapperEventRunningOrder
								.toEntity(dtoEventMasterAdminPortal.getDtoEventRunningOrder());
						runningOrder = repositoryEventRunningOrder.save(runningOrder);
						// entity.setNumInfoFilledStatus(entity.getNumInfoFilledStatus() + 1);
					}
					entity.setEventRunningOrder(runningOrder);

					// entity.setNumInfoFilledStatus(30);

				}

				// Setting Venue Master
				// ********************

				// This is for which you only need to specify which venu is selected
				if (UtilRandomKey.isNotNull(dtoEventMasterAdminPortal.getSerVenueMasterId())) {
					VenueMaster venueMaster = serviceVenueMaster
							.getByPK(dtoEventMasterAdminPortal.getSerVenueMasterId());
					if (UtilRandomKey.isNull(venueMaster)) {
						dtoResult.setTxtMessage(
								"Venue Not Found For Id: " + dtoEventMasterAdminPortal.getSerVenueMasterId());
						return dtoResult;
					}
					entity.setVenueMaster(venueMaster);
					// entity.setNumInfoFilledStatus(50);
				}

				// //This is For when you need to save which hall of the venu was selected
				// if (UtilRandomKey.isNotNull(dtoEventMasterAdminPortal.getDtoEventVenue())) {
				// if
				// (UtilRandomKey.isNotNull(dtoEventMasterAdminPortal.getDtoEventVenue().getSerVenueMasterDetailId()))
				// {
				// DtoResult res = serviceVenueMaster.getVenueDetailByVenueMasterDetailId(
				// dtoEventMasterAdminPortal.getDtoEventVenue().getSerVenueMasterDetailId());
				// if (res.getTxtMessage().equalsIgnoreCase("Success")) {
				// VenueMasterDetail venueMasterDetail = (VenueMasterDetail) res.getResult();
				// entity.setVenueMasterDetail(venueMasterDetail);
				// } else {
				// dtoResult.setTxtMessage("Venue Hall Is Not Active");
				// return dtoResult;
				// }
				// } else {
				// dtoResult.setTxtMessage("Venue Hall Is Not Selected");
				// return dtoResult;
				// }
				// entity.setNumInfoFilledStatus(50);
				//
				// }

				// Set Decor Item Selections
				// *************************
				if (UtilRandomKey.isNotNull(dtoEventMasterAdminPortal.getDtoEventDecorSelections())) {

					// Deleting Existing Selections
					// if(entity.getDecorSelections() != null &&
					// !entity.getDecorSelections().isEmpty()) {
					// serviceEventDecorCategorySelection.deleteByEventMasterId(entity.getSerEventMasterId());
					// }
					if (entity.getDecorSelections() != null) {
						entity.getDecorSelections().clear();
					}

					List<EventDecorCategorySelection> decorSelections = new ArrayList<>();

					for (DtoEventDecorCategorySelection dto : dtoEventMasterAdminPortal.getDtoEventDecorSelections()) {
						EventDecorCategorySelection decorSelection = MapperEventDecorCategorySelection.toEntity(dto);
						decorSelection.setEventMaster(entity);
						// decorSelection = repositoryEventDecorCategorySelection.save(decorSelection);

						// Set property selections' back reference
						// if (decorSelection.getSelectedProperties() != null) {
						// for (EventDecorPropertySelection prop :
						// decorSelection.getSelectedProperties()) {
						// prop.setEventDecorCategorySelection(decorSelection);
						//
						// }
						// }
						if (dto.getSelectedProperties() != null && !dto.getSelectedProperties().isEmpty()) {
							if (decorSelection.getSelectedProperties() != null) {
								decorSelection.getSelectedProperties().clear();
							}

							List<EventDecorPropertySelection> newSelectedProperties = new ArrayList<>();
							for (DtoEventDecorPropertySelection property : dto.getSelectedProperties()) {
								EventDecorPropertySelection eventDecorPropertySelection = new EventDecorPropertySelection();
								eventDecorPropertySelection.setBlnIsActive(true);
								eventDecorPropertySelection.setBlnIsDeleted(false);
								eventDecorPropertySelection.setCreatedDate(UtilDateAndTime.getCurrentDate());
								eventDecorPropertySelection.setEventDecorCategorySelection(decorSelection);
								eventDecorPropertySelection.setNumPrice(property.getNumPrice());

								DecorCategoryPropertyMaster matchedMaster = decorCategoryPropertyMasterLst.stream()
										.filter(pm -> pm.getSerPropertyId().intValue() == property.getSerPropertyId()
												.intValue())
										.findFirst().orElse(null);

								DecorCategoryPropertyValue matchedValue = decorCategoryPropertyValueLst.stream()
										.filter(pv -> pv.getSerPropertyValueId().intValue() == property
												.getSerPropertyValueId().intValue())
										.findFirst().orElse(null);

								eventDecorPropertySelection.setProperty(matchedMaster);
								eventDecorPropertySelection.setSelectedValue(matchedValue);
								newSelectedProperties.add(eventDecorPropertySelection);
							}

							decorSelection.getSelectedProperties().addAll(newSelectedProperties);
						}

						// Set reference image back reference

						if (decorSelection.getUserUploadedDocuments() != null && UtilRandomKey.isNotNull(files)) {
							decorSelection.getUserUploadedDocuments().clear();
							List<EventDecorReferenceDocument> documents = new ArrayList<>();
							for (DtoEventDecorReferenceDocument dtoImg : dto.getUserUploadedDocuments()) {
								MultipartFile file = fileMap.get(dtoImg.getOriginalName());
								if (file != null) {
									String uploadPath = UtilFileStorage.saveFile(file, "UserReferenceDecor");
									EventDecorReferenceDocument doc = new EventDecorReferenceDocument();
									doc.setDocumentName(file.getName());
									doc.setOriginalName(file.getOriginalFilename());
									doc.setDocumentType(file.getContentType());
									doc.setSize(String.valueOf(file.getSize()));
									doc.setFilePath(uploadPath);
									doc.setEventDecorCategorySelection(decorSelection);
									documents.add(doc);
								}
							}
							// decorSelection.setUserUploadedDocuments(documents);
							decorSelection.getUserUploadedDocuments().addAll(documents);
						}

						decorSelections.add(decorSelection);
					}

					// entity.setDecorSelections(decorSelections);
					entity.getDecorSelections().addAll(decorSelections);
					// entity.setNumInfoFilledStatus(70);
				}

				// Set Food Menu Selection
				// ***********************
				if (dtoEventMasterAdminPortal.getFoodSelections() != null
						&& !dtoEventMasterAdminPortal.getFoodSelections().isEmpty()) {

					// Clear existing selections if any
					if (entity.getFoodSelections() != null) {
						entity.getFoodSelections().clear();
					}

					List<EventMenuFoodSelection> eventMenuFoodSelectionLst = new ArrayList<>();

					// Map<String, List<DtoMenuFoodMaster>> foodSelectionsMap =
					// dtoEventMasterAdminPortal
					// .getFoodSelections();

					List<DtoMenuFoodMaster> foodSelections = dtoEventMasterAdminPortal.getFoodSelections();

					// for (Map.Entry<String, List<DtoMenuFoodMaster>> entry :
					// foodSelectionsMap.entrySet()) {
					// String foodType = entry.getKey(); // e.g., "MainCourse", "Starter"
					// List<DtoMenuFoodMaster> foodList = entry.getValue();

					// for (DtoMenuFoodMaster dto : foodList) {
					// if (UtilRandomKey.isNotNull(dto.getSerMenuFoodId())) {
					for (DtoMenuFoodMaster dto : foodSelections) {
						if (UtilRandomKey.isNotNull(dto.getSerMenuItemId())) {

							// Find the actual MenuFoodMaster from your list
							// MenuFoodMaster menuFoodMaster = dtoMenuFoodMasterLst.stream()
							// .filter(food -> food.getSerMenuFoodId() != null && food.getSerMenuFoodId()
							// .intValue() == dto.getSerMenuFoodId().intValue())
							// .findFirst().orElse(null);

							MenuItem menuItem = menuItems.stream()
									.filter(item -> item.getSerMenuItemId() != null
											&& item.getSerMenuItemId().intValue() == dto.getSerMenuItemId().intValue())
									.findFirst().orElse(null);

							if (UtilRandomKey.isNotNull(menuItem)) {
								EventMenuFoodSelection eventMenuFoodSelection = new EventMenuFoodSelection();
								eventMenuFoodSelection.setEventMaster(entity);
								eventMenuFoodSelection.setBlnIsActive(true);
								eventMenuFoodSelection.setBlnIsApproved(true);
								eventMenuFoodSelection.setBlnIsDeleted(false);
								// eventMenuFoodSelection.setMenuFoodMaster(menuFoodMaster);
								eventMenuFoodSelection.setMenuItem(menuItem);
								eventMenuFoodSelection.setNumPrice(dto.getNumPrice());
								// eventMenuFoodSelection.setTxtFoodType(foodType); // Set from map key

								eventMenuFoodSelectionLst.add(eventMenuFoodSelection);
							} else {
								dtoResult.setTxtMessage("Food Selection Item Does Not Have Food Menu With Id: "
										+ dto.getSerMenuFoodId() + " In DB.");
								return dtoResult;
							}
						} else {
							dtoResult.setTxtMessage("Food Selection Item Does Not Have The Id OF Food Menu");
							return dtoResult;
						}
					}
					// }

					// Add all to entity
					entity.getFoodSelections().addAll(eventMenuFoodSelectionLst);
					// entity.setNumInfoFilledStatus(90);
				}
				// Set Vendor
				// **********
				if (UtilRandomKey.isNotNull(dtoEventMasterAdminPortal.getSerVendorId())) {
					VendorMaster vendorMaster = serviceVendorMaster.getByPK(dtoEventMasterAdminPortal.getSerVendorId());
					if (UtilRandomKey.isNotNull(vendorMaster)) {

						entity.setVendorMaster(vendorMaster);
					} else {
						dtoResult.setTxtMessage("External Supplier Not Found Against Id: "
								+ dtoEventMasterAdminPortal.getSerVendorId());
						return dtoResult;
					}
					// entity.setNumInfoFilledStatus(100);
				}

				// Setting Event Quoted Price
				// **************************
				eventBudget = serviceEventBudget.getEventBudgetByEventId(entity.getSerEventMasterId());

				if (eventBudget != null) {
					if (eventBudget.getNumPaidAmount() != null
							&& (eventBudget.getNumPaidAmount().compareTo(BigDecimal.ZERO) == 1
									|| (dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus() != null
											&& dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumPaidAmount()
													.compareTo(BigDecimal.ZERO) == 1))) {
						eventBudget.setTxtStatus("Confirmed");
						if (dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus() != null && dtoEventMasterAdminPortal
								.getDtoEventQuoteAndStatus().getNumPaidAmount().compareTo(BigDecimal.ZERO) == 1) {
							eventBudget.setNumPaidAmount(
									dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumPaidAmount());
						}

					} else if (dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus() != null
							&& dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumQuotedPrice() != null
							&& dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumQuotedPrice()
									.compareTo(BigDecimal.ZERO) == 1) {
						eventBudget.setTxtStatus("Quoted");
						eventBudget.setNumQuotedPrice(
								dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumQuotedPrice());
						eventBudget.setNumPaidAmount(BigDecimal.ZERO);

					} else {
						eventBudget.setTxtStatus("Enquiry");
						eventBudget.setNumQuotedPrice(BigDecimal.ZERO);
						eventBudget.setNumPaidAmount(BigDecimal.ZERO);

					}

				} else {
					eventBudget = new EventBudget();

					if (dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus() != null && dtoEventMasterAdminPortal
							.getDtoEventQuoteAndStatus().getNumPaidAmount().compareTo(BigDecimal.ZERO) == 1) {
						eventBudget.setTxtStatus("Confirmed");
						eventBudget.setNumPaidAmount(
								dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumPaidAmount());
						if (dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumQuotedPrice() != null
								&& dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumQuotedPrice()
										.compareTo(BigDecimal.ZERO) == 1) {
							eventBudget.setNumQuotedPrice(
									dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumQuotedPrice());
						}

					} else if (dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus() != null
							&& dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumQuotedPrice() != null
							&& dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumQuotedPrice()
									.compareTo(BigDecimal.ZERO) == 1) {
						eventBudget.setTxtStatus("Quoted");
						eventBudget.setNumQuotedPrice(
								dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumQuotedPrice());
						eventBudget.setNumPaidAmount(BigDecimal.ZERO);

					} else {
						eventBudget.setTxtStatus("Enquiry");
						eventBudget.setNumQuotedPrice(BigDecimal.ZERO);
						eventBudget.setNumPaidAmount(BigDecimal.ZERO);

					}

				}

			} else {

				// ******************************************
				// *************** Create New ***************
				// ******************************************

				blnIsNewEvent = true;
				entity = MapperEventMaster.dtoEventMasterAdminPortalToEntity(dtoEventMasterAdminPortal);
				entity.setEventRunningOrder(null);
				entity.setEventType(null);
				entity.setDecorSelections(null);
				entity.setFoodSelections(null);
				entity = repositoryEventMaster.save(entity);
				entity.setCreatedBy(ServiceCurrentUser.getCurrentUserId());

				// if (UtilRandomKey.isNull(entity.getNumInfoFilledStatus())) {
				// entity.setNumInfoFilledStatus(0);
				// }

				// Set customer
				// ************
				if (UtilRandomKey.isNotNull(dtoEventMasterAdminPortal.getSerCustId())) {
					CustomerMaster customer = serviceCustomerMaster.getByPK(dtoEventMasterAdminPortal.getSerCustId());
					if (UtilRandomKey.isNull(customer)) {
						dtoResult
								.setTxtMessage("Customer Not Foound For Id" + dtoEventMasterAdminPortal.getSerCustId());
						return dtoResult;
					}
					entity.setCustomerMaster(customer);
					// entity.setNumInfoFilledStatus(10);

				}

				// Set event type
				// **************
				if (UtilRandomKey.isNotNull(dtoEventMasterAdminPortal.getSerEventTypeId())) {
					EventType eventType = serviceEventType.getByPK(dtoEventMasterAdminPortal.getSerEventTypeId());
					if (UtilRandomKey.isNull(eventType)) {
						dtoResult.setTxtMessage(
								"Event Type Not Foound For Id" + dtoEventMasterAdminPortal.getSerEventTypeId());
						return dtoResult;
					}
					entity.setEventType(eventType);
				}

				// Set optional event running order
				// ********************************
				if (dtoEventMasterAdminPortal.getDtoEventRunningOrder() != null) {
					EventRunningOrder runningOrder = new EventRunningOrder();
					runningOrder = MapperEventRunningOrder
							.toEntity(dtoEventMasterAdminPortal.getDtoEventRunningOrder());
					runningOrder = repositoryEventRunningOrder.save(runningOrder);
					entity.setEventRunningOrder(runningOrder);
					// entity.setNumInfoFilledStatus(30);
				}

				// Set Venue Master
				// ****************

				// This is for which you only need to specify which venu is selected
				if (UtilRandomKey.isNotNull(dtoEventMasterAdminPortal.getSerVenueMasterId())) {
					VenueMaster venueMaster = serviceVenueMaster
							.getByPK(dtoEventMasterAdminPortal.getSerVenueMasterId());
					if (UtilRandomKey.isNull(venueMaster)) {
						dtoResult.setTxtMessage(
								"Venue Not Found For Id: " + dtoEventMasterAdminPortal.getSerVenueMasterId());
						return dtoResult;
					}
					entity.setVenueMaster(venueMaster);
					// entity.setNumInfoFilledStatus(50);
				}

				// //This is For when you need to save which hall of the venu was selected
				// if (UtilRandomKey.isNotNull(dtoEventMasterAdminPortal.getDtoEventVenue())) {
				// if
				// (UtilRandomKey.isNotNull(dtoEventMasterAdminPortal.getDtoEventVenue().getSerVenueMasterDetailId()))
				// {
				// DtoResult res = serviceVenueMaster.getVenueDetailByVenueMasterDetailId(
				// dtoEventMasterAdminPortal.getDtoEventVenue().getSerVenueMasterDetailId());
				// if (res.getTxtMessage().equalsIgnoreCase("Success")) {
				// VenueMasterDetail venueMasterDetail = (VenueMasterDetail) res.getResult();
				// entity.setVenueMasterDetail(venueMasterDetail);
				// } else {
				// dtoResult.setTxtMessage("Venue Hall Is Not Active");
				// return dtoResult;
				// }
				// } else {
				// dtoResult.setTxtMessage("Venue Hall Is Not Selected");
				// return dtoResult;
				// }
				// entity.setNumInfoFilledStatus(50);
				//
				// }

				// Set Decore Item Selections
				// **************************
				if (UtilRandomKey.isNotNull(dtoEventMasterAdminPortal.getDtoEventDecorSelections())) {
					List<EventDecorCategorySelection> decorSelections = new ArrayList<>();

					for (DtoEventDecorCategorySelection dto : dtoEventMasterAdminPortal.getDtoEventDecorSelections()) {
						EventDecorCategorySelection decorSelection = MapperEventDecorCategorySelection.toEntity(dto);
						decorSelection.setEventMaster(entity);

						// if (decorSelection.getSelectedProperties() != null) {
						// decorSelection.getSelectedProperties()
						// .forEach(p -> p.setEventDecorCategorySelection(decorSelection));
						// }

						if (dto.getSelectedProperties() != null && !dto.getSelectedProperties().isEmpty()) {
							if (decorSelection.getSelectedProperties() != null) {
								decorSelection.getSelectedProperties().clear();
							}

							List<EventDecorPropertySelection> newSelectedProperties = new ArrayList<>();
							for (DtoEventDecorPropertySelection property : dto.getSelectedProperties()) {
								EventDecorPropertySelection eventDecorPropertySelection = new EventDecorPropertySelection();
								eventDecorPropertySelection.setBlnIsActive(true);
								eventDecorPropertySelection.setBlnIsDeleted(false);
								eventDecorPropertySelection.setCreatedDate(UtilDateAndTime.getCurrentDate());
								eventDecorPropertySelection.setEventDecorCategorySelection(decorSelection);
								eventDecorPropertySelection.setNumPrice(property.getNumPrice());

								DecorCategoryPropertyMaster matchedMaster = decorCategoryPropertyMasterLst.stream()
										.filter(pm -> pm.getSerPropertyId().intValue() == property.getSerPropertyId()
												.intValue())
										.findFirst().orElse(null);

								DecorCategoryPropertyValue matchedValue = decorCategoryPropertyValueLst.stream()
										.filter(pv -> pv.getSerPropertyValueId().intValue() == property
												.getSerPropertyValueId().intValue())
										.findFirst().orElse(null);

								eventDecorPropertySelection.setProperty(matchedMaster);
								eventDecorPropertySelection.setSelectedValue(matchedValue);
								newSelectedProperties.add(eventDecorPropertySelection);
							}

							decorSelection.getSelectedProperties().addAll(newSelectedProperties);
						}

						// Set reference image back reference

						if (decorSelection.getUserUploadedDocuments() != null && UtilRandomKey.isNotNull(files)) {
							decorSelection.getUserUploadedDocuments().clear();
							List<EventDecorReferenceDocument> documents = new ArrayList<>();
							for (DtoEventDecorReferenceDocument dtoImg : dto.getUserUploadedDocuments()) {
								MultipartFile file = fileMap.get(dtoImg.getOriginalName());
								if (file != null) {
									String uploadPath = UtilFileStorage.saveFile(file, "UserReferenceDecor");
									EventDecorReferenceDocument doc = new EventDecorReferenceDocument();
									doc.setDocumentName(file.getName());
									doc.setOriginalName(file.getOriginalFilename());
									doc.setDocumentType(file.getContentType());
									doc.setSize(String.valueOf(file.getSize()));
									doc.setFilePath(uploadPath);
									doc.setEventDecorCategorySelection(decorSelection);
									documents.add(doc);
								}
							}
							// decorSelection.setUserUploadedDocuments(documents);
							decorSelection.getUserUploadedDocuments().addAll(documents);
						}

						decorSelections.add(decorSelection);
					}

					entity.setDecorSelections(decorSelections);
					// entity.setNumInfoFilledStatus(70);
				}

				// Set Food Menu Selection
				// ***********************
				if (UtilRandomKey.isNotNull(dtoEventMasterAdminPortal.getFoodSelections())
						&& !dtoEventMasterAdminPortal.getFoodSelections().isEmpty()) {

					List<EventMenuFoodSelection> eventMenuFoodSelectionLst = new ArrayList<>();
					// Map<String, List<DtoMenuFoodMaster>> foodSelectionsMap =
					// dtoEventMasterAdminPortal
					// .getFoodSelections();
					List<DtoMenuFoodMaster> foodSelections = dtoEventMasterAdminPortal.getFoodSelections();

					// for (Map.Entry<String, List<DtoMenuFoodMaster>> entry :
					// foodSelectionsMap.entrySet()) {
					// String foodType = entry.getKey(); // Example: "MainCourse", "Starter"
					// List<DtoMenuFoodMaster> foodList = entry.getValue();

					// for (DtoMenuFoodMaster dto : foodList) {
					// if (UtilRandomKey.isNotNull(dto.getSerMenuFoodId())) {
					//
					for (DtoMenuFoodMaster dto : foodSelections) {
						if (UtilRandomKey.isNotNull(dto.getSerMenuItemId())) {

							// MenuFoodMaster menuFoodMaster = dtoMenuFoodMasterLst.stream()
							// .filter(food -> food.getSerMenuFoodId() != null && food.getSerMenuFoodId()
							// .intValue() == dto.getSerMenuFoodId().intValue())
							// .findFirst().orElse(null);

							MenuItem menuItem = menuItems.stream()
									.filter(item -> item.getSerMenuItemId() != null
											&& item.getSerMenuItemId().intValue() == dto.getSerMenuItemId().intValue())
									.findFirst().orElse(null);

							if (UtilRandomKey.isNotNull(menuItem)) {
								EventMenuFoodSelection eventMenuFoodSelection = new EventMenuFoodSelection();
								eventMenuFoodSelection.setEventMaster(entity);
								eventMenuFoodSelection.setBlnIsActive(true);
								eventMenuFoodSelection.setBlnIsApproved(true);
								eventMenuFoodSelection.setBlnIsDeleted(false);
								// eventMenuFoodSelection.setMenuFoodMaster(menuFoodMaster);
								eventMenuFoodSelection.setMenuItem(menuItem);
								eventMenuFoodSelection.setNumPrice(dto.getNumPrice());
								// eventMenuFoodSelection.setTxtFoodType(foodType); // Use map key as food type
								// (can
								// replace with getFoodName() if
								// preferred)

								eventMenuFoodSelectionLst.add(eventMenuFoodSelection);
							} else {
								dtoResult.setTxtMessage("Food Selection Item Does Not Have Food Menu With Id: "
										+ dto.getSerMenuItemId() + " In DB.");
								return dtoResult;
							}
						} else {
							dtoResult.setTxtMessage("Food Selection Item Does Not Have The Id OF Food Menu");
							return dtoResult;
						}
					}
					// }

					String result = serviceEventMenuFoodSelection.saveAll(eventMenuFoodSelectionLst);
					if (!result.equalsIgnoreCase("Success")) {
						dtoResult.setTxtMessage(result);
						return dtoResult;
					}

					// entity.setNumInfoFilledStatus(90);
				}

				// Set Vendor
				// **********
				if (UtilRandomKey.isNotNull(dtoEventMasterAdminPortal.getSerVendorId())) {
					VendorMaster vendorMaster = serviceVendorMaster.getByPK(dtoEventMasterAdminPortal.getSerVendorId());
					if (UtilRandomKey.isNotNull(vendorMaster)) {
						entity.setVendorMaster(vendorMaster);
						// entity.setNumInfoFilledStatus(entity.getNumInfoFilledStatus() + 1);
					} else {
						dtoResult.setTxtMessage("External Supplier Not Found Against Id: "
								+ dtoEventMasterAdminPortal.getSerVendorId());
						return dtoResult;
					}
				}
				// entity.setNumInfoFilledStatus(100);

				// Generate event master code
				String code = generateNextEventMasterCode();
				entity.setTxtEventMasterCode(code);

				// Setting Event Quoted Price
				// **************************

				eventBudget = new EventBudget();
				if (dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus() != null && dtoEventMasterAdminPortal
						.getDtoEventQuoteAndStatus().getNumPaidAmount().compareTo(BigDecimal.ZERO) == 1) {
					eventBudget.setTxtStatus("Confirmed");
					eventBudget
							.setNumPaidAmount(dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumPaidAmount());
					if (dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumQuotedPrice() != null
							&& dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumQuotedPrice()
									.compareTo(BigDecimal.ZERO) == 1) {
						eventBudget.setNumQuotedPrice(
								dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumQuotedPrice());
					}

				} else if (dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus() != null
						&& dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumQuotedPrice() != null
						&& dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumQuotedPrice()
								.compareTo(BigDecimal.ZERO) == 1) {
					eventBudget.setTxtStatus("Quoted");
					eventBudget.setNumQuotedPrice(
							dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumQuotedPrice());
					eventBudget.setNumPaidAmount(BigDecimal.ZERO);

				} else {
					eventBudget.setTxtStatus("Enquiry");
					eventBudget.setNumQuotedPrice(BigDecimal.ZERO);
					eventBudget.setNumPaidAmount(BigDecimal.ZERO);

				}

			}

			// ****** Setting Event Decor Extras ******
			if (entity.getExtrasSelections() != null) {
				entity.getExtrasSelections().clear();
			}

			if (UtilRandomKey.isNotNull(dtoEventMasterAdminPortal.getExtrasSelections())
					&& !dtoEventMasterAdminPortal.getExtrasSelections().isEmpty()) {
				// serviceEventDecorExtrasSelection.deleteByEventMasterId(entity.getSerEventMasterId());

				List<EventDecorExtrasSelection> newSelections = new ArrayList<>();
				for (DtoEventDecorExtrasSelection dto : dtoEventMasterAdminPortal.getExtrasSelections()) {
					EventDecorExtrasSelection selection = new EventDecorExtrasSelection();
					selection.setTxtDynamicProperty1(dto.getTxtDynamicProperty1());
					selection.setTxtDynamicProperty2(dto.getTxtDynamicProperty2());
					selection.setNumPrice(dto.getNumPrice());
					selection.setEventMaster(entity);
					if (dto.getSerExtrasId() != null) {
						selection.setDecorExtrasMaster(
								serviceDecorExtrasMaster.getByIdAndNotDeleted(dto.getSerExtrasId()));
					}
					if (dto.getSerExtraOptionId() != null) {
						selection.setDecorExtrasOption(
								serviceDecorExtrasOption.getByIdAndNotDeleted(dto.getSerExtraOptionId()));
					}

					// selection = serviceEventDecorExtrasSelection.save(selection);
					newSelections.add(selection);
				}
//				entity.setExtrasSelections(newSelections);
//				entity.setNumInfoFilledStatus(entity.getNumInfoFilledStatus() + 1);
				if(entity.getExtrasSelections() == null) {
					entity.setExtrasSelections(newSelections);
				}else {
					entity.getExtrasSelections().addAll(newSelections);
				}
				
			}

			entity.setNumInfoFilledStatus(getEventCompletionPercentage(entity));
			entity = repositoryEventMaster.save(entity);
			if (eventBudget != null) {
				eventBudget.setEventMaster(entity);
				serviceEventBudget.save(eventBudget);
			}

			// ***** Sending Notification Of New Customer Registration *****
			if (blnIsNewEvent) {
				this.sendNewEventRegistrationNotification(
						entity.getEventType() != null ? entity.getEventType().getTxtEventTypeName() : "",
						entity.getTxtEventMasterCode() != null ? entity.getTxtEventMasterCode() : "",
						dtoEventMasterAdminPortal.getDteEventDate() != null
								? dtoEventMasterAdminPortal.getDteEventDate()
								: "",
						entity.getSerEventMasterId());
			}

			if (this.isEventRegistrationCompleted(entity)) {
				UserMaster userMaster = ServiceCurrentUser.getCurrentUser();
				if (userMaster != null) {
					serviceEmailSender.sendEventRegistrationEmail(userMaster.getTxtEmail(), userMaster.getTxtName(),
							entity.getTxtEventMasterCode(), entity.getEventType().getTxtEventTypeName(),
							entity.getDteEventDate());

					// ***** Send Email To All Admin Users ********
					serviceEmailSender.sendEventRegistrationEmailToAdminUsers(userMaster.getTxtName(),
							entity.getTxtEventMasterCode(), entity.getEventType().getTxtEventTypeName(),
							entity.getDteEventDate());

					dtoResult.setTxtMessage(entity.getEventType().getTxtEventTypeName()
							+ " Event Has Been Registered. A Confirmation Email Has Been Sent To Your Registered Email.");
				} else {
					dtoResult.setTxtMessage("Success");

				}
			} else {
				dtoResult.setTxtMessage("Success");
			}

			DtoEventMasterAdminPortal dtoEvent = this.getEventByIdAdminPortal(entity.getSerEventMasterId());
			dtoResult.setResult(dtoEvent);
			return dtoResult;
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			dtoResult.setTxtMessage("Failure");
			return dtoResult;
		}
	}

	private DtoEventMasterAdminPortal getEventByIdAdminPortal(Integer serEventId) {
		DtoResult dtoResult = new DtoResult();

		try {

			Optional<EventMaster> optEvent = repositoryEventMaster.findById(serEventId);
			if (UtilRandomKey.isNotNull(optEvent)) {
				EventMaster event = optEvent.get();
				DtoEventMasterAdminPortal dto = MapperEventMaster.toDtoEventMasterAdminPortal(event);

				// Fetching Event Venue Detail
				// ***********************************
				if (UtilRandomKey.isNotNull(event.getVenueMasterDetail())) {
					DtoResult res = serviceVenueMaster
							.getVenueByVenueMasterDetailId(event.getVenueMasterDetail().getSerVenueMasterDetailId());
					if (UtilRandomKey.isNotNull(res) && res.getTxtMessage().equalsIgnoreCase("Success")) {
						VenueMaster venueMaster = (VenueMaster) res.getResult();
						DtoEventVenue dtoEventVenue = new DtoEventVenue();
						dtoEventVenue.setSerVenueMasterId(venueMaster.getSerVenueMasterId());
						dtoEventVenue.setTxtVenueCode(venueMaster.getTxtVenueCode());
						dtoEventVenue.setTxtVenueName(venueMaster.getTxtVenueName());
						dtoEventVenue
								.setSerVenueMasterDetailId(event.getVenueMasterDetail().getSerVenueMasterDetailId());
						dtoEventVenue.setTxtHallCode(event.getVenueMasterDetail().getTxtHallCode());
						dtoEventVenue.setTxtHallName(event.getVenueMasterDetail().getTxtHallName());
					}
				}

				// Fetching Decor
				// ***********************************
				List<DtoEventDecorCategorySelection> eventDecorCategorySelections = serviceEventDecorCategorySelection
						.getSelectionsWithChosenValues(dto.getSerEventMasterId());
				dto.setDtoEventDecorSelections(eventDecorCategorySelections);

				// Fetching Event Menu Food Selection
				// ***********************************

				List<EventMenuFoodSelection> eventMenuFoodSelections = serviceEventMenuFoodSelection
						.getByEventMasterId(dto.getSerEventMasterId());

				// Map<String, List<DtoMenuFoodMaster>> foodSelectionsMap = new HashMap<>();
				List<DtoMenuFoodMaster> foodSelections = new ArrayList<>();
				if (UtilRandomKey.isNotNull(eventMenuFoodSelections)) {
					for (EventMenuFoodSelection entity : eventMenuFoodSelections) {
						// if (entity.getMenuFoodMaster() != null) {
						if (entity.getMenuItem() != null) {
							DtoMenuFoodMaster dtoMenuFoodMaster = new DtoMenuFoodMaster();
							MenuItem menuItem = entity.getMenuItem();

							// dtoMenuFoodMaster.setSerMenuFoodId(foodMaster.getSerMenuFoodId());
							// dtoMenuFoodMaster.setTxtMenuFoodCode(foodMaster.getTxtMenuFoodCode());
							// dtoMenuFoodMaster.setTxtMenuFoodName(foodMaster.getTxtMenuFoodName());
							// dtoMenuFoodMaster.setBlnIsMainCourse(foodMaster.getBlnIsMainCourse());
							// dtoMenuFoodMaster.setBlnIsAppetiser(foodMaster.getBlnIsAppetiser());
							// dtoMenuFoodMaster.setBlnIsStarter(foodMaster.getBlnIsStarter());
							// dtoMenuFoodMaster.setBlnIsSaladAndCondiment(foodMaster.getBlnIsSaladAndCondiment());
							// dtoMenuFoodMaster.setBlnIsDessert(foodMaster.getBlnIsDessert());
							// dtoMenuFoodMaster.setBlnIsDrink(foodMaster.getBlnIsDrink());
							dtoMenuFoodMaster.setBlnIsActive(menuItem.getBlnIsActive());
							dtoMenuFoodMaster.setSerMenuItemId(menuItem.getSerMenuItemId());
							dtoMenuFoodMaster.setTxtName(menuItem.getTxtName());
							dtoMenuFoodMaster.setTxtCode(menuItem.getTxtCode());
							dtoMenuFoodMaster.setTxtDescription(menuItem.getTxtDescription());
							dtoMenuFoodMaster.setNumPrice(entity.getNumPrice());

							// String foodType = getFoodType(foodMaster);

							// if (!foodSelectionsMap.containsKey(foodType)) {
							// foodSelectionsMap.put(foodType, new ArrayList<>());
							// }
							// foodSelectionsMap.get(foodType).add(dtoMenuFoodMaster);
							foodSelections.add(dtoMenuFoodMaster);
						}
					}
				}

				// dto.setFoodSelections(foodSelectionsMap);
				dto.setFoodSelections(foodSelections);

				// Fetching Event Extras Selection
				// ***********************************

				List<EventDecorExtrasSelection> eventDecorExtrasSelection = serviceEventDecorExtrasSelection
						.getByEventMasterId(dto.getSerEventMasterId());
				List<DtoEventDecorExtrasSelection> dtoEventDecorExtrasSelections = new ArrayList<>();
				if (UtilRandomKey.isNotNull(eventDecorExtrasSelection)) {
					for (EventDecorExtrasSelection entity : eventDecorExtrasSelection) {
						DtoEventDecorExtrasSelection dtoEventDecorExtrasSelection = new DtoEventDecorExtrasSelection();
						dtoEventDecorExtrasSelection.setSerExtrasSelectionId(entity.getSerExtrasSelectionId());
						dtoEventDecorExtrasSelection.setTxtDynamicProperty1(entity.getTxtDynamicProperty1());
						dtoEventDecorExtrasSelection.setTxtDynamicProperty2(entity.getTxtDynamicProperty2());
						dtoEventDecorExtrasSelection.setNumPrice(entity.getNumPrice());
						if (entity.getDecorExtrasMaster() != null) {
							dtoEventDecorExtrasSelection.setSerExtrasId(entity.getDecorExtrasMaster().getSerExtrasId());
							dtoEventDecorExtrasSelection
									.setTxtExtrasCode(entity.getDecorExtrasMaster().getTxtExtrasCode());
							dtoEventDecorExtrasSelection
									.setTxtExtrasName(entity.getDecorExtrasMaster().getTxtExtrasName());
						}

						if (entity.getDecorExtrasOption() != null) {
							dtoEventDecorExtrasSelection
									.setSerExtraOptionId(entity.getDecorExtrasOption().getSerExtraOptionId());
							dtoEventDecorExtrasSelection
									.setTxtOptionCode(entity.getDecorExtrasOption().getTxtOptionCode());
							dtoEventDecorExtrasSelection
									.setTxtOptionName(entity.getDecorExtrasOption().getTxtOptionName());
						}

						dtoEventDecorExtrasSelections.add(dtoEventDecorExtrasSelection);
					}
				}
				dto.setExtrasSelections(dtoEventDecorExtrasSelections);

				// Fetching Event Quoted Price
				// ***************************
				EventBudget eventBudget = serviceEventBudget.getEventBudgetByEventId(serEventId);
				if (eventBudget != null) {
					DtoEventQuoteAndStatus dtoEventQuoteAndStatus = new DtoEventQuoteAndStatus();
					dtoEventQuoteAndStatus.setNumQuotedPrice(eventBudget.getNumQuotedPrice());
					dtoEventQuoteAndStatus.setNumPaidAmount(eventBudget.getNumPaidAmount());
					dtoEventQuoteAndStatus.setTxtStatus(eventBudget.getTxtStatus());
					dto.setDtoEventQuoteAndStatus(dtoEventQuoteAndStatus);
				}

				return dto;

			} else {
				dtoResult.setTxtMessage("No Data Found In System");
				return null;
			}

		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			dtoResult.setTxtMessage("Error occurred: " + e.getMessage());
			return null;
		}

	}

	@Override
	public DtoResult getAllEventsAdminPortal() {
		DtoResult dtoResult = new DtoResult();

		try {

			List<EventMaster> events = repositoryEventMaster.getAllNotDeleted();
			if (UtilRandomKey.isNotNull(events)) {

				List<DtoEventMasterAdminPortal> dtoEventMasterLst = new ArrayList<>();
				for (EventMaster event : events) {

					DtoEventMasterAdminPortal dto = MapperEventMaster.toDtoEventMasterAdminPortal(event);

					// Fetching Event Venue Detail
					// ***********************************
					if (UtilRandomKey.isNotNull(event.getVenueMasterDetail())) {
						DtoResult res = serviceVenueMaster.getVenueByVenueMasterDetailId(
								event.getVenueMasterDetail().getSerVenueMasterDetailId());
						if (UtilRandomKey.isNotNull(res) && res.getTxtMessage().equalsIgnoreCase("Success")) {
							VenueMaster venueMaster = (VenueMaster) res.getResult();
							DtoEventVenue dtoEventVenue = new DtoEventVenue();
							dtoEventVenue.setSerVenueMasterId(venueMaster.getSerVenueMasterId());
							dtoEventVenue.setTxtVenueCode(venueMaster.getTxtVenueCode());
							dtoEventVenue.setTxtVenueName(venueMaster.getTxtVenueName());
							dtoEventVenue.setSerVenueMasterDetailId(
									event.getVenueMasterDetail().getSerVenueMasterDetailId());
							dtoEventVenue.setTxtHallCode(event.getVenueMasterDetail().getTxtHallCode());
							dtoEventVenue.setTxtHallName(event.getVenueMasterDetail().getTxtHallName());
						}
					}

					// Fetching Decor
					// ***********************************
					List<DtoEventDecorCategorySelection> eventDecorCategorySelections = serviceEventDecorCategorySelection
							.getSelectionsWithChosenValues(dto.getSerEventMasterId());
					dto.setDtoEventDecorSelections(eventDecorCategorySelections);

					// Fetching Event Menu Food Selection
					// ***********************************

					// List<EventMenuFoodSelection> eventMenuFoodSelections =
					// serviceEventMenuFoodSelection
					// .getByEventMasterId(dto.getSerEventMasterId());
					//
					// Map<String, List<DtoMenuFoodMaster>> foodSelectionsMap = new HashMap<>();
					//
					// if (UtilRandomKey.isNotNull(eventMenuFoodSelections)) {
					// for (EventMenuFoodSelection entity : eventMenuFoodSelections) {
					// if (entity.getMenuFoodMaster() != null) {
					// DtoMenuFoodMaster dtoMenuFoodMaster = new DtoMenuFoodMaster();
					// MenuFoodMaster foodMaster = entity.getMenuFoodMaster();
					//
					// dtoMenuFoodMaster.setSerMenuFoodId(foodMaster.getSerMenuFoodId());
					// dtoMenuFoodMaster.setTxtMenuFoodCode(foodMaster.getTxtMenuFoodCode());
					// dtoMenuFoodMaster.setTxtMenuFoodName(foodMaster.getTxtMenuFoodName());
					// dtoMenuFoodMaster.setBlnIsMainCourse(foodMaster.getBlnIsMainCourse());
					// dtoMenuFoodMaster.setBlnIsAppetiser(foodMaster.getBlnIsAppetiser());
					// dtoMenuFoodMaster.setBlnIsStarter(foodMaster.getBlnIsStarter());
					// dtoMenuFoodMaster.setBlnIsSaladAndCondiment(foodMaster.getBlnIsSaladAndCondiment());
					// dtoMenuFoodMaster.setBlnIsDessert(foodMaster.getBlnIsDessert());
					// dtoMenuFoodMaster.setBlnIsDrink(foodMaster.getBlnIsDrink());
					// dtoMenuFoodMaster.setBlnIsActive(foodMaster.getBlnIsActive());
					//
					// String foodType = getFoodType(foodMaster);
					//
					// if (!foodSelectionsMap.containsKey(foodType)) {
					// foodSelectionsMap.put(foodType, new ArrayList<>());
					// }
					// foodSelectionsMap.get(foodType).add(dtoMenuFoodMaster);
					// }
					// }
					// }
					//
					// dto.setFoodSelections(foodSelectionsMap);

					// Fetching Event Extras Selection
					// ***********************************

					List<EventDecorExtrasSelection> eventDecorExtrasSelection = serviceEventDecorExtrasSelection
							.getByEventMasterId(dto.getSerEventMasterId());
					List<DtoEventDecorExtrasSelection> dtoEventDecorExtrasSelections = new ArrayList<>();
					if (UtilRandomKey.isNotNull(eventDecorExtrasSelection)) {
						for (EventDecorExtrasSelection entity : eventDecorExtrasSelection) {
							DtoEventDecorExtrasSelection dtoEventDecorExtrasSelection = new DtoEventDecorExtrasSelection();
							dtoEventDecorExtrasSelection.setSerExtrasSelectionId(entity.getSerExtrasSelectionId());
							dtoEventDecorExtrasSelection.setTxtDynamicProperty1(entity.getTxtDynamicProperty1());
							dtoEventDecorExtrasSelection.setTxtDynamicProperty2(entity.getTxtDynamicProperty2());
							dtoEventDecorExtrasSelection.setNumPrice(entity.getNumPrice());
							if (entity.getDecorExtrasMaster() != null) {
								dtoEventDecorExtrasSelection
										.setSerExtrasId(entity.getDecorExtrasMaster().getSerExtrasId());
								dtoEventDecorExtrasSelection
										.setTxtExtrasCode(entity.getDecorExtrasMaster().getTxtExtrasCode());
								dtoEventDecorExtrasSelection
										.setTxtExtrasName(entity.getDecorExtrasMaster().getTxtExtrasName());
							}

							if (entity.getDecorExtrasOption() != null) {
								dtoEventDecorExtrasSelection
										.setSerExtraOptionId(entity.getDecorExtrasOption().getSerExtraOptionId());
								dtoEventDecorExtrasSelection
										.setTxtOptionCode(entity.getDecorExtrasOption().getTxtOptionCode());
								dtoEventDecorExtrasSelection
										.setTxtOptionName(entity.getDecorExtrasOption().getTxtOptionName());
							}

							dtoEventDecorExtrasSelections.add(dtoEventDecorExtrasSelection);
						}
					}
					dto.setExtrasSelections(dtoEventDecorExtrasSelections);

					// Fetching Event Quoted Price
					// ***************************
					EventBudget eventBudget = serviceEventBudget.getEventBudgetByEventId(event.getSerEventMasterId());
					if (eventBudget != null) {
						DtoEventQuoteAndStatus dtoEventQuoteAndStatus = new DtoEventQuoteAndStatus();
						dtoEventQuoteAndStatus.setNumQuotedPrice(eventBudget.getNumQuotedPrice());
						dtoEventQuoteAndStatus.setNumPaidAmount(eventBudget.getNumPaidAmount());
						dtoEventQuoteAndStatus.setTxtStatus(eventBudget.getTxtStatus());
						dto.setDtoEventQuoteAndStatus(dtoEventQuoteAndStatus);
					}

					dtoEventMasterLst.add(dto);

				}

				dtoResult.setTxtMessage("Success");
				dtoResult.setResulList(new ArrayList<>(dtoEventMasterLst));
				return dtoResult;

			} else {
				dtoResult.setTxtMessage("No Data Found In System");
				return dtoResult;
			}

		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			dtoResult.setTxtMessage("Error occurred: " + e.getMessage());
			return dtoResult;
		}

	}

	public Page<DtoEventMasterTableView> search(DtoEventMasterSearch dto) {
		// enforce sane defaults and caps
		int page = dto.getPage() != null && dto.getPage() >= 0 ? dto.getPage() : 0;
		int size = dto.getSize() != null && dto.getSize() > 0 ? Math.min(dto.getSize(), 250) : 20;
		String sortBy = dto.getSortBy() != null ? dto.getSortBy() : "dteEventDate";
		Sort.Direction dir = "ASC".equalsIgnoreCase(dto.getSortDir()) ? Sort.Direction.ASC : Sort.Direction.DESC;

		Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));

		Specification<EventMaster> spec = SepecificationsEventMaster.fromDto(dto);

		Page<EventMaster> pageResult = repositoryEventMaster.findAll(spec, pageable);

		// Map entity -> DtoEventMasterTableView
		Page<DtoEventMasterTableView> mapped = pageResult.map(em -> {
			DtoEventMasterTableView tv = new DtoEventMasterTableView();
			tv.setSerEventMasterId(em.getSerEventMasterId());
			tv.setTxtEventMasterCode(em.getTxtEventMasterCode());
			tv.setTxtEventMasterName(em.getTxtEventMasterName());
			tv.setDteEventDate(UtilDateAndTime.mmddyyyyDateToString(em.getDteEventDate()));
			if (em.getCustomerMaster() != null) {
				tv.setSerCustId(em.getCustomerMaster().getSerCustId());
				tv.setTxtCustCode(em.getCustomerMaster().getTxtCustCode());
				tv.setTxtCustName(em.getCustomerMaster().getTxtCustName());
			}
			if (em.getEventType() != null) {
				tv.setSerEventTypeId(em.getEventType().getSerEventTypeId());
				tv.setTxtEventTypeCode(em.getEventType().getTxtEventTypeCode());
				tv.setTxtEventTypeName(em.getEventType().getTxtEventTypeName());
			}
			if (em.getVenueMaster() != null) {
				tv.setSerVenueMasterId(em.getVenueMaster().getSerVenueMasterId());
				tv.setTxtVenueCode(em.getVenueMaster().getTxtVenueCode());
				tv.setTxtVenueName(em.getVenueMaster().getTxtVenueName());
			}
			if (em.getVendorMaster() != null) {
				tv.setSerVendorId(em.getVendorMaster().getSerVendorId());
				tv.setTxtVendorCode(em.getVendorMaster().getTxtVendorCode());
				tv.setTxtVendorName(em.getVendorMaster().getTxtVendorName());
			}
			return tv;
		});

		return mapped;
	}

	public Page<EventMaster> searchEntity(DtoEventMasterSearch dto) {
		// enforce sane defaults and caps
		int page = dto.getPage() != null && dto.getPage() >= 0 ? dto.getPage() : 0;
		int size = dto.getSize() != null && dto.getSize() > 0 ? Math.min(dto.getSize(), 250) : 20;
		String sortBy = dto.getSortBy() != null ? dto.getSortBy() : "dteEventDate";
		Sort.Direction dir = "ASC".equalsIgnoreCase(dto.getSortDir()) ? Sort.Direction.ASC : Sort.Direction.DESC;

		Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));

		Specification<EventMaster> spec = SepecificationsEventMaster.fromDto(dto);

		Page<EventMaster> pageResult = repositoryEventMaster.findAll(spec, pageable);

		return pageResult;
	}

	@Override
	public Page<DtoEventMasterTableView> searchByBudgetStatus(String status, int page, int size) {
		Specification<EventMaster> spec = SepecificationsEventMaster.hasBudgetStatus(status);

		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dteEventDate"));

		Page<EventMaster> pageResult = repositoryEventMaster.findAll(spec, pageable);

		return pageResult.map(em -> {
			DtoEventMasterTableView tv = new DtoEventMasterTableView();
			tv.setSerEventMasterId(em.getSerEventMasterId());
			tv.setTxtEventMasterCode(em.getTxtEventMasterCode());
			tv.setTxtEventMasterName(em.getTxtEventMasterName());
			tv.setDteEventDate(UtilDateAndTime.mmddyyyyDateToString(em.getDteEventDate()));
			return tv;
		});
	}

	// @Override
	// public Page<EventMaster> searchInEntityAndEventBudget(DtoEventMasterSearch
	// dto) {
	// // enforce sane defaults and caps
	// int page = dto.getPage() != null && dto.getPage() >= 0 ? dto.getPage() : 0;
	// int size = dto.getSize() != null && dto.getSize() > 0 ?
	// Math.min(dto.getSize(), 250) : 20;
	// String sortBy = dto.getSortBy() != null ? dto.getSortBy() : "dteEventDate";
	// Sort.Direction dir = "ASC".equalsIgnoreCase(dto.getSortDir()) ?
	// Sort.Direction.ASC : Sort.Direction.DESC;
	//
	// Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));
	//
	// // Base specification from DTO (already covers many filters)
	// Specification<EventMaster> spec = SepecificationsEventMaster.fromDto(dto);
	//
	// // If txtBudgetStatus provided, AND the budget-spec explicitly (makes intent
	// clear).
	// if (dto.getTxtBudgetStatus() != null &&
	// !dto.getTxtBudgetStatus().trim().isEmpty()) {
	// spec =
	// spec.and(SepecificationsEventMaster.hasBudgetStatus(dto.getTxtBudgetStatus().trim()));
	// }
	//
	// // Execute paged query — repository's @EntityGraph now includes eventBudget
	// Page<EventMaster> pageResult = repositoryEventMaster.findAll(spec, pageable);
	//
	// return pageResult;
	// }

	@Override
	public Page<DtoEventMasterAdminPortal> searchInEntityAndEventBudget(DtoEventMasterSearch dto) {
		// enforce sane defaults and caps
		int page = dto.getPage() != null && dto.getPage() >= 0 ? dto.getPage() : 0;
		int size = dto.getSize() != null && dto.getSize() > 0 ? Math.min(dto.getSize(), 250) : 20;
		String sortBy = dto.getSortBy() != null ? dto.getSortBy() : "dteEventDate";
		Sort.Direction dir = "ASC".equalsIgnoreCase(dto.getSortDir()) ? Sort.Direction.ASC : Sort.Direction.DESC;

		Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));

		// Base specification from DTO (already covers many filters)
		Specification<EventMaster> spec = SepecificationsEventMaster.fromDto(dto);

		// If txtBudgetStatus provided, AND the budget-spec explicitly (makes intent
		// clear).
		if (dto.getTxtBudgetStatus() != null && !dto.getTxtBudgetStatus().trim().isEmpty()) {
			spec = spec.and(SepecificationsEventMaster.hasBudgetStatus(dto.getTxtBudgetStatus().trim()));
		}

		// Execute paged query — repository's @EntityGraph now includes eventBudget
		Page<EventMaster> pageResult = repositoryEventMaster.findAll(spec, pageable);

		// If sorting by date, apply custom sorting logic
		List<EventMaster> events = pageResult.getContent();
		if (!events.isEmpty()) {
			events = applyCustomDateSorting(events);
			// Create a new page with sorted content
			pageResult = new PageImpl<>(events, pageable, pageResult.getTotalElements());
		}
		// Map EventMaster -> DtoEventMasterAdminPortal and enrich with related
		// selections
		List<DtoEventMasterAdminPortal> dtos = new ArrayList<>(pageResult.getContent().size());

		// Collect event ids for optional batch lookups (see optimization comments
		// below)
		List<Integer> eventIds = new ArrayList<>();
		for (EventMaster em : pageResult.getContent()) {
			eventIds.add(em.getSerEventMasterId());
		}

		for (EventMaster event : pageResult.getContent()) {
			DtoEventMasterAdminPortal dtoEvent = MapperEventMaster.toDtoEventMasterAdminPortal(event);

			// 1) Venue detail (if present)
			try {
				if (UtilRandomKey.isNotNull(event.getVenueMasterDetail())) {
					DtoResult res = serviceVenueMaster
							.getVenueByVenueMasterDetailId(event.getVenueMasterDetail().getSerVenueMasterDetailId());
					if (UtilRandomKey.isNotNull(res) && "Success".equalsIgnoreCase(res.getTxtMessage())) {
						VenueMaster venueMaster = (VenueMaster) res.getResult();
						DtoEventVenue dtoEventVenue = new DtoEventVenue();
						dtoEventVenue.setSerVenueMasterId(venueMaster.getSerVenueMasterId());
						dtoEventVenue.setTxtVenueCode(venueMaster.getTxtVenueCode());
						dtoEventVenue.setTxtVenueName(venueMaster.getTxtVenueName());
						dtoEventVenue
								.setSerVenueMasterDetailId(event.getVenueMasterDetail().getSerVenueMasterDetailId());
						dtoEventVenue.setTxtHallCode(event.getVenueMasterDetail().getTxtHallCode());
						dtoEventVenue.setTxtHallName(event.getVenueMasterDetail().getTxtHallName());

						dtoEvent.setDtoEventVenue(dtoEventVenue);
					}
				}
			} catch (Exception ex) {
				LOGGER.debug("Failed to fetch venue detail for event {}: {}", event.getSerEventMasterId(),
						ex.getMessage(), ex);
			}

			// 2) Decor category selections
			try {
				List<DtoEventDecorCategorySelection> decorSelections = serviceEventDecorCategorySelection
						.getSelectionsWithChosenValues(dtoEvent.getSerEventMasterId());
				dtoEvent.setDtoEventDecorSelections(decorSelections);
			} catch (Exception ex) {
				LOGGER.debug("Failed to fetch decor selections for event {}: {}", event.getSerEventMasterId(),
						ex.getMessage(), ex);
				dtoEvent.setDtoEventDecorSelections(new ArrayList<>());
			}

			// 3) Menu Food Selections -> grouped by food type
			try {
				List<EventMenuFoodSelection> eventMenuFoodSelections = serviceEventMenuFoodSelection
						.getByEventMasterId(dtoEvent.getSerEventMasterId());

				// Map<String, List<DtoMenuFoodMaster>> foodSelectionsMap = new HashMap<>();
				List<DtoMenuFoodMaster> foodSelections = new ArrayList<>();
				if (UtilRandomKey.isNotNull(eventMenuFoodSelections)) {
					for (EventMenuFoodSelection entity : eventMenuFoodSelections) {
						// if (entity.getMenuFoodMaster() != null) {
						if (entity.getMenuItem() != null) {
							DtoMenuFoodMaster dtoMenuFoodMaster = new DtoMenuFoodMaster();
							// MenuFoodMaster foodMaster = entity.getMenuFoodMaster();
							MenuItem menuItem = entity.getMenuItem();

							// dtoMenuFoodMaster.setSerMenuFoodId(foodMaster.getSerMenuFoodId());
							// dtoMenuFoodMaster.setTxtMenuFoodCode(foodMaster.getTxtMenuFoodCode());
							// dtoMenuFoodMaster.setTxtMenuFoodName(foodMaster.getTxtMenuFoodName());
							// dtoMenuFoodMaster.setBlnIsMainCourse(foodMaster.getBlnIsMainCourse());
							// dtoMenuFoodMaster.setBlnIsAppetiser(foodMaster.getBlnIsAppetiser());
							// dtoMenuFoodMaster.setBlnIsStarter(foodMaster.getBlnIsStarter());
							// dtoMenuFoodMaster.setBlnIsSaladAndCondiment(foodMaster.getBlnIsSaladAndCondiment());
							// dtoMenuFoodMaster.setBlnIsDessert(foodMaster.getBlnIsDessert());
							// dtoMenuFoodMaster.setBlnIsDrink(foodMaster.getBlnIsDrink());
							// dtoMenuFoodMaster.setBlnIsActive(foodMaster.getBlnIsActive());

							dtoMenuFoodMaster.setBlnIsActive(menuItem.getBlnIsActive());
							dtoMenuFoodMaster.setSerMenuItemId(menuItem.getSerMenuItemId());
							dtoMenuFoodMaster.setTxtName(menuItem.getTxtName());
							dtoMenuFoodMaster.setTxtCode(menuItem.getTxtCode());
							dtoMenuFoodMaster.setTxtDescription(menuItem.getTxtDescription());
							dtoMenuFoodMaster.setNumPrice(entity.getNumPrice());

							// String foodType = getFoodType(foodMaster);
							// foodSelectionsMap.computeIfAbsent(foodType, k -> new
							// ArrayList<>()).add(dtoMenuFoodMaster);
							foodSelections.add(dtoMenuFoodMaster);
						}
					}
				}
				// dtoEvent.setFoodSelections(foodSelectionsMap);
				dtoEvent.setFoodSelections(foodSelections);
			} catch (Exception ex) {
				LOGGER.debug("Failed to fetch menu food selections for event {}: {}", event.getSerEventMasterId(),
						ex.getMessage(), ex);
				dtoEvent.setFoodSelections(new ArrayList<>());
			}

			// 4) Decor extras selections
			try {
				List<EventDecorExtrasSelection> extras = serviceEventDecorExtrasSelection
						.getByEventMasterId(dtoEvent.getSerEventMasterId());
				List<DtoEventDecorExtrasSelection> dtoExtras = new ArrayList<>();
				if (UtilRandomKey.isNotNull(extras)) {
					for (EventDecorExtrasSelection entity : extras) {
						DtoEventDecorExtrasSelection e = new DtoEventDecorExtrasSelection();
						e.setSerExtrasSelectionId(entity.getSerExtrasSelectionId());
						e.setTxtDynamicProperty1(entity.getTxtDynamicProperty1());
						e.setTxtDynamicProperty2(entity.getTxtDynamicProperty2());
						e.setNumPrice(entity.getNumPrice());
						if (entity.getDecorExtrasMaster() != null) {
							e.setSerExtrasId(entity.getDecorExtrasMaster().getSerExtrasId());
							e.setTxtExtrasCode(entity.getDecorExtrasMaster().getTxtExtrasCode());
							e.setTxtExtrasName(entity.getDecorExtrasMaster().getTxtExtrasName());
						}
						if (entity.getDecorExtrasOption() != null) {
							e.setSerExtraOptionId(entity.getDecorExtrasOption().getSerExtraOptionId());
							e.setTxtOptionCode(entity.getDecorExtrasOption().getTxtOptionCode());
							e.setTxtOptionName(entity.getDecorExtrasOption().getTxtOptionName());
						}
						dtoExtras.add(e);
					}
				}
				dtoEvent.setExtrasSelections(dtoExtras);
			} catch (Exception ex) {
				LOGGER.debug("Failed to fetch extras selections for event {}: {}", event.getSerEventMasterId(),
						ex.getMessage(), ex);
				dtoEvent.setExtrasSelections(new ArrayList<>());
			}

			// 5) Budget / quoted price and status
			try {
				EventBudget eventBudget = serviceEventBudget.getEventBudgetByEventId(event.getSerEventMasterId());
				if (eventBudget != null) {
					DtoEventQuoteAndStatus quote = new DtoEventQuoteAndStatus();
					quote.setNumQuotedPrice(eventBudget.getNumQuotedPrice());
					quote.setNumPaidAmount(eventBudget.getNumPaidAmount());
					quote.setTxtStatus(eventBudget.getTxtStatus());
					dtoEvent.setDtoEventQuoteAndStatus(quote);
				}
			} catch (Exception ex) {
				LOGGER.debug("Failed to fetch budget for event {}: {}", event.getSerEventMasterId(), ex.getMessage(),
						ex);
			}

			dtos.add(dtoEvent);
		}

		// Build and return a Page of DTOs with the same total/count as the original
		// Page<EventMaster>
		Page<DtoEventMasterAdminPortal> dtoPage = new PageImpl<>(dtos, pageable, pageResult.getTotalElements());
		return dtoPage;
	}

	@Override
	public EventMaster getEventMasterById(Integer serEventId) {
		try {

			Optional<EventMaster> optEvent = repositoryEventMaster.findById(serEventId);
			if (UtilRandomKey.isNotNull(optEvent)) {
				return optEvent.get();
			} else {
				return null;
			}

		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			return null;
		}
	}

	private List<EventMaster> applyCustomDateSorting(List<EventMaster> events) {
		Date currentDate = new Date(); // Today's date

		// Separate future events, past events, and events with null date
		List<EventMaster> futureEvents = new ArrayList<>();
		List<EventMaster> pastEvents = new ArrayList<>();
		List<EventMaster> nullDateEvents = new ArrayList<>();

		for (EventMaster event : events) {
			if (event.getDteEventDate() == null) {
				nullDateEvents.add(event);
			} else if (event.getDteEventDate().compareTo(currentDate) >= 0) {
				futureEvents.add(event);
			} else {
				pastEvents.add(event);
			}
		}

		// Sort future events in ascending order (closest first)
		futureEvents.sort(Comparator.comparing(EventMaster::getDteEventDate));

		// Sort past events in descending order (most recent first)
		pastEvents.sort(Comparator.comparing(EventMaster::getDteEventDate).reversed());

		// Combine: future first, then past, then null dates at the end
		List<EventMaster> sortedEvents = new ArrayList<>();
		sortedEvents.addAll(futureEvents);
		sortedEvents.addAll(pastEvents);
		sortedEvents.addAll(nullDateEvents);

		return sortedEvents;
	}
}
