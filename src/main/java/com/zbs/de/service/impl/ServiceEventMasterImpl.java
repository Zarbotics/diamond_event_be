package com.zbs.de.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zbs.de.controller.ControllerCountryMaster;
import com.zbs.de.mapper.MapperEventDecorItemSelection;
import com.zbs.de.mapper.MapperEventMaster;
import com.zbs.de.mapper.MapperEventMenuFoodSelection;
import com.zbs.de.mapper.MapperEventRunningOrder;
import com.zbs.de.model.CustomerMaster;
import com.zbs.de.model.EventDecorItemSelection;
import com.zbs.de.model.EventMaster;
import com.zbs.de.model.EventMenuFoodSelection;
import com.zbs.de.model.EventRunningOrder;
import com.zbs.de.model.EventType;
import com.zbs.de.model.MenuFoodMaster;
import com.zbs.de.model.VendorMaster;
import com.zbs.de.model.VenueMaster;
import com.zbs.de.model.dto.DtoEventDecorItemSelection;
import com.zbs.de.model.dto.DtoEventMaster;
import com.zbs.de.model.dto.DtoEventMenuFoodSelection;
import com.zbs.de.model.dto.DtoMenuFoodMaster;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.repository.RepositoryEventMaster;
import com.zbs.de.repository.RepositoryEventRunningOrder;
import com.zbs.de.service.ServiceCustomerMaster;
import com.zbs.de.service.ServiceEventMaster;
import com.zbs.de.service.ServiceEventMenuFoodSelection;
import com.zbs.de.service.ServiceEventType;
import com.zbs.de.service.ServiceMenuFoodMaster;
import com.zbs.de.service.ServiceVendorMaster;
import com.zbs.de.service.ServiceVenueMaster;
import com.zbs.de.util.UtilDateAndTime;
import com.zbs.de.util.UtilRandomKey;

@Service("serviceEventMaster")
public class ServiceEventMasterImpl implements ServiceEventMaster {

	private final ControllerCountryMaster controllerCountryMaster;

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
	private ServiceEventDecorItemSelectionImpl serviceEventDecorItemSelectionImpl;

	@Autowired
	private ServiceEventMenuFoodSelection serviceEventMenuFoodSelection;

