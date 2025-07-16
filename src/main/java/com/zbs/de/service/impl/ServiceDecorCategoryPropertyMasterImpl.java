package com.zbs.de.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zbs.de.mapper.MapperDecorCategoryPropertyMaster;
import com.zbs.de.model.DecorCategoryMaster;
import com.zbs.de.model.DecorCategoryPropertyMaster;
import com.zbs.de.model.dto.DtoDecorCategoryMaster;
import com.zbs.de.model.dto.DtoDecorCategoryPropertyMaster;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryDecorCategoryPropertyMaster;
import com.zbs.de.service.ServiceDecorCategoryMaster;
import com.zbs.de.service.ServiceDecorCategoryPropertyMaster;
import com.zbs.de.service.ServiceDecorCategoryPropertyValue;
import com.zbs.de.util.UtilRandomKey;

@Service("serviceDecorCategoryPropertyMaster")
public class ServiceDecorCategoryPropertyMasterImpl implements ServiceDecorCategoryPropertyMaster {
	@Autowired
	RepositoryDecorCategoryPropertyMaster repositoryDecorCategoryPropertyMaster;

	@Autowired
	ServiceDecorCategoryMaster serviceDecorCategoryMaster;

	@Autowired
	ServiceDecorCategoryPropertyValue serviceDecorCategoryPropertyValue;

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceEventMasterImpl.class);

	@Override
	public DtoResult saveOrUpdate(DtoDecorCategoryPropertyMaster dto) {
		DecorCategoryPropertyMaster entity = MapperDecorCategoryPropertyMaster.toEntity(dto);
		repositoryDecorCategoryPropertyMaster.save(entity);
		return new DtoResult("Saved Successfully", null, MapperDecorCategoryPropertyMaster.toDto(entity), null);
	}

	@Override
	public DtoResult getAll() {
		List<DtoDecorCategoryPropertyMaster> list = repositoryDecorCategoryPropertyMaster.findAllByBlnIsDeletedFalse()
				.stream().map(MapperDecorCategoryPropertyMaster::toDto).collect(Collectors.toList());
		return new DtoResult("Fetched Successfully", null, null, new ArrayList<>(list));
	}

	@Override
	public DtoResult getById(Integer id) {
		Optional<DecorCategoryPropertyMaster> optional = repositoryDecorCategoryPropertyMaster
				.findBySerPropertyIdAndBlnIsDeletedFalse(id);
		return optional.map(value -> new DtoResult("Found", null, MapperDecorCategoryPropertyMaster.toDto(value), null))
				.orElseGet(() -> new DtoResult("Not Found", null, null, null));
	}

	@Override
	public DtoResult deleteById(Integer id) {
		Optional<DecorCategoryPropertyMaster> optional = repositoryDecorCategoryPropertyMaster
				.findBySerPropertyIdAndBlnIsDeletedFalse(id);
		if (optional.isPresent()) {
			DecorCategoryPropertyMaster entity = optional.get();
			entity.setBlnIsDeleted(true);
			repositoryDecorCategoryPropertyMaster.save(entity);
			return new DtoResult("Deleted (soft) successfully", null, null, null);
		}
		return new DtoResult("No record found to delete", null, null, null);
	}

	@Override
	public DtoResult saveWithListProperties(DtoDecorCategoryMaster dto) {
		DtoResult dtoResult = new DtoResult();
		try {
			if (UtilRandomKey.isNotNull(dto) && UtilRandomKey.isNotNull(dto.getCategoryProperties())
					&& UtilRandomKey.isNotNull(dto.getSerDecorCategoryId())) {
				DecorCategoryMaster decorCategoryMaster = serviceDecorCategoryMaster
						.getByPK(dto.getSerDecorCategoryId());
				if (UtilRandomKey.isNull(decorCategoryMaster)) {
					dtoResult.setTxtMessage("Invalid Category");
					return dtoResult;
				}
				for (DtoDecorCategoryPropertyMaster property : dto.getCategoryProperties()) {
					DecorCategoryPropertyMaster entity = new DecorCategoryPropertyMaster();
					entity.setDecorCategoryMaster(decorCategoryMaster);
					entity.setTxtPropertyName(property.getTxtPropertyName());
					entity.setTxtInputType(property.getTxtInputType());
					entity.setTxtRemarks(property.getTxtRemarks());
					entity.setBlnIsActive(property.getBlnIsActive());
					entity.setBlnIsApproved(true);
					entity.setBlnIsRequired(property.getBlnIsRequired());
					repositoryDecorCategoryPropertyMaster.save(entity);
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
	public DecorCategoryPropertyMaster getByPk(Integer id) {
		try {
			Optional<DecorCategoryPropertyMaster> optional = repositoryDecorCategoryPropertyMaster.findById(id);
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
	public DtoResult deleteByCategoryId(Integer id) {
		DtoResult dtoResult = new DtoResult();
		try {
			List<DecorCategoryPropertyMaster> propertiesLst = repositoryDecorCategoryPropertyMaster
					.findByDecorCategoryMaster_SerDecorCategoryIdAndBlnIsDeletedFalse(id);
			if (UtilRandomKey.isNotNull(propertiesLst) && !propertiesLst.isEmpty()) {
				for (DecorCategoryPropertyMaster property : propertiesLst) {
					serviceDecorCategoryPropertyValue.deleteByPropertyId(property.getSerPropertyId());
					property.setBlnIsDeleted(true);
					repositoryDecorCategoryPropertyMaster.save(property);
				}
			} else {
				dtoResult.setTxtMessage("No Property Found Against This Category");
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
