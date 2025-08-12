package com.zbs.de.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.zbs.de.controller.ControllerEventType;
import com.zbs.de.mapper.MapperDecorCategoryPropertyValue;
import com.zbs.de.model.DecorCategoryPropertyMaster;
import com.zbs.de.model.DecorCategoryPropertyValue;
import com.zbs.de.model.DecorCategoryPropertyValueDocument;
import com.zbs.de.model.dto.DtoDecorCategoryPropertyMaster;
import com.zbs.de.model.dto.DtoDecorCategoryPropertyValue;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryDecorCategoryPropertyValue;
import com.zbs.de.service.ServiceDecorCategoryPropertyMaster;
import com.zbs.de.service.ServiceDecorCategoryPropertyValue;
import com.zbs.de.service.ServiceDecorCategoryPropertyValueDocument;
import com.zbs.de.util.UtilFileStorage;
import com.zbs.de.util.UtilRandomKey;

@Service("serviceDecorCategoryPropertyValue")
public class ServiceDecorCategoryPropertyValueImpl implements ServiceDecorCategoryPropertyValue {

	@Autowired
	RepositoryDecorCategoryPropertyValue repositoryDecorCategoryPropertyValue;

	@Autowired
	@Lazy
	ServiceDecorCategoryPropertyMaster serviceDecorCategoryPropertyMaster;