	@Autowired
	private ServiceMenuFoodMaster serviceMenuFoodMaster;

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceEventMasterImpl.class);

	ServiceEventMasterImpl(ControllerCountryMaster controllerCountryMaster) {
		this.controllerCountryMaster = controllerCountryMaster;
	}

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
			entity.setTxtGroomName(dtoEventMaster.getTxtGroomName());
			entity.setTxtBirthDayCelebrant(dtoEventMaster.getTxtBirthDayCelebrant());
			entity.setTxtAgeCategory(dtoEventMaster.getTxtAgeCategory());
			entity.setTxtChiefGuest(dtoEventMaster.getTxtChiefGuest());
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
				if (UtilRandomKey.isNull(entity.getCustomerMaster())) {
					entity.setNumInfoFilledStatus(entity.getNumInfoFilledStatus() + 1);
				}
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

				if (UtilRandomKey.isNull(entity.getEventType())) {
					entity.setNumInfoFilledStatus(entity.getNumInfoFilledStatus() + 1);
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
					runningOrder = repositoryEventRunningOrder.save(runningOrder);
				} else {
					runningOrder = MapperEventRunningOrder.toEntity(dtoEventMaster.getDtoEventRunningOrder());
					runningOrder = repositoryEventRunningOrder.save(runningOrder);
					entity.setNumInfoFilledStatus(entity.getNumInfoFilledStatus() + 1);
				}
				entity.setEventRunningOrder(runningOrder);

			}

			// Set Decore Item Selections
			// **************************
			if (dtoEventMaster.getDtoEventDecorItemSelections() != null) {
				serviceEventDecorItemSelectionImpl.deleteByEventMasterId(entity.getSerEventMasterId());
				for (DtoEventDecorItemSelection decorDto : dtoEventMaster.getDtoEventDecorItemSelections()) {
					decorDto.setSerEventMasterId(entity.getSerEventMasterId());
					serviceEventDecorItemSelectionImpl.saveOrUpdate(decorDto);
				}
				entity.setNumInfoFilledStatus(4);
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
				
				entity.setNumInfoFilledStatus(6);

			}

			// Setting Venue Master
			// ********************
			if (UtilRandomKey.isNotNull(dtoEventMaster.getSerVenueMasterId())) {
				VenueMaster venueMaster = serviceVenueMaster.getByPK(dtoEventMaster.getSerVenueMasterId());
				if (UtilRandomKey.isNull(venueMaster)) {
					dtoResult.setTxtMessage("Venue Not Found For Id: " + dtoEventMaster.getSerVenueMasterId());
				}

				if (UtilRandomKey.isNull(entity.getVenueMaster())) {
					entity.setNumInfoFilledStatus(entity.getNumInfoFilledStatus() + 1);
				}

				entity.setVenueMaster(venueMaster);
			}

			// Set Vendor
			// **********
			if (UtilRandomKey.isNotNull(dtoEventMaster.getSerVendorId())) {
				VendorMaster vendorMaster = serviceVendorMaster.getByPK(dtoEventMaster.getSerVendorId());
				if (UtilRandomKey.isNotNull(vendorMaster)) {

					if (UtilRandomKey.isNotNull(entity.getVendorMaster())) {
						entity.setNumInfoFilledStatus(entity.getNumInfoFilledStatus() + 1);
					}
					entity.setVendorMaster(vendorMaster);
				} else {
					dtoResult.setTxtMessage(
							"External Supplier Not Found Against Id: " + dtoEventMaster.getSerVendorId());
					return dtoResult;
				}
			}

			// Set Food Menu
			// *************
			if (dtoEventMaster.getFoodSelections() != null && !dtoEventMaster.getFoodSelections().isEmpty()) {
				List<EventMenuFoodSelection> foodSelections = new ArrayList<>();
				for (DtoEventMenuFoodSelection foodDto : dtoEventMaster.getFoodSelections()) {
					MenuFoodMaster menu = serviceMenuFoodMaster.getByPK(foodDto.getSerMenuFoodId());
					if (menu != null) {
						EventMenuFoodSelection selection = new EventMenuFoodSelection();
						selection.setEventMaster(entity); // event already persisted
						selection.setMenuFoodMaster(menu);
						selection.setTxtFoodType(foodDto.getTxtFoodType());
						foodSelections.add(selection);
					}
				}

				// Clear previous and reassign
				if (UtilRandomKey.isNull(entity.getFoodSelections())) {
					entity.setNumInfoFilledStatus(entity.getNumInfoFilledStatus() + 1);
				}
				entity.getFoodSelections().clear();
				entity.getFoodSelections().addAll(foodSelections);
			}

		} else {
			// Create new
			entity = MapperEventMaster.toEntity(dtoEventMaster);
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
				entity.setNumInfoFilledStatus(entity.getNumInfoFilledStatus() + 1);

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
				entity.setNumInfoFilledStatus(entity.getNumInfoFilledStatus() + 1);
			}

			// Set optional event running order
			// ********************************
			if (dtoEventMaster.getDtoEventRunningOrder() != null) {
				EventRunningOrder runningOrder = new EventRunningOrder();
				runningOrder = MapperEventRunningOrder.toEntity(dtoEventMaster.getDtoEventRunningOrder());
				runningOrder = repositoryEventRunningOrder.save(runningOrder);
				entity.setEventRunningOrder(runningOrder);
				entity.setNumInfoFilledStatus(entity.getNumInfoFilledStatus() + 1);
			}

			// Set Venue Master
			// ****************
			if (UtilRandomKey.isNotNull(dtoEventMaster.getSerVenueMasterId())) {
				VenueMaster venueMaster = serviceVenueMaster.getByPK(dtoEventMaster.getSerVenueMasterId());
				if (UtilRandomKey.isNull(venueMaster)) {
					dtoResult.setTxtMessage("Venue Not Found For Id: " + dtoEventMaster.getSerVenueMasterId());
					return dtoResult;
				}
				entity.setVenueMaster(venueMaster);
				entity.setNumInfoFilledStatus(entity.getNumInfoFilledStatus() + 1);
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

			// Set Decore Item Selections
			// **************************
			if (dtoEventMaster.getDtoEventDecorItemSelections() != null) {
				for (DtoEventDecorItemSelection decorDto : dtoEventMaster.getDtoEventDecorItemSelections()) {
					decorDto.setSerEventMasterId(entity.getSerEventMasterId());
					serviceEventDecorItemSelectionImpl.saveOrUpdate(decorDto);
				}
				entity.setNumInfoFilledStatus(entity.getNumInfoFilledStatus() + 1);
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
				entity.setNumInfoFilledStatus(entity.getNumInfoFilledStatus() + 1);
			}


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
				List<EventDecorItemSelection> eventDecorItemSelections = serviceEventDecorItemSelectionImpl
						.getByEventMasterId(dto.getSerEventMasterId());
				List<DtoEventDecorItemSelection> dtoEventDecorItemSelections = new ArrayList<>();
				if (UtilRandomKey.isNotNull(eventDecorItemSelections)) {
					for (EventDecorItemSelection entity : eventDecorItemSelections) {
						DtoEventDecorItemSelection dtoEventDecorItemSelection = MapperEventDecorItemSelection
								.toDto(entity);
						dtoEventDecorItemSelections.add(dtoEventDecorItemSelection);
					}
				}
				dto.setDtoEventDecorItemSelections(dtoEventDecorItemSelections);

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
}
