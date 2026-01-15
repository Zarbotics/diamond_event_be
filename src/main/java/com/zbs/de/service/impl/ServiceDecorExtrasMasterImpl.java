package com.zbs.de.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.zbs.de.controller.ControllerEventType;
import com.zbs.de.mapper.MapperDecorExtrasMaster;
import com.zbs.de.model.DecorExtrasMaster;
import com.zbs.de.model.DecorExtrasOption;
import com.zbs.de.model.DecorExtrasOptionDocument;
import com.zbs.de.model.dto.DtoDecorExtrasMaster;
import com.zbs.de.model.dto.DtoDecorExtrasOption;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryDecorExtrasMaster;
import com.zbs.de.repository.RepositoryDecorExtrasOption;
import com.zbs.de.service.ServiceDecorExtrasMaster;
import com.zbs.de.service.ServiceDecorExtrasOptionDocument;
import com.zbs.de.service.ServiceEventDecorExtrasSelection;
import com.zbs.de.util.UtilFileStorage;
import com.zbs.de.util.UtilRandomKey;

import jakarta.transaction.Transactional;

@Service("serviceDecorExtrasMaster")
public class ServiceDecorExtrasMasterImpl implements ServiceDecorExtrasMaster {

	@Autowired
	private RepositoryDecorExtrasMaster repositoryDecorExtrasMaster;

	@Autowired
	private RepositoryDecorExtrasOption repositoryDecorExtrasOption;
	
	@Autowired
	private ServiceEventDecorExtrasSelection serviceEventDecorExtrasSelection;

	@Autowired
	private ServiceDecorExtrasOptionDocument serviceDecorExtrasOptionDocument;

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerEventType.class);

	
	@Override
	public DtoResult saveAndUpdate(DtoDecorExtrasMaster dto) {
		DtoResult dtoResult = new DtoResult();
		try {
			DecorExtrasMaster decorExtrasMaster = null;
			if (UtilRandomKey.isNotNull(dto) && UtilRandomKey.isNotNull(dto.getSerExtrasId())) {
				decorExtrasMaster = this.getByPk(dto.getSerExtrasId());
				if (UtilRandomKey.isNull(decorExtrasMaster)) {
					dtoResult.setTxtMessage("Invalid Extras ID: " + dto.getSerExtrasId());
					return dtoResult;
				}

				List<DecorExtrasOption> existingValues = decorExtrasMaster.getDecorExtrasOptions();
				decorExtrasMaster.setDecorExtrasOptions(existingValues);
				decorExtrasMaster.setTxtExtrasCode(dto.getTxtExtrasCode());
				decorExtrasMaster.setTxtExtrasName(dto.getTxtExtrasName());
				decorExtrasMaster.setBlnIsActive(dto.getBlnIsActive());
				decorExtrasMaster.setNumPrice(dto.getNumPrice());
				decorExtrasMaster.setUpdatedBy(ServiceCurrentUser.getCurrentUserId());
			} else {
				decorExtrasMaster = MapperDecorExtrasMaster.toEntity(dto);
				decorExtrasMaster.setBlnIsActive(Boolean.TRUE);
				decorExtrasMaster.setCreatedBy(ServiceCurrentUser.getCurrentUserId());
				decorExtrasMaster.setBlnIsApproved(Boolean.TRUE);
				decorExtrasMaster.setBlnIsDeleted(Boolean.FALSE);
				decorExtrasMaster.setCreatedBy(ServiceCurrentUser.getCurrentUserId());
				decorExtrasMaster.setNumPrice(dto.getNumPrice());
			}

			repositoryDecorExtrasMaster.save(decorExtrasMaster);

			dtoResult.setTxtMessage("Success");

		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			dtoResult.setTxtMessage(e.getMessage());
		}
		return dtoResult;

	}

	@Override
	public DecorExtrasMaster getByPk(Integer id) {
		try {
			Optional<DecorExtrasMaster> optional = repositoryDecorExtrasMaster.findById(id);
			if (optional.isPresent()) {
				return optional.get();
			} else {
				return null;
			}

		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			return null;
		}

	}

