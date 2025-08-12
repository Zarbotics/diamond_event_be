package com.zbs.de.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zbs.de.model.EventMenuFoodSelection;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryEventMenuFoodSelection;
import com.zbs.de.service.ServiceEventMenuFoodSelection;
import com.zbs.de.util.UtilRandomKey;

@Service("serviceEventMenuFoodSelectionImpl")
public class ServiceEventMenuFoodSelectionImpl implements ServiceEventMenuFoodSelection {

	@Autowired
	private RepositoryEventMenuFoodSelection repositoryEventMenuFoodSelection;

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceEventMasterImpl.class);

	@Override
	public List<EventMenuFoodSelection> getByEventMasterId(Integer serEventMasterId) {
		try {
			List<EventMenuFoodSelection> eventMenuFoodSelections = repositoryEventMenuFoodSelection
					.findByEventId(serEventMasterId);
			return eventMenuFoodSelections;
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			return null;
		}
	}

	@Override
	public DtoResult deleteByEventMasterId(Integer serEventMasterId) {
		DtoResult dtoResult = new DtoResult();

		try {
			List<EventMenuFoodSelection> eventMenuFoodSelections = repositoryEventMenuFoodSelection
					.findByEventId(serEventMasterId);

			if (UtilRandomKey.isNotNull(eventMenuFoodSelections)) {
				for (EventMenuFoodSelection selection : eventMenuFoodSelections) {
					selection.setBlnIsActive(true);
					selection.setUpdatedBy(ServiceCurrentUser.getCurrentUserId());

					repositoryEventMenuFoodSelection.save(selection);
				}
			}
			dtoResult.setTxtMessage("Deleted SuccessFully");

		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			dtoResult.setTxtMessage(e.getMessage());
		}
		return dtoResult;
	}

	public String saveAll(List<EventMenuFoodSelection> eventMenuFoodSelectionLst) {
		try {
			if (UtilRandomKey.isNotNull(eventMenuFoodSelectionLst)) {
				for (EventMenuFoodSelection entity : eventMenuFoodSelectionLst) {
					if (UtilRandomKey.isNull(entity.getEventMaster())) {
						return "EventMenueFoodSelection Don't Have EventMaster Id.";
					}

					if (UtilRandomKey.isNull(entity.getMenuFoodMaster())) {
						return "EventMenuFoodSelection Don't Have MenuFoodMaster Id";
					}
				}

				repositoryEventMenuFoodSelection.saveAllAndFlush(eventMenuFoodSelectionLst);
			}

			return "Success";
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			return e.getMessage();
		}
	}
}
