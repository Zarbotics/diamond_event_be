package com.zbs.de.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.zbs.de.mapper.MapperEventDecorCategorySelection;
import com.zbs.de.mapper.MapperEventMaster;
import com.zbs.de.mapper.MapperEventMenuFoodSelection;
import com.zbs.de.mapper.MapperEventRunningOrder;
import com.zbs.de.model.CustomerMaster;
import com.zbs.de.model.DecorCategoryPropertyMaster;
import com.zbs.de.model.DecorCategoryPropertyValue;
import com.zbs.de.model.EventDecorCategorySelection;
import com.zbs.de.model.EventDecorExtrasSelection;
import com.zbs.de.model.EventDecorPropertySelection;
import com.zbs.de.model.EventDecorReferenceDocument;
import com.zbs.de.model.EventMaster;
import com.zbs.de.model.EventMenuFoodSelection;
import com.zbs.de.model.EventRunningOrder;
import com.zbs.de.model.EventType;
import com.zbs.de.model.MenuFoodMaster;
import com.zbs.de.model.UserMaster;
import com.zbs.de.model.VendorMaster;
import com.zbs.de.model.VenueMaster;
import com.zbs.de.model.dto.DtoEventBudget;
import com.zbs.de.model.dto.DtoEventDecorCategorySelection;
import com.zbs.de.model.dto.DtoEventDecorExtrasSelection;
import com.zbs.de.model.dto.DtoEventDecorPropertySelection;
import com.zbs.de.model.dto.DtoEventDecorReferenceDocument;
import com.zbs.de.model.dto.DtoEventMaster;
import com.zbs.de.model.dto.DtoEventMasterStats;
import com.zbs.de.model.dto.DtoEventMenuFoodSelection;
import com.zbs.de.model.dto.DtoEventVenue;
import com.zbs.de.model.dto.DtoMenuFoodMaster;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.repository.RepositoryEventDecorCategorySelection;
import com.zbs.de.repository.RepositoryEventDecorPropertySelection;
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
import com.zbs.de.service.ServiceVendorMaster;
import com.zbs.de.service.ServiceVenueMaster;
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
	private RepositoryEventDecorCategorySelection repositoryEventDecorCategorySelection;
	
	@Autowired
	private RepositoryEventDecorPropertySelection repositoryEventDecorPropertySelection;
	
	@Autowired
	private ServiceDecorCategoryPropertyMaster serviceDecorCategoryPropertyMaster;
	
	@Autowired
	private ServiceDecorCategoryPropertyValue serviceDecorCategoryPropertyValue;

	@Autowired
	private ServiceEmailSender serviceEmailSender;

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
			
			if (UtilRandomKey.isNull(entity.getNumInfoFilledStatus())) {
				entity.setNumInfoFilledStatus(0);
			}

			// Set customer
			// ************
			if (UtilRandomKey.isNotNull(dtoEventMaster.getSerCustId())) {
				CustomerMaster customer = serviceCustomerMaster.getByPK(dtoEventMaster.getSerCustId());
				if (UtilRandomKey.isNull(customer)) {
					dtoResult.setTxtMessage("Customer Not Foound For Id" + dtoEventMaster.getSerCustId());
					return dtoResult;
				}
				entity.setNumInfoFilledStatus(10);
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
					entity.setNumInfoFilledStatus(entity.getNumInfoFilledStatus() + 1);
				}
				entity.setEventRunningOrder(runningOrder);

				entity.setNumInfoFilledStatus(30);

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
				entity.setNumInfoFilledStatus(50);
			}

