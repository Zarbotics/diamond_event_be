package com.zbs.de.service.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import com.zbs.de.model.Booking;
import com.zbs.de.model.CustomerMaster;
import com.zbs.de.model.DecorCategoryPropertyMaster;
import com.zbs.de.model.DecorCategoryPropertyValue;
import com.zbs.de.model.DecorExtrasMaster;
import com.zbs.de.model.DecorExtrasOption;
import com.zbs.de.model.EventBudget;
import com.zbs.de.model.EventDecorCategorySelection;
import com.zbs.de.model.EventDecorExtrasSelection;
import com.zbs.de.model.EventDecorPropertySelection;
import com.zbs.de.model.EventDecorPropertyValueSelection;
import com.zbs.de.model.EventDecorReferenceDocument;
import com.zbs.de.model.EventMaster;
import com.zbs.de.model.EventMenuCategorySelection;
import com.zbs.de.model.EventMenuFoodSelection;
import com.zbs.de.model.EventMenuSubCategorySelection;
import com.zbs.de.model.EventRunningOrder;
import com.zbs.de.model.EventType;
import com.zbs.de.model.EventExternalSupplier;
import com.zbs.de.model.MenuFoodMaster;
import com.zbs.de.model.MenuItem;
import com.zbs.de.model.UserMaster;
import com.zbs.de.model.VenueMaster;
import com.zbs.de.model.VenueMasterDetail;
import com.zbs.de.model.dto.DtoEventBookingValidationResult;
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
import com.zbs.de.model.dto.DtoEventQuoteAndStatus;
import com.zbs.de.model.dto.DtoEventExternalSupplier;
import com.zbs.de.model.dto.DtoEventRunningOrder;
import com.zbs.de.model.dto.DtoEventVenue;
import com.zbs.de.model.dto.DtoMenuComponentRequest;
import com.zbs.de.model.dto.DtoMenuFoodMaster;
import com.zbs.de.model.dto.DtoMenuItem;
import com.zbs.de.model.dto.DtoNotificationMaster;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.model.dto.menu.DtoCustomerMenuCategory;
import com.zbs.de.model.dto.menu.DtoCustomerMenuSubCategory;
import com.zbs.de.repository.RepositoryBooking;
import com.zbs.de.repository.RepositoryEventDateLock;
import com.zbs.de.repository.RepositoryEventMaster;
import com.zbs.de.repository.RepositoryEventPaymentMaster;
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
import com.zbs.de.service.EventDayCapacity;
import com.zbs.de.service.ServiceEventMaster;
import com.zbs.de.service.ServiceEventMenuFoodSelection;
import com.zbs.de.service.ServiceEventType;
import com.zbs.de.service.ServiceMenuFoodMaster;
import com.zbs.de.service.ServiceMenuItem;
import com.zbs.de.service.ServiceNotificationMaster;
import com.zbs.de.service.ServiceVenueMaster;
import com.zbs.de.spec.SepecificationsEventMaster;
import com.zbs.de.util.UtilDateAndTime;
import com.zbs.de.util.enums.EnmNotificationCategory;
import com.zbs.de.util.UtilFileStorage;
import com.zbs.de.util.UtilRandomKey;

import jakarta.transaction.Transactional;

@Service("serviceEventMaster")
public class ServiceEventMasterImpl implements ServiceEventMaster {

	@Autowired
	private RepositoryEventMaster repositoryEventMaster;

	@Autowired
	private RepositoryEventDateLock repositoryEventDateLock;

	@Autowired
	private RepositoryEventRunningOrder repositoryEventRunningOrder;

	@Autowired
	private RepositoryBooking repositoryBooking;

	@Autowired
	private ServiceCustomerMaster serviceCustomerMaster;

	@Autowired
	private ServiceEventType serviceEventType;

	@Autowired
	private ServiceVenueMaster serviceVenueMaster;

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
	
	@Autowired
	private com.zbs.de.repository.RepositoryEventExternalSupplier repositoryEventExternalSupplier;

	@Autowired
	private com.zbs.de.repository.RepositoryExternalSupplierCategory repositoryExternalSupplierCategory;

	@Autowired
	private ServiceAppSettings serviceAppSettings;

	@Autowired
	private ServiceEventPricing serviceEventPricing;

	@Autowired
	private ServiceNotifications serviceNotifications;

	@Autowired
	private RepositoryEventPaymentMaster repositoryEventPaymentMaster;

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceEventMasterImpl.class);

	/**
	 * Records what has been paid, without letting a stale form overwrite it.
	 *
	 * <h3>What was happening</h3>
	 *
	 * Two things owned {@code num_paid_amount} and the wrong one won.
	 * {@code ServiceEventBudgetImpl.recalculateBudget} sums the payments actually
	 * recorded against the budget, which is right. These four save methods set it
	 * to whatever the event form sent — and the form sends the figure it loaded,
	 * which was read before the payment was taken.
	 *
	 * <p>
	 * So: take a £500 payment, then save the booking from a form that was already
	 * open, and the £500 disappears from every figure that matters. It is in
	 * production. Budget 146 shows {@code num_paid_amount = 0.00} against a £500
	 * payment, and its {@code updated_date} is four seconds after the payment's —
	 * the save that followed, carrying the zero it had loaded.
	 *
	 * <p>
	 * Nothing announces it. The payment row is still there, the receipt was still
	 * sent, and the booking simply reads as unpaid.
	 *
	 * <h3>The rule</h3>
	 *
	 * Where payments exist, they are the authority and the form cannot write over
	 * them. Where none do, the figure the form sends is used unchanged — which is
	 * how the catering form works, and it has a Paid Amount box that is the only
	 * record of money taken for a delivery. Both production payments belong to
	 * events, not deliveries, so this narrows nothing that is in use.
	 *
	 * @param budget    the budget being saved
	 * @param requested what the form says has been paid
	 */
	private void setPaidAmount(EventBudget budget, BigDecimal requested) {

		BigDecimal fromPayments = budget.getSerEventBudgetId() == null
				? null
				: repositoryEventPaymentMaster.sumPaidByBudgetId(budget.getSerEventBudgetId());

		if (fromPayments != null && fromPayments.compareTo(BigDecimal.ZERO) > 0) {

			if (requested == null || requested.compareTo(fromPayments) != 0) {
				LOGGER.warn("Budget {} was sent a paid amount of {} but {} has been recorded in payments; "
						+ "keeping the payments — see PLATFORM.md §15.3 stage 3",
						budget.getSerEventBudgetId(), requested, fromPayments);
			}

			budget.setNumPaidAmount(fromPayments);
			return;
		}

		budget.setNumPaidAmount(requested == null ? BigDecimal.ZERO : requested);
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

	/** Event references restart at this serial each year. */
	private static final int FIRST_SERIAL_OF_YEAR = 1001;

	/** How many consecutive taken codes to step over before giving up. */
	private static final int MAX_CODE_ATTEMPTS = 100;

	/**
	 * The next free event reference for this year, as {@code DE-<yy>-<serial>}.
	 *
	 * <p>
	 * This reads the highest code and adds one, which on its own is a race: two
	 * customers starting a booking in the same moment both read DE-26-1004 and
	 * both are handed DE-26-1005. The reference is what a customer quotes on the
	 * phone and what staff search on, so two events sharing one is a real
	 * problem rather than a cosmetic one.
	 *
	 * <p>
	 * Two things close it. The loop below skips any code already taken, which
	 * covers the common case of a gap or a code claimed since the maximum was
	 * read. And {@code ux_event_master_code} (migration V5) makes a genuine
	 * simultaneous collision fail the insert instead of quietly creating a
	 * duplicate — a failed save the customer can retry is a far better outcome
	 * than two bookings with the same number, which nobody notices until someone
	 * telephones.
	 *
	 * <p>
	 * A per-year database sequence would remove the race entirely and is the
	 * right long-term answer; it needs the existing production codes migrated
	 * onto it, which is not something to do blind.
	 */
	@Override
	public String generateNextEventMasterCode() {
		int year = LocalDate.now().getYear() % 100;

		String maxCode = repositoryEventMaster.findMaxEventCodeForYear(year);

		int nextSerial = FIRST_SERIAL_OF_YEAR;

		if (maxCode != null && maxCode.startsWith("DE-" + year + "-")) {
			try {
				String serialPart = maxCode.substring(maxCode.lastIndexOf("-") + 1);
				nextSerial = Integer.parseInt(serialPart) + 1;
			} catch (NumberFormatException e) {
				LOGGER.warn("Event code {} does not end in a serial; starting the year again.", maxCode);
				nextSerial = FIRST_SERIAL_OF_YEAR;
			}
		}

		for (int attempt = 0; attempt < MAX_CODE_ATTEMPTS; attempt++) {
			String candidate = String.format("DE-%02d-%04d", year, nextSerial + attempt);
			if (!repositoryEventMaster.existsByTxtEventMasterCode(candidate)) {
				return candidate;
			}
		}

		// Bounded rather than infinite: if this many consecutive codes are taken,
		// something is wrong with the numbering rather than with this booking, and
		// spinning would turn that into a hung request.
		throw new IllegalStateException(
				"Could not find a free event reference for 20" + year + " after " + MAX_CODE_ATTEMPTS
						+ " attempts starting at " + nextSerial);
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
					try {
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
								dto.setDtoEventVenue(dtoEventVenue);
							}
						}
					} catch (Exception ex) {
						LOGGER.debug("Failed to fetch venue detail for event {}: {}", eventMaster.getSerEventMasterId(),
								ex.getMessage(), ex);
					}

					// Fetching Decor
					// ***********************************
					List<DtoEventDecorCategorySelection> eventDecorCategorySelections = serviceEventDecorCategorySelection
							.getSelectionsWithChosenValues(dto.getSerEventMasterId());
					dto.setDtoEventDecorSelections(eventDecorCategorySelections);

