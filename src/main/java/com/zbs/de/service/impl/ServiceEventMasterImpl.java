package com.zbs.de.service.impl;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zbs.de.mapper.MapperEventMaster;
import com.zbs.de.mapper.MapperEventRunningOrder;
import com.zbs.de.model.CustomerMaster;
import com.zbs.de.model.EventMaster;
import com.zbs.de.model.EventRunningOrder;
import com.zbs.de.model.EventType;
import com.zbs.de.model.dto.DtoEventMaster;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryEventMaster;
import com.zbs.de.repository.RepositoryEventRunningOrder;
import com.zbs.de.service.ServiceCustomerMaster;
import com.zbs.de.service.ServiceEventMaster;
import com.zbs.de.service.ServiceEventType;
import com.zbs.de.util.UtilDateAndTime;
import com.zbs.de.util.UtilRandomKey;

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

			// Set customer
			CustomerMaster customer = serviceCustomerMaster.getByPK(dtoEventMaster.getSerCustId());
			if (UtilRandomKey.isNull(customer)) {
				dtoResult.setTxtMessage("Customer Not Foound For Id" + dtoEventMaster.getSerCustId());
				return dtoResult;
			}

			if (UtilRandomKey.isNull(entity.getCustomerMaster())) {
				if (UtilRandomKey.isNull(entity.getNumInfoFilledStatus())) {
					entity.setNumInfoFilledStatus(0);
				}
				entity.setNumInfoFilledStatus(entity.getNumInfoFilledStatus() + 1);
			}
			entity.setCustomerMaster(customer);

			// Set event type
			EventType eventType = serviceEventType.getByPK(dtoEventMaster.getSerEventTypeId());
			if (UtilRandomKey.isNull(eventType)) {
				dtoResult.setTxtMessage("Event Type Not Foound For Id" + dtoEventMaster.getSerEventTypeId());
				return dtoResult;
			}

			if (UtilRandomKey.isNull(entity.getEventType())) {
				if (UtilRandomKey.isNull(entity.getNumInfoFilledStatus())) {
					entity.setNumInfoFilledStatus(0);
				}
				entity.setNumInfoFilledStatus(entity.getNumInfoFilledStatus() + 1);
			}
			entity.setEventType(eventType);
			entity.setNumInfoFilledStatus(entity.getNumInfoFilledStatus() + 1);

			// Set optional event running order
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
					if (UtilRandomKey.isNull(entity.getNumInfoFilledStatus())) {
						entity.setNumInfoFilledStatus(0);
					}
					entity.setNumInfoFilledStatus(entity.getNumInfoFilledStatus() + 1);
				}
				entity.setEventRunningOrder(runningOrder);
			}

		} else {
			// Create new
			entity = MapperEventMaster.toEntity(dtoEventMaster);
			if (UtilRandomKey.isNull(entity.getNumInfoFilledStatus())) {
				entity.setNumInfoFilledStatus(0);
			}

			// Set customer
			CustomerMaster customer = serviceCustomerMaster.getByPK(dtoEventMaster.getSerCustId());
			if (UtilRandomKey.isNull(customer)) {
				dtoResult.setTxtMessage("Customer Not Foound For Id" + dtoEventMaster.getSerCustId());
				return dtoResult;
			}
			entity.setCustomerMaster(customer);
			entity.setNumInfoFilledStatus(entity.getNumInfoFilledStatus() + 1);

			// Set event type
			EventType eventType = serviceEventType.getByPK(dtoEventMaster.getSerEventTypeId());
			if (UtilRandomKey.isNull(eventType)) {
				dtoResult.setTxtMessage("Event Type Not Foound For Id" + dtoEventMaster.getSerEventTypeId());
				return dtoResult;
			}
			entity.setEventType(eventType);
			entity.setNumInfoFilledStatus(entity.getNumInfoFilledStatus() + 1);

			// Set optional event running order
			if (dtoEventMaster.getDtoEventRunningOrder() != null) {
				EventRunningOrder runningOrder = new EventRunningOrder();
				runningOrder = MapperEventRunningOrder.toEntity(dtoEventMaster.getDtoEventRunningOrder());
				runningOrder = repositoryEventRunningOrder.save(runningOrder);
				entity.setEventRunningOrder(runningOrder);
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
}
