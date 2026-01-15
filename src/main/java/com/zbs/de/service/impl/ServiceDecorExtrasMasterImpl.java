package com.zbs.de.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.zbs.de.controller.ControllerEventType;
import com.zbs.de.mapper.MapperDecorExtrasMaster;
import com.zbs.de.model.DecorCategoryPropertyValue;
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
import com.zbs.de.util.UtilFileStorage;
import com.zbs.de.util.UtilRandomKey;

@Service("serviceDecorExtrasMaster")
public class ServiceDecorExtrasMasterImpl implements ServiceDecorExtrasMaster {

	@Autowired
	private RepositoryDecorExtrasMaster repositoryDecorExtrasMaster;

	@Autowired
	private RepositoryDecorExtrasOption repositoryDecorExtrasOption;

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

	@Override
	public DtoResult saveWithListOptions(DtoDecorExtrasMaster dto, List<MultipartFile> files) {
		DtoResult dtoResult = new DtoResult();
		try {
			DecorExtrasMaster decorExtrasMaster = null;
			if (UtilRandomKey.isNotNull(dto) && UtilRandomKey.isNotNull(dto.getDecorExtrasOptions())
					&& UtilRandomKey.isNotNull(dto.getSerExtrasId())) {
				decorExtrasMaster = this.getByPk(dto.getSerExtrasId());
				if (UtilRandomKey.isNull(decorExtrasMaster)) {
					dtoResult.setTxtMessage("Invalid Property");
					return dtoResult;
				} else {
					decorExtrasMaster.getDecorExtrasOptions().clear();
				}
			} else {
				decorExtrasMaster = MapperDecorExtrasMaster.toEntity(dto);
				decorExtrasMaster.setBlnIsActive(Boolean.TRUE);
				decorExtrasMaster.setBlnIsApproved(Boolean.TRUE);
			}
			repositoryDecorExtrasMaster.save(decorExtrasMaster);

			Map<String, MultipartFile> fileMap = files.stream()
					.collect(Collectors.toMap(MultipartFile::getOriginalFilename, f -> f));
			List<DecorExtrasOption> optionList = new ArrayList<>();
			for (DtoDecorExtrasOption value : dto.getDecorExtrasOptions()) {
				DecorExtrasOption decorExtrasOption = new DecorExtrasOption();
				decorExtrasOption.setSerExtraOptionId(value.getSerExtraOptionId());
				decorExtrasOption.setTxtOptionCode(value.getTxtOptionCode());
				decorExtrasOption.setTxtOptionName(value.getTxtOptionName());
				decorExtrasOption.setBlnIsActive(Boolean.valueOf(true));
				decorExtrasOption.setDecorExtrasMaster(decorExtrasMaster);
				if (value.getSerExtraOptionId() != null) {
					decorExtrasOption.setUpdatedBy(ServiceCurrentUser.getCurrentUserId());
				} else {
					decorExtrasOption.setCreatedBy(ServiceCurrentUser.getCurrentUserId());

				}
				decorExtrasOption.setBlnIsApproved(true);

				// *******Saving Document********
				if (value.getDocument() != null && value.getDocument().getOriginalName() != null) {
					MultipartFile file = fileMap.get(value.getDocument().getOriginalName());
					if (file != null) {
						String uploadPath = UtilFileStorage.saveFile(file, "extrasOption");
						DecorExtrasOptionDocument doc = new DecorExtrasOptionDocument();
						doc.setDocumentName(file.getName());
						doc.setOriginalName(file.getOriginalFilename());
						doc.setDocumentType(file.getContentType());
						doc.setSize(String.valueOf(file.getSize()));
						doc.setFilePath(uploadPath);
						doc = serviceDecorExtrasOptionDocument.save(doc);
						decorExtrasOption.setDocument(doc);
						decorExtrasOption.setBlnIsDocument(Boolean.TRUE);
					}
				}

				optionList.add(decorExtrasOption);
			}
			decorExtrasMaster.setDecorExtrasOptions(optionList);
			repositoryDecorExtrasMaster.save(decorExtrasMaster);

			dtoResult.setTxtMessage("Success");

		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			dtoResult.setTxtMessage(e.getMessage());
		}
		return dtoResult;

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