	@Autowired
	ServiceDecorCategoryPropertyValueDocument serviceDecorCategoryPropertyValueDocument;

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerEventType.class);

	@Override
	public DtoResult saveOrUpdate(DtoDecorCategoryPropertyValue dto) {
		DecorCategoryPropertyValue entity = MapperDecorCategoryPropertyValue.toEntity(dto);
		repositoryDecorCategoryPropertyValue.save(entity);
		return new DtoResult("Saved Successfully", null, MapperDecorCategoryPropertyValue.toDto(entity), null);
	}

	@Override
	public DtoResult saveWithListValues(DtoDecorCategoryPropertyMaster dto) {
		DtoResult dtoResult = new DtoResult();
		try {
			if (UtilRandomKey.isNotNull(dto) && UtilRandomKey.isNotNull(dto.getPropertyValues())
					&& UtilRandomKey.isNotNull(dto.getSerPropertyId())) {
				DecorCategoryPropertyMaster decorCategoryPropertyMaster = serviceDecorCategoryPropertyMaster
						.getByPk(dto.getSerPropertyId());
				if (UtilRandomKey.isNull(decorCategoryPropertyMaster)) {
					dtoResult.setTxtMessage("Invalid Property");
					return dtoResult;
				}
				for (DtoDecorCategoryPropertyValue value : dto.getPropertyValues()) {
					DecorCategoryPropertyValue entity = new DecorCategoryPropertyValue();
					entity.setDecorCategoryProperty(decorCategoryPropertyMaster);
					entity.setTxtPropertyValue(value.getTxtPropertyValue());
					entity.setBlnIsActive(value.getBlnIsActive());
					entity.setCreatedBy(ServiceCurrentUser.getCurrentUserId());
					entity.setBlnIsApproved(true);
					repositoryDecorCategoryPropertyValue.save(entity);
				}
				dtoResult.setTxtMessage("Success");
			} else {
				dtoResult.setTxtMessage("Invalid Data");
				return dtoResult;
			}

		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			dtoResult.setTxtMessage(e.getMessage());
		}

		return dtoResult;
	}

	@Override
	public DtoResult saveListValuesWithDocuments(DtoDecorCategoryPropertyMaster dto, List<MultipartFile> files) {
		DtoResult dtoResult = new DtoResult();
		try {
			if (UtilRandomKey.isNotNull(dto) && UtilRandomKey.isNotNull(dto.getPropertyValues())
					&& UtilRandomKey.isNotNull(dto.getSerPropertyId())) {
				DecorCategoryPropertyMaster decorCategoryPropertyMaster = serviceDecorCategoryPropertyMaster
						.getByPk(dto.getSerPropertyId());
				if (UtilRandomKey.isNull(decorCategoryPropertyMaster)) {
					dtoResult.setTxtMessage("Invalid Property");
					return dtoResult;
				}

				Map<String, MultipartFile> fileMap = files.stream()
						.collect(Collectors.toMap(MultipartFile::getOriginalFilename, f -> f));

				for (DtoDecorCategoryPropertyValue value : dto.getPropertyValues()) {
					DecorCategoryPropertyValue entity = new DecorCategoryPropertyValue();
					entity.setDecorCategoryProperty(decorCategoryPropertyMaster);
					entity.setTxtPropertyValue(value.getTxtPropertyValue());
					entity.setBlnIsActive(value.getBlnIsActive());
					entity.setBlnIsApproved(true);
					entity.setCreatedBy(ServiceCurrentUser.getCurrentUserId());

					// *******Saving Document********
					if (value.getDocument() != null && value.getDocument().getOriginalName() != null) {
						MultipartFile file = fileMap.get(value.getDocument().getOriginalName());
						if (file != null) {
							String uploadPath = UtilFileStorage.saveFile(file, "propertyValue");
							DecorCategoryPropertyValueDocument doc = new DecorCategoryPropertyValueDocument();
							doc.setDocumentName(file.getName());
							doc.setOriginalName(file.getOriginalFilename());
							doc.setDocumentType(file.getContentType());
							doc.setSize(String.valueOf(file.getSize()));
							doc.setFilePath(uploadPath);
							doc = serviceDecorCategoryPropertyValueDocument.save(doc);
							entity.setDecorCategoryPropertyValueDocument(doc);
							entity.setBlnIsDocument(Boolean.TRUE);
						}
					}

					repositoryDecorCategoryPropertyValue.save(entity);
				}
				dtoResult.setTxtMessage("Success");
			} else {
				dtoResult.setTxtMessage("Invalid Data");
				return dtoResult;
			}

		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			dtoResult.setTxtMessage(e.getMessage());
		}
		return dtoResult;

	}

	@Override
	public DtoResult getAll() {
		List<DtoDecorCategoryPropertyValue> list = repositoryDecorCategoryPropertyValue.findAll().stream()
				.map(MapperDecorCategoryPropertyValue::toDto).collect(Collectors.toList());
		return new DtoResult("Fetched Successfully", null, null, new ArrayList<>(list));
	}

	@Override
	public DtoResult getByPropertyId(Integer propertyId) {
		List<DtoDecorCategoryPropertyValue> list = repositoryDecorCategoryPropertyValue
				.findByPropertyIdAndNotDeleted(propertyId).stream().map(MapperDecorCategoryPropertyValue::toDto)
				.collect(Collectors.toList());
		return new DtoResult("Fetched by PropertyId", null, null, new ArrayList<>(list));
	}

	@Override
	public DtoResult getById(Integer id) {
		Optional<DecorCategoryPropertyValue> optional = repositoryDecorCategoryPropertyValue.findById(id);
		return optional.map(value -> new DtoResult("Found", null, MapperDecorCategoryPropertyValue.toDto(value), null))
				.orElseGet(() -> new DtoResult("Not Found", null, null, null));
	}

	@Override
	public DtoResult deleteById(Integer id) {
		Optional<DecorCategoryPropertyValue> optional = repositoryDecorCategoryPropertyValue
				.findBySerPropertyValueIdAndBlnIsDeletedFalse(id);
		if (optional.isPresent()) {
			DecorCategoryPropertyValue entity = optional.get();
			entity.setBlnIsDeleted(true);
			entity.setUpdatedBy(ServiceCurrentUser.getCurrentUserId());
			repositoryDecorCategoryPropertyValue.save(entity);
			return new DtoResult("Deleted (soft) successfully", null, null, null);
		}
		return new DtoResult("No record found to delete", null, null, null);
	}

	@Override
	public DtoResult deleteByPropertyId(Integer serPropertyId) {
		DtoResult dtoResult = new DtoResult();
		try {
			List<DecorCategoryPropertyValue> decorCategoryPropertyValues = repositoryDecorCategoryPropertyValue
					.findByPropertyIdAndNotDeleted(serPropertyId);
			if (UtilRandomKey.isNotNull(decorCategoryPropertyValues) && !decorCategoryPropertyValues.isEmpty()) {
				for (DecorCategoryPropertyValue value : decorCategoryPropertyValues) {
					value.setBlnIsDeleted(false);
					value.setUpdatedBy(ServiceCurrentUser.getCurrentUserId());
					repositoryDecorCategoryPropertyValue.save(value);
				}
			} else {
				dtoResult.setTxtMessage("No Property Value Found Agains This Property.");
				return dtoResult;
			}

			dtoResult.setTxtMessage("Success");
			return dtoResult;
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			dtoResult.setTxtMessage(e.getMessage());
			return dtoResult;
		}
	}

}
