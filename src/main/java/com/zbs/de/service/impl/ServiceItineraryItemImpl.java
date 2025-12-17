package com.zbs.de.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zbs.de.mapper.MapperItinerary;
import com.zbs.de.model.ItineraryItem;
import com.zbs.de.model.ItineraryItemType;
import com.zbs.de.model.dto.DtoItineraryItem;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryItineraryItem;
import com.zbs.de.service.ServiceItineraryItem;
import com.zbs.de.service.ServiceItineraryItemType;

@Service("serviceItineraryItemImpl")
public class ServiceItineraryItemImpl implements ServiceItineraryItem {

	@Autowired
	private RepositoryItineraryItem repo;

	@Autowired
	private ServiceItineraryItemType serviceItineraryItemType;

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceEventMasterImpl.class);

	@Override
	public DtoResult create(DtoItineraryItem dto) {
		try {
			ItineraryItem it = MapperItinerary.toEntity(dto);

			// Set ItineraryItemType
			if (dto.getSerItineraryItemTypeId() != null) {
				ItineraryItemType type = serviceItineraryItemType
						.getItineraryItemTypeById(dto.getSerItineraryItemTypeId());
				if (type == null) {
					return new DtoResult("Invalid Item Type.", null, null, null);
				} else {
					it.setItineraryItemType(type);
				}
			}

			// Generate txtCode if not provided
			if (dto.getTxtCode() == null || dto.getTxtCode().isEmpty()) {
				dto.setTxtCode(generateNextCodeInternal());
				it.setTxtCode(dto.getTxtCode());
			}

			if (dto.getBlnIsActive() == null) {
				it.setBlnIsActive(true);
			} else {
				it.setBlnIsActive(dto.getBlnIsActive());
			}
			it.setBlnIsDeleted(false);
			it.setBlnIsApproved(true);

			repo.save(it);
			return new DtoResult("Saved successfully", null, MapperItinerary.toDto(it), null);

		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			return new DtoResult("Failed to create: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult update(DtoItineraryItem dto) {
		try {
			ItineraryItem exist = repo.getById(dto.getSerItineraryItemId());
			if (exist == null) {
				return new DtoResult("Record Not Found.", null, null, null);
			}

			exist.setTxtName(dto.getTxtName());
			exist.setMetadata(dto.getMetadata());

			// Set ItineraryItemType
			if (dto.getSerItineraryItemTypeId() != null) {
				ItineraryItemType type = serviceItineraryItemType
						.getItineraryItemTypeById(dto.getSerItineraryItemTypeId());
				if (type == null) {
					return new DtoResult("Invalid Item Type.", null, null, null);
				} else {
					exist.setItineraryItemType(type);
				}
			}

			if (dto.getBlnIsActive() != null) {
				exist.setBlnIsActive(dto.getBlnIsActive());
			}

			repo.save(exist);
			return new DtoResult("Updated successfully", null, MapperItinerary.toDto(exist), null);

		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			return new DtoResult("Failed to update: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult getById(Long id) {
		try {
			ItineraryItem exist = repo.getById(id);
			return new DtoResult("Fetched successfully", null, MapperItinerary.toDto(exist), null);
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			return new DtoResult("Failed to fetch: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult getAll() {
		try {
			List<DtoItineraryItem> list = repo.findByBlnIsDeletedFalseOrderBySerItineraryItemIdDesc().stream()
					.map(MapperItinerary::toDto).collect(Collectors.toList());
			return new DtoResult("Fetched successfully", null, list, null);
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			return new DtoResult("Failed to fetch: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult getAllActive() {
		try {
			List<DtoItineraryItem> list = repo.findByBlnIsActiveTrueAndBlnIsDeletedFalseOrderBySerItineraryItemIdDesc()
					.stream().map(MapperItinerary::toDto).collect(Collectors.toList());
			return new DtoResult("Fetched successfully", null, list, null);
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			return new DtoResult("Failed to fetch active items: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult generateNextCode() {
		try {
			Integer maxNumber = repo.findMaxCodeNumber();
			int nextNumber = (maxNumber != null) ? maxNumber + 1 : 1;

			if (nextNumber > 999) { // Limit for 3-digit code
				return new DtoResult("Maximum code limit reached (ITI-999)", null, null, null);
			}

			String nextCode = String.format("ITI-%03d", nextNumber);
			return new DtoResult("Next code generated successfully", null, nextCode, null);

		} catch (Exception e) {
			e.printStackTrace();
			return new DtoResult("Failed to generate next code: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public ItineraryItem getItineraryItemById(Long id) {
		try {
			ItineraryItem exist = repo.getById(id);
			return exist;
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			return null;
		}
	}

	@Override
	public DtoResult getAllActiveItineraryItemsByType(Integer typeId) {
		try {
			if (typeId == null) {
				return new DtoResult("Type ID cannot be null", null, null, null);
			}

			List<DtoItineraryItem> list = repo.findAllActiveByType(typeId).stream().map(MapperItinerary::toDto)
					.collect(Collectors.toList());

			if (list.isEmpty()) {
				return new DtoResult("No active items found for this type", null, list, null);
			}

			return new DtoResult("Fetched active items successfully", null, list, null);

		} catch (Exception e) {
			return new DtoResult("Failed to fetch active items: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult getAllItineraryItemsByType(Integer typeId) {
		try {
			if (typeId == null) {
				return new DtoResult("Type ID cannot be null", null, null, null);
			}

			List<DtoItineraryItem> list = repo.findAllByType(typeId).stream().map(MapperItinerary::toDto)
					.collect(Collectors.toList());

			if (list.isEmpty()) {
				return new DtoResult("No items found for this type", null, list, null);
			}

			return new DtoResult("Fetched items successfully", null, list, null);

		} catch (Exception e) {
			return new DtoResult("Failed to fetch items: " + e.getMessage(), null, null, null);
		}
	}

	private String generateNextCodeInternal() {
		DtoResult dtoResult = this.generateNextCode();

		if (dtoResult != null && dtoResult.getResult() != null) {
			return dtoResult.getResult().toString();
		} else {
			return null;
		}
	}

}
