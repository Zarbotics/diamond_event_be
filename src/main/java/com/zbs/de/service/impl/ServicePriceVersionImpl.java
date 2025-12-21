package com.zbs.de.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zbs.de.model.dto.DtoPriceVersion;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.mapper.MapperPriceVersion;
import com.zbs.de.model.PriceVersion;
import com.zbs.de.repository.RepositoryPriceVersion;
import com.zbs.de.service.ServicePriceVersion;

@Service
@Transactional
public class ServicePriceVersionImpl implements ServicePriceVersion {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServicePriceVersionImpl.class);

	@Autowired
	private RepositoryPriceVersion repository;

	@Autowired
	private MapperPriceVersion mapper;

	@Override
	public DtoResult create(DtoPriceVersion dto) {
		try {
			// Generate version code if not provided
			if (dto.getTxtVersionCode() == null || dto.getTxtVersionCode().trim().isEmpty()) {
				dto.setTxtVersionCode(generateVersionCode());
			}

			// Check if code already exists
			Optional<PriceVersion> existing = repository
					.findByTxtVersionCodeAndBlnIsDeletedFalse(dto.getTxtVersionCode());
			if (existing.isPresent()) {
				return new DtoResult("Price version code already exists: " + dto.getTxtVersionCode(), null, null, null);
			}

			// If setting as default, unset current default
			if (Boolean.TRUE.equals(dto.getBlnIsDefault())) {
				repository.findByBlnIsDefaultTrueAndBlnIsActiveTrueAndBlnIsDeletedFalse().ifPresent(currentDefault -> {
					currentDefault.setBlnIsDefault(false);
					repository.save(currentDefault);
				});
			}

			// Create entity
			PriceVersion entity = mapper.toEntity(dto);
			entity.setBlnIsDeleted(false);
			entity.setBlnIsActive(dto.getBlnIsActive() != null ? dto.getBlnIsActive() : true);

			repository.save(entity);

			return new DtoResult("Price version created successfully.", null, mapper.toDto(entity), null);

		} catch (Exception e) {
			LOGGER.error("Error creating price version", e);
			return new DtoResult("Failed to create price version: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult update(DtoPriceVersion dto) {
		try {
			PriceVersion entity = repository.findById(dto.getSerPriceVersionId()).orElseThrow(
					() -> new RuntimeException("Price version not found with ID: " + dto.getSerPriceVersionId()));

			if (Boolean.TRUE.equals(entity.getBlnIsDeleted())) {
				return new DtoResult("Price version is deleted and cannot be updated.", null, null, null);
			}

			// Check if code is being changed and if it already exists
			if (dto.getTxtVersionCode() != null && !dto.getTxtVersionCode().equals(entity.getTxtVersionCode())) {
				Optional<PriceVersion> existing = repository
						.findByTxtVersionCodeAndBlnIsDeletedFalse(dto.getTxtVersionCode());
				if (existing.isPresent()
						&& !existing.get().getSerPriceVersionId().equals(entity.getSerPriceVersionId())) {
					return new DtoResult("Price version code already exists: " + dto.getTxtVersionCode(), null, null,
							null);
				}
			}

			// If setting as default, unset current default
			if (Boolean.TRUE.equals(dto.getBlnIsDefault()) && !Boolean.TRUE.equals(entity.getBlnIsDefault())) {
				repository.findByBlnIsDefaultTrueAndBlnIsActiveTrueAndBlnIsDeletedFalse().ifPresent(currentDefault -> {
					if (!currentDefault.getSerPriceVersionId().equals(entity.getSerPriceVersionId())) {
						currentDefault.setBlnIsDefault(false);
						repository.save(currentDefault);
					}
				});
			}

			// Update entity
			mapper.updateEntityFromDto(dto, entity);
			repository.save(entity);

			return new DtoResult("Price version updated successfully.", null, mapper.toDto(entity), null);

		} catch (Exception e) {
			LOGGER.error("Error updating price version", e);
			return new DtoResult("Failed to update price version: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult delete(Long id) {
		try {
			PriceVersion entity = repository.findById(id)
					.orElseThrow(() -> new RuntimeException("Price version not found with ID: " + id));

			if (Boolean.TRUE.equals(entity.getBlnIsDeleted())) {
				return new DtoResult("Price version is already deleted.", null, null, null);
			}

			// Check if this is the default version
			if (Boolean.TRUE.equals(entity.getBlnIsDefault())) {
				return new DtoResult("Cannot delete default price version. Set another version as default first.", null,
						null, null);
			}

			// Soft delete
			entity.setBlnIsDeleted(true);
			entity.setBlnIsActive(false);
			repository.save(entity);

			return new DtoResult("Price version deleted successfully.", null, null, null);

		} catch (Exception e) {
			LOGGER.error("Error deleting price version", e);
			return new DtoResult("Failed to delete price version: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult getById(Long id) {
		try {
			PriceVersion entity = repository.findById(id)
					.orElseThrow(() -> new RuntimeException("Price version not found with ID: " + id));

			if (Boolean.TRUE.equals(entity.getBlnIsDeleted())) {
				return new DtoResult("Price version is deleted.", null, null, null);
			}

			return new DtoResult("Fetched successfully.", null, mapper.toDto(entity), null);

		} catch (Exception e) {
			LOGGER.error("Error fetching price version by ID", e);
			return new DtoResult("Failed to fetch price version: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult getAll() {
		try {
			List<DtoPriceVersion> list = mapper
					.toDtoList(repository.findByBlnIsDeletedFalseOrderByNumPriorityDescSerPriceVersionIdDesc());
			return new DtoResult("Fetched successfully.", null, list, null);

		} catch (Exception e) {
			LOGGER.error("Error fetching all price versions", e);
			return new DtoResult("Failed to fetch price versions: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult getAllActive() {
		try {
			List<DtoPriceVersion> list = mapper.toDtoList(
					repository.findByBlnIsActiveTrueAndBlnIsDeletedFalseOrderByNumPriorityDescSerPriceVersionIdDesc());
			return new DtoResult("Fetched successfully.", null, list, null);

		} catch (Exception e) {
			LOGGER.error("Error fetching active price versions", e);
			return new DtoResult("Failed to fetch active price versions: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult getDefault() {
		try {
			Optional<PriceVersion> entity = repository.findByBlnIsDefaultTrueAndBlnIsActiveTrueAndBlnIsDeletedFalse();

			if (entity.isPresent()) {
				return new DtoResult("Fetched successfully.", null, mapper.toDto(entity.get()), null);
			} else {
				return new DtoResult("No default price version found.", null, null, null);
			}

		} catch (Exception e) {
			LOGGER.error("Error fetching default price version", e);
			return new DtoResult("Failed to fetch default price version: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult activate(Long id) {
		try {
			PriceVersion entity = repository.findById(id)
					.orElseThrow(() -> new RuntimeException("Price version not found with ID: " + id));

			if (Boolean.TRUE.equals(entity.getBlnIsDeleted())) {
				return new DtoResult("Price version is deleted.", null, null, null);
			}

			entity.setBlnIsActive(true);
			repository.save(entity);

			return new DtoResult("Price version activated successfully.", null, mapper.toDto(entity), null);

		} catch (Exception e) {
			LOGGER.error("Error activating price version", e);
			return new DtoResult("Failed to activate price version: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult deactivate(Long id) {
		try {
			PriceVersion entity = repository.findById(id)
					.orElseThrow(() -> new RuntimeException("Price version not found with ID: " + id));

			if (Boolean.TRUE.equals(entity.getBlnIsDeleted())) {
				return new DtoResult("Price version is deleted.", null, null, null);
			}

			// Check if this is the default version
			if (Boolean.TRUE.equals(entity.getBlnIsDefault())) {
				return new DtoResult("Cannot deactivate default price version. Set another version as default first.",
						null, null, null);
			}

			entity.setBlnIsActive(false);
			repository.save(entity);

			return new DtoResult("Price version deactivated successfully.", null, mapper.toDto(entity), null);

		} catch (Exception e) {
			LOGGER.error("Error deactivating price version", e);
			return new DtoResult("Failed to deactivate price version: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult setAsDefault(Long id) {
		try {
			PriceVersion entity = repository.findById(id)
					.orElseThrow(() -> new RuntimeException("Price version not found with ID: " + id));

			if (Boolean.TRUE.equals(entity.getBlnIsDeleted())) {
				return new DtoResult("Price version is deleted.", null, null, null);
			}

			if (!Boolean.TRUE.equals(entity.getBlnIsActive())) {
				return new DtoResult("Cannot set inactive price version as default.", null, null, null);
			}

			// Unset current default
			repository.findByBlnIsDefaultTrueAndBlnIsActiveTrueAndBlnIsDeletedFalse().ifPresent(currentDefault -> {
				if (!currentDefault.getSerPriceVersionId().equals(entity.getSerPriceVersionId())) {
					currentDefault.setBlnIsDefault(false);
					repository.save(currentDefault);
				}
			});

			// Set new default
			entity.setBlnIsDefault(true);
			repository.save(entity);

			return new DtoResult("Price version set as default successfully.", null, mapper.toDto(entity), null);

		} catch (Exception e) {
			LOGGER.error("Error setting price version as default", e);
			return new DtoResult("Failed to set price version as default: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult duplicate(Long sourceId, String newName) {
		try {
			PriceVersion source = repository.findById(sourceId)
					.orElseThrow(() -> new RuntimeException("Source price version not found with ID: " + sourceId));

			if (Boolean.TRUE.equals(source.getBlnIsDeleted())) {
				return new DtoResult("Source price version is deleted.", null, null, null);
			}

			// Create duplicate
			PriceVersion duplicate = new PriceVersion();
			duplicate.setTxtVersionCode(generateVersionCode());
			duplicate.setTxtName(newName != null ? newName : source.getTxtName() + " (Copy)");
			duplicate.setTxtDescription(source.getTxtDescription());
			duplicate.setDteEffectiveFrom(source.getDteEffectiveFrom());
			duplicate.setDteEffectiveTo(source.getDteEffectiveTo());
			duplicate.setBlnIsActive(true);
			duplicate.setBlnIsDefault(false);
			duplicate.setNumPriority(source.getNumPriority() + 1); // Lower priority than original
			duplicate.setMetadata(source.getMetadata());
			duplicate.setBlnIsDeleted(false);

			repository.save(duplicate);

			return new DtoResult("Price version duplicated successfully.", null, mapper.toDto(duplicate), null);

		} catch (Exception e) {
			LOGGER.error("Error duplicating price version", e);
			return new DtoResult("Failed to duplicate price version: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult getActiveForDate(Date date) {
		try {
			List<PriceVersion> entities = repository.findActiveForDate(date != null ? date : new Date());
			List<DtoPriceVersion> dtos = mapper.toDtoList(entities);

			return new DtoResult("Fetched successfully.", null, dtos, null);

		} catch (Exception e) {
			LOGGER.error("Error fetching active price versions for date", e);
			return new DtoResult("Failed to fetch active price versions: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public DtoResult getByCode(String versionCode) {
		try {
			Optional<PriceVersion> entity = repository.findByTxtVersionCodeAndBlnIsDeletedFalse(versionCode);

			if (entity.isPresent()) {
				return new DtoResult("Fetched successfully.", null, mapper.toDto(entity.get()), null);
			} else {
				return new DtoResult("Price version not found with code: " + versionCode, null, null, null);
			}

		} catch (Exception e) {
			LOGGER.error("Error fetching price version by code", e);
			return new DtoResult("Failed to fetch price version: " + e.getMessage(), null, null, null);
		}
	}

	@Override
	public PriceVersion getPriceVersionEntityById(Long id) {
		try {
			return repository.findById(id)
					.orElseThrow(() -> new RuntimeException("Price version not found with ID: " + id));
		} catch (Exception e) {
			LOGGER.error("Error fetching price version entity", e);
			throw new RuntimeException("Failed to fetch price version entity: " + e.getMessage());
		}
	}

	@Override
	public String generateVersionCode() {
		try {
			Optional<Long> maxCodeNumber = repository.findMaxVersionCodeNumber();
			long nextNumber = maxCodeNumber.orElse(1000L) + 1;
			return String.format("PV-%04d", nextNumber);
		} catch (Exception e) {
			LOGGER.error("Error generating version code", e);
			// Fallback
			return "PV-" + System.currentTimeMillis() % 10000;
		}
	}

	@Override
	public PriceVersion getActivePriceVersionForDate(Date date) {
		try {
			List<PriceVersion> activeVersions = repository.findActiveForDate(date != null ? date : new Date());

			if (!activeVersions.isEmpty()) {
				// Return highest priority version
				return activeVersions.get(0);
			}

			// Fallback to default version
			return repository.findByBlnIsDefaultTrueAndBlnIsActiveTrueAndBlnIsDeletedFalse()
					.orElseThrow(() -> new RuntimeException("No active price version found for date"));

		} catch (Exception e) {
			LOGGER.error("Error getting active price version for date", e);
			throw new RuntimeException("Failed to get active price version: " + e.getMessage());
		}
	}
}
