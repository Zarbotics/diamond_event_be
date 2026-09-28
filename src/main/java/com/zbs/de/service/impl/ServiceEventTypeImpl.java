package com.zbs.de.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.zbs.de.service.ServiceEventType;
import com.zbs.de.mapper.MapperEventType;
import com.zbs.de.model.EventType;
import com.zbs.de.model.EventTypeDocument;
import com.zbs.de.model.dto.DtoEventType;
import com.zbs.de.model.dto.DtoEventTypeDocument;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.EventTypeRunningOrderMoment;
import com.zbs.de.repository.RepositoryEventType;
import com.zbs.de.repository.RepositoryEventTypeRunningOrderMoment;
import com.zbs.de.util.ResponseMessage;
import com.zbs.de.util.UtilFileStorage;
import com.zbs.de.util.UtilRandomKey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Service("serviceEventType")
public class ServiceEventTypeImpl implements ServiceEventType {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceEventTypeImpl.class);

	@Autowired
	RepositoryEventType repositoryEventType;

	@Autowired
	RepositoryEventTypeRunningOrderMoment repositoryRunningOrderMoment;

	/**
	 * Which moments each kind of event has, read once for the whole answer.
	 *
	 * <p>
	 * The control panel asks for every event type at once to fill its dropdown,
	 * so the moments ride along with them rather than costing a request each.
	 */
	private Map<Integer, List<String>> momentsByEventType() {
		Map<Integer, List<String>> byType = new LinkedHashMap<>();

		for (EventTypeRunningOrderMoment moment : repositoryRunningOrderMoment.findAllInDayOrder()) {
			if (moment.getEventType() == null) {
				continue;
			}
			byType.computeIfAbsent(moment.getEventType().getSerEventTypeId(), k -> new ArrayList<>())
					.add(moment.getTxtField());
		}

		return byType;
	}

	/**
	 * Attaches the moments to every type in the answer, sub-events included.
	 *
	 * <p>
	 * The booking form flattens the sub-events and works from those, so a type
	 * that carried its moments only at the top would arrive with none.
	 */
	private List<DtoEventType> withMoments(List<DtoEventType> dtos) {
		attachMoments(dtos, momentsByEventType());
		return dtos;
	}

	private void attachMoments(List<DtoEventType> dtos, Map<Integer, List<String>> byType) {
		if (dtos == null) {
			return;
		}

		for (DtoEventType dto : dtos) {
			dto.setTxtRunningOrderMoments(byType.getOrDefault(dto.getSerEventTypeId(), List.of()));
			attachMoments(dto.getSubEvents(), byType);
		}
	}

	@Override
	public List<DtoEventType> getAllData() {
		List<EventType> list = repositoryEventType.findByBlnIsDeleted(false);
		List<DtoEventType> dtos = new ArrayList<>();
		for (EventType type : list) {
			dtos.add(MapperEventType.toDto(type));
		}
		return withMoments(dtos);
	}

	@Override
	public List<DtoEventType> getAllEventTypesWithSubEvents() {
		List<EventType> list = repositoryEventType.findByBlnIsDeleted(false);
		List<DtoEventType> dtos = new ArrayList<>();
		for (EventType type : list) {
			if (UtilRandomKey.isNotNull(type.getBlnIsMainEvent()) && !type.getBlnIsMainEvent()) {
				continue;
			}
			DtoEventType eventType = MapperEventType.toDto(type);

			// ******** Filtering Sub Events ****************
			// **********************************************
			List<DtoEventType> subEvents = new ArrayList<>();
			for (EventType subtype : list) {
				if (UtilRandomKey.isNotNull(subtype.getParentEventType()) && subtype.getParentEventType()
						.getSerEventTypeId().intValue() == type.getSerEventTypeId().intValue()) {
					subEvents.add(MapperEventType.toDto(subtype));

				}
			}

			eventType.setSubEvents(subEvents);
			dtos.add(eventType);

		}
		return withMoments(dtos);
	}
	
	
	@Override
	public List<DtoEventType> getAllActiveEventTypesWithSubEvents() {
		List<EventType> list = repositoryEventType.findAllActive();
		List<DtoEventType> dtos = new ArrayList<>();
		for (EventType type : list) {
			if (UtilRandomKey.isNotNull(type.getBlnIsMainEvent()) && !type.getBlnIsMainEvent()) {
				continue;
			}
			DtoEventType eventType = MapperEventType.toDto(type);

			// ******** Filtering Sub Events ****************
			// **********************************************
			List<DtoEventType> subEvents = new ArrayList<>();
			for (EventType subtype : list) {
				if (UtilRandomKey.isNotNull(subtype.getParentEventType()) && subtype.getParentEventType()
						.getSerEventTypeId().intValue() == type.getSerEventTypeId().intValue() && subtype.getBlnIsActive()!= null && subtype.getBlnIsActive()) {
					subEvents.add(MapperEventType.toDto(subtype));

				}
			}

			eventType.setSubEvents(subEvents);
			dtos.add(eventType);

		}
		return withMoments(dtos);
	}
	
	@Override
	public List<DtoEventType> getAllActiveSubEventsOnlyCP() {
		List<EventType> list = repositoryEventType.findAllActive();
		List<DtoEventType> subEvents = new ArrayList<>();
		for (EventType type : list) {
			if (UtilRandomKey.isNotNull(type.getBlnIsMainEvent()) && !type.getBlnIsMainEvent()) {
				continue;
			}

			// ******** Filtering Sub Events ****************
			// **********************************************
			for (EventType subtype : list) {
				if (UtilRandomKey.isNotNull(subtype.getParentEventType()) && subtype.getParentEventType()
						.getSerEventTypeId().intValue() == type.getSerEventTypeId().intValue()
						&& subtype.getBlnIsActive() != null && subtype.getBlnIsActive()) {
					subEvents.add(MapperEventType.toDto(subtype));

				}
			}

		}
		return withMoments(subEvents);
	}

	@Override
	public ResponseMessage saveAndUpdate(DtoEventType dto) {
		ResponseMessage res = new ResponseMessage();
		try {

			EventType entity = null;
			if (dto.getSerEventTypeId() != null) {
				Optional<EventType> existingOptional = repositoryEventType.findById(dto.getSerEventTypeId());
				if (existingOptional.isEmpty()) {
					res.setMessage("Event type not found for update.");
					return res;
				}
				entity = existingOptional.get();
			}

			// Prevent self-parenting
			if (dto.getParentEventTypeId() != null && dto.getSerEventTypeId() != null
					&& dto.getParentEventTypeId().equals(dto.getSerEventTypeId())) {
				res.setMessage("An event cannot be its own parent.");
				return res;
			}

			EventType parent = null;
			if (dto.getParentEventTypeId() != null) {
				Optional<EventType> parentOptional = repositoryEventType.findById(dto.getParentEventTypeId());
				if (parentOptional.isEmpty()) {
					res.setMessage("Parent Event Type not found.");
					return res;
				}
				parent = parentOptional.get();
			}

			if (entity == null) {
				entity = MapperEventType.toEntity(dto);
				entity.setParentEventType(parent);
				if(parent != null) {
					entity.setTxtEventTypeCode(generateNextEventTypeCode());
				}
				entity.setBlnIsActive(true);
				entity.setBlnIsDeleted(false);
				entity.setBlnIsApproved(true);
			} else {
				entity.setTxtEventTypeName(dto.getTxtEventTypeName());
				entity.setBlnIsMainEvent(dto.getBlnIsMainEvent());
				entity.setBlnIsActive(dto.getBlnIsActive());
				entity.setUpdatedDate(new Date());
			}

			boolean isNew = entity.getSerEventTypeId() == null;
			EventType saved = repositoryEventType.saveAndFlush(entity);

			if (isNew) {
				giveItTheWholeDay(saved);
			}

			res.setMessage("Saved successfully");
			res.setResult(saved);

		} catch (Exception e) {
			LOGGER.error("Error saving event type", e);
			res.setMessage("Unexpected error occurred while saving event type.");
		}
		return res;
	}

	/**
	 * A new kind of event starts with every moment a running order can have.
	 *
	 * <h4>Why it is seeded rather than left empty</h4>
	 *
	 * V26 gave every event type that existed at the time the full sixteen, so
	 * that nothing narrowed and no stored time was lost. A type created
	 * afterwards would have had none — and a running order with no moments is
	 * one that asks for nothing, so a booking for the business's newest kind of
	 * event would silently have nowhere to record when the guests arrive.
	 *
	 * <p>
	 * The whole day is also the only safe starting point. Narrowing is a
	 * decision with a consequence — a moment that stops being shown is a time
	 * that gets cleared on the next save — so it belongs to the business,
	 * deliberately, and not to whatever a new row happened to default to.
	 *
	 * <p>
	 * Failing here does not fail the save. The event type is the thing that
	 * matters and its moments can be set afterwards; losing the type because a
	 * secondary insert failed would be the worse outcome.
	 */
	private void giveItTheWholeDay(EventType type) {
		try {
			List<EventTypeRunningOrderMoment> moments = new ArrayList<>();
			int order = 10;

			for (String field : THE_WHOLE_DAY) {
				EventTypeRunningOrderMoment moment = new EventTypeRunningOrderMoment();
				moment.setEventType(type);
				moment.setTxtField(field);
				moment.setNumDisplayOrder(order);
				moment.setBlnIsActive(true);
				moment.setBlnIsDeleted(false);
				moments.add(moment);
				order += 10;
			}

			repositoryRunningOrderMoment.saveAll(moments);

		} catch (Exception e) {
			LOGGER.error("Could not give event type {} its running order moments", type.getSerEventTypeId(), e);
		}
	}

	/** In the order the day runs. The same sixteen V26 seeded. */
	private static final List<String> THE_WHOLE_DAY = List.of(
			"txtGuestArrival", "txtBrideGuestArrival", "txtGroomGuestArrival", "txtBaratArrival",
			"txtNikah", "txtBrideEntrance", "txtGroomEntrance", "txtCouplesEntrance",
			"txtDua", "txtRingExchange", "txtCakeCutting", "txtRams",
			"txtSpeeches", "txtDance", "txtMeal", "txtEndOfNight");

	@Override
	public ResponseMessage getById(Integer id) {
		ResponseMessage res = new ResponseMessage();
		try {
			Optional<EventType> optional = repositoryEventType.findById(id);
			if (optional.isPresent()) {
				res.setMessage("Record fetched successfully");
				res.setResult(MapperEventType.toDto(optional.get()));
			} else {
				res.setMessage("Record not found");
			}
		} catch (Exception e) {
			LOGGER.error("Error fetching event type", e);
			res.setMessage(e.getMessage());
		}
		return res;
	}

	@Override
	public EventType getByPK(Integer id) {
		try {
			Optional<EventType> optional = repositoryEventType.findById(id);
			if (optional.isPresent()) {
				return optional.get();
			} else {
				return null;
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			LOGGER.debug("Error fetching event type", e);
			return null;
		}

	}

	@Override
	public DtoResult saveEventTypeWithDocuments(DtoEventType dto, List<MultipartFile> files) throws IOException {
		DtoResult dtoResult = new DtoResult();
		EventType entity = null;
		if (dto.getSerEventTypeId() != null) {
			Optional<EventType> existingOptional = repositoryEventType.findById(dto.getSerEventTypeId());
			if (existingOptional.isEmpty()) {
				dtoResult.setTxtMessage("Event type not found for update.");
				return dtoResult;
			}
			entity = existingOptional.get();
		}

		// Prevent self-parenting
		if (dto.getParentEventTypeId() != null && dto.getSerEventTypeId() != null
				&& dto.getParentEventTypeId().equals(dto.getSerEventTypeId())) {
			dtoResult.setTxtMessage("An event cannot be its own parent.");
			return dtoResult;
		}

		EventType parent = null;
		if (dto.getParentEventTypeId() != null) {
			Optional<EventType> parentOptional = repositoryEventType.findById(dto.getParentEventTypeId());
			if (parentOptional.isEmpty()) {
				dtoResult.setTxtMessage("Parent Event Type not found.");
				return dtoResult;
			}
			parent = parentOptional.get();
		}

		if (entity == null) {
			entity = MapperEventType.toEntity(dto);
			entity.setParentEventType(parent);
			if(parent != null) {
				entity.setTxtEventTypeCode(generateNextEventTypeCode());
			}
			entity.setBlnIsActive(true);
			entity.setBlnIsDeleted(false);
			entity.setBlnIsApproved(true);
		} else {
			entity.setTxtEventTypeName(dto.getTxtEventTypeName());
			entity.setBlnIsMainEvent(dto.getBlnIsMainEvent());
			entity.setBlnIsActive(dto.getBlnIsActive());
			entity.setUpdatedDate(new Date());
		}

		Map<String, MultipartFile> fileMap = files.stream()
				.collect(Collectors.toMap(MultipartFile::getOriginalFilename, f -> f));

		List<EventTypeDocument> documents = new ArrayList<>();
		for (DtoEventTypeDocument docDto : dto.getDocuments()) {
			MultipartFile file = fileMap.get(docDto.getOriginalName());
			if (file != null) {
				String uploadPath = UtilFileStorage.saveFile(file, "eventTypes");
				EventTypeDocument doc = new EventTypeDocument();
				doc.setDocumentName(file.getName());
				doc.setOriginalName(file.getOriginalFilename());
				doc.setDocumentType(file.getContentType());
				doc.setSize(String.valueOf(file.getSize()));
				doc.setFilePath(uploadPath);
				doc.setEventType(entity);
				documents.add(doc);
			}
		}

		entity.setEventTypeDocuments(documents);

		EventType saved = repositoryEventType.saveAndFlush(entity);
		dtoResult.setTxtMessage("Saved successfully");
		dtoResult.setResult(saved);
		return dtoResult;
	}

	@Override
	public DtoResult deleteById(Integer id) {
		DtoResult result = new DtoResult();
		Optional<EventType> optional = repositoryEventType.findById(id);
		if (optional.isPresent()) {
			EventType e = optional.get();
			e.setBlnIsDeleted(true);
			repositoryEventType.save(e);
			result.setTxtMessage("Deleted (soft) successfully");
		} else {
			result.setTxtMessage("No record found to delete");
		}
		return result;
	}

	@Override
	public String generateNextEventTypeCode() {
		String maxCode = repositoryEventType.findMaxEventTypeCode();

		int nextNumber = 1;

		if (maxCode != null && maxCode.startsWith("ET-")) {
			try {
				String numberPart = maxCode.substring(3);
				nextNumber = Integer.parseInt(numberPart) + 1;
			} catch (NumberFormatException e) {
				nextNumber = 1;
			}
		}

		return String.format("ET-%03d", nextNumber);
	}

}
