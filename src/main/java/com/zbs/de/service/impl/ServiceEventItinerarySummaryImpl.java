package com.zbs.de.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zbs.de.model.EventItinerarySummary;
import com.zbs.de.model.EventMaster;
import com.zbs.de.model.EventMenuFoodSelection;
import com.zbs.de.model.EventMenuItinerary;
import com.zbs.de.model.ItineraryAssignment;
import com.zbs.de.model.ItineraryAssignmentDetail;
import com.zbs.de.model.ItineraryItem;
import com.zbs.de.model.MenuItem;
import com.zbs.de.model.dto.DtoEventItinerarySummary;
import com.zbs.de.model.dto.DtoEventMenuItinerary;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryEventItinerarySummary;
import com.zbs.de.repository.RepositoryEventMenuItinerary;
import com.zbs.de.service.ServiceEventItinerarySummary;
import com.zbs.de.service.ServiceEventMaster;
import com.zbs.de.service.ServiceEventMenuFoodSelection;
import com.zbs.de.service.ServiceItineraryAssignment;
import com.zbs.de.util.enums.EnmItineraryUnitType;

@Service("serviceEventItinerarySummaryImpl")
public class ServiceEventItinerarySummaryImpl implements ServiceEventItinerarySummary {

	@Autowired
	private ServiceEventMaster serviceEventMaster;

	@Autowired
	private ServiceEventMenuFoodSelection serviceEventMenuFoodSelection;

	@Autowired
	private ServiceItineraryAssignment serviceItineraryAssignment;

	@Autowired
	private RepositoryEventMenuItinerary eventMenuItineraryRepo;

	@Autowired
	private RepositoryEventItinerarySummary eventItinerarySummaryRepo;

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceEventItinerarySummaryImpl.class);

	@Override
	public void calculateEventItinerary(Integer serEventMasterId) {

		EventMaster event = serviceEventMaster.getEventMasterById(serEventMasterId);

		Integer guests = event.getNumNumberOfGuests();
		if (guests == null || guests <= 0) {
			throw new RuntimeException("Guest count missing");
		}

		// 🔴 Step 0: Clean previous calculations
//		eventMenuItineraryRepo.softDeleteByEventId(serEventMasterId);
//		eventItinerarySummaryRepo.softDeleteByEventId(serEventMasterId);
		
		deleteEventMenuItineraryByEventMasterId(serEventMasterId);
		deleteEventItinerarySummaryBySerEventMasterId(serEventMasterId);

		// 🔹 Step 1: Menu-level calculation
		List<EventMenuFoodSelection> selections = serviceEventMenuFoodSelection.getByEventMasterId(serEventMasterId);

		List<EventMenuItinerary> menuItineraries = new ArrayList<>();

		for (EventMenuFoodSelection selection : selections) {

			MenuItem menuItem = selection.getMenuItem();

			List<ItineraryAssignment> itineraryAssignments = serviceItineraryAssignment
					.getAssignmentsByMenuItemId(menuItem.getSerMenuItemId());

			for (ItineraryAssignment assignment : itineraryAssignments) {

				for (ItineraryAssignmentDetail detail : assignment.getItineraryAssignmentDetails()) {

					int quantity = calculateQuantity(detail, guests);

					EventMenuItinerary emi = new EventMenuItinerary();
					emi.setEventMaster(event);
					emi.setMenuItem(menuItem);
					emi.setItineraryItem(detail.getItineraryItem());
					emi.setNumCalculatedQuantity(quantity);
					emi.setNumSequenceOrder(detail.getNumDisplayOrder());
					emi.setBlnIsActive(true);
					emi.setBlnIsDeleted(false);

					menuItineraries.add(emi);
				}
			}
		}

		eventMenuItineraryRepo.saveAll(menuItineraries);

		// 🔹 Step 2: Event-level aggregation
		Map<Long, Integer> aggregated = new LinkedHashMap<>();

		for (EventMenuItinerary emi : menuItineraries) {
			Long itineraryItemId = emi.getItineraryItem().getSerItineraryItemId();

			aggregated.merge(itineraryItemId, emi.getNumCalculatedQuantity(), Integer::sum);
		}

		List<EventItinerarySummary> summaries = new ArrayList<>();

		for (Map.Entry<Long, Integer> entry : aggregated.entrySet()) {

			ItineraryItem itineraryItem = new ItineraryItem();
			itineraryItem.setSerItineraryItemId(entry.getKey());

			EventItinerarySummary summary = new EventItinerarySummary();

			summary.setEventMaster(event);
			summary.setItineraryItem(itineraryItem);
			summary.setNumTotalQuantity(entry.getValue());
			summary.setBlnIsActive(true);
			summary.setBlnIsDeleted(false);

			summaries.add(summary);
		}

		eventItinerarySummaryRepo.saveAll(summaries);
	}

	private int calculateQuantity(ItineraryAssignmentDetail detail, Integer guests) {

		if (detail.getEnmMultiplierType() == EnmItineraryUnitType.PER_GUEST) {
			return detail.getNumMultiplierValue().multiply(BigDecimal.valueOf(guests)).intValue();
		}

		return detail.getNumMultiplierValue().intValue();
	}

	@Override
	public DtoResult getMenuItinerary(Integer eventId) {
		DtoResult dtoResult = new DtoResult();

		try {
			List<DtoEventMenuItinerary> itinerary = eventMenuItineraryRepo.findMenuItineraryViewByEvent(eventId);
			dtoResult.setResult(itinerary);
			return dtoResult;
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			dtoResult.setTxtMessage(e.getMessage());
			dtoResult.setResult(null);
			return dtoResult;
		}

	}

	@Override
	public DtoResult getEventItinerarySummary(Integer eventId) {
		DtoResult dtoResult = new DtoResult();
		try {
			List<DtoEventItinerarySummary> summary = eventItinerarySummaryRepo.findSummaryViewByEvent(eventId);
			dtoResult.setResult(summary);
			return dtoResult;
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			dtoResult.setTxtMessage(e.getMessage());
			dtoResult.setResult(null);
			return dtoResult;
		}

	}
	
	
	private void deleteEventMenuItineraryByEventMasterId(Integer serEventMasterId) {
		try {
			List<EventMenuItinerary> eventMenuItineraries = eventMenuItineraryRepo.findByEventMaster_SerEventMasterId(serEventMasterId);
			for(EventMenuItinerary itinerary : eventMenuItineraries) {
				itinerary.setBlnIsDeleted(true);
				itinerary.setBlnIsApproved(false);
				itinerary.setBlnIsActive(false);
			}
			eventMenuItineraryRepo.saveAllAndFlush(eventMenuItineraries);
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(),e);
		}
		
	}
	
	private void deleteEventItinerarySummaryBySerEventMasterId(Integer serEventMasterId) {
		
		try {
			List<EventItinerarySummary> summaries = eventItinerarySummaryRepo.findByEventMaster_SerEventMasterId(serEventMasterId);
			for(EventItinerarySummary summary: summaries) {
				summary.setBlnIsActive(false);
				summary.setBlnIsApproved(false);
				summary.setBlnIsDeleted(true);
			}
			eventItinerarySummaryRepo.saveAllAndFlush(summaries);
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(),e);
		}

	}
}