//	@Override
//	@Transactional
//	public DtoResult saveWithListOptions(DtoDecorExtrasMaster dto, List<MultipartFile> files) {
//
//	    DtoResult dtoResult = new DtoResult();
//	    List<String> warningMessages = new ArrayList<>();
//
//	    try {
//	        DecorExtrasMaster decorExtrasMaster;
//
//	        /* ============================
//	           LOAD OR CREATE MASTER
//	           ============================ */
//	        if (dto.getSerExtrasId() != null) {
//	            decorExtrasMaster = this.getByPk(dto.getSerExtrasId());
//
//	            if (decorExtrasMaster == null) {
//	                dtoResult.setTxtMessage("Invalid Decor Extras Master");
//	                return dtoResult;
//	            }
//	        } else {
//	            decorExtrasMaster = MapperDecorExtrasMaster.toEntity(dto);
//	            decorExtrasMaster.setBlnIsActive(true);
//	            decorExtrasMaster.setBlnIsApproved(true);
//	            decorExtrasMaster.setCreatedBy(ServiceCurrentUser.getCurrentUserId());
//	        }
//
//	        repositoryDecorExtrasMaster.save(decorExtrasMaster);
//
//	        /* ============================
//	           INDEX EXISTING OPTIONS
//	           ============================ */
//	        Map<Integer, DecorExtrasOption> existingOptionMap =
//	                decorExtrasMaster.getDecorExtrasOptions()
//	                        .stream()
//	                        .filter(o -> o.getSerExtraOptionId() != null)
//	                        .collect(Collectors.toMap(
//	                                DecorExtrasOption::getSerExtraOptionId,
//	                                Function.identity()
//	                        ));
//
//	        /* ============================
//	           INCOMING OPTION IDS
//	           ============================ */
//	        Set<Integer> incomingIds = dto.getDecorExtrasOptions().stream()
//	                .map(DtoDecorExtrasOption::getSerExtraOptionId)
//	                .filter(Objects::nonNull)
//	                .collect(Collectors.toSet());
//
//	        /* ============================
//	           HANDLE REMOVED OPTIONS
//	           ============================ */
//	        for (DecorExtrasOption existing : existingOptionMap.values()) {
//
//	            if (!incomingIds.contains(existing.getSerExtraOptionId())) {
//
//	                boolean isUsed = serviceEventDecorExtrasSelection.existsByDecorExtrasOptionId(existing.getSerExtraOptionId());
//
//	                if (isUsed) {
//	                    existing.setBlnIsActive(false);
//	                    warningMessages.add(
//	                            "Option '" + existing.getTxtOptionName()
//	                                    + "' is already used in events and cannot be deleted."
//	                    );
//	                } else {
//	                    existing.setBlnIsDeleted(true);
//	                }
//
//	                existing.setUpdatedBy(ServiceCurrentUser.getCurrentUserId());
//	            }
//	        }
//
//	        /* ============================
//	           FILE MAP
//	           ============================ */
//	        Map<String, MultipartFile> fileMap = files == null
//	                ? Collections.emptyMap()
//	                : files.stream().collect(Collectors.toMap(
//	                        MultipartFile::getOriginalFilename,
//	                        f -> f
//	                ));
//
//	        /* ============================
//	           INSERT / UPDATE OPTIONS
//	           ============================ */
//	        List<DecorExtrasOption> finalOptionList = new ArrayList<>();
//
//	        for (DtoDecorExtrasOption dtoOption : dto.getDecorExtrasOptions()) {
//
//	            DecorExtrasOption option;
//
//	            if (dtoOption.getSerExtraOptionId() != null
//	                    && existingOptionMap.containsKey(dtoOption.getSerExtraOptionId())) {
//
//	                /* UPDATE */
//	                option = existingOptionMap.get(dtoOption.getSerExtraOptionId());
//	                option.setTxtOptionCode(dtoOption.getTxtOptionCode());
//	                option.setTxtOptionName(dtoOption.getTxtOptionName());
//	                option.setBlnIsActive(true);
//	                option.setBlnIsDeleted(false);
//	                option.setUpdatedBy(ServiceCurrentUser.getCurrentUserId());
//
//	            } else {
//
//	                /* INSERT */
//	                option = new DecorExtrasOption();
//	                option.setTxtOptionCode(dtoOption.getTxtOptionCode());
//	                option.setTxtOptionName(dtoOption.getTxtOptionName());
//	                option.setDecorExtrasMaster(decorExtrasMaster);
//	                option.setBlnIsActive(true);
//	                option.setBlnIsApproved(true);
//	                option.setCreatedBy(ServiceCurrentUser.getCurrentUserId());
//	            }
//
//	            /* ============================
//	               DOCUMENT HANDLING
//	               ============================ */
//	            if (dtoOption.getDocument() != null
//	                    && dtoOption.getDocument().getOriginalName() != null) {
//
//	                MultipartFile file =
//	                        fileMap.get(dtoOption.getDocument().getOriginalName());
//
//	                if (file != null) {
//	                    String uploadPath = UtilFileStorage.saveFile(file, "extrasOption");
//
//	                    DecorExtrasOptionDocument doc = new DecorExtrasOptionDocument();
//	                    doc.setDocumentName(file.getName());
//	                    doc.setOriginalName(file.getOriginalFilename());
//	                    doc.setDocumentType(file.getContentType());
//	                    doc.setSize(String.valueOf(file.getSize()));
//	                    doc.setFilePath(uploadPath);
//
//	                    doc = serviceDecorExtrasOptionDocument.save(doc);
//
//	                    option.setDocument(doc);
//	                    option.setBlnIsDocument(true);
//	                }
//	            }
//
//	            finalOptionList.add(option);
//	        }
//
//	        /* ============================
//	           FINAL SAVE
//	           ============================ */
//	        decorExtrasMaster.setDecorExtrasOptions(finalOptionList);
//	        repositoryDecorExtrasMaster.save(decorExtrasMaster);
//
//	        /* ============================
//	           RESULT MESSAGE
//	           ============================ */
//	        if (!warningMessages.isEmpty()) {
//	            dtoResult.setTxtMessage(
//	                    "Saved with warnings: " + String.join(" | ", warningMessages)
//	            );
//	        } else {
//	            dtoResult.setTxtMessage("Success");
//	        }
//
//	    } catch (Exception e) {
//	        LOGGER.error("Error saving Decor Extras", e);
//	        dtoResult.setTxtMessage("Operation failed: " + e.getMessage());
//	    }
//
//	    return dtoResult;
//	}

	@Override
	@Transactional
	public DtoResult saveWithListOptions(DtoDecorExtrasMaster dto, List<MultipartFile> files) {

	    DtoResult result = new DtoResult();
	    List<String> warnings = new ArrayList<>();

	    try {
	        DecorExtrasMaster master;

	        /* =========================
	           LOAD OR CREATE MASTER
	           ========================= */
	        if (dto.getSerExtrasId() != null) {
	            master = this.getByPk(dto.getSerExtrasId());
	            if (master == null) {
	                result.setTxtMessage("Invalid Decor Extras Master");
	                return result;
	            }

	            master.setTxtExtrasCode(dto.getTxtExtrasCode());
	            master.setTxtExtrasName(dto.getTxtExtrasName());
	            master.setNumPrice(dto.getNumPrice());
	            master.setBlnIsActive(dto.getBlnIsActive());
	            master.setUpdatedBy(ServiceCurrentUser.getCurrentUserId());

	        } else {
	            master = MapperDecorExtrasMaster.toEntity(dto);
	            master.setBlnIsActive(true);
	            master.setBlnIsApproved(true);
	            master.setCreatedBy(ServiceCurrentUser.getCurrentUserId());
	        }

	        /* =========================
	           EXISTING OPTIONS (BEFORE SAVE)
	           ========================= */
	        List<DecorExtrasOption> managedOptions = master.getDecorExtrasOptions();
	        if (managedOptions == null) {
	            managedOptions = new ArrayList<>();
	            master.setDecorExtrasOptions(managedOptions);
	        }

	        /* CRITICAL: Capture IDs that existed BEFORE this save */
	        Set<Integer> preExistingIds =
	                managedOptions.stream()
	                        .map(DecorExtrasOption::getSerExtraOptionId)
	                        .filter(Objects::nonNull)
	                        .collect(Collectors.toSet());

	        repositoryDecorExtrasMaster.save(master);

	        /* =========================
	           EXISTING MAP (PRE-EXISTING ONLY)
	           ========================= */
	        Map<Integer, DecorExtrasOption> existingMap =
	                managedOptions.stream()
	                        .filter(o -> o.getSerExtraOptionId() != null
	                                && preExistingIds.contains(o.getSerExtraOptionId()))
	                        .collect(Collectors.toMap(
	                                DecorExtrasOption::getSerExtraOptionId,
	                                Function.identity()
	                        ));

	        /* =========================
	           INCOMING IDS (DTO EXISTING ONLY)
	           ========================= */
	        Set<Integer> incomingIds =
	                dto.getDecorExtrasOptions().stream()
	                        .map(DtoDecorExtrasOption::getSerExtraOptionId)
	                        .filter(Objects::nonNull)
	                        .collect(Collectors.toSet());

	        /* =========================
	           FILE MAP
	           ========================= */
	        Map<String, MultipartFile> fileMap =
	                files == null
	                        ? Map.of()
	                        : files.stream().collect(Collectors.toMap(
	                                MultipartFile::getOriginalFilename,
	                                Function.identity()
	                        ));

	        /* =========================
	           INSERT / UPDATE OPTIONS
	           ========================= */
	        for (DtoDecorExtrasOption dtoOption : dto.getDecorExtrasOptions()) {

	            DecorExtrasOption option;

	            if (dtoOption.getSerExtraOptionId() != null
	                    && existingMap.containsKey(dtoOption.getSerExtraOptionId())) {

	                /* UPDATE */
	                option = existingMap.get(dtoOption.getSerExtraOptionId());
	                option.setUpdatedBy(ServiceCurrentUser.getCurrentUserId());

	            } else {
	                /* INSERT (NEW OPTION) */
	                option = new DecorExtrasOption();
	                option.setDecorExtrasMaster(master);
	                option.setCreatedBy(ServiceCurrentUser.getCurrentUserId());
	                option.setBlnIsApproved(true);
	                option.setBlnIsActive(true);
	                option.setBlnIsDeleted(false);
	                managedOptions.add(option);
	            }

	            option.setTxtOptionCode(dtoOption.getTxtOptionCode());
	            option.setTxtOptionName(dtoOption.getTxtOptionName());
	            option.setBlnIsActive(true);
	            option.setBlnIsDeleted(false);

	            /* =========================
	               DOCUMENT HANDLING
	               ========================= */
	            if (dtoOption.getDocument() != null
	                    && dtoOption.getDocument().getOriginalName() != null) {

	                MultipartFile file =
	                        fileMap.get(dtoOption.getDocument().getOriginalName());

	                if (file != null) {
	                    String path = UtilFileStorage.saveFile(file, "extrasOption");

	                    DecorExtrasOptionDocument doc = new DecorExtrasOptionDocument();
	                    doc.setOriginalName(file.getOriginalFilename());
	                    doc.setDocumentName(file.getName());
	                    doc.setDocumentType(file.getContentType());
	                    doc.setSize(String.valueOf(file.getSize()));
	                    doc.setFilePath(path);

	                    doc = serviceDecorExtrasOptionDocument.save(doc);
	                    option.setDocument(doc);
	                    option.setBlnIsDocument(true);
	                }
	            }
	        }

	        /* =========================
	           SOFT REMOVALS (PRE-EXISTING ONLY)
	           ========================= */
	        for (DecorExtrasOption existing : managedOptions) {

	            Integer id = existing.getSerExtraOptionId();

	            if (id != null
	                    && preExistingIds.contains(id)
	                    && !incomingIds.contains(id)) {

	                boolean isUsed =
	                        serviceEventDecorExtrasSelection
	                                .existsByDecorExtrasOptionId(id);

	                if (isUsed) {
	                    existing.setBlnIsActive(false);
	                    warnings.add(
	                            "Option '" + existing.getTxtOptionName()
	                                    + "' is already used and cannot be removed."
	                    );
	                } else {
	                    existing.setBlnIsDeleted(true);
	                }

	                existing.setUpdatedBy(ServiceCurrentUser.getCurrentUserId());
	            }
	        }

	        repositoryDecorExtrasMaster.save(master);

	        result.setTxtMessage(
	                warnings.isEmpty()
	                        ? "Success"
	                        : "Saved with warnings: " + String.join(" | ", warnings)
	        );

	    } catch (Exception e) {
	        LOGGER.error("Decor Extras save failed", e);
	        result.setTxtMessage("Failed: " + e.getMessage());
	    }

	    return result;
	}



	@Override
	public DtoResult getAll() {
		DtoResult dtoResult = new DtoResult();
		try {
			List<DecorExtrasMaster> extras = repositoryDecorExtrasMaster.findAllWithOptionsWhereNotDeleted();
			List<DtoDecorExtrasMaster> dtoList = extras.stream().map(MapperDecorExtrasMaster::toDto)
					.collect(Collectors.toList());
			dtoResult.setTxtMessage("Success");
			dtoResult.setResult(dtoList);
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			dtoResult.setTxtMessage("Failed to retrieve extras");
		}
		return dtoResult;
	}
	
	

	@Override
	public List<DecorExtrasMaster> getAllDecorExtrasMaster() {
		try {
			List<DecorExtrasMaster> extras = repositoryDecorExtrasMaster.findAllWithOptionsWhereNotDeleted();
			return extras;
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			return null;
		}
	}

	@Override
	public DtoResult getById(Integer id) {
		DtoResult dtoResult = new DtoResult();
		try {
			DecorExtrasMaster entity = this.getByPk(id);
			if (entity == null) {
				dtoResult.setTxtMessage("No Extras found for ID: " + id);
				return dtoResult;
			}
			dtoResult.setResult(MapperDecorExtrasMaster.toDto(entity));
			dtoResult.setTxtMessage("Success");
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			dtoResult.setTxtMessage("Failed to retrieve extras");
		}
		return dtoResult;
	}

	@Override
	public DtoResult deleteById(Integer id) {
		DtoResult dtoResult = new DtoResult();
		try {
			Optional<DecorExtrasMaster> optional = repositoryDecorExtrasMaster.findById(id);
			if (optional.isPresent()) {
				DecorExtrasMaster entity = optional.get();

				if (entity.getDecorExtrasOptions() != null && !entity.getDecorExtrasOptions().isEmpty()) {
					for (DecorExtrasOption option : entity.getDecorExtrasOptions()) {
						option.setBlnIsDeleted(true);
						option.setUpdatedBy(ServiceCurrentUser.getCurrentUserId());
						repositoryDecorExtrasOption.save(option);
					}
				}
				entity.setBlnIsDeleted(true);
				entity.setUpdatedBy(ServiceCurrentUser.getCurrentUserId());
				repositoryDecorExtrasMaster.save(entity);
				dtoResult.setTxtMessage("Deleted successfully");
			} else {
				dtoResult.setTxtMessage("Extras not found with ID: " + id);
			}
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			dtoResult.setTxtMessage("Error deleting extras");
		}
		return dtoResult;
	}

	@Override
	public DtoResult deleteExtrasOptionById(Integer id) {
		DtoResult dtoResult = new DtoResult();
		try {
			Optional<DecorExtrasOption> optional = repositoryDecorExtrasOption.findById(id);
			if (optional.isPresent()) {
				DecorExtrasOption entity = optional.get();
				entity.setBlnIsDeleted(true);
				entity.setUpdatedBy(ServiceCurrentUser.getCurrentUserId());
				repositoryDecorExtrasOption.save(entity);
				dtoResult.setTxtMessage("Deleted successfully");
			} else {
				dtoResult.setTxtMessage("Extras Option not found with ID: " + id);
			}
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			dtoResult.setTxtMessage("Error deleting DecorExtrasOption");
		}
		return dtoResult;
	}

	@Override
	public DecorExtrasMaster getByIdAndNotDeleted(Integer id) {
		try {
			return repositoryDecorExtrasMaster.findByIdAndNotDeleted(id).orElse(null);
		} catch (Exception e) {
			LOGGER.debug("Failed to get DecorExtrasMaster by ID {}: {}", id, e.getMessage());
			return null;
		}
	}

	
	@Override
	public DtoResult getAllActive() {
		DtoResult dtoResult = new DtoResult();
		try {
			List<DecorExtrasMaster> extras = repositoryDecorExtrasMaster.findAllActiveWithOptionsWhereNotDeleted();
			List<DtoDecorExtrasMaster> dtoList = extras.stream().map(MapperDecorExtrasMaster::toDto)
					.collect(Collectors.toList());
			dtoResult.setTxtMessage("Success");
			dtoResult.setResult(dtoList);
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			dtoResult.setTxtMessage("Failed to retrieve extras");
		}
		return dtoResult;
	}
	
	@Override
	public String generateExtrasCode() {
		String maxCode = repositoryDecorExtrasMaster.findMaxExtrasCode();

		int nextNumber = 1;

		if (maxCode != null && maxCode.startsWith("EXT-")) {
			try {
				String numberPart = maxCode.substring(4);
				nextNumber = Integer.parseInt(numberPart) + 1;
			} catch (NumberFormatException e) {
				nextNumber = 1;
			}
		}

		return String.format("EXT-%03d", nextNumber);
	}

}