//			//This is For when you need to save which hall of the venu was selected				
//			if (UtilRandomKey.isNotNull(dtoEventMaster.getDtoEventVenue())) {
//				if (UtilRandomKey.isNotNull(dtoEventMaster.getDtoEventVenue().getSerVenueMasterDetailId())) {
//					DtoResult res = serviceVenueMaster.getVenueDetailByVenueMasterDetailId(
//							dtoEventMaster.getDtoEventVenue().getSerVenueMasterDetailId());
//					if (res.getTxtMessage().equalsIgnoreCase("Success")) {
//						VenueMasterDetail venueMasterDetail = (VenueMasterDetail) res.getResult();
//						entity.setVenueMasterDetail(venueMasterDetail);
//					} else {
//						dtoResult.setTxtMessage("Venue Hall Is Not Active");
//						return dtoResult;
//					}
//				} else {
//					dtoResult.setTxtMessage("Venue Hall Is Not Selected");
//					return dtoResult;
//				}
//				entity.setNumInfoFilledStatus(50);
//
//			}

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
				entity.setNumInfoFilledStatus(70);
			}

			// Set Food Menu Selection
			// ***********************
			if (UtilRandomKey.isNotNull(dtoEventMaster.getFoodSelections())) {
				serviceEventMenuFoodSelection.deleteByEventMasterId(entity.getSerEventMasterId());
				List<EventMenuFoodSelection> eventMenuFoodSelectionLst = new ArrayList<>();
				for (DtoEventMenuFoodSelection dto : dtoEventMaster.getFoodSelections()) {
					EventMenuFoodSelection eventMenuFoodSelection = new EventMenuFoodSelection();
					eventMenuFoodSelection.setEventMaster(entity);
					eventMenuFoodSelection.setBlnIsActive(true);
					eventMenuFoodSelection.setBlnIsApproved(true);
					eventMenuFoodSelection.setBlnIsDeleted(false);
					if (UtilRandomKey.isNotNull(dto.getSerMenuFoodId())) {
						DtoMenuFoodMaster dtoMenuFoodMaster = dtoMenuFoodMasterLst.stream()
								.filter(food -> food.getSerMenuFoodId() != null
										&& food.getSerMenuFoodId().intValue() == dto.getSerMenuFoodId().intValue())
								.findFirst().orElse(null);
						if (UtilRandomKey.isNotNull(dtoMenuFoodMaster)) {
							eventMenuFoodSelection.setTxtFoodType(getFoodType(dtoMenuFoodMaster));
							MenuFoodMaster menuFoodMaster = new MenuFoodMaster();
							menuFoodMaster.setSerMenuFoodId(dtoMenuFoodMaster.getSerMenuFoodId());
							eventMenuFoodSelection.setMenuFoodMaster(menuFoodMaster);
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

				entity.setNumInfoFilledStatus(90);

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
				entity.setNumInfoFilledStatus(100);
			}

		} else {
			// Create new
			entity = MapperEventMaster.toEntity(dtoEventMaster);
			entity.setCreatedBy(ServiceCurrentUser.getCurrentUserId());
			if (UtilRandomKey.isNull(entity.getNumInfoFilledStatus())) {
				entity.setNumInfoFilledStatus(0);
			}

			// Set customer
			// ************
			if (UtilRandomKey.isNotNull(dtoEventMaster.getSerCustId())) {
				CustomerMaster customer = serviceCustomerMaster.getByPK(dtoEventMaster.getSerCustId());
				if (UtilRandomKey.isNull(customer)) {
					dtoResult.setTxtMessage("Customer Not Foound For Id" + dtoEventMaster.getSerCustId());
					return dtoResult;
				}
				entity.setCustomerMaster(customer);
				entity.setNumInfoFilledStatus(10);

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
				entity.setNumInfoFilledStatus(30);
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
				entity.setNumInfoFilledStatus(50);
			}

//			//This is For when you need to save which hall of the venu was selected				
//			if (UtilRandomKey.isNotNull(dtoEventMaster.getDtoEventVenue())) {
//				if (UtilRandomKey.isNotNull(dtoEventMaster.getDtoEventVenue().getSerVenueMasterDetailId())) {
//					DtoResult res = serviceVenueMaster.getVenueDetailByVenueMasterDetailId(
//							dtoEventMaster.getDtoEventVenue().getSerVenueMasterDetailId());
//					if (res.getTxtMessage().equalsIgnoreCase("Success")) {
//						VenueMasterDetail venueMasterDetail = (VenueMasterDetail) res.getResult();
//						entity.setVenueMasterDetail(venueMasterDetail);
//					} else {
//						dtoResult.setTxtMessage("Venue Hall Is Not Active");
//						return dtoResult;
//					}
//				} else {
//					dtoResult.setTxtMessage("Venue Hall Is Not Selected");
//					return dtoResult;
//				}
//				entity.setNumInfoFilledStatus(50);
//
//			}

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
				entity.setNumInfoFilledStatus(70);
			}

			// Set Food Menu Selection
			// ***********************
			if (UtilRandomKey.isNotNull(dtoEventMaster.getFoodSelections())) {
				List<EventMenuFoodSelection> eventMenuFoodSelectionLst = new ArrayList<>();
				for (DtoEventMenuFoodSelection dto : dtoEventMaster.getFoodSelections()) {
					EventMenuFoodSelection eventMenuFoodSelection = new EventMenuFoodSelection();
					eventMenuFoodSelection.setEventMaster(entity);
					eventMenuFoodSelection.setBlnIsActive(true);
					eventMenuFoodSelection.setBlnIsApproved(true);
					eventMenuFoodSelection.setBlnIsDeleted(false);
					if (UtilRandomKey.isNotNull(dto.getSerMenuFoodId())) {
						DtoMenuFoodMaster dtoMenuFoodMaster = dtoMenuFoodMasterLst.stream()
								.filter(food -> food.getSerMenuFoodId() != null
										&& food.getSerMenuFoodId().intValue() == dto.getSerMenuFoodId().intValue())
								.findFirst().orElse(null);
						if (UtilRandomKey.isNotNull(dtoMenuFoodMaster)) {
							eventMenuFoodSelection.setTxtFoodType(getFoodType(dtoMenuFoodMaster));
							MenuFoodMaster menuFoodMaster = new MenuFoodMaster();
							menuFoodMaster.setSerMenuFoodId(dtoMenuFoodMaster.getSerMenuFoodId());
							eventMenuFoodSelection.setMenuFoodMaster(menuFoodMaster);
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
				entity.setNumInfoFilledStatus(90);
			}

			// Set Vendor
			// **********
			if (UtilRandomKey.isNotNull(dtoEventMaster.getSerVendorId())) {
				VendorMaster vendorMaster = serviceVendorMaster.getByPK(dtoEventMaster.getSerVendorId());
				if (UtilRandomKey.isNotNull(vendorMaster)) {
					entity.setVendorMaster(vendorMaster);
					entity.setNumInfoFilledStatus(entity.getNumInfoFilledStatus() + 1);
				} else {
					dtoResult.setTxtMessage(
							"External Supplier Not Found Against Id: " + dtoEventMaster.getSerVendorId());
					return dtoResult;
				}
			}
			entity.setNumInfoFilledStatus(100);

			// Generate event master code
			String code = generateNextEventMasterCode();
			entity.setTxtEventMasterCode(code);
		}

		entity = repositoryEventMaster.save(entity);

		dtoResult.setTxtMessage("Success");
		dtoResult.setResult(entity);
		return dtoResult;
	}

	public String generateNextEventMasterCode() {
		String maxCode = repositoryEventMaster.findMaxEventCode();

		int nextNumber = 1;

		if (maxCode != null && maxCode.startsWith("EVT-")) {
			try {
				String numberPart = maxCode.substring(4);
				nextNumber = Integer.parseInt(numberPart) + 1;
			} catch (NumberFormatException e) {
				nextNumber = 1;
			}
		}

		return String.format("EVT-%03d", nextNumber);
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
				List<DtoEventMenuFoodSelection> dtoEventMenuFoodSelections = new ArrayList<>();
				if (UtilRandomKey.isNotNull(eventMenuFoodSelections)) {
					for (EventMenuFoodSelection entity : eventMenuFoodSelections) {
						DtoEventMenuFoodSelection dtoEventMenuFoodSelection = MapperEventMenuFoodSelection
								.toDto(entity);
						dtoEventMenuFoodSelections.add(dtoEventMenuFoodSelection);
					}
				}
				dto.setFoodSelections(dtoEventMenuFoodSelections);

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

			List<EventMaster> eventMasterLst = repositoryEventMaster.findByCustomerId(dtoSearch.getId());
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

					// Fetching Event Menu Food Selection
					// ***********************************

					List<EventMenuFoodSelection> eventMenuFoodSelections = serviceEventMenuFoodSelection
							.getByEventMasterId(dto.getSerEventMasterId());
					List<DtoEventMenuFoodSelection> dtoEventMenuFoodSelections = new ArrayList<>();
					if (UtilRandomKey.isNotNull(eventMenuFoodSelections)) {
						for (EventMenuFoodSelection entity : eventMenuFoodSelections) {
							DtoEventMenuFoodSelection dtoEventMenuFoodSelection = MapperEventMenuFoodSelection
									.toDto(entity);
							dtoEventMenuFoodSelections.add(dtoEventMenuFoodSelection);
						}
					}
					dto.setFoodSelections(dtoEventMenuFoodSelections);
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
					List<DtoEventDecorCategorySelection> eventDecorCategorySelections = serviceEventDecorCategorySelection.getSelectionsWithChosenValues(dto.getSerEventMasterId());
					dto.setDtoEventDecorSelections(eventDecorCategorySelections);

					// Fetching Event Menu Food Selection
					// ***********************************

					List<EventMenuFoodSelection> eventMenuFoodSelections = serviceEventMenuFoodSelection
							.getByEventMasterId(dto.getSerEventMasterId());
					List<DtoEventMenuFoodSelection> dtoEventMenuFoodSelections = new ArrayList<>();
					if (UtilRandomKey.isNotNull(eventMenuFoodSelections)) {
						for (EventMenuFoodSelection entity : eventMenuFoodSelections) {
							DtoEventMenuFoodSelection dtoEventMenuFoodSelection = MapperEventMenuFoodSelection
									.toDto(entity);
							dtoEventMenuFoodSelections.add(dtoEventMenuFoodSelection);
						}
					}
					dto.setFoodSelections(dtoEventMenuFoodSelections);

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

			// Fetch existing if exists
			Optional<EventMaster> optionalExisting = null;
			if(dtoEventMaster.getSerEventMasterId() != null) {
				optionalExisting = repositoryEventMaster
						.findByIdAndBlnIsDeletedFalse(dtoEventMaster.getSerEventMasterId());
			}


			List<MenuFoodMaster> dtoMenuFoodMasterLst = serviceMenuFoodMaster.getAllDataEntity();
			if (UtilRandomKey.isNull(dtoMenuFoodMasterLst)) {
				dtoResult.setTxtMessage("No Food Item Is Present In DB");
				return dtoResult;
			}
			
			List<DecorCategoryPropertyMaster> decorCategoryPropertyMasterLst = serviceDecorCategoryPropertyMaster.getAllPropertiesMaster();
			List<DecorCategoryPropertyValue> decorCategoryPropertyValueLst = serviceDecorCategoryPropertyValue.getAllPropertyValueMaster();
			

			EventMaster entity;
			Map<String, MultipartFile> fileMap = null;
			if (UtilRandomKey.isNotNull(files)) {
				fileMap = files.stream().collect(Collectors.toMap(MultipartFile::getOriginalFilename, f -> f));
			}

			if (optionalExisting != null && optionalExisting.isPresent()) {
				// Update existing
				entity = optionalExisting.get();

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
				if (UtilRandomKey.isNull(entity.getNumInfoFilledStatus())) {
					entity.setNumInfoFilledStatus(0);
				}

				// Set customer
				// ************
				if (UtilRandomKey.isNotNull(dtoEventMaster.getSerCustId())) {
					CustomerMaster customer = serviceCustomerMaster.getByPK(dtoEventMaster.getSerCustId());
					if (UtilRandomKey.isNull(customer)) {
						dtoResult.setTxtMessage("Customer Not Foound For Id" + dtoEventMaster.getSerCustId());
						return dtoResult;
					}
					entity.setNumInfoFilledStatus(10);
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
						entity.setNumInfoFilledStatus(entity.getNumInfoFilledStatus() + 1);
					}
					entity.setEventRunningOrder(runningOrder);

					entity.setNumInfoFilledStatus(30);

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
					entity.setNumInfoFilledStatus(50);
				}

//				//This is For when you need to save which hall of the venu was selected				
//				if (UtilRandomKey.isNotNull(dtoEventMaster.getDtoEventVenue())) {
//					if (UtilRandomKey.isNotNull(dtoEventMaster.getDtoEventVenue().getSerVenueMasterDetailId())) {
//						DtoResult res = serviceVenueMaster.getVenueDetailByVenueMasterDetailId(
//								dtoEventMaster.getDtoEventVenue().getSerVenueMasterDetailId());
//						if (res.getTxtMessage().equalsIgnoreCase("Success")) {
//							VenueMasterDetail venueMasterDetail = (VenueMasterDetail) res.getResult();
//							entity.setVenueMasterDetail(venueMasterDetail);
//						} else {
//							dtoResult.setTxtMessage("Venue Hall Is Not Active");
//							return dtoResult;
//						}
//					} else {
//						dtoResult.setTxtMessage("Venue Hall Is Not Selected");
//						return dtoResult;
//					}
//					entity.setNumInfoFilledStatus(50);
//
//				}

				// Set Decor Item Selections
				// *************************
				if (UtilRandomKey.isNotNull(dtoEventMaster.getDtoEventDecorSelections())) {

					// Deleting Existing Selections
					if(entity.getDecorSelections() != null && !entity.getDecorSelections().isEmpty()) {
						serviceEventDecorCategorySelection.deleteByEventMasterId(entity.getSerEventMasterId());
					}
//					entity.getDecorSelections().clear();

					List<EventDecorCategorySelection> decorSelections = new ArrayList<>();

					for (DtoEventDecorCategorySelection dto : dtoEventMaster.getDtoEventDecorSelections()) {
						EventDecorCategorySelection decorSelection = MapperEventDecorCategorySelection.toEntity(dto);
						decorSelection.setEventMaster(entity);
						decorSelection = repositoryEventDecorCategorySelection.save(decorSelection);

						// Set property selections' back reference
//						if (decorSelection.getSelectedProperties() != null) {
//							for (EventDecorPropertySelection prop : decorSelection.getSelectedProperties()) {
//								prop.setEventDecorCategorySelection(decorSelection);
//								
//							}
//						}
						if (dto.getSelectedProperties() != null  && !dto.getSelectedProperties().isEmpty()) {
							decorSelection.getSelectedProperties().clear();
							List<EventDecorPropertySelection> newSelectedProperties = new  ArrayList<>();
							for (DtoEventDecorPropertySelection property : dto.getSelectedProperties()) {
									EventDecorPropertySelection eventDecorPropertySelection = new EventDecorPropertySelection();
									eventDecorPropertySelection.setBlnIsActive(true);
									eventDecorPropertySelection.setBlnIsDeleted(false);
									eventDecorPropertySelection.setCreatedDate(UtilDateAndTime.getCurrentDate());
									eventDecorPropertySelection.setEventDecorCategorySelection(decorSelection);
									
									DecorCategoryPropertyMaster matchedMaster =
									        decorCategoryPropertyMasterLst.stream()
									                .filter(pm -> pm.getSerPropertyId().intValue() == property.getSerPropertyId().intValue())
									                .findFirst()
									                .orElse(null);
									
									DecorCategoryPropertyValue matchedValue =
									        decorCategoryPropertyValueLst.stream()
									                .filter(pv -> pv.getSerPropertyValueId().intValue()== property.getSerPropertyValueId().intValue())
									                .findFirst()
									                .orElse(null);
									
									eventDecorPropertySelection.setProperty(matchedMaster);
									eventDecorPropertySelection.setSelectedValue(matchedValue);
									newSelectedProperties.add(eventDecorPropertySelection);
							}
							
							decorSelection.getSelectedProperties().addAll(newSelectedProperties);
						}
						
						
						

						// Set reference image back reference
						if (decorSelection.getUserUploadedDocuments() != null && UtilRandomKey.isNotNull(files)) {
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
							decorSelection.setUserUploadedDocuments(documents);
						}

						decorSelections.add(decorSelection);
					}
				
					entity.setDecorSelections(decorSelections);
					entity.setNumInfoFilledStatus(70);
				}

				// Set Food Menu Selection
				// ***********************
				if (UtilRandomKey.isNotNull(dtoEventMaster.getFoodSelections()) && !dtoEventMaster.getFoodSelections().isEmpty()) {
//					serviceEventMenuFoodSelection.deleteByEventMasterId(entity.getSerEventMasterId());
					entity.getFoodSelections().clear();
					List<EventMenuFoodSelection> eventMenuFoodSelectionLst = new ArrayList<>();
					for (DtoEventMenuFoodSelection dto : dtoEventMaster.getFoodSelections()) {
						EventMenuFoodSelection eventMenuFoodSelection = new EventMenuFoodSelection();
						eventMenuFoodSelection.setEventMaster(entity);
						eventMenuFoodSelection.setBlnIsActive(true);
						eventMenuFoodSelection.setBlnIsApproved(true);
						eventMenuFoodSelection.setBlnIsDeleted(false);
						eventMenuFoodSelection.setEventMaster(entity);
						if (UtilRandomKey.isNotNull(dto.getSerMenuFoodId())) {
							MenuFoodMaster menuFoodMaster = dtoMenuFoodMasterLst.stream()
									.filter(food -> food.getSerMenuFoodId() != null
											&& food.getSerMenuFoodId().intValue() == dto.getSerMenuFoodId().intValue())
									.findFirst().orElse(null);
							if (UtilRandomKey.isNotNull(menuFoodMaster)) {
								eventMenuFoodSelection.setMenuFoodMaster(menuFoodMaster);
								eventMenuFoodSelection.setTxtFoodType(getFoodName(menuFoodMaster));
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
					entity.setNumInfoFilledStatus(90);

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
					entity.setNumInfoFilledStatus(100);
				}

			} else {
				// Create new
				entity = MapperEventMaster.toEntity(dtoEventMaster);
				entity.setEventRunningOrder(null);
				entity.setEventType(null);
				entity.setDecorSelections(null);
				entity.setFoodSelections(null);
				entity = repositoryEventMaster.save(entity);
				entity.setCreatedBy(ServiceCurrentUser.getCurrentUserId());

				if (UtilRandomKey.isNull(entity.getNumInfoFilledStatus())) {
					entity.setNumInfoFilledStatus(0);
				}

				// Set customer
				// ************
				if (UtilRandomKey.isNotNull(dtoEventMaster.getSerCustId())) {
					CustomerMaster customer = serviceCustomerMaster.getByPK(dtoEventMaster.getSerCustId());
					if (UtilRandomKey.isNull(customer)) {
						dtoResult.setTxtMessage("Customer Not Foound For Id" + dtoEventMaster.getSerCustId());
						return dtoResult;
					}
					entity.setCustomerMaster(customer);
					entity.setNumInfoFilledStatus(10);

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
					entity.setNumInfoFilledStatus(30);
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
					entity.setNumInfoFilledStatus(50);
				}

//				//This is For when you need to save which hall of the venu was selected				
//				if (UtilRandomKey.isNotNull(dtoEventMaster.getDtoEventVenue())) {
//					if (UtilRandomKey.isNotNull(dtoEventMaster.getDtoEventVenue().getSerVenueMasterDetailId())) {
//						DtoResult res = serviceVenueMaster.getVenueDetailByVenueMasterDetailId(
//								dtoEventMaster.getDtoEventVenue().getSerVenueMasterDetailId());
//						if (res.getTxtMessage().equalsIgnoreCase("Success")) {
//							VenueMasterDetail venueMasterDetail = (VenueMasterDetail) res.getResult();
//							entity.setVenueMasterDetail(venueMasterDetail);
//						} else {
//							dtoResult.setTxtMessage("Venue Hall Is Not Active");
//							return dtoResult;
//						}
//					} else {
//						dtoResult.setTxtMessage("Venue Hall Is Not Selected");
//						return dtoResult;
//					}
//					entity.setNumInfoFilledStatus(50);
//
//				}

				// Set Decore Item Selections
				// **************************
				if (UtilRandomKey.isNotNull(dtoEventMaster.getDtoEventDecorSelections())) {
					List<EventDecorCategorySelection> decorSelections = new ArrayList<>();

					for (DtoEventDecorCategorySelection dto : dtoEventMaster.getDtoEventDecorSelections()) {
						EventDecorCategorySelection decorSelection = MapperEventDecorCategorySelection.toEntity(dto);
						decorSelection.setEventMaster(entity);

//						if (decorSelection.getSelectedProperties() != null) {
//							decorSelection.getSelectedProperties()
//									.forEach(p -> p.setEventDecorCategorySelection(decorSelection));
//						}
						
						
						if (dto.getSelectedProperties() != null  && !dto.getSelectedProperties().isEmpty()) {
							decorSelection.getSelectedProperties().clear();
							List<EventDecorPropertySelection> newSelectedProperties = new  ArrayList<>();
							for (DtoEventDecorPropertySelection property : dto.getSelectedProperties()) {
									EventDecorPropertySelection eventDecorPropertySelection = new EventDecorPropertySelection();
									eventDecorPropertySelection.setBlnIsActive(true);
									eventDecorPropertySelection.setBlnIsDeleted(false);
									eventDecorPropertySelection.setCreatedDate(UtilDateAndTime.getCurrentDate());
									eventDecorPropertySelection.setEventDecorCategorySelection(decorSelection);
									
									DecorCategoryPropertyMaster matchedMaster =
									        decorCategoryPropertyMasterLst.stream()
									                .filter(pm -> pm.getSerPropertyId().intValue() == property.getSerEventDecorPropertyId().intValue())
									                .findFirst()
									                .orElse(null);
									
									DecorCategoryPropertyValue matchedValue =
									        decorCategoryPropertyValueLst.stream()
									                .filter(pv -> pv.getSerPropertyValueId().intValue()== property.getSerPropertyValueId().intValue())
									                .findFirst()
									                .orElse(null);
									
									eventDecorPropertySelection.setProperty(matchedMaster);
									eventDecorPropertySelection.setSelectedValue(matchedValue);
									newSelectedProperties.add(eventDecorPropertySelection);
							}
							
							decorSelection.getSelectedProperties().addAll(newSelectedProperties);
						}
						

						// Set reference image back reference
						if (decorSelection.getUserUploadedDocuments() != null && UtilRandomKey.isNotNull(files)) {
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
							decorSelection.setUserUploadedDocuments(documents);
						}

						decorSelections.add(decorSelection);
					}

					entity.setDecorSelections(decorSelections);
					entity.setNumInfoFilledStatus(70);
				}

				// Set Food Menu Selection
				// ***********************
				if (UtilRandomKey.isNotNull(dtoEventMaster.getFoodSelections())) {
					List<EventMenuFoodSelection> eventMenuFoodSelectionLst = new ArrayList<>();
					for (DtoEventMenuFoodSelection dto : dtoEventMaster.getFoodSelections()) {
						EventMenuFoodSelection eventMenuFoodSelection = new EventMenuFoodSelection();
						eventMenuFoodSelection.setEventMaster(entity);
						eventMenuFoodSelection.setBlnIsActive(true);
						eventMenuFoodSelection.setBlnIsApproved(true);
						eventMenuFoodSelection.setBlnIsDeleted(false);
						if (UtilRandomKey.isNotNull(dto.getSerMenuFoodId())) {
							MenuFoodMaster menuFoodMaster = dtoMenuFoodMasterLst.stream()
									.filter(food -> food.getSerMenuFoodId() != null
											&& food.getSerMenuFoodId().intValue() == dto.getSerMenuFoodId().intValue())
									.findFirst().orElse(null);
							if (UtilRandomKey.isNotNull(menuFoodMaster)) {
								eventMenuFoodSelection.setMenuFoodMaster(menuFoodMaster);
								eventMenuFoodSelection.setTxtFoodType(getFoodName(menuFoodMaster));
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
					entity.setNumInfoFilledStatus(90);
				}

				// Set Vendor
				// **********
				if (UtilRandomKey.isNotNull(dtoEventMaster.getSerVendorId())) {
					VendorMaster vendorMaster = serviceVendorMaster.getByPK(dtoEventMaster.getSerVendorId());
					if (UtilRandomKey.isNotNull(vendorMaster)) {
						entity.setVendorMaster(vendorMaster);
						entity.setNumInfoFilledStatus(entity.getNumInfoFilledStatus() + 1);
					} else {
						dtoResult.setTxtMessage(
								"External Supplier Not Found Against Id: " + dtoEventMaster.getSerVendorId());
						return dtoResult;
					}
				}
				entity.setNumInfoFilledStatus(100);

				// Generate event master code
				String code = generateNextEventMasterCode();
				entity.setTxtEventMasterCode(code);
			}

			// ****** Setting Event Decor Extras ******
			if (UtilRandomKey.isNotNull(dtoEventMaster.getExtrasSelections()) && !dtoEventMaster.getExtrasSelections().isEmpty()) {
				serviceEventDecorExtrasSelection.deleteByEventMasterId(entity.getSerEventMasterId());

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

					selection = serviceEventDecorExtrasSelection.save(selection);
					newSelections.add(selection);
				}
				entity.setExtrasSelections(newSelections);
				entity.setNumInfoFilledStatus(entity.getNumInfoFilledStatus() + 1);
			}

			entity = repositoryEventMaster.save(entity);

			if (this.isEventRegistrationCompleted(entity)) {
				UserMaster userMaster = ServiceCurrentUser.getCurrentUser();
				if (userMaster != null) {
					serviceEmailSender.sendEventRegistrationEmail(userMaster.getTxtEmail(), userMaster.getTxtName(),
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
				&& eventMaster.getEventType() != null && eventMaster.getVendorMaster() != null
				&& (eventMaster.getVenueMaster() != null || eventMaster.getVenueMasterDetail() != null)
				&& (eventMaster.getFoodSelections() != null && !eventMaster.getFoodSelections().isEmpty())
				&& (eventMaster.getDecorSelections() != null && !eventMaster.getDecorSelections().isEmpty())) {
			return true;
		} else {
			return false;
		}
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
				List<DtoEventDecorCategorySelection> eventDecorCategorySelections = serviceEventDecorCategorySelection.getSelectionsWithChosenValues(dto.getSerEventMasterId());
				dto.setDtoEventDecorSelections(eventDecorCategorySelections);
				
				// Fetching Event Menu Food Selection
				// ***********************************

				List<EventMenuFoodSelection> eventMenuFoodSelections = serviceEventMenuFoodSelection
						.getByEventMasterId(dto.getSerEventMasterId());
				List<DtoEventMenuFoodSelection> dtoEventMenuFoodSelections = new ArrayList<>();
				if (UtilRandomKey.isNotNull(eventMenuFoodSelections)) {
					for (EventMenuFoodSelection entity : eventMenuFoodSelections) {
						DtoEventMenuFoodSelection dtoEventMenuFoodSelection = MapperEventMenuFoodSelection
								.toDto(entity);
						dtoEventMenuFoodSelections.add(dtoEventMenuFoodSelection);
					}
				}
				dto.setFoodSelections(dtoEventMenuFoodSelections);

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
		if(food.getBlnIsAppetiser() != null && food.getBlnIsAppetiser()) {
			return "Appetizers";
		}else if(food.getBlnIsDessert() != null && food.getBlnIsDessert()) {
			return "Desserts";
		}else if(food.getBlnIsStarter() != null && food.getBlnIsStarter()) {
			return "Starters & Main course";
		}else if(food.getBlnIsSaladAndCondiment() != null && food.getBlnIsSaladAndCondiment()) {
			return "Salad & Condiments";
		}else if(food.getBlnIsDrink() != null && food.getBlnIsDrink()) {
			return "Reception Drinks";
		}else if(food.getBlnIsAppetiser() != null && food.getBlnIsAppetiser()) {
			
		}else if(food.getBlnIsMainCourse() != null && food.getBlnIsMainCourse()) {
			return "Mains";
		}
		return null;
	}
}
