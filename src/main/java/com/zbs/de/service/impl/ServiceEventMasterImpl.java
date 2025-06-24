package com.zbs.de.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zbs.de.mapper.MapperEventType;
import com.zbs.de.model.EventType;
import com.zbs.de.model.dto.DtoEventType;
import com.zbs.de.repository.RepositoryEventMaster;
import com.zbs.de.repository.RepositoryEventType;
import com.zbs.de.service.ServiceEventMaster;
import com.zbs.de.util.ResponseMessage;

@Service("serviceEventMaster")
public class ServiceEventMasterImpl implements ServiceEventMaster {
	
	@Autowired
	private RepositoryEventMaster repositoryEventMaster;

//	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceEventTypeImpl.class);
//
//	@Autowired
//	RepositoryEventType repositoryEventType;
//
//	@Override
//	public List<DtoEventType> getAllData() {
//		List<EventType> list = repositoryEventType.findByBlnIsDeleted(false);
//		List<DtoEventType> dtos = new ArrayList<>();
//		for (EventType type : list) {
//			dtos.add(MapperEventType.toDto(type));
//		}
//		return dtos;
//	}
//
//	@Override
//	public ResponseMessage saveAndUpdate(DtoEventType dto) {
//		ResponseMessage res = new ResponseMessage();
//		try {
//			// Prevent self-parenting
//			if (dto.getParentEventTypeId() != null && dto.getSerEventTypeId() != null
//					&& dto.getParentEventTypeId().equals(dto.getSerEventTypeId())) {
//				res.setMessage("An event cannot be its own parent.");
//				return res;
//			}
//
//			EventType parent = null;
//			if (dto.getParentEventTypeId() != null) {
//				Optional<EventType> parentOptional = repositoryEventType.findById(dto.getParentEventTypeId());
//				if (parentOptional.isEmpty()) {
//					res.setMessage("Parent Event Type not found.");
//					return res;
//				}
//				parent = parentOptional.get();
//			}
//
//			EventType entity = MapperEventType.toEntity(dto);
//			entity.setParentEventType(parent);
//			entity.setBlnIsActive(true);
//			entity.setBlnIsDeleted(false);
//			entity.setBlnIsApproved(true);
//
//			EventType saved = repositoryEventType.saveAndFlush(entity);
//			res.setMessage("Saved successfully");
//			res.setResult(saved);
//
//		} catch (Exception e) {
//			LOGGER.error("Error saving event type", e);
//			res.setMessage("Unexpected error occurred while saving event type.");
//		}
//		return res;
//	}
//
//	@Override
//	public ResponseMessage getById(Integer id) {
//		ResponseMessage res = new ResponseMessage();
//		try {
//			Optional<EventType> optional = repositoryEventType.findById(id);
//			if (optional.isPresent()) {
//				res.setMessage("Record fetched successfully");
//				res.setResult(MapperEventType.toDto(optional.get()));
//			} else {
//				res.setMessage("Record not found");
//			}
//		} catch (Exception e) {
//			LOGGER.error("Error fetching event type", e);
//			res.setMessage(e.getMessage());
//		}
//		return res;
//	}
	
	
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
