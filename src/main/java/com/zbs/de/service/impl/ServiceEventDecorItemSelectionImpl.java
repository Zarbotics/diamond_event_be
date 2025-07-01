package com.zbs.de.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.zbs.de.mapper.MapperEventDecorItemSelection;
import com.zbs.de.model.EventDecorItemSelection;
import com.zbs.de.model.dto.DtoEventDecorItemSelection;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryEventDecorItemSelection;
import com.zbs.de.service.ServiceEventDecorItemSelection;
import com.zbs.de.util.UtilRandomKey;

public class ServiceEventDecorItemSelectionImpl implements ServiceEventDecorItemSelection {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceEventMasterImpl.class);

	@Autowired
	private RepositoryEventDecorItemSelection repositoryEventDecorItemSelection;

	@Override
	public DtoResult saveOrUpdate(DtoEventDecorItemSelection dto) {
		EventDecorItemSelection entity = MapperEventDecorItemSelection.toEntity(dto);
		repositoryEventDecorItemSelection.save(entity);
		return new DtoResult("Saved Successfully", null, MapperEventDecorItemSelection.toDto(entity), null);
	}

	@Override
	public DtoResult getAll() {
		List<DtoEventDecorItemSelection> list = repositoryEventDecorItemSelection.findAll().stream()
				.map(MapperEventDecorItemSelection::toDto).collect(Collectors.toList());
		return new DtoResult("Fetched Successfully", null, null, new ArrayList<>(list));
	}

	@Override
	public DtoResult getById(Integer id) {
		Optional<EventDecorItemSelection> optional = repositoryEventDecorItemSelection.findById(id);
		if (optional.isPresent()) {
			return new DtoResult("Found", null, MapperEventDecorItemSelection.toDto(optional.get()), null);
		}
		return new DtoResult("Not Found", null, null, null);
	}

	@Override
	public DtoResult deleteById(Integer id) {
		repositoryEventDecorItemSelection.deleteById(id);
		return new DtoResult("Deleted", null, null, null);
	}

	public DtoResult deleteByEventMasterId(Integer serEventMasterId) {
		DtoResult dtoResult = new DtoResult();
		try {
			List<EventDecorItemSelection> optionalResult = repositoryEventDecorItemSelection
					.findByEventId(serEventMasterId);
			if (UtilRandomKey.isNotNull(optionalResult)) {
				for (EventDecorItemSelection entity : optionalResult) {
					entity.setBlnIsDeleted(true);
					repositoryEventDecorItemSelection.save(entity);
				}
				dtoResult.setTxtMessage("Deleted Successfully");
			} else {
				dtoResult.setTxtMessage("No Event Decor Found In System");
			}
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			dtoResult.setTxtMessage(e.getMessage());
		}
		return dtoResult;
	}

	public List<EventDecorItemSelection> getByEventMasterId(Integer serEventMasterId) {
		try {
			List<EventDecorItemSelection> eventDecorItemSelections = repositoryEventDecorItemSelection
					.findByEventId(serEventMasterId);
			return eventDecorItemSelections;
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			return null;
		}
	}
}