//					// Fetching Event Menu Food Selection
//					// ***********************************
//
//					List<EventMenuFoodSelection> eventMenuFoodSelections = serviceEventMenuFoodSelection
//							.getByEventMasterId(dto.getSerEventMasterId());
////					List<DtoEventMenuFoodSelection> dtoEventMenuFoodSelections = new ArrayList<>();
////					if (UtilRandomKey.isNotNull(eventMenuFoodSelections)) {
////						for (EventMenuFoodSelection entity : eventMenuFoodSelections) {
////							DtoEventMenuFoodSelection dtoEventMenuFoodSelection = MapperEventMenuFoodSelection
////									.toDto(entity);
////							dtoEventMenuFoodSelections.add(dtoEventMenuFoodSelection);
////						}
////					}
////					dto.setFoodSelections(dtoEventMenuFoodSelections);
//					
//					List<DtoMenuFoodMaster> foodSelections = new ArrayList<>();
//					if (UtilRandomKey.isNotNull(eventMenuFoodSelections)) {
//						for (EventMenuFoodSelection entity : eventMenuFoodSelections) {
//							if (entity.getMenuItem() != null) {
//								DtoMenuFoodMaster dtoMenuFoodMaster = new DtoMenuFoodMaster();
//								MenuItem menuItem = entity.getMenuItem();
//								dtoMenuFoodMaster.setBlnIsActive(menuItem.getBlnIsActive());
//								dtoMenuFoodMaster.setSerMenuItemId(menuItem.getSerMenuItemId());
//								dtoMenuFoodMaster.setTxtName(menuItem.getTxtName());
//								dtoMenuFoodMaster.setTxtCode(menuItem.getTxtCode());
//								dtoMenuFoodMaster.setTxtDescription(menuItem.getTxtDescription());
//								dtoMenuFoodMaster.setNumPrice(entity.getNumPrice());
//								foodSelections.add(dtoMenuFoodMaster);
//							}
//						}
//					}
//					dto.setFoodSelections(foodSelections);
					
					
					
					// **********************************************************************************************
					// ************************ Food Selections Category and SubCategory Pricing ********************
					// **********************************************************************************************
					List<DtoCustomerMenuCategory> catDtos = new ArrayList<>();

					for (EventMenuCategorySelection cat : eventMaster.getMenuCategorySelections()) {

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
								if (mi != null) {
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
							}

							subDto.setItems(itemDtos);
							subDtos.add(subDto);
						}

						catDto.setSubCategories(subDtos);
						catDtos.add(catDto);
					}
					dto.setMenuCategoriesSelection(catDtos);

					// 4) Decor extras selections
					try {
						List<EventDecorExtrasSelection> extras = serviceEventDecorExtrasSelection
								.getExtrasSelectionsByEventMasterId(dto.getSerEventMasterId());
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
						dto.setExtrasSelections(dtoExtras);
					} catch (Exception ex) {
						LOGGER.debug("Failed to fetch extras selections for event {}: {}", dto.getSerEventMasterId(),
								ex.getMessage(), ex);
						dto.setExtrasSelections(new ArrayList<>());
					}

					// 4) Decor services selections
					try {
						List<EventDecorExtrasSelection> services = serviceEventDecorExtrasSelection
								.getServicesSelectionsByEventMasterId(dto.getSerEventMasterId());
						List<DtoEventDecorExtrasSelection> dtoServices = new ArrayList<>();
						if (UtilRandomKey.isNotNull(services)) {
							for (EventDecorExtrasSelection entity : services) {
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
								dtoServices.add(e);
							}
						}
						dto.setServicesSelections(dtoServices);
					} catch (Exception ex) {
						LOGGER.debug("Failed to fetch services selections for event {}: {}", dto.getSerEventMasterId(),
								ex.getMessage(), ex);
						dto.setExtrasSelections(new ArrayList<>());
					}
					
					// **********************************************************************************************
					// **********************************************************************************************
					// **********************************************************************************************

					
					

					// 5) Budget / quoted price and status
					try {
						EventBudget eventBudget = serviceEventBudget.getEventBudgetByEventId(dto.getSerEventMasterId());
						if (eventBudget != null) {
							DtoEventQuoteAndStatus quote = new DtoEventQuoteAndStatus();
							quote.setNumQuotedPrice(eventBudget.getNumQuotedPrice());
							quote.setNumPaidAmount(eventBudget.getNumPaidAmount());
							quote.setTxtStatus(eventBudget.getTxtStatus());
							quote.setNumDiscount(eventBudget.getNumDiscount());
							quote.setNumDecorAmount(eventBudget.getNumDecorAmount());
							quote.setNumFoodAmount(eventBudget.getNumFoodAmount());
							quote.setNumServicesAmount(eventBudget.getNumServicesAmount());
							quote.setNumDecorExtrasVat(eventBudget.getNumDecorExtrasVat());
							quote.setNumFinalAmount(eventBudget.getNumFinalAmount());
							dto.setDtoEventQuoteAndStatus(quote);
						}
					} catch (Exception ex) {
						LOGGER.debug("Failed to fetch budget for event {}: {}", dto.getSerEventMasterId(), ex.getMessage(),
								ex);
					}


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
		BigDecimal numDecorCategoryPrice= BigDecimal.ZERO;
		BigDecimal numDecorPropertyPrice= BigDecimal.ZERO;
		BigDecimal numServicesPrice = BigDecimal.ZERO;
		BigDecimal numFoodCategoryPrice = BigDecimal.ZERO;
		BigDecimal numFoodSubcategoryPrice = BigDecimal.ZERO;
		
		try {
			if (dtoEventMaster.getSerCustId() == null || dtoEventMaster.getSerEventTypeId() == null) {
				LOGGER.debug("Customer ID and Event Type ID are required");
				dtoResult.setTxtMessage("Customer ID and Event Type ID are required");
				return dtoResult;
			}

			DtoResult unreadableDate = refuseUnreadableDate(dtoEventMaster.getDteEventDate());
			if (unreadableDate != null) {
				return unreadableDate;
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

				// Refused rather than allowed to overwrite somebody else's changes.
				// See hasChangedElsewhere for who counts as somebody else.
				if (hasChangedElsewhere(entity, dtoEventMaster.getNumVersion())) {
					dtoResult.setTxtMessage(CHANGED_ELSEWHERE);
					dtoResult.setResult(CHANGED_ELSEWHERE_MESSAGE);
					return dtoResult;
				}

				// Recorded for the check above, on the next save.
				Integer savingNow = ServiceCurrentUser.getCurrentUserId();
				if (savingNow != null) {
					entity.setUpdatedBy(savingNow);
				}

				// ****Check if Edit Allowed or Not***
				if (entity.getIsEditAllowed() != null && entity.getIsEditAllowed() == false) {
					LOGGER.debug("This Event Can't be deleted as Event Is Marked For Restrict Edit.");
					dtoResult.setTxtMessage(
							"You do not have permission to edit this event. Contact Diamond Event administration for changes!");
					return dtoResult;
				}

				// Manually update values (keep ID)
				entity.setTxtEventMasterName(dtoEventMaster.getTxtEventMasterName());
				Date newDate= UtilDateAndTime.parseDateFromClient(dtoEventMaster.getDteEventDate());

				if(newDate != null && entity.getDteEventDate() != null && newDate.compareTo(entity.getDteEventDate()) != 0) {
//					Boolean isalreadyBooked = repositoryEventMaster.existsByDteEventDateAndBlnIsDeletedFalse(newDate);
//					if(isalreadyBooked) {
//						dtoResult.setTxtMessage("already_booked");
//						return dtoResult;
//					}
					DtoEventBookingValidationResult bookingValidation =  this.canBookEvent(newDate, entity.getDteEventDate(), entity.getSerEventMasterId());
					if(!bookingValidation.isAllowed()) {
						dtoResult.setTxtMessage("already_booked");
						dtoResult.setResult(bookingValidation.getMessage());
						return dtoResult;
					}
				}
				setEventDate(entity, dtoEventMaster.getDteEventDate());
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
				entity.setTxtEventServicesRemarks(dtoEventMaster.getTxtEventServicesRemarks());
//				if (UtilRandomKey.isNull(entity.getNumInfoFilledStatus())) {
//					entity.setNumInfoFilledStatus(0);
//				}

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
				applyRunningOrder(dtoEventMaster.getDtoEventRunningOrder(), entity);

				// Setting Venue Master
				// ********************

//				// This is for which you only need to specify which venu is selected
//				if (UtilRandomKey.isNotNull(dtoEventMaster.getSerVenueMasterId())) {
//					VenueMaster venueMaster = serviceVenueMaster.getByPK(dtoEventMaster.getSerVenueMasterId());
//					if (UtilRandomKey.isNull(venueMaster)) {
//						dtoResult.setTxtMessage("Venue Not Found For Id: " + dtoEventMaster.getSerVenueMasterId());
//						return dtoResult;
//					}
//					entity.setVenueMaster(venueMaster);
////					entity.setNumInfoFilledStatus(50);
//				}

				//This is For when you need to save which hall of the venu was selected				
				DtoResult venueRefusal = applyVenue(dtoEventMaster.getDtoEventVenue(), entity);
				if (venueRefusal != null) {
					return venueRefusal;
				}

				// Set Decor Item Selections
				// *************************
				DecorTotals decorTotals = applyDecorSelections(dtoEventMaster.getDtoEventDecorSelections(), entity,
						decorCategoryPropertyMasterLst, decorCategoryPropertyValueLst, files, fileMap, JOURNEY_DECOR);
				numDecorCategoryPrice = numDecorCategoryPrice.add(decorTotals.categories());
				numDecorPropertyPrice = numDecorPropertyPrice.add(decorTotals.properties());

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


//				// Setting Event Quoted Price
//				// **************************
//
//				eventBudget = serviceEventBudget.getEventBudgetByEventId(entity.getSerEventMasterId());
//				if (eventBudget != null) {
//					if (eventBudget.getNumPaidAmount() != null
//							&& eventBudget.getNumPaidAmount().compareTo(BigDecimal.ZERO) == 1) {
//						eventBudget.setTxtStatus("Confirmed");
//
//					} else if (dtoEventMaster.getDtoEventQuoteAndStatus() != null
//							&& dtoEventMaster.getDtoEventQuoteAndStatus().getNumQuotedPrice() != null && dtoEventMaster
//									.getDtoEventQuoteAndStatus().getNumQuotedPrice().compareTo(BigDecimal.ZERO) == 1) {
//						eventBudget.setTxtStatus("Quoted");
//						eventBudget.setNumQuotedPrice(dtoEventMaster.getDtoEventQuoteAndStatus().getNumQuotedPrice());
//						eventBudget.setNumPaidAmount(BigDecimal.ZERO);
//
//					} else {
//						eventBudget.setTxtStatus("Enquiry");
//						eventBudget.setNumQuotedPrice(BigDecimal.ZERO);
//						eventBudget.setNumPaidAmount(BigDecimal.ZERO);
//
//					}
//
//				} else {
//					eventBudget = new EventBudget();
//					if (dtoEventMaster.getDtoEventQuoteAndStatus() != null && dtoEventMaster.getDtoEventQuoteAndStatus().getNumQuotedPrice() != null && dtoEventMaster
//							.getDtoEventQuoteAndStatus().getNumQuotedPrice().compareTo(BigDecimal.ZERO) == 1) {
//						eventBudget.setTxtStatus("Quoted");
//						eventBudget.setNumQuotedPrice(dtoEventMaster.getDtoEventQuoteAndStatus().getNumQuotedPrice());
//						eventBudget.setNumPaidAmount(BigDecimal.ZERO);
//
//					} else {
//						eventBudget.setTxtStatus("Enquiry");
//						eventBudget.setNumQuotedPrice(BigDecimal.ZERO);
//						eventBudget.setNumPaidAmount(BigDecimal.ZERO);
//
//					}
//
//				}
				// Setting Event Quoted Price
				// **************************
				eventBudget = serviceEventBudget.getEventBudgetByEventId(entity.getSerEventMasterId());

				if (eventBudget == null) {
					eventBudget = new EventBudget();
				}

				DtoEventQuoteAndStatus quoteAndStatus = dtoEventMaster.getDtoEventQuoteAndStatus();

//				boolean hasIncomingPaidAmount = quoteAndStatus != null
//				        && quoteAndStatus.getNumPaidAmount() != null
//				        && quoteAndStatus.getNumPaidAmount().compareTo(BigDecimal.ZERO) > 0;
//
//				boolean hasExistingPaidAmount = eventBudget.getNumPaidAmount() != null
//				        && eventBudget.getNumPaidAmount().compareTo(BigDecimal.ZERO) > 0;
//
//				boolean hasQuotedPrice = quoteAndStatus != null
//				        && quoteAndStatus.getNumQuotedPrice() != null
//				        && quoteAndStatus.getNumQuotedPrice().compareTo(BigDecimal.ZERO) > 0;
//
//				BigDecimal discount = (quoteAndStatus != null && quoteAndStatus.getNumDiscount() != null)
//				        ? quoteAndStatus.getNumDiscount()
//				        : BigDecimal.ZERO;

				boolean hasIncomingPaidAmount = quoteAndStatus != null && quoteAndStatus.getNumPaidAmount() != null
						&& quoteAndStatus.getNumPaidAmount().compareTo(BigDecimal.ZERO) > 0;

				boolean hasExistingPaidAmount = eventBudget.getNumPaidAmount() != null
						&& eventBudget.getNumPaidAmount().compareTo(BigDecimal.ZERO) > 0;

				boolean hasQuotedPrice = quoteAndStatus != null && quoteAndStatus.getNumQuotedPrice() != null
						&& quoteAndStatus.getNumQuotedPrice().compareTo(BigDecimal.ZERO) > 0;

				boolean hasDecorPrice = quoteAndStatus != null && quoteAndStatus.getNumDecorAmount() != null
						&& quoteAndStatus.getNumDecorAmount().compareTo(BigDecimal.ZERO) > 0;

				boolean hasServicePrice = quoteAndStatus != null && quoteAndStatus.getNumServicesAmount() != null
						&& quoteAndStatus.getNumServicesAmount().compareTo(BigDecimal.ZERO) > 0;

				boolean hasFoodPrice = quoteAndStatus != null && quoteAndStatus.getNumFoodAmount() != null
						&& quoteAndStatus.getNumFoodAmount().compareTo(BigDecimal.ZERO) > 0;

				boolean hasFinalPrice = quoteAndStatus != null && quoteAndStatus.getNumFinalAmount() != null
						&& quoteAndStatus.getNumFinalAmount().compareTo(BigDecimal.ZERO) > 0;

				boolean hasDecorVat = quoteAndStatus != null && quoteAndStatus.getNumDecorExtrasVat() != null
						&& quoteAndStatus.getNumDecorExtrasVat().compareTo(BigDecimal.ZERO) > 0;

				boolean hasDiscount = quoteAndStatus != null && quoteAndStatus.getNumDiscount() != null
						&& quoteAndStatus.getNumDiscount().compareTo(BigDecimal.ZERO) > 0;

				if (hasIncomingPaidAmount || hasExistingPaidAmount) {
					eventBudget.setTxtStatus("Confirmed");
//				    eventBudget.setNumDiscount(discount);
//
//				    if (hasIncomingPaidAmount) {
//				        eventBudget.setNumPaidAmount(quoteAndStatus.getNumPaidAmount());
//				    }
//				    if (hasQuotedPrice) {
//				        eventBudget.setNumQuotedPrice(quoteAndStatus.getNumQuotedPrice());
//				    }
//
//				    // Derive VAT and final amount from whatever quoted price is now on the record
//				    BigDecimal effectiveQuotedPrice = safeValue(eventBudget.getNumQuotedPrice());
//				    eventBudget.setNumDecorExtrasVat(
//				        calculateDecorExtrasVat(safeValue(
//				            quoteAndStatus != null ? quoteAndStatus.getNumDecorAmount() : null)));
//				    eventBudget.setNumFinalAmount(calculateFinalAmount(effectiveQuotedPrice, discount));
//
//				    if (quoteAndStatus != null) {
//				        applyAmountFields(eventBudget, quoteAndStatus);
//				    }

				} else if (hasQuotedPrice) {
					eventBudget.setTxtStatus("Quoted");
//				    eventBudget.setNumQuotedPrice(quoteAndStatus.getNumQuotedPrice());
//				    eventBudget.setNumPaidAmount(BigDecimal.ZERO);
//				    eventBudget.setNumDiscount(discount);
//				    applyAmountFields(eventBudget, quoteAndStatus);

				} else {
					eventBudget.setTxtStatus("Enquiry");
//				    eventBudget.setNumQuotedPrice(BigDecimal.ZERO);
//				    eventBudget.setNumPaidAmount(BigDecimal.ZERO);
//				    eventBudget.setNumDiscount(BigDecimal.ZERO);
//				    eventBudget.setNumFoodAmount(BigDecimal.ZERO);
//				    eventBudget.setNumServicesAmount(BigDecimal.ZERO);
//				    eventBudget.setNumDecorAmount(BigDecimal.ZERO);
//				    eventBudget.setNumDecorExtrasVat(BigDecimal.ZERO);
//				    eventBudget.setNumFinalAmount(BigDecimal.ZERO);
				}

				if (hasIncomingPaidAmount) {
					setPaidAmount(eventBudget, quoteAndStatus.getNumPaidAmount());
				} else {
					setPaidAmount(eventBudget, BigDecimal.ZERO);
				}

				if (hasQuotedPrice) {
					eventBudget.setNumQuotedPrice(quoteAndStatus.getNumQuotedPrice());
				} else {
					eventBudget.setNumQuotedPrice(BigDecimal.ZERO);
				}

				if (hasFinalPrice) {
					eventBudget.setNumFinalAmount(quoteAndStatus.getNumFinalAmount());
				} else {
					eventBudget.setNumFinalAmount(BigDecimal.ZERO);
				}

				if (hasDecorPrice) {
					eventBudget.setNumDecorAmount(quoteAndStatus.getNumDecorAmount());
				} else {
					eventBudget.setNumDecorAmount(BigDecimal.ZERO);
				}

				if (hasServicePrice) {
					eventBudget.setNumServicesAmount(quoteAndStatus.getNumServicesAmount());
				} else {
					eventBudget.setNumServicesAmount(BigDecimal.ZERO);
				}

				if (hasFoodPrice) {
					eventBudget.setNumFoodAmount(quoteAndStatus.getNumFoodAmount());
				} else {
					eventBudget.setNumFoodAmount(BigDecimal.ZERO);
				}

				if (hasDecorVat) {
					eventBudget.setNumDecorExtrasVat(quoteAndStatus.getNumDecorExtrasVat());
				} else {
					eventBudget.setNumDecorExtrasVat(BigDecimal.ZERO);
				}

				if (hasDiscount) {
					eventBudget.setNumDiscount(quoteAndStatus.getNumDiscount());
				} else {
					eventBudget.setNumDiscount(BigDecimal.ZERO);
				}

			} else {

				// ******************************************
				// *************** Create New ***************
				// ******************************************

				blnIsNewEvent = true;
				entity = MapperEventMaster.toEntity(dtoEventMaster);
				if(dtoEventMaster.getDteEventDate() != null) {
					Date newDate = UtilDateAndTime.parseDateFromClient(dtoEventMaster.getDteEventDate());

//					Boolean isalreadyBooked = repositoryEventMaster.existsByDteEventDateAndBlnIsDeletedFalse(newDate);
//					if (newDate != null && isalreadyBooked) {
//						dtoResult.setTxtMessage("already_booked");
//						return dtoResult;
//					}
					
					DtoEventBookingValidationResult bookingValidation =  this.canBookEvent(newDate, null, null);
					if(!bookingValidation.isAllowed()) {
						dtoResult.setTxtMessage("already_booked");
						dtoResult.setResult(bookingValidation.getMessage());
						return dtoResult;
					}

				}

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
				applyRunningOrder(dtoEventMaster.getDtoEventRunningOrder(), entity);

				// Set Venue Master
				// ****************

//				// This is for which you only need to specify which venu is selected
//				if (UtilRandomKey.isNotNull(dtoEventMaster.getSerVenueMasterId())) {
//					VenueMaster venueMaster = serviceVenueMaster.getByPK(dtoEventMaster.getSerVenueMasterId());
//					if (UtilRandomKey.isNull(venueMaster)) {
//						dtoResult.setTxtMessage("Venue Not Found For Id: " + dtoEventMaster.getSerVenueMasterId());
//						return dtoResult;
//					}
//					entity.setVenueMaster(venueMaster);
////					entity.setNumInfoFilledStatus(50);
//				}

				//This is For when you need to save which hall of the venu was selected				
				DtoResult venueRefusal = applyVenue(dtoEventMaster.getDtoEventVenue(), entity);
				if (venueRefusal != null) {
					return venueRefusal;
				}

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

//								DecorCategoryPropertyValue matchedValue = decorCategoryPropertyValueLst.stream()
//										.filter(pv -> pv.getSerPropertyValueId().intValue() == property
//												.getSerPropertyValueId().intValue())
//										.findFirst().orElse(null);
								
								//************************************************
								Set<EventDecorPropertyValueSelection> selectedValues = new HashSet<>();

								for (Integer valueId : property.getSerPropertyValueIds()) {

								    DecorCategoryPropertyValue matchedValue = decorCategoryPropertyValueLst.stream()
								        .filter(pv -> pv.getSerPropertyValueId().intValue() == valueId)
								        .findFirst()
								        .orElse(null);

								    EventDecorPropertyValueSelection val = new EventDecorPropertyValueSelection();
								    val.setEventDecorPropertySelection(eventDecorPropertySelection);
								    val.setPropertyValue(matchedValue);

								    selectedValues.add(val);
								}

								eventDecorPropertySelection.setSelectedValues(selectedValues);
								
								
								//*************************************************

								eventDecorPropertySelection.setProperty(matchedMaster);
//								eventDecorPropertySelection.setSelectedValue(matchedValue);
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

				// entity.setNumInfoFilledStatus(100);

				// Generate event master code
				String code = generateNextEventMasterCode();
				entity.setTxtEventMasterCode(code);

				// A booking above it, so that stage 3 has a parent to move the budget
				// and the payments onto. See giveItABooking.
				giveItABooking(entity);

//				// Setting up Event QuotePrice
//				// ***************************
//
//				eventBudget = new EventBudget();
//				if (dtoEventMaster.getDtoEventQuoteAndStatus() != null
//						&& dtoEventMaster.getDtoEventQuoteAndStatus().getNumQuotedPrice() != null && dtoEventMaster
//								.getDtoEventQuoteAndStatus().getNumQuotedPrice().compareTo(BigDecimal.ZERO) == 1) {
//					eventBudget.setTxtStatus("Quoted");
//					eventBudget.setNumQuotedPrice(dtoEventMaster.getDtoEventQuoteAndStatus().getNumQuotedPrice());
//					eventBudget.setNumPaidAmount(BigDecimal.ZERO);
//
//				} else {
//					eventBudget.setTxtStatus("Enquiry");
//					eventBudget.setNumQuotedPrice(BigDecimal.ZERO);
//					eventBudget.setNumPaidAmount(BigDecimal.ZERO);
//
//				}
				
				// Setting Event Quoted Price
				// **************************
				eventBudget = new EventBudget();

				DtoEventQuoteAndStatus quoteAndStatus = dtoEventMaster.getDtoEventQuoteAndStatus();

				boolean hasPaidAmount = quoteAndStatus != null && quoteAndStatus.getNumPaidAmount() != null
						&& quoteAndStatus.getNumPaidAmount().compareTo(BigDecimal.ZERO) > 0;

//				boolean hasQuotedPrice = quoteAndStatus != null
//				        && quoteAndStatus.getNumQuotedPrice() != null
//				        && quoteAndStatus.getNumQuotedPrice().compareTo(BigDecimal.ZERO) > 0;
//
//				BigDecimal discount = (quoteAndStatus != null && quoteAndStatus.getNumDiscount() != null)
//				        ? quoteAndStatus.getNumDiscount()
//				        : BigDecimal.ZERO;

				boolean hasIncomingPaidAmount = quoteAndStatus != null && quoteAndStatus.getNumPaidAmount() != null
						&& quoteAndStatus.getNumPaidAmount().compareTo(BigDecimal.ZERO) > 0;

				boolean hasExistingPaidAmount = eventBudget.getNumPaidAmount() != null
						&& eventBudget.getNumPaidAmount().compareTo(BigDecimal.ZERO) > 0;

				boolean hasQuotedPrice = quoteAndStatus != null && quoteAndStatus.getNumQuotedPrice() != null
						&& quoteAndStatus.getNumQuotedPrice().compareTo(BigDecimal.ZERO) > 0;

				boolean hasDecorPrice = quoteAndStatus != null && quoteAndStatus.getNumDecorAmount() != null
						&& quoteAndStatus.getNumDecorAmount().compareTo(BigDecimal.ZERO) > 0;

				boolean hasServicePrice = quoteAndStatus != null && quoteAndStatus.getNumServicesAmount() != null
						&& quoteAndStatus.getNumServicesAmount().compareTo(BigDecimal.ZERO) > 0;

				boolean hasFoodPrice = quoteAndStatus != null && quoteAndStatus.getNumFoodAmount() != null
						&& quoteAndStatus.getNumFoodAmount().compareTo(BigDecimal.ZERO) > 0;

				boolean hasFinalPrice = quoteAndStatus != null && quoteAndStatus.getNumFinalAmount() != null
						&& quoteAndStatus.getNumFinalAmount().compareTo(BigDecimal.ZERO) > 0;

				boolean hasDecorVat = quoteAndStatus != null && quoteAndStatus.getNumDecorExtrasVat() != null
						&& quoteAndStatus.getNumDecorExtrasVat().compareTo(BigDecimal.ZERO) > 0;

				boolean hasDiscount = quoteAndStatus != null && quoteAndStatus.getNumDiscount() != null
						&& quoteAndStatus.getNumDiscount().compareTo(BigDecimal.ZERO) > 0;

				if (hasPaidAmount) {
					eventBudget.setTxtStatus("Confirmed");
//				    eventBudget.setNumPaidAmount(quoteAndStatus.getNumPaidAmount());
//				    eventBudget.setNumDiscount(discount);
//				    if (hasQuotedPrice) {
//				        eventBudget.setNumQuotedPrice(quoteAndStatus.getNumQuotedPrice());
//				    }
//				    applyAmountFields(eventBudget, quoteAndStatus);

				} else if (hasQuotedPrice) {
					eventBudget.setTxtStatus("Quoted");
//				    eventBudget.setNumQuotedPrice(quoteAndStatus.getNumQuotedPrice());
//				    eventBudget.setNumPaidAmount(BigDecimal.ZERO);
//				    eventBudget.setNumDiscount(discount);
//				    applyAmountFields(eventBudget, quoteAndStatus);

				} else {
					eventBudget.setTxtStatus("Enquiry");
//				    eventBudget.setNumQuotedPrice(BigDecimal.ZERO);
//				    eventBudget.setNumPaidAmount(BigDecimal.ZERO);
//				    eventBudget.setNumDiscount(BigDecimal.ZERO);
//				    eventBudget.setNumFoodAmount(BigDecimal.ZERO);
//				    eventBudget.setNumServicesAmount(BigDecimal.ZERO);
//				    eventBudget.setNumDecorAmount(BigDecimal.ZERO);
//				    eventBudget.setNumDecorExtrasVat(BigDecimal.ZERO);
//				    eventBudget.setNumFinalAmount(BigDecimal.ZERO);
				}

				if (hasIncomingPaidAmount) {
					setPaidAmount(eventBudget, quoteAndStatus.getNumPaidAmount());
				} else {
					setPaidAmount(eventBudget, BigDecimal.ZERO);
				}

				if (hasQuotedPrice) {
					eventBudget.setNumQuotedPrice(quoteAndStatus.getNumQuotedPrice());
				} else {
					eventBudget.setNumQuotedPrice(BigDecimal.ZERO);
				}

				if (hasFinalPrice) {
					eventBudget.setNumFinalAmount(quoteAndStatus.getNumFinalAmount());
				} else {
					eventBudget.setNumFinalAmount(BigDecimal.ZERO);
				}

				if (hasDecorPrice) {
					eventBudget.setNumDecorAmount(quoteAndStatus.getNumDecorAmount());
				} else {
					eventBudget.setNumDecorAmount(BigDecimal.ZERO);
				}

				if (hasServicePrice) {
					eventBudget.setNumServicesAmount(quoteAndStatus.getNumServicesAmount());
				} else {
					eventBudget.setNumServicesAmount(BigDecimal.ZERO);
				}

				if (hasFoodPrice) {
					eventBudget.setNumFoodAmount(quoteAndStatus.getNumFoodAmount());
				} else {
					eventBudget.setNumFoodAmount(BigDecimal.ZERO);
				}

				if (hasDecorVat) {
					eventBudget.setNumDecorExtrasVat(quoteAndStatus.getNumDecorExtrasVat());
				} else {
					eventBudget.setNumDecorExtrasVat(BigDecimal.ZERO);
				}

				if (hasDiscount) {
					eventBudget.setNumDiscount(quoteAndStatus.getNumDiscount());
				} else {
					eventBudget.setNumDiscount(BigDecimal.ZERO);
				}
			}

			//*********************************************************************************************
			//************************ Food Menu Categories and Sub Categories ****************************
			//*********************************************************************************************
			MenuResult menuResult = applyMenuSelections(dtoEventMaster.getMenuCategoriesSelection(), entity, menuItems);
			entity = menuResult.entity();
			numFoodCategoryPrice = numFoodCategoryPrice.add(menuResult.categories());
			numFoodSubcategoryPrice = numFoodSubcategoryPrice.add(menuResult.subCategories());

			//*********************************************************************************************
			//*********************************************************************************************
			//*********************************************************************************************

			

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
					selection.setNumPrice(dto.getNumPrice());
			        selection.setBlnIsServices(false);
			    	if(selection.getNumPrice() != null) {
						numDecorCategoryPrice = numDecorCategoryPrice.add(selection.getNumPrice());
					}
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
			
			
			
			// ****** Setting Event Services ******

			if (entity.getServicesSelections() != null) {
			    entity.getServicesSelections().clear();
			} else {
			    entity.setServicesSelections(new ArrayList<>());
			}

			if (UtilRandomKey.isNotNull(dtoEventMaster.getServicesSelections())
			        && !dtoEventMaster.getServicesSelections().isEmpty()) {

			    List<EventDecorExtrasSelection> newServiceSelections = new ArrayList<>();

			    for (DtoEventDecorExtrasSelection dto : dtoEventMaster.getServicesSelections()) {

			        EventDecorExtrasSelection selection = new EventDecorExtrasSelection();

			        selection.setTxtDynamicProperty1(dto.getTxtDynamicProperty1());
			        selection.setTxtDynamicProperty2(dto.getTxtDynamicProperty2());
			        selection.setNumPrice(dto.getNumPrice());
			        selection.setEventMaster(entity);
			        if(dto.getNumPrice()!=null) {
			        	numServicesPrice = numServicesPrice.add(dto.getNumPrice());
			        }

			        // 🔥 THIS IS THE DIFFERENCE
			        selection.setBlnIsServices(true);

			        if (dto.getSerExtrasId() != null) {
			            selection.setDecorExtrasMaster(
			                serviceDecorExtrasMaster.getByIdAndNotDeleted(dto.getSerExtrasId()));
			        }

			        if (dto.getSerExtraOptionId() != null) {
			            selection.setDecorExtrasOption(
			                serviceDecorExtrasOption.getByIdAndNotDeleted(dto.getSerExtraOptionId()));
			        }

			        newServiceSelections.add(selection);
			    }

			    entity.getServicesSelections().addAll(newServiceSelections);
			}
			
			//*********************************************************************************************
			//*********************************************************************************************
			//*********************************************************************************************


			// *********************************************************************************************
			//*********************************************************************************************
			//*********************************************************************************************

			
			

			entity.setNumInfoFilledStatus(getEventCompletionPercentage(entity));
//			entity = repositoryEventMaster.save(entity);
//			if (eventBudget != null) {
//				eventBudget.setEventMaster(entity);
//				serviceEventBudget.save(eventBudget);
//			}

			// ***** Sending Notification Of New Customer Registration *****
			if (blnIsNewEvent) {
				this.sendNewEventRegistrationNotification(
						entity.getEventType() != null ? entity.getEventType().getTxtEventTypeName() : "",
						entity.getTxtEventMasterCode() != null ? entity.getTxtEventMasterCode() : "",
						dtoEventMaster.getDteEventDate() != null ? dtoEventMaster.getDteEventDate() : "",
						entity.getSerEventMasterId());
			}

			if (this.isEventRegistrationCompleted(entity) && (dtoEventMaster.getBlnIsCE() == null || (dtoEventMaster.getBlnIsCE() != null && dtoEventMaster.getBlnIsCE()))) {
				UserMaster userMaster = ServiceCurrentUser.getCurrentUser();
				if (userMaster != null) {
					dtoResult.setTxtMessage("Success");
					if (entity.getBlnIsClientEmailSend() != null
							&& entity.getBlnIsClientEmailSend().equals(Boolean.FALSE)) {
						serviceEmailSender.sendEventRegistrationEmail(userMaster.getTxtEmail(), userMaster.getTxtName(),
								entity.getTxtEventMasterCode(), entity.getEventType().getTxtEventTypeName(),
								entity.getDteEventDate());
						entity.setBlnIsClientEmailSend(true);
						dtoResult.setTxtMessage(entity.getEventType().getTxtEventTypeName()
								+ " Event Has Been Registered. A Confirmation Email Has Been Sent To Your Registered Email.");
					}

					// ***** Send Email To All Admin Users ********
					if (entity.getBlnIsAllAdminEmailSend() != null
							&& entity.getBlnIsAllAdminEmailSend().equals(Boolean.FALSE)) {
						serviceEmailSender.sendEventRegistrationEmailToAdminUsers(userMaster.getTxtName(),
								entity.getTxtEventMasterCode(), entity.getEventType().getTxtEventTypeName(),
								entity.getDteEventDate());
						entity.setBlnIsAllAdminEmailSend(true);
						dtoResult.setTxtMessage(entity.getEventType().getTxtEventTypeName()
								+ " Event Has Been Registered. A Confirmation Email Has Been Sent To Your Registered Email.");
					}
				} else {
					dtoResult.setTxtMessage("Success");

				}
			} else {
				dtoResult.setTxtMessage("Success");
			}

			entity = repositoryEventMaster.save(entity);
			if (eventBudget != null) {
				eventBudget.setEventMaster(entity);
//				BigDecimal decorCategory = numDecorCategoryPrice != null ? numDecorCategoryPrice : BigDecimal.ZERO;
//				BigDecimal decorProperty = numDecorPropertyPrice != null ? numDecorPropertyPrice : BigDecimal.ZERO;
//
//				BigDecimal foodCategory = numFoodCategoryPrice != null ? numFoodCategoryPrice : BigDecimal.ZERO;
//				BigDecimal foodSubcategory = numFoodSubcategoryPrice != null ? numFoodSubcategoryPrice
//						: BigDecimal.ZERO;
//
//				BigDecimal services = numServicesPrice != null ? numServicesPrice : BigDecimal.ZERO;
//
//				eventBudget.setNumDecorAmount(decorCategory.add(decorProperty));
//				eventBudget.setNumFoodAmount(foodCategory.add(foodSubcategory));
//				eventBudget.setNumServicesAmount(services);
				serviceEventBudget.save(eventBudget);
			}
			
			recordTermsAcceptance(dtoEventMaster.getBlnTermsAccepted(), entity.getSerEventMasterId());
			replaceExternalSuppliers(dtoEventMaster.getExternalSuppliers(), entity);
			priceTheBooking(entity.getSerEventMasterId(), dtoEventMaster.getDtoEventQuoteAndStatus());

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
			if (eventMaster.getMenuCategorySelections() != null && !eventMaster.getMenuCategorySelections().isEmpty()) {
				percentage += 25;
			}
			if (eventMaster.getExtrasSelections() != null && !eventMaster.getExtrasSelections().isEmpty()) {
				percentage += 10;
			}

		}
		return percentage;
	}

	@Override
	/**
	 * One event, with everything hanging off it.
	 *
	 * <p>
	 * Transactional because it walks lazy collections — the menu categories, the
	 * decor and food selections — and without a session those throw
	 * {@code LazyInitializationException}. It worked in a browser only because
	 * open-in-view holds a session open for the whole request, so the fault was
	 * invisible until something called this outside a web request.
	 *
	 * <p>
	 * That was not a harmless dependency. The method catches everything and
	 * returns {@code null}, so the failure arrives at the caller as "no such
	 * event" — and this is the endpoint the journey now uses to open every
	 * booking a customer chooses.
	 */
	@Transactional
	public DtoEventMaster getEventById(Integer serEventId) {
		DtoResult dtoResult = new DtoResult();

		try {

			Optional<EventMaster> optEvent = repositoryEventMaster.findById(serEventId);
			if (UtilRandomKey.isNotNull(optEvent)) {
				EventMaster event = optEvent.get();
				DtoEventMaster dto = MapperEventMaster.toDto(event);

				// Fetching Event Venue Detail
				// ***********************************
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

							dto.setDtoEventVenue(dtoEventVenue);
						}
					}
				} catch (Exception ex) {
					LOGGER.debug("Failed to fetch venue detail for event {}: {}", event.getSerEventMasterId(),
							ex.getMessage(), ex);
				}


				// Fetching Decor
				// ***********************************
				List<DtoEventDecorCategorySelection> eventDecorCategorySelections = serviceEventDecorCategorySelection
						.getSelectionsWithChosenValues(dto.getSerEventMasterId());
				dto.setDtoEventDecorSelections(eventDecorCategorySelections);

				dto.setExternalSuppliers(readExternalSuppliers(dto.getSerEventMasterId()));

				
				
				// **********************************************************************************************
				// ************************ Food Selections Category and SubCategory Pricing ********************
				// **********************************************************************************************
				List<DtoCustomerMenuCategory> catDtos = new ArrayList<>();

				for (EventMenuCategorySelection cat : event.getMenuCategorySelections()) {

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


				// 4) Decor extras selections
				try {
					List<EventDecorExtrasSelection> extras = serviceEventDecorExtrasSelection
							.getExtrasSelectionsByEventMasterId(dto.getSerEventMasterId());
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
					dto.setExtrasSelections(dtoExtras);
				} catch (Exception ex) {
					LOGGER.debug("Failed to fetch extras selections for event {}: {}", event.getSerEventMasterId(),
							ex.getMessage(), ex);
					dto.setExtrasSelections(new ArrayList<>());
				}

				// 4) Decor services selections
				try {
					List<EventDecorExtrasSelection> services = serviceEventDecorExtrasSelection
							.getServicesSelectionsByEventMasterId(dto.getSerEventMasterId());
					List<DtoEventDecorExtrasSelection> dtoServices = new ArrayList<>();
					if (UtilRandomKey.isNotNull(services)) {
						for (EventDecorExtrasSelection entity : services) {
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
							dtoServices.add(e);
						}
					}
					dto.setServicesSelections(dtoServices);
				} catch (Exception ex) {
					LOGGER.debug("Failed to fetch services selections for event {}: {}", event.getSerEventMasterId(),
							ex.getMessage(), ex);
					dto.setExtrasSelections(new ArrayList<>());
				}
				
				// **********************************************************************************************
				// **********************************************************************************************
				// **********************************************************************************************

				
				// 5) Budget / quoted price and status
				try {
					EventBudget eventBudget = serviceEventBudget.getEventBudgetByEventId(event.getSerEventMasterId());
					if (eventBudget != null) {
						DtoEventQuoteAndStatus quote = new DtoEventQuoteAndStatus();
						quote.setNumQuotedPrice(eventBudget.getNumQuotedPrice());
						quote.setNumPaidAmount(eventBudget.getNumPaidAmount());
						quote.setTxtStatus(eventBudget.getTxtStatus());
						quote.setNumDiscount(eventBudget.getNumDiscount());
						quote.setNumDecorAmount(eventBudget.getNumDecorAmount());
						quote.setNumFoodAmount(eventBudget.getNumFoodAmount());
						quote.setNumServicesAmount(eventBudget.getNumServicesAmount());
						quote.setNumDecorExtrasVat(eventBudget.getNumDecorExtrasVat());
						quote.setNumFinalAmount(eventBudget.getNumFinalAmount());
						dto.setDtoEventQuoteAndStatus(quote);
					}
				} catch (Exception ex) {
					LOGGER.debug("Failed to fetch budget for event {}: {}", event.getSerEventMasterId(), ex.getMessage(),
							ex);
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

	/**
	 * Tells the office a booking has been taken.
	 *
	 * <h4>What this replaced, and why it never worked</h4>
	 *
	 * The previous version addressed the notification to
	 * {@code getCurrentUserId()} — the person who had just done the thing.
	 * When a booking arrives through the customer journey that person is the
	 * <em>customer</em>, so the customer was notified about their own booking
	 * and the office was told nothing. That is why {@code notification_master}
	 * holds no rows.
	 *
	 * <p>
	 * It also called {@code .longValue()} on a possibly-null id, inside the
	 * save's try block. A null current user did not skip the notification; it
	 * turned a successful save into a reported failure.
	 *
	 * <p>
	 * The target it carried was an API path rather than a route in the portal,
	 * so following one went nowhere a person could use.
	 */
	private void sendNewEventRegistrationNotification(String eventTypeName, String txtEventCode, String date,
			Integer eventId) {

		Integer actor = null;
		try {
			actor = ServiceCurrentUser.getCurrentUserId();
		} catch (Exception e) {
			// Nobody in context. The office still needs telling.
		}

		serviceNotifications.raise(
				new ServiceNotifications.Event(EnmNotificationCategory.BOOKING_TAKEN,
						(eventTypeName == null || eventTypeName.isBlank() ? "An event" : eventTypeName)
								+ " booked for " + date)
						.body("Reference " + txtEventCode + ". Nothing has been quoted yet.")
						.about("EVENT", eventId == null ? null : eventId.longValue(),
								"/admin/event-master/" + eventId)
						.causedBy(actor == null ? null : actor.longValue()));
	}

	@Override
	@Transactional
	public DtoResult saveAndUpdateWithDocsAdminPortal(DtoEventMasterAdminPortal dtoEventMasterAdminPortal,
			List<MultipartFile> files) throws IOException {
		// Validate required IDs
		DtoResult dtoResult = new DtoResult();
		BigDecimal numDecorCategoryPrice= BigDecimal.ZERO;
		BigDecimal numDecorPropertyPrice= BigDecimal.ZERO;
		BigDecimal numServicesPrice = BigDecimal.ZERO;
		BigDecimal numFoodCategoryPrice = BigDecimal.ZERO;
		BigDecimal numFoodSubcategoryPrice = BigDecimal.ZERO;
		try {
			if (dtoEventMasterAdminPortal.getSerCustId() == null
					|| dtoEventMasterAdminPortal.getSerEventTypeId() == null) {
				LOGGER.debug("Customer ID and Event Type ID are required");
				dtoResult.setTxtMessage("Customer ID and Event Type ID are required");
				return dtoResult;
			}

			DtoResult unreadableDate = refuseUnreadableDate(dtoEventMasterAdminPortal.getDteEventDate());
			if (unreadableDate != null) {
				return unreadableDate;
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

				// Refused rather than allowed to overwrite somebody else's changes.
				// See hasChangedElsewhere for who counts as somebody else.
				if (hasChangedElsewhere(entity, dtoEventMasterAdminPortal.getNumVersion())) {
					dtoResult.setTxtMessage(CHANGED_ELSEWHERE);
					dtoResult.setResult(CHANGED_ELSEWHERE_MESSAGE);
					return dtoResult;
				}

				// Recorded for the check above, on the next save.
				Integer savingNow = ServiceCurrentUser.getCurrentUserId();
				if (savingNow != null) {
					entity.setUpdatedBy(savingNow);
				}

				// Manually update values (keep ID)
				entity.setTxtEventMasterName(dtoEventMasterAdminPortal.getTxtEventMasterName());
				Date newDate= UtilDateAndTime.parseDateFromClient(dtoEventMasterAdminPortal.getDteEventDate());

				if(newDate != null && entity.getDteEventDate() != null && newDate.compareTo(entity.getDteEventDate()) != 0) {
//					Boolean isalreadyBooked = repositoryEventMaster.existsByDteEventDateAndBlnIsDeletedFalse(newDate);
//					if(isalreadyBooked) {
//						dtoResult.setTxtMessage("already_booked");
//						dtoResult.setResult(null);
//						return dtoResult;
//					}
					DtoEventBookingValidationResult bookingValidation =  this.canBookEvent(newDate, entity.getDteEventDate(), entity.getSerEventMasterId());
					if(!bookingValidation.isAllowed()) {
						dtoResult.setTxtMessage("already_booked");
						dtoResult.setResult(bookingValidation.getMessage());
						return dtoResult;
					}
				}
				setEventDate(entity, dtoEventMasterAdminPortal.getDteEventDate());
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
				entity.setTxtEventServicesRemarks(dtoEventMasterAdminPortal.getTxtEventServicesRemarks());
				entity.setIsEditAllowed(dtoEventMasterAdminPortal.getIsEditAllowed());
				entity.setTxtContactPersonFirstName(dtoEventMasterAdminPortal.getTxtContactPersonFirstName());
				entity.setTxtContactPersonLastName(dtoEventMasterAdminPortal.getTxtContactPersonLastName());
				entity.setTxtContactPersonPhoneNo(dtoEventMasterAdminPortal.getTxtContactPersonPhoneNo());
				entity.setNumItineraryPrice(dtoEventMasterAdminPortal.getNumItineraryPrice());
				entity.setNumServingDishesPrice(dtoEventMasterAdminPortal.getNumServingDishesPrice());
				entity.setNumDiscount(dtoEventMasterAdminPortal.getNumDiscount());
//				if (UtilRandomKey.isNull(entity.getNumInfoFilledStatus())) {
//					entity.setNumInfoFilledStatus(0);
//				}

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
				applyRunningOrder(dtoEventMasterAdminPortal.getDtoEventRunningOrder(), entity);

				// Setting Venue Master
				// ********************

//				// This is for which you only need to specify which venu is selected
//				if (UtilRandomKey.isNotNull(dtoEventMasterAdminPortal.getSerVenueMasterId())) {
//					VenueMaster venueMaster = serviceVenueMaster
//							.getByPK(dtoEventMasterAdminPortal.getSerVenueMasterId());
//					if (UtilRandomKey.isNull(venueMaster)) {
//						dtoResult.setTxtMessage(
//								"Venue Not Found For Id: " + dtoEventMasterAdminPortal.getSerVenueMasterId());
//						return dtoResult;
//					}
//					entity.setVenueMaster(venueMaster);
////					entity.setNumInfoFilledStatus(50);
//				}

				//This is For when you need to save which hall of the venu was selected				
				DtoResult venueRefusal = applyVenue(dtoEventMasterAdminPortal.getDtoEventVenue(), entity);
				if (venueRefusal != null) {
					return venueRefusal;
				}

				// Set Decor Item Selections
				// *************************
				

				DecorTotals decorTotals = applyDecorSelections(dtoEventMasterAdminPortal.getDtoEventDecorSelections(), entity,
						decorCategoryPropertyMasterLst, decorCategoryPropertyValueLst, files, fileMap, OFFICE_DECOR);
				numDecorCategoryPrice = numDecorCategoryPrice.add(decorTotals.categories());
				numDecorPropertyPrice = numDecorPropertyPrice.add(decorTotals.properties());

				

				// Setting Event Quoted Price
				// **************************
//				eventBudget = serviceEventBudget.getEventBudgetByEventId(entity.getSerEventMasterId());
//
//				if (eventBudget != null) {
//					if (eventBudget.getNumPaidAmount() != null
//							&& (eventBudget.getNumPaidAmount().compareTo(BigDecimal.ZERO) == 1
//									|| (dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus() != null
//											&& dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumPaidAmount()
//													.compareTo(BigDecimal.ZERO) == 1))) {
//						eventBudget.setTxtStatus("Confirmed");
//						if (dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus() != null && dtoEventMasterAdminPortal
//								.getDtoEventQuoteAndStatus().getNumPaidAmount().compareTo(BigDecimal.ZERO) == 1) {
//							eventBudget.setNumPaidAmount(
//									dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumPaidAmount());
//						}
//						if(dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus() != null && dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumDiscount() != null) {
//							eventBudget.setNumDiscount(dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumDiscount());
//						}else {
//							eventBudget.setNumDiscount(BigDecimal.ZERO);
//						}
//
//					} else if (dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus() != null
//							&& dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumQuotedPrice() != null
//							&& dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumQuotedPrice()
//									.compareTo(BigDecimal.ZERO) == 1) {
//						eventBudget.setTxtStatus("Quoted");
//						eventBudget.setNumQuotedPrice(
//								dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumQuotedPrice());
//						eventBudget.setNumPaidAmount(BigDecimal.ZERO);
//						if(dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus() != null && dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumDiscount() != null) {
//							eventBudget.setNumDiscount(dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumDiscount());
//						}else {
//							eventBudget.setNumDiscount(BigDecimal.ZERO);
//						}
//
//
//					} else {
//						eventBudget.setTxtStatus("Enquiry");
//						eventBudget.setNumQuotedPrice(BigDecimal.ZERO);
//						eventBudget.setNumPaidAmount(BigDecimal.ZERO);
//						eventBudget.setNumDiscount(BigDecimal.ZERO);
//						eventBudget.setNumFinalAmount(BigDecimal.ZERO);
//
//					}
//
//				} else {
//					eventBudget = new EventBudget();
//
//					if (dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus() != null && dtoEventMasterAdminPortal
//							.getDtoEventQuoteAndStatus().getNumPaidAmount().compareTo(BigDecimal.ZERO) == 1) {
//						eventBudget.setTxtStatus("Confirmed");
//						eventBudget.setNumPaidAmount(
//								dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumPaidAmount());
//						if (dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumQuotedPrice() != null
//								&& dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumQuotedPrice()
//										.compareTo(BigDecimal.ZERO) == 1) {
//							eventBudget.setNumQuotedPrice(
//									dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumQuotedPrice());
//						}
//						if(dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus() != null && dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumDiscount() != null) {
//							eventBudget.setNumDiscount(dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumDiscount());
//						}else {
//							eventBudget.setNumDiscount(BigDecimal.ZERO);
//						}
//
//
//					} else if (dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus() != null
//							&& dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumQuotedPrice() != null
//							&& dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumQuotedPrice()
//									.compareTo(BigDecimal.ZERO) == 1) {
//						eventBudget.setTxtStatus("Quoted");
//						eventBudget.setNumQuotedPrice(
//								dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumQuotedPrice());
//						eventBudget.setNumPaidAmount(BigDecimal.ZERO);
//						if(dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus() != null && dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumDiscount() != null) {
//							eventBudget.setNumDiscount(dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumDiscount());
//						}else {
//							eventBudget.setNumDiscount(BigDecimal.ZERO);
//						}
//
//
//					} else {
//						eventBudget.setTxtStatus("Enquiry");
//						eventBudget.setNumQuotedPrice(BigDecimal.ZERO);
//						eventBudget.setNumPaidAmount(BigDecimal.ZERO);
//						eventBudget.setNumDiscount(BigDecimal.ZERO);
//
//					}
//
//				}
				
				// Setting Event Quoted Price
				// **************************
				eventBudget = serviceEventBudget.getEventBudgetByEventId(entity.getSerEventMasterId());

				if (eventBudget == null) {
				    eventBudget = new EventBudget();
				}

				DtoEventQuoteAndStatus quoteAndStatus = dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus();

				boolean hasIncomingPaidAmount = quoteAndStatus != null
				        && quoteAndStatus.getNumPaidAmount() != null
				        && quoteAndStatus.getNumPaidAmount().compareTo(BigDecimal.ZERO) > 0;

				boolean hasExistingPaidAmount = eventBudget.getNumPaidAmount() != null
				        && eventBudget.getNumPaidAmount().compareTo(BigDecimal.ZERO) > 0;

				boolean hasQuotedPrice = quoteAndStatus != null
				        && quoteAndStatus.getNumQuotedPrice() != null
						&& quoteAndStatus.getNumQuotedPrice().compareTo(BigDecimal.ZERO) > 0;

				boolean hasDecorPrice = quoteAndStatus != null && quoteAndStatus.getNumDecorAmount() != null
						&& quoteAndStatus.getNumDecorAmount().compareTo(BigDecimal.ZERO) > 0;

				boolean hasServicePrice = quoteAndStatus != null && quoteAndStatus.getNumServicesAmount() != null
						&& quoteAndStatus.getNumServicesAmount().compareTo(BigDecimal.ZERO) > 0;

				boolean hasFoodPrice = quoteAndStatus != null && quoteAndStatus.getNumFoodAmount() != null
						&& quoteAndStatus.getNumFoodAmount().compareTo(BigDecimal.ZERO) > 0;

				boolean hasFinalPrice = quoteAndStatus != null && quoteAndStatus.getNumFinalAmount() != null
						&& quoteAndStatus.getNumFinalAmount().compareTo(BigDecimal.ZERO) > 0;
						
				boolean hasDecorVat = quoteAndStatus != null && quoteAndStatus.getNumDecorExtrasVat() != null 
						&& quoteAndStatus.getNumDecorExtrasVat().compareTo(BigDecimal.ZERO) > 0;
						
				boolean hasDiscount = quoteAndStatus != null && quoteAndStatus.getNumDiscount() != null &&
						quoteAndStatus.getNumDiscount().compareTo(BigDecimal.ZERO) > 0;
//						
//				BigDecimal discount = (quoteAndStatus != null && quoteAndStatus.getNumDiscount() != null)
//						? quoteAndStatus.getNumDiscount()
//						: BigDecimal.ZERO;

				if (hasIncomingPaidAmount || hasExistingPaidAmount) {
					eventBudget.setTxtStatus("Confirmed");
//					eventBudget.setNumDiscount(discount);
				    

//				    // Derive VAT and final amount from whatever quoted price is now on the record
//				    BigDecimal effectiveQuotedPrice = safeValue(eventBudget.getNumQuotedPrice());
//				    eventBudget.setNumDecorExtrasVat(
//				        calculateDecorExtrasVat(safeValue(
//				            quoteAndStatus != null ? quoteAndStatus.getNumDecorAmount() : null)));
//				    eventBudget.setNumFinalAmount(calculateFinalAmount(effectiveQuotedPrice, discount));

//				    if (quoteAndStatus != null) {
//				        applyAmountFields(eventBudget, quoteAndStatus);
//				    }

				} else if (hasQuotedPrice) {
				    eventBudget.setTxtStatus("Quoted");
//				    eventBudget.setNumQuotedPrice(quoteAndStatus.getNumQuotedPrice());
//				    eventBudget.setNumPaidAmount(BigDecimal.ZERO);
//				    eventBudget.setNumDiscount(discount);
//				    applyAmountFields(eventBudget, quoteAndStatus);

				} else {
				    eventBudget.setTxtStatus("Enquiry");
//				    eventBudget.setNumQuotedPrice(BigDecimal.ZERO);
//				    eventBudget.setNumPaidAmount(BigDecimal.ZERO);
//				    eventBudget.setNumDiscount(BigDecimal.ZERO);
//				    eventBudget.setNumFoodAmount(BigDecimal.ZERO);
//				    eventBudget.setNumServicesAmount(BigDecimal.ZERO);
//				    eventBudget.setNumDecorAmount(BigDecimal.ZERO);
//				    eventBudget.setNumDecorExtrasVat(BigDecimal.ZERO);
//				    eventBudget.setNumFinalAmount(BigDecimal.ZERO);
				}
				

			    if (hasIncomingPaidAmount) {
			        setPaidAmount(eventBudget, quoteAndStatus.getNumPaidAmount());
			    }else {
			    	setPaidAmount(eventBudget, BigDecimal.ZERO);
			    }
			   
			    if (hasQuotedPrice) {
			        eventBudget.setNumQuotedPrice(quoteAndStatus.getNumQuotedPrice());
			    }else {
			    	 eventBudget.setNumQuotedPrice(BigDecimal.ZERO);
			    }
			    
			    if(hasFinalPrice) {
			    	eventBudget.setNumFinalAmount(quoteAndStatus.getNumFinalAmount());
			    }else {
			    	eventBudget.setNumFinalAmount(BigDecimal.ZERO);
			    }
			    
			    if(hasDecorPrice) {
			    	eventBudget.setNumDecorAmount(quoteAndStatus.getNumDecorAmount());
			    }else {
			    	eventBudget.setNumDecorAmount(BigDecimal.ZERO);
			    }
			    
			    if(hasServicePrice) {
			    	eventBudget.setNumServicesAmount(quoteAndStatus.getNumServicesAmount());
			    }else {
			    	eventBudget.setNumServicesAmount(BigDecimal.ZERO);
			    }
			    
			    if(hasFoodPrice) {
			    	eventBudget.setNumFoodAmount(quoteAndStatus.getNumFoodAmount());
			    }else {
			    	eventBudget.setNumFoodAmount(BigDecimal.ZERO);
			    }
			    
			    if(hasDecorVat) {
			    	eventBudget.setNumDecorExtrasVat(quoteAndStatus.getNumDecorExtrasVat());
			    }else {
			    	eventBudget.setNumDecorExtrasVat(BigDecimal.ZERO);
			    }
			    
			    if(hasDiscount) {
			    	eventBudget.setNumDiscount(quoteAndStatus.getNumDiscount());
			    }else {
			    	eventBudget.setNumDiscount(BigDecimal.ZERO);
			    }

			} else {

				// ******************************************
				// *************** Create New ***************
				// ******************************************

				blnIsNewEvent = true;
				entity = MapperEventMaster.dtoEventMasterAdminPortalToEntity(dtoEventMasterAdminPortal);

				if (dtoEventMasterAdminPortal.getDteEventDate() != null) {
					Date newDate = UtilDateAndTime
							.parseDateFromClient(dtoEventMasterAdminPortal.getDteEventDate());

//					Boolean isalreadyBooked = repositoryEventMaster.existsByDteEventDateAndBlnIsDeletedFalse(newDate);
//					if (newDate != null && isalreadyBooked) {
//						dtoResult.setTxtMessage("already_booked");
//						return dtoResult;
//					}
					DtoEventBookingValidationResult bookingValidation = this.canBookEvent(newDate,
							entity.getDteEventDate(), entity.getSerEventMasterId());
					if (!bookingValidation.isAllowed()) {
						dtoResult.setTxtMessage("already_booked");
						dtoResult.setResult(bookingValidation.getMessage());
						return dtoResult;
					}
				}
				
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
				applyRunningOrder(dtoEventMasterAdminPortal.getDtoEventRunningOrder(), entity);

				// Set Venue Master
				// ****************

//				// This is for which you only need to specify which venu is selected
//				if (UtilRandomKey.isNotNull(dtoEventMasterAdminPortal.getSerVenueMasterId())) {
//					VenueMaster venueMaster = serviceVenueMaster
//							.getByPK(dtoEventMasterAdminPortal.getSerVenueMasterId());
//					if (UtilRandomKey.isNull(venueMaster)) {
//						dtoResult.setTxtMessage(
//								"Venue Not Found For Id: " + dtoEventMasterAdminPortal.getSerVenueMasterId());
//						return dtoResult;
//					}
//					entity.setVenueMaster(venueMaster);
////					entity.setNumInfoFilledStatus(50);
//				}

				//This is For when you need to save which hall of the venu was selected				
				DtoResult venueRefusal = applyVenue(dtoEventMasterAdminPortal.getDtoEventVenue(), entity);
				if (venueRefusal != null) {
					return venueRefusal;
				}

				// Set Decore Item Selections
				// **************************
				if (UtilRandomKey.isNotNull(dtoEventMasterAdminPortal.getDtoEventDecorSelections())) {
					List<EventDecorCategorySelection> decorSelections = new ArrayList<>();

					for (DtoEventDecorCategorySelection dto : dtoEventMasterAdminPortal.getDtoEventDecorSelections()) {
						EventDecorCategorySelection decorSelection = MapperEventDecorCategorySelection.toEntity(dto);
						decorSelection.setEventMaster(entity);
						if(decorSelection.getNumPrice() != null) {
							numDecorCategoryPrice = numDecorCategoryPrice.add(decorSelection.getNumPrice());
						}
						

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
								if(eventDecorPropertySelection.getNumPrice() != null) {
									numDecorPropertyPrice = numDecorPropertyPrice.add(eventDecorPropertySelection.getNumPrice());
								}
								DecorCategoryPropertyMaster matchedMaster = decorCategoryPropertyMasterLst.stream()
										.filter(pm -> pm.getSerPropertyId().intValue() == property.getSerPropertyId()
												.intValue())
										.findFirst().orElse(null);

//								DecorCategoryPropertyValue matchedValue = decorCategoryPropertyValueLst.stream()
//										.filter(pv -> pv.getSerPropertyValueId().intValue() == property
//												.getSerPropertyValueId().intValue())
//										.findFirst().orElse(null);

								// **********************************************
								Set<EventDecorPropertyValueSelection> selectedValues = new HashSet<>();

								if( property.getSerPropertyValueIds() != null && ! property.getSerPropertyValueIds().isEmpty()) {
									for (Integer valueId : property.getSerPropertyValueIds()) {

										DecorCategoryPropertyValue matchedValue = decorCategoryPropertyValueLst.stream()
												.filter(pv -> pv.getSerPropertyValueId().intValue() == valueId).findFirst()
												.orElse(null);

										EventDecorPropertyValueSelection val = new EventDecorPropertyValueSelection();
										val.setEventDecorPropertySelection(eventDecorPropertySelection);
										val.setPropertyValue(matchedValue);

										selectedValues.add(val);
									}

								}
								
								eventDecorPropertySelection.setSelectedValues(selectedValues);

								// **********************************************
								eventDecorPropertySelection.setProperty(matchedMaster);
//								eventDecorPropertySelection.setSelectedValue(matchedValue);
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


				// entity.setNumInfoFilledStatus(100);

				// Generate event master code
				String code = generateNextEventMasterCode();
				entity.setTxtEventMasterCode(code);

				// A booking above it, so that stage 3 has a parent to move the budget
				// and the payments onto. See giveItABooking.
				giveItABooking(entity);

//				// Setting Event Quoted Price
//				// **************************
//
//				eventBudget = new EventBudget();
//				if (dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus() != null && dtoEventMasterAdminPortal
//						.getDtoEventQuoteAndStatus().getNumPaidAmount().compareTo(BigDecimal.ZERO) == 1) {
//					eventBudget.setTxtStatus("Confirmed");
//					eventBudget
//							.setNumPaidAmount(dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumPaidAmount());
//					if (dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumQuotedPrice() != null
//							&& dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumQuotedPrice()
//									.compareTo(BigDecimal.ZERO) == 1) {
//						eventBudget.setNumQuotedPrice(
//								dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumQuotedPrice());
//					}
//					
//					if(dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus() != null && dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumDiscount() != null) {
//						eventBudget.setNumDiscount(dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumDiscount());
//					}else {
//						eventBudget.setNumDiscount(BigDecimal.ZERO);
//					}
//
//
//				} else if (dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus() != null
//						&& dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumQuotedPrice() != null
//						&& dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumQuotedPrice()
//								.compareTo(BigDecimal.ZERO) == 1) {
//					eventBudget.setTxtStatus("Quoted");
//					eventBudget.setNumQuotedPrice(
//							dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumQuotedPrice());
//					eventBudget.setNumPaidAmount(BigDecimal.ZERO);
//					if(dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus() != null && dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumDiscount() != null) {
//						eventBudget.setNumDiscount(dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus().getNumDiscount());
//					}else {
//						eventBudget.setNumDiscount(BigDecimal.ZERO);
//					}
//
//
//				} else {
//					eventBudget.setTxtStatus("Enquiry");
//					eventBudget.setNumQuotedPrice(BigDecimal.ZERO);
//					eventBudget.setNumPaidAmount(BigDecimal.ZERO);
//					eventBudget.setNumDiscount(BigDecimal.ZERO);
//
//				}
				
				// Setting Event Quoted Price
				// **************************
				eventBudget = new EventBudget();

				DtoEventQuoteAndStatus quoteAndStatus = dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus();

				boolean hasPaidAmount = quoteAndStatus != null && quoteAndStatus.getNumPaidAmount() != null
						&& quoteAndStatus.getNumPaidAmount().compareTo(BigDecimal.ZERO) > 0;
//
//				boolean hasQuotedPrice = quoteAndStatus != null && quoteAndStatus.getNumQuotedPrice() != null
//						&& quoteAndStatus.getNumQuotedPrice().compareTo(BigDecimal.ZERO) > 0;
//
//				BigDecimal discount = (quoteAndStatus != null && quoteAndStatus.getNumDiscount() != null)
//						? quoteAndStatus.getNumDiscount()
//						: BigDecimal.ZERO;

				boolean hasIncomingPaidAmount = quoteAndStatus != null && quoteAndStatus.getNumPaidAmount() != null
						&& quoteAndStatus.getNumPaidAmount().compareTo(BigDecimal.ZERO) > 0;

//				boolean hasExistingPaidAmount = eventBudget.getNumPaidAmount() != null
//						&& eventBudget.getNumPaidAmount().compareTo(BigDecimal.ZERO) > 0;

				boolean hasQuotedPrice = quoteAndStatus != null && quoteAndStatus.getNumQuotedPrice() != null
						&& quoteAndStatus.getNumQuotedPrice().compareTo(BigDecimal.ZERO) > 0;

				boolean hasDecorPrice = quoteAndStatus != null && quoteAndStatus.getNumDecorAmount() != null
						&& quoteAndStatus.getNumDecorAmount().compareTo(BigDecimal.ZERO) > 0;

				boolean hasServicePrice = quoteAndStatus != null && quoteAndStatus.getNumServicesAmount() != null
						&& quoteAndStatus.getNumServicesAmount().compareTo(BigDecimal.ZERO) > 0;

				boolean hasFoodPrice = quoteAndStatus != null && quoteAndStatus.getNumFoodAmount() != null
						&& quoteAndStatus.getNumFoodAmount().compareTo(BigDecimal.ZERO) > 0;

				boolean hasFinalPrice = quoteAndStatus != null && quoteAndStatus.getNumFinalAmount() != null
						&& quoteAndStatus.getNumFinalAmount().compareTo(BigDecimal.ZERO) > 0;

				boolean hasDecorVat = quoteAndStatus != null && quoteAndStatus.getNumDecorExtrasVat() != null
						&& quoteAndStatus.getNumDecorExtrasVat().compareTo(BigDecimal.ZERO) > 0;

				boolean hasDiscount = quoteAndStatus != null && quoteAndStatus.getNumDiscount() != null
						&& quoteAndStatus.getNumDiscount().compareTo(BigDecimal.ZERO) > 0;

				if (hasPaidAmount) {
					eventBudget.setTxtStatus("Confirmed");
//					eventBudget.setNumPaidAmount(quoteAndStatus.getNumPaidAmount());
//					eventBudget.setNumDiscount(discount);
//					if (hasQuotedPrice) {
//						eventBudget.setNumQuotedPrice(quoteAndStatus.getNumQuotedPrice());
//					}
//					applyAmountFields(eventBudget, quoteAndStatus);

				} else if (hasQuotedPrice) {
					eventBudget.setTxtStatus("Quoted");
//					eventBudget.setNumQuotedPrice(quoteAndStatus.getNumQuotedPrice());
//					eventBudget.setNumPaidAmount(BigDecimal.ZERO);
//					eventBudget.setNumDiscount(discount);
//					applyAmountFields(eventBudget, quoteAndStatus);

				} else {
					eventBudget.setTxtStatus("Enquiry");
//					eventBudget.setNumQuotedPrice(BigDecimal.ZERO);
//					eventBudget.setNumPaidAmount(BigDecimal.ZERO);
//					eventBudget.setNumDiscount(BigDecimal.ZERO);
//					eventBudget.setNumFoodAmount(BigDecimal.ZERO);
//					eventBudget.setNumServicesAmount(BigDecimal.ZERO);
//					eventBudget.setNumDecorAmount(BigDecimal.ZERO);
//					eventBudget.setNumDecorExtrasVat(BigDecimal.ZERO);
//					eventBudget.setNumFinalAmount(BigDecimal.ZERO);
				}

				if (hasIncomingPaidAmount) {
					setPaidAmount(eventBudget, quoteAndStatus.getNumPaidAmount());
				} else {
					setPaidAmount(eventBudget, BigDecimal.ZERO);
				}

				if (hasQuotedPrice) {
					eventBudget.setNumQuotedPrice(quoteAndStatus.getNumQuotedPrice());
				} else {
					eventBudget.setNumQuotedPrice(BigDecimal.ZERO);
				}

				if (hasFinalPrice) {
					eventBudget.setNumFinalAmount(quoteAndStatus.getNumFinalAmount());
				} else {
					eventBudget.setNumFinalAmount(BigDecimal.ZERO);
				}

				if (hasDecorPrice) {
					eventBudget.setNumDecorAmount(quoteAndStatus.getNumDecorAmount());
				} else {
					eventBudget.setNumDecorAmount(BigDecimal.ZERO);
				}

				if (hasServicePrice) {
					eventBudget.setNumServicesAmount(quoteAndStatus.getNumServicesAmount());
				} else {
					eventBudget.setNumServicesAmount(BigDecimal.ZERO);
				}

				if (hasFoodPrice) {
					eventBudget.setNumFoodAmount(quoteAndStatus.getNumFoodAmount());
				} else {
					eventBudget.setNumFoodAmount(BigDecimal.ZERO);
				}

				if (hasDecorVat) {
					eventBudget.setNumDecorExtrasVat(quoteAndStatus.getNumDecorExtrasVat());
				} else {
					eventBudget.setNumDecorExtrasVat(BigDecimal.ZERO);
				}

				if (hasDiscount) {
					eventBudget.setNumDiscount(quoteAndStatus.getNumDiscount());
				} else {
					eventBudget.setNumDiscount(BigDecimal.ZERO);
				}

			}
			
			
			
			//*********************************************************************************************
			//************************ Food Menu Categories and Sub Categories ****************************
			//*********************************************************************************************
			MenuResult menuResult = applyMenuSelections(dtoEventMasterAdminPortal.getMenuCategoriesSelection(), entity, menuItems);
			entity = menuResult.entity();
			numFoodCategoryPrice = numFoodCategoryPrice.add(menuResult.categories());
			numFoodSubcategoryPrice = numFoodSubcategoryPrice.add(menuResult.subCategories());

			//*********************************************************************************************
			//*********************************************************************************************
			//*********************************************************************************************

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
					selection.setBlnIsServices(false);
					selection.setEventMaster(entity);
					if(selection.getNumPrice() != null) {
						numDecorCategoryPrice = numDecorCategoryPrice.add(selection.getNumPrice());
					}
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
			
			
			// ****** Setting Event Services ******

			if (entity.getServicesSelections() != null) {
			    entity.getServicesSelections().clear();
			} else {
			    entity.setServicesSelections(new ArrayList<>());
			}

			if (UtilRandomKey.isNotNull(dtoEventMasterAdminPortal.getServicesSelections())
			        && !dtoEventMasterAdminPortal.getServicesSelections().isEmpty()) {

			    List<EventDecorExtrasSelection> newServiceSelections = new ArrayList<>();

			    for (DtoEventDecorExtrasSelection dto : dtoEventMasterAdminPortal.getServicesSelections()) {

			        EventDecorExtrasSelection selection = new EventDecorExtrasSelection();

			        selection.setTxtDynamicProperty1(dto.getTxtDynamicProperty1());
			        selection.setTxtDynamicProperty2(dto.getTxtDynamicProperty2());
			        selection.setNumPrice(dto.getNumPrice());
			        selection.setEventMaster(entity);
			        if(dto.getNumPrice() != null) {
			        	numServicesPrice = numServicesPrice.add(dto.getNumPrice());
			        }
			        // 🔥 THIS IS THE DIFFERENCE
			        selection.setBlnIsServices(true);

			        if (dto.getSerExtrasId() != null) {
			            selection.setDecorExtrasMaster(
			                serviceDecorExtrasMaster.getByIdAndNotDeleted(dto.getSerExtrasId()));
			        }

			        if (dto.getSerExtraOptionId() != null) {
			            selection.setDecorExtrasOption(
			                serviceDecorExtrasOption.getByIdAndNotDeleted(dto.getSerExtraOptionId()));
			        }

			        newServiceSelections.add(selection);
			    }

			    entity.getServicesSelections().addAll(newServiceSelections);
			}
			
			
			

			// *********************************************************************************************
			// *********************************************************************************************
			// *********************************************************************************************

			entity.setNumInfoFilledStatus(getEventCompletionPercentage(entity));
//			entity = repositoryEventMaster.save(entity);
//			if (eventBudget != null) {
//				eventBudget.setEventMaster(entity);
//				serviceEventBudget.save(eventBudget);
//			}

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
					dtoResult.setTxtMessage("Success");
					if (entity.getBlnIsClientEmailSend() != null
							&& entity.getBlnIsClientEmailSend().equals(Boolean.FALSE)) {
						serviceEmailSender.sendEventRegistrationEmail(userMaster.getTxtEmail(), userMaster.getTxtName(),
								entity.getTxtEventMasterCode(), entity.getEventType().getTxtEventTypeName(),
								entity.getDteEventDate());
						entity.setBlnIsClientEmailSend(true);
						dtoResult.setTxtMessage(entity.getEventType().getTxtEventTypeName()
								+ " Event Has Been Registered. A Confirmation Email Has Been Sent To Your Registered Email.");

					}

					if (entity.getBlnIsAllAdminEmailSend() != null
							&& entity.getBlnIsAllAdminEmailSend().equals(false)) {
						// ***** Send Email To All Admin Users ********
						serviceEmailSender.sendEventRegistrationEmailToAdminUsers(userMaster.getTxtName(),
								entity.getTxtEventMasterCode(), entity.getEventType().getTxtEventTypeName(),
								entity.getDteEventDate());
						entity.setBlnIsAllAdminEmailSend(true);

						dtoResult.setTxtMessage(entity.getEventType().getTxtEventTypeName()
								+ " Event Has Been Registered. A Confirmation Email Has Been Sent To Your Registered Email.");

					}
				} else {
					dtoResult.setTxtMessage("Success");

				}
			} else {
				dtoResult.setTxtMessage("Success");
			}
			
			entity = repositoryEventMaster.save(entity);
			if (eventBudget != null) {
//				BigDecimal decorCategory = numDecorCategoryPrice != null ? numDecorCategoryPrice : BigDecimal.ZERO;
//				BigDecimal decorProperty = numDecorPropertyPrice != null ? numDecorPropertyPrice : BigDecimal.ZERO;
//
//				BigDecimal foodCategory = numFoodCategoryPrice != null ? numFoodCategoryPrice : BigDecimal.ZERO;
//				BigDecimal foodSubcategory = numFoodSubcategoryPrice != null ? numFoodSubcategoryPrice
//						: BigDecimal.ZERO;
//
//				BigDecimal services = numServicesPrice != null ? numServicesPrice : BigDecimal.ZERO;
//
//				eventBudget.setNumDecorAmount(decorCategory.add(decorProperty));
//				eventBudget.setNumFoodAmount(foodCategory.add(foodSubcategory));
//				eventBudget.setNumServicesAmount(services);
				
				eventBudget.setEventMaster(entity);
				serviceEventBudget.save(eventBudget);
			}

			/*
			  The same two the journey does, and for the same reasons.

			  They were missing here, so the office's suppliers panel showed
			  nothing and saved nothing: the rows were posted, Jackson dropped
			  them as an unknown property on a DTO that lacked the field, and
			  nothing here looked for them. Most suppliers arrive by telephone
			  a fortnight before the day — the office is the side that most
			  needs to record them.
			*/
			recordTermsAcceptance(dtoEventMasterAdminPortal.getBlnTermsAccepted(),
					entity.getSerEventMasterId());
			replaceExternalSuppliers(dtoEventMasterAdminPortal.getExternalSuppliers(), entity);
			priceTheBooking(entity.getSerEventMasterId(),
					dtoEventMasterAdminPortal.getDtoEventQuoteAndStatus());

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

				
				// **********************************************************************************************
				// ************************ Food Selections Category and SubCategory Pricing ********************
				// **********************************************************************************************
				List<DtoCustomerMenuCategory> catDtos = new ArrayList<>();

				for (EventMenuCategorySelection cat : event.getMenuCategorySelections()) {

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
					dtoEventQuoteAndStatus.setNumDiscount(eventBudget.getNumDiscount());
					dto.setDtoEventQuoteAndStatus(dtoEventQuoteAndStatus);
				}

				/*
				  The suppliers the customer declared, and whether they agreed
				  the terms.

				  Both read by the journey's own getEventById and neither read
				  here, so the office's suppliers panel opened empty over a
				  booking that had three of them stored.
				*/
				dto.setExternalSuppliers(readExternalSuppliers(dto.getSerEventMasterId()));
				/*
				  Derived from the timestamp, which is the actual fact. The
				  boolean on the DTO is an input — "the customer has just
				  ticked the box" — and the moment they did is what is stored.
				*/
				dto.setBlnTermsAccepted(event.getDteTermsAcceptedOn() != null);

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
				dtoEvent.setFoodSelections(foodSelections);
			} catch (Exception ex) {
				LOGGER.debug("Failed to fetch menu food selections for event {}: {}", event.getSerEventMasterId(),
						ex.getMessage(), ex);
				dtoEvent.setFoodSelections(new ArrayList<>());
			}

			// **********************************************************************************************
			// ************************ Food Selections Category and SubCategory Pricing ********************
			// **********************************************************************************************
			try {
				List<DtoCustomerMenuCategory> catDtos = new ArrayList<>();

				for (EventMenuCategorySelection cat : event.getMenuCategorySelections()) {

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
				dtoEvent.setMenuCategoriesSelection(catDtos);

			} catch (Exception ex) {
				LOGGER.debug("Failed to fetch menu food category and subcategory wise for event {}: {}",
						event.getSerEventMasterId(), ex.getMessage(), ex);
				dtoEvent.setMenuCategoriesSelection(new ArrayList<>());
			}

			// **********************************************************************************************
			// **********************************************************************************************
			// **********************************************************************************************

			// 4) Decor extras selections
			try {
				List<EventDecorExtrasSelection> extras = serviceEventDecorExtrasSelection
						.getExtrasSelectionsByEventMasterId(dtoEvent.getSerEventMasterId());
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
			
			
			// 4) Decor services selections
						try {
							List<EventDecorExtrasSelection> services = serviceEventDecorExtrasSelection
									.getServicesSelectionsByEventMasterId(dtoEvent.getSerEventMasterId());
							List<DtoEventDecorExtrasSelection> dtoServices = new ArrayList<>();
							if (UtilRandomKey.isNotNull(services)) {
								for (EventDecorExtrasSelection entity : services) {
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
									dtoServices.add(e);
								}
							}
							dtoEvent.setServicesSelections(dtoServices);
						} catch (Exception ex) {
							LOGGER.debug("Failed to fetch services selections for event {}: {}", event.getSerEventMasterId(),
									ex.getMessage(), ex);
							dtoEvent.setExtrasSelections(new ArrayList<>());
						}
			
			// **********************************************************************************************
			// **********************************************************************************************
			// **********************************************************************************************


			// 5) Budget / quoted price and status
			try {
				EventBudget eventBudget = serviceEventBudget.getEventBudgetByEventId(event.getSerEventMasterId());
				if (eventBudget != null) {
					DtoEventQuoteAndStatus quote = new DtoEventQuoteAndStatus();
					quote.setNumQuotedPrice(eventBudget.getNumQuotedPrice());
					quote.setNumPaidAmount(eventBudget.getNumPaidAmount());
					quote.setTxtStatus(eventBudget.getTxtStatus());
					quote.setNumDiscount(eventBudget.getNumDiscount());
					quote.setNumDecorAmount(eventBudget.getNumDecorAmount());
					quote.setNumFoodAmount(eventBudget.getNumFoodAmount());
					quote.setNumServicesAmount(eventBudget.getNumServicesAmount());
					quote.setNumDecorExtrasVat(eventBudget.getNumDecorExtrasVat());
					quote.setNumFinalAmount(eventBudget.getNumFinalAmount());
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
	
	@Transactional
	public DtoResult validateEventDateAvailability(Date eventDate) {
		DtoResult dtoResult = new DtoResult();
		try {
			 boolean alreadyBooked =
			            repositoryEventMaster.existsByDteEventDateAndBlnIsDeletedFalse(eventDate);

			    if (alreadyBooked) {
			        dtoResult.setTxtMessage("An event is already registered at this date.");
			        dtoResult.setResult(alreadyBooked);
			    }else {
			        dtoResult.setTxtMessage("No event is registered at this date.");
			        dtoResult.setResult(alreadyBooked);
			    }
			    return dtoResult;
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			dtoResult.setTxtMessage(e.getMessage());
			return dtoResult;
		}
	   
	}

//	@Override
//	public DtoResult getAlreadyBookedDates() {
//		DtoResult dtoResult = new DtoResult();
//		try {
//			List<Date> dates = repositoryEventMaster.getAlreadyBookedDates();
//			List<String> strDates =new ArrayList<>();
//			for(Date date : dates) {
//				String strdate = UtilDateAndTime.mmddyyyyDateToString(date);
//				strDates.add(strdate);
//			}
//			dtoResult.setTxtMessage("Success");
//			dtoResult.setResult(strDates);
//			return dtoResult;
//		} catch (Exception e) {
//			LOGGER.debug(e.getMessage(), e);
//			dtoResult.setTxtMessage(e.getMessage());
//			return dtoResult;
//		}
//	}
	
	
	/**
	 * The dates the calendar greys out.
	 *
	 * <h3>Why this is not its own copy of the rule any more</h3>
	 *
	 * It was. This method held a third hand-written statement of "two on an
	 * ordinary day, three on a quiet Sunday, none on the Monday after a full
	 * one" — beside {@link EventDayCapacity}, which {@code canBookEvent} uses to
	 * decide whether a save is allowed, and beside {@code getDaysOverCapacity},
	 * which reports the days that breach it. This file has a history of one
	 * routine existing in several near-identical copies that drift apart, and
	 * the interesting thing about this copy is which way it drifted: it greyed
	 * out days that {@code canBookEvent} would have accepted. The calendar and
	 * the thing enforcing the calendar disagreed.
	 *
	 * <h3>The event being edited does not block itself</h3>
	 *
	 * A customer reopening their booking was shown their own date as taken,
	 * because their event counted towards the capacity that closed the day. On a
	 * Saturday already holding two — theirs and somebody else's — the day they
	 * had booked, paid a deposit against and were looking at on the confirmation
	 * email came back greyed out, reading as "no longer available". Pressing it
	 * did nothing, because an unavailable day is not a control.
	 *
	 * <p>
	 * {@code canBookEvent} has always excluded the event being saved — that is
	 * what lets the office open a day that is over capacity and save it without
	 * being refused. The calendar now excludes it too, so what is greyed out is
	 * exactly what a save would refuse.
	 */
	@Override
	public DtoResult getAlreadyBookedDates(Integer excludeEventId) {

		DtoResult dtoResult = new DtoResult();

		try {
			/*
			 * One query for every day that holds events, then the rule applied in
			 * memory — the same shape getDaysOverCapacity uses, and for the same
			 * reason: the rule needs the day either side of a Sunday or a Monday,
			 * so answering it per-day in SQL would be three round trips per date.
			 */
			Map<java.time.LocalDate, Integer> counts = new HashMap<>();
			for (Object[] row : repositoryEventMaster.getEventDateCounts(excludeEventId)) {
				Date date = (Date) row[0];
				if (date == null) {
					continue;
				}
				counts.merge(toLocalDate(UtilDateAndTime.getStartOfDay(date)), ((Long) row[1]).intValue(),
						Integer::sum);
			}

			List<String> blockedDates = new ArrayList<>();

			/*
			 * A Monday can be closed by its Sunday while holding nothing itself,
			 * so the days to test are the ones with events *and* the day after
			 * each of them. Testing only the keys of the map would leave an empty
			 * Monday selectable that canBookEvent then refuses.
			 */
			java.util.Set<java.time.LocalDate> daysToTest = new java.util.HashSet<>(counts.keySet());
			for (java.time.LocalDate day : counts.keySet()) {
				daysToTest.add(day.plusDays(1));
			}

			for (java.time.LocalDate day : daysToTest) {
				if (counts.getOrDefault(day, 0) >= EventDayCapacity.of(day, counts)) {
					blockedDates.add(UtilDateAndTime.mmddyyyyDateToString(
							Date.from(day.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())));
				}
			}

			/*
			 * Today, always. Nothing can be sourced, staffed or delivered in the
			 * hours left, and canBookEvent refuses it outright before capacity is
			 * considered — so the calendar must not offer it.
			 */
			blockedDates.add(UtilDateAndTime.mmddyyyyDateToString(UtilDateAndTime.getStartOfDay(new Date())));

			dtoResult.setTxtMessage("Success");
			dtoResult.setResult(blockedDates);
			return dtoResult;

		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			dtoResult.setTxtMessage("Failure");
			return dtoResult;
		}
	}

	@Override
	@Transactional
	public DtoResult getEventSummariesByCustomerId(Integer serCustId) {
		DtoResult dtoResult = new DtoResult();

		if (serCustId == null) {
			dtoResult.setTxtMessage("Customer ID Is Required");
			return dtoResult;
		}

		try {
			dtoResult.setResulList(new ArrayList<>(repositoryEventMaster.findEventSummariesByCustomerId(serCustId)));
			dtoResult.setTxtMessage("Success");
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			dtoResult.setTxtMessage("Could not load your bookings");
		}

		return dtoResult;
	}

	@Override
	@Transactional
	public DtoResult getCalendarEntries() {
		DtoResult dtoResult = new DtoResult();

		try {
			dtoResult.setResulList(new ArrayList<>(repositoryEventMaster.getCalendarEntries()));
			dtoResult.setTxtMessage("Success");
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			dtoResult.setTxtMessage("Could not load the calendar");
		}

		return dtoResult;
	}

	@Override
	// Plain @Transactional: this class imports the Jakarta annotation, which has
	// no readOnly attribute. Not worth switching the file's import for one hint.
	@Transactional
	public DtoResult getDaysOverCapacity() {
		DtoResult dtoResult = new DtoResult();

		try {
			/*
			 * One query for every day that has events, then the rule applied in
			 * memory. The rule needs the days either side of each date — a Sunday
			 * looks at its Monday and a Monday at its Sunday — so answering it
			 * per-day in SQL would be three queries per date for no gain.
			 */
			java.util.Map<java.time.LocalDate, Integer> counts = new java.util.HashMap<>();
			for (Object[] row : repositoryEventMaster.getEventDateCounts()) {
				Date date = (Date) row[0];
				if (date == null) {
					continue;
				}
				java.time.LocalDate day = UtilDateAndTime.getStartOfDay(date).toInstant()
						.atZone(java.time.ZoneId.systemDefault()).toLocalDate();
				counts.merge(day, ((Long) row[1]).intValue(), Integer::sum);
			}

			java.time.LocalDate today = java.time.LocalDate.now();
			List<java.util.Map<String, Object>> over = new ArrayList<>();

			for (java.time.LocalDate day : new java.util.TreeSet<>(counts.keySet())) {
				// History cannot be staffed differently; only what is still to come.
				if (day.isBefore(today) || !EventDayCapacity.isOverCapacity(day, counts)) {
					continue;
				}

				java.util.Map<String, Object> entry = new java.util.LinkedHashMap<>();
				entry.put("dteEventDate", day.toString());
				entry.put("txtDayOfWeek", day.getDayOfWeek().getDisplayName(
						java.time.format.TextStyle.FULL, java.util.Locale.UK));
				entry.put("numEvents", counts.get(day));
				entry.put("numCapacity", EventDayCapacity.of(day, counts));
				over.add(entry);
			}

			dtoResult.setTxtMessage("Success");
			dtoResult.setResult(over);
			return dtoResult;

		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			dtoResult.setTxtMessage("Failure");
			return dtoResult;
		}
	}

	/**
	 * Whether an event may sit on this date.
	 *
	 * <p>
	 * The capacity rule itself lives in {@link EventDayCapacity} and is applied
	 * from there. This method's job is the part the rule cannot know about: what
	 * the days involved would hold <em>after</em> this save. That means counting
	 * the day, counting the day the rule couples it to, and accounting for the
	 * event being edited — which may be arriving on this date, leaving another,
	 * or staying exactly where it is.
	 *
	 * <p>
	 * It used to carry its own copy of the rule, in {@code Calendar} arithmetic,
	 * and the copy had drifted. {@code countEventsOnDate} already excludes the
	 * event being edited, and the Sunday and Monday branches then subtracted it a
	 * second time when it was moving off the adjacent day. The effect was real
	 * rather than theoretical: moving an event from a Monday to the Sunday before
	 * it under-counted that Monday by one, so the Sunday was allowed a third
	 * event while a Monday booking still stood — the exact pairing the rule
	 * exists to prevent, since the team need the Monday to break down.
	 */
	public DtoEventBookingValidationResult canBookEvent(Date newDate, Date oldDate, Integer eventId) {

		if (newDate == null) {
			return new DtoEventBookingValidationResult(false, "Invalid date");
		}

		Date newStart = UtilDateAndTime.getStartOfDay(newDate);

		// No same-day booking. Nothing can be sourced, staffed or delivered in
		// the time left, so this is refused before capacity is even considered.
		if (newStart.equals(UtilDateAndTime.getStartOfDay(new Date()))) {
			return new DtoEventBookingValidationResult(false, "Same-day booking is not allowed");
		}

		/*
		 * A ceiling on how far ahead a date can be.
		 *
		 * Not the business's answer to "how far ahead do we take bookings" — that
		 * is a real question and still open (A5b). This is the narrower one that
		 * needs nobody's decision: a date this far out is a typing mistake, not a
		 * booking. The year stepper goes forward indefinitely, so 2027 becomes
		 * 2207 with one stray keypress, and the result is a row that never
		 * surfaces in any diary, never gets chased, and is found years later by
		 * somebody wondering why the earliest booking is in the twenty-third
		 * century.
		 *
		 * Ten years is deliberately far beyond anything plausible. It rejects
		 * only the obviously impossible, and leaves the business free to set a
		 * real limit — two years, three — without this having pre-empted it.
		 */
		/*
		  The limit is a setting now, not a constant.
		  
		  Ten years was a stopgap chosen to reject only the obviously
		  impossible, on the explicit basis that how far ahead the business
		  actually takes bookings was its decision and this should not
		  pre-empt it. `booking.horizon.months` is where that decision lives,
		  and the office can change it without asking anybody.
		*/
		int horizonMonths = serviceAppSettings.getBookingHorizonMonths();
		java.time.LocalDate furthestSensible = java.time.LocalDate.now(java.time.ZoneId.systemDefault())
				.plusMonths(horizonMonths);
		if (toLocalDate(newStart).isAfter(furthestSensible)) {
			return new DtoEventBookingValidationResult(false,
					horizonMonths % 12 == 0
							? "That date is more than " + (horizonMonths / 12)
									+ " years away — please check the year."
							: "That date is more than " + horizonMonths
									+ " months away — please check the year.");
		}

		/*
		 * Nobody else may be counting this date while we are.
		 *
		 * How many events a day can hold is a counting rule, so the check is
		 * "count what is there, then insert" — and between those two steps a
		 * second request can do exactly the same thing. Both count one, both see
		 * room, both insert, and the day ends up over capacity. Neither customer
		 * did anything wrong and nothing in the log says what happened; the team
		 * finds out when they try to staff it.
		 *
		 * The lock is held to the end of the surrounding transaction and only
		 * ever contends with another booking for the same day, so two customers
		 * booking different dates never meet. Every caller of this method reaches
		 * it, which is why the lock is here rather than at the six call sites —
		 * one of those would eventually be added without it.
		 */
		repositoryEventDateLock.lockForBooking(newStart);

		java.time.LocalDate day = toLocalDate(newStart);
		java.time.LocalDate oldDay = oldDate == null ? null : toLocalDate(UtilDateAndTime.getStartOfDay(oldDate));

		/*
		 * Is this event arriving on this date, or was it already here?
		 *
		 * Every count below excludes the event being edited, so one that is
		 * arriving has to be added back. One that is staying put must not be —
		 * that is what lets the team open and save a booking on a day that is
		 * already over capacity without being refused. Those days exist, they are
		 * commitments to real customers, and correcting one is a conversation
		 * rather than a data fix.
		 */
		boolean arriving = eventId == null || oldDay == null || !oldDay.equals(day);

		java.util.Map<java.time.LocalDate, Integer> counts = new java.util.HashMap<>();
		counts.put(day, countEventsOn(day, eventId) + (arriving ? 1 : 0));

		/*
		 * The rule couples each Sunday to the Monday after it, in both
		 * directions, so that neighbour has to be counted too. Its count excludes
		 * the event being edited, which is right either way: if the event is
		 * moving off that Monday it will not be there afterwards, and if it is
		 * not, it is on `day` and counted there instead.
		 */
		java.time.LocalDate neighbour = null;
		if (day.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
			neighbour = day.plusDays(1);
		} else if (day.getDayOfWeek() == java.time.DayOfWeek.MONDAY) {
			neighbour = day.minusDays(1);
		}
		if (neighbour != null) {
			counts.put(neighbour, countEventsOn(neighbour, eventId));
		}

		int capacity = EventDayCapacity.of(day, counts);
		if (counts.get(day) > capacity) {
			return new DtoEventBookingValidationResult(false, fullMessage(day, capacity));
		}

		return new DtoEventBookingValidationResult(true, "Allowed");
	}

	/**
	 * Gives a newly created event the booking it belongs to.
	 *
	 * <h3>What this is</h3>
	 *
	 * Stage 2 of §15.3. V11 gave every event that existed at the time a parent;
	 * this keeps that true for every event created since. Without it the
	 * proportion of events with no booking grows with each sale, and stage 3 —
	 * which moves the budget and the payments onto the booking — would have to
	 * begin by inventing parents for whatever accumulated in between.
	 *
	 * <h3>One booking per event, still</h3>
	 *
	 * That is not the shape this is heading for, and it is not an oversight. The
	 * thing worth having now is the invariant: <em>every event has a parent</em>,
	 * with no exceptions to remember. Several events sharing one booking — the
	 * mehndi, the nikkah and the walima of one wedding — is stage 4, and it
	 * needs the journey to ask "is this another day of a booking you already
	 * have?", which nothing does yet. Guessing at it here, by matching on a
	 * customer and a nearby date, would silently merge two unrelated bookings
	 * for the same family and there would be no way to tell afterwards which
	 * money belonged to which.
	 *
	 * <h3>Why the code is copied</h3>
	 *
	 * Same convention V11 used: the booking carries the event's reference, so a
	 * person on the telephone quoting a code finds the booking without a join,
	 * and a booking created here is indistinguishable from a backfilled one.
	 *
	 * <h3>Why there are two ways of attaching it</h3>
	 *
	 * Because the four create paths do not agree on when the event row appears.
	 * {@code saveAndUpdate} builds the whole event and inserts it once, at the
	 * end; the other three insert a bare row early — right after the capacity
	 * check, with its associations deliberately nulled — and fill it in
	 * afterwards. So at this point the event sometimes has an id and sometimes
	 * does not, and only one of those two cases has an insert left to carry the
	 * booking id.
	 *
	 * <p>
	 * Both are the same one-time attachment, and neither is an ordinary save
	 * writing the column: {@code ser_booking_id} stays {@code updatable = false},
	 * so a DTO-shaped save can never blank it, and
	 * {@code attachToBooking} only ever fills an empty one.
	 */
	private void giveItABooking(EventMaster entity) {
		if (entity.getSerBookingId() != null) {
			return;
		}

		Booking booking = new Booking();
		booking.setTxtBookingCode(entity.getTxtEventMasterCode());
		booking.setCustomerMaster(entity.getCustomerMaster());
		booking.setCreatedBy(ServiceCurrentUser.getCurrentUserId());

		/*
		 * save, deliberately, and not saveAndFlush.
		 *
		 * The id is needed here and now. save is enough for it: a booking's id is
		 * IDENTITY-generated, so Hibernate must run this one insert immediately in
		 * order to learn it.
		 *
		 * saveAndFlush would also return the id, and is wrong. It flushes the
		 * whole persistence context, which at this point holds an event that has
		 * not been saved yet with decor selections and a running order pointing at
		 * it. Hibernate refuses that — TransientObjectException — and these four
		 * methods catch everything and report "Failure", so the symptom is every
		 * new booking being rejected with the reason recorded nowhere but a debug
		 * log.
		 */
		Long bookingId = repositoryBooking.save(booking).getSerBookingId();

		// The event has not been inserted yet: its own insert carries the id.
		entity.setSerBookingId(bookingId);

		// It has: say so explicitly, because no insert is coming.
		if (entity.getSerEventMasterId() != null) {
			repositoryEventMaster.attachToBooking(entity.getSerEventMasterId(), bookingId);
		}
	}

	/**
	 * The code the frontends switch on when a booking has moved under them.
	 *
	 * <p>
	 * Same convention as {@code already_booked}: a stable code in the message
	 * field, the words for a person in the result.
	 */
	private static final String CHANGED_ELSEWHERE = "changed_elsewhere";

	private static final String CHANGED_ELSEWHERE_MESSAGE = "Somebody else saved changes to this booking while you "
			+ "had it open. Refresh to see their version, then make your change again.";

	/** Same convention again: a code for the frontends, words for a person. */
	private static final String BAD_EVENT_DATE = "bad_event_date";

	private static final String BAD_EVENT_DATE_MESSAGE = "The event date could not be read. Send it as dd-MM-yyyy — "
			+ "for example 14-06-2026.";

	/**
	 * Refuses the save outright when the request carries a date that is not a
	 * date.
	 *
	 * <h4>Why this is a refusal and not a shrug</h4>
	 *
	 * The parser returns nothing for a string it cannot read, and every save path
	 * used to pass that nothing straight to {@code setDteEventDate}. So a request
	 * carrying a malformed date saved the event <em>with no date at all</em>, and
	 * answered 200.
	 *
	 * <p>
	 * That is worse than it sounds, because of what a dateless event is allowed to
	 * do next. The capacity check on an edit reads
	 * {@code newDate != null && entity.getDteEventDate() != null} — an event whose
	 * stored date is null skips the check entirely, so the following save can put
	 * it on a day that is already full, and the day after that the kitchen is
	 * cooking for two weddings it agreed to cater for one.
	 *
	 * <p>
	 * An absent date is still allowed through: the journey creates the event
	 * before the customer has chosen a day, and that is a real state. What is
	 * refused is a date that was <em>offered</em> and is not one.
	 *
	 * @param given the date string exactly as the request sent it
	 * @return the result to hand back to the caller, or {@code null} to carry on
	 */
	private DtoResult refuseUnreadableDate(String given) {
		if (given == null || given.trim().isEmpty()) {
			return null;
		}
		if (UtilDateAndTime.parseDateFromClient(given) != null) {
			return null;
		}

		LOGGER.warn("Refusing to save an event: the date '{}' is not readable in any accepted format", given);

		DtoResult refusal = new DtoResult();
		refusal.setTxtMessage(BAD_EVENT_DATE);
		refusal.setResult(BAD_EVENT_DATE_MESSAGE);
		return refusal;
	}

	/**
	 * Writes the event date, and keeps the stored one when the request did not
	 * carry a date at all.
	 *
	 * <h4>What this fixes</h4>
	 *
	 * Every save path wrote {@code parseDateFromClient(dto.getDteEventDate())}
	 * straight onto the entity. The parser answers null for an absent date as well
	 * as for an unreadable one, so a request that simply left the field out
	 * cleared the date of a booking that had one — and answered 200. Sending
	 * {@code {serEventMasterId, serCustId, serEventTypeId}} and nothing else was
	 * enough to wipe a wedding's date.
	 *
	 * <p>
	 * The consequence is the one {@link #refuseUnreadableDate} exists for: an
	 * event whose stored date is null skips the capacity check on its next save —
	 * that check reads {@code newDate != null && entity.getDteEventDate() != null}
	 * — so the booking can then be put on a day that is already full.
	 *
	 * <h4>Why this is silence and not a refusal</h4>
	 *
	 * Its sibling above refuses, and the symmetrical thing would be to refuse
	 * here too. That was considered and rejected: the admin portal builds its
	 * payload from the form's values, so a date that is absent because its field
	 * was never mounted looks exactly like a date that is absent because nobody
	 * meant to send one. Refusing would block a legitimate save in a case that
	 * cannot be seen from here, and blocking a real save is worse than ignoring
	 * an instruction nobody gives.
	 *
	 * <p>
	 * What is given up is the ability to clear a date by omitting it. Neither
	 * frontend does that — the journey cannot reach the state, and the admin form
	 * is initialised from the booking it is editing — and an explicit way to clear
	 * a date can be added the day somebody needs one. A malformed date is still a
	 * refusal, because that is the case where the caller did mean something.
	 *
	 * <p>
	 * Only the date is protected this way. A guest count or a name that arrives
	 * empty is still allowed to overwrite what is stored, because emptying one of
	 * those in a form is something a person does on purpose, and quietly keeping
	 * the old value would be its own silent failure.
	 *
	 * @param entity the booking being written, already loaded
	 * @param given  the date string exactly as the request sent it
	 */
	private void setEventDate(EventMaster entity, String given) {
		Date parsed = UtilDateAndTime.parseDateFromClient(given);

		if (parsed == null && entity.getDteEventDate() != null) {
			LOGGER.debug("Event {} was saved without a date; keeping the one it has ({})",
					entity.getSerEventMasterId(), entity.getDteEventDate());
			return;
		}

		entity.setDteEventDate(parsed);
	}

	/**
	 * Whether the client is holding an out-of-date copy of this booking.
	 *
	 * <p>
	 * The failure being prevented: an administrator opens a booking to change the
	 * guest count while the customer, in the journey, is choosing their menu. Both
	 * save. One set of changes is simply gone — nothing in either interface says
	 * so, nothing in the log says so, and it is found weeks later when the kitchen
	 * cooks for the wrong number of people.
	 *
	 * <p>
	 * {@code @Version} on the entity catches two transactions overlapping, which
	 * is the rarer half of this — the writes have to land within milliseconds of
	 * one another. The case above is minutes apart, and the only thing that can
	 * detect it is the version the client fetched.
	 *
	 * <p>
	 * A missing version is allowed through deliberately. It means a caller that
	 * predates this field, and refusing those saves would break working screens
	 * to protect against a rarer fault than the one it caused.
	 */
	private boolean hasChangedElsewhere(EventMaster entity, Long heldVersion) {
		if (heldVersion == null || entity.getNumVersion() == null) {
			return false;
		}
		if (heldVersion.equals(entity.getNumVersion())) {
			return false;
		}

		/*
		 * The booking has moved since this copy was fetched — but by whom?
		 *
		 * The customer journey saves on every step, so by the third screen the
		 * copy in the browser is several revisions behind through nothing but its
		 * own progress. Refusing on staleness alone would stop a customer at the
		 * second screen and tell them somebody else had edited their booking,
		 * which would be their own previous click.
		 *
		 * The question worth asking is not "is this copy old" but "did somebody
		 * else save in the meantime". updatedBy still holds the previous saver
		 * here, because this runs before any of the caller's values are applied.
		 */
		Integer lastSavedBy = entity.getUpdatedBy();
		Integer savingNow = ServiceCurrentUser.getCurrentUserId();

		if (lastSavedBy == null || savingNow == null) {
			// Nothing to compare people by. Allowed rather than refused: an
			// unattributed save is not evidence of a second editor, and refusing
			// here would break the paths that have no user in context at all.
			return false;
		}

		return !lastSavedBy.equals(savingNow);
	}

	/** How many events are on this day, never counting the one being edited. */

	/**
	 * The menu, and what it came to.
	 *
	 * <p>
	 * It carries the event back because the save flushes mid-way and the
	 * instance the caller holds is no longer the one being written to. Both
	 * totals are write-only today, as the décor ones are, and are carried for
	 * the same reason: taking them out is a separate decision from writing this
	 * once instead of twice.
	 */
	private record MenuResult(EventMaster entity, BigDecimal categories, BigDecimal subCategories) {
	}

	/**
	 * The food chosen for the booking: categories, the courses under them, and
	 * the dishes under those.
	 *
	 * <h3>What this replaced</h3>
	 *
	 * A hundred and forty lines that were byte-for-byte identical in the
	 * journey's save and the office's, down to the emoji in the comments. Of
	 * everything duplicated across the two paths, this was the one pair with no
	 * differences at all between them.
	 *
	 * <h3>Why the old rows are taken apart before being cleared</h3>
	 *
	 * The menu is three levels deep and every level points back at its parent.
	 * Clearing the top collection alone leaves the levels below it still
	 * holding references, so {@code orphanRemoval} does not fire for them and
	 * the rows stay in the database attached to nothing. Breaking each link on
	 * the way down is what makes the delete reach the bottom.
	 *
	 * <h3>Why it flushes in the middle</h3>
	 *
	 * The deletions have to reach the database before the replacements are
	 * added, or Hibernate holds both in the same session and the insert
	 * collides with the row it is meant to be replacing. That flush returns a
	 * different instance, which is why the event is handed back rather than
	 * only mutated.
	 */
	private MenuResult applyMenuSelections(List<DtoCustomerMenuCategory> given, EventMaster entity,
			List<MenuItem> menuItems) {

		BigDecimal categoryTotal = BigDecimal.ZERO;
		BigDecimal subCategoryTotal = BigDecimal.ZERO;

		/*
		 * Nothing chosen is not the same as nothing said. A booking that
		 * already exists and arrives with an empty menu is a customer who has
		 * taken everything back off it, and the old rows have to go.
		 */
		if (given == null || given.isEmpty()) {
			if (entity.getSerEventMasterId() != null && hasMenu(entity)) {
				entity = clearMenu(entity);
			}
			return new MenuResult(entity, categoryTotal, subCategoryTotal);
		}

		if (hasMenu(entity)) {
			entity = clearMenu(entity);
		} else if (entity.getMenuCategorySelections() == null) {
			entity.setMenuCategorySelections(new ArrayList<>());
		}

		for (DtoCustomerMenuCategory catDto : given) {
			EventMenuCategorySelection catEntity = new EventMenuCategorySelection();
			catEntity.setEventMaster(entity);
			catEntity.setCategory(menuItemById(menuItems, catDto.getCategoryId()));
			catEntity.priceAs(catDto.getNumPrice(), catDto.getNumFinalPrice());

			if (catDto.getNumFinalPrice() != null) {
				categoryTotal = categoryTotal.add(catDto.getNumFinalPrice());
			}

			if (catEntity.getSubCategories() == null) {
				catEntity.setSubCategories(new ArrayList<>());
			}

			for (DtoCustomerMenuSubCategory subDto : catDto.getSubCategories()) {
				EventMenuSubCategorySelection subEntity = new EventMenuSubCategorySelection();
				subEntity.setEventCategory(catEntity);
				subEntity.setSubCategory(menuItemById(menuItems, subDto.getSubCategoryId()));
				subEntity.priceAs(subDto.getNumPrice(), subDto.getNumFinalPrice());

				if (subDto.getNumFinalPrice() != null) {
					subCategoryTotal = subCategoryTotal.add(subDto.getNumFinalPrice());
				}

				if (subEntity.getItems() == null) {
					subEntity.setItems(new ArrayList<>());
				}

				for (DtoMenuItem itemDto : subDto.getItems()) {
					subEntity.getItems().add(dishOn(entity, subEntity,
							menuItemById(menuItems, itemDto.getSerMenuItemId()),
							itemDto.getNumPrice(), itemDto.getNumCalculatedPrice(), itemDto.getNumFinalPrice()));
				}

				/*
				 * A composite dish — a grazing bar, a dessert table — is stored
				 * against the parent it was assembled from, not against its
				 * parts, so it is matched by the parent's id.
				 */
				if (subDto.getCompositeItems() != null) {
					for (DtoMenuComponentRequest itemDto : subDto.getCompositeItems()) {
						subEntity.getItems().add(dishOn(entity, subEntity,
								menuItemById(menuItems, itemDto.getParentMenuItemId()),
								itemDto.getNumPrice(), itemDto.getNumCalculatedPrice(), itemDto.getNumFinalPrice()));
					}
				}

				catEntity.getSubCategories().add(subEntity);
			}

			entity.getMenuCategorySelections().add(catEntity);
		}

		return new MenuResult(entity, categoryTotal, subCategoryTotal);
	}

	private static boolean hasMenu(EventMaster entity) {
		return entity.getMenuCategorySelections() != null && !entity.getMenuCategorySelections().isEmpty();
	}

	/**
	 * Takes the stored menu apart and deletes it.
	 *
	 * <p>
	 * Every level is unlinked from its parent on the way down before anything
	 * is cleared. Clearing the top collection alone leaves the courses and
	 * dishes beneath it still referenced, so {@code orphanRemoval} never fires
	 * for them and they stay in the database attached to nothing.
	 *
	 * <p>
	 * The flush is what makes the deletes land before the replacements are
	 * added, and it returns a different instance — which is why the event is
	 * handed back rather than only mutated.
	 */
	private EventMaster clearMenu(EventMaster entity) {
		for (EventMenuCategorySelection category : new ArrayList<>(entity.getMenuCategorySelections())) {
			category.setEventMaster(null);

			if (category.getSubCategories() != null) {
				for (EventMenuSubCategorySelection subCategory : new ArrayList<>(category.getSubCategories())) {
					subCategory.setEventCategory(null);

					if (subCategory.getItems() != null) {
						for (EventMenuFoodSelection item : new ArrayList<>(subCategory.getItems())) {
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
		return repositoryEventMaster.saveAndFlush(entity);
	}

	/**
	 * The catalogue entry behind a chosen id, or null when it no longer exists.
	 *
	 * <p>
	 * Takes a {@link Number} because the identifier is a {@code Long} on the
	 * menu DTOs and an {@code Integer} on the entity, and every call site
	 * compared them by {@code intValue()} already.
	 */
	private static MenuItem menuItemById(List<MenuItem> menuItems, Number id) {
		if (id == null) {
			return null;
		}
		return menuItems.stream()
				.filter(item -> item.getSerMenuItemId() != null && item.getSerMenuItemId().intValue() == id.intValue())
				.findFirst().orElse(null);
	}

	/** One dish on the booking, under its course. */
	private static EventMenuFoodSelection dishOn(EventMaster entity, EventMenuSubCategorySelection subEntity,
			MenuItem menuItem, BigDecimal price, BigDecimal calculatedPrice, BigDecimal finalPrice) {
		EventMenuFoodSelection itemEntity = new EventMenuFoodSelection();
		itemEntity.setEventMaster(entity);
		itemEntity.setEventSubCategory(subEntity);
		itemEntity.setMenuItem(menuItem);
		itemEntity.setNumPrice(price);
		itemEntity.setNumCalculatedPrice(calculatedPrice);
		itemEntity.setNumFinalPrice(finalPrice);
		return itemEntity;
	}

	/**
	 * The two ways a décor payload is treated, by who sent it.
	 *
	 * <p>
	 * Named rather than passed as a pair of bare booleans, because
	 * {@code applyDecorSelections(…, false, false)} at a call site says nothing
	 * about what is different, and these two differences are the whole reason
	 * the code was written out twice.
	 *
	 * @param keepRowIds            the office posts back the row ids it was
	 *                              given and they are left on the entity; the
	 *                              journey clears them so every save inserts
	 *                              fresh rows.
	 * @param takePricesFromPayload the office prices décor properties. A
	 *                              customer does not, so the journey drops any
	 *                              price in the payload and the column keeps the
	 *                              entity's default of zero.
	 */
	private record DecorRules(boolean keepRowIds, boolean takePricesFromPayload) {
	}

	private static final DecorRules JOURNEY_DECOR = new DecorRules(false, false);
	private static final DecorRules OFFICE_DECOR = new DecorRules(true, true);

	/**
	 * What the décor added up to, handed back for the caller's running totals.
	 *
	 * <p>
	 * Both totals are write-only today — every read of them in this file is
	 * commented out. They are returned and accumulated anyway, because removing
	 * a total is a separate decision from writing this code once instead of
	 * twice, and mixing the two would make it impossible to say this change
	 * altered nothing.
	 */
	private record DecorTotals(BigDecimal categories, BigDecimal properties) {
	}

	/**
	 * The décor chosen for a booking that already exists.
	 *
	 * <h3>What this replaced</h3>
	 *
	 * The same hundred and seventy lines in the journey's save and in the
	 * office's, differing in the two things {@link DecorRules} names and
	 * nothing else.
	 *
	 * <h3>Why only the bookings that already exist</h3>
	 *
	 * Each save path has a second décor block for a booking being created, and
	 * those two are <em>not</em> copies of these. They skip the running totals,
	 * handle reference documents the older way, and assign the collection with
	 * {@code setDecorSelections} rather than adding to it. The journey's also
	 * matches the décor catalogue by the wrong identifier — the id of the
	 * selection row, which is not set on a booking being created, rather than
	 * the id of the catalogue property. Folding those in here would silently
	 * change four behaviours at once, so they are left where they are.
	 *
	 * <h3>Why the collection is cleared and added to</h3>
	 *
	 * {@code orphanRemoval} requires it. Assigning a new list leaves Hibernate
	 * holding a collection it no longer manages and the old rows are never
	 * deleted, which is the defect the commented-out {@code setDecorSelections}
	 * beneath the loop used to cause.
	 */
	private DecorTotals applyDecorSelections(List<DtoEventDecorCategorySelection> given, EventMaster entity,
			List<DecorCategoryPropertyMaster> propertyMasters, List<DecorCategoryPropertyValue> propertyValues,
			List<MultipartFile> files, Map<String, MultipartFile> fileMap, DecorRules rules) throws IOException {

		BigDecimal categoryTotal = BigDecimal.ZERO;
		BigDecimal propertyTotal = BigDecimal.ZERO;

		if (UtilRandomKey.isNull(given)) {
			return new DecorTotals(categoryTotal, propertyTotal);
		}

		if (entity.getDecorSelections() != null) {
			entity.getDecorSelections().clear();
		}

		List<EventDecorCategorySelection> decorSelections = new ArrayList<>();

		for (DtoEventDecorCategorySelection dto : given) {
			EventDecorCategorySelection decorSelection = MapperEventDecorCategorySelection.toEntity(dto);
			if (!rules.keepRowIds()) {
				decorSelection.setSerEventDecorCategorySelectionId(null);
			}
			decorSelection.setEventMaster(entity);

			if (decorSelection.getNumPrice() != null) {
				categoryTotal = categoryTotal.add(decorSelection.getNumPrice());
			}

			if (dto.getSelectedProperties() != null && !dto.getSelectedProperties().isEmpty()) {
				if (decorSelection.getSelectedProperties() != null) {
					decorSelection.getSelectedProperties().clear();
				}

				List<EventDecorPropertySelection> newSelectedProperties = new ArrayList<>();
				for (DtoEventDecorPropertySelection property : dto.getSelectedProperties()) {
					EventDecorPropertySelection propertySelection = new EventDecorPropertySelection();
					propertySelection.setBlnIsActive(true);
					propertySelection.setBlnIsDeleted(false);
					propertySelection.setCreatedDate(UtilDateAndTime.getCurrentDate());
					propertySelection.setEventDecorCategorySelection(decorSelection);

					if (rules.takePricesFromPayload()) {
						propertySelection.setNumPrice(property.getNumPrice());
					}
					if (propertySelection.getNumPrice() != null) {
						propertyTotal = propertyTotal.add(propertySelection.getNumPrice());
					}

					propertySelection.setProperty(propertyMasters.stream()
							.filter(pm -> pm.getSerPropertyId().intValue() == property.getSerPropertyId().intValue())
							.findFirst().orElse(null));

					Set<EventDecorPropertyValueSelection> selectedValues = new HashSet<>();
					if (property.getSerPropertyValueIds() != null) {
						for (Integer valueId : property.getSerPropertyValueIds()) {
							EventDecorPropertyValueSelection val = new EventDecorPropertyValueSelection();
							val.setEventDecorPropertySelection(propertySelection);
							val.setPropertyValue(propertyValues.stream()
									.filter(pv -> pv.getSerPropertyValueId().intValue() == valueId)
									.findFirst().orElse(null));
							selectedValues.add(val);
						}
					}
					propertySelection.setSelectedValues(selectedValues);

					newSelectedProperties.add(propertySelection);
				}

				decorSelection.getSelectedProperties().addAll(newSelectedProperties);
			}

			applyReferenceDocuments(dto, decorSelection, files, fileMap);
			decorSelections.add(decorSelection);
		}

		entity.getDecorSelections().addAll(decorSelections);
		return new DecorTotals(categoryTotal, propertyTotal);
	}

	/**
	 * The customer's own reference pictures for a décor choice.
	 *
	 * <h3>Why the entities are rebuilt rather than reused</h3>
	 *
	 * The pictures already on the booking arrive back as DTOs, detached from
	 * any session. Handing those to Hibernate attached to a freshly built
	 * selection makes it treat them as rows to update rather than insert, and
	 * they end up pointing at the selection that has just been cleared away.
	 * Building new ones and letting the ids be assigned is what stops a save
	 * that changes nothing about the pictures from losing them.
	 */
	private void applyReferenceDocuments(DtoEventDecorCategorySelection dto,
			EventDecorCategorySelection decorSelection, List<MultipartFile> files,
			Map<String, MultipartFile> fileMap) throws IOException {

		if (decorSelection.getUserUploadedDocuments() == null) {
			return;
		}

		boolean hasNewFiles = files != null && files.stream().anyMatch(f -> f != null && !f.isEmpty());

		if (hasNewFiles) {
			decorSelection.getUserUploadedDocuments().clear();
			List<EventDecorReferenceDocument> documents = new ArrayList<>();

			for (DtoEventDecorReferenceDocument dtoImg : dto.getUserUploadedDocuments()) {
				MultipartFile file = fileMap.get(dtoImg.getOriginalName());
				if (file != null && !file.isEmpty()) {
					EventDecorReferenceDocument doc = new EventDecorReferenceDocument();
					doc.setDocumentName(file.getName());
					doc.setOriginalName(file.getOriginalFilename());
					doc.setDocumentType(file.getContentType());
					doc.setSize(String.valueOf(file.getSize()));
					doc.setFilePath(UtilFileStorage.saveFile(file, "UserReferenceDecor"));
					doc.setEventDecorCategorySelection(decorSelection);
					documents.add(doc);
				}
			}
			decorSelection.getUserUploadedDocuments().addAll(documents);

		} else if (dto.getUserUploadedDocuments() != null && !dto.getUserUploadedDocuments().isEmpty()) {
			decorSelection.getUserUploadedDocuments().clear();
			List<EventDecorReferenceDocument> existingDocs = new ArrayList<>();

			for (DtoEventDecorReferenceDocument dtoImg : dto.getUserUploadedDocuments()) {
				EventDecorReferenceDocument doc = new EventDecorReferenceDocument();
				doc.setDocumentName(dtoImg.getDocumentName());
				doc.setOriginalName(dtoImg.getOriginalName());
				doc.setDocumentType(dtoImg.getDocumentType());
				doc.setSize(dtoImg.getSize());
				doc.setFilePath(dtoImg.getTxtDocumentUrl());
				doc.setEventDecorCategorySelection(decorSelection);
				existingDocs.add(doc);
			}
			decorSelection.getUserUploadedDocuments().addAll(existingDocs);
		}
		// else: no pictures at all — leave empty
	}

	/**
	 * Which hall of which venue, or the reason the booking cannot have it.
	 *
	 * <h3>Why a venue can be refused</h3>
	 *
	 * A hall taken out of service must not end up on a booking made after it
	 * was withdrawn, so the save stops rather than storing a room the business
	 * can no longer offer. The other refusal is a venue chosen without a hall,
	 * which is not enough to cater or lay out.
	 *
	 * <p>
	 * It hands back the refusal rather than a boolean, matching
	 * {@code refuseUnreadableDate} above: {@code null} means the venue was
	 * applied and the save may continue.
	 *
	 * <h3>What this replaced</h3>
	 *
	 * The same eighteen lines four times over — once per portal, and again in
	 * each for whether the booking already existed. All four were identical,
	 * down to the commented-out progress counter at the bottom.
	 */
	private DtoResult applyVenue(DtoEventVenue given, EventMaster entity) {
		if (UtilRandomKey.isNull(given)) {
			return null;
		}

		if (UtilRandomKey.isNull(given.getSerVenueMasterDetailId())) {
			DtoResult refusal = new DtoResult();
			refusal.setTxtMessage("Venue Hall Is Not Selected");
			return refusal;
		}

		DtoResult found = serviceVenueMaster
				.getVenueDetailByVenueMasterDetailId(given.getSerVenueMasterDetailId());
		if (!found.getTxtMessage().equalsIgnoreCase("Success")) {
			DtoResult refusal = new DtoResult();
			refusal.setTxtMessage("Venue Hall Is Not Active");
			return refusal;
		}

		entity.setVenueMasterDetail((VenueMasterDetail) found.getResult());
		return null;
	}

	/**
	 * The order of the evening, from whichever portal typed it.
	 *
	 * <h3>What this replaced</h3>
	 *
	 * Four copies of the same forty lines: the journey and the office each had
	 * one for a booking that already exists and another for a booking being
	 * created. The two "new booking" copies were the {@code else} half of the
	 * two "existing booking" copies, written out again a few hundred lines
	 * further down.
	 *
	 * <p>
	 * Nothing is lost by collapsing them. A booking being created has no
	 * running order yet, so it takes the same {@code else} branch the separate
	 * copies hard-coded — the difference between the four was which of them the
	 * reader happened to be looking at, not what any of them did.
	 *
	 * <h3>Why it updates in place rather than replacing</h3>
	 *
	 * The row is pointed at by {@code event_master}, so replacing it would
	 * leave the old one orphaned and the booking briefly pointing at nothing.
	 * Updating the existing row keeps the foreign key valid throughout.
	 */
	private void applyRunningOrder(DtoEventRunningOrder given, EventMaster entity) {
		if (given == null) {
			return;
		}

		EventRunningOrder runningOrder;
		if (UtilRandomKey.isNotNull(entity.getEventRunningOrder())) {
			runningOrder = entity.getEventRunningOrder();
			runningOrder.setTxtGuestArrival(given.getTxtGuestArrival());
			runningOrder.setTxtBaratArrival(given.getTxtBaratArrival());
			runningOrder.setTxtBrideEntrance(given.getTxtBrideEntrance());
			runningOrder.setTxtNikah(given.getTxtNikah());
			runningOrder.setTxtMeal(given.getTxtMeal());
			runningOrder.setTxtEndOfNight(given.getTxtEndOfNight());
			runningOrder.setTxtBrideGuestArrival(given.getTxtBrideGuestArrival());
			runningOrder.setTxtGroomGuestArrival(given.getTxtGroomGuestArrival());
			runningOrder.setTxtGroomEntrance(given.getTxtGroomEntrance());
			runningOrder.setTxtCouplesEntrance(given.getTxtCouplesEntrance());
			runningOrder.setTxtDua(given.getTxtDua());
			runningOrder.setTxtDance(given.getTxtDance());
			runningOrder.setTxtCakeCutting(given.getTxtCakeCutting());
			runningOrder.setTxtRingExchange(given.getTxtRingExchange());
			runningOrder.setTxtRams(given.getTxtRams());
			runningOrder.setTxtSpeeches(given.getTxtSpeeches());
		} else {
			runningOrder = MapperEventRunningOrder.toEntity(given);
		}

		entity.setEventRunningOrder(repositoryEventRunningOrder.save(runningOrder));
	}

	/**
	 * Writes down that the customer agreed to the terms.
	 *
	 * <h3>Why this is not just a field on the entity</h3>
	 *
	 * Because {@code dteTermsAcceptedOn} is {@code updatable = false}, and it
	 * has to be: every save in this codebase writes a detached entity built
	 * from a DTO, so a writable column would be included in each UPDATE as null
	 * and the record of agreement would be erased on the next save of the same
	 * booking — silently, starting with the bookings the office edits most.
	 * That closes the ordinary route, so this takes the deliberate one.
	 *
	 * <h3>Why it matters that it is recorded at all</h3>
	 *
	 * The tick lived in a React {@code useState} and nowhere else. It was gone
	 * when the component unmounted, so a customer coming back to the step was
	 * asked to agree again — and the business had no record that anybody had
	 * agreed to anything. The policy it gates says the deposit is
	 * non-refundable, that the balance falls due four weeks out, and that the
	 * venue may cancel and keep the deposit over a third-party decorator. Every
	 * one of those is a clause somebody may dispute, and "the box was ticked"
	 * is not evidence unless it was written down.
	 *
	 * <p>
	 * Only ever true sets it, and only the first one: see the repository
	 * statement, which will not move a timestamp that is already there.
	 */
	/*
	  Takes the value rather than the DTO.

	  It used to take DtoEventMaster, which meant only the journey's save path
	  could call it — the office's path holds a DtoEventMasterAdminPortal, a
	  near-identical class that happened not to carry the field. That is the
	  shape of every drift bug in this file: a helper typed to one caller's DTO
	  cannot be reused by the other, so the other quietly goes without.
	*/
	private void recordTermsAcceptance(Boolean accepted, Integer eventId) {
		if (eventId == null || !Boolean.TRUE.equals(accepted)) {
			return;
		}

		if (repositoryEventMaster.recordTermsAccepted(eventId, new Date()) > 0) {
			LOGGER.info("Event {} — customer accepted the terms and payment policy", eventId);
		}
	}


	/**
	 * Replaces the customer's own supplier list, when the save mentions it.
	 *
	 * <h3>The rule this follows</h3>
	 *
	 * "A save that does not mention something must not remove it" — the same
	 * rule as C4c, and for the same reason. Every step of the journey posts the
	 * whole event back, and a step that has never heard of external suppliers
	 * sends no {@code externalSuppliers} field at all. Treating that as "the
	 * customer has none" would have the venue step quietly delete the
	 * photographer the customer declared two screens earlier.
	 *
	 * <p>
	 * A null list means the save is silent on the subject and is left alone. An
	 * empty list is a statement — "I have removed them all" — and is honoured.
	 *
	 * <h3>Replace rather than merge</h3>
	 *
	 * Because the journey sends the list as the customer has it on screen:
	 * rows added, rows edited, rows deleted, in order. Merging by id would need
	 * the client to track deletions separately, which is the shape that leaves
	 * a removed supplier alive in the database and on the run sheet.
	 *
	 * <p>
	 * Rows with nothing in them are dropped rather than stored. The journey
	 * shows an empty row to type into, and a customer who opens the step and
	 * leaves without typing has not declared a blank supplier.
	 */
	/* Takes the list rather than the DTO, for the reason above. */
	/**
	 * Works out what the booking costs, and writes it down.
	 *
	 * <h3>Why it reloads rather than pricing what is in hand</h3>
	 *
	 * Because the entity in memory is not the booking. The menu is saved three
	 * levels deep — a dish hangs off its course, which hangs off its category —
	 * so {@code entity.getFoodSelections()} is still whatever it held when the
	 * save began, and pricing that would miss every dish just chosen. Reading it
	 * back is what makes "priced from what is stored" true rather than
	 * aspirational.
	 *
	 * <h3>Why a failure here does not fail the save</h3>
	 *
	 * The booking is already written by this point. Refusing to save it because
	 * a price could not be worked out would lose a customer's enquiry over an
	 * arithmetic problem, which is the wrong trade in both directions: the
	 * enquiry is the thing the business cannot replace, and a missing price is
	 * visible on the next screen anybody opens.
	 *
	 * <h3>Why the client's figures are still used</h3>
	 *
	 * Until somebody turns {@code pricing.server.authoritative} on. Two sets of
	 * rules that have never been compared will differ, and the place to find out
	 * is a log on real bookings rather than a customer's invoice. Both sets of
	 * figures are stored either way, which is what makes the comparison possible.
	 */
	private void priceTheBooking(Integer eventId, DtoEventQuoteAndStatus fromClient) {
		if (eventId == null) {
			return;
		}

		try {
			EventMaster stored = repositoryEventMaster.findByIdAndBlnIsDeletedFalse(eventId).orElse(null);
			if (stored == null) {
				return;
			}

			ServiceEventPricing.Priced priced = serviceEventPricing.priceAndRecord(stored);

			EventBudget budget = serviceEventBudget.getEventBudgetByEventId(eventId);
			if (budget == null) {
				return;
			}

			budget.setNumCalculatedFood(priced.getFood());
			budget.setNumCalculatedDecor(priced.getDecor());
			budget.setNumCalculatedExtras(priced.getExtras());
			budget.setNumCalculatedServices(priced.getServices());
			budget.setNumCalculatedVat(priced.getVat());
			budget.setNumCalculatedSubtotal(priced.getSubtotal());
			budget.setNumCalculatedTotal(priced.getTotal());
			budget.setDteCalculatedOn(UtilDateAndTime.getCurrentDate());

			if (serviceAppSettings.isServerPricingAuthoritative()) {
				budget.setNumFoodAmount(priced.getFood());
				budget.setNumDecorAmount(priced.getDecor().add(priced.getExtras()));
				budget.setNumServicesAmount(priced.getServices());
				budget.setNumDecorExtrasVat(priced.getVat());
				budget.setNumQuotedPrice(priced.getSubtotal().add(priced.getVat()));
				budget.setNumFinalAmount(priced.getTotal());
			} else {
				serviceEventPricing.reportDisagreement(eventId,
						fromClient == null ? null : fromClient.getNumFinalAmount(),
						priced.getTotal());
			}

			serviceEventBudget.save(budget);

		} catch (Exception e) {
			/*
			 * Logged rather than thrown. See the note above: the booking is
			 * already saved, and losing it over a pricing fault would be the
			 * worse outcome by a long way.
			 */
			LOGGER.error("Could not price event {}: {}", eventId, e.getMessage(), e);
		}
	}

	private void replaceExternalSuppliers(List<DtoEventExternalSupplier> given, EventMaster entity) {
		if (given == null || entity == null || entity.getSerEventMasterId() == null) {
			return;
		}

		repositoryEventExternalSupplier
				.deleteAll(repositoryEventExternalSupplier.findByEventMaster_SerEventMasterId(
						entity.getSerEventMasterId()));

		int order = 0;
		for (DtoEventExternalSupplier dto : given) {
			EventExternalSupplier supplier = new EventExternalSupplier();
			supplier.setEventMaster(entity);
			/*
			  Resolved from the id, never taken from a name the client sent. A
			  category is a row the office owns; letting a payload create or
			  rename one by spelling it differently is how the free text this
			  replaced ended up holding "DJ", "dj" and "Disc Jockey".
			 */
			supplier.setSupplierCategory(dto.getSerSupplierCategoryId() == null ? null
					: repositoryExternalSupplierCategory
							.findById(dto.getSerSupplierCategoryId()).orElse(null));
			supplier.setTxtSupplierName(trimToNull(dto.getTxtSupplierName()));
			supplier.setTxtContactName(trimToNull(dto.getTxtContactName()));
			supplier.setTxtContactPhone(trimToNull(dto.getTxtContactPhone()));
			supplier.setTxtContactEmail(trimToNull(dto.getTxtContactEmail()));
			supplier.setTxtNotes(trimToNull(dto.getTxtNotes()));

			if (supplier.isEmpty()) {
				continue;
			}

			supplier.setNumDisplayOrder(order++);
			supplier.setBlnIsActive(true);
			supplier.setBlnIsDeleted(false);
			repositoryEventExternalSupplier.save(supplier);
		}
	}

	private static String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	/** The customer's own suppliers, for the event document and the portals. */
	private List<DtoEventExternalSupplier> readExternalSuppliers(Integer eventId) {
		List<DtoEventExternalSupplier> dtos = new ArrayList<>();

		for (EventExternalSupplier supplier : repositoryEventExternalSupplier
				.findByEventMaster_SerEventMasterIdAndBlnIsDeletedFalseOrderByNumDisplayOrderAscSerEventExternalSupplierIdAsc(
						eventId)) {
			DtoEventExternalSupplier dto = new DtoEventExternalSupplier();
			dto.setSerEventExternalSupplierId(supplier.getSerEventExternalSupplierId());
			if (supplier.getSupplierCategory() != null) {
				dto.setSerSupplierCategoryId(supplier.getSupplierCategory().getSerSupplierCategoryId());
				// The word as well as the number, so the review screen, the
				// enquiry document and the office's form can all print
				// "Photographer" without fetching the category list.
				dto.setTxtSupplierCategoryName(supplier.getSupplierCategory().getTxtName());
			}
			dto.setTxtSupplierName(supplier.getTxtSupplierName());
			dto.setTxtContactName(supplier.getTxtContactName());
			dto.setTxtContactPhone(supplier.getTxtContactPhone());
			dto.setTxtContactEmail(supplier.getTxtContactEmail());
			dto.setTxtNotes(supplier.getTxtNotes());
			dto.setNumDisplayOrder(supplier.getNumDisplayOrder());
			dtos.add(dto);
		}

		return dtos;
	}

	private int countEventsOn(java.time.LocalDate day, Integer eventId) {
		Date asDate = Date.from(day.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());

		return repositoryEventMaster.countEventsOnDate(
				UtilDateAndTime.getStartOfDay(asDate), UtilDateAndTime.getEndOfDay(asDate), eventId);
	}

	/**
	 * Why a date was refused, in the words the frontends already show.
	 *
	 * <p>
	 * A Monday whose Sunday took a third is the one worth naming: its capacity is
	 * zero, and "fully booked (max 0 events)" tells somebody nothing about why a
	 * completely empty day cannot be used.
	 */
	private String fullMessage(java.time.LocalDate day, int capacity) {
		if (capacity == 0) {
			return "Cannot book Monday because Sunday is fully booked";
		}
		if (day.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
			return "Sunday is fully booked (Max " + capacity + " events)";
		}
		return "This date is fully booked (Max " + capacity + " events)";
	}

	private java.time.LocalDate toLocalDate(Date date) {
		return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
	}
	
	
	private BigDecimal calculateDecorExtrasVat(BigDecimal numDecorAmount) {
	    if (numDecorAmount == null || numDecorAmount.compareTo(BigDecimal.ZERO) <= 0) {
	        return BigDecimal.ZERO;
	    }
	    return numDecorAmount.multiply(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP);
	}
	
	private BigDecimal calculateFinalAmount(BigDecimal numQuotedPrice, BigDecimal numDiscount) {
	    BigDecimal quoted   = (numQuotedPrice != null) ? numQuotedPrice : BigDecimal.ZERO;
	    BigDecimal discount = (numDiscount    != null) ? numDiscount    : BigDecimal.ZERO;
	    BigDecimal result   = quoted.subtract(discount);
	    return result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result;
	}
	private void applyAmountFields(EventBudget eventBudget, DtoEventQuoteAndStatus quoteAndStatus) {
	    BigDecimal foodAmount     = safeValue(quoteAndStatus.getNumFoodAmount());
	    BigDecimal servicesAmount = safeValue(quoteAndStatus.getNumServicesAmount());
	    BigDecimal decorAmount    = safeValue(quoteAndStatus.getNumDecorAmount());
	    BigDecimal quotedPrice    = safeValue(quoteAndStatus.getNumQuotedPrice());
	    BigDecimal discount       = safeValue(quoteAndStatus.getNumDiscount());

	    BigDecimal decorExtrasVat = calculateDecorExtrasVat(decorAmount);
	    BigDecimal finalAmount    = calculateFinalAmount(quotedPrice, discount);

	    eventBudget.setNumFoodAmount(foodAmount);
	    eventBudget.setNumServicesAmount(servicesAmount);
	    eventBudget.setNumDecorAmount(decorAmount);
	    eventBudget.setNumDecorExtrasVat(decorExtrasVat);
	    eventBudget.setNumFinalAmount(finalAmount);
	}
	
	private BigDecimal safeValue(BigDecimal value) {
	    return (value != null) ? value : BigDecimal.ZERO;
	}
}
