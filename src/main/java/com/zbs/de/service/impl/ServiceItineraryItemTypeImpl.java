package com.zbs.de.service.impl;

import com.zbs.de.model.ItineraryItemType;
import com.zbs.de.model.dto.DtoItineraryItemType;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryItineraryItemType;
import com.zbs.de.service.ServiceItineraryItemType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceItineraryItemTypeImpl implements ServiceItineraryItemType {

	@Autowired
	private RepositoryItineraryItemType repository;

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceEventMasterImpl.class);

	@Override
	public DtoResult create(DtoItineraryItemType dto) {

		try {
			ItineraryItemType entity = new ItineraryItemType();
			entity.setTxtCode(dto.getTxtCode());
			entity.setTxtName(dto.getTxtName());
			entity.setBlnIsActive(dto.getBlnIsActive() != null ? dto.getBlnIsActive() : true);
			entity.setBlnIsDeleted(false);
			entity.setBlnIsApproved(true);
			repository.save(entity);
			dto.setSerItineraryItemTypeId(entity.getSerItineraryItemTypeId());
			return new DtoResult("Saved Successfully.", null, dto, null);

		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			return new DtoResult(e.getMessage(), null, null, null);
		}

	}

	@Override
	public DtoResult update(DtoItineraryItemType dto) {
		try {
			ItineraryItemType entity = repository.findById(dto.getSerItineraryItemTypeId()).orElse(null);

			if (entity != null) {
				entity.setTxtCode(dto.getTxtCode());
				entity.setTxtName(dto.getTxtName());
				entity.setBlnIsActive(dto.getBlnIsActive());
				repository.save(entity);
				return new DtoResult("Updated Successfully.", null, convertToDto(entity), null);
			} else {
				return new DtoResult("ItineraryItemType Not Found.", null, null, null);
			}
		} catch (Exception e) {
			LOGGER.error("Error in update ItineraryItemType", e);
			return new DtoResult(e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult getAll() {
		try {
			List<DtoItineraryItemType> list = repository.findByBlnIsDeletedFalseOrderBySerItineraryItemTypeIdDesc().stream().map(this::convertToDto)
					.collect(Collectors.toList());
			return new DtoResult("Fetched Successfully.", null, list, null);
		} catch (Exception e) {
			LOGGER.error("Error in getAll ItineraryItemType", e);
			return new DtoResult(e.getMessage(), null, null, null);
		}
	}
	
	@Override
	public List<ItineraryItemType> findAll() {
		try {
			List<ItineraryItemType> list = repository.findByBlnIsDeletedFalseOrderBySerItineraryItemTypeIdDesc();
			return list;
		} catch (Exception e) {
			LOGGER.error("Error in getAll ItineraryItemType", e);
			return new ArrayList<>();
		}
	}

	@Override
	public DtoResult getAllActive() {
		try {
			List<DtoItineraryItemType> list = repository.findByBlnIsActiveTrueAndBlnIsDeletedFalseOrderBySerItineraryItemTypeIdDesc().stream().map(this::convertToDto)
					.collect(Collectors.toList());
			return new DtoResult("Fetched Successfully.", null, list, null);
		} catch (Exception e) {
			LOGGER.error("Error in getAllActive ItineraryItemType", e);
			return new DtoResult(e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult getById(Integer id) {
		try {
			ItineraryItemType entity = repository.findById(id).orElse(null);
			if (entity != null) {
				return new DtoResult("Fetched Successfully.", null, convertToDto(entity), null);
			} else {
				return new DtoResult("ItineraryItemType Not Found.", null, null, null);
			}
		} catch (Exception e) {
			LOGGER.error("Error in getById ItineraryItemType", e);
			return new DtoResult(e.getMessage(), null, null, null);
		}
	}

	@Override
	public ItineraryItemType getItineraryItemTypeById(Integer Id) {
		try {
			ItineraryItemType entity = repository.findById(Id).orElse(null);
			if (entity != null) {
				return entity;
			} else {
				return null;
			}
		} catch (Exception e) {
			LOGGER.debug("Error in getById ItineraryItemType", e);
			return null;
		}
	}

	@Override
	public DtoResult generateNextCode() {
	    try {
	        Integer maxNumber = repository.findMaxCodeNumber();
	        int nextNumber = (maxNumber != null) ? maxNumber + 1 : 1;

	        if (nextNumber > 999) {  // Limit for 3-digit code
	            return new DtoResult("Maximum code limit reached (IIT-999)", null, null, null);
	        }

	        String nextCode = String.format("IIT-%03d", nextNumber);
	        return new DtoResult("Next code generated successfully", null, nextCode, null);

	    } catch (Exception e) {
	        e.printStackTrace();
	        return new DtoResult("Failed to generate next code: " + e.getMessage(), null, null, null);
	    }
	}
	
	private DtoItineraryItemType convertToDto(ItineraryItemType entity) {
		DtoItineraryItemType dto = new DtoItineraryItemType();
		dto.setSerItineraryItemTypeId(entity.getSerItineraryItemTypeId());
		dto.setTxtCode(entity.getTxtCode());
		dto.setTxtName(entity.getTxtName());
		dto.setBlnIsActive(entity.getBlnIsActive());
		return dto;
	}
}
