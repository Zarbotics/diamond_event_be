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
import com.zbs.de.repository.RepositoryEventType;
import com.zbs.de.util.ResponseMessage;
import com.zbs.de.util.UtilFileStorage;
import com.zbs.de.util.UtilRandomKey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

	@Override
	public List<DtoEventType> getAllData() {
		List<EventType> list = repositoryEventType.findByBlnIsDeleted(false);
		List<DtoEventType> dtos = new ArrayList<>();
		for (EventType type : list) {
			dtos.add(MapperEventType.toDto(type));
		}
		return dtos;
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
		return dtos;
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
				entity.setBlnIsActive(true);
				entity.setBlnIsDeleted(false);
				entity.setBlnIsApproved(true);
			} else {
				entity.setTxtEventTypeName(dto.getTxtEventTypeName());
				entity.setBlnIsMainEvent(dto.getBlnIsMainEvent());
				entity.setBlnIsActive(dto.getBlnIsActive());
				entity.setUpdatedDate(new Date());
			}

			EventType saved = repositoryEventType.saveAndFlush(entity);
			res.setMessage("Saved successfully");
			res.setResult(saved);

		} catch (Exception e) {
			LOGGER.error("Error saving event type", e);
			res.setMessage("Unexpected error occurred while saving event type.");
		}
		return res;
	}

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

}
