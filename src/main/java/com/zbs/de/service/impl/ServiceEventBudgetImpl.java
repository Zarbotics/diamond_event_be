package com.zbs.de.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zbs.de.controller.ControllerCityMaster;
import com.zbs.de.mapper.MapperEventBudget;
import com.zbs.de.model.EventBudget;
import com.zbs.de.model.EventMaster;
import com.zbs.de.model.dto.DtoEventAnalytics;
import com.zbs.de.model.dto.DtoEventBudget;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryEventBudget;
import com.zbs.de.repository.RepositoryEventMaster;
import com.zbs.de.service.ServiceEventBudget;

@Service("serviceEventBudgetImpl")
public class ServiceEventBudgetImpl implements ServiceEventBudget {

	private final ControllerCityMaster controllerCityMaster;

	@Autowired
	private RepositoryEventBudget repositoryEventBudget;

	@Autowired
	private RepositoryEventMaster repositoryEventMaster;

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCustomerMasterImpl.class);

	ServiceEventBudgetImpl(ControllerCityMaster controllerCityMaster) {
		this.controllerCityMaster = controllerCityMaster;
	}

	@Override
	public DtoResult saveOrUpdate(DtoEventBudget dtoEventBudget) {
		DtoResult result = new DtoResult();

		if (dtoEventBudget.getSerEventMasterId() == null) {
			result.setTxtMessage("Event ID is required.");
			return result;
		}

		Optional<EventMaster> optionalEvent = repositoryEventMaster.findById(dtoEventBudget.getSerEventMasterId());
		if (optionalEvent.isEmpty()) {
			result.setTxtMessage("Invalid Event ID.");
			return result;
		}

		EventMaster eventMaster = optionalEvent.get();

		Optional<EventBudget> optionalExisting = repositoryEventBudget
				.findByEventMaster_SerEventMasterId(dtoEventBudget.getSerEventMasterId());

		EventBudget eventBudget = MapperEventBudget.toEntity(dtoEventBudget, eventMaster);

		// Preserve ID if updating
		optionalExisting.ifPresent(existing -> eventBudget.setSerEventBudgetId(existing.getSerEventBudgetId()));

		repositoryEventBudget.save(eventBudget);

		result.setTxtMessage("Event budget saved successfully.");
		return result;
	}

	@Override
	public DtoEventBudget getByEventId(Integer serEventMasterId) {
		return repositoryEventBudget.findByEventMaster_SerEventMasterId(serEventMasterId).map(MapperEventBudget::toDto)
				.orElse(null);
	}

	@Override
	public List<DtoEventAnalytics> getMonthlySales() {
		return repositoryEventBudget.getMonthlySales();
	}

	@Override
	public List<DtoEventAnalytics> getMonthlyProfitByEventType() {
		List<Object[]> rawResults = repositoryEventBudget.getRawProfitByEventTypeAndMonth();
		List<DtoEventAnalytics> result = new ArrayList<>();

		for (Object[] row : rawResults) {
			String month = (String) row[0];
			String eventType = (String) row[1];
			BigDecimal profit = row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO;

			DtoEventAnalytics dto = new DtoEventAnalytics(month, eventType, profit);
			result.add(dto);
		}

		return result;
	}

	@Override
	public DtoEventAnalytics getOverallSummary() {
		return repositoryEventBudget.getSalesSummary();
	}

	@Override
	public DtoResult getAllData() {

		DtoResult dtoResult = new DtoResult();
		try {
			List<EventBudget> list = repositoryEventBudget.findByBlnIsDeleted(false);
			List<DtoEventBudget> dtolist = new ArrayList<>();
			if (list != null) {
				for (EventBudget eventBudget : list) {
					DtoEventBudget dtoEventBudget = MapperEventBudget.toDto(eventBudget);
					dtolist.add(dtoEventBudget);
				}
			}
			dtoResult.setTxtMessage("Success");
			dtoResult.setResulList(new ArrayList<>(dtolist));
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			dtoResult.setTxtMessage(e.getMessage());
		}

		return dtoResult;
	}

}
